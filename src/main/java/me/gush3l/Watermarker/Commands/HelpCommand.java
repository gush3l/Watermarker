package me.gush3l.Watermarker.Commands;

import me.gush3l.Watermarker.Files;
import me.gush3l.Watermarker.Util;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.bspfsystems.yamlconfiguration.file.FileConfiguration;

@SuppressWarnings({"all"})
public class HelpCommand extends ListenerAdapter {

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {

        FileConfiguration config = Files.getConfig();

        if (!event.getMessage().getContentRaw().equals(config.getString("Commands.help.command"))) {
            return;
        }

        if (!event.getAuthor().getId().equals(config.getString("Bot.owner-id"))
        && config.getBoolean("Commands.help.restrict-to-owner")) {
            event.getMessage().reply(config.getString("Messages.cant-use-this-bot")).queue();
            return;
        }

        event.getMessage().replyEmbeds(Util.embedBuilder("Commands.help.response").build()).queue();

    }

}