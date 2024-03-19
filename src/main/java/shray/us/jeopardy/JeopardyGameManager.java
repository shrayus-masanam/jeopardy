package shray.us.jeopardy;

import java.io.FileReader;
import java.io.Reader;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;

import com.google.gson.Gson;
import eu.decentsoftware.holograms.api.DHAPI;
import eu.decentsoftware.holograms.api.holograms.Hologram;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.checkerframework.checker.units.qual.A;

public class JeopardyGameManager { // 12 13 6 - // 12 7 -4
    private Player host;
    private ArrayList<JeopardyContestant> contestants;
    private boolean started = false;
    private GameBoard game_board;
    JeopardyGame game;
    String current_round;
    Logger logger = Logger.getLogger("Jeopardy");

    public JeopardyGameManager(Player sender, String[] args) {
        // args[1]: game file name (without extension)
        // args[2]: host name
        // args[3]: contestant #1 name
        // args[4]: contestant #2 name
        // args[5]: contestant #3 name
        if (args.length < 3) {
            sender.sendMessage(ChatColor.RED + "Jepoardy requires 1 host and 2-3 contestants.\nUsage: /jeopardy create <game name> <host> <contestant 1> <contestant 2> ...");
            return;
        }
        Gson gson = new Gson();
        try (Reader reader = new FileReader(Paths.get(Jeopardy.getInstance().getDataFolder().toString(), "games", args[1] + ".json").toString())) {
            game = gson.fromJson(reader, JeopardyGame.class); // converting json file to object

            // Now you can use the 'game' object however you need
            //logger.info(game.get_categories(round_name).get("0").get_name()); // For example

        } catch (Exception e) {
            logger.info(e.toString());
            sender.sendMessage(ChatColor.RED + "Something went wrong when trying to load " + args[1] + ".json!");
        }
        host = Bukkit.getPlayerExact(args[2]);
        contestants = new ArrayList<JeopardyContestant>();
        for (int i = 3; i < args.length; i++) {
            Player contestant = Bukkit.getPlayerExact(args[i]);
            if (contestant == null) {
                sender.sendMessage(ChatColor.RED + "Couldn't find the player \"" + args[i] + "\"");
                return;
            }
            contestants.add(new JeopardyContestant(contestant));
        }
        game_board = new GameBoard(sender.getWorld(), 12, 13, 6, 12, 7, -4); // hardcoded
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
    public void load(String round_name) {
        game_board.fill_board(round_name);
        current_round = round_name;
    }

    public void reveal_category(String idx) {
        game_board.set_cat_holo(idx, game.get_categories(current_round).get(idx).get_name());
    }
    public void reveal_clue(String cat_idx, String clue_idx) {
        game_board.set_contestant_cat_holo(game.get_categories(current_round).get(cat_idx).get_name());
        game_board.set_contestant_clue_holo(game.get_clue(current_round, cat_idx, clue_idx).toString());
    }
}
