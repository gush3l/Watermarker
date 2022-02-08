package me.gush3l.Watermarker.Images;

import me.gush3l.Watermarker.Files;
import me.gush3l.Watermarker.Gifs.GifDecoder;
import me.gush3l.Watermarker.Gifs.GifSequenceWriter;
import me.gush3l.Watermarker.Util;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.User;
import org.bspfsystems.yamlconfiguration.file.FileConfiguration;

import javax.imageio.ImageIO;
import javax.imageio.stream.FileImageOutputStream;
import javax.imageio.stream.ImageOutputStream;
import java.io.*;
import java.awt.image.BufferedImage;

public class WatermarkCreator {

    public static void sendWatermarkedFile(User user, File imageToWatermark, String type, TextChannel textChannel) throws Exception {

        FileConfiguration config = Files.getConfig();

        if (!imageToWatermark.getName().endsWith(".gif")) try {
            BufferedImage image = ImageIO.read(imageToWatermark);

            image = WatermarkUtils.applyWatermark(image, type);

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(image, "png", baos);
            byte[] imageOut = baos.toByteArray();
            baos.close();

            if (config.getBoolean("Bot.watermark.only-in-direct-messages")|| textChannel == null) user.openPrivateChannel().flatMap(privateChannel ->
                privateChannel.sendFile(imageOut, Util.fileNameCreator(imageToWatermark, type))
            ).queue();

            else textChannel.sendFile(imageOut, Util.fileNameCreator(imageToWatermark, type)).queue();

        } catch (Exception e) {
            e.printStackTrace();
        }

        else {

            final FileInputStream data = new FileInputStream(imageToWatermark);
            final GifDecoder.GifImage gif = GifDecoder.read(data);

            File outputGifFile = new File("output.gif");

            BufferedImage firstImage = gif.getFrame(0);

            ImageOutputStream output =
                    new FileImageOutputStream(outputGifFile);

            int delay = 0;

            for (int i = 0; i < gif.getFrameCount(); i++) {
                delay = delay + gif.getDelay(i);
            }

            delay = (delay/gif.getFrameCount())*10;

            GifSequenceWriter writer =
                    new GifSequenceWriter(output, firstImage.getType(), delay, true);

            writer.writeToSequence(firstImage);

            for (int i = 0; i < gif.getFrameCount(); i++) {
                final BufferedImage gifFrame = WatermarkUtils.applyWatermark(gif.getFrame(i), type);
                writer.writeToSequence(gifFrame);
            }

            writer.close();
            output.close();


            if (config.getBoolean("Bot.watermark.only-in-direct-messages") || textChannel == null) user.openPrivateChannel().flatMap(privateChannel ->
                    privateChannel.sendFile(outputGifFile, Util.fileNameCreator(imageToWatermark, type))
            ).queue();

            else textChannel.sendFile(outputGifFile, Util.fileNameCreator(imageToWatermark, type)).queue();

            outputGifFile.delete();

        }

        imageToWatermark.delete();
    }

}