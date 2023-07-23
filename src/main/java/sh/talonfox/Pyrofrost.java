package sh.talonfox;

import net.fabricmc.api.ModInitializer;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sh.talonfox.temperature.Temperature;
import sh.talonfox.temperature.ThermalRadiation;

import java.util.HashMap;
import java.util.UUID;

public class Pyrofrost implements ModInitializer {
	// This logger is used to write text to the console and the log file.
	// It is considered best practice to use your mod id as the logger's name.
	// That way, it's clear which mod wrote info, warnings, and errors.
    public static final Logger LOGGER = LoggerFactory.getLogger("pyrofrost");

	public HashMap<UUID, Temperature> playerTemps = new HashMap<>();

	@Override
	public void onInitialize() {
		// This code runs as soon as Minecraft is in a mod-load-ready state.
		// However, some things (like resources) may still be uninitialized.
		// Proceed with mild caution.
		ThermalRadiation.initialize();
		LOGGER.info("owo");

		ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
			playerTemps.put(handler.getPlayer().getUuid(),new Temperature(handler.getPlayer(),true));
		});
		ServerTickEvents.START_SERVER_TICK.register((server) -> {
			playerTemps.forEach((uuid, temperature) -> {
				temperature.tick();
			});
		});
	}
}