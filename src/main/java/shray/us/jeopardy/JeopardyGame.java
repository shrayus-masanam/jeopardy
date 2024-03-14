package shray.us.jeopardy;

import java.util.ArrayList;
import java.util.Dictionary;
import java.util.Hashtable;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

public class JeopardyGame { // 12 13 6 - // 12 7 -4
    private Player host;
    private ArrayList<Player> contestants;
    private boolean started = false;
    private GameBoard game_board;

    public JeopardyGame(Player sender, String[] args) {
        if (args.length < 3) {
            sender.sendMessage(ChatColor.RED + "Jepoardy requires 1 host and 2-3 contestants.\nUsage: /jeopardy create <host> <contestant 1> <contestant 2> ...");
            return;
        }
        host = Bukkit.getPlayerExact(args[0]);
        contestants = new ArrayList<Player>();
        for (int i = 1; i < args.length; i++) {
            Player contestant = Bukkit.getPlayerExact(args[i]);
            if (contestant == null) {
                sender.sendMessage(ChatColor.RED + "Couldn't find the player \"" + args[i] + "\"");
                return;
            }
            contestants.add(contestant);
        }
        game_board = new GameBoard(sender.getWorld(), 12, 13, 6, 12, 7, -4);
        sender.sendMessage(ChatColor.GREEN + "Created a game. Use /jeopardy start to begin!");
    }

    public void init() {
        game_board.black_out();
    }
    public void start() {
        started = true;
        game_board.power_on();
        // play cutscene
    }
    public void fill_board(String s) {
        try {
            game_board.fill_board("single");
        } catch(InterruptedException e) {}
    }
}
