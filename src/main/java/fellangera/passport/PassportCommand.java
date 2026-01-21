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

    public PassportCommand(PassportManager manager, PassportPlugin plugin) {
        this.manager = manager;
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if (!(sender instanceof Player player)) {
            return true;
        }

        // /passport help
        if (args.length == 1 && args[0].equalsIgnoreCase("help")) {

            player.sendMessage(
                    ColorUtil.toComponent(plugin.lang().getString("help.title"))
            );

            player.sendMessage(
                    ColorUtil.toComponent(plugin.lang().getString("help.passport"))
            );

            if (player.hasPermission("passport.check")) {
                player.sendMessage(
                        ColorUtil.toComponent(plugin.lang().getString("help.check"))
                );
            }

            if (player.hasPermission("passport.reload")) {
                player.sendMessage(
                        ColorUtil.toComponent(plugin.lang().getString("help.reload"))
                );
            }

            return true;
        }

        // /passport reload
        if (args.length == 1 && args[0].equalsIgnoreCase("reload")) {

            if (!player.hasPermission("passport.reload")) {
                player.sendMessage(
                        ColorUtil.toComponent(plugin.lang().getString("errors.no-permission"))
                );
                return true;
            }

            plugin.reloadAll();

            player.sendMessage(
                    ColorUtil.toComponent(plugin.lang().getString("success.reloaded"))
            );

            return true;
        }

        // /passport check <player>
        if (args.length == 2 && args[0].equalsIgnoreCase("check")) {

            if (!player.hasPermission("passport.check")) {
                player.sendMessage(
                        ColorUtil.toComponent(plugin.lang().getString("errors.no-permission"))
                );
                return true;
            }

            Player target = Bukkit.getPlayerExact(args[1]);
            if (target == null) {
                player.sendMessage(
                        ColorUtil.toComponent(plugin.lang().getString("errors.player-not-found"))
                );
                return true;
            }

            Passport passport = manager.getPassport(target);
            if (passport == null) {
                player.sendMessage(
                        ColorUtil.toComponent(plugin.lang().getString("errors.no-passport"))
                );
                return true;
            }

            openPassportBook(player, passport);
            return true;
        }

        // /passport
        if (args.length == 0) {

            Passport passport = manager.getPassport(player);
            if (passport == null) {
                player.sendMessage(
                        ColorUtil.toComponent(plugin.lang().getString("errors.no-passport"))
                );
                return true;
            }

            openPassportBook(player, passport);
            return true;
        }

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
                    .replace("%passport_id%",
                            passport.getSeries() + " " + passport.getNumber());

            meta.addPages(ColorUtil.toComponent(page));
        }

        book.setItemMeta(meta);
        viewer.openBook(book);
    }
}
