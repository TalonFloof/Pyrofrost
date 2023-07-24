package sh.talonfox.pyrofrost.mixin.client;

import net.minecraft.client.gui.hud.InGameHud;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(InGameHud.class)
public class InGameHudMixin {
    @Inject(method = "getHeartRows", at = @At(value = "HEAD"), cancellable = true)
    private void vanillaThirst$fixThirstBarPosition(int heartCount, CallbackInfoReturnable<Integer> cir) {
        cir.setReturnValue((int) Math.ceil((double) heartCount / 10.0D) + 1);
    }
}
