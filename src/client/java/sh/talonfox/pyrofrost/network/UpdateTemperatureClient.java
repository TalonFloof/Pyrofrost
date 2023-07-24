package sh.talonfox.pyrofrost.network;

import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;
import sh.talonfox.pyrofrost.Pyrofrost;
import sh.talonfox.pyrofrost.PyrofrostClient;
import sh.talonfox.pyrofrost.temperature.Temperature;

public class UpdateTemperatureClient {
    public static Identifier PACKET_ID = new Identifier("pyrofrost","update_temperature_s2c");
    public static void onReceive(MinecraftClient client, ClientPlayNetworkHandler clientPlayNetworkHandler, PacketByteBuf packetByteBuf, PacketSender packetSender) {
        PyrofrostClient.coreTemp = packetByteBuf.readFloat();
        PyrofrostClient.skinTemp = packetByteBuf.readFloat();
        PyrofrostClient.localTemp = packetByteBuf.readFloat();
        Pyrofrost.LOGGER.info("Core Temp: "+Temperature.mcTempConv(PyrofrostClient.coreTemp));
        Pyrofrost.LOGGER.info("Skin Temp: "+Temperature.mcTempConv(PyrofrostClient.skinTemp));
        Pyrofrost.LOGGER.info("Local Temp: "+Temperature.mcTempConv(PyrofrostClient.localTemp));
    }
}
