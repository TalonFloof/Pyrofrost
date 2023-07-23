package sh.talonfox.temperature;

import net.minecraft.util.Identifier;

import java.util.HashMap;
import java.util.UUID;

public class ThermalRadiation {
    public static HashMap<Identifier, Float> radiationBlocks = new HashMap<>();
    public static void initialize() {
        radiationBlocks.put(new Identifier("minecraft","campfire"),5550F);
        radiationBlocks.put(new Identifier("minecraft","torch"),350F);
    }
}
