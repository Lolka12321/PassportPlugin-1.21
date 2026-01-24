package fellangera.passport;

import com.destroystokyo.paper.profile.PlayerProfile;
import com.destroystokyo.paper.profile.ProfileProperty;
import fellangera.passport.util.ColorUtil;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import net.wesjd.anvilgui.AnvilGUI;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.*;

public class PassportGUI implements Listener {
    private final PassportPlugin plugin;
    private final PassportManager manager;
    private final Map<UUID, GUISession> sessions = new HashMap<>();

    public PassportGUI(PassportPlugin plugin, PassportManager manager) {
        this.plugin = plugin;
        this.manager = manager;
    }

    public void reloadGuiConfig() {
        // Метод для перезагрузки конфигурации GUI (пока не используется)
    }

    public void openCreateGUI(Player player) {
        GUISession session = sessions.computeIfAbsent(player.getUniqueId(),
                k -> new GUISession(player, GUIType.CREATE));

        Inventory inv = Bukkit.createInventory(null, 36,
                ColorUtil.toComponent(plugin.lang().getString("gui.create.title")));

        fillCreateEditGUI(inv, session);
        player.openInventory(inv);
    }

    public void openEditGUI(Player player) {
        Passport passport = manager.getPassport(player);
        if (passport == null) return;

        GUISession session = sessions.get(player.getUniqueId());
        if (session == null || session.type != GUIType.EDIT) {
            session = new GUISession(player, GUIType.EDIT);
            session.name = passport.getName();
            session.surname = passport.getSurname();
            session.age = passport.getAge();
            session.country = passport.getRegion();
            sessions.put(player.getUniqueId(), session);
        }

        Inventory inv = Bukkit.createInventory(null, 36,
                ColorUtil.toComponent(plugin.lang().getString("gui.edit.title")));

        fillCreateEditGUI(inv, session);
        player.openInventory(inv);
    }

    private void fillCreateEditGUI(Inventory inv, GUISession session) {
        boolean isCreate = session.type == GUIType.CREATE;
        String prefix = isCreate ? "gui.create." : "gui.edit.";

        int nameSlot = plugin.getConfig().getInt(isCreate ? "gui.create.name-slot" : "gui.edit.name-slot");
        int surnameSlot = plugin.getConfig().getInt(isCreate ? "gui.create.surname-slot" : "gui.edit.surname-slot");
        int ageSlot = plugin.getConfig().getInt(isCreate ? "gui.create.age-slot" : "gui.edit.age-slot");
        int countrySlot = plugin.getConfig().getInt(isCreate ? "gui.create.country-slot" : "gui.edit.country-slot");
        int confirmSlot = plugin.getConfig().getInt(isCreate ? "gui.create.confirm-slot" : "gui.edit.save-slot");
        int cancelSlot = plugin.getConfig().getInt(isCreate ? "gui.create.cancel-slot" : "gui.edit.cancel-slot");

        inv.setItem(nameSlot, createButton(Material.NAME_TAG,
                plugin.lang().getString(prefix + "name-button"),
                session.name != null ?
                        List.of(Component.empty(), ColorUtil.toComponent("<!italic><#A78BFA>" + session.name)) :
                        List.of(Component.empty(), ColorUtil.toComponent(plugin.lang().getString("gui.create.click-to-set")))));

        inv.setItem(surnameSlot, createButton(Material.NAME_TAG,
                plugin.lang().getString(prefix + "surname-button"),
                session.surname != null ?
                        List.of(Component.empty(), ColorUtil.toComponent("<!italic><#A78BFA>" + session.surname)) :
                        List.of(Component.empty(), ColorUtil.toComponent(plugin.lang().getString("gui.create.click-to-set")))));

        inv.setItem(ageSlot, createButton(Material.CLOCK,
                plugin.lang().getString(prefix + "age-button"),
                session.age != null ?
                        List.of(Component.empty(), ColorUtil.toComponent("<!italic><#FBBF24>" + String.valueOf(session.age))) :
                        List.of(Component.empty(), ColorUtil.toComponent(plugin.lang().getString("gui.create.click-to-set")))));

        ItemStack countryItem;
        if (session.country != null) {
            String countryKey = getCountryKey(session.country);
            if (countryKey != null && !countryKey.isEmpty()) {
                countryItem = createCountrySkull(countryKey, session.country);
                ItemMeta meta = countryItem.getItemMeta();
                if (meta != null) {
                    meta.displayName(ColorUtil.toComponent("<!italic>" + plugin.lang().getString(prefix + "country-button")));
                    meta.lore(List.of(Component.empty(), ColorUtil.toComponent("<!italic><#A78BFA>" + session.country)));
                    countryItem.setItemMeta(meta);
                }
            } else {
                countryItem = createButton(Material.MAP,
                        plugin.lang().getString(prefix + "country-button"),
                        List.of(Component.empty(), ColorUtil.toComponent("<!italic><#A78BFA>" + session.country)));
            }
        } else {
            countryItem = createButton(Material.MAP,
                    plugin.lang().getString(prefix + "country-button"),
                    List.of(Component.empty(), ColorUtil.toComponent(plugin.lang().getString("gui.create.click-to-select"))));
        }
        inv.setItem(countrySlot, countryItem);

        if (session.isComplete()) {
            inv.setItem(confirmSlot, createButton(Material.EMERALD_BLOCK,
                    plugin.lang().getString(isCreate ? "gui.create.confirm" : "gui.edit.save"),
                    List.of(Component.empty(), ColorUtil.toComponent(plugin.lang().getString(isCreate ? "gui.create.confirm-lore" : "gui.edit.save-lore")))));
        }

        inv.setItem(cancelSlot, createButton(Material.BARRIER,
                plugin.lang().getString("gui.cancel"), List.of()));
    }

