package arekkuusu.enderskills.api.event;

import arekkuusu.enderskills.api.registry.Skill;
import net.minecraft.entity.EntityLivingBase;
import net.minecraftforge.fml.common.eventhandler.Event;

public class SkillActionableEvent extends Event {

    private final EntityLivingBase entity;
    private final Skill skill;

    public SkillActionableEvent(EntityLivingBase entity, Skill skill) {
        this.entity = entity;
        this.skill = skill;
    }

    public EntityLivingBase getEntityLiving() {
        return entity;
    }

    public Skill getSkill() {
        return skill;
    }

    @Override
    public boolean isCancelable() {
        return true;
    }
}
