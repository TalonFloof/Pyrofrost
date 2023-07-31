package sh.talonfox.pyrofrost.temperature;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.tag.BiomeTags;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.property.Properties;
import net.minecraft.util.Identifier;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.GameRules;
import net.minecraft.world.LightType;
import net.minecraft.world.RaycastContext;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.PalettedContainer;
import sh.talonfox.pyrofrost.Pyrofrost;
import sh.talonfox.pyrofrost.modcompat.ModCompatManager;
import sh.talonfox.pyrofrost.network.UpdateTemperature;
import sh.talonfox.pyrofrost.registry.ItemRegistry;

import java.util.HashMap;
import java.util.Map;

public class Temperature {
    public int wetness = 0;
    public float moistureLevel = 0F;
    private boolean isSubmerged = false;
    private boolean isPartialSubmersion = false;
    public float coreTemp = 1.634457832F;
    public float skinTemp = 1.634457832F;
    private TemperatureDirection skinTempDir = TemperatureDirection.NONE;
    private ServerPlayerEntity serverPlayer;
    private boolean isServerSide;
    private double envRadiation;
    private int ticks = 0;
    private double wbgt;
    private static HashMap<TagKey<Biome>, Float> temperature = new HashMap<>();
    private static HashMap<TagKey<Biome>, Float> humidity = new HashMap<>();
    private static HashMap<TagKey<Biome>, Float> dayNightOffset = new HashMap<>();
    private static HashMap<TagKey<Biome>, Float> seasonOffset = new HashMap<>();

    public static final float LOW = 1.554216868F;
    public static final float LOW_WARNING3 = 1.572048193F;
    public static final float LOW_WARNING2 = 1.589879518F;
    public static final float LOW_WARNING1 = 1.612168675F;
    public static final float NORMAL = 1.634457832F;
    public static final float HIGH_WARNING1 = 1.700210844F;
    public static final float HIGH_WARNING2 = 1.765963856F;
    public static final float HIGH_WARNING3 = 1.7826807235F;
    public static final float HIGH = 1.799397591F;

    public enum TemperatureDirection {

        WARMING(0.025F),
        WARMING_NORMALLY(0.00625F),
        WARMING_RAPIDLY(0.2F),
        NONE(0.0F),
        COOLING(0.0125F),
        COOLING_NORMALLY(0.00625F),
        COOLING_RAPIDLY(0.2F);

        public final float coreRate;

        TemperatureDirection(float coreRate) {
            this.coreRate = coreRate;
        }

    }

    public void incMoisture(float amount, int resistance) {
        float moistureReduction = amount * ((float)resistance / 20F);

        amount = amount - moistureReduction;

        if (moistureLevel > 4.0F) {
            moistureLevel -= 4.0F;
            wetness = Math.min(this.wetness + 1, 20 - resistance);
        }

        if (amount > 0.0F) {
            moistureLevel = Math.min(this.moistureLevel + amount, 20F);
        }
    }

    public static TemperatureDirection getCoreTemperatureDirection(float lastSkinTemperature, float coreTemperature, float skinTemperature) {
        TemperatureDirection direction = TemperatureDirection.NONE;

        if (lastSkinTemperature > skinTemperature) {
            direction = TemperatureDirection.COOLING_NORMALLY;

            if (coreTemperature > NORMAL) {
                if (skinTemperature < coreTemperature) {
                    direction = TemperatureDirection.COOLING_RAPIDLY;
                } else {
                    direction = TemperatureDirection.COOLING;
                }
            }
        }
        else if (lastSkinTemperature < skinTemperature) {
            direction = TemperatureDirection.WARMING_NORMALLY;

            if (coreTemperature < NORMAL) {
                if (skinTemperature > coreTemperature) {
                    direction = TemperatureDirection.WARMING_RAPIDLY;
                } else {
                    direction = TemperatureDirection.WARMING;
                }
            }
        }

        return direction;
    }

