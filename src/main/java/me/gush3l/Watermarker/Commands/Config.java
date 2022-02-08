package me.gush3l.Watermarker.Commands;

import me.gush3l.Watermarker.Files;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.bspfsystems.yamlconfiguration.configuration.ConfigurationSection;
import org.bspfsystems.yamlconfiguration.file.FileConfiguration;

public class Config extends ListenerAdapter {

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {

        FileConfiguration config = Files.getConfig();

        if (!event.getMessage().getContentRaw().startsWith(config.getString("Commands.config.command"))) {
            return;
        }

        if (!event.getAuthor().getId().equals(config.getString("Bot.owner-id"))
                && config.getBoolean("Commands.help.restrict-to-owner")) {
            event.getMessage().reply(config.getString("Messages.cant-use-this-bot")).queue();
            return;
        }

        String[] args = event.getMessage().getContentRaw().replace(config.getString("Commands.config.command")+" ","").split("\\|",2);

        String configPath = args[0];
        String newValue = args[1];
        String oldValue = config.getString(configPath);
        String pathToEmbed = "Commands.config.response";

        EmbedBuilder embedBuilder = new EmbedBuilder();
        if (!config.getString(pathToEmbed+".embed-title").equals("")) embedBuilder.setTitle(config.getString(pathToEmbed+".embed-title")
                .replace("%configPath%",configPath)
                .replace("%oldConfigValue%",oldValue)
                .replace("%newConfigValue%",newValue));
        if (!config.getString(pathToEmbed+".embed-description").equals("")) embedBuilder.setDescription(config.getString(pathToEmbed+".embed-description")
                .replace("%configPath%",configPath)
                .replace("%oldConfigValue%",oldValue)
                .replace("%newConfigValue%",newValue));
        if (!config.getString(pathToEmbed+".embed-footer").equals("")) embedBuilder.setFooter(config.getString(pathToEmbed+".embed-footer")
                .replace("%configPath%",configPath)
                .replace("%oldConfigValue%",oldValue)
                .replace("%newConfigValue%",newValue));
        if (!config.getString(pathToEmbed+".embed-thumbnail").equals("")) embedBuilder.setThumbnail(config.getString(pathToEmbed+".embed-thumbnail")
                .replace("%configPath%",configPath)
                .replace("%oldConfigValue%",oldValue)
                .replace("%newConfigValue%",newValue));
        ConfigurationSection section = config.getConfigurationSection(pathToEmbed+".fields");
        if (section != null){
            for (String key : section.getKeys(false)){
                embedBuilder.addField(config.getString(pathToEmbed+".fields."+key+".title")
                                .replace("%configPath%",configPath)
                                .replace("%oldConfigValue%",oldValue)
                                .replace("%newConfigValue%",newValue),
                        config.getString(pathToEmbed+".fields."+key+".description")
                                .replace("%configPath%",configPath)
                                .replace("%oldConfigValue%",oldValue)
                                .replace("%newConfigValue%",newValue),
                        config.getBoolean(pathToEmbed+".fields."+key+".in-line"));
            }
        }

        config.set(configPath,newValue);

        Files.reloadConfig();

        event.getMessage().replyEmbeds(embedBuilder.build()).queue();

    }

}