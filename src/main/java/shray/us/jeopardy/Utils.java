package shray.us.jeopardy;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.UUID;

public final class Utils {

    /*
     * Returns a player head ItemStack for the given player.
     * 
     * @param player the player to get the head for.
     */
    public static ItemStack get_head(Player player) {
        ItemStack head = new ItemStack(Material.PLAYER_HEAD, 1);
        SkullMeta meta = (SkullMeta)(head.getItemMeta());
        meta.setDisplayName(ChatColor.RESET + player.getName());
        meta.setOwner(player.getName());
        head.setItemMeta(meta);
        return head;
    }
}
