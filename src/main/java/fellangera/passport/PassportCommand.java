package fellangera.passport;

import fellangera.passport.util.ColorUtil;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;

public class PassportCommand implements CommandExecutor {
    private final PassportManager manager;
    private final PassportPlugin plugin;
    private final PassportRequest requestManager;

    public PassportCommand(PassportManager manager, PassportPlugin plugin, PassportRequest requestManager) {
        this.manager = manager;
        this.plugin = plugin;
        this.requestManager = requestManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Only players can use this command!");
            return true;
        }

        // /passport
        if (args.length == 0) {
            return handleViewOwn(player);
        }

        String subCommand = args[0].toLowerCase();

        return switch (subCommand) {
            case "help" -> handleHelp(player);
            case "reload" -> handleReload(player);
            case "check" -> handleCheck(player, args);
            case "remove" -> handleRemove(player, args);
            case "accept" -> handleAccept(player);
            case "deny" -> handleDeny(player);
            default -> handleViewOwn(player);
        };
    }

    private boolean handleViewOwn(Player player) {
        Passport passport = manager.getPassport(player);
        if (passport == null) {
            player.sendMessage(ColorUtil.toComponent(plugin.lang().getString("errors.no-passport")));
            return true;
        }
        openPassportBook(player, passport);
        return true;
    }

    private boolean handleHelp(Player player) {
        player.sendMessage(ColorUtil.toComponent(plugin.lang().getString("help.title")));
        player.sendMessage(ColorUtil.toComponent(plugin.lang().getString("help.passport")));

        if (player.hasPermission("passport.check")) {
            player.sendMessage(ColorUtil.toComponent(plugin.lang().getString("help.check")));
        }

        if (player.hasPermission("passport.remove")) {
            player.sendMessage(ColorUtil.toComponent(plugin.lang().getString("help.remove")));
        }

        if (player.hasPermission("passport.reload")) {
            player.sendMessage(ColorUtil.toComponent(plugin.lang().getString("help.reload")));
        }

        return true;
    }

    private boolean handleReload(Player player) {
        if (!player.hasPermission("passport.reload")) {
            player.sendMessage(ColorUtil.toComponent(plugin.lang().getString("errors.no-permission")));
            return true;
        }

        plugin.reloadAll();
        player.sendMessage(ColorUtil.toComponent(plugin.lang().getString("success.reloaded")));
        return true;
    }

    private boolean handleCheck(Player player, String[] args) {
        if (!player.hasPermission("passport.check")) {
            player.sendMessage(ColorUtil.toComponent(plugin.lang().getString("errors.no-permission")));
            return true;
        }

        if (args.length < 2) {
            player.sendMessage(ColorUtil.toComponent(plugin.lang().getString("errors.usage-check")));
            return true;
        }

        Player target = Bukkit.getPlayerExact(args[1]);
        if (target == null) {
            player.sendMessage(ColorUtil.toComponent(plugin.lang().getString("errors.player-not-found")));
            return true;
        }

        if (target.equals(player)) {
            return handleViewOwn(player);
        }

        Passport passport = manager.getPassport(target);
        if (passport == null) {
            player.sendMessage(ColorUtil.toComponent(
                    plugin.lang().getString("errors.target-no-passport")
                            .replace("%player%", target.getName())
            ));
            return true;
        }

        // Отправляем запрос на просмотр
        requestManager.sendRequest(player, target);
        return true;
    }

    private boolean handleRemove(Player player, String[] args) {
        if (!player.hasPermission("passport.remove")) {
            player.sendMessage(ColorUtil.toComponent(plugin.lang().getString("errors.no-permission")));
            return true;
        }

        if (args.length < 2) {
            player.sendMessage(ColorUtil.toComponent(plugin.lang().getString("errors.usage-remove")));
            return true;
        }

        Player target = Bukkit.getPlayerExact(args[1]);
        if (target == null) {
            player.sendMessage(ColorUtil.toComponent(plugin.lang().getString("errors.player-not-found")));
            return true;
        }

        Passport passport = manager.getPassport(target);
        if (passport == null) {
            player.sendMessage(ColorUtil.toComponent(
                    plugin.lang().getString("errors.target-no-passport")
                            .replace("%player%", target.getName())
            ));
            return true;
        }

        manager.removePassport(target);

        player.sendMessage(ColorUtil.toComponent(
                plugin.lang().getString("success.removed")
                        .replace("%player%", target.getName())
        ));

        target.sendMessage(ColorUtil.toComponent(plugin.lang().getString("success.your-passport-removed")));
        target.kick(ColorUtil.toComponent(plugin.lang().getString("success.kicked-for-reregistration")));

        return true;
    }

    private boolean handleAccept(Player player) {
        if (requestManager.acceptRequest(player)) {
            Passport passport = manager.getPassport(player);
            if (passport != null) {
                // Находим запросившего игрока через активные запросы
                // Так как мы только что приняли, нужно открыть книгу
                // Но запрос уже удален из мапы, поэтому открываем через событие
                // Лучше переделать логику
            }
        }
        return true;
    }

    private boolean handleDeny(Player player) {
        requestManager.denyRequest(player);
        return true;
    }

    private void openPassportBook(Player viewer, Passport passport) {
        ItemStack book = new ItemStack(Material.WRITTEN_BOOK);
        BookMeta meta = (BookMeta) book.getItemMeta();

        String title = plugin.lang().getString("book.title", "Passport");
        String author = plugin.lang().getString("book.author", "Government");

        meta.setTitle(title.replace("&", ""));
        meta.setAuthor(author.replace("&", ""));

        for (String rawPage : plugin.lang().getStringList("book.pages")) {
            String page = rawPage
                    .replace("%name%", passport.getName())
                    .replace("%surname%", passport.getSurname())
                    .replace("%age%", String.valueOf(passport.getAge()))
                    .replace("%region%", passport.getRegion())
                    .replace("%series%", passport.getSeries())
                    .replace("%number%", String.valueOf(passport.getNumber()))
                    .replace("%passport_id%", passport.getPassportId());

            meta.addPages(ColorUtil.toComponent(page));
        }

        book.setItemMeta(meta);
        viewer.openBook(book);
    }

    public void openPassportBookForRequester(Player requester, Passport passport) {
        openPassportBook(requester, passport);
    }
}