    public void openViewGUI(Player player, Passport passport) {
        Inventory inv = Bukkit.createInventory(null, 36,
                ColorUtil.toComponent(plugin.lang().getString("gui.view.title")));

        int nameSlot = plugin.getConfig().getInt("gui.view.name-slot");
        int surnameSlot = plugin.getConfig().getInt("gui.view.surname-slot");
        int ageSlot = plugin.getConfig().getInt("gui.view.age-slot");
        int countrySlot = plugin.getConfig().getInt("gui.view.country-slot");
        int seriesSlot = plugin.getConfig().getInt("gui.view.series-slot");
        int numberSlot = plugin.getConfig().getInt("gui.view.number-slot");

        inv.setItem(nameSlot, createButton(Material.PLAYER_HEAD,
                plugin.lang().getString("gui.view.name"),
                List.of(Component.empty(), ColorUtil.toComponent("<!italic><#A78BFA>" + passport.getName()))));

        inv.setItem(surnameSlot, createButton(Material.PLAYER_HEAD,
                plugin.lang().getString("gui.view.surname"),
                List.of(Component.empty(), ColorUtil.toComponent("<!italic><#A78BFA>" + passport.getSurname()))));

        inv.setItem(ageSlot, createButton(Material.CLOCK,
                plugin.lang().getString("gui.view.age"),
                List.of(Component.empty(), ColorUtil.toComponent("<!italic><#FBBF24>" + String.valueOf(passport.getAge())))));

        String countryKey = getCountryKey(passport.getRegion());
        ItemStack countrySkull = createCountrySkull(countryKey, passport.getRegion());
        ItemMeta countryMeta = countrySkull.getItemMeta();
        if (countryMeta != null) {
            countryMeta.displayName(ColorUtil.toComponent("<!italic>" + plugin.lang().getString("gui.view.country")));
            countryMeta.lore(List.of(Component.empty(), ColorUtil.toComponent("<!italic><#A78BFA>" + passport.getRegion())));
            countrySkull.setItemMeta(countryMeta);
        }
        inv.setItem(countrySlot, countrySkull);

        inv.setItem(seriesSlot, createButton(Material.PAPER,
                plugin.lang().getString("gui.view.series"),
                List.of(Component.empty(), ColorUtil.toComponent("<!italic><gray>" + passport.getSeries()))));

        inv.setItem(numberSlot, createButton(Material.PAPER,
                plugin.lang().getString("gui.view.number"),
                List.of(Component.empty(), ColorUtil.toComponent("<!italic><gray>" + String.valueOf(passport.getNumber())))));

        player.openInventory(inv);
    }

