package sh.talonfox.pyrofrost;

import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.util.Identifier;
import sh.talonfox.pyrofrost.temperature.Temperature;

public class TemperatureHud implements HudRenderCallback {
    private static final Identifier ICONS = new Identifier("pyrofrost","textures/gui/icons.png");
    @Override
    public void onHudRender(DrawContext drawContext, float tickDelta) {
        float coreTexX = 0F;
        float skinTexX = 0F;
        if(PyrofrostClient.coreTemp <= Temperature.LOW) {
            coreTexX = 0F;
        } else if(PyrofrostClient.coreTemp <= Temperature.LOW_WARNING3) {
            coreTexX = 16F*1;
        } else if(PyrofrostClient.coreTemp <= Temperature.LOW_WARNING2) {
            coreTexX = 16F*2;
        } else if(PyrofrostClient.coreTemp <= Temperature.LOW_WARNING1) {
            coreTexX = 16F*3;
        } else if(PyrofrostClient.coreTemp < Temperature.HIGH_WARNING1) {
            coreTexX = 16F*4;
        } else if(PyrofrostClient.coreTemp >= Temperature.HIGH) {
            coreTexX = 16F*8;
        } else if(PyrofrostClient.coreTemp >= Temperature.HIGH_WARNING3) {
            coreTexX = 16F*7;
        } else if(PyrofrostClient.coreTemp >= Temperature.HIGH_WARNING2) {
            coreTexX = 16F*6;
        } else if(PyrofrostClient.coreTemp >= Temperature.HIGH_WARNING1) {
            coreTexX = 16F*5;
        }
        if(PyrofrostClient.localTemp <= 0.3283192771F) { // EXTREMELY_COLD
            skinTexX = 0F;
        } else if(PyrofrostClient.localTemp <= 0.886F) { // COLD
            skinTexX = 20F;
        } else if(PyrofrostClient.localTemp <= 0.997F) { // LOW
            skinTexX = 20F*2;
        } else if(PyrofrostClient.localTemp > 0.997F && PyrofrostClient.localTemp < 1.220F) { // NEUTRAL
            skinTexX = 20F*3;
        } else if(PyrofrostClient.localTemp >= 2.2225F) { // EXTREMELY_HOT
            skinTexX = 20F*6;
        } else if(PyrofrostClient.localTemp >= 1.888F) { // HOT
            skinTexX = 20F*5;
        } else if(PyrofrostClient.localTemp >= 1.220F) { // HIGH
            skinTexX = 20F*4;
        }
        if(PyrofrostClient.sweat || PyrofrostClient.coreTemp <= Temperature.LOW || PyrofrostClient.coreTemp >= Temperature.HIGH) {
            int offset = ((MinecraftClient.getInstance().world.getTimeOfDay() % 4) < 2) ? -2 : 2;
            drawContext.drawTexture(ICONS, ((drawContext.getScaledWindowWidth() / 2) - 8) + offset, drawContext.getScaledWindowHeight() - 56, 16, 16, coreTexX, 0F, 16, 16, 256, 256);
            drawContext.drawTexture(ICONS, ((drawContext.getScaledWindowWidth() / 2) - 10) + offset, drawContext.getScaledWindowHeight() - 58, 20, 20, skinTexX, 16F, 20, 20, 256, 256);
        } else {
            drawContext.drawTexture(ICONS, (drawContext.getScaledWindowWidth() / 2) - 8, drawContext.getScaledWindowHeight() - 56, 16, 16, coreTexX, 0F, 16, 16, 256, 256);
            drawContext.drawTexture(ICONS, (drawContext.getScaledWindowWidth() / 2) - 10, drawContext.getScaledWindowHeight() - 58, 20, 20, skinTexX, 16F, 20, 20, 256, 256);
        }
        for(int i=0; i < 10; i++) {
            drawContext.drawTexture(ICONS,((drawContext.getScaledWindowWidth() / 2) + 82 - (i * 9) + i),drawContext.getScaledWindowHeight() - 49,7,9,(PyrofrostClient.thirst>i?0F:7F),36F,7,9,256,256);
        }
    }
}
