package arekkuusu.enderskills.client.render.model;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.entity.Entity;

public class ModelFist extends ModelBase {
    public ModelRenderer model;

    public ModelFist() {
        this.textureWidth = 16;
        this.textureHeight = 16;
        this.model = new ModelRenderer(this, 0, 0);
        this.model.setRotationPoint(0.0F, 0.0F, 0.0F);
        this.model.addBox(-2.0F, -6.0F, -2.0F, 4, 12, 4, 0.0F);
        this.setRotateAngle(model, 0.0F, 0.0F, -1.5707963267948966F);
    }

    @Override
    public void render(Entity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, float scale) {
        setRotationAngles(limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scale, entity);
        this.model.render(scale);
    }

    public void setRotationAngles(float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, float scaleFactor, Entity entityIn) {
        this.model.rotateAngleY = netHeadYaw * 0.017453292F;
        this.model.rotateAngleX = headPitch * 0.017453292F;
    }

    /**
     * This is a helper function from Tabula to set the rotation of model parts
     */
    public void setRotateAngle(ModelRenderer modelRenderer, float x, float y, float z) {
        modelRenderer.rotateAngleX = x;
        modelRenderer.rotateAngleY = y;
        modelRenderer.rotateAngleZ = z;
    }
}
