package sh.talonfox.pyrofrost.modcompat;

import io.github.lucaargolo.seasons.FabricSeasons;
import io.github.lucaargolo.seasons.utils.Season;
import net.minecraft.server.world.ServerWorld;
import sh.talonfox.pyrofrost.Pyrofrost;

public class FabricSeasonsCompat {
    public static void init() {
        Pyrofrost.LOGGER.info("Loaded Compatibility Module for Fabric Seasons");
        getSubSeason(null);
    }

    public static int getSubSeason(ServerWorld world) {
        if(world == null) {
            return -1;
        }
        if(!FabricSeasons.CONFIG.isValidInDimension(world.getRegistryKey())) {
            return -1;
        }
        Season season = FabricSeasons.getCurrentSeason(world);
        long time = FabricSeasons.getTimeToNextSeason(world);
        long timeLen = season.getSeasonLength();
        if(time == Long.MAX_VALUE) {
            return 1+(season.ordinal()*3);
        } else {
            Pyrofrost.LOGGER.info(String.valueOf((season.ordinal()*3)+(int)Math.floor(((double)(timeLen-time))/((double)timeLen/3))));
            return (season.ordinal()*3)+(int)Math.floor(((double)(timeLen-time))/((double)timeLen/3));
        }
    }
}
