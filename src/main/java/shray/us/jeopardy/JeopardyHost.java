package shray.us.jeopardy;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;

public class JeopardyHost {
    private Player player;

    private JeopardyGame game;

    public JeopardyHost(Player plr, JeopardyGame g) {
        player = plr;
        game = g;
    }

    public Player get_player() {
        return player;
    }
    public void set_player(Player p) {
        player = p;
    }

    /*
     * Gives the host menu to the player.
     */
    public void give_host_menu() {
        ItemStack menu = new ItemStack(Material.PAPER);
        ItemMeta meta = menu.getItemMeta();
        meta.addEnchant(Enchantment.LUCK, 1, true);
        meta.setDisplayName(ChatColor.RESET + "" + ChatColor.LIGHT_PURPLE + "Host Menu");
        menu.setItemMeta(meta);
        player.getInventory().setItem(0, menu);
    }

    /*
     * Opens the host menu for a given round
     * 
     * @param round the round to open the menu for.
     */
    public void open_menu(String round) {
        Inventory menu = Bukkit.createInventory((InventoryHolder)player, 54, round.substring(0, 1).toUpperCase() + round.substring(1) + " Jeopardy!");
        ItemStack[] items = new ItemStack[54];
        for (int i = 0; i < 54; i++) {
            int row = i / 9;
            int col = i % 9;
            if (col > 5) {
                if (col == 7) {
                    switch (row) {
                        case 1:
                            ItemStack times_up = new ItemStack(Material.BARRIER);
                            ItemMeta meta = times_up.getItemMeta();
                            meta.setDisplayName(ChatColor.RED + "Time's up");
                            times_up.setItemMeta(meta);
                            items[i] = times_up;
                            break;
                        case 2:
                            ItemStack short_applause = new ItemStack(Material.BELL);
                            ItemMeta meta2 = short_applause.getItemMeta();
                            meta2.setDisplayName(ChatColor.YELLOW + "Applause (short)");
                            short_applause.setItemMeta(meta2);
                            items[i] = short_applause;
                            break;
                        case 3:
                            ItemStack long_applause = new ItemStack(Material.BELL);
                            ItemMeta meta3 = long_applause.getItemMeta();
                            meta3.setDisplayName(ChatColor.YELLOW + "Applause (long)");
                            long_applause.setItemMeta(meta3);
                            items[i] = long_applause;
                            break;
                        case 4:
                            ItemStack old_theme = new ItemStack(Material.JUKEBOX);
                            ItemMeta meta4 = old_theme.getItemMeta();
                            meta4.setDisplayName(ChatColor.BLUE + "Jeopardy Legacy Theme");
                            old_theme.setItemMeta(meta4);
                            items[i] = old_theme;
                            break;
                    }
                } else {
                    items[i] = new ItemStack(Material.AIR);
                }
                continue;
            }
            if (round.equalsIgnoreCase("final")) {
                if (col == 0) {
                    JeopardyCategory category = game.get_category(round);
                    switch (row) {
                        case 0:
                            ItemStack cat_button = new ItemStack(Material.OAK_SIGN);
                            ItemMeta cat_button_meta = cat_button.getItemMeta();
                            cat_button_meta.setDisplayName(ChatColor.RESET + "" + ChatColor.YELLOW + category.get_name());
                            cat_button.setItemMeta(cat_button_meta);
                            items[i] = cat_button;
                            break;
                        case 1:
                            ItemStack books_button = new ItemStack(Material.WRITABLE_BOOK);
                            ItemMeta books_button_meta = books_button.getItemMeta();
                            books_button_meta.setDisplayName(ChatColor.RESET + "" + ChatColor.YELLOW + "Give Wager/Response Books");
                            books_button.setItemMeta(books_button_meta);
                            items[i] = books_button;
                            break;
                        case 2:
                            ItemStack take_wager_button = new ItemStack(Material.TRIPWIRE_HOOK);
                            ItemMeta take_wager_button_meta = take_wager_button.getItemMeta();
                            take_wager_button_meta.setDisplayName(ChatColor.RESET + "" + ChatColor.YELLOW + "Take Wager Books");
                            take_wager_button.setItemMeta(take_wager_button_meta);
                            items[i] = take_wager_button;
                            break;
                        case 3:
                            ItemStack clue_button = new ItemStack(Material.BLUE_CONCRETE);
                            ItemMeta clue_button_meta = clue_button.getItemMeta();
                            clue_button_meta.setDisplayName(ChatColor.RESET + "" + ChatColor.YELLOW + "Reveal Clue");
                            clue_button.setItemMeta(clue_button_meta);
                            items[i] = clue_button;
                            break;
                        case 4:
                            ItemStack start_timer_button = new ItemStack(Material.CLOCK);
                            ItemMeta start_timer_button_meta = start_timer_button.getItemMeta();
                            start_timer_button_meta.setDisplayName(ChatColor.RESET + "" + ChatColor.YELLOW + "Start Timer");
                            start_timer_button.setItemMeta(start_timer_button_meta);
                            items[i] = start_timer_button;
                            break;
                        case 5:
                            ItemStack take_responses_button = new ItemStack(Material.TRIPWIRE_HOOK);
                            ItemMeta take_responses_button_meta = take_responses_button.getItemMeta();
                            take_responses_button_meta.setDisplayName(ChatColor.RESET + "" + ChatColor.YELLOW + "Take Response Books");
                            take_responses_button.setItemMeta(take_responses_button_meta);
                            items[i] = take_responses_button;
                            break;
                    }
                } else if (col == 1) {
                    ArrayList<JeopardyContestant> contestants = Jeopardy.getInstance().get_game_manager().get_contestants();
                    if (row % 2 == 0) {
                        ItemStack head = Utils.get_head(contestants.get(row/2).get_player());
                        ItemMeta meta = head.getItemMeta();
                        meta.setDisplayName(ChatColor.YELLOW + "Reveal " + contestants.get(row/2).get_player().getName() + "'s Response");
                        head.setItemMeta(meta);
                        items[i] = head;
                    } else {
                        ItemStack wager = new ItemStack(Material.EMERALD);
                        ItemMeta meta = wager.getItemMeta();
                        meta.setDisplayName(ChatColor.YELLOW + "Reveal " + contestants.get((row-1)/2).get_player().getName() + "'s Wager");
                        wager.setItemMeta(meta);
                        items[i] = wager;
                    }
                }
            } else {
                JeopardyCategory category = game.get_categories(round).get(String.valueOf(col));
                if (row == 0) { // category headers
                    ItemStack cat_button = new ItemStack(Material.OAK_SIGN);
                    ItemMeta meta = cat_button.getItemMeta();
                    meta.setDisplayName(ChatColor.RESET + "" + ChatColor.YELLOW + category.get_name());
                    cat_button.setItemMeta(meta);
                    items[i] = cat_button;
                } else { // clues
                    String clue_idx = String.valueOf(200 * row * (round.equalsIgnoreCase("double") ? 2 : 1));
                    JeopardyClue clue = category.get_clue(clue_idx);
                    ItemStack clue_button;
                    if (clue.revealed) {
                        clue_button = new ItemStack(Material.WHITE_CONCRETE);
                    } else if (clue.daily_double) {
                        clue_button = new ItemStack(Material.YELLOW_CONCRETE);
                    } else {
                        clue_button = new ItemStack(Material.BLUE_CONCRETE);
                    }
                    ItemMeta meta = clue_button.getItemMeta();
                    meta.setDisplayName(ChatColor.RESET + "" + ChatColor.YELLOW + "$" + clue_idx + ChatColor.RESET + " - " + category.get_name());
                    clue_button.setItemMeta(meta);
                    items[i] = clue_button;
                }
            }
        }
        menu.setContents(items);
        player.openInventory(menu);
    }

    public void click_menu_item(InventoryClickEvent event) {
        String round = event.getView().getTitle().split(" ")[0].toLowerCase();
        int i = event.getSlot(); // slot clicked
        int row = i / 9;
        int col = i % 9;
        if (col > 5) {
            if (col == 7) {
                switch (row) {
                    case 1:
                        String[] args = {"host", "timesup"};
                        Jeopardy.getInstance().command_handler(player, args);
                        player.closeInventory();
                        break;
                    case 2:
                        for (Player plr : Bukkit.getOnlinePlayers()) {
                            plr.playSound(plr.getLocation(), "jeopardy.audience.clapping_short", 1.0F, 1.0F);
                        }
                        break;
                    case 3:
                        for (Player plr : Bukkit.getOnlinePlayers()) {
                            plr.playSound(plr.getLocation(), "jeopardy.audience.clapping_long", 1.0F, 1.0F);
                        }
                        break;
                    case 4:
                        for (Player plr : Bukkit.getOnlinePlayers()) {
                            plr.playSound(plr.getLocation(), "jeopardy.music.old_theme", 1.0F, 1.0F);
                        }
                        break;
                }

            }
            return;
        }
        if (round.equalsIgnoreCase("final")) {
            if (col == 0) {
                switch (row) {
                    case 0:
                        String[] args = {"host", "reveal", String.valueOf(col)}; // reveal category
                        Jeopardy.getInstance().command_handler(player, args);
                        break;
                    case 1:
                        String[] args2 = {"host", "givewager", "*"};
                        Jeopardy.getInstance().command_handler(player, args2);
                        String[] args22 = {"host", "giveresponse", "*"};
                        Jeopardy.getInstance().command_handler(player, args22);
                        break;
                    case 2:
                        String[] args3 = {"host", "takewager", "*"};
                        Jeopardy.getInstance().command_handler(player, args3);
                        break;
                    case 3:
                        String[] args4 = {"host", "reveal", String.valueOf(col), "0"}; // reveal clue
                        Jeopardy.getInstance().command_handler(player, args4);
                        break;
                    case 4:
                        String[] args5 = {"host", "finaltimer"};
                        Jeopardy.getInstance().command_handler(player, args5);
                        break;
                    case 5:
                        String[] args6 = {"host", "takeresponse", "*"};
                        Jeopardy.getInstance().command_handler(player, args6);
                        break;
                }
            } else if (col == 1) {
                if (row % 2 == 0) {
                    String[] args = {"host", "finalreveal", String.valueOf(row / 2)}; // reveal fj response
                    Jeopardy.getInstance().command_handler(player, args);
                } else {
                    String[] args = {"host", "finalreveal", String.valueOf((row-1) / 2), "wager"}; // reveal fj wager
                    Jeopardy.getInstance().command_handler(player, args);
                }
            }
        } else {
            if (row == 0) { // category headers
                String[] args = {"host", "reveal", String.valueOf(col)};
                Jeopardy.getInstance().command_handler(player, args);
                // don't close inv after this
            } else { // clues
                String clue_idx = String.valueOf(200 * row * (round.equalsIgnoreCase("double") ? 2 : 1));
                String[] args = {"host", "reveal", String.valueOf(col), clue_idx};
                Jeopardy.getInstance().command_handler(player, args);
                if (event.getCurrentItem().getType() != Material.YELLOW_CONCRETE) player.closeInventory(); // don't close for daily doubles so we can play applause
            }
        }
    }
}
