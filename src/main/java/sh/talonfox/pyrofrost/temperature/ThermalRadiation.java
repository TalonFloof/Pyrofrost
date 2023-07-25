package sh.talonfox.pyrofrost.temperature;

import net.minecraft.util.Identifier;

import java.util.HashMap;
import java.util.UUID;

public class ThermalRadiation {
    public static HashMap<Identifier, Float> radiationBlocks = new HashMap<>();
    public static void initialize() {
        radiationBlocks.put(new Identifier("minecraft","campfire"),5550F);
        radiationBlocks.put(new Identifier("minecraft","torch"),350F);
        radiationBlocks.put(new Identifier("minecraft","wall_torch"),350F);
        radiationBlocks.put(new Identifier("minecraft","fire"),1300F);
        radiationBlocks.put(new Identifier("minecraft","lantern"),350F);
        radiationBlocks.put(new Identifier("minecraft","lava"),1550F);
        radiationBlocks.put(new Identifier("minecraft","magma_block"),1200F);
        radiationBlocks.put(new Identifier("minecraft","nether_portal"),350F);
        radiationBlocks.put(new Identifier("minecraft","soul_campfire"),8325F);
        radiationBlocks.put(new Identifier("minecraft","soul_fire"),1950F);
        radiationBlocks.put(new Identifier("minecraft","soul_lantern"),525F);
        radiationBlocks.put(new Identifier("minecraft","soul_torch"),525F);
        radiationBlocks.put(new Identifier("minecraft","soul_wall_torch"),525F);
    }
}
