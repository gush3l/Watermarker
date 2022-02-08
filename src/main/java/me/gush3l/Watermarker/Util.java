package me.gush3l.Watermarker;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.User;
import org.bspfsystems.yamlconfiguration.configuration.ConfigurationSection;
import org.bspfsystems.yamlconfiguration.file.FileConfiguration;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@SuppressWarnings({"all"})
public class Util {

    public static void log(String logInfo, Level logLevel) {
        SimpleDateFormat formatter = new SimpleDateFormat("HH:mm:ss dd/MM/yyyy");
        Date date = new Date();
        System.out.println("[" + formatter.format(date) + "] [" + logLevel.getName() + "]: " + logInfo);
    }

    public static void logCmd(User user, TextChannel textChannel, String commandName, String subCommand) {
        SimpleDateFormat formatter = new SimpleDateFormat("HH:mm:ss dd/MM/yyyy");
        Date date = new Date();
        String userID = user.getId();
        String textChannelID = textChannel.getId();
        if (subCommand == null) {
            System.out.println("[" + formatter.format(date) + "] [COMMAND]: " + userID + " executed command " + commandName + " in text channel " + textChannelID);
            return;
        }
        System.out.println("[" + formatter.format(date) + "] [COMMAND]: " + userID + " executed command " + commandName + " " + subCommand + " in text channel " + textChannelID);
    }

    private static final DateTimeFormatter date = DateTimeFormatter.ofPattern("dd.MMM.yyyy hh:mm:ss");


    public static void deleteAfterMessage(Message message, int delay) {
        message.delete().queueAfter(delay, TimeUnit.SECONDS);
    }

    public static void deleteAfterChannel(TextChannel channel, int delay) {
        channel.delete().queueAfter(delay, TimeUnit.SECONDS);
    }

    public static String formatTime(LocalDateTime localDateTime) {
        LocalDateTime time = LocalDateTime.from(localDateTime.atOffset(ZoneOffset.UTC));
        return time.format(date) + " UTC";
    }

    public static String fileNameCreator(File file, String type) {
        FileConfiguration config = Files.getConfig();

        String noExtensionFileName = file.getName().substring(0, file.getName().lastIndexOf("."));
        String newFileName = config.getString("Watermark." + type + ".final-file-name");

        newFileName = newFileName.replace("%fileName%", noExtensionFileName);
        newFileName = newFileName.replace("%fileExtension%", file.getName().replace(noExtensionFileName + ".", ""));

        return newFileName;
    }

    public static Dimension getScaledDimension(Dimension imgSize, Dimension boundary, String type) {
        FileConfiguration config = Files.getConfig();

        int originalWidth = imgSize.width;
        int originalHeight = imgSize.height;
        int boundWidth = boundary.width;
        int boundHeight = boundary.height;
        int newWidth = originalWidth;
        int newHeight = originalHeight;

        if (originalWidth > boundWidth) {
            newWidth = boundWidth;
            newHeight = (newWidth * originalHeight) / originalWidth;
        }

        if (newHeight > boundHeight) {
            newHeight = boundHeight;
            newWidth = (newHeight * originalWidth) / originalHeight;
        }
        if (config.getInt("Watermark." + type + ".scale-percentage") >= 1) {
            newHeight = (newHeight / 100) * config.getInt("Watermark." + type + ".scale-percentage");

            newWidth = (newWidth / 100) * config.getInt("Watermark." + type + ".scale-percentage");
        }
        return new Dimension(newWidth, newHeight);
    }

    public static BufferedImage toBufferedImage(Image image) {
        if (image instanceof BufferedImage) {
            return (BufferedImage) image;
        }

        BufferedImage bufferedImage = new BufferedImage(image.getWidth(null), image.getHeight(null), BufferedImage.TYPE_INT_ARGB);

        Graphics2D bufferedGraphics = bufferedImage.createGraphics();
        bufferedGraphics.drawImage(image, 0, 0, null);
        bufferedGraphics.dispose();

        return bufferedImage;
    }

    public static void drawRotated(final Graphics g, final BufferedImage bufferedImage, final int x, final int y, final int width, final int height, final double angle) {
        final Rectangle collision = new Rectangle(x, y, width, height);
        final BufferedImage resize = resize(bufferedImage, collision.width, collision.height);
        final BufferedImage rotate = rotate(resize, angle, collision);
        g.drawImage(rotate, collision.x, collision.y, collision.width, collision.height, null);
    }

