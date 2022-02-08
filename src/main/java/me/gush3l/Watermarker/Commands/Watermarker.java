package me.gush3l.Watermarker.Commands;

import me.gush3l.Watermarker.Files;
import me.gush3l.Watermarker.Images.WatermarkCreator;
import me.gush3l.Watermarker.Util;
import net.dv8tion.jda.api.entities.ChannelType;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.bspfsystems.yamlconfiguration.file.FileConfiguration;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

@SuppressWarnings({"all"})
public class Watermarker extends ListenerAdapter {

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {

        FileConfiguration config = Files.getConfig();

        if (!(event.getMessage().getContentRaw().startsWith(config.getString("Bot.watermark.command"))
                || event.getMessage().getContentRaw().startsWith(config.getString("Bot.watermark.command-dark-watermark"))
                || event.getMessage().getContentRaw().startsWith(config.getString("Bot.watermark.command-lighter-watermark")))) {
            return;
        }

        if (!event.getChannelType().equals(ChannelType.PRIVATE) && config.getBoolean("Bot.watermark.only-in-direct-messages")) return;


        if (!event.getAuthor().getId().equals(config.getString("Bot.owner-id"))
                && config.getBoolean("Bot.watermark.restrict-to-owner")) {
            event.getMessage().reply(config.getString("Messages.cant-use-this-bot")).queue();
            return;
        }

        if (event.getMessage().getAttachments().isEmpty() && !event.getMessage().getContentRaw().contains("https://")) {
            event.getMessage().reply(config.getString("Messages.no-attachements-found")).queue();
            return;
        }

        File outputfile = null;

        if (event.getMessage().getContentRaw().contains("https://")) {
            try {
                String urlString = Util.extractUrls(event.getMessage().getContentRaw()).get(0);
                URL url = new URL(urlString);
                HttpURLConnection httpcon = (HttpURLConnection) url.openConnection();
                httpcon.addRequestProperty("User-Agent", "Mozilla/4.0");
                InputStream input_stream = httpcon.getInputStream();
                BufferedImage img = ImageIO.read(input_stream);
                if (urlString.contains(".png")) {
                    outputfile = new File("image.png");
                    ImageIO.write(img, "png", outputfile);
                }
                if (urlString.contains(".jpg")) {
                    outputfile = new File("image.jpg");
                    ImageIO.write(img, "jpg", outputfile);
                }
                if (urlString.contains(".jpeg")) {
                    outputfile = new File("image.jpeg");
                    ImageIO.write(img, "jpeg", outputfile);
                }
                if (urlString.contains(".gif")) {
                    outputfile = new File("image.gif");
                    ImageIO.write(img, "gif", outputfile);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        String type = "normal";

        if (event.getMessage().getContentRaw().startsWith(config.getString("Bot.watermark.command"))) type = "normal";
        if (event.getMessage().getContentRaw().startsWith(config.getString("Bot.watermark.command-dark-watermark")))
            type = "dark";
        if (event.getMessage().getContentRaw().startsWith(config.getString("Bot.watermark.command-lighter-watermark")))
            type = "light";

        List<Message.Attachment> attachments = new ArrayList<>();

        for (Message.Attachment attachment : event.getMessage().getAttachments()) {
            if (attachment.getFileName().contains("png")
                    || attachment.getFileName().contains("jpg")
                    || attachment.getFileName().contains("jpeg")
                    || attachment.getFileName().contains("gif")) {
                attachments.add(attachment);
            }
        }

        TextChannel textChannel = null;
        if (event.isFromType(ChannelType.TEXT)) textChannel = event.getTextChannel();

        if (attachments.isEmpty()) {
            try {
                WatermarkCreator.sendWatermarkedFile(event.getAuthor(), outputfile, type, textChannel);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else for (Message.Attachment attachment : attachments) {
            try {
                WatermarkCreator.sendWatermarkedFile(event.getAuthor(), attachment.downloadToFile().get(), type, textChannel);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }

}
