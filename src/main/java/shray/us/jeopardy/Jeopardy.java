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
import org.bukkit.event.player.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.inventory.meta.ItemMeta;
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
	public JeopardyGameManager get_game_manager() {
		return game;
	}

	public void onEnable() {
		instance = this;
		try {
			if (!instance.getDataFolder().exists()) {
				this.getDataFolder().mkdir();
			}
			File games_dir = new File(this.getDataFolder(), "games");
			if (!(games_dir.exists())) {
				games_dir.mkdir();
			}
			File as_dir = new File(this.getDataFolder(), "autosaves");
			if (!(as_dir.exists())) {
				as_dir.mkdir();
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
		if (sender.isOp()) {
			if (args[0].equalsIgnoreCase("create")) {
				game = new JeopardyGameManager(sender, args);
				game.init();
			} else if (args[0].equalsIgnoreCase("start")) {
				if (args.length >= 2 && args[1].equalsIgnoreCase("intro"))
					game.start(true);
				else
					game.start(false);
				sender.sendMessage(ChatColor.GREEN + "Jepoardy has started.");
			} else if (args[0].equalsIgnoreCase("load")) {
				game.load(args[1] == null ? "single" : args[1]);
			}
		}
		if (args[0].equalsIgnoreCase("host")) {
			// host commands
			if (!(sender.getName().equalsIgnoreCase(game.get_host().get_player().getName()))) return false;
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
			} else if (args[1].equals("finishread")) {
				game.set_finished_reading();
			} else if (args[1].equalsIgnoreCase("unbuzz")) {
				game.dismiss_buzzed_in();
			} else if (args[1].equalsIgnoreCase("correct") || args[1].equalsIgnoreCase("incorrect")) {
				if (args.length < 3) { // args[2] DNE
					sender.sendMessage(ChatColor.RED + "You must specify a player index!");
					return false;
				}
				game.declare_correctness(Integer.parseInt(args[2]), args[1].equalsIgnoreCase("correct"));
			} else if (args[1].equalsIgnoreCase("addmoney")) {
				game.change_contestent_money(args[2], Integer.parseInt(args[3]));
			} else if (args[1].equalsIgnoreCase("timesup")) {
				game.clue_timed_out();
			} else if (args[1].equalsIgnoreCase("wager") || args[1].equalsIgnoreCase("setwager")) { // refers to host setting contestant contestant wagers
				game.set_wager(args[2], Integer.parseInt(args[3]));
			} else if (args[1].equalsIgnoreCase("givewager")) { // referring to gving final jeopardy wager book
				game.give_book(args[2], "Wager", 1);
			} else if (args[1].equalsIgnoreCase("giveresponse")) {
				game.give_book(args[2], "Final Jeopardy Response", 2);
			} else if (args[1].equalsIgnoreCase("takewager")) {
				game.take_book(args[2], 1);
			} else if (args[1].equalsIgnoreCase("takeresponse")) {
				game.take_book(args[2], 2);
			} else if (args[1].equalsIgnoreCase("finaltimer")) {
				game.final_jeopardy_timer();
			} else if (args[1].equalsIgnoreCase("finalreveal")) {
				if (args.length > 3) {
					game.reveal_final_wager(args[2]);
				} else {
					game.reveal_final_response(args[2]);
				}
			} else if (args[1].equalsIgnoreCase("align")) {
				game.align_players();
			} else if (args[1].equalsIgnoreCase("showoff")) {
				game.align_players();
				game.show_off(args[2]);
			} else if (args[1].equalsIgnoreCase("setplayer")) {
				if (args.length < 4) {
					sender.sendMessage(ChatColor.RED + "Usage: /jeopardy host setplayer <index> <username>");
					return false;
				}
				game.set_contestant(args[2], args[3]);
			} else if (args[1].equalsIgnoreCase("loadautosave")) {
				game.load_autosave();
			}
		} else if (args[0].equalsIgnoreCase("contestant")) {
			// contestant commands
			if (args[1].equalsIgnoreCase("buzzin")) {
				game.buzz_in(sender);
			}

		}
		return true;
	}

	@EventHandler(priority = EventPriority.HIGH)
	public void onPlayerUse(PlayerInteractEvent event){
		Player player = event.getPlayer();
		ItemStack item = player.getInventory().getItemInMainHand();
		ItemMeta item_meta = item.getItemMeta();
		if (item_meta == null) {
			if (event.getAction() == Action.RIGHT_CLICK_BLOCK && event.getClickedBlock().getBlockData().getMaterial() == Material.LECTERN)
				event.setCancelled(true); // dont let players use lecterns to hold books

			return;
		}
		if (item.getType() == Material.PLAYER_HEAD) {
			if (item_meta.getLore() != null && item_meta.getLore().get(2) != null) {
				String loreline = item_meta.getLore().get(2);
				if (loreline.split(" - ")[0].equalsIgnoreCase("Player index")) {
					String giveortake = "";
					Action eaction = event.getAction();
					if (eaction == Action.RIGHT_CLICK_AIR || eaction == Action.RIGHT_CLICK_BLOCK)
						giveortake = "incorrect";
					else if (eaction == Action.LEFT_CLICK_AIR || eaction == Action.LEFT_CLICK_BLOCK)
						giveortake = "correct";
					String[] args = {"host", giveortake, loreline.split(" - ")[1]};
					command_handler(player, args);
				}
			}
		}
		if (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK) {
			if (item_meta.getDisplayName().equals("Buzzer")) {
				String[] args = {"contestant", "buzzin"};
				command_handler(player, args);
			} else if (item_meta.getDisplayName().contains("Host Menu")) {
				String[] args = {"host", "menu"};
				command_handler(player, args);
			}
		} else if (event.getAction() == Action.LEFT_CLICK_AIR || event.getAction() == Action.LEFT_CLICK_BLOCK) {
			if (item_meta.getDisplayName().contains("Host Menu")) {
				String[] args = {"host", "finishread"};
				command_handler(player, args);
			}
		}
	}

	@EventHandler
	public void invClickEvent(InventoryClickEvent event) {
		ItemStack item  = event.getCurrentItem();
		if (item == null) return;
		ItemMeta meta = item.getItemMeta();
		if (meta != null) {
			if (meta.getDisplayName().equals("Buzzer"))
				event.setCancelled(true);
			if (meta.getDisplayName().contains("Final Jeopardy"))
				event.setCancelled(true);
		}
		if (event.getView().getTitle().contains("Jeopardy!")) {
			event.setCancelled(true);
			game.host_click_menu(event);
		}
	}

	@EventHandler
	public void itemDropEvent(PlayerDropItemEvent event) {
		ItemStack item = event.getItemDrop().getItemStack();
		if (
				item.getType() == Material.PAPER ||
				(item.getItemMeta() != null && item.getItemMeta().getDisplayName().equalsIgnoreCase("buzzer")) ||
				item.getType() == Material.WRITABLE_BOOK ||
				item.getType() == Material.WRITTEN_BOOK ||
				item.getType() == Material.PLAYER_HEAD
		)
			event.setCancelled(true);
	}

	@EventHandler
	public void bookEditEvent(PlayerEditBookEvent event) {
		if (event.getPreviousBookMeta().getDisplayName().contains("Wager") || event.getPreviousBookMeta().getDisplayName().contains("Final Jeopardy Response")) {
			event.setSigning(false); // don't let them sign the book
			BookMeta meta = event.getNewBookMeta();
			meta.setDisplayName(event.getPreviousBookMeta().getDisplayName()); // don't let them change the name
			event.setNewBookMeta(meta);
		}
	}

	@EventHandler
	public void recipeEvent(PlayerRecipeDiscoverEvent event) {
		event.setCancelled(true);
	}
}