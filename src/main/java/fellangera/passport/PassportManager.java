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
    private final Map<String, Set<Integer>> usedNumbers = new HashMap<>();
    private final File file;
    private final FileConfiguration config;
    private final PassportPlugin plugin;

    public PassportManager(PassportPlugin plugin) {
        this.plugin = plugin;
        file = new File(plugin.getDataFolder(), "passports.yml");
        try {
            if (!file.exists()) {
                plugin.getDataFolder().mkdirs();
                file.createNewFile();
            }
        } catch (IOException e) {
            plugin.getLogger().severe("Failed to create passports.yml: " + e.getMessage());
        }
        config = YamlConfiguration.loadConfiguration(file);
        load();
    }

    public boolean hasPassport(Player p) {
        return passports.containsKey(p.getUniqueId());
    }

    public Passport getPassport(Player p) {
        return passports.get(p.getUniqueId());
    }

    public PassportId generateUniqueId(String countryName) {
        String series = getCountrySeries(countryName);
        if (series == null) {
            series = "XX";
        }

        usedNumbers.putIfAbsent(series, new HashSet<>());
        Set<Integer> numbers = usedNumbers.get(series);

        int maxAttempts = 1000;
        int attempts = 0;

        while (attempts < maxAttempts) {
            int n = ThreadLocalRandom.current().nextInt(100000, 1000000);

            if (!numbers.contains(n)) {
                numbers.add(n);
                return new PassportId(series, n);
            }
            attempts++;
        }

        plugin.getLogger().warning("Failed to generate unique passport ID after " + maxAttempts + " attempts");
        int n = ThreadLocalRandom.current().nextInt(100000, 1000000);
        return new PassportId(series, n);
    }

    private String getCountrySeries(String countryName) {
        var countries = plugin.getConfig().getConfigurationSection("countries");
        if (countries == null) return null;

        for (String key : countries.getKeys(false)) {
            String name = plugin.getConfig().getString("countries." + key + ".name");
            if (name != null && name.equals(countryName)) {
                return plugin.getConfig().getString("countries." + key + ".series");
            }
        }
        return null;
    }

    public void setPassport(Player p, Passport passport) {
        Passport oldPassport = passports.get(p.getUniqueId());

        if (oldPassport != null) {
            Set<Integer> oldNumbers = usedNumbers.get(oldPassport.getSeries());
            if (oldNumbers != null) {
                oldNumbers.remove(oldPassport.getNumber());
            }
        }

        passports.put(p.getUniqueId(), passport);

        usedNumbers.putIfAbsent(passport.getSeries(), new HashSet<>());
        usedNumbers.get(passport.getSeries()).add(passport.getNumber());

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
            Set<Integer> numbers = usedNumbers.get(passport.getSeries());
            if (numbers != null) {
                numbers.remove(passport.getNumber());
            }
            config.set(uuid.toString(), null);
            save();

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
            try {
                UUID u = UUID.fromString(k);
                String s = config.getString(k + ".series");
                int n = config.getInt(k + ".number");
                String name = config.getString(k + ".name");
                String surname = config.getString(k + ".surname");
                int age = config.getInt(k + ".age");
                String region = config.getString(k + ".region");

                if (s != null && name != null && surname != null && region != null) {
                    passports.put(u, new Passport(name, surname, age, region, s, n));
                    usedNumbers.putIfAbsent(s, new HashSet<>());
                    usedNumbers.get(s).add(n);
                } else {
                    plugin.getLogger().warning("Invalid passport data for UUID: " + k);
                }
            } catch (IllegalArgumentException e) {
                plugin.getLogger().warning("Invalid UUID in passports.yml: " + k);
            }
        }
    }

    private void save() {
        try {
            config.save(file);
        } catch (IOException e) {
            plugin.getLogger().severe("Failed to save passports.yml: " + e.getMessage());
        }
    }

    public record PassportId(String series, int number) {}
}
