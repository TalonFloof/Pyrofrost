package sh.talonfox.pyrofrost;

import net.fabricmc.api.ModInitializer;

import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sh.talonfox.pyrofrost.modcompat.ModCompatManager;
import sh.talonfox.pyrofrost.registry.ItemRegistry;
import sh.talonfox.pyrofrost.temperature.Temperature;
import sh.talonfox.pyrofrost.temperature.ThermalRadiation;

import java.util.HashMap;
import java.util.UUID;

public class Pyrofrost implements ModInitializer {
    public static final Logger LOGGER = LoggerFactory.getLogger("pyrofrost");

	public static HashMap<UUID, Temperature> playerTemps = new HashMap<>();

	@Override
	public void onInitialize() {
		ThermalRadiation.initialize();
		Temperature.initialize();
		ModCompatManager.init();
		ItemRegistry.init();
		LOGGER.info("owo");

		ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
			playerTemps.put(handler.getPlayer().getUuid(),new Temperature(handler.getPlayer(),true));
		});
		ServerTickEvents.START_SERVER_TICK.register((server) -> {
			playerTemps.forEach((uuid, temperature) -> {
				temperature.tick();
			});
		});
		ServerPlayerEvents.AFTER_RESPAWN.register((oldP, newP, alive) -> {
			playerTemps.remove(oldP.getUuid());
			playerTemps.put(newP.getUuid(),new Temperature(newP,true));
		});
	}
}