    public static TemperatureDirection getSkinTemperatureDirection(float localTemperature, float lastSkinTemperature) {
        TemperatureDirection direction = TemperatureDirection.NONE;

        if (lastSkinTemperature > NORMAL) {
            if (localTemperature > 1.220F) {
                direction = TemperatureDirection.WARMING_NORMALLY;

                if (localTemperature > 1.888F) {
                    direction = TemperatureDirection.WARMING;
                }
            }
            else if (localTemperature < 1.888F){
                direction = TemperatureDirection.COOLING;

                if (localTemperature < 0.997F) {
                    direction = TemperatureDirection.COOLING_RAPIDLY;
                }
            }
        }
        else if (lastSkinTemperature < NORMAL) {
            if (localTemperature > 0.997F) {
                direction = TemperatureDirection.WARMING_NORMALLY;

                if (localTemperature > 2.557F) {
                    direction = TemperatureDirection.WARMING_RAPIDLY;
                }
                else if (localTemperature > 1.220F) {
                    direction = TemperatureDirection.WARMING;
                }
            }
            else {
                direction = TemperatureDirection.COOLING_NORMALLY;
            }
        }
        else {
            if (localTemperature > 1.220F) {
                direction = TemperatureDirection.WARMING_NORMALLY;
            }
            else if (localTemperature < 0.997F) {
                direction = TemperatureDirection.COOLING_NORMALLY;
            }
        }

        return direction;
    }


    public static void initialize() {
        temperature.put(BiomeTags.IS_BADLANDS,1.309F);
        humidity.put(BiomeTags.IS_BADLANDS,20.0F);
        dayNightOffset.put(BiomeTags.IS_BADLANDS,15F);
        seasonOffset.put(BiomeTags.IS_BADLANDS,40F);

        temperature.put(BiomeTags.IS_BEACH,0.663F);
        humidity.put(BiomeTags.IS_BEACH,70.0F);
        dayNightOffset.put(BiomeTags.IS_BEACH,10F);
        seasonOffset.put(BiomeTags.IS_BEACH,40F);

        temperature.put(BiomeTags.IS_FOREST,0.663F);
        humidity.put(BiomeTags.IS_FOREST,50.0F);
        dayNightOffset.put(BiomeTags.IS_FOREST,12F);
        seasonOffset.put(BiomeTags.IS_FOREST,40F);

        temperature.put(BiomeTags.IS_END,0.551F);
        humidity.put(BiomeTags.IS_END,40.0F);
        dayNightOffset.put(BiomeTags.IS_END,0F);
        seasonOffset.put(BiomeTags.IS_END,40F);

        temperature.put(BiomeTags.IS_HILL,0.618F);
        humidity.put(BiomeTags.IS_HILL,50.0F);
        dayNightOffset.put(BiomeTags.IS_HILL,10F);
        seasonOffset.put(BiomeTags.IS_HILL,40F);

        temperature.put(BiomeTags.IS_DEEP_OCEAN,0.596F);
        humidity.put(BiomeTags.IS_DEEP_OCEAN,70.0F);
        dayNightOffset.put(BiomeTags.IS_DEEP_OCEAN,5F);
        seasonOffset.put(BiomeTags.IS_DEEP_OCEAN,20F);

        temperature.put(BiomeTags.IS_OCEAN,0.640F);
        humidity.put(BiomeTags.IS_OCEAN,70.0F);
        dayNightOffset.put(BiomeTags.IS_OCEAN,10F);
        seasonOffset.put(BiomeTags.IS_OCEAN,20F);

        temperature.put(BiomeTags.IS_MOUNTAIN,0.618F);
        humidity.put(BiomeTags.IS_MOUNTAIN,50.0F);
        dayNightOffset.put(BiomeTags.IS_MOUNTAIN,10F);
        seasonOffset.put(BiomeTags.IS_MOUNTAIN,40F);

        temperature.put(BiomeTags.IS_JUNGLE,0.997F);
        humidity.put(BiomeTags.IS_JUNGLE,90.0F);
        dayNightOffset.put(BiomeTags.IS_JUNGLE,15F);
        seasonOffset.put(BiomeTags.IS_JUNGLE,40F);

        temperature.put(BiomeTags.IS_NETHER,1.666F);
        humidity.put(BiomeTags.IS_NETHER,20.0F);
        dayNightOffset.put(BiomeTags.IS_NETHER,0F);
        seasonOffset.put(BiomeTags.IS_NETHER,40F);

        temperature.put(BiomeTags.IS_RIVER,0.551F);
        humidity.put(BiomeTags.IS_RIVER,70.0F);
        dayNightOffset.put(BiomeTags.IS_RIVER,10F);
        seasonOffset.put(BiomeTags.IS_RIVER,40F);

        temperature.put(BiomeTags.IS_SAVANNA,1.108F);
        humidity.put(BiomeTags.IS_SAVANNA,30.0F);
        dayNightOffset.put(BiomeTags.IS_SAVANNA,15F);
        seasonOffset.put(BiomeTags.IS_SAVANNA,40F);

        temperature.put(BiomeTags.IS_TAIGA,0.507F);
        humidity.put(BiomeTags.IS_TAIGA,50.0F);
        dayNightOffset.put(BiomeTags.IS_TAIGA,10F);
        seasonOffset.put(BiomeTags.IS_TAIGA,40F);

        temperature.put(BiomeTags.IGLOO_HAS_STRUCTURE,0.507F);
        humidity.put(BiomeTags.IGLOO_HAS_STRUCTURE,20.0F);
        dayNightOffset.put(BiomeTags.IGLOO_HAS_STRUCTURE,5F);
        seasonOffset.put(BiomeTags.IGLOO_HAS_STRUCTURE,20F);

        temperature.put(BiomeTags.VILLAGE_PLAINS_HAS_STRUCTURE,0.774F);
        humidity.put(BiomeTags.VILLAGE_PLAINS_HAS_STRUCTURE,60.0F);
        dayNightOffset.put(BiomeTags.VILLAGE_PLAINS_HAS_STRUCTURE,15F);
        seasonOffset.put(BiomeTags.VILLAGE_PLAINS_HAS_STRUCTURE,40F);

        temperature.put(BiomeTags.RUINED_PORTAL_SWAMP_HAS_STRUCTURE,0.685F);
        humidity.put(BiomeTags.RUINED_PORTAL_SWAMP_HAS_STRUCTURE,90.0F);
        dayNightOffset.put(BiomeTags.RUINED_PORTAL_SWAMP_HAS_STRUCTURE,10F);
        seasonOffset.put(BiomeTags.RUINED_PORTAL_SWAMP_HAS_STRUCTURE,40F);

        temperature.put(BiomeTags.DESERT_PYRAMID_HAS_STRUCTURE,1.354F);
        humidity.put(BiomeTags.DESERT_PYRAMID_HAS_STRUCTURE,20.0F);
        dayNightOffset.put(BiomeTags.DESERT_PYRAMID_HAS_STRUCTURE,20F);
        seasonOffset.put(BiomeTags.DESERT_PYRAMID_HAS_STRUCTURE,40F);
    }

