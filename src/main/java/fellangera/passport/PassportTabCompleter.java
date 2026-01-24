package fellangera.passport;

import org.bukkit.Bukkit;
import org.bukkit.command.*;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.stream.Collectors;

public class PassportTabCompleter implements TabCompleter {

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (!(sender instanceof Player player)) {
            return List.of();
        }

        // /passport <tab>
        if (args.length == 1) {
            List<String> suggestions = new ArrayList<>();

            suggestions.add("help");
            suggestions.add("accept");
            suggestions.add("deny");

            if (player.hasPermission("passport.check")) {
                suggestions.add("check");
            }

            if (player.hasPermission("passport.remove")) {
                suggestions.add("remove");
            }

            if (player.hasPermission("passport.reload")) {
                suggestions.add("reload");
            }

            // Фильтруем по введенному тексту
            return suggestions.stream()
                    .filter(s -> s.toLowerCase().startsWith(args[0].toLowerCase()))
                    .collect(Collectors.toList());
        }

        // /passport check <tab>
        if (args.length == 2 && args[0].equalsIgnoreCase("check")) {
            if (!player.hasPermission("passport.check")) {
                return List.of();
            }

            // Список онлайн игроков
            return Bukkit.getOnlinePlayers().stream()
                    .map(Player::getName)
                    .filter(name -> name.toLowerCase().startsWith(args[1].toLowerCase()))
                    .collect(Collectors.toList());
        }

        // /passport remove <tab>
        if (args.length == 2 && args[0].equalsIgnoreCase("remove")) {
            if (!player.hasPermission("passport.remove")) {
                return List.of();
            }

            // Список онлайн игроков
            return Bukkit.getOnlinePlayers().stream()
                    .map(Player::getName)
                    .filter(name -> name.toLowerCase().startsWith(args[1].toLowerCase()))
                    .collect(Collectors.toList());
        }

        return List.of();
    }
}
