package sh.talonfox.pyrofrost.modcompat;

import net.dehydration.api.DehydrationAPI;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import sh.talonfox.pyrofrost.Pyrofrost;
import net.dehydration.access.ThirstManagerAccess;

public class DehydrationCompat implements DehydrationAPI {
    public static void init() {
        Pyrofrost.LOGGER.info("Loaded Compatibility Module for Dehydration");
    }

    public static boolean sweat(ServerPlayerEntity player, float amount) {
        if(((ThirstManagerAccess)player).getThirstManager().getThirstLevel() > 4) {
            ((ThirstManagerAccess) player).getThirstManager().addDehydration(amount);
            return true;
        }
        return false;
    }

    @Override
    public int calculateDrinkThirst(ItemStack itemStack, PlayerEntity playerEntity) {
        return 0;
    }
}