    public void openCountrySelectGUI(Player player) {
        ConfigurationSection countries = plugin.getConfig().getConfigurationSection("countries");
        if (countries == null) return;

        Set<String> countryKeys = countries.getKeys(false);
        int size = plugin.getConfig().getInt("gui.country.size");
        int backSlot = plugin.getConfig().getInt("gui.country.back-slot");

        Inventory inv = Bukkit.createInventory(null, size,
                ColorUtil.toComponent(plugin.lang().getString("gui.country.title")));

        int slot = 0;
        for (String key : countryKeys) {
            if (slot >= backSlot) break;

            String countryName = plugin.getConfig().getString("countries." + key + ".name");
            ItemStack skull = createCountrySkullForSelection(key, countryName);
            inv.setItem(slot++, skull);
        }

        inv.setItem(backSlot, createButton(Material.ARROW,
                plugin.lang().getString("gui.back"),
                List.of(Component.empty(), ColorUtil.toComponent("<gray>Return to passport"))));

        player.openInventory(inv);
    }

    @EventHandler
    public void onClick(InventoryClickEvent e) {
        if (!(e.getWhoClicked() instanceof Player player)) return;

        Component titleComponent = e.getView().title();
        String title = PlainTextComponentSerializer.plainText().serialize(titleComponent);

        if (!isPassportGUI(title)) return;

        e.setCancelled(true);

        ItemStack item = e.getCurrentItem();
        if (item == null || item.getType() == Material.AIR) return;

        int slot = e.getSlot();
        int backSlot = plugin.getConfig().getInt("gui.country.back-slot");

        String countryTitlePlain = PlainTextComponentSerializer.plainText()
                .serialize(ColorUtil.toComponent(plugin.lang().getString("gui.country.title")));

        if (title.contains(countryTitlePlain)) {
            if (slot == backSlot) {
                player.closeInventory();
                GUISession currentSession = sessions.get(player.getUniqueId());
                if (currentSession != null) {
                    Bukkit.getScheduler().runTaskLater(plugin, () -> {
                        if (currentSession.type == GUIType.CREATE) {
                            openCreateGUI(player);
                        } else {
                            openEditGUI(player);
                        }
                    }, 2L);
                }
            } else {
                handleCountrySelect(player, item);
            }
            return;
        }

        GUISession session = sessions.get(player.getUniqueId());
        if (session == null) return;

        if (session.type == GUIType.CREATE || session.type == GUIType.EDIT) {
            handleEditClick(player, session, slot);
        }
    }

    private void handleEditClick(Player player, GUISession session, int slot) {
        boolean isCreate = session.type == GUIType.CREATE;

        int nameSlot = plugin.getConfig().getInt(isCreate ? "gui.create.name-slot" : "gui.edit.name-slot");
        int surnameSlot = plugin.getConfig().getInt(isCreate ? "gui.create.surname-slot" : "gui.edit.surname-slot");
        int ageSlot = plugin.getConfig().getInt(isCreate ? "gui.create.age-slot" : "gui.edit.age-slot");
        int countrySlot = plugin.getConfig().getInt(isCreate ? "gui.create.country-slot" : "gui.edit.country-slot");
        int confirmSlot = plugin.getConfig().getInt(isCreate ? "gui.create.confirm-slot" : "gui.edit.save-slot");
        int cancelSlot = plugin.getConfig().getInt(isCreate ? "gui.create.cancel-slot" : "gui.edit.cancel-slot");

        if (slot == nameSlot) {
            openAnvilInput(player, "name", session.name);
        } else if (slot == surnameSlot) {
            openAnvilInput(player, "surname", session.surname);
        } else if (slot == ageSlot) {
            openAnvilInput(player, "age", session.age != null ? String.valueOf(session.age) : null);
        } else if (slot == countrySlot) {
            player.closeInventory();
            Bukkit.getScheduler().runTaskLater(plugin, () -> openCountrySelectGUI(player), 2L);
        } else if (slot == confirmSlot) {
            if (session.isComplete()) {
                createOrUpdatePassport(player, session);
            }
        } else if (slot == cancelSlot) {
            player.closeInventory();
            sessions.remove(player.getUniqueId());
        }
    }

