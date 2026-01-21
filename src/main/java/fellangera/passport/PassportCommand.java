package fellangera.passport;

import fellangera.passport.util.ColorUtil;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.*;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;

public class PassportCommand implements CommandExecutor {

    private final PassportManager manager;
    private final PassportPlugin plugin;

    public PassportCommand(PassportManager m, PassportPlugin p) {
        manager = m;
        plugin = p;
    }

    @Override
    public boolean onCommand(CommandSender s, Command c, String l, String[] a) {
        if (!(s instanceof Player p)) return true;

        if (a.length == 1 && a[0].equalsIgnoreCase("help")) {
            p.sendMessage(ColorUtil.toComponent(plugin.lang().getString("help.title")));
            p.sendMessage(ColorUtil.toComponent(plugin.lang().getString("help.passport")));
            if (p.hasPermission("passport.check"))
                p.sendMessage(ColorUtil.toComponent(plugin.lang().getString("help.check")));
            if (p.hasPermission("passport.reload"))
                p.sendMessage(ColorUtil.toComponent(plugin.lang().getString("help.reload")));
            return true;
        }

        if (a.length == 1 && a[0].equalsIgnoreCase("reload")) {
            if (!p.hasPermission("passport.reload")) {
                p.sendMessage(ColorUtil.toComponent(plugin.lang().getString("errors.no-permission")));
                return true;
            }
            plugin.reloadAll();
            p.sendMessage(ColorUtil.toComponent(plugin.lang().getString("success.reloaded")));
            return true;
        }

        if (a.length == 2 && a[0].equalsIgnoreCase("check")) {
            if (!p.hasPermission("passport.check")) {
                p.sendMessage(ColorUtil.toComponent(plugin.lang().getString("errors.no-permission")));
                return true;
            }
            Player t = Bukkit.getPlayerExact(a[1]);
            if (t == null || manager.getPassport(t) == null) {
                p.sendMessage(ColorUtil.toComponent(plugin.lang().getString("errors.no-passport")));
                return true;
            }
            open(p, manager.getPassport(t));
            return true;
        }

        Passport ps = manager.getPassport(p);
        if (ps == null) {
            p.sendMessage(ColorUtil.toComponent(plugin.lang().getString("errors.no-passport")));
            return true;
        }
        open(p, ps);
        return true;
    }

    private void open(Player p, Passport ps) {
        ItemStack b = new ItemStack(Material.WRITTEN_BOOK);
        BookMeta m = (BookMeta) b.getItemMeta();
        m.setTitle(plugin.getConfig().getString("passport.book.title").replace("&",""));
        m.setAuthor(plugin.getConfig().getString("passport.book.author").replace("&",""));
        for (String r : plugin.getConfig().getStringList("passport.book.pages")) {
            m.addPages(ColorUtil.toComponent(
                    r.replace("%name%", ps.getName())
                            .replace("%surname%", ps.getSurname())
                            .replace("%age%", String.valueOf(ps.getAge()))
                            .replace("%region%", ps.getRegion())
                            .replace("%series%", ps.getSeries())
                            .replace("%number%", String.valueOf(ps.getNumber()))
            ));
        }
        b.setItemMeta(m);
        p.openBook(b);
    }
}