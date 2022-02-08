package me.gush3l.Watermarker;

import me.gush3l.Watermarker.Commands.*;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.requests.GatewayIntent;
import org.bspfsystems.yamlconfiguration.file.FileConfiguration;

import javax.security.auth.login.LoginException;
import java.util.logging.Level;

@SuppressWarnings({"all"})
public class Main {

    public static JDA getJda() {
        return jda;
    }

    private static JDA jda;

    public static void main(String[] args) {
        long timeToStart = System.currentTimeMillis();
        Files.initialize();
        FileConfiguration config = Files.getConfig();
        Activity activity = switch (config.getString("Bot.activity.option")) {
            case "watching" -> Activity.watching(config.getString("Bot.activity.activities.watching"));
            case "playing" -> Activity.playing(config.getString("Bot.activity.activities.playing"));
            case "listening" -> Activity.listening(config.getString("Bot.activity.activities.listening"));
            case "competing" -> Activity.competing(config.getString("Bot.activity.activities.competing"));
            default -> Activity.playing(config.getString("Bot.activity.activities.default"));
        };
        try {
            JDABuilder builder = JDABuilder.createDefault(config.getString("Bot.token"));
            builder.enableIntents(GatewayIntent.GUILD_MEMBERS);
            builder.enableIntents(GatewayIntent.GUILD_PRESENCES);
            builder.setBulkDeleteSplittingEnabled(true);
            builder.setActivity(activity);
            builder.addEventListeners(new Watermarker());
            builder.addEventListeners(new HelpCommand());
            builder.addEventListeners(new Reload());
            builder.addEventListeners(new Config());
            builder.addEventListeners(new ShutDown());
            builder.addEventListeners(new Purge());
            jda = builder.build();
            jda.awaitReady();

            Util.log("Bot started successfully in "+(System.currentTimeMillis()-timeToStart)+"ms !",Level.INFO);
            Files.reloadConfig();
        }catch (LoginException | InterruptedException e){
            e.printStackTrace();
        }

    }

}
