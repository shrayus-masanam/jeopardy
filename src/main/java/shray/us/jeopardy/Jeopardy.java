package shray.us.jeopardy;

import java.io.File;
import java.util.logging.Logger;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDropItemEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
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

	//public String getDataFolder() { return this.instance.getDataFolder().getAbsolutePath().toString(); }

	static JeopardyGameManager game;
	public static JeopardyGameManager get_game_manager() {
		return game;
	}

	public void onEnable() {
		instance = this;
		try {
			if (!instance.getDataFolder().exists()) {
				this.getDataFolder().mkdir();
			}
			File games_dir = new File(this.getDataFolder(), "games");
			if (!games_dir.exists()) {
				games_dir.mkdir();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		getServer().getPluginManager().registerEvents(this, (Plugin)this);
		LOGGER.info("Jeopardy enabled");
	}

	public void onDisable() {
		LOGGER.info("Jeopardy disabled");
	}

	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if (!command.getName().equalsIgnoreCase("jeopardy") || args.length < 1) return false;
		Player player = (Player)sender;

		return command_handler(player, args);
	}

	public boolean command_handler(Player sender, String[] args) {
		String out = sender.getDisplayName() + " issued command /jeopardy ";
		for (String word : args) {
			out += word + " ";
		}
		LOGGER.info(out);

		if (args[0].equalsIgnoreCase("create")) {
			game = new JeopardyGameManager(sender, args);
			game.init();
		}
		else if (args[0].equalsIgnoreCase("start")) {
			game.start();
			sender.sendMessage(ChatColor.GREEN + "Jepoardy has started.");
		}
		else if (args[0].equalsIgnoreCase("load")) {
			game.load(args[1] == null ? "single" : args[1]);
		}
		else if (args[0].equalsIgnoreCase("host")) {
			// host commands
			if (args[1].equalsIgnoreCase("menu")) {
				game.host_open_menu();
			} else if (args[1].equalsIgnoreCase("reveal")) {
				if (args.length < 3) { // args[2] DNE
					sender.sendMessage(ChatColor.RED + "You must specify a category index!");
					return false;
				}
				if (args.length == 3) { // revealing a category name
					game.reveal_category(args[2]);
				} else { // revealing a clue in a category
					game.reveal_clue(args[2], args[3]);
				}
			} else if (args[1].equals("finishread") || args[1].equalsIgnoreCase("unfinishread")) {
				game.set_finished_reading(args[1].equalsIgnoreCase("finishread"));
			} else if (args[1].equalsIgnoreCase("correct") || args[1].equalsIgnoreCase("incorrect")) {
				if (args.length < 3) { // args[2] DNE
					sender.sendMessage(ChatColor.RED + "You must specify a player index!");
					return false;
				}
				game.change_contestant_money(Integer.parseInt(args[2]), args[1].equalsIgnoreCase("correct"));
			} else if (args[1].equalsIgnoreCase("timesup")) {
				game.clue_timed_out();
			}
		} else if (args[0].equalsIgnoreCase("contestant")) {
			// contestant commands
			if (args[1].equalsIgnoreCase("buzzin")) {
				game.buzz_in(sender);
			}

		}
		return true;
	}

	@EventHandler(priority= EventPriority.HIGH)
	public void onPlayerUse(PlayerInteractEvent event){
		Player player = event.getPlayer();
		ItemStack item = player.getInventory().getItemInMainHand();
		if (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK) {
			if (item.getItemMeta().getDisplayName().equals("Buzzer")) {
				String[] args = {"contestant", "buzzin"};
				command_handler(player, args);
			} else if (item.getItemMeta().getDisplayName().contains("Host Menu")) {
				String[] args = {"host", "menu"};
				command_handler(player, args);
			}
		}
	}

	@EventHandler
	public void invClickEvent(InventoryClickEvent event) {
		if (event.getCurrentItem().getItemMeta().getDisplayName().equals("Buzzer")) {
			event.setCancelled(true);
		}
		if (event.getView().getTitle().contains("Jeopardy!")) {
			event.setCancelled(true);
			game.host_click_menu(event);
		}
	}

	@EventHandler void itemDropEvent(EntityDropItemEvent event) {
		ItemStack item = event.getItemDrop().getItemStack();
		if (
				item.getType() == Material.PAPER ||
				item.getItemMeta().getDisplayName().equalsIgnoreCase("buzzer") ||
				item.getType() == Material.WRITABLE_BOOK ||
				item.getType() == Material.WRITTEN_BOOK ||
				item.getType() == Material.PLAYER_HEAD
		)
			event.setCancelled(true);
	}
}