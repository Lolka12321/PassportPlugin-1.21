package fellangera.passport.util;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;

import java.util.regex.*;

public class ColorUtil {

    private static final MiniMessage MM = MiniMessage.miniMessage();
    private static final Pattern HEX = Pattern.compile("&#([A-Fa-f0-9]{6})");

    public static Component toComponent(String s) {
        if (s == null) return Component.empty();
        Matcher m = HEX.matcher(s);
        StringBuffer b = new StringBuffer();
        while (m.find()) m.appendReplacement(b, "<#" + m.group(1) + ">");
        m.appendTail(b);
        String t = b.toString()
                .replace("&0","<black>").replace("&1","<dark_blue>")
                .replace("&2","<dark_green>").replace("&3","<dark_aqua>")
                .replace("&4","<dark_red>").replace("&5","<dark_purple>")
                .replace("&6","<gold>").replace("&7","<gray>")
                .replace("&8","<dark_gray>").replace("&9","<blue>")
                .replace("&a","<green>").replace("&b","<aqua>")
                .replace("&c","<red>").replace("&d","<light_purple>")
                .replace("&e","<yellow>").replace("&f","<white>")
                .replace("&l","<bold>").replace("&o","<italic>")
                .replace("&n","<underlined>").replace("&m","<strikethrough>")
                .replace("&k","<obfuscated>").replace("&r","<reset>");
        return MM.deserialize(t);
    }
}
