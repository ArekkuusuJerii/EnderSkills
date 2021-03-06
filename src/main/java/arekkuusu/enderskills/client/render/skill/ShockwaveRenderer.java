package arekkuusu.enderskills.client.render.skill;

import arekkuusu.enderskills.client.proxy.ClientProxy;
import arekkuusu.enderskills.common.entity.placeable.EntityPlaceableShockwave;
import arekkuusu.enderskills.common.skill.ability.defense.earth.Shockwave;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockRendererDispatcher;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.opengl.GL11;

import javax.annotation.Nonnull;

@SideOnly(Side.CLIENT)
public class ShockwaveRenderer extends SkillRenderer<Shockwave> {

    @SideOnly(Side.CLIENT)
    public static class Placeable extends Render<EntityPlaceableShockwave> {

        public Placeable(RenderManager renderManager) {
            super(renderManager);
        }

        @Override
        public void doRender(EntityPlaceableShockwave entity, double x, double y, double z, float entityYaw, float partialTicks) {
            GlStateManager.pushMatrix();
            Vec3d originVec = entity.getPositionVector();
            int index = 0;
            for (BlockPos[] arr : entity.getTerrainBlocks()) {
                for (BlockPos pos : arr) {
                    IBlockState state = entity.world.getBlockState(pos);
                    if (state.getRenderType() == EnumBlockRenderType.MODEL && state.getRenderType() != EnumBlockRenderType.INVISIBLE && entity.curves[index] != 0) {
                        for (int i = 0; i < 2; i++) {
                            if (entity.world.rand.nextDouble() < 0.1D && ClientProxy.canParticleSpawn()) {
                                double posX = pos.getX() + 1 * entity.world.rand.nextDouble();
                                double posY = pos.getY() + 1.15 + 0.1 * (entity.world.rand.nextDouble() - 0.5);
                                double posZ = pos.getZ() + 1 * entity.world.rand.nextDouble();
                                entity.world.spawnParticle(EnumParticleTypes.BLOCK_CRACK, posX, posY, posZ, 0.0D, 0.01D, 0.0D, Block.getStateId(state));
                            }
                        }
                        GlStateManager.pushMatrix();
                        GlStateManager.translate(x - originVec.x, y - originVec.y - 1D, z - originVec.z);
                        GlStateManager.translate(0, entity.curves[index], 0);
                        this.bindTexture(getEntityTexture(entity));
                        GlStateManager.disableLighting();
                        Tessellator tessellator = Tessellator.getInstance();
                        BufferBuilder buffer = tessellator.getBuffer();
                        buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.BLOCK);
                        BlockRendererDispatcher dispatcher = Minecraft.getMinecraft().getBlockRendererDispatcher();
                        dispatcher.getBlockModelRenderer().renderModel(entity.world, dispatcher.getModelForState(state),
                                state, pos.up(), buffer, false, 0);
                        tessellator.draw();
                        GlStateManager.enableLighting();
                        GlStateManager.popMatrix();
                    }
                }
                index++;
            }
            GlStateManager.popMatrix();
        }

        @Override
        @Nonnull
        protected ResourceLocation getEntityTexture(EntityPlaceableShockwave entity) {
            return TextureMap.LOCATION_BLOCKS_TEXTURE;
        }
    }
}
