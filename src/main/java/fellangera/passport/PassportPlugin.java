package fellangera.passport;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;

public class PassportPlugin extends JavaPlugin {
    private static PassportPlugin instance;
    private PassportManager passportManager;
    private PassportRequest requestManager;
    private FileConfiguration lang;

    @Override
    public void onEnable() {
        instance = this;

        // Загружаем конфигурацию
        saveDefaultConfig();
        loadLanguage();

        // Инициализация менеджеров
        passportManager = new PassportManager(this);
        requestManager = new PassportRequest(this, passportManager);

        // Регистрация команд
        PassportCommand commandExecutor = new PassportCommand(passportManager, this, requestManager);
        requestManager.setCommandHandler(commandExecutor);

        getCommand("passport").setExecutor(commandExecutor);
        getCommand("passport").setTabCompleter(new PassportTabCompleter());

        // Регистрация событий
        getServer().getPluginManager().registerEvents(
                new PassportListener(passportManager, this),
                this
        );

        getLogger().info("PassportPlugin успешно загружен!");
    }

    @Override
    public void onDisable() {
        getLogger().info("PassportPlugin выгружен!");
    }

    public void reloadAll() {
        reloadConfig();
        loadLanguage();
        getLogger().info("Конфигурация и локализация перезагружены!");
    }

    private void loadLanguage() {
        String languageName = getConfig().getString("language", "en-EN");
        File languageFile = new File(getDataFolder(), languageName + ".yml");

        if (!languageFile.exists()) {
            saveResource(languageName + ".yml", false);
        }

        lang = YamlConfiguration.loadConfiguration(languageFile);
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
