package sh.talonfox.pyrofrost.temperature;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.tag.BiomeTags;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.LightType;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.PalettedContainer;
import sh.talonfox.pyrofrost.Pyrofrost;

import java.util.HashMap;
import java.util.Map;

public class Temperature {
    private int wetness;
    private float coreTemp = 1.634457832F;
    private float skinTemp;
    private ServerPlayerEntity serverPlayer;
    private boolean isServerSide;
    private double envRadiation;
    private int ticks = 0;
    private static HashMap<TagKey<Biome>, Float> temperature = new HashMap<>();
    private static HashMap<TagKey<Biome>, Float> humidity = new HashMap<>();
    private static HashMap<TagKey<Biome>, Float> dayNightOffset = new HashMap<>();

    public static void initialize() {
        temperature.put(BiomeTags.IS_BADLANDS,1.309F);
        humidity.put(BiomeTags.IS_BADLANDS,20.0F);
        dayNightOffset.put(BiomeTags.IS_BADLANDS,15F);
        temperature.put(BiomeTags.IS_BEACH,0.663F);
        humidity.put(BiomeTags.IS_BEACH,70.0F);
        dayNightOffset.put(BiomeTags.IS_BEACH,10F);
        temperature.put(BiomeTags.IS_FOREST,0.663F);
        humidity.put(BiomeTags.IS_FOREST,50.0F);
        dayNightOffset.put(BiomeTags.IS_FOREST,12F);
        temperature.put(BiomeTags.IS_END,0.551F);
        humidity.put(BiomeTags.IS_END,40.0F);
        dayNightOffset.put(BiomeTags.IS_END,0F);
        temperature.put(BiomeTags.IS_HILL,0.618F);
        humidity.put(BiomeTags.IS_HILL,50.0F);
        dayNightOffset.put(BiomeTags.IS_HILL,10F);
        temperature.put(BiomeTags.IS_DEEP_OCEAN,0.596F);
        humidity.put(BiomeTags.IS_DEEP_OCEAN,70.0F);
        dayNightOffset.put(BiomeTags.IS_DEEP_OCEAN,5F);
        temperature.put(BiomeTags.IS_OCEAN,0.640F);
        humidity.put(BiomeTags.IS_OCEAN,70.0F);
        dayNightOffset.put(BiomeTags.IS_OCEAN,10F);
        temperature.put(BiomeTags.IS_MOUNTAIN,0.618F);
        humidity.put(BiomeTags.IS_MOUNTAIN,50.0F);
        dayNightOffset.put(BiomeTags.IS_MOUNTAIN,10F);
        temperature.put(BiomeTags.IS_JUNGLE,0.997F);
        humidity.put(BiomeTags.IS_JUNGLE,90.0F);
        dayNightOffset.put(BiomeTags.IS_JUNGLE,15F);
        temperature.put(BiomeTags.IS_NETHER,1.666F);
        humidity.put(BiomeTags.IS_NETHER,20.0F);
        dayNightOffset.put(BiomeTags.IS_NETHER,0F);
        temperature.put(BiomeTags.IS_RIVER,0.551F);
        humidity.put(BiomeTags.IS_RIVER,70.0F);
        dayNightOffset.put(BiomeTags.IS_RIVER,10F);
        temperature.put(BiomeTags.IS_SAVANNA,1.108F);
        humidity.put(BiomeTags.IS_SAVANNA,30.0F);
        dayNightOffset.put(BiomeTags.IS_SAVANNA,15F);
        temperature.put(BiomeTags.IS_TAIGA,0.507F);
        humidity.put(BiomeTags.IS_TAIGA,50.0F);
        dayNightOffset.put(BiomeTags.IS_TAIGA,10F);
        temperature.put(BiomeTags.IGLOO_HAS_STRUCTURE,0.507F);
        humidity.put(BiomeTags.IGLOO_HAS_STRUCTURE,20.0F);
        dayNightOffset.put(BiomeTags.IGLOO_HAS_STRUCTURE,5F);
        temperature.put(BiomeTags.VILLAGE_PLAINS_HAS_STRUCTURE,0.774F);
        humidity.put(BiomeTags.VILLAGE_PLAINS_HAS_STRUCTURE,60.0F);
        dayNightOffset.put(BiomeTags.VILLAGE_PLAINS_HAS_STRUCTURE,15F);
        temperature.put(BiomeTags.RUINED_PORTAL_SWAMP_HAS_STRUCTURE,0.685F);
        humidity.put(BiomeTags.RUINED_PORTAL_SWAMP_HAS_STRUCTURE,90.0F);
        dayNightOffset.put(BiomeTags.RUINED_PORTAL_SWAMP_HAS_STRUCTURE,10F);
        temperature.put(BiomeTags.DESERT_PYRAMID_HAS_STRUCTURE,1.354F);
        humidity.put(BiomeTags.DESERT_PYRAMID_HAS_STRUCTURE,20.0F);
        dayNightOffset.put(BiomeTags.DESERT_PYRAMID_HAS_STRUCTURE,20F);
    }

