package sh.talonfox.pyrofrost.mixin;

import net.minecraft.block.PowderSnowBlock;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import sh.talonfox.pyrofrost.registry.ItemRegistry;

@Mixin(PowderSnowBlock.class)
public class PowderSnowMixin {
    @Inject(method = "canWalkOnPowderSnow", at = @At("HEAD"), cancellable = true)
    private static void wolfPawsOnSnowOwO(Entity entity, CallbackInfoReturnable<Boolean> info) {
        if (entity instanceof LivingEntity && ((LivingEntity) entity).getEquippedStack(EquipmentSlot.FEET).isOf(ItemRegistry.WOLF_FUR_PAWS))
            info.setReturnValue(true);
    }
}
