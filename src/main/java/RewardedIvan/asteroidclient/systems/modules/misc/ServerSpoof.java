package RewardedIvan.asteroidclient.systems.modules.misc;

import RewardedIvan.asteroidclient.AsteroidClient;
import RewardedIvan.asteroidclient.mixin.CustomPayloadC2SPacketAccessor;
import RewardedIvan.asteroidclient.settings.BoolSetting;
import RewardedIvan.asteroidclient.settings.Setting;
import RewardedIvan.asteroidclient.settings.SettingGroup;
import RewardedIvan.asteroidclient.settings.StringSetting;
import io.netty.buffer.Unpooled;
import RewardedIvan.asteroidclient.events.packets.PacketEvent;
import RewardedIvan.asteroidclient.systems.modules.Categories;
import RewardedIvan.asteroidclient.systems.modules.Module;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.packet.c2s.play.CustomPayloadC2SPacket;
import net.minecraft.network.packet.s2c.play.ResourcePackSendS2CPacket;
import net.minecraft.text.BaseText;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.LiteralText;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import org.apache.commons.lang3.StringUtils;

import java.nio.charset.StandardCharsets;

public class ServerSpoof extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<String> brand = sgGeneral.add(new StringSetting.Builder()
            .name("brand")
            .description("Specify the brand that will be send to the server.")
            .defaultValue("Asteroid Client")
            .build()
    );

    private final Setting<Boolean> resourcePack = sgGeneral.add(new BoolSetting.Builder()
            .name("resource-pack")
            .description("Spoof accepting server resource pack.")
            .defaultValue(false)
            .build()
    );

    public ServerSpoof() {
        super(Categories.Misc, "server-spoof", "Spoof client brand and/or resource pack.");

        AsteroidClient.EVENT_BUS.subscribe(new Listener());
    }

    private class Listener {
        @EventHandler
        private void onPacketSend(PacketEvent.Send event) {
            if (!isActive()) return;
            if (!(event.packet instanceof CustomPayloadC2SPacket)) return;
            CustomPayloadC2SPacketAccessor packet = (CustomPayloadC2SPacketAccessor) event.packet;
            Identifier id = packet.getChannel();

            if (id.equals(CustomPayloadC2SPacket.BRAND)) {
                packet.setData(new PacketByteBuf(Unpooled.buffer()).writeString(brand.get()));
            }
            else if (StringUtils.containsIgnoreCase(packet.getData().toString(StandardCharsets.UTF_8), "fabric") && brand.get().equalsIgnoreCase("fabric")) {
                event.cancel();
            }
        }

        @EventHandler
        private void onPacketRecieve(PacketEvent.Receive event) {
            if (!isActive() || !resourcePack.get()) return;
            if (!(event.packet instanceof ResourcePackSendS2CPacket packet)) return;
            event.cancel();
            BaseText msg = new LiteralText("This server has ");
            msg.append(packet.isRequired() ? "a required " : "an optional ");
            BaseText link = new LiteralText("resource pack");
            link.setStyle(link.getStyle()
                .withColor(Formatting.BLUE)
                .withUnderline(true)
                .withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, packet.getURL()))
                .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new LiteralText("Click to download")))
            );
            msg.append(link);
            msg.append(".");
            info(msg);
        }
    }
}
