package eu.pb4.polymer.core.impl.networking.payloads.s2c;

import eu.pb4.polymer.core.impl.PolymerImpl;
import eu.pb4.polymer.core.impl.networking.S2CPackets;
import eu.pb4.polymer.networking.api.ContextByteBuf;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.text.Text;
import net.minecraft.text.TextCodecs;
import net.minecraft.util.Identifier;

public record PolymerItemGroupDefineS2CPayload(Identifier groupId, Text name, ItemStack icon) implements CustomPayload {
    public static final CustomPayload.Id<PolymerItemGroupDefineS2CPayload> ID = new CustomPayload.Id<>(S2CPackets.SYNC_ITEM_GROUP_DEFINE);
    public static final PacketCodec<ContextByteBuf, PolymerItemGroupDefineS2CPayload> CODEC = PacketCodec.of(PolymerItemGroupDefineS2CPayload::write, PolymerItemGroupDefineS2CPayload::read);

    public void write(PacketByteBuf buf) {
        buf.writeIdentifier(this.groupId);

        TextCodecs.PACKET_CODEC.encode(buf, name);
        if (icon.isEmpty()) {
			PolymerImpl.LOGGER.error("ItemGroup with id \"{}\" and name \"{}\" has empty icon!", this.groupId, this.name);
            ItemStack.PACKET_CODEC.encode((RegistryByteBuf) buf, Items.STICK.getDefaultStack());
        } else {
            ItemStack.PACKET_CODEC.encode((RegistryByteBuf) buf, icon);
        }
    }

    public static PolymerItemGroupDefineS2CPayload read(PacketByteBuf buf) {
        return new PolymerItemGroupDefineS2CPayload(buf.readIdentifier(), TextCodecs.PACKET_CODEC.decode(buf), decodeIcon(buf));
    }

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }

    private static ItemStack decodeIcon(PacketByteBuf buf) {
        ItemStack itemStack = ItemStack.OPTIONAL_PACKET_CODEC.decode((RegistryByteBuf) buf);

        if (itemStack.isEmpty()) {
            PolymerImpl.LOGGER.error("Received ItemGroup with empty icon!");
            return Items.STICK.getDefaultStack();
        } else {
            return itemStack;
        }
    }
}
