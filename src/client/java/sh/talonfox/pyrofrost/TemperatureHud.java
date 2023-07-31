package sh.talonfox.pyrofrost;

import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.*;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.Identifier;
import sh.talonfox.pyrofrost.registry.ItemRegistry;
import sh.talonfox.pyrofrost.temperature.Temperature;

public class TemperatureHud implements HudRenderCallback {
    private static final Identifier ICONS = new Identifier("pyrofrost","textures/gui/icons.png");
    private static final Identifier WET_OVERLAY = new Identifier("pyrofrost","textures/gui/wetness_overlay.png");
    private static final boolean DEBUG = true;
    private static long frame = 0;
    @Override
    public void onHudRender(DrawContext drawContext, float tickDelta) {
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.depthMask(true);
        RenderSystem.enableDepthTest();
        frame += 1;
        assert MinecraftClient.getInstance().interactionManager != null;
        if(MinecraftClient.getInstance().interactionManager.hasStatusBars()) {
            float coreTexX = 0F;
            float skinTexX = 0F;
            if (PyrofrostClient.coreTemp <= Temperature.LOW) {
                coreTexX = 0F;
            } else if (PyrofrostClient.coreTemp <= Temperature.LOW_WARNING3) {
                coreTexX = 16F * 1;
            } else if (PyrofrostClient.coreTemp <= Temperature.LOW_WARNING2) {
                coreTexX = 16F * 2;
            } else if (PyrofrostClient.coreTemp <= Temperature.LOW_WARNING1) {
                coreTexX = 16F * 3;
            } else if (PyrofrostClient.coreTemp < Temperature.HIGH_WARNING1) {
                coreTexX = 16F * 4;
            } else if (PyrofrostClient.coreTemp >= Temperature.HIGH) {
                coreTexX = 16F * 8;
            } else if (PyrofrostClient.coreTemp >= Temperature.HIGH_WARNING3) {
                coreTexX = 16F * 7;
            } else if (PyrofrostClient.coreTemp >= Temperature.HIGH_WARNING2) {
                coreTexX = 16F * 6;
            } else if (PyrofrostClient.coreTemp >= Temperature.HIGH_WARNING1) {
                coreTexX = 16F * 5;
            }
            if (PyrofrostClient.localTemp <= 0.3283192771F) { // EXTREMELY_COLD
                skinTexX = 0F;
            } else if (PyrofrostClient.localTemp <= 0.886F) { // COLD
                skinTexX = 20F;
            } else if (PyrofrostClient.localTemp <= 0.997F) { // LOW
                skinTexX = 20F * 2;
            } else if (PyrofrostClient.localTemp > 0.997F && PyrofrostClient.localTemp < 1.220F) { // NEUTRAL
                skinTexX = 20F * 3;
            } else if (PyrofrostClient.localTemp >= 2.2225F) { // EXTREMELY_HOT
                skinTexX = 20F * 6;
            } else if (PyrofrostClient.localTemp >= 1.888F) { // HOT
                skinTexX = 20F * 5;
            } else if (PyrofrostClient.localTemp >= 1.220F) { // HIGH
                skinTexX = 20F * 4;
            }
            if (PyrofrostClient.coreTemp <= Temperature.LOW || PyrofrostClient.coreTemp >= Temperature.HIGH) {
                int offset = 0;
                assert MinecraftClient.getInstance().world != null;
                if(PyrofrostClient.coreTemp >= 2.222891566F) {
                    offset = ((frame % 8) < 4) ? -2 : 2;
                } else {
                    offset = ((frame % 16) < 8) ? -2 : 2;
                }
                drawContext.drawTexture(ICONS, ((drawContext.getScaledWindowWidth() / 2) - 8) + Pyrofrost.CONFIG.Client_IconX + offset, drawContext.getScaledWindowHeight() - 56, 16, 16, coreTexX, 0F, 16, 16, 256, 256);
                drawContext.drawTexture(ICONS, ((drawContext.getScaledWindowWidth() / 2) - 10) + Pyrofrost.CONFIG.Client_IconY + offset, drawContext.getScaledWindowHeight() - 58, 20, 20, skinTexX, 16F, 20, 20, 256, 256);

            } else {
                drawContext.drawTexture(ICONS, ((drawContext.getScaledWindowWidth() / 2) - 8) + Pyrofrost.CONFIG.Client_IconX, drawContext.getScaledWindowHeight() - 56, 16, 16, coreTexX, 0F, 16, 16, 256, 256);
                drawContext.drawTexture(ICONS, ((drawContext.getScaledWindowWidth() / 2) - 10) + Pyrofrost.CONFIG.Client_IconY, drawContext.getScaledWindowHeight() - 58, 20, 20, skinTexX, 16F, 20, 20, 256, 256);
            }
            for(int i = 0; i < 9; i++) {
                if(MinecraftClient.getInstance().player.getInventory().main.get(i).getItem() == ItemRegistry.THERMOMETOR_ITEM) {
                    drawContext.getMatrices().push();
                    drawContext.getMatrices().scale(0.5F,0.5F,1F);
                    if(Pyrofrost.CONFIG.Client_UseFahrenheit) {
                        drawContext.drawCenteredTextWithShadow(MinecraftClient.getInstance().textRenderer, String.format("%.1f°F", Temperature.mcTempConv(PyrofrostClient.localTemp)), drawContext.getScaledWindowWidth(), ((drawContext.getScaledWindowHeight() * 2) - (65 * 2)) + (Pyrofrost.CONFIG.Client_IconX * 2), 0xffffffff);
                        drawContext.drawCenteredTextWithShadow(MinecraftClient.getInstance().textRenderer, String.format("%.1f°F", Temperature.mcTempConv(PyrofrostClient.coreTemp)), drawContext.getScaledWindowWidth(), ((drawContext.getScaledWindowHeight() * 2) - (50 * 2)) + (Pyrofrost.CONFIG.Client_IconY * 2), 0xffffffff);
                    } else {
                        drawContext.drawCenteredTextWithShadow(MinecraftClient.getInstance().textRenderer, String.format("%.1f°C", Temperature.mcTempToCelsius(PyrofrostClient.localTemp)), drawContext.getScaledWindowWidth(), ((drawContext.getScaledWindowHeight() * 2) - (65 * 2)) + (Pyrofrost.CONFIG.Client_IconX * 2), 0xffffffff);
                        drawContext.drawCenteredTextWithShadow(MinecraftClient.getInstance().textRenderer, String.format("%.1f°C", Temperature.mcTempToCelsius(PyrofrostClient.coreTemp)), drawContext.getScaledWindowWidth(), ((drawContext.getScaledWindowHeight() * 2) - (50 * 2)) + (Pyrofrost.CONFIG.Client_IconY * 2), 0xffffffff);
                    }
                    drawContext.getMatrices().pop();
                }
            }
            if(PyrofrostClient.wetness > 0) {
                Tessellator tessellator = Tessellator.getInstance();
                BufferBuilder bufferbuilder = tessellator.getBuffer();
                double width = drawContext.getScaledWindowWidth();
                double height = drawContext.getScaledWindowHeight();

                drawContext.getMatrices().push();
                RenderSystem.disableDepthTest();
                RenderSystem.depthMask(false);
                RenderSystem.enableBlend();
                RenderSystem.defaultBlendFunc();
                RenderSystem.setShader(GameRenderer::getPositionTexProgram);
                RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, ((float)PyrofrostClient.wetness)/20F);
                RenderSystem.setShaderTexture(0, WET_OVERLAY);
                bufferbuilder.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE);
                bufferbuilder.vertex(0.0D, height, -90.0D).texture(0.0F, 1.0F).next();
                bufferbuilder.vertex(width, height, -90.0D).texture(1.0F, 1.0F).next();
                bufferbuilder.vertex(width, 0.0D, -90.0D).texture(1.0F, 0.0F).next();
                bufferbuilder.vertex(0.0D, 0.0D, -90.0D).texture(0.0F, 0.0F).next();
                tessellator.draw();
                RenderSystem.depthMask(true);
                RenderSystem.enableDepthTest();
                RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
                RenderSystem.disableBlend();
                drawContext.getMatrices().pop();
            }
            RenderSystem.disableBlend();
        }
    }
}
