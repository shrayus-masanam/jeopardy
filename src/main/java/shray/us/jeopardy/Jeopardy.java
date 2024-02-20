package shray.us;

import java.util.logging.Logger;
import org.bukkit.plugin.java.JavaPlugin;

import main.java.shray.us.jeopardy.JeopardyGame;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.event.Listener;
import org.Bukkit.entity.Player;

/*
 * jeopardy java plugin
 */
public class Jeopardy extends JavaPlugin implements CommandExecutor, Listener {
	private static final Logger LOGGER = Logger.getLogger("Jeopardy");

	JeopardyGame game;

	public void onEnable() {
		LOGGER.info("Jeopardy enabled");
	}

	public void onDisable() {
		LOGGER.info("Jeopardy disabled");
	}

	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if (!command.getName().equalsIgnoreCase("jeopardy") || args.length < 1) return false;
		if (args[0].equalsIgnoreCase("create")) {
			game = new JeopardyGame((Player)sender, args);
		}
		else if (args[0].equalsIgnoreCase("start")) {
			game.start();
			sender.sendMessage(ChatColor.GREEN + "Jepoardy has started.");
		}
		else if (args[0].equalsIgnoreCase("host")) {
			// host commands

		} else if (args[0].equalsIgnoreCase("contestant")) {
			// contestant commands
			
		}
		
		return true;
	}
}