    public Temperature(ServerPlayerEntity player, boolean shouldUpdate) {
        isServerSide = shouldUpdate;
        serverPlayer = player;
    }

    public void tick() {
        ticks += 1;
        if(ticks % 20 == 0) {
            float humidity = this.getBiomeHumidity(serverPlayer.getServerWorld().getBiome(serverPlayer.getBlockPos()));
            float dryTemperature = serverPlayer.getServerWorld().getBiome(serverPlayer.getBlockPos()).value().computeTemperature(serverPlayer.getBlockPos());
            float dayNightOffset = getDayNightOffset(serverPlayer.getServerWorld(),getBiomeDayNightOffset(serverPlayer.getServerWorld().getBiome(serverPlayer.getBlockPos())),humidity);
            Pyrofrost.LOGGER.info("Dry Temperature: "+mcTempConv(dryTemperature+dayNightOffset)+" degrees F");
            Pyrofrost.LOGGER.info("Relative Humidity: "+humidity+"%");
            Pyrofrost.LOGGER.info("Core Temperature: "+mcTempConv(coreTemp)+" degrees F");
            Pyrofrost.LOGGER.info("WGBT: "+getWBGT());
        }
    }

    private static double mcTempToCelsius(float temp) {
        double out = 25.27027027 + (44.86486486 * temp);
        out = (out - 32) * 0.5556;
        return temp;
    }

    private static double mcTempConv(float temp) {
        return 25.27027027 + (44.86486486 * temp);
    }

    private static double tempToCelsius(double temp) {
        double out = (temp / 0.5556) + 32;
        return (out - 25.27027027) / 44.86486486;
    }

    private static double tempToF(double temp) {
        return (temp - 25.27027027) / 44.86486486;
    }

    private static double getBlackGlobe(double radiation, float dryTemp, double relativeHumidity) {
        double dryTempC = mcTempToCelsius(dryTemp);

        double blackGlobeTemp = (0.01498 * radiation) + (1.184 * dryTempC) - (0.0789 * (relativeHumidity / 100)) - 2.739;

        return tempToCelsius(blackGlobeTemp);
    }

    private float getBiomeTemperature(RegistryEntry<Biome> biome) {
        for(Map.Entry<TagKey<Biome>,Float> entry : temperature.entrySet()) {
            if (biome.isIn(entry.getKey())) {
                return entry.getValue();
            }
        }
        return 0.663F;
    }

    private float getBiomeHumidity(RegistryEntry<Biome> biome) {
        float rainBonus = ((serverPlayer.getServerWorld().hasRain(serverPlayer.getBlockPos().withY(320)) || serverPlayer.getServerWorld().isRaining())?0F:-20F); // We put both conditions to retain compatibility with Enhanced Weather
        for(Map.Entry<TagKey<Biome>,Float> entry : humidity.entrySet()) {
            if (biome.isIn(entry.getKey())) {
                return entry.getValue()+rainBonus;
            }
        }
        return 40.0F+rainBonus;
    }

    private float getBiomeDayNightOffset(RegistryEntry<Biome> biome) {
        for(Map.Entry<TagKey<Biome>,Float> entry : dayNightOffset.entrySet()) {
            if (biome.isIn(entry.getKey())) {
                return entry.getValue();
            }
        }
        return 0F;
    }

    private static double getHeatIndex(float dryTemp, double rh) {
        double dryTempF = mcTempConv(dryTemp);
        double hIndex;

        if (dryTempF < 80.0) {
            hIndex = 0.5 * (dryTempF + 61.0 +((dryTempF - 68.0) * 1.2)) + (rh*0.094);
        }
        else {
            hIndex = -42.379 + 2.04901523 * dryTempF + 10.14333127 * rh;
            hIndex = hIndex - 0.22475541 * dryTempF * rh - 6.83783 * Math.pow(10, -3) * dryTempF * dryTempF;
            hIndex = hIndex - 5.481717 * Math.pow(10, -2) * rh * rh;
            hIndex = hIndex + 1.22874 * Math.pow(10, -3) * dryTempF * dryTempF * rh;
            hIndex = hIndex + 8.5282 * Math.pow(10, -4) * dryTempF * rh * rh;
            hIndex = hIndex - 1.99 * Math.pow(10, -6) * dryTempF * dryTempF * rh * rh;
        }

        return tempToF(hIndex);
    }