    public Temperature(ServerPlayerEntity player, boolean shouldUpdate) {
        isServerSide = shouldUpdate;
        serverPlayer = player;
    }

    public void tick() {
        if(serverPlayer.isCreative())
            return;
        if(ticks % 2 == 0) {
            if (this.coreTemp < LOW) {
                if(serverPlayer.getFrozenTicks() < 200) {
                    serverPlayer.setFrozenTicks(serverPlayer.getFrozenTicks() + 5);
                }
            }
        }
        if(ticks % 60 == 0) {
            if (this.coreTemp > HIGH && this.coreTemp < 2.222891566F) {
                serverPlayer.damage(new DamageSource(serverPlayer.getServerWorld().getRegistryManager().get(RegistryKeys.DAMAGE_TYPE).getEntry(serverPlayer.getServerWorld().getRegistryManager().get(RegistryKeys.DAMAGE_TYPE).get(new Identifier("pyrofrost","hyperthermia")))),1F);
            }
        }
        if(ticks % 20 == 0) {
            if (this.coreTemp >= 2.222891566F) {
                serverPlayer.damage(new DamageSource(serverPlayer.getServerWorld().getRegistryManager().get(RegistryKeys.DAMAGE_TYPE).getEntry(serverPlayer.getServerWorld().getRegistryManager().get(RegistryKeys.DAMAGE_TYPE).get(new Identifier("pyrofrost","hyperthermia")))),1F);
            }
        }
        ticks += 1;
        if(ticks % 16 == 0 || ticks % 60 == 0) {
            this.wbgt = getWBGT();
            this.skinTempDir = getSkinTemperatureDirection((float)this.wbgt, this.skinTemp);
            double insulationModifier = getInsulationModifier(serverPlayer, wetness, skinTempDir, (float)wbgt);
            boolean canSweat = skinTemp >= NORMAL && wetness == 0;
            float tempChange = getAirTemperatureSkinChange(this.serverPlayer, insulationModifier);
            if (tempChange > 0.0F) {
                switch (skinTempDir) {
                    case COOLING -> {
                        tempChange = Math.max(-(tempChange) * 70.0F, -(0.022289157F * 3.0F));
                        if (wetness > 0) {
                            tempChange = tempChange * (float) (1.0 + (this.wetness / 20.0));
                        }
                    }
                    case COOLING_RAPIDLY -> {
                        tempChange = Math.max(-(tempChange) * 100.0F, -(0.022289157F * 4.0F));
                        if (this.wetness > 0) {
                            tempChange = tempChange * (float) (2.0 + (this.wetness / 20.0));
                        }
                    }
                    case COOLING_NORMALLY -> {
                        tempChange = -(tempChange);
                        if(!canSweat) {
                            float exhaustion = Math.abs(Math.min(tempChange * 200.0F, 0.2F));
                            serverPlayer.getHungerManager().addExhaustion(exhaustion);
                        }
                    }
                    case WARMING -> {
                        if(canSweat && ModCompatManager.isModAvailable("dehydration")) {
                            if (!(boolean)ModCompatManager.runMethod("dehydration","sweat",this.serverPlayer, Math.min(tempChange * 150.0F, 0.3F))) {
                                tempChange = Math.min(tempChange * 70.0F, 0.022289157F * 3.0F);
                            }
                        }
                    }
                    case WARMING_RAPIDLY -> tempChange = Math.min(tempChange * 100.0F, 0.022289157F * 4.0F);
                    case WARMING_NORMALLY -> {
                        if(canSweat && ModCompatManager.isModAvailable("dehydration")) {
                            ModCompatManager.runMethod("dehydration","sweat",this.serverPlayer,Math.min(tempChange * 100.0F, 0.1F));
                        }
                    }
                }
            }
            if (tempChange == 0.0F) {
                if (this.skinTemp < NORMAL) {
                    tempChange = (NORMAL - this.skinTemp) / 20.0F;
                }
                else if (this.skinTemp > NORMAL) {
                    tempChange = -((this.skinTemp - NORMAL) / 40.0F);
                }
            }
            var oldSkinTemp = this.skinTemp;
            if (this.skinTemp > NORMAL && this.skinTemp > oldSkinTemp && this.wbgt < this.skinTemp) {
                float coolingRate = Math.max((this.skinTemp - NORMAL) / 20.0F, 0.022289157F);
                if(canSweat && ModCompatManager.isModAvailable("dehydration")) {
                    if(wetness == 0 && (boolean)ModCompatManager.runMethod("dehydration","sweat",this.serverPlayer, 0F)) {
                        ModCompatManager.runMethod("dehydration","sweat",this.serverPlayer, Math.min(tempChange * 150.0F, 0.2F));
                        this.skinTemp = Math.max(this.skinTemp - coolingRate, NORMAL);
                    } else {
                        this.skinTemp = Math.max(this.skinTemp - (coolingRate * 2.0F), NORMAL);
                    }
                } else {
                    this.skinTemp = Math.max(this.skinTemp - (coolingRate * 2.0F), NORMAL);
                }
            }
            this.skinTemp += tempChange;
            final TemperatureDirection coreTempDir = getCoreTemperatureDirection(oldSkinTemp, this.coreTemp, this.skinTemp);
            float diff = Math.abs(this.skinTemp - this.coreTemp);
            float change;
            if (coreTempDir.coreRate > 0.0F) {
                change = diff * coreTempDir.coreRate;
            }
            else {
                change = diff * 0.1F;
            }
            if (this.skinTemp < this.coreTemp) {
                this.coreTemp -= change;
                if (coreTempDir == TemperatureDirection.COOLING_RAPIDLY) {
                    this.coreTemp = Math.max(this.coreTemp, NORMAL);
                }
            } else {
                this.coreTemp += change;
                if (coreTempDir == TemperatureDirection.WARMING_RAPIDLY) {
                    this.coreTemp = Math.min(this.coreTemp, NORMAL);
                }
            }
            UpdateTemperature.send(serverPlayer.getServer(),serverPlayer,this.coreTemp,this.skinTemp,(float)this.wbgt,this.wetness,(float)this.envRadiation);
        }
    }

