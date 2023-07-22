package sh.talonfox.temperature;

import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.LightType;

public class Temperature {
    private int wetness;
    private float coreTemp;
    private float skinTemp;

    public Temperature(ServerPlayerEntity player, boolean shouldUpdate) {

    }

    private float getSolarRadiation(ServerWorld world, BlockPos pos) {
        double radiation = 0.0;
        double sunlight = world.getLightLevel(LightType.SKY, pos.up()) - world.getAmbientDarkness();
        float f = world.getSkyAngleRadians(1.0F);

        if (sunlight > 0) {
            float f1 = f < (float)Math.PI ? 0.0F : ((float)Math.PI * 2F);
            f += (f1 - f) * 0.2F;
            sunlight = sunlight * MathHelper.cos(f);
        }

        radiation += sunlight * 100;

        return (float)Math.max(radiation, 0);
    }
}
