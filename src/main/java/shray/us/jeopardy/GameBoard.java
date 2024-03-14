package shray.us.jeopardy;

import java.util.*;
import java.util.logging.Logger;

import eu.decentsoftware.holograms.api.DHAPI;
import eu.decentsoftware.holograms.api.holograms.Hologram;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.ItemFrame;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.MapMeta;
import org.bukkit.map.MapView;
import org.bukkit.Bukkit;
import org.bukkit.Material;
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

    ItemFrame[][] board_frames = new ItemFrame[6][12];

    public GameBoard(World world, int x, int y, int z, int x2, int y2, int z2) {
        Logger logger = Logger.getLogger("Jeopardy");

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
        for (Hologram cat : categories) {
            List<String> lines = Arrays.asList("<#f0dbaf>Jeopardy!</#f7cdbc>");
            DHAPI.setHologramLines(cat, lines);
        }
    }

    public void fill_board(String round_name) throws InterruptedException {
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
        }.runTaskTimer(Jeopardy.getInstance(), 0L, 10L);
    }

}
