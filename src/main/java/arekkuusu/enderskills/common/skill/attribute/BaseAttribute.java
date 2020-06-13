package arekkuusu.enderskills.common.skill.attribute;

import arekkuusu.enderskills.api.capability.data.SkillInfo;
import arekkuusu.enderskills.api.registry.Skill;
import arekkuusu.enderskills.common.skill.ModAttributes;
import net.minecraft.nbt.NBTTagCompound;

import javax.annotation.Nonnull;

public abstract class BaseAttribute extends Skill {

    public BaseAttribute(String id) {
        ModAttributes.setRegistry(this, id);
    }

    @Override
    public boolean hasStatusIcon() {
        return false;
    }

    @Nonnull
    @Override
    public SkillInfo createInfo(NBTTagCompound compound) {
        return new AttributeInfo(compound);
    }
}
