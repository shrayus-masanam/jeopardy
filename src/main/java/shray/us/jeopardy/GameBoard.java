package shray.us.jeopardy;

import java.util.*;
import java.util.logging.Logger;

import eu.decentsoftware.holograms.api.DHAPI;
import eu.decentsoftware.holograms.api.holograms.Hologram;
import org.bukkit.*;
import org.bukkit.entity.ItemFrame;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.MapMeta;
import org.bukkit.map.MapView;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

public class GameBoard {
    Location top_left;
    Location bottom_right;
    World board_world;
    Dictionary<String, ItemStack> tiles = new Hashtable<>();
    ArrayList<Hologram> categories = new ArrayList<Hologram>();
    ArrayList<Hologram> money_displays = new ArrayList<Hologram>();
    Hologram contestant_category_display;
    Hologram contestant_clue_display;
    ArrayList<Hologram> contestant_clue_display_bgs = new ArrayList<Hologram>();

    ItemFrame[][] board_frames = new ItemFrame[6][12];

    Logger logger = Logger.getLogger("Jeopardy");

    public GameBoard(World world, int x, int y, int z, int x2, int y2, int z2) {

        top_left = new Location(world, x, y, z);
        bottom_right = new Location(world, x2, y2, z2);
        board_world = world;
        String[] unique_tiles = { "black", "blank", "200", "400", "600", "800", "1000", "1200", "1600", "2000" };
        for (String tile : unique_tiles) {
            MapView view = Bukkit.createMap(world);
            view.getRenderers().clear();
            MapImage image = new MapImage(tile + ".png", 0);
            view.addRenderer(image);
            ItemStack map = new ItemStack(Material.FILLED_MAP);
            MapMeta meta = (MapMeta)(map.getItemMeta());
            assert meta != null;
            meta.setMapView(view);
            map.setItemMeta(meta);
            tiles.put(tile + "0", map);

            view = Bukkit.createMap(world);
            view.getRenderers().clear();
            image = new MapImage(tile + ".png", 1);
            view.addRenderer(image);
            map = new ItemStack(Material.FILLED_MAP);
            meta = (MapMeta)(map.getItemMeta());
            assert meta != null;
            meta.setMapView(view);
            map.setItemMeta(meta);
            tiles.put(tile + "1", map);
        }
        for (int i = 0; i < 6; i++) {
            Hologram cat = DHAPI.getHologram("jeopardy_category_" + i);
            if (cat == null)
                cat = DHAPI.createHologram("jeopardy_category_" + i, new Location(board_world, 0, 0, 0));
            DHAPI.moveHologram(cat, new Location(board_world, top_left.getX() + 0.5, top_left.getY() + 0.5, top_left.getZ() - 2*i));
            List<String> lines = Arrays.asList("");
            DHAPI.setHologramLines(cat, lines);
            categories.add(cat);
        }

        // find all item frames
        Collection<Entity> entities = board_world.getNearbyEntities(new Location(board_world, (int)(top_left.getX()), (int)(top_left.getY()), (int)(top_left.getZ())), 12, 12, 12);
        for (Entity entity : entities) {
            if (entity.getType() != EntityType.GLOW_ITEM_FRAME && entity.getType() != EntityType.ITEM_FRAME) continue;
            //logger.info("Found an entity");
            if ((int)(entity.getLocation().getX()) != (int)(top_left.getX())) continue;
            int zOff = (int)(top_left.getZ()) - (int)(entity.getLocation().getZ() - 0.5);
            int yOff = (int)(top_left.getY()) - (int)(entity.getLocation().getY() - 0.5);
            if (zOff < 0 || yOff < 0 || zOff >= board_frames[0].length || yOff >= board_frames.length) continue;
            //logger.info("Found a frame at " + zOff + ", " + yOff + " (" + entity.getLocation().getZ() + ", " + entity.getLocation().getY() + ")");
            //logger.info("zOff: " + zOff + ", yOff: " + yOff);
            ItemFrame frame = (ItemFrame)entity;
            if (frame == null) {
                //logger.info("Warning: this frame is null.");
                continue;
            }
            board_frames[yOff][zOff] = frame;
            /*String msg = "\n";
            for (int i = 0; i < board_frames.length; i++) {
                for (int j = 0; j < board_frames[i].length; j++) {
                    if (board_frames[i][j] != null)
                        msg += "X ";
                    else
                        msg += "O ";
                }
                msg += "\n";
            }
            logger.info(msg);*/
        }
        // create contestant money displays
        for (int i = 0; i < 3; i++) {
            Hologram money = DHAPI.getHologram("jeopardy_contestant_money_" + i);
            if (money == null)
                money = DHAPI.createHologram("jeopardy_contestant_money_" + i, new Location(board_world, 0, 0, 0));
            DHAPI.moveHologram(money, new Location(board_world, 22.5, 8, 5.5 - 4*i)); // hardcoded
            List<String> lines = Arrays.asList("$0");
            DHAPI.setHologramLines(money, lines);
            money_displays.add(money);
        }


        // create contestant category display
        Hologram cur_cat = DHAPI.getHologram("jeopardy_contestant_current_category");
        if (cur_cat == null)
            cur_cat = DHAPI.createHologram("jeopardy_contestant_current_category", new Location(board_world, 0, 0, 0));
        DHAPI.moveHologram(cur_cat, new Location(board_world, 18, 10.5, 1.5)); // hardcoded
        DHAPI.setHologramLines(cur_cat, Arrays.asList(""));
        contestant_category_display = cur_cat;

        // create contestant clue display
        Hologram cur_clue = DHAPI.getHologram("jeopardy_contestant_current_clue");
        if (cur_clue == null)
            cur_clue = DHAPI.createHologram("jeopardy_contestant_current_clue", new Location(board_world, 0, 0, 0));
        DHAPI.moveHologram(cur_clue, new Location(board_world, 18, 10, 1.5)); // hardcoded
        DHAPI.setHologramLines(cur_clue, Arrays.asList(""));
        contestant_clue_display = cur_clue;

        // todo: make a method to make holograms
        // we need 4 total backgrounds to make a solid color
        // #1
        Hologram cur_bg_1 = DHAPI.getHologram("jeopardy_contestant_current_bg_1");
        if (cur_bg_1 == null)
            cur_bg_1 = DHAPI.createHologram("jeopardy_contestant_current_bg_1", new Location(board_world, 0, 0, 0));
        DHAPI.moveHologram(cur_bg_1, new Location(board_world, 17.9, 10.7, 1.4)); // hardcoded
        DHAPI.setHologramLines(cur_bg_1, Arrays.asList(""));
        // #2
        Hologram cur_bg_2 = DHAPI.getHologram("jeopardy_contestant_current_bg_2");
        if (cur_bg_2 == null)
            cur_bg_2 = DHAPI.createHologram("jeopardy_contestant_current_bg_2", new Location(board_world, 0, 0, 0));
        DHAPI.moveHologram(cur_bg_2, new Location(board_world, 17.9, 10.7, 1.6)); // hardcoded
        DHAPI.setHologramLines(cur_bg_2, Arrays.asList(""));
        // #3
        Hologram cur_bg_3 = DHAPI.getHologram("jeopardy_contestant_current_bg_3");
        if (cur_bg_3 == null)
            cur_bg_3 = DHAPI.createHologram("jeopardy_contestant_current_bg_3", new Location(board_world, 0, 0, 0));
        DHAPI.moveHologram(cur_bg_3, new Location(board_world, 17.9, 10.5, 1.4)); // hardcoded
        DHAPI.setHologramLines(cur_bg_3, Arrays.asList(""));
        // #4
        Hologram cur_bg_4 = DHAPI.getHologram("jeopardy_contestant_current_bg_4");
        if (cur_bg_4 == null)
            cur_bg_4 = DHAPI.createHologram("jeopardy_contestant_current_bg_4", new Location(board_world, 0, 0, 0));
        DHAPI.moveHologram(cur_bg_4, new Location(board_world, 17.9, 10.5, 1.6)); // hardcoded
        DHAPI.setHologramLines(cur_bg_4, Arrays.asList(""));

        contestant_clue_display_bgs.add(cur_bg_1);
        contestant_clue_display_bgs.add(cur_bg_2);
        contestant_clue_display_bgs.add(cur_bg_3);
        contestant_clue_display_bgs.add(cur_bg_4);

    }
    // Splits a string into lines of ideally 20 characters, but only splits at the next space
    // optional prefix/suffic to prepend/append to each line (like formatting codes)
    private List<String> split_to_lines(String text) {
        return split_to_lines(text, "", "");
    }
    private List<String> split_to_lines(String text, String prefix, String suffix) {
        ArrayList<String> list = new ArrayList<String>();
        String current = prefix;
        int counter = 0;
        for (char c : text.toCharArray()) {
            if (c == ' ' || c == '\n') {
                if (counter >= 30 || c == '\n') { // if its a newline char we'll just add it on a new line regardless
                    list.add(current + suffix);
                    current = prefix;
                    counter = 0;
                }
            }
            current += c;
            counter++;
        }
        list.add(current + suffix);
        return list;
    }

