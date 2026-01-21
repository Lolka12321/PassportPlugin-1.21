package fellangera.passport;

import org.bukkit.Bukkit;
import org.bukkit.command.*;
import org.bukkit.entity.Player;

import java.util.*;

public class PassportTabCompleter implements TabCompleter {

    @Override
    public List<String> onTabComplete(CommandSender s, Command c, String a, String[] args) {
        if (!(s instanceof Player p)) return List.of();
        if (args.length == 1) {
            List<String> l = new ArrayList<>();
            if (p.hasPermission("passport.check")) l.add("check");
            if (p.hasPermission("passport.reload")) l.add("reload");
            l.add("help");
            return l;
        }
        if (args.length == 2 && args[0].equalsIgnoreCase("check") && p.hasPermission("passport.check")) {
            List<String> l = new ArrayList<>();
            Bukkit.getOnlinePlayers().forEach(pl -> l.add(pl.getName()));
            return l;
        }
        return List.of();
    }
}