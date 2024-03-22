package shray.us.jeopardy;

import java.io.FileReader;
import java.io.Reader;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;

import com.google.gson.Gson;
import eu.decentsoftware.holograms.api.DHAPI;
import eu.decentsoftware.holograms.api.holograms.Hologram;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.checkerframework.checker.units.qual.A;

public class JeopardyGameManager { // 12 13 6 - // 12 7 -4
    private Player host;
    private ArrayList<JeopardyContestant> contestants;
    private boolean started = false;
    private GameBoard game_board;
    private JeopardyGame game;
    private String current_round;
    private JeopardyClue current_clue;
    private boolean finished_reading = false;
    private JeopardyContestant buzzed_in = null;
    private boolean[] accepting_responses = new boolean[3];
    Hologram[] buzzer_timers = new Hologram[3];
    Logger logger = Logger.getLogger("Jeopardy");


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
        host = Bukkit.getPlayerExact(args[2]);
        contestants = new ArrayList<JeopardyContestant>();
        for (int i = 3; i < args.length; i++) {
            Player contestant = Bukkit.getPlayerExact(args[i]);
            if (contestant == null) {
                sender.sendMessage(ChatColor.RED + "Couldn't find the player \"" + args[i] + "\"");
                return;
            }
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
        }
        sender.sendMessage(ChatColor.GREEN + "Created a game. Use /jeopardy start to begin!");
    }

    public void init() {
        game_board.black_out();
    }
    public void start() {
        started = true;
        game_board.power_on();
        // play cutscene
        //
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
    }
    public void load(String round_name) {
        game_board.fill_board(round_name);
        current_round = round_name;
    }

    public void reveal_category(String idx) {
        game_board.set_cat_holo(idx, game.get_categories(current_round).get(idx).get_name());
    }
    public void reveal_clue(String cat_idx, String clue_idx) {
        current_clue = game.get_clue(current_round, cat_idx, clue_idx);
        current_clue.set_value(Integer.parseInt(clue_idx));
        game_board.set_contestant_cat_holo(game.get_categories(current_round).get(cat_idx).get_name());
        game_board.set_contestant_clue_holo(current_clue.toString());
        game_board.set_tile(Integer.parseInt(cat_idx), Integer.parseInt(clue_idx)/200 * (current_round.equals("double") ? 2 : 1), "blank"); // we do not subtract 1 from the clue_idx because 0 is the category names row
    }

    public void hide_clue() {
        game_board.set_contestant_cat_holo("");
        game_board.set_contestant_clue_holo("");
    }

    // change their money based on the correctness of their response
    public void change_contestant_money(int idx, boolean correct) {
        contestants.get(idx).add_money(current_clue.get_value() * (correct ? 1 : -1));
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
    // nobody answered in time
    public void clue_timed_out() {
        for (Player plr : Bukkit.getOnlinePlayers()) {
            plr.playSound(plr.getLocation(), "jeopardy.game.times_up", 1.0F, 1.0F);
        }
        new BukkitRunnable() {
            @Override
            public void run() {
                buzzed_in = null;
                hide_clue();
                current_clue = null;
                finished_reading = false;
                accepting_responses[0] = true;
                accepting_responses[1] = true;
                accepting_responses[2] = true;
                cancel();
            }
        }.runTaskTimer(Jeopardy.getInstance(), 20L, 20L);
    }

    public void set_finished_reading(boolean val) {
        finished_reading = val;
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
                    sender.sendMessage(ChatColor.RED + "You cannot buzz in right now.");
                } else if (buzz_status == 1) {
                    sender.sendMessage(ChatColor.RED + "Responses are not being accepted right now!");
                    // time them out for 0.25 seconds
                    accepting_responses[idx] = false;
                    new BukkitRunnable() { @Override public void run() {accepting_responses[idx] = true;cancel();}}.runTaskTimer(Jeopardy.getInstance(), 5L, 20L);
                } else {
                    buzzed_in = contestants.get(i);
                    sender.sendMessage(ChatColor.GREEN + "You're up!");
                    sender.playSound(sender.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0F, 1.0F);
                    int[] plr_idx = {i};
                    int[] time_left = {8};
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
                                //buzzed_in = null; // do not reset, the host needs to mark the player incorrect
                                cancel();
                            }
                        }
                    }.runTaskTimer(Jeopardy.getInstance(), 0L, 20L);
                }
                return;
            }
        };
    }
}