    // board is powered off at the beginning of the game
    public void black_out() {
        for (ItemFrame[] boardFrame : board_frames) {
            for (ItemFrame itemFrame : boardFrame) {
                if (itemFrame == null) continue;
                itemFrame.setItem(tiles.get("black0").clone());
            }
        }
    }
    
    // power on the board to blank blue tiles
    public void power_on() {
        for (ItemFrame[] boardFrame : board_frames) {
            for (ItemFrame itemFrame : boardFrame) {
                if (itemFrame == null) continue;
                itemFrame.setItem(tiles.get("blank0").clone());
            }
        }
        reset_category_labels();
    }
    private void reset_category_labels() {
        reset_category_labels("");
    }
    private void reset_category_labels(String round) {
        for (Hologram cat : categories) {
            List<String> lines;
            if (!(round.equalsIgnoreCase(""))) {
                String special_color = "fcba03";
                if (round.equalsIgnoreCase("final") || round.equalsIgnoreCase("tiebreaker"))
                    special_color = "ff8400";
                lines = Arrays.asList(
                        "<#" + special_color + ">" + round.substring(0, 1).toUpperCase() + round.substring(1) + "</#" + special_color + ">", // capitalizes first letter
                        "<#f0dbaf>Jeopardy!</#f7cdbc>"
                );
            } else {
                lines = Arrays.asList("<#f0dbaf>Jeopardy!</#f7cdbc>");
            }
            DHAPI.setHologramLines(cat, lines);
        }
    }

