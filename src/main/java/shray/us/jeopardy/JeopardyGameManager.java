package shray.us.jeopardy;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import com.google.gson.Gson;
import eu.decentsoftware.holograms.api.DHAPI;
import eu.decentsoftware.holograms.api.holograms.Hologram;
import org.bukkit.*;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.checkerframework.checker.units.qual.A;

public class JeopardyGameManager { // 12 13 6 - // 12 7 -4
    private JeopardyHost host;
    private ArrayList<JeopardyContestant> contestants;
    private boolean started = false;
    private GameBoard game_board;
    private JeopardyGame game;
    private String current_round;
    private JeopardyClue current_clue;
    private boolean finished_reading = false;
    private JeopardyContestant buzzed_in = null;
    private boolean[] accepting_responses = new boolean[3];
    private Hologram[] buzzer_timers = new Hologram[3];
    private int[] wagers = new int[3];
    private String[] final_jeopardy_responses = new String[3];

    ConsoleCommandSender console = Bukkit.getServer().getConsoleSender();
    private Logger logger = Logger.getLogger("Jeopardy");

    public JeopardyGameManager(Player sender, String[] args) {
        // args[1]: game file name (without extension)
        // args[2]: host name
        // args[3]: contestant #1 name
        // args[4]: contestant #2 name
        // args[5]: contestant #3 name
        if (args.length < 3) {
            sender.sendMessage(ChatColor.RED + "Jepoardy requires 1 host and 2-3 contestants.\nUsage: /jeopardy create <game name> <host> <contestant 1> <contestant 2> ...");
            return;
        }
        Gson gson = new Gson();
        try (Reader reader = new FileReader(Paths.get(Jeopardy.getInstance().getDataFolder().toString(), "games", args[1] + ".json").toString())) {
            game = gson.fromJson(reader, JeopardyGame.class); // converting json file to object

            // Now you can use the 'game' object however you need
            //logger.info(game.get_categories(round_name).get("0").get_name()); // For example

        } catch (Exception e) {
            logger.info(e.toString());
            sender.sendMessage(ChatColor.RED + "Something went wrong when trying to load " + args[1] + ".json!");
        }
        game_board = new GameBoard(sender.getWorld(), 12, 13, 6, 12, 7, -4); // hardcoded
        host = new JeopardyHost(Bukkit.getPlayerExact(args[2]), game);
        host.get_player().getInventory().clear();
        host.get_player().setGameMode(GameMode.ADVENTURE);
        host.give_host_menu();
        host.get_player().addPotionEffect(new PotionEffect(PotionEffectType.SATURATION, 999999, 255, true, false));
        contestants = new ArrayList<JeopardyContestant>();
        for (int i = 3; i < args.length; i++) {
            Player contestant = Bukkit.getPlayerExact(args[i]);
            if (contestant == null) {
                sender.sendMessage(ChatColor.RED + "Couldn't find the player \"" + args[i] + "\"");
                return;
            }
            contestant.getInventory().clear();
            contestant.setGameMode(GameMode.ADVENTURE);
            contestant.addPotionEffect(new PotionEffect(PotionEffectType.SATURATION, 999999, 255, true, false));

            int j = i - 3;
            Hologram timer = DHAPI.getHologram("jeopardy_contestant_timer_" + j);
            if (timer == null)
                timer = DHAPI.createHologram("jeopardy_contestant_timer_" + j, new Location(game_board.getWorld(), 0, 0, 0));
            DHAPI.moveHologram(timer, new Location(game_board.getWorld(), 22.5, 10, 5.5 - 4*j)); // hardcoded
            buzzer_timers[j] = timer;
            JeopardyContestant cont = new JeopardyContestant(contestant);
            cont.give_buzzer();
            cont.get_player().sendMessage(ChatColor.GREEN + "Welcome to Jeopardy! Pick up your signaling device and get ready...");
            contestants.add(cont);
            accepting_responses[j] = true;

            ItemStack money_changer = Utils.get_head(cont.get_player());
            ItemMeta meta = money_changer.getItemMeta();
            meta.setLore(Arrays.asList("Left click - give money (correct)", "Right click - take money (incorrect)", "Player index - " + j));
            money_changer.setItemMeta(meta);
            host.get_player().getInventory().addItem(money_changer);
        }

        sender.sendMessage(ChatColor.GREEN + "Created a game with host " + ChatColor.YELLOW + host.get_player().getName() + ChatColor.GREEN + ". Use " + ChatColor.RESET + "/jeopardy start intro" + ChatColor.GREEN + " to begin!");
    }

