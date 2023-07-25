package sh.talonfox.pyrofrost;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import sh.talonfox.pyrofrost.network.UpdateTemperatureClient;

public class PyrofrostClient implements ClientModInitializer {
	public static float coreTemp = 1.634457832F;
	public static float skinTemp = 1.634457832F;
	public static float localTemp = 1.108F;
	public static float thirst = 20F;
	public static boolean sweat = false;
	@Override
	public void onInitializeClient() {
		HudRenderCallback.EVENT.register(new TemperatureHud());
		ClientPlayNetworking.registerGlobalReceiver(UpdateTemperatureClient.PACKET_ID, UpdateTemperatureClient::onReceive);
	}
}