package fellangera.passport;

import fellangera.passport.util.ColorUtil;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PassportRequest {
    private final PassportPlugin plugin;
    private final PassportManager manager;
    private final Map<UUID, Request> activeRequests = new HashMap<>();
    private PassportCommand commandHandler;

    public PassportRequest(PassportPlugin plugin, PassportManager manager) {
        this.plugin = plugin;
        this.manager = manager;
    }

    public void setCommandHandler(PassportCommand handler) {
        this.commandHandler = handler;
    }

    public void sendRequest(Player requester, Player target) {
        UUID targetId = target.getUniqueId();

        // Удаляем предыдущий запрос если есть
        if (activeRequests.containsKey(targetId)) {
            activeRequests.get(targetId).cancel();
        }

        Request request = new Request(requester, target);
        activeRequests.put(targetId, request);

        // Отправляем сообщение цели
        String message = plugin.lang().getString("request.question")
                .replace("%player%", requester.getName());
        target.sendMessage(ColorUtil.toComponent(message));

        // Создаем интерактивные кнопки
        Component accept = ColorUtil.toComponent(plugin.lang().getString("request.accept-button"))
                .clickEvent(ClickEvent.runCommand("/passport accept"));
        Component deny = ColorUtil.toComponent(plugin.lang().getString("request.deny-button"))
                .clickEvent(ClickEvent.runCommand("/passport deny"));

        target.sendMessage(Component.text("  ").append(accept).append(Component.text("   ")).append(deny));

        // Сообщение запросившему
        requester.sendMessage(ColorUtil.toComponent(
                plugin.lang().getString("request.sent").replace("%player%", target.getName())
        ));

        // Таймаут через 30 секунд
        new BukkitRunnable() {
            @Override
            public void run() {
                if (activeRequests.containsKey(targetId)) {
                    timeout(targetId);
                }
            }
        }.runTaskLater(plugin, 30 * 20L);
    }

    public boolean acceptRequest(Player target) {
        UUID targetId = target.getUniqueId();
        Request request = activeRequests.remove(targetId);

        if (request == null) {
            target.sendMessage(ColorUtil.toComponent(plugin.lang().getString("request.no-active")));
            return false;
        }

        request.cancel();
        Player requester = request.requester;

        if (!requester.isOnline()) {
            target.sendMessage(ColorUtil.toComponent(plugin.lang().getString("request.requester-offline")));
            return false;
        }

        target.sendMessage(ColorUtil.toComponent(plugin.lang().getString("request.accepted")));
        requester.sendMessage(ColorUtil.toComponent(
                plugin.lang().getString("request.target-accepted").replace("%player%", target.getName())
        ));

        // Открываем паспорт запросившему игроку
        Passport passport = manager.getPassport(target);
        if (passport != null && commandHandler != null) {
            commandHandler.openPassportBookForRequester(requester, passport);
        }

        return true;
    }

    public boolean denyRequest(Player target) {
        UUID targetId = target.getUniqueId();
        Request request = activeRequests.remove(targetId);

        if (request == null) {
            target.sendMessage(ColorUtil.toComponent(plugin.lang().getString("request.no-active")));
            return false;
        }

        request.cancel();
        Player requester = request.requester;

        target.sendMessage(ColorUtil.toComponent(plugin.lang().getString("request.denied")));

        if (requester.isOnline()) {
            requester.sendMessage(ColorUtil.toComponent(
                    plugin.lang().getString("request.target-denied").replace("%player%", target.getName())
            ));
        }

        return false;
    }

    private void timeout(UUID targetId) {
        Request request = activeRequests.remove(targetId);
        if (request == null) return;

        request.cancel();

        if (request.target.isOnline()) {
            request.target.sendMessage(ColorUtil.toComponent(plugin.lang().getString("request.timeout")));
        }

        if (request.requester.isOnline()) {
            request.requester.sendMessage(ColorUtil.toComponent(
                    plugin.lang().getString("request.timeout-requester")
                            .replace("%player%", request.target.getName())
            ));
        }
    }

    private static class Request {
        final Player requester;
        final Player target;

        Request(Player requester, Player target) {
            this.requester = requester;
            this.target = target;
        }

        void cancel() {
            // Дополнительная логика отмены если нужно
        }
    }
}
