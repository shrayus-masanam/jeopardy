package shray.us.jeopardy;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.logging.Logger;

import javax.imageio.ImageIO;

import org.bukkit.entity.Player;
import org.bukkit.map.MapCanvas;
import org.bukkit.map.MapRenderer;
import org.bukkit.map.MapView;

public class MapImage extends MapRenderer {
    
    private static final Logger LOGGER = Logger.getLogger("Jeopardy");
    private BufferedImage image;
    private boolean rendered;

    /*
     * Loads an image from the given file name and divides it into portions
     * 
     * @param file_name the name of the file to load the image from.
     * @param portions the number of portions to divide the image into.
     * @param index the index of the portions to use.
     */
    public MapImage(String file_name, int portions, int index) {
        try {
            BufferedImage wide_image;
            wide_image = ImageIO.read(MapImage.class.getResourceAsStream("/shray/us/images/" + file_name));
            int width = wide_image.getWidth();
            int height = wide_image.getHeight();
            image = wide_image.getSubimage(width/portions*index, 0, width/portions, height); // the image is divided by vertical lines, and a portion is returned
        } catch(IOException e) {
            LOGGER.info(e.toString());
        }
    }
    public MapImage(String file_name, int index) {
        try {
            BufferedImage wide_image;
            wide_image = ImageIO.read(MapImage.class.getResourceAsStream("/shray/us/images/" + file_name));
            int width = wide_image.getWidth();
            int portions = (int)(width/128.0);
            int height = wide_image.getHeight();
            image = wide_image.getSubimage(width/portions*index, 0, width/portions, height); // the image is divided by vertical lines, and a portion is returned
        } catch(IOException e) {
            LOGGER.info(e.toString());
        }
    }

    @Override
    public void render(MapView mv, MapCanvas mc, Player player) {
        if (rendered) return; // guard to prevent running every tick
        rendered = true;
        mv.setTrackingPosition(false);
        mv.setUnlimitedTracking(false);
        mc.drawImage(0, 0, image);
    }
}
