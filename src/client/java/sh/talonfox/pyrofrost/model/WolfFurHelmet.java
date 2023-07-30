package sh.talonfox.pyrofrost.model;

import net.minecraft.client.model.*;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.util.math.MatrixStack;

public class WolfFurHelmet extends Model {
    private final ModelPart wolfyOwO;
    public WolfFurHelmet(ModelPart root) {
        super(RenderLayer::getArmorCutoutNoCull);
        this.wolfyOwO = root.getChild("wolfyOwO");
    }
    public static TexturedModelData getTexturedModelData() {
        ModelData modelData = new ModelData();
        ModelPartData modelPartData = modelData.getRoot();
        ModelPartData wolfyOwO = modelPartData.addChild("wolfyOwO", ModelPartBuilder.create().uv(0, 0).cuboid(-12.0F, -8.0F, 4.0F, 8.0F, 8.0F, 8.0F, new Dilation(0.0F))
                .uv(0, 0).cuboid(-12.0F, -13.0F, 8.0F, 3.0F, 5.0F, 0.001F, new Dilation(0.0F))
                .uv(0, 0).mirrored().cuboid(-7.0F, -13.0F, 8.0F, 3.0F, 5.0F, 0.001F, new Dilation(0.0F)).mirrored(false)
                .uv(24, 0).cuboid(-10.0F, -6.0F, 2.0F, 4.0F, 2.0F, 2.0F, new Dilation(0.0F)), ModelTransform.pivot(8.0F, 24.0F, -8.0F));
        return TexturedModelData.of(modelData, 64, 32);
    }
    @Override
    public void render(MatrixStack matrices, VertexConsumer vertexConsumer, int light, int overlay, float red, float green, float blue, float alpha) {
        wolfyOwO.render(matrices, vertexConsumer, light, overlay, red, green, blue, alpha);
    }
}
