package arekkuusu.enderskills.client.render.entity;

import arekkuusu.enderskills.client.ClientConfig;
import arekkuusu.enderskills.client.proxy.ClientProxy;
import arekkuusu.enderskills.client.render.effect.ParticleVanilla;
import arekkuusu.enderskills.client.render.model.ModelCrush;
import arekkuusu.enderskills.client.render.skill.SkillRenderer;
import arekkuusu.enderskills.client.util.ShaderLibrary;
import arekkuusu.enderskills.client.util.helper.GLHelper;
import arekkuusu.enderskills.common.entity.EntityCrush;
import arekkuusu.enderskills.common.lib.LibMod;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.Vec3d;

public class EntityCrushRenderer extends Render<EntityCrush> {

    public static final ResourceLocation TEXTURE = new ResourceLocation(LibMod.MOD_ID, "textures/entity/crush.png");
    public final ModelCrush model = new ModelCrush();

    public EntityCrushRenderer(RenderManager manager) {
        super(manager);
    }

    public void doRender(EntityCrush entity, double x, double y, double z, float entityYaw, float partialTicks) {
        if (entity.lifeTicks % 10 == 0) {
            Vec3d vec = entity.getPositionVector();
            for (int i = 1; i < 2; i++) {
                if (entity.world.rand.nextDouble() < 0.6D && ClientProxy.canParticleSpawn()) {
                    double posX = vec.x + (entity.world.rand.nextDouble() - 0.5D) * entity.width;
                    double posY = vec.y - 0.5 * entity.world.rand.nextDouble();
                    double posZ = vec.z + (entity.world.rand.nextDouble() - 0.5D) * entity.width;
                    double motionX = (entity.world.rand.nextDouble() - 0.5D) * 0.25;
                    double motionZ = (entity.world.rand.nextDouble() - 0.5D) * 0.25;
                    ParticleVanilla vanilla = new ParticleVanilla(entity.world, new Vec3d(posX, posY, posZ), new Vec3d(motionX, 0.4, motionZ), 12F, 25, 0xFFFFFF, 0);
                    vanilla.setCanCollide(true);
                    Minecraft.getMinecraft().effectRenderer.addEffect(vanilla);
                }
            }
        }
        float animationProgress = entity.getAnimationProgress(partialTicks);
        if (animationProgress != 0.0F) {
            float scale = entity.getSize() * 2;
            if (animationProgress > 0.9F) {
                scale = (float) (scale * ((1.0F - animationProgress) / 0.10000000149011612D));
            }

            GlStateManager.pushMatrix();
            GlStateManager.disableCull();
            this.bindEntityTexture(entity);
            GlStateManager.translate(x, y, z);
            GLHelper.BLEND_NORMAL.blend();
            if (!ClientConfig.RENDER_CONFIG.rendering.helpMyFramesAreDying) {
                ShaderLibrary.ALPHA.begin();
                ShaderLibrary.ALPHA.set("alpha", SkillRenderer.getDiffuseBlend(22 - entity.lifeTicks, 22, 0.4F));
            }
            GlStateManager.disableLighting();
            GlStateManager.enableBlend();
            GlStateManager.translate(-0.5, 0, -0.5);
            GlStateManager.rotate(90.0F - entity.rotationYaw, 0.0F, 1.0F, 0.0F);
            GlStateManager.scale(-scale, -scale, -scale);
            GlStateManager.translate(0F, -0.626F, 0F);
            this.model.render(entity, animationProgress, 0.0F, 0.0F, entity.rotationYaw, entity.rotationPitch, 0.03125F);
            GlStateManager.disableBlend();
            GlStateManager.enableLighting();
            if (!ClientConfig.RENDER_CONFIG.rendering.helpMyFramesAreDying) {
                ShaderLibrary.ALPHA.end();
            }
            GLHelper.BLEND_NORMAL.blend();
            GlStateManager.enableCull();
            GlStateManager.popMatrix();
            super.doRender(entity, x, y, z, entityYaw, partialTicks);
        }
    }

    protected ResourceLocation getEntityTexture(EntityCrush entity) {
        return TEXTURE;
    }
}
