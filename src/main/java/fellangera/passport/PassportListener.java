package fellangera.passport;

import fellangera.passport.util.ColorUtil;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.*;

import java.util.*;

public class PassportListener implements Listener {
    private final PassportManager manager;
    private final PassportPlugin plugin;

    private enum Step { NAME, SURNAME, AGE, REGION }

    private final Map<UUID, Step> steps = new HashMap<>();
    private final Map<UUID, String> names = new HashMap<>();
    private final Map<UUID, String> surnames = new HashMap<>();
    private final Map<UUID, Integer> ages = new HashMap<>();

    // Настройки валидации
    private static final int MIN_AGE = 1;
    private static final int MAX_AGE = 150;
    private static final int MAX_NAME_LENGTH = 32;

    public PassportListener(PassportManager manager, PassportPlugin plugin) {
        this.manager = manager;
        this.plugin = plugin;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        Passport passport = manager.getPassport(player);

        if (passport != null) {
            manager.applyName(player, passport);
            return;
        }

        startRegistration(player);
    }

    @EventHandler
    public void onMove(PlayerMoveEvent event) {
        if (!manager.hasPassport(event.getPlayer())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();

        if (!steps.containsKey(uuid)) {
            return;
        }

        event.setCancelled(true);
        String message = event.getMessage().trim();

        switch (steps.get(uuid)) {
            case NAME -> handleNameInput(player, uuid, message);
            case SURNAME -> handleSurnameInput(player, uuid, message);
            case AGE -> handleAgeInput(player, uuid, message);
            case REGION -> handleRegionInput(player, uuid, message);
        }
    }

    private void startRegistration(Player player) {
        UUID uuid = player.getUniqueId();
        steps.put(uuid, Step.NAME);
        player.sendMessage(ColorUtil.toComponent(plugin.lang().getString("input.name")));
    }

    private void handleNameInput(Player player, UUID uuid, String name) {
        if (!isValidName(name)) {
            player.sendMessage(ColorUtil.toComponent(plugin.lang().getString("errors.invalid-name")));
            return;
        }

        names.put(uuid, name);
        steps.put(uuid, Step.SURNAME);
        player.sendMessage(ColorUtil.toComponent(plugin.lang().getString("input.surname")));
    }

    private void handleSurnameInput(Player player, UUID uuid, String surname) {
        if (!isValidName(surname)) {
            player.sendMessage(ColorUtil.toComponent(plugin.lang().getString("errors.invalid-surname")));
            return;
        }

        surnames.put(uuid, surname);
        steps.put(uuid, Step.AGE);
        player.sendMessage(ColorUtil.toComponent(plugin.lang().getString("input.age")));
    }

    private void handleAgeInput(Player player, UUID uuid, String ageStr) {
        try {
            int age = Integer.parseInt(ageStr);

            if (age < MIN_AGE || age > MAX_AGE) {
                player.sendMessage(ColorUtil.toComponent(
                        plugin.lang().getString("errors.age-range")
                                .replace("%min%", String.valueOf(MIN_AGE))
                                .replace("%max%", String.valueOf(MAX_AGE))
                ));
                return;
            }

            ages.put(uuid, age);
            steps.put(uuid, Step.REGION);
            player.sendMessage(ColorUtil.toComponent(plugin.lang().getString("input.region")));

        } catch (NumberFormatException e) {
            player.sendMessage(ColorUtil.toComponent(plugin.lang().getString("errors.age-not-number")));
        }
    }

    private void handleRegionInput(Player player, UUID uuid, String region) {
        if (!isValidName(region)) {
            player.sendMessage(ColorUtil.toComponent(plugin.lang().getString("errors.invalid-region")));
            return;
        }

        // Генерируем уникальный ID
        PassportManager.PassportId id = manager.generateUniqueId();

        // Создаем паспорт
        Passport passport = new Passport(
                names.get(uuid),
                surnames.get(uuid),
                ages.get(uuid),
                region,
                id.series(),
                id.number()
        );

        manager.setPassport(player, passport);

        // Очищаем временные данные
        cleanupRegistration(uuid);

        player.sendMessage(ColorUtil.toComponent(plugin.lang().getString("success.created")));
    }

    private void cleanupRegistration(UUID uuid) {
        steps.remove(uuid);
        names.remove(uuid);
        surnames.remove(uuid);
        ages.remove(uuid);
    }

    private boolean isValidName(String name) {
        if (name == null || name.isEmpty()) {
            return false;
        }

        if (name.length() > MAX_NAME_LENGTH) {
            return false;
        }

        // Проверяем на недопустимые символы (оставляем буквы, пробелы, дефисы)
        return name.matches("[a-zA-Zа-яА-ЯёЁ\\s-]+");
    }
}
