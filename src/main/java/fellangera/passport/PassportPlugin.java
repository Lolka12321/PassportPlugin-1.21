package fellangera.passport;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

public class PassportPlugin extends JavaPlugin {
    private static PassportPlugin instance;
    private PassportManager passportManager;
    private PassportRequest requestManager;
    private PassportGUI gui;
    private FileConfiguration lang;

    @Override
    public void onEnable() {
        instance = this;

        saveDefaultConfig();
        loadLanguage();

        passportManager = new PassportManager(this);
        requestManager = new PassportRequest(this, passportManager);
        gui = new PassportGUI(this, passportManager);

        PassportCommand commandExecutor = new PassportCommand(
                passportManager, this, requestManager, gui);
        requestManager.setCommandHandler(commandExecutor);

        if (getCommand("passport") != null) {
            getCommand("passport").setExecutor(commandExecutor);
            getCommand("passport").setTabCompleter(new PassportTabCompleter());
        } else {
            getLogger().severe("Failed to register /passport command!");
        }

        getServer().getPluginManager().registerEvents(
                new PassportListener(passportManager, this),
                this
        );
        getServer().getPluginManager().registerEvents(gui, this);

        getLogger().info("PassportPlugin v1.0 successfully loaded!");
    }

    @Override
    public void onDisable() {
        getLogger().info("PassportPlugin unloaded!");
    }

    public void reloadAll() {
        reloadConfig();
        loadLanguage();
        gui.reloadGuiConfig();
        getLogger().info("Configuration and localization reloaded!");
    }

    private void loadLanguage() {
        String languageName = getConfig().getString("language", "en-EN");

        File langFolder = new File(getDataFolder(), "lang");
        if (!langFolder.exists()) {
            langFolder.mkdirs();
        }

        File languageFile = new File(langFolder, languageName + ".yml");

        if (!languageFile.exists()) {
            try {
                InputStream resourceStream = getResource(languageName + ".yml");
                if (resourceStream != null) {
                    Files.copy(resourceStream, languageFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                    resourceStream.close();
                    getLogger().info("Created language file: " + languageName + ".yml");
                } else {
                    getLogger().warning("Language file " + languageName + ".yml not found in resources!");
                    if (!languageName.equals("en-EN")) {
                        languageName = "en-EN";
                        languageFile = new File(langFolder, "en-EN.yml");
                        resourceStream = getResource("en-EN.yml");
                        if (resourceStream != null) {
                            Files.copy(resourceStream, languageFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                            resourceStream.close();
                        }
                    }
                }
            } catch (Exception e) {
                getLogger().severe("Failed to create language file: " + e.getMessage());
            }
        }

        lang = YamlConfiguration.loadConfiguration(languageFile);
        getLogger().info("Loaded language: " + languageName);
    }

    public FileConfiguration lang() {
        return lang;
    }

    public PassportManager getPassportManager() {
        return passportManager;
    }

    public static PassportPlugin getInstance() {
        return instance;
    }
}
