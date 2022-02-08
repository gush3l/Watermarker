package me.gush3l.Watermarker;

import org.bspfsystems.yamlconfiguration.configuration.InvalidConfigurationException;
import org.bspfsystems.yamlconfiguration.file.FileConfiguration;
import org.bspfsystems.yamlconfiguration.file.YamlConfiguration;

import java.io.*;
import java.util.logging.Level;

@SuppressWarnings({"all"})
public class Files {

    private static FileConfiguration config;

    public static FileConfiguration getConfig() {
        return config;
    }

    public static File getConfigFile() {
        return configFile;
    }

    private static File configFile;

    public static void initialize() {
        config = new YamlConfiguration();
        try {
            Util.log("Loading config.yml for the Discord bot...", Level.INFO);
            config.load(createConfig());
        }catch (IOException | InvalidConfigurationException e){
            Util.log("Failed to load the config/storage file!",Level.SEVERE);
            e.printStackTrace();
            Main.getJda().shutdown();
            System.exit(0);
        }
    }

    public static void reloadConfig(){
        try{
            config.save(Files.getConfigFile());
            config.load(configFile);
        }catch (IOException | InvalidConfigurationException e){
            Util.log("Failed to reload the config file!",Level.SEVERE);
            e.printStackTrace();
        }
    }

    public static File createConfig() {
        try{
            String configPath = getProgramPath()+"/config.yml";
            File configFileF = new File(configPath);
            configFile = configFileF;
            if (configFileF.createNewFile()){
                Util.log("The config hasn't been found in the folder where the bot is running, so a new one has been created!",Level.WARNING);
                InputStream jarConfigFile = Files.class.getResourceAsStream("/config.yml");
                try (FileOutputStream outputStream = new FileOutputStream(configFileF)) {
                    int read;
                    byte[] bytes = new byte[1024];

                    while ((read = jarConfigFile.read(bytes)) != -1) {
                        outputStream.write(bytes, 0, read);
                    }
                }
            }
            else{
                Util.log("Config has been found!",Level.INFO);
            }
            return configFileF;
        } catch (IOException e){
            Util.log("Failed to create the config file!",Level.SEVERE);
            e.printStackTrace();
            return null;
        }
    }

    public static String getProgramPath() {
        String currentdir = System.getProperty("user.dir");
        currentdir = currentdir.replace("\\", "/");
        return currentdir;
    }

}
