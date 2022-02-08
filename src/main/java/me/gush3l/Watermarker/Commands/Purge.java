package me.gush3l.Watermarker.Commands;

import me.gush3l.Watermarker.Files;
import me.gush3l.Watermarker.Main;
import me.gush3l.Watermarker.Util;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.bspfsystems.yamlconfiguration.configuration.ConfigurationSection;
import org.bspfsystems.yamlconfiguration.file.FileConfiguration;

import java.util.List;

public class Purge extends ListenerAdapter {

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        FileConfiguration config = Files.getConfig();

        String[] args = event.getMessage().getContentRaw().split("\\s+");

        if (args[0].equalsIgnoreCase(config.getString("Commands.purge.command"))) {
            if (args.length < 2) {
                event.getMessage().replyEmbeds(Util.embedBuilder("Commands.purge.error-no-args").build()).queue();
            } else if (Integer.parseInt(args[1]) > 100 || Integer.parseInt(args[1]) < 1) {
                event.getMessage().replyEmbeds(Util.embedBuilder("Commands.purge.error-too-many").build()).queue();
            } else if (Integer.parseInt(args[1]) < 100 && Integer.parseInt(args[1]) > 1) {
                int values = Integer.parseInt(args[1]);
                List<Message> messages = event.getChannel().getHistory().retrievePast(values).complete();

                messages.removeIf(message -> !message.getAuthor().getId().equals(Main.getJda().getSelfUser().getId()));

                event.getTextChannel().deleteMessages(messages).queue();
                String pathToEmbed = "Commands.purge.success";

                EmbedBuilder embedBuilder = new EmbedBuilder();
                if (!config.getString(pathToEmbed+".embed-title").equals("")) embedBuilder.setTitle(config.getString(pathToEmbed+".embed-title")
                        .replace("%messages%", args[1]));
                if (!config.getString(pathToEmbed+".embed-description").equals("")) embedBuilder.setDescription(config.getString(pathToEmbed+".embed-description")
                        .replace("%messages%", args[1]));
                if (!config.getString(pathToEmbed+".embed-footer").equals("")) embedBuilder.setFooter(config.getString(pathToEmbed+".embed-footer")
                        .replace("%messages%", args[1]));
                if (!config.getString(pathToEmbed+".embed-thumbnail").equals("")) embedBuilder.setThumbnail(config.getString(pathToEmbed+".embed-thumbnail")
                        .replace("%messages%", args[1]));
                ConfigurationSection section = config.getConfigurationSection(pathToEmbed+".fields");
                if (section != null){
                    for (String key : section.getKeys(false)){
                        embedBuilder.addField(config.getString(pathToEmbed+".fields."+key+".title")
                                        .replace("%messages%", args[1]),
                                config.getString(pathToEmbed+".fields."+key+".description")
                                        .replace("%messages%", args[1]),
                                config.getBoolean(pathToEmbed+".fields."+key+".in-line"));
                    }
                }
                
                event.getMessage().replyEmbeds(embedBuilder.build()).queue();
            }
        }
    }

}
