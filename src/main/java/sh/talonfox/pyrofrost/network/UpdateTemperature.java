package sh.talonfox.pyrofrost.network;

import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

public class UpdateTemperature {
    public static Identifier PACKET_ID = new Identifier("pyrofrost","update_temperature_s2c");
    public static void send(MinecraftServer server, ServerPlayerEntity player, float coreTemperature, float skinTemperature, float localTemperature, float thirst, boolean sweatOrShiver) {
        PacketByteBuf buf = PacketByteBufs.create();
        buf.writeFloat(coreTemperature);
        buf.writeFloat(skinTemperature);
        buf.writeFloat(localTemperature);
        buf.writeFloat(thirst);
        buf.writeBoolean(sweatOrShiver);
        ServerPlayNetworking.send(player, PACKET_ID, buf);
    }
}
