package sh.talonfox.pyrofrost.temperature;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;
import sh.talonfox.pyrofrost.Pyrofrost;

import java.io.InputStream;
import java.io.InputStreamReader;

public class ThermalRadiationDataLoader implements SimpleSynchronousResourceReloadListener {
    @Override
    public void reload(ResourceManager manager) {
        manager.findResources("thermal_radiation", id -> id.getPath().endsWith(".json")).forEach((id, resourceRef) -> {
            try {
                InputStream stream = resourceRef.getInputStream();
                JsonObject data = JsonParser.parseReader(new InputStreamReader(stream)).getAsJsonObject();
                ThermalRadiation.radiationBlocks.put(new Identifier(data.get("id").getAsString()), data.get("amount").getAsFloat());
            } catch(Exception e) {
                Pyrofrost.LOGGER.error("Thermal Radiation Resource {} Failed to load! {}", id.toString(), e.toString());
            }
        });
    }

    @Override
    public Identifier getFabricId() {
        return new Identifier("pyrofrost","thermal_radiation");
    }
}
