package fellangera.passport;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.*;

import java.util.*;

public class PassportListener implements Listener {

    private final PassportManager manager;
    private final PassportPlugin plugin;

    private enum Step { NAME, SURNAME, AGE, REGION }

    private final Map<UUID, Step> steps = new HashMap<>();
    private final Map<UUID, String> names = new HashMap<>();
    private final Map<UUID, String> surnames = new HashMap<>();
    private final Map<UUID, Integer> ages = new HashMap<>();

    public PassportListener(PassportManager m, PassportPlugin p) {
        manager = m;
        plugin = p;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        Player p = e.getPlayer();
        Passport ps = manager.getPassport(p);
        if (ps != null) {
            manager.applyName(p, ps);
            return;
        }
        steps.put(p.getUniqueId(), Step.NAME);
        p.sendMessage(fellangera.passport.util.ColorUtil.toComponent(plugin.lang().getString("input.name")));
    }

    @EventHandler
    public void onMove(PlayerMoveEvent e) {
        if (!manager.hasPassport(e.getPlayer())) e.setCancelled(true);
    }

    @EventHandler
    public void onChat(AsyncPlayerChatEvent e) {
        Player p = e.getPlayer();
        UUID u = p.getUniqueId();
        if (!steps.containsKey(u)) return;
        e.setCancelled(true);
        String m = e.getMessage();

        switch (steps.get(u)) {
            case NAME -> {
                names.put(u, m);
                steps.put(u, Step.SURNAME);
                p.sendMessage(fellangera.passport.util.ColorUtil.toComponent(plugin.lang().getString("input.surname")));
            }
            case SURNAME -> {
                surnames.put(u, m);
                steps.put(u, Step.AGE);
                p.sendMessage(fellangera.passport.util.ColorUtil.toComponent(plugin.lang().getString("input.age")));
            }
            case AGE -> {
                try {
                    ages.put(u, Integer.parseInt(m));
                    steps.put(u, Step.REGION);
                    p.sendMessage(fellangera.passport.util.ColorUtil.toComponent(plugin.lang().getString("input.region")));
                } catch (NumberFormatException ex) {
                    p.sendMessage(fellangera.passport.util.ColorUtil.toComponent(plugin.lang().getString("errors.age-not-number")));
                }
            }
            case REGION -> {
                PassportManager.PassportId id = manager.generateUniqueId();
                manager.setPassport(p, new Passport(
                        names.get(u), surnames.get(u), ages.get(u), m,
                        id.series(), id.number()
                ));
                steps.remove(u);
                names.remove(u);
                surnames.remove(u);
                ages.remove(u);
                p.sendMessage(fellangera.passport.util.ColorUtil.toComponent(plugin.lang().getString("success.created")));
            }
        }
    }
}