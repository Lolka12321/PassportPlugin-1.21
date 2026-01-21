package fellangera.passport.api;

import fellangera.passport.Passport;
import fellangera.passport.PassportPlugin;
import org.bukkit.entity.Player;

public class PassportAPI {

    /**
     * Получить плейсхолдер паспорта
     *
     * %name%
     * %surname%
     * %age%
     * %region%
     * %passport_id%
     */
    public static String get(Player player, String placeholder) {

        Passport passport = PassportPlugin.getInstance()
                .getPassportManager()
                .getPassport(player);

        if (passport == null) return "";

        return switch (placeholder) {
            case "%name%" -> passport.getName();
            case "%surname%" -> passport.getSurname();
            case "%age%" -> String.valueOf(passport.getAge());
            case "%region%" -> passport.getRegion();
            case "%passport_id%" ->
                    passport.getSeries() + " " + passport.getNumber();
            default -> "";
        };
    }
}
