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
        if (row == 0) { // category headers
            String[] args = {"host", "reveal", String.valueOf(col)};
            Jeopardy.getInstance().command_handler(player, args);
            player.closeInventory();
        } else { // clues
            String clue_idx = String.valueOf(200 * row * (round.equalsIgnoreCase("double") ? 2 : 1));
            String[] args = {"host", "reveal", String.valueOf(col), clue_idx};
            Jeopardy.getInstance().command_handler(player, args);
            player.closeInventory();
        }
    }
}
