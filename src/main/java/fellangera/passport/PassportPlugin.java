package fellangera.passport;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;

public class PassportPlugin extends JavaPlugin {

    private static PassportPlugin instance;
    private PassportManager passportManager;
    private FileConfiguration lang;

    @Override
    public void onEnable() {
        instance = this;
        saveDefaultConfig();
        loadLanguage();
        passportManager = new PassportManager(this);
        getCommand("passport").setExecutor(new PassportCommand(passportManager, this));
        getCommand("passport").setTabCompleter(new PassportTabCompleter());
        getServer().getPluginManager().registerEvents(
                new PassportListener(passportManager, this), this
        );
    }

    public void reloadAll() {
        reloadConfig();
        loadLanguage();
    }

    private void loadLanguage() {
        String name = getConfig().getString("language", "en-EN");
        File file = new File(getDataFolder(), name + ".yml");
        if (!file.exists()) saveResource(name + ".yml", false);
        lang = YamlConfiguration.loadConfiguration(file);
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