package me.gush3l.Watermarker.Images;

import me.gush3l.Watermarker.Files;
import me.gush3l.Watermarker.Util;
import org.bspfsystems.yamlconfiguration.file.FileConfiguration;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class WatermarkUtils {

    public static BufferedImage applyWatermark(BufferedImage image, String type) throws IOException {
        FileConfiguration config = Files.getConfig();

        BufferedImage watermark = ImageIO.read(new File(config.getString("Watermark."+type+".path")));

        Graphics2D graphics = (Graphics2D) image.getGraphics();

        Dimension imgSize = new Dimension(watermark.getWidth(), watermark.getHeight());
        Dimension boundary = new Dimension(image.getWidth(), image.getHeight());

        Dimension newWatermarkSize = Util.getScaledDimension(imgSize,boundary, type);

        if (!(newWatermarkSize.getHeight() == 0 && newWatermarkSize.getWidth() == 0)) {
            watermark = Util.toBufferedImage(watermark.getScaledInstance(newWatermarkSize.width, newWatermarkSize.height, Image.SCALE_DEFAULT));
        }

        AlphaComposite alphaChannel = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, config.getFloat("Watermark."+type+".oppacity")/100);
        graphics.setComposite(alphaChannel);

        if (config.getString("Watermark."+type+".type").equals("middle")){
            int centerX = (image.getWidth() / 2) - (watermark.getWidth()/2);
            int centerY = (image.getHeight() / 2) - (watermark.getHeight()/2);

            graphics.drawImage(watermark, centerX, centerY, null);
            graphics.dispose();
        }

        else if (config.getString("Watermark."+type+".type").equals("mosaic")) {
            int width = image.getWidth();
            int height = image.getHeight();
            int size = (watermark.getHeight()+ watermark.getWidth())/2;
            int xcount;
            int ycount;
            if (width % size == 0) {
                xcount = width / size;
            } else {
                xcount = width / size + 1;
            }
            if (height % size == 0) {
                ycount = height / size;
            } else {
                ycount = height / size + 1;
            }
            int x = 0;
            int y = 0;

            for (int i = 0; i < xcount; i++) {
                for (int j = 0; j < ycount; j++) {
                    if((i%2==1 && j%2==1) || (i%2==0 && j%2==0)){
                        if (config.getDouble("Watermark."+type+".rotation-degrees") > 0){
                            if (watermark.getType() == 0) graphics.drawImage(watermark, x, y, watermark.getWidth(), watermark.getHeight(), null);
                            else Util.drawRotated(graphics, watermark, x, y, watermark.getWidth(), watermark.getHeight(), config.getDouble("Watermark."+type+".rotation-degrees"));
                        } else graphics.drawImage(watermark, x, y, null);
                        y = y + size;
                    }
                    else{
                        y = y + size;
                    }
                }
                y = 0;
                x = x + size;
            }
        }

        return image;

    }

}
