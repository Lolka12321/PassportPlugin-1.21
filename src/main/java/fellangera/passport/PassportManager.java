package fellangera.passport;

import net.kyori.adventure.text.Component;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

public class PassportManager {

    private final Map<UUID, Passport> passports = new HashMap<>();
    private final Set<String> usedIds = new HashSet<>();
    private final File file;
    private final FileConfiguration config;

    public PassportManager(PassportPlugin plugin) {
        file = new File(plugin.getDataFolder(), "passports.yml");
        try {
            if (!file.exists()) {
                plugin.getDataFolder().mkdirs();
                file.createNewFile();
            }
        } catch (IOException ignored) {}
        config = YamlConfiguration.loadConfiguration(file);
        load();
    }

    public boolean hasPassport(Player p) {
        return passports.containsKey(p.getUniqueId());
    }

    public Passport getPassport(Player p) {
        return passports.get(p.getUniqueId());
    }

    public PassportId generateUniqueId() {
        while (true) {
            String s = "" + (char)('A' + r()) + (char)('A' + r());
            int n = ThreadLocalRandom.current().nextInt(100000, 1000000);
            String key = s + "-" + n;
            if (!usedIds.contains(key)) return new PassportId(s, n);
        }
    }

    private int r() {
        return ThreadLocalRandom.current().nextInt(26);
    }

    public void setPassport(Player p, Passport passport) {
        passports.put(p.getUniqueId(), passport);
        usedIds.add(passport.getSeries() + "-" + passport.getNumber());
        String k = p.getUniqueId().toString();
        config.set(k + ".name", passport.getName());
        config.set(k + ".surname", passport.getSurname());
        config.set(k + ".age", passport.getAge());
        config.set(k + ".region", passport.getRegion());
        config.set(k + ".series", passport.getSeries());
        config.set(k + ".number", passport.getNumber());
        save();
        applyName(p, passport);
    }

    public void removePassport(Player p) {
        UUID uuid = p.getUniqueId();
        Passport passport = passports.remove(uuid);

        if (passport != null) {
            usedIds.remove(passport.getSeries() + "-" + passport.getNumber());
            config.set(uuid.toString(), null);
            save();

            // Сбрасываем отображаемое имя
            p.displayName(Component.text(p.getName()));
            p.playerListName(Component.text(p.getName()));
            p.customName(null);
            p.setCustomNameVisible(false);
        }
    }

    public void applyName(Player p, Passport passport) {
        Component c = Component.text(passport.getName() + ", " + passport.getAge());
        p.displayName(c);
        p.playerListName(c);
        p.customName(c);
        p.setCustomNameVisible(true);
    }

    private void load() {
        for (String k : config.getKeys(false)) {
            UUID u = UUID.fromString(k);
            String s = config.getString(k + ".series");
            int n = config.getInt(k + ".number");
            passports.put(u, new Passport(
                    config.getString(k + ".name"),
                    config.getString(k + ".surname"),
                    config.getInt(k + ".age"),
                    config.getString(k + ".region"),
                    s, n
            ));
            usedIds.add(s + "-" + n);
        }
    }

    private void save() {
        try { config.save(file); } catch (IOException ignored) {}
    }

    public record PassportId(String series, int number) {}
}
