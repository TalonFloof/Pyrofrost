package sh.talonfox.pyrofrost;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.EntityModelLayerRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.item.ModelPredicateProviderRegistry;
import net.minecraft.client.render.entity.model.EntityModelLayer;
import net.minecraft.util.Identifier;
import sh.talonfox.pyrofrost.model.WolfFurHelmet;
import sh.talonfox.pyrofrost.network.UpdateTemperatureClient;
import sh.talonfox.pyrofrost.registry.ItemRegistry;

public class PyrofrostClient implements ClientModInitializer {
	public static final EntityModelLayer WOLF_FUR_HELMET_LAYER = new EntityModelLayer(new Identifier("pyrofrost","wolf_fur_helmet_render_layer"), "wolf_fur_helmet_render_layer");
	public static float coreTemp = 1.634457832F;
	public static float skinTemp = 1.634457832F;
	public static float localTemp = 1.108F;
	public static float rad = 0F;
	public static int wetness = 0;
	@Override
	public void onInitializeClient() {
		EntityModelLayerRegistry.registerModelLayer(WOLF_FUR_HELMET_LAYER, WolfFurHelmet::getTexturedModelData);
		HudRenderCallback.EVENT.register(new TemperatureHud());
		ClientPlayNetworking.registerGlobalReceiver(UpdateTemperatureClient.PACKET_ID, UpdateTemperatureClient::onReceive);
		ModelPredicateProviderRegistry.register(ItemRegistry.ICE_PACK_ITEM, new Identifier("percent_used"), (stack, world, entity, seed) -> {
			if (entity == null)
				return 0F;
			else
				return stack.getDamage() == 0 ? 0F : (float) stack.getDamage() / stack.getMaxDamage();
		});
	}
}