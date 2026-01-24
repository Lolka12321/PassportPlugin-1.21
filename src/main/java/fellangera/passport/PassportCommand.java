package fellangera.passport;

import fellangera.passport.util.ColorUtil;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class PassportCommand implements CommandExecutor {
    private final PassportManager manager;
    private final PassportPlugin plugin;
    private final PassportRequest requestManager;
    private final PassportGUI gui;

    public PassportCommand(PassportManager manager, PassportPlugin plugin,
                           PassportRequest requestManager, PassportGUI gui) {
        this.manager = manager;
        this.plugin = plugin;
        this.requestManager = requestManager;
        this.gui = gui;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Only players can use this command!");
            return true;
        }

        // /passport
        if (args.length == 0) {
            return handleView(player);
        }

        String subCommand = args[0].toLowerCase();

        return switch (subCommand) {
            case "help" -> handleHelp(player);
            case "create" -> handleCreate(player);
            case "edit" -> handleEdit(player);
            case "reload" -> handleReload(player);
            case "check" -> handleCheck(player, args);
            case "remove" -> handleRemove(player, args);
            case "accept" -> handleAccept(player);
            case "deny" -> handleDeny(player);
            default -> handleView(player);
        };
    }

    private boolean handleView(Player player) {
        if (!player.hasPermission("passport.view")) {
            player.sendMessage(ColorUtil.toComponent(
                    plugin.lang().getString("errors.no-permission", "No permission!")));
            return true;
        }

        Passport passport = manager.getPassport(player);
        if (passport == null) {
            player.sendMessage(ColorUtil.toComponent(
                    plugin.lang().getString("errors.no-passport", "You don't have a passport yet!")));
            player.sendMessage(ColorUtil.toComponent(
                    plugin.lang().getString("errors.use-create", "Use /passport create")));
            return true;
        }
        gui.openViewGUI(player, passport);
        return true;
    }

    private boolean handleCreate(Player player) {
        if (!player.hasPermission("passport.create")) {
            player.sendMessage(ColorUtil.toComponent(
                    plugin.lang().getString("errors.no-permission", "No permission!")));
            return true;
        }

        if (manager.hasPassport(player)) {
            player.sendMessage(ColorUtil.toComponent(
                    plugin.lang().getString("errors.already-have-passport", "You already have a passport!")));
            return true;
        }

        gui.openCreateGUI(player);
        return true;
    }

    private boolean handleEdit(Player player) {
        if (!player.hasPermission("passport.edit")) {
            player.sendMessage(ColorUtil.toComponent(
                    plugin.lang().getString("errors.no-permission", "No permission!")));
            return true;
        }

        if (!manager.hasPassport(player)) {
            player.sendMessage(ColorUtil.toComponent(
                    plugin.lang().getString("errors.no-passport", "You don't have a passport yet!")));
            return true;
        }

        gui.openEditGUI(player);
        return true;
    }

    private boolean handleHelp(Player player) {
        if (!player.hasPermission("passport.help")) {
            player.sendMessage(ColorUtil.toComponent(
                    plugin.lang().getString("errors.no-permission", "No permission!")));
            return true;
        }

        player.sendMessage(ColorUtil.toComponent(
                plugin.lang().getString("help.title", "=== PassportPlugin ===")));

        if (player.hasPermission("passport.view")) {
            player.sendMessage(ColorUtil.toComponent(
                    plugin.lang().getString("help.passport", "/passport - view passport")));
        }

        if (player.hasPermission("passport.create")) {
            player.sendMessage(ColorUtil.toComponent(
                    plugin.lang().getString("help.create", "/passport create - create passport")));
        }

        if (player.hasPermission("passport.edit")) {
            player.sendMessage(ColorUtil.toComponent(
                    plugin.lang().getString("help.edit", "/passport edit - edit passport")));
        }

        if (player.hasPermission("passport.check")) {
            player.sendMessage(ColorUtil.toComponent(
                    plugin.lang().getString("help.check", "/passport check <player>")));
        }

        if (player.hasPermission("passport.remove")) {
            player.sendMessage(ColorUtil.toComponent(
                    plugin.lang().getString("help.remove", "/passport remove <player>")));
        }

        if (player.hasPermission("passport.reload")) {
            player.sendMessage(ColorUtil.toComponent(
                    plugin.lang().getString("help.reload", "/passport reload")));
        }

        return true;
    }

    private boolean handleReload(Player player) {
        if (!player.hasPermission("passport.reload")) {
            player.sendMessage(ColorUtil.toComponent(
                    plugin.lang().getString("errors.no-permission", "No permission!")));
            return true;
        }

        plugin.reloadAll();
        player.sendMessage(ColorUtil.toComponent(
                plugin.lang().getString("success.reloaded", "Configuration reloaded!")));
        return true;
    }

    private boolean handleCheck(Player player, String[] args) {
        if (!player.hasPermission("passport.check")) {
            player.sendMessage(ColorUtil.toComponent(
                    plugin.lang().getString("errors.no-permission", "No permission!")));
            return true;
        }

        if (args.length < 2) {
            player.sendMessage(ColorUtil.toComponent(
                    plugin.lang().getString("errors.usage-check", "Usage: /passport check <player>")));
            return true;
        }

        Player target = Bukkit.getPlayerExact(args[1]);
        if (target == null || !target.isOnline()) {
            player.sendMessage(ColorUtil.toComponent(
                    plugin.lang().getString("errors.player-not-found", "Player not found!")));
            return true;
        }

        if (target.equals(player)) {
            return handleView(player);
        }

        Passport passport = manager.getPassport(target);
        if (passport == null) {
            player.sendMessage(ColorUtil.toComponent(
                    plugin.lang().getString("errors.target-no-passport", "%player% doesn't have a passport!")
                            .replace("%player%", target.getName())
            ));
            return true;
        }

        requestManager.sendRequest(player, target);
        return true;
    }

    private boolean handleRemove(Player player, String[] args) {
        if (!player.hasPermission("passport.remove")) {
            player.sendMessage(ColorUtil.toComponent(
                    plugin.lang().getString("errors.no-permission", "No permission!")));
            return true;
        }

        if (args.length < 2) {
            player.sendMessage(ColorUtil.toComponent(
                    plugin.lang().getString("errors.usage-remove", "Usage: /passport remove <player>")));
            return true;
        }

        Player target = Bukkit.getPlayerExact(args[1]);
        if (target == null || !target.isOnline()) {
            player.sendMessage(ColorUtil.toComponent(
                    plugin.lang().getString("errors.player-not-found", "Player not found!")));
            return true;
        }

        Passport passport = manager.getPassport(target);
        if (passport == null) {
            player.sendMessage(ColorUtil.toComponent(
                    plugin.lang().getString("errors.target-no-passport", "%player% doesn't have a passport!")
                            .replace("%player%", target.getName())
            ));
            return true;
        }

        manager.removePassport(target);

        player.sendMessage(ColorUtil.toComponent(
                plugin.lang().getString("success.removed", "Passport removed!")
                        .replace("%player%", target.getName())
        ));

        target.sendMessage(ColorUtil.toComponent(
                plugin.lang().getString("success.your-passport-removed", "Your passport has been removed!")));
        target.kick(ColorUtil.toComponent(
                plugin.lang().getString("success.kicked-for-reregistration", "Reconnect to create new passport")));

        return true;
    }

    private boolean handleAccept(Player player) {
        requestManager.acceptRequest(player);
        return true;
    }

    private boolean handleDeny(Player player) {
        requestManager.denyRequest(player);
        return true;
    }

    public void openPassportForRequester(Player requester, Passport passport) {
        gui.openViewGUI(requester, passport);
    }
}
