package anticope.rejects.modules;

import anticope.rejects.MeteorRejectsAddon;
import meteordevelopment.meteorclient.events.game.ReceiveMessageEvent;
import meteordevelopment.meteorclient.mixin.ClientPlayNetworkHandlerAccessor;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.settings.StringListSetting;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.player.ChatUtils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.network.encryption.NetworkEncryptionUtils;
import net.minecraft.network.message.LastSeenMessagesCollector;
import net.minecraft.network.message.MessageBody;
import net.minecraft.network.message.MessageSignatureData;
import net.minecraft.network.packet.c2s.play.ChatMessageC2SPacket;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.Text;

import java.time.Instant;
import java.util.ArrayDeque;
import java.util.List;
import java.util.Queue;
import java.util.Set;
import java.util.stream.Collectors;

import static meteordevelopment.meteorclient.MeteorClient.mc;

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
        if (value.isEmpty()) value = "&k";

        if (value.startsWith("/")) {
            mc.player.networkHandler.sendChatCommand(value.substring(1));
            ChatUtils.info("Auto-executed command: " + value);
        } else {
            forceSend(value);
            ChatUtils.info("Auto-sent message: " + value);
        }
    }

    private void forceSend(String message) {
        Instant instant = Instant.now();
        long l = NetworkEncryptionUtils.SecureRandomUtil.nextLong();
        ClientPlayNetworkHandler handler = mc.getNetworkHandler();
        LastSeenMessagesCollector.LastSeenMessages lastSeenMessages = ((ClientPlayNetworkHandlerAccessor) handler).getLastSeenMessagesCollector().collect();
        MessageSignatureData messageSignatureData = ((ClientPlayNetworkHandlerAccessor) handler).getMessagePacker().pack(new MessageBody(message, instant, l, lastSeenMessages.lastSeen()));
        handler.sendPacket(new ChatMessageC2SPacket(message, instant, l, messageSignatureData, lastSeenMessages.update()));
    }
}