package sh.talonfox.pyrofrost;

import blue.endless.jankson.JsonElement;
import blue.endless.jankson.JsonObject;
import blue.endless.jankson.JsonPrimitive;
import net.fabricmc.api.ModInitializer;

import blue.endless.jankson.Jankson;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerWorldEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.util.WorldSavePath;
import net.minecraft.world.dimension.DimensionTypes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sh.talonfox.pyrofrost.modcompat.ModCompatManager;
import sh.talonfox.pyrofrost.registry.ItemRegistry;
import sh.talonfox.pyrofrost.registry.ResourceManagerRegistry;
import sh.talonfox.pyrofrost.temperature.Temperature;
import sh.talonfox.pyrofrost.temperature.ThermalRadiation;
import net.minecraft.server.MinecraftServer;

import java.io.File;
import java.io.FileWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class Pyrofrost implements ModInitializer {
    public static final Logger LOGGER = LoggerFactory.getLogger("pyrofrost");

	public static HashMap<UUID, Temperature> playerTemps = new HashMap<>();
	public static HashMap<UUID, JsonObject> playerTempSave = new HashMap<>();

	public static void saveTemperatureData(MinecraftServer server) {
		LOGGER.info("Attempting to save Temperature Data...");
		JsonObject jsonObject = new JsonObject();
		for(UUID i : playerTemps.keySet()) {
			Temperature temp = playerTemps.get(i);
			JsonObject tempObject = new JsonObject();
			tempObject.put("Wetness",new JsonPrimitive(temp.wetness));
			tempObject.put("Moisture",new JsonPrimitive(temp.moistureLevel));
			tempObject.put("CoreTemperature",new JsonPrimitive(temp.coreTemp));
			tempObject.put("SkinTemperature",new JsonPrimitive(temp.skinTemp));
			jsonObject.put(i.toString(),tempObject);
		}
		for(UUID i : playerTempSave.keySet()) {
			jsonObject.put(i.toString(), playerTempSave.get(i));
		}
		String data = jsonObject.toJson(true,true);
		File file = new File(server.getSavePath(WorldSavePath.ROOT).toAbsolutePath() + "/Pyrofrost.json5");
		try {
			new File(file.getParent()).mkdir();
			file.delete();
			file.createNewFile();
			FileWriter stream = new FileWriter(file);
			stream.write(data);
			stream.close();
		} catch (Exception e) {
			LOGGER.error("Failed to save Temperature Data");
			LOGGER.error("Reason: "+e.toString());
		}
	}

	public static void loadTemperatureData(MinecraftServer server) {
		LOGGER.info("Attempting to load Temperature Data...");
		playerTempSave.clear();
		playerTemps.clear();
		File file = new File(server.getSavePath(WorldSavePath.ROOT).toAbsolutePath() + "/Pyrofrost.json5");
		if(file.exists() && file.isFile()) {
			try {
				JsonObject jsonObject = Jankson.builder().build().load(file);
				for(Map.Entry<String, JsonElement> entry : jsonObject.entrySet()) {
					playerTempSave.put(UUID.fromString(entry.getKey()),(JsonObject)entry.getValue());
				}
			} catch (Exception e) {
				LOGGER.error("Failed to load Temperature Data");
				LOGGER.error("Reason: "+e.toString());
			}
		}
	}

	@Override
	public void onInitialize() {
		Temperature.initialize();
		ModCompatManager.init();
		ItemRegistry.init();
		ResourceManagerRegistry.init();
		Pyrofrost.LOGGER.info("A strange new sensation sweeps across the blocky hills of Minecraft...");

		ServerLifecycleEvents.SERVER_STARTING.register(Pyrofrost::loadTemperatureData);
		ServerLifecycleEvents.SERVER_STOPPING.register(Pyrofrost::saveTemperatureData);
		ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
			if(playerTempSave.containsKey(handler.getPlayer().getUuid())) {
				Pyrofrost.LOGGER.info("Loading saved temperature data from Player {}...", handler.getPlayer().getUuidAsString());
				JsonObject obj = playerTempSave.remove(handler.getPlayer().getUuid());
				Temperature temp = new Temperature(handler.getPlayer(), true);
				temp.coreTemp = obj.getFloat("CoreTemperature", 1.634457832F);
				temp.skinTemp = obj.getFloat("SkinTemperature", 1.634457832F);
				temp.wetness = obj.getInt("Wetness", 0);
				temp.moistureLevel = obj.getFloat("Moisture", 0F);
				playerTemps.put(handler.getPlayer().getUuid(), temp);
			} else {
				playerTemps.put(handler.getPlayer().getUuid(), new Temperature(handler.getPlayer(), true));
			}
		});
		ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> {
			Pyrofrost.LOGGER.info("Transfering Player {}'s Temperature Data to Save Cache...", handler.getPlayer().getUuidAsString());
			Temperature temp = playerTemps.remove(handler.getPlayer().getUuid());
			JsonObject tempObject = new JsonObject();
			tempObject.put("Wetness",new JsonPrimitive(temp.wetness));
			tempObject.put("Moisture",new JsonPrimitive(temp.moistureLevel));
			tempObject.put("CoreTemperature",new JsonPrimitive(temp.coreTemp));
			tempObject.put("SkinTemperature",new JsonPrimitive(temp.skinTemp));
			playerTempSave.put(handler.getPlayer().getUuid(),tempObject);
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