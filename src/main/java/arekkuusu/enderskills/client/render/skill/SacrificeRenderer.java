package arekkuusu.enderskills.client.render.skill;

import arekkuusu.enderskills.api.capability.data.SkillHolder;
import arekkuusu.enderskills.client.ClientConfig;
import arekkuusu.enderskills.client.util.ResourceLibrary;
import arekkuusu.enderskills.client.util.ShaderLibrary;
import arekkuusu.enderskills.common.EnderSkills;
import arekkuusu.enderskills.common.skill.ModAbilities;
import arekkuusu.enderskills.common.skill.SkillHelper;
import arekkuusu.enderskills.common.skill.ability.offence.blood.Sacrifice;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.client.event.RenderLivingEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class SacrificeRenderer extends SkillRenderer<Sacrifice> {

    public SacrificeRenderer() {
        MinecraftForge.EVENT_BUS.register(new Events());
    }

    @Override
    public void render(Entity entity, double x, double y, double z, float partialTicks, SkillHolder skillHolder) {
        if (skillHolder.tick == 1) {
            Vec3d vec = entity.getPositionVector();
            double posX = vec.x;
            double posY = vec.y + entity.height / 2;
            double posZ = vec.z;
            EnderSkills.getProxy().spawnParticle(entity.world, new Vec3d(posX, posY, posZ), new Vec3d(0, 0.05, 0), 20F, 50, 0x8A0303, ResourceLibrary.CROSS);
        }
    }

    @SideOnly(Side.CLIENT)
    public static class Events {

        @SubscribeEvent
        public void onRenderPre(RenderLivingEvent.Pre<EntityLivingBase> event) {
            if (SkillHelper.isActiveFrom(event.getEntity(), ModAbilities.SACRIFICE)) {
                if (!ClientConfig.RENDER_CONFIG.rendering.helpMyShadersAreDying) {
                    ShaderLibrary.BLEED.begin();
                    ShaderLibrary.BLEED.set("intensity", 0.8F);
                }
            }
        }

        @SubscribeEvent
        public void onRenderPost(RenderLivingEvent.Post<EntityLivingBase> event) {
            if (SkillHelper.isActiveFrom(event.getEntity(), ModAbilities.SACRIFICE)) {
                if (!ClientConfig.RENDER_CONFIG.rendering.helpMyShadersAreDying) {
                    ShaderLibrary.BLEED.end();
                }
            }
        }
    }
}
