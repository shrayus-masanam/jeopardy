package shray.us.jeopardy;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Arrays;

public class JeopardyContestant {
    private Player player;
    private int money = 0;

    private static ItemMeta buzzer_meta;
    private static ItemStack gray_buzzer = new ItemStack(Material.GRAY_DYE, 1);
    private static ItemStack red_buzzer = new ItemStack(Material.RED_DYE, 1);
    private static ItemStack green_buzzer = new ItemStack(Material.LIME_DYE, 1);

    public JeopardyContestant(Player plr) {
        player = plr;
        if (buzzer_meta == null) {
            ItemMeta meta = gray_buzzer.getItemMeta();
            meta.setDisplayName(ChatColor.RESET + "Buzzer");
            meta.setLore(Arrays.asList("Your signaling device to buzz in during Jeopardy!", "Green: responses open!", "Gray: responses not being accepted.", "Red: not allowed to buzz in."));
            //buzzer.setItemMeta(meta);
            buzzer_meta = meta;
            gray_buzzer.setItemMeta(buzzer_meta);
            red_buzzer.setItemMeta(buzzer_meta);
            green_buzzer.setItemMeta(buzzer_meta);
        }
        JeopardyContestant[] x = {this};
        new BukkitRunnable() {
            @Override
            public void run() {
                if (plr == null) return;
                Inventory inv = plr.getInventory();
                if (inv.getItem(0) == null || inv.getItem(0).getType() == Material.AIR || !(inv.getItem(0).getItemMeta().equals(buzzer_meta))) return;
                int buzz_status = Jeopardy.get_game_manager().can_buzz_in(x[0]);
                ItemStack to_set = null;
                switch (buzz_status) {
                    case 0:
                        to_set = red_buzzer;
                        break;
                    case 1:
                        to_set = gray_buzzer;
                        break;
                    case 2:
                        to_set = green_buzzer;
                        break;
                }
                if (!(inv.getItem(0).getType().equals(to_set.getType()))) // set if different
                    inv.setItem(0, to_set);

            }
        }.runTaskTimer(Jeopardy.getInstance(), 0L, 1L);
    }
    public Player get_player() {
        return player;
    }
    public void set_player(Player p) {
        player = p;
    }
    public int get_money() {
        return money;
    }
    public void set_money(int amount) {
        money = amount;
    }
    public void add_money(int addend) {
        money += addend;
    }
    public void subtract_money(int subtrahend) {
        money -= subtrahend;
    }

    public void give_buzzer() {
        ItemStack buzzer = new ItemStack(Material.GRAY_DYE, 1);
        buzzer.setItemMeta(buzzer_meta);
        player.getInventory().setItem(0, buzzer);
    }

    public void remove_buzzer() {
        for (ItemStack i : player.getInventory()) {
            if (i.getItemMeta().getDisplayName().equals(buzzer_meta.getDisplayName())) {
                player.getInventory().remove(i);
            }
        }
    }
}