    public void fill_board(String round_name) {

        logger.info(Jeopardy.getInstance().getDataFolder().getAbsolutePath());

        // generating random sequence with numbers 1-30
        int[] numbered = new int[30];
        for (int i = 0; i < 30; i++) {
            numbered[i] = i;
        }
        Random rand = new Random();
        for (int i = 0; i < numbered.length; i++) {
            int randomIndexToSwap = rand.nextInt(numbered.length);
            int temp = numbered[randomIndexToSwap];
            numbered[randomIndexToSwap] = numbered[i];
            numbered[i] = temp;
        }

        // fill the board
        if (!(round_name.equalsIgnoreCase("single")))
            reset_category_labels(round_name);
        else
            reset_category_labels();
        if (round_name.equalsIgnoreCase("final") || round_name.equalsIgnoreCase("tiebreaker")) {
            return;
        }
        for (Player plr : Bukkit.getOnlinePlayers()) {
            plr.playSound(plr.getLocation(), "jeopardy.board.fill", 1.0F, 1.0F);
        }
        int[] idx = new int[] { 0 };
        new BukkitRunnable() {
            @Override
            public void run() {
                if (idx[0] >= board_frames.length * board_frames[0].length)
                    cancel();
                for (int i = 0; i < 5 && idx[0] + i < numbered.length; i ++) {
                    int tile_num = numbered[idx[0] + i];
                    int x = (tile_num % 6) * 2; // multiply by 2 since each tile is 2 item frames long
                    int y = tile_num / 6 + 1; // add 1 to skip the top row (category names)
                    String dollarAmount = "";
                    if (round_name.equalsIgnoreCase("single"))
                        dollarAmount = String.valueOf(200 * y);
                    else if (round_name.equalsIgnoreCase("double")) {
                        dollarAmount = String.valueOf(400 * y);
                    }
                    board_frames[y][x].setItem(tiles.get(dollarAmount + "0").clone());
                    board_frames[y][x + 1].setItem(tiles.get(dollarAmount + "1").clone());
                }
                idx[0] += 5;
            }
        }.runTaskTimer(Jeopardy.getInstance(), 0L, 7L);
    }

    public void set_tile(int cat_idx, int clue_idx, String tile_name) {
        board_frames[clue_idx][cat_idx * 2].setItem(tiles.get(tile_name + "0").clone());
        board_frames[clue_idx][cat_idx * 2 + 1].setItem(tiles.get(tile_name + "1").clone());
    }

    // set the text of a category hologram
    public void set_cat_holo(String idx, String text) {
        List<String> lines = Arrays.asList(text);
        DHAPI.setHologramLines(categories.get(Integer.parseInt(idx)), lines);
    }

    public void set_contestant_cat_holo(String text) {
        DHAPI.setHologramLines(contestant_category_display, split_to_lines(text, "&n", ""));
        if (!text.isEmpty()) {
            for (Hologram bg : contestant_clue_display_bgs) {
                DHAPI.setHologramLines(bg, Arrays.asList("<#000073>█████████████████████</#000073>", "<#000073>█████████████████████</#000073>", "<#000073>█████████████████████</#000073>", "<#000073>█████████████████████</#000073>", "<#000073>█████████████████████</#000073>", "<#000073>█████████████████████</#000073>", "<#000073>█████████████████████</#000073>"));
            }
        } else {
            for (Hologram bg : contestant_clue_display_bgs) {
                DHAPI.setHologramLines(bg, Arrays.asList(""));
            }
        }
    }

    public void set_contestant_clue_holo(String text) {
        DHAPI.setHologramLines(contestant_clue_display, split_to_lines(text));
        //DHAPI.setHologramLines(contestant_clue_display_bg, Arrays.asList("<#000073>███████████████</#000073>", "<#000073>███████████████</#000073>", "<#000073>███████████████</#000073>", "<#000073>███████████████</#000073>", "<#000073>███████████████</#000073>", "<#000073>███████████████</#000073>"));
        if (!text.isEmpty())
            board_world.spawnParticle(Particle.CLOUD, new Location(board_world, 19, 10, 1.5), 25); // little explosion to catch attention

    }

    public void set_money_display(int idx, String text) {
        DHAPI.setHologramLines(money_displays.get(idx), split_to_lines(text));
    }


    public World getWorld() {
        return board_world;
    }
}
