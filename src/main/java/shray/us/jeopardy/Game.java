package main.java.shray.us.jeopardy;

import java.util.ArrayList;
import org.bukkit.entity.Player;
import org.bukkit.Bukkit;

public class JeopardyGame {
    Player host;
    ArrayList<Player> contestants;
    boolean started = false;

    public JeopardyGame(Player sender, String[] args) {
        if (args.length < 3) {
            sender.sendMessage(ChatColor.RED + "Jepoardy requires 1 host and 2-3 contestants.\nUsage: /jeopardy create <host> <contestant 1> <contestant 2> ...");
            return;
        }
        host = args[0];
        contestants = new ArrayList<Player>();
        for (int i = 1; i < args.length; i++) {
            Player contestant = Bukkit.getPlayerExact(args[i]);
            if (contestant == null) {
                sender.sendMessage(ChatColor.RED + "Couldn't find the player \"" + args[i] + "\"");
                return;
            }
            contestants.add(contestant);
        }
        sender.sendMessage(ChatColor.GREEN + "Created a game. Use /jeopardy start to begin!");
    }

    public void start() {
        started = true;
        // ...
    }
}
