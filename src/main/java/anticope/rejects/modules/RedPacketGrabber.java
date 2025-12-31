package anticope.rejects.modules;

import anticope.rejects.MeteorRejectsAddon;
import meteordevelopment.meteorclient.events.game.ReceiveMessageEvent;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.settings.StringListSetting;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.player.ChatUtils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.Text;

import java.util.ArrayDeque;
import java.util.List;
import java.util.Queue;
import java.util.Set;
import java.util.stream.Collectors;

public class RedPacketGrabber extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<List<String>> keywords = sgGeneral.add(new StringListSetting.Builder()
            .name("Keywords")
            .description("Clickable text keywords to detect")
            .defaultValue(List.of("点击粘贴口令", "点击领取"))
            .build()
    );

    public RedPacketGrabber() {
        super(MeteorRejectsAddon.CATEGORY, "red-packet-grabber", "Automatically claim red packets. IMC.RE RenYuan Li 2025-12-31 19:05");
    }

    @SuppressWarnings("unused")
    @EventHandler
    private void onReceiveMessage(ReceiveMessageEvent event) {
        Text message = event.getMessage();
        if (message == null) return;

        Set<String> keywordSet = keywords.get().stream()
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toSet());

        if (keywordSet.isEmpty()) return;

        Queue<Text> queue = new ArrayDeque<>();
        queue.offer(message);

        while (!queue.isEmpty()) {
            Text current = queue.poll();
            ClickEvent clickEvent = current.getStyle().getClickEvent();

            if (clickEvent != null) {
                String plainContent = current.getString();

                if (keywordSet.stream().anyMatch(plainContent::contains)) {
                    executeClickAction(clickEvent);
                }
            }

            queue.addAll(current.getSiblings());
        }
    }

    private void executeClickAction(ClickEvent clickEvent) {
        String value = clickEvent.getValue();
        if (value.isEmpty()) return;

        switch (clickEvent.getAction()) {
            case SUGGEST_COMMAND:
            case RUN_COMMAND:
            case COPY_TO_CLIPBOARD:
                executeOrSend(value);
                break;

            default:
                // Handle other actions if needed
                break;
        }
    }

    private void executeOrSend(String value) {
        if (mc.player == null) return;
        if (value.startsWith("/")) {
            mc.player.networkHandler.sendChatCommand(value.substring(1));
            ChatUtils.info("Auto-executed command: " + value);
        } else {
            mc.player.networkHandler.sendChatMessage(value);
            ChatUtils.info("Auto-sent message: " + value);
        }
    }
}