    private void handleCountrySelect(Player player, ItemStack item) {
        GUISession session = sessions.get(player.getUniqueId());
        if (session == null) return;

        ItemMeta meta = item.getItemMeta();
        if (meta == null) return;

        String selectedCountry = null;

        List<Component> lore = meta.lore();
        if (lore != null && lore.size() > 1) {
            selectedCountry = PlainTextComponentSerializer.plainText().serialize(lore.get(1));
        } else {
            String displayName = PlainTextComponentSerializer.plainText().serialize(meta.displayName());

            ConfigurationSection countries = plugin.getConfig().getConfigurationSection("countries");
            if (countries != null) {
                for (String key : countries.getKeys(false)) {
                    String countryName = plugin.getConfig().getString("countries." + key + ".name");
                    if (countryName != null && countryName.equals(displayName)) {
                        selectedCountry = countryName;
                        break;
                    }
                }
            }
        }

        if (selectedCountry != null) {
            session.country = selectedCountry;
        }

        player.closeInventory();

        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            if (session.type == GUIType.CREATE) {
                openCreateGUI(player);
            } else {
                openEditGUI(player);
            }
        }, 2L);
    }

    private void openAnvilInput(Player player, String field, String currentValue) {
        player.closeInventory();

        String title = switch (field) {
            case "name" -> plugin.lang().getString("gui.anvil.name-title");
            case "surname" -> plugin.lang().getString("gui.anvil.surname-title");
            case "age" -> plugin.lang().getString("gui.anvil.age-title");
            default -> "Input";
        };

        // Создаем бумагу со специальным названием (один пробел)
        ItemStack paper = new ItemStack(Material.PAPER);
        ItemMeta meta = paper.getItemMeta();
        if (meta != null) {
            meta.displayName(Component.text(" "));
            paper.setItemMeta(meta);
        }

        new AnvilGUI.Builder()
                .onClose(p -> {
                    GUISession session = sessions.get(player.getUniqueId());
                    if (session != null) {
                        Bukkit.getScheduler().runTaskLater(plugin, () -> {
                            if (session.type == GUIType.CREATE) {
                                openCreateGUI(player);
                            } else {
                                openEditGUI(player);
                            }
                        }, 1L);
                    }
                })
                .onClick((clickSlot, stateSnapshot) -> {
                    if (clickSlot != AnvilGUI.Slot.OUTPUT) {
                        return Collections.emptyList();
                    }

                    GUISession session = sessions.get(player.getUniqueId());
                    if (session == null) {
                        return List.of(AnvilGUI.ResponseAction.close());
                    }

                    String input = stateSnapshot.getText();
                    if (input == null || input.trim().isEmpty()) {
                        return Collections.emptyList();
                    }

                    input = input.trim();

                    boolean valid = switch (field) {
                        case "name", "surname" -> {
                            session.setField(field, input);
                            yield true;
                        }
                        case "age" -> {
                            try {
                                int age = Integer.parseInt(input);
                                if (age >= 1 && age <= 150) {
                                    session.age = age;
                                    yield true;
                                } else {
                                    player.sendMessage(ColorUtil.toComponent(
                                            plugin.lang().getString("errors.age-range")
                                                    .replace("%min%", "1")
                                                    .replace("%max%", "150")));
                                    yield false;
                                }
                            } catch (NumberFormatException ex) {
                                player.sendMessage(ColorUtil.toComponent(
                                        plugin.lang().getString("errors.age-not-number")));
                                yield false;
                            }
                        }
                        default -> false;
                    };

                    if (valid) {
                        return List.of(AnvilGUI.ResponseAction.close());
                    }

                    return Collections.emptyList();
                })
                .text(currentValue != null ? currentValue : " ")
                .title(title)
                .itemLeft(paper)
                .plugin(plugin)
                .open(player);
    }

    private void createOrUpdatePassport(Player player, GUISession session) {
        if (session.type == GUIType.CREATE) {
            PassportManager.PassportId id = manager.generateUniqueId(session.country);
            Passport passport = new Passport(
                    session.name, session.surname, session.age, session.country,
                    id.series(), id.number()
            );
            manager.setPassport(player, passport);
            player.sendMessage(ColorUtil.toComponent(
                    plugin.lang().getString("success.created")));
        } else {
            Passport oldPassport = manager.getPassport(player);
            if (oldPassport != null) {
                PassportManager.PassportId id;

                if (!oldPassport.getRegion().equals(session.country)) {
                    id = manager.generateUniqueId(session.country);
                } else {
                    id = new PassportManager.PassportId(oldPassport.getSeries(), oldPassport.getNumber());
                }

                Passport newPassport = new Passport(
                        session.name, session.surname, session.age, session.country,
                        id.series(), id.number()
                );
                manager.setPassport(player, newPassport);
                player.sendMessage(ColorUtil.toComponent(
                        plugin.lang().getString("success.edited")));
            }
        }

        player.closeInventory();
        sessions.remove(player.getUniqueId());
    }

    private boolean isPassportGUI(String title) {
        if (title == null || title.isEmpty()) return false;

        String createTitle = PlainTextComponentSerializer.plainText()
                .serialize(ColorUtil.toComponent(plugin.lang().getString("gui.create.title")));
        String editTitle = PlainTextComponentSerializer.plainText()
                .serialize(ColorUtil.toComponent(plugin.lang().getString("gui.edit.title")));
        String viewTitle = PlainTextComponentSerializer.plainText()
                .serialize(ColorUtil.toComponent(plugin.lang().getString("gui.view.title")));
        String countryTitle = PlainTextComponentSerializer.plainText()
                .serialize(ColorUtil.toComponent(plugin.lang().getString("gui.country.title")));

        return title.equals(createTitle) ||
                title.equals(editTitle) ||
                title.equals(viewTitle) ||
                title.equals(countryTitle);
    }

    private ItemStack createButton(Material material, String name, List<Component> lore) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.displayName(ColorUtil.toComponent("<!italic>" + name));

            List<Component> nonItalicLore = new ArrayList<>();
            for (Component line : lore) {
                nonItalicLore.add(Component.text().append(line).decoration(net.kyori.adventure.text.format.TextDecoration.ITALIC, false).build());
            }
            meta.lore(nonItalicLore);

            item.setItemMeta(meta);
        }
        return item;
    }

    private ItemStack createCountrySkullForSelection(String countryKey, String displayName) {
        ItemStack skull = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta meta = (SkullMeta) skull.getItemMeta();

        if (meta != null) {
            String texture = plugin.getConfig().getString("countries." + countryKey + ".texture");
            if (texture != null && !texture.isEmpty()) {
                try {
                    PlayerProfile profile = Bukkit.createProfile(UUID.randomUUID());
                    profile.setProperty(new ProfileProperty("textures", texture));
                    meta.setPlayerProfile(profile);
                } catch (Exception e) {
                    plugin.getLogger().warning("Failed to set skull texture for " + countryKey + ": " + e.getMessage());
                }
            }

            meta.displayName(ColorUtil.toComponent("<!italic><#A78BFA>" + displayName));
            meta.lore(List.of(Component.empty(), ColorUtil.toComponent("<!italic><#A78BFA>" + displayName)));

            skull.setItemMeta(meta);
        }
        return skull;
    }

    private ItemStack createCountrySkull(String countryKey, String displayName) {
        ItemStack skull = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta meta = (SkullMeta) skull.getItemMeta();

        if (meta != null) {
            String texture = plugin.getConfig().getString("countries." + countryKey + ".texture");
            if (texture != null && !texture.isEmpty()) {
                try {
                    PlayerProfile profile = Bukkit.createProfile(UUID.randomUUID());
                    profile.setProperty(new ProfileProperty("textures", texture));
                    meta.setPlayerProfile(profile);
                } catch (Exception e) {
                    plugin.getLogger().warning("Failed to set skull texture for " + countryKey + ": " + e.getMessage());
                }
            }

            meta.displayName(ColorUtil.toComponent("<!italic>" + displayName));
            skull.setItemMeta(meta);
        }
        return skull;
    }

    private String getCountryKey(String countryName) {
        ConfigurationSection countries = plugin.getConfig().getConfigurationSection("countries");
        if (countries == null) return "";

        for (String key : countries.getKeys(false)) {
            String name = plugin.getConfig().getString("countries." + key + ".name");
            if (name != null && name.equals(countryName)) {
                return key;
            }
        }
        return "";
    }

    public void removeSession(UUID uuid) {
        sessions.remove(uuid);
    }

    private enum GUIType {
        CREATE, EDIT, VIEW
    }

    private static class GUISession {
        final Player player;
        final GUIType type;
        String name;
        String surname;
        Integer age;
        String country;

        GUISession(Player player, GUIType type) {
            this.player = player;
            this.type = type;
        }

        void setField(String field, String value) {
            switch (field) {
                case "name" -> this.name = value;
                case "surname" -> this.surname = value;
            }
        }

        boolean isComplete() {
            return name != null && surname != null && age != null && country != null;
        }
    }
}