    public static BufferedImage resize(final BufferedImage bufferedImage, final int newWidth, final int newHeight) {
        final BufferedImage resized = new BufferedImage(newWidth, newHeight, bufferedImage.getType());
        final Graphics g = resized.createGraphics();
        g.drawImage(bufferedImage, 0, 0, newWidth, newHeight, null);
        return resized;
    }

    public static BufferedImage rotate(final BufferedImage bufferedImage, final double angle, final Rectangle collision) {
        final double sin = Math.sin(Math.toRadians(angle));
        final double cos = Math.cos(Math.toRadians(angle));

        final int x1 = collision.x;
        final int y1 = collision.y;

        final int x2 = collision.x + collision.width;
        final int y2 = collision.y;

        final int x3 = collision.x;
        final int y3 = collision.y + collision.height;

        final int x4 = collision.x + collision.width;
        final int y4 = collision.y + collision.height;

        final int newx1 = collision.x;
        final int newy1 = collision.y;

        final int newx2 = (int) (collision.x + collision.width * cos);
        final int newy2 = (int) (collision.y + collision.width * sin);

        final int newx3 = (int) (collision.x - collision.height * sin);
        final int newy3 = (int) (collision.y + collision.height * cos);

        final int newx4 = (int) (collision.x + collision.width * cos - collision.height * sin);
        final int newy4 = (int) (collision.y + collision.width * sin + collision.height * cos);

        collision.x = Math.min(Math.min(newx1, newx2), Math.min(newx3, newx4));
        collision.y = Math.min(Math.min(newy1, newy2), Math.min(newy3, newy4));

        collision.width = Math.max(Math.max(newx1, newx2), Math.max(newx3, newx4)) - collision.x;
        collision.height = Math.max(Math.max(newy1, newy2), Math.max(newy3, newy4)) - collision.y;

        final BufferedImage rotated = new BufferedImage(collision.width, collision.height, bufferedImage.getType());
        final Graphics2D g2d = rotated.createGraphics();
        g2d.translate(newx1 - collision.x, newy1 - collision.y);
        g2d.rotate(Math.toRadians(angle), 0, 0);
        g2d.drawRenderedImage(bufferedImage, null);
        g2d.dispose();
        return rotated;
    }

    public static List<String> extractUrls(String text) {
        List<String> containedUrls = new ArrayList<String>();
        String urlRegex = "((https?|ftp|gopher|telnet|file):((//)|(\\\\))+[\\w\\d:#@%/;$()~_?\\+-=\\\\\\.&]*)";
        Pattern pattern = Pattern.compile(urlRegex, Pattern.CASE_INSENSITIVE);
        Matcher urlMatcher = pattern.matcher(text);

        while (urlMatcher.find()) {
            containedUrls.add(text.substring(urlMatcher.start(0),
                    urlMatcher.end(0)));
        }

        return containedUrls;
    }

    public static EmbedBuilder embedBuilder(String pathToEmbed){
        FileConfiguration config = Files.getConfig();
        EmbedBuilder embedBuilder = new EmbedBuilder();
        if (!config.getString(pathToEmbed+".embed-title").equals("")) embedBuilder.setTitle(config.getString(pathToEmbed+".embed-title"));
        if (!config.getString(pathToEmbed+".embed-description").equals("")) embedBuilder.setDescription(config.getString(pathToEmbed+".embed-description"));
        if (!config.getString(pathToEmbed+".embed-footer").equals("")) embedBuilder.setFooter(config.getString(pathToEmbed+".embed-footer"));
        if (!config.getString(pathToEmbed+".embed-thumbnail").equals("")) embedBuilder.setThumbnail(config.getString(pathToEmbed+".embed-thumbnail"));
        ConfigurationSection section = config.getConfigurationSection(pathToEmbed+".fields");
        if (section != null){
            for (String key : section.getKeys(false)){
                embedBuilder.addField(config.getString(pathToEmbed+".fields."+key+".title"),
                        config.getString(pathToEmbed+".fields."+key+".description"),
                        config.getBoolean(pathToEmbed+".fields."+key+".in-line"));
            }
        }
        return embedBuilder;
    }

}
