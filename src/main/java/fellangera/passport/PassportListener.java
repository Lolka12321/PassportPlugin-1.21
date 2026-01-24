package fellangera.passport;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class PassportListener implements Listener {
    private final PassportManager manager;
    private final PassportPlugin plugin;

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
        }
    }
}