    public void init() {
        game_board.black_out();
    }

    public void start() {
        start(false);
    }
    public void start(boolean intro) {
        started = true;
        game_board.power_on();
        // play cutscene
        if (intro) {
            started = false; // we need this to be false so that we can run some commands during the intro, like showoff with player heads
            host.get_player().addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, 999999, 255, true, false));
            for (Player plr : Bukkit.getOnlinePlayers()) {
                Bukkit.dispatchCommand(console, "cinematic play intro " + plr.getName());
            }
        }
        // runnable
        new BukkitRunnable() {
            @Override
            public void run() {
                for (int i = 0; i < contestants.size(); i++) {
                    int money = contestants.get(i).get_money();
                    String money_text = "";
                    if (money < 0) {
                        money_text = ChatColor.RED + "-$" + Math.abs(money);
                    } else {
                        money_text = "$" + money;
                    }
                    game_board.set_money_display(i, money_text);
                }
            }
        }.runTaskTimer(Jeopardy.getInstance(), 0L, 20L);
        // set the started field after 45 seconds
        int[] time_left = {45};
        new BukkitRunnable() {
            @Override
            public void run() {
                started = true;
                host.get_player().removePotionEffect(PotionEffectType.INVISIBILITY);
                host.get_player().sendMessage(ChatColor.BLUE + "Intro is over.");
                cancel();
            }
        }.runTaskTimer(Jeopardy.getInstance(), time_left[0] * 20L, 20L);
        // give the host an indicator as to how much time they have left
        new BukkitRunnable() {
            @Override
            public void run() {
                host.get_player().sendMessage(ChatColor.YELLOW + "" + time_left[0] + ChatColor.BLUE + " seconds left in intro");
                time_left[0]--;
                if (time_left[0] <= 0 || started) cancel();
            }
        }.runTaskTimer(Jeopardy.getInstance(), 0L, 20L);
    }
    public void load(String round_name) {
        if (round_name.equalsIgnoreCase("final") || round_name.equalsIgnoreCase("tiebreaker"))
            game_board.power_on(); // to clear the tiles
        game_board.fill_board(round_name);
        current_round = round_name;
        if (current_round.equalsIgnoreCase("single") || current_round.equalsIgnoreCase("tiebreaker"))
            set_wall_color("blue");
        else if (current_round.equalsIgnoreCase("double"))
            set_wall_color("purple");
    }

    public void reveal_category(String idx) {
        if (current_round.equalsIgnoreCase("single") || current_round.equalsIgnoreCase("double")) {
            game_board.set_cat_holo(idx, game.get_categories(current_round).get(idx).get_name());
        } else { // final and tiebreaker
            reveal_clue(idx, "0");
        }
    }

    public void reveal_clue(String cat_idx, String clue_idx) { // wager is only used for daily double
        JeopardyClue clue = game.get_clue(current_round, cat_idx, clue_idx);
        // first set everything
        finished_reading = false;
        current_clue = clue;
        // blank out the tile
        game_board.set_tile(Integer.parseInt(cat_idx), Integer.parseInt(clue_idx)/(200 * (current_round.equals("double") ? 2 : 1)), "blank"); // we do not subtract 1 from the clue_idx because 0 is the category names row

        if (current_round.equalsIgnoreCase("final") || current_round.equalsIgnoreCase("tiebreaker")) {
            if (!(clue.cat_revealed)) {
                // reveal category and play reveal sound
                game_board.set_contestant_cat_holo(game.get_category(current_round).get_name());
                game_board.set_contestant_clue_holo(" ");
                for (Player plr : Bukkit.getOnlinePlayers()) {
                    plr.playSound(plr.getLocation(), "jeopardy.board.clue_reveal", 1.0F, 1.0F);
                }
                clue.cat_revealed = true;
                current_clue = null;
                host.get_player().sendMessage(ChatColor.BLUE + "Set their wagers using " + ChatColor.YELLOW + "/jeopady host setwager <name> <wager>");
                return;
            } else {
                // they'll be responding without buzzing in, so don't let anyone buzz in
                accepting_responses[0] = false;
                accepting_responses[1] = false;
                accepting_responses[2] = false;
                buzzed_in = new JeopardyContestant(); // set a dummy contestant to make the game think someone is buzzed in
                clue.set_value(0);
                //host.get_player().sendMessage(ChatColor.BLUE + "After revealing answers, run " + ChatColor.YELLOW + "/jeopardy host addmoney <player index 0-2> <+/-wager>");
                for (Player plr : Bukkit.getOnlinePlayers()) {
                    plr.playSound(plr.getLocation(), "jeopardy.board.clue_reveal", 1.0F, 1.0F);
                }
                game_board.set_contestant_cat_holo(game.get_category(current_round).get_name());
                game_board.set_contestant_clue_holo(current_clue.toString());
                current_clue.set_revealed(true);
                host.get_player().sendMessage(ChatColor.BLUE + "[Clue]: " + ChatColor.RESET + clue.toString() + ChatColor.RED + "\n[Response]: " + ChatColor.RESET + clue.get_acceptable_responses());
                return;
            }
        } else if (clue.daily_double) {
            if (!(clue.cat_revealed)) {
                // play dd animation
                game_board.set_contestant_cat_holo(game.get_categories(current_round).get(cat_idx).get_name());
                game_board.set_contestant_clue_holo("&uDAILY DOUBLE&r");
                for (Player plr : Bukkit.getOnlinePlayers()) {
                    plr.playSound(plr.getLocation(), "jeopardy.board.daily_double", 1.0F, 1.0F);
                }
                clue.cat_revealed = true;
                current_clue = null;
                host.get_player().sendMessage(ChatColor.BLUE + "Set their wager using " + ChatColor.YELLOW + "/jeopady host setwager <name> <wager>");
                return;
            } else {
                // they'll be responding without buzzing in, so don't let anyone buzz in
                accepting_responses[0] = false;
                accepting_responses[1] = false;
                accepting_responses[2] = false;
                buzzed_in = new JeopardyContestant(); // set a dummy contestant to make the game think someone is buzzed in
                clue.set_value(0);
                //host.get_player().sendMessage(ChatColor.BLUE + "After they answer, run " + ChatColor.YELLOW + "/jeopardy host addmoney <player index 0-2> <+/-wager>");
                // then continue
            }
        } else {
            // normal clue
            accepting_responses[0] = true;
            accepting_responses[1] = true;
            accepting_responses[2] = true;
            current_clue.set_value(Integer.parseInt(clue_idx));
            // then continue
        }
        game_board.set_contestant_cat_holo(game.get_categories(current_round).get(cat_idx).get_name());
        game_board.set_contestant_clue_holo(current_clue.toString());
        current_clue.set_revealed(true);
        host.get_player().sendMessage(ChatColor.BLUE + "[Clue]: " + ChatColor.RESET + clue.toString() + ChatColor.RED + "\n[Response]: " + ChatColor.RESET + clue.get_acceptable_responses());
    }

    public void hide_clue() {
        game_board.set_contestant_cat_holo("");
        game_board.set_contestant_clue_holo("");
        try {
            autosave();
        } catch (Exception e) {}
    }

    // change their money based on the correctness of their response
    public void declare_correctness(int idx, boolean correct) {
        if (!started) {  // use player heads as showoff instead during the intro
            if (!correct) {
                align_players();
            } else {
                align_players();
                show_off(contestants.get(idx).get_player().getName());
            }
            return;
        }
        if (current_clue == null) return;
        int addend = current_clue.get_value();
        if (current_round.equalsIgnoreCase("final") || current_clue.daily_double)
            addend = wagers[idx];
        contestants.get(idx).add_money(addend * (correct ? 1 : -1));

        String name = contestants.get(idx).get_player().getName();
        if (addend * (correct ? 1 : -1) >= 0)
            host.get_player().sendMessage(ChatColor.GREEN + "Added $" + addend + " to " + ChatColor.YELLOW + name);
        else
            host.get_player().sendMessage(ChatColor.RED + "Removed $" + Math.abs(addend) + " from " + ChatColor.YELLOW + name);

        if (current_round.equalsIgnoreCase("final")) return; // stop here for final jeopardy
        buzzed_in = null;
        accepting_responses[idx] = false; // incorrect respondents cannot retry
        if (correct) {
            // reset everything for a new clue
            hide_clue();
            current_clue = null;
            finished_reading = false;
            accepting_responses[0] = true;
            accepting_responses[1] = true;
            accepting_responses[2] = true;
        }
    }
    // change their money by a value (pos or neg) (HOST USER-FACING COMMAND)
    public void change_contestent_money(String name, int change) {
        for (JeopardyContestant c : contestants) {
            if (c.get_player().getName().equalsIgnoreCase(name)) {
                c.add_money(change);
                if (change >= 0)
                    host.get_player().sendMessage(ChatColor.GREEN + "Added $" + change + " to " + ChatColor.YELLOW + name);
                else
                    host.get_player().sendMessage(ChatColor.RED + "Removed $" + Math.abs(change) + " from " + ChatColor.YELLOW + name);
                return;
            }
        }
    }
    // unbuzz a player. maybe buzzed in when they shouldn't be able to
    public void dismiss_buzzed_in() {
        buzzed_in = null;
    }
    // nobody answered in time
    public void clue_timed_out() {
        buzzed_in = null;
        hide_clue();
        current_clue = null;
        finished_reading = false;
        accepting_responses[0] = true;
        accepting_responses[1] = true;
        accepting_responses[2] = true;
        for (Player plr : Bukkit.getOnlinePlayers()) {
            plr.playSound(plr.getLocation(), "jeopardy.game.times_up", 1.0F, 1.0F);
        }
    }

    public void set_finished_reading() {
        finished_reading = !finished_reading;
        if (!finished_reading)
            host.get_player().sendMessage(ChatColor.BLUE + "Responding is now " + ChatColor.RED + "disabled");
    }

    public int can_buzz_in(JeopardyContestant contestant) {
        if (buzzed_in != null || !accepting_responses[contestants.indexOf(contestant)]) {
            return 0;
        } else if (current_clue == null || !finished_reading) {
            return 1;
        } else {
            return 2;
        }
    }

    public void buzz_in(Player sender) {
        for (int i = 0; i < contestants.size(); i++) {
            JeopardyContestant contestant = contestants.get(i);
            if (contestant.get_player().getUniqueId().equals(sender.getUniqueId())) {
                int idx = contestants.indexOf(contestant);
                int buzz_status = can_buzz_in(contestant);
                if (buzz_status == 0) {
                    //sender.sendMessage(ChatColor.RED + "You cannot buzz in right now.");
                } else if (buzz_status == 1) {
                    sender.sendMessage(ChatColor.RED + "Responses are not being accepted right now!");
                    // time them out for 0.25 seconds
                    accepting_responses[idx] = false;
                    new BukkitRunnable() { @Override public void run() {accepting_responses[idx] = true;cancel();}}.runTaskTimer(Jeopardy.getInstance(), 5L, 20L);
                } else {
                    buzzed_in = contestants.get(i);
                    sender.sendMessage(ChatColor.GREEN + "You're up!");
                    host.get_player().sendMessage(ChatColor.YELLOW + sender.getName() + ChatColor.BLUE + " buzzed in!");
                    sender.playSound(sender.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0F, 1.0F);
                    int[] plr_idx = {i};
                    int[] time_left = {7};
                    DHAPI.setHologramLines(buzzer_timers[plr_idx[0]], Arrays.asList(ChatColor.RED + new String(new char[time_left[0]]).replace("\0", "█")));
                    //int[] ticks_passed = {0};
                    new BukkitRunnable() {
                        @Override public void run() {
                            time_left[0]--;
                            DHAPI.setHologramLines(buzzer_timers[plr_idx[0]], Arrays.asList(ChatColor.RED + new String(new char[time_left[0]]).replace("\0", "█")));
                            if (buzzed_in == null || !(buzzed_in.get_player().getUniqueId().equals(contestant.get_player().getUniqueId()))) {
                                DHAPI.setHologramLines(buzzer_timers[plr_idx[0]], Arrays.asList(""));
                                cancel();
                            }
                            if (time_left[0] <= 0) {
                                //buzzed_in = null; // do not reset, the host needs to mark the player incorrect/correct
                                cancel();
                            }
                        }
                    }.runTaskTimer(Jeopardy.getInstance(), 0L, 20L);
                }
                return;
            }
        };
    }

    public void host_open_menu() {
        host.open_menu(current_round);
    }

    public void host_click_menu(InventoryClickEvent event) {host.click_menu_item(event);}

    public void give_book(String player_name, String book_name, int slot) {
        if (player_name.equalsIgnoreCase("*")) {
            for (JeopardyContestant c : contestants) {
                give_book(c.get_player().getName(), book_name, slot);
            }
        }

        for (JeopardyContestant c : contestants) {
            if (c.get_player().getName().equalsIgnoreCase(player_name)) {
                ItemStack book = new ItemStack(Material.WRITABLE_BOOK);
                ItemMeta meta = book.getItemMeta();
                meta.setDisplayName(ChatColor.RESET + "" + ChatColor.YELLOW + c.get_player().getName() + "'s " + book_name);
                book.setItemMeta(meta);
                c.get_player().getInventory().setItem(slot, book);
                break;
            }
        }
    }
    public void take_book(String player_name, int slot) {
        if (player_name.equalsIgnoreCase("*")) {
            for (JeopardyContestant c : contestants) {
                take_book(c.get_player().getName(), slot);
            }
        }

        for (JeopardyContestant c : contestants) {
            if (c.get_player().getName().equalsIgnoreCase(player_name)) {
                ItemStack book = c.get_player().getInventory().getItem(slot);
                if (book.getType() != Material.WRITABLE_BOOK && book.getType() != Material.WRITTEN_BOOK) {
                    host.get_player().sendMessage(ChatColor.RED + "Couldn't get a book from " + player_name);
                    break;
                }
                if (slot == 2) { // fj response
                    BookMeta meta2 = (BookMeta)(book.getItemMeta());
                    String response = meta2.getPage(1);
                    int idx = contestants.indexOf(c);
                    final_jeopardy_responses[idx] = response;
                }
                host.get_player().getInventory().addItem(book);
                c.get_player().getInventory().setItem(slot, new ItemStack(Material.AIR));
                break;
            }
        }
    }

    public void final_jeopardy_timer() {
        for (Player plr : Bukkit.getOnlinePlayers()) {
            plr.playSound(plr.getLocation(), "jeopardy.music.think", 1.0F, 1.0F);
        }
        set_wall_color("red");
        new BukkitRunnable() {
            @Override public void run() {
                set_wall_color("blue");
                hide_clue();
                cancel();
            }
        }.runTaskTimer(Jeopardy.getInstance(), 20*30L, 20L);
    }

    public void set_wall_color(String color) {
        // pos1: 52 -2 -50
        // pos2: 1 43 52
        // //replace blue_terracotta,purple_terracotta,red_terracotta blue_terracotta
        Bukkit.dispatchCommand(console, "/world world");
        Bukkit.dispatchCommand(console, "/pos1 52,-2,-50");
        Bukkit.dispatchCommand(console, "/pos2 1,43,52");
        Bukkit.dispatchCommand(console, "/replace blue_terracotta,purple_terracotta,red_terracotta " + color + "_terracotta");
    }

    // set wager (HOST USER-FACING COMMAND)
    public void set_wager(String name, int wager) {
        for (JeopardyContestant c : contestants) {
            if (c.get_player().getName().equalsIgnoreCase(name)) {
                int idx = contestants.indexOf(c);
                wagers[idx] = wager;
                host.get_player().sendMessage(ChatColor.BLUE + "Set " + ChatColor.YELLOW + c.get_player().getName() + ChatColor.BLUE + "'s wager to " + ChatColor.GREEN + "$" + wager);
                return;
            }
        }
        host.get_player().sendMessage(ChatColor.RED + "Couldn't find that player.");
    }

    public ArrayList<JeopardyContestant> get_contestants() {
        return contestants;
    }
    public JeopardyHost get_host() { return host; }

    public void reveal_final_response(String arg) {
        int idx = Integer.parseInt(arg);
        Player player = contestants.get(idx).get_player();
        for (Player plr : Bukkit.getOnlinePlayers()) {
            plr.sendMessage(ChatColor.YELLOW + player.getName() + ChatColor.BLUE + "'s Response:\n"+ChatColor.RESET + final_jeopardy_responses[idx]);
        }
    }
    public void reveal_final_wager(String arg) {
        int idx = Integer.parseInt(arg);
        Player player = contestants.get(idx).get_player();
        for (Player plr : Bukkit.getOnlinePlayers()) {
            plr.sendMessage(ChatColor.YELLOW + player.getName() + ChatColor.BLUE + "'s Wager:\n"+ChatColor.RESET + "$" + wagers[idx]);
        }
    }

    public void align_players() {
        ArrayList<Hologram> money_displays = game_board.get_money_displays();
        for (JeopardyContestant c : contestants) {
            int idx = contestants.indexOf(c);
            Location money_loc = money_displays.get(idx).getLocation();
            Location c_loc =  new Location(game_board.getWorld(), money_loc.getX() + 2, money_loc.getY() + 0, money_loc.getZ() + 0, 90, 0);
            c.get_player().teleport(c_loc);
        }
    }

    public void show_off(String username) {
        JeopardyContestant contestant = null;
        for (JeopardyContestant c : contestants) {
            if (c.get_player().getName().equalsIgnoreCase(username)) {
                contestant = c;
                break;
            }
        }
        if (contestant == null) return;
        Location money_loc = game_board.get_money_displays().get(1).getLocation();
        contestant.get_player().teleport(new Location(game_board.getWorld(), money_loc.getX() - 3, money_loc.getY(), money_loc.getZ(), -90, 0));
    }

    public void set_contestant(String idx, String username) {
        for (Player plr : Bukkit.getOnlinePlayers()) {
            if (plr.getName().equalsIgnoreCase(username)) {
                contestants.get(Integer.parseInt(idx)).set_player(plr);
                break;
            }
        }
    }

    private void autosave() {
        String save_string = current_round + "," + host.get_player().getName() + ",";
        for (int i = 0; i < contestants.size(); i++) {
            save_string += contestants.get(i).get_player().getName() + "," + contestants.get(i).get_money() + ",";
        }
        Map<String, JeopardyCategory> cats = game.get_categories(current_round);
        for (int i = 0; i < cats.size(); i++) {
            for (int j = 1; j <= 5; j++) {
                save_string += cats.get(String.valueOf(i)).get_clue(String.valueOf(j * 200 * (current_round.equalsIgnoreCase("double") ? 2 : 1))).revealed + ",";
            }
        }
        try (FileWriter myWriter = new FileWriter(Paths.get(Jeopardy.getInstance().getDataFolder().toString(), "autosaves", "latest.txt").toString())) {
            myWriter.write(save_string);
            myWriter.close();
        } catch (IOException e) {
            logger.info(e.toString());
        }
    }

    public void load_autosave() {
        String val = "";
        try {
            val = new String(Files.readAllBytes(Paths.get(Jeopardy.getInstance().getDataFolder().toString(), "autosaves", "latest.txt")), StandardCharsets.UTF_8);
        } catch (IOException e) {
            logger.info(e.toString());
        }
        String[] vals = val.split(",");
        start(false);
        load(vals[0]);
        new BukkitRunnable() {
            @Override public void run() {
                host.set_player(Bukkit.getPlayerExact(vals[1]));
                contestants.get(0).set_player(Bukkit.getPlayerExact(vals[2]));
                contestants.get(0).set_money(Integer.parseInt(vals[3]));
                contestants.get(1).set_player(Bukkit.getPlayerExact(vals[4]));
                contestants.get(1).set_money(Integer.parseInt(vals[5]));
                contestants.get(2).set_player(Bukkit.getPlayerExact(vals[6]));
                contestants.get(2).set_money(Integer.parseInt(vals[7]));
                align_players();
                for (int i = 8; i < 38; i++) {
                    reveal_category(String.valueOf((i - 8) / 5));
                    if (Boolean.parseBoolean(vals[i])) { // revealed or not
                        reveal_clue(String.valueOf((i - 8) / 5), String.valueOf(((i - 8) % 5 + 1) * 200 * (vals[0].equalsIgnoreCase("double") ? 2 : 1)));
                        reveal_clue(String.valueOf((i - 8) / 5), String.valueOf(((i - 8) % 5 + 1) * 200 * (vals[0].equalsIgnoreCase("double") ? 2 : 1))); // do it twice in case it's a daily double
                    }
                }
                clue_timed_out();
                cancel();
            }
        }.runTaskTimer(Jeopardy.getInstance(), 5 * 20L, 20*9999L);
    }
}
