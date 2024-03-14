package shray.us.jeopardy;

import java.net.http.WebSocket.Listener;
import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.MapMeta;
import org.bukkit.map.MapView;
import org.bukkit.plugin.java.JavaPlugin;

/*
 * jeopardy java plugin
 */
public class Jeopardy extends JavaPlugin implements CommandExecutor, Listener {
	private static final Logger LOGGER = Logger.getLogger("Jeopardy");
	public static Jeopardy instance = null;
	public static Jeopardy getInstance() {
		return instance;
	}

	JeopardyGame game;

	public void onEnable() {
		instance = this;
		LOGGER.info("Jeopardy enabled");
	}

	public void onDisable() {
		LOGGER.info("Jeopardy disabled");
	}

	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if (!command.getName().equalsIgnoreCase("jeopardy") || args.length < 1) return false;
		Player player = (Player)sender;
		if (args[0].equalsIgnoreCase("create")) {
			game = new JeopardyGame(player, args);
			game.init();
		}
		else if (args[0].equalsIgnoreCase("start")) {
			game.start();
			sender.sendMessage(ChatColor.GREEN + "Jepoardy has started.");
		}
		else if (args[0].equalsIgnoreCase("load")) {
			game.fill_board("single");
		}
		else if (args[0].equalsIgnoreCase("host")) {
			// host commands

		} else if (args[0].equalsIgnoreCase("contestant")) {
			// contestant commands
			
		} else if (args[0].equalsIgnoreCase("map")) {
			// temporary command
			MapView view = Bukkit.createMap(player.getWorld());
			view.getRenderers().clear();

			if (args[1].equals("blank.png")) {
				MapImage image = new MapImage(args[1], 1, 0);
				view.addRenderer(image);
				ItemStack map = new ItemStack(Material.FILLED_MAP);
				MapMeta meta = (MapMeta) (map.getItemMeta());
				meta.setMapView(view);
				map.setItemMeta(meta);
				player.getInventory().addItem(map);
				return true;
			}

			MapImage image = new MapImage(args[1], 2, 0);
			view.addRenderer(image);
			ItemStack map = new ItemStack(Material.FILLED_MAP);
			MapMeta meta = (MapMeta)(map.getItemMeta());
			meta.setMapView(view);
			map.setItemMeta(meta);
			player.getInventory().addItem(map);

			image = new MapImage(args[1], 2, 1);
			MapView view2 = Bukkit.createMap(player.getWorld());
			view2.addRenderer(image);
			ItemStack map2 = new ItemStack(Material.FILLED_MAP);
			meta = (MapMeta) (map2.getItemMeta());
			meta.setMapView(view2);
			map2.setItemMeta(meta);
			player.getInventory().addItem(map2);
		}
		
		return true;
	}
}