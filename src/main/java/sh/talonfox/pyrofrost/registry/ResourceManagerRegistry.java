package sh.talonfox.pyrofrost.registry;

import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.minecraft.resource.ResourceType;
import sh.talonfox.pyrofrost.temperature.ThermalRadiationDataLoader;

public class ResourceManagerRegistry {
    public static void init() {
        ResourceManagerHelper.get(ResourceType.SERVER_DATA).registerReloadListener(new ThermalRadiationDataLoader());
    }
}