    private double getWBGT() {
        float humidity = this.getBiomeHumidity(serverPlayer.getServerWorld().getBiome(serverPlayer.getBlockPos()));
        float dryTemperature = serverPlayer.getServerWorld().getBiome(serverPlayer.getBlockPos()).value().computeTemperature(serverPlayer.getBlockPos())+getDayNightOffset(serverPlayer.getServerWorld(),getBiomeDayNightOffset(serverPlayer.getServerWorld().getBiome(serverPlayer.getBlockPos())),humidity);
        double wetTemperature = getHeatIndex(dryTemperature,humidity);
        double blackGlobeTemp = (float)getBlackGlobe(getSolarRadiation(serverPlayer.getServerWorld(),serverPlayer.getBlockPos()), dryTemperature, humidity);
        EnvironmentData data = getInfo();
        double airTemperature;
        if (data.isSheltered() || data.isUnderground()) {
            airTemperature = (wetTemperature * 0.7F) + (blackGlobeTemp * 0.3F);
        } else {
            airTemperature = (wetTemperature * 0.7F) + (blackGlobeTemp * 0.2F) + (dryTemperature * 0.1F);
        }
        return airTemperature;
    }

    private EnvironmentData getInfo() {
        boolean isSheltered = true; // So basically me
        boolean isUnderground = true;
        double waterBlocks = 0;
        double totalBlocks = 0;
        double radiation = 0.0;
        BlockPos pos = serverPlayer.getBlockPos();
        for (int x = -12; x <= 12; x++) {
            for (int z = -12; z <= 12; z++) {
                if (isSheltered && (x <= 2 && x >= -2) && (z <= 2 && z >= -2)) {
                    isSheltered = !serverPlayer.getServerWorld().isSkyVisible(BlockPos.ofFloored(serverPlayer.getEyePos()).add(x, 0, z).up());
                }
                for (int y = -3; y <= 11; y++) {
                    ChunkPos chunkPos = new ChunkPos((pos.getX() + x) >> 4,(pos.getZ() + z) >> 4);
                    Chunk chunk = serverPlayer.getServerWorld().getChunk(chunkPos.getStartPos());

                    if (chunk == null) continue;
                    BlockPos blockpos = pos.add(x, y, z);
                    PalettedContainer<BlockState> palette;
                    try {
                        palette = chunk.getSection((blockpos.getY() >> 4) - chunk.getHighestNonEmptySection()).getBlockStateContainer();

                    }
                    catch (Exception e) {
                        continue;
                    }
                    BlockState state = palette.get(blockpos.getX() & 15, blockpos.getY() & 15, blockpos.getZ() & 15);
                    boolean isWater = state.isOf(Blocks.WATER);
                    if (isUnderground && y >= 0 && !isWater) {
                        isUnderground = !serverPlayer.getServerWorld().isSkyVisible(BlockPos.ofFloored(serverPlayer.getEyePos()).add(x, y, z).up());
                    }
                    if ((x <= 5 && x >= -5) && (y <= 5) && (z <= 5 && z >= -5)) {
                        totalBlocks++;

                        if (isWater) {
                            waterBlocks++;
                        }
                    }
                    if(y <= 3) {
                        Float rad = ThermalRadiation.radiationBlocks.get(new Identifier(state.getBlock().toString()));
                        if (rad != null) {
                            radiation += rad;
                        }
                    }
                }
            }
        }
        return new EnvironmentData(isUnderground,isSheltered,radiation);
    }

    private static float getSolarRadiation(ServerWorld world, BlockPos pos) {
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

    private static float getDayNightOffset(ServerWorld world, float maxTemp, double relativeHumidity) {
        if(maxTemp == 0F) return 0F;
        long time = (world.getTimeOfDay() % 24000);
        float increaseTemp = (maxTemp * 0.022289157F) / 10000F;
        float decreaseTemp = (maxTemp * 0.022289157F) / 14000F;
        float humidityOffset = 1.0F - (float) (relativeHumidity / 100);
        float offset;

        if (time > 23000) {
            offset = (24001 - time) * increaseTemp;
        } else if (time < 9001) {
            offset = (time + 1000) * increaseTemp;
        } else {
            offset = (maxTemp * 0.022289157F) - ((time - 9000) * decreaseTemp);
        }

        return offset * humidityOffset;
    }
}
