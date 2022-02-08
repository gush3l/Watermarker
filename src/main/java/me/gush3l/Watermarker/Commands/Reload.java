package me.gush3l.Watermarker.Commands;

import me.gush3l.Watermarker.Files;
import me.gush3l.Watermarker.Util;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.bspfsystems.yamlconfiguration.file.FileConfiguration;

@SuppressWarnings({"all"})
public class Reload extends ListenerAdapter {

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {

        FileConfiguration config = Files.getConfig();

        if (!event.getMessage().getContentRaw().equals(config.getString("Commands.reload.command"))) {
            return;
        }

        if (!event.getAuthor().getId().equals(config.getString("Bot.owner-id"))
                && config.getBoolean("Commands.reload.restrict-to-owner")) {
            event.getMessage().reply(config.getString("Messages.cant-use-this-bot")).queue();
            return;
        }

        Files.reloadConfig();

        event.getMessage().replyEmbeds(Util.embedBuilder("Commands.reload.response").build()).queue();

    }

}