    public static double mcTempToCelsius(float temp) {
        double out = 25.27027027 + (44.86486486 * temp);
        out = (out - 32) * 0.5556;
        return out;
    }

    public static double mcTempConv(float temp) {
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
        float rainBonus = (((serverPlayer.getServerWorld().hasRain(serverPlayer.getBlockPos().withY(320)))||(serverPlayer.getServerWorld().isRaining()))?0F:-20F);
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

    private static float getBiomeSeasonOffset(RegistryEntry<Biome> biome) {
        for(Map.Entry<TagKey<Biome>,Float> entry : seasonOffset.entrySet()) {
            if (biome.isIn(entry.getKey())) {
                return entry.getValue();
            }
        }
        return 40F;
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
        float humidity = serverPlayer.getServer().getGameRules().getBoolean(GameRules.DO_WEATHER_CYCLE)?this.getBiomeHumidity(serverPlayer.getServerWorld().getBiome(serverPlayer.getBlockPos())):0F;
        RegistryEntry<Biome> biome = serverPlayer.getServerWorld().getBiome(serverPlayer.getBlockPos());
        float dryTemperature = getSeasonTemp(serverPlayer.getServerWorld(),biome,getBiomeTemperature(biome))+getDayNightOffset(serverPlayer.getServerWorld(),getBiomeDayNightOffset(biome),humidity);
        double wetTemperature = getHeatIndex(dryTemperature,humidity);
        EnvironmentData data = getInfo();
        this.envRadiation = data.getRadiation() + getSolarRadiation(serverPlayer.getServerWorld(), BlockPos.ofFloored(serverPlayer.getCameraPosVec(1.0F)));
        double blackGlobeTemp = (float)getBlackGlobe(this.envRadiation, dryTemperature, humidity);
        double airTemperature;
        if (data.isSheltered() || data.isUnderground()) {
            airTemperature = (wetTemperature * 0.7F) + (blackGlobeTemp * 0.3F);
        } else {
            airTemperature = (wetTemperature * 0.7F) + (blackGlobeTemp * 0.2F) + (dryTemperature * 0.1F);
        }
        return airTemperature;
    }

    public static double getDistance(ServerPlayerEntity sp, Vec3d vPos) {
        double x = Math.max(0, Math.abs(sp.getX() - vPos.x) - sp.getWidth() / 2);
        double y = Math.max(0, Math.abs((sp.getY() + sp.getHeight() / 2) - vPos.y) - sp.getHeight() / 2);
        double z = Math.max(0, Math.abs(sp.getZ() - vPos.z) - sp.getWidth() / 2);

        return Math.sqrt(x * x + y * y + z * z);
    }

    public static boolean isBlockObscured(ServerPlayerEntity sp, Vec3d blockVec) {
        Vec3d playerVec = new Vec3d(sp.getX(), sp.getEyeY(), sp.getZ());
        RaycastContext raycastContext = new RaycastContext(playerVec, blockVec, RaycastContext.ShapeType.COLLIDER, RaycastContext.FluidHandling.NONE, sp);

        return sp.getServerWorld().raycast(raycastContext).getType() != HitResult.Type.MISS;
    }

    private EnvironmentData getInfo() {
        boolean isSheltered = true; // So basically me
        boolean isUnderground = true;
        double waterBlocks = 0;
        double totalBlocks = 0;
        double radiation = 0.0;
        float moisture = 0.0F;
        this.isPartialSubmersion = !serverPlayer.isSubmergedInWater() && serverPlayer.isTouchingWater() && serverPlayer.isWet();
        this.isSubmerged = serverPlayer.isSubmergedInWater() && serverPlayer.isTouchingWater() && serverPlayer.isWet();
        if (isSubmerged) {
            moisture = 20.0F;
        }
        else if (isPartialSubmersion) {
            moisture = 10.0F;
        }
        else if (serverPlayer.isWet()) {
            moisture = 0.5F;
        }
        if (moisture > 0.0F) {
            incMoisture(moisture,0);
        }
        BlockPos pos = serverPlayer.getBlockPos();
        for (int x = -12; x <= 12; x++) {
            for (int z = -12; z <= 12; z++) {
                if (isSheltered && (x <= 2 && x >= -2) && (z <= 2 && z >= -2)) {
                    isSheltered = !serverPlayer.getServerWorld().isSkyVisible(BlockPos.ofFloored(serverPlayer.getCameraPosVec(1.0F)).add(x, 0, z).up());
                }
                for (int y = -3; y <= 11; y++) {
                    ChunkPos chunkPos = new ChunkPos((pos.getX() + x) >> 4,(pos.getZ() + z) >> 4);
                    Chunk chunk = serverPlayer.getServerWorld().getChunk(chunkPos.getStartPos());

                    if (chunk == null) continue;
                    BlockPos blockpos = pos.add(x, y, z);
                    PalettedContainer<BlockState> palette;
                    try {
                        palette = chunk.getSection((blockpos.getY() >> 4) - chunk.getBottomSectionCoord()).getBlockStateContainer();

                    }
                    catch (Exception e) {
                        continue;
                    }
                    BlockState state = palette.get(blockpos.getX() & 15, blockpos.getY() & 15, blockpos.getZ() & 15);
                    boolean isWater = state.isOf(Blocks.WATER);
                    if (isUnderground && y >= 0 && !isWater) {
                        isUnderground = !serverPlayer.getServerWorld().isSkyVisible(BlockPos.ofFloored(serverPlayer.getCameraPosVec(1.0F)).add(x, y, z).up());
                    }
                    if ((x <= 5 && x >= -5) && (y <= 5) && (z <= 5 && z >= -5)) {
                        totalBlocks++;

                        if (isWater) {
                            waterBlocks++;
                        }
                    }
                    if (state.isAir()) continue;
                    if(y <= 3) {
                        Float rad = ThermalRadiation.radiationBlocks.get(Registries.BLOCK.getId(state.getBlock()));
                        if (rad != null) {
                            boolean emitting = true;
                            if (state.contains(Properties.LIT)) {
                                emitting = state.get(Properties.LIT);
                            }
                            if(emitting) {
                                Vec3d vPos = new Vec3d(blockpos.getX() + 0.5, blockpos.getY() + 0.5, blockpos.getZ() + 0.5);
                                double distance = getDistance(serverPlayer, vPos);
                                boolean obscured = isBlockObscured(serverPlayer, vPos);
                                double radi;

                                if (distance <= 1) {
                                    radi = rad;
                                } else {
                                    radi = rad / distance;
                                }

                                if (y > 0 && y < 5) {
                                    radi = radi * ((4 - y) * 0.25);
                                }

                                if (obscured) {
                                    radi = radi * 0.9;
                                }
                                radiation += Math.min(radi, rad);
                            }
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

    private static double getInsulationModifier(double coldBase, ItemStack armor, double wetnessModifier, float lTemperature) {
        double modifier;
        double armorModifier = !armor.isEmpty() ? 3.0 : 0.0;
        boolean isCold = lTemperature < 0.997F;
        boolean isWarm = lTemperature > 1.220F;

        if (isCold) {
            modifier = coldBase + armorModifier;
        }
        else {
            // Don't include coldBase if warm
            modifier = armorModifier;

            // Increase base armor insulation effectiveness if wet
            if (isWarm && wetnessModifier != -1) {
                if (armorModifier != 0.0) {
                    modifier = armorModifier * (1.0 + wetnessModifier);
                }
                else {
                    modifier = coldBase * wetnessModifier;
                }
            }
        }

        // Add modifier for any armor type
        if (armorModifier != 0.0) {
            if (armor.isOf(ItemRegistry.WOLF_FUR_HELMET) || armor.isOf(ItemRegistry.WOLF_FUR_CHESTPLATE) || armor.isOf(ItemRegistry.WOLF_FUR_LEGGINGS) || armor.isOf(ItemRegistry.WOLF_FUR_PAWS)) {
                if (isCold) {
                    modifier += 4.0;
                }

                if (isCold && wetnessModifier != -1) {
                    modifier = modifier * (wetnessModifier / 2.0);
                }
            }
        }
        else if (wetnessModifier != -1 && isCold) {
            modifier = modifier * wetnessModifier;
        }

        return modifier;
    }

    public static double getInsulationModifier(ServerPlayerEntity sp, int wetness, TemperatureDirection direction, float lTemperature) {
        double modifier = 0.0;
        double wetnessModifier = -1;

        if (wetness > 0) {
            if (lTemperature < 1.220F) {
                wetnessModifier = 1.0 - (wetness / 20.0);
            } else {
                wetnessModifier = wetness / 20.0;
            }
        }

        if (direction != TemperatureDirection.WARMING_RAPIDLY && direction != TemperatureDirection.COOLING_RAPIDLY) {
            modifier += getInsulationModifier(0.0, sp.getEquippedStack(EquipmentSlot.HEAD), wetnessModifier, lTemperature);
            modifier += getInsulationModifier(4.3, sp.getEquippedStack(EquipmentSlot.CHEST), wetnessModifier, lTemperature);
            modifier += getInsulationModifier(4.3, sp.getEquippedStack(EquipmentSlot.LEGS), wetnessModifier, lTemperature);
            modifier += getInsulationModifier(4.3, sp.getEquippedStack(EquipmentSlot.FEET), wetnessModifier, lTemperature);
        }

        return modifier;
    }


    public float getAirTemperatureSkinChange(ServerPlayerEntity sp, double insulationModifier) {
        float localTemperature = (float)this.wbgt;
        float change = 0.0F;
        double localTempF = mcTempConv(localTemperature);
        double parityTempF = mcTempConv(1.108F);
        double extremeTempF = mcTempConv(2.557F);
        double minutes;
        float radiationModifier = (float) (this.envRadiation / 5000) + 1.0F;
        float moisture = 0.0F;
        double temp;

        if (!serverPlayer.isWet()) {
            moisture = 0.2F * (3.0F + radiationModifier);
        }

        if (moisture > 0.0F) {
            if (this.moistureLevel < -4.0F) {
                moistureLevel += 4.0F;
                wetness = Math.max(wetness - 1, 0);
            }
            if (this.wetness > 0) {
                this.moistureLevel = Math.max(this.moistureLevel - moisture, -20F);
            }
            else {
                this.moistureLevel = 0.0F;
            }
        }

        if (this.skinTempDir == TemperatureDirection.NONE) return change;

        if (localTemperature < 1.108F) {
            temp = Math.min(localTempF + insulationModifier, parityTempF);

            if (Math.abs(parityTempF - temp) > 5.0) {
                minutes = 383.4897 + (12.38784 - 383.4897) / (1 + Math.pow((temp / 43.26779), 8.271186));

                change = (NORMAL - LOW) / (float) minutes;
            }
        }
        else {
            temp = Math.max(localTempF - insulationModifier, parityTempF);

            if (Math.abs(parityTempF - temp) > 5.0) {
                // It is really, really hot ... increase rapidly
                if (temp > extremeTempF) {
                    change = (float) ((temp - extremeTempF) / 50.0) * 0.0067F;
                } else {
                    minutes = 24.45765 + (599.3552 - 24.45765) / (1 + Math.pow((temp / 109.1499), 27.47623));

                    change = (HIGH - NORMAL) / (float) minutes;
                }
            }
        }

        if ((this.coreTemp < NORMAL && this.envRadiation > 0) || radiationModifier > 5.0F) {
            change = change * radiationModifier;
        }

        return change;
    }

    private static float getSeasonTemp(ServerWorld world, RegistryEntry<Biome> biome, float biomeTemp) {
        if(ModCompatManager.isModAvailable("seasons")) {
            int subSeason = (int)ModCompatManager.runMethod("seasons","getSubSeason",world);
            if(subSeason == -1) {
                return biomeTemp;
            }
            int season;
            if ((subSeason + 9) <= 12) {
                season = subSeason + 9;
            }
            else {
                season = subSeason - 3;
            }
            float lateSummerOffset = 0.022289157F * 5;
            float variation = (getBiomeSeasonOffset(biome) * 0.022289157F) / 2.0F;
            double temp = variation * Math.cos( ((season - 1) * Math.PI) / 6) + biomeTemp;
            if (season == 2) {
                temp += lateSummerOffset;
            }
            return (float)temp;
        } else {
            return biomeTemp;
        }
    }
}
