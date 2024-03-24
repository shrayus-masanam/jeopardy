package shray.us.jeopardy;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

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

    public void give_host_menu() {
        ItemStack menu = new ItemStack(Material.PAPER);
        ItemMeta meta = menu.getItemMeta();
        meta.addEnchant(Enchantment.LUCK, 1, true);
        meta.setDisplayName(ChatColor.RESET + "" + ChatColor.LIGHT_PURPLE + "Host Menu");
        menu.setItemMeta(meta);
        player.getInventory().setItem(1, menu);
    }

    public void open_menu(String round) {
        Inventory menu = Bukkit.createInventory((InventoryHolder)player, 54, round.substring(0, 1).toUpperCase() + round.substring(1) + " Jeopardy!");
        ItemStack[] items = new ItemStack[54];
        for (int i = 0; i < 54; i++) {
            int row = i / 9;
            int col = i % 9;
            if (col > 5) {
                items[i] = new ItemStack(Material.AIR);
                continue;
            }
            JeopardyCategory category = game.get_categories(round).get(String.valueOf(col));
            if (round.equalsIgnoreCase("final")) {
                if (col == 0) {
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
                }
            } else {
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
            return;
        }
        JeopardyCategory category = game.get_categories(round).get(String.valueOf(col));
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
                        String[] args3 = {"host", "takwager", "*"};
                        Jeopardy.getInstance().command_handler(player, args3);
                        break;
                    case 3:
                        String[] args4 = {"host", "reveal", String.valueOf(col), "200"}; // reveal clue
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
                player.closeInventory();
            }
        }
    }
}
