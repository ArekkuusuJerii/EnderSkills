package arekkuusu.enderskills.api.capability;

import arekkuusu.enderskills.api.capability.data.IInfoCooldown;
import arekkuusu.enderskills.api.capability.data.SkillHolder;
import arekkuusu.enderskills.api.capability.data.SkillInfo;
import arekkuusu.enderskills.api.helper.NBTHelper;
import arekkuusu.enderskills.api.registry.Skill;
import arekkuusu.enderskills.common.lib.LibMod;
import arekkuusu.enderskills.common.skill.ModAbilities;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import it.unimi.dsi.fastutil.objects.Object2IntArrayMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.registries.IForgeRegistry;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;
import java.util.Map.Entry;
import java.util.function.Function;

@SuppressWarnings("ConstantConditions")
public class SkilledEntityCapability implements ICapabilitySerializable<NBTTagCompound>, Capability.IStorage<SkilledEntityCapability> {

    private Map<Skill, SkillInfo> skillPlayerInfoMap = Maps.newHashMap();
    private Object2IntMap<Skill> skillWeightMap = new Object2IntArrayMap<>();
    private List<SkillHolder> skillHolders = Lists.newLinkedList();

    {
        //Defense-Light
        int i = 0;
        putWeight(ModAbilities.CHARM, i + 1);
        putWeight(ModAbilities.HEAL_AURA, i + 2);
        putWeight(ModAbilities.POWER_BOOST, i + 3);
        putWeight(ModAbilities.HEAL_OTHER, i + 4);
        putWeight(ModAbilities.HEAL_SELF, i + 5);
        putWeight(ModAbilities.NEARBY_INVINCIBILITY, i + 6);
        //Defense-Earth
        i += 6;
        putWeight(ModAbilities.TAUNT, i + 1);
        putWeight(ModAbilities.WALL, i + 2);
        putWeight(ModAbilities.DOME, i + 3);
        putWeight(ModAbilities.THORNY, i + 4);
        putWeight(ModAbilities.SHOCKWAVE, i + 5);
        putWeight(ModAbilities.ANIMATED_STONE_GOLEM, i + 6);
        //Mobility-Wind
        i += 6;
        putWeight(ModAbilities.DASH, i + 1);
        putWeight(ModAbilities.EXTRA_JUMP, i + 2);
        putWeight(ModAbilities.FOG, i + 3);
        putWeight(ModAbilities.SMASH, i + 4);
        putWeight(ModAbilities.HASTEN, i + 5);
        putWeight(ModAbilities.SPEED_BOOST, i + 6);
        //Mobility-Void
        i += 6;
        putWeight(ModAbilities.WARP, i + 1);
        putWeight(ModAbilities.INVISIBILITY, i + 2);
        putWeight(ModAbilities.HOVER, i + 3);
        putWeight(ModAbilities.UNSTABLE_PORTAL, i + 4);
        putWeight(ModAbilities.PORTAL, i + 5);
        putWeight(ModAbilities.TELEPORT, i + 6);
        //Offense-Void
        i += 6;
        putWeight(ModAbilities.SHADOW, i + 1);
        putWeight(ModAbilities.GLOOM, i + 2);
        putWeight(ModAbilities.SHADOW_JAB, i + 3);
        putWeight(ModAbilities.GAS_CLOUD, i + 4);
        putWeight(ModAbilities.GRASP, i + 5);
        putWeight(ModAbilities.BLACK_HOLE, i + 6);
        //Offense-Blood
        i += 6;
        putWeight(ModAbilities.BLEED, i + 1);
        putWeight(ModAbilities.BLOOD_POOL, i + 2);
        putWeight(ModAbilities.CONTAMINATE, i + 3);
        putWeight(ModAbilities.LIFE_STEAL, i + 4);
        putWeight(ModAbilities.SYPHON, i + 5);
        putWeight(ModAbilities.SACRIFICE, i + 6);
        //Offense-Wind
        i += 6;
        putWeight(ModAbilities.SLASH, i + 1);
        putWeight(ModAbilities.PUSH, i + 2);
        putWeight(ModAbilities.PULL, i + 3);
        putWeight(ModAbilities.CRUSH, i + 4);
        putWeight(ModAbilities.UPDRAFT, i + 5);
        putWeight(ModAbilities.SUFFOCATE, i + 6);
        //Offense-Fire
        i += 6;
        putWeight(ModAbilities.FIRE_SPIRIT, i + 1);
        putWeight(ModAbilities.FLAMING_BREATH, i + 2);
        putWeight(ModAbilities.FLAMING_RAIN, i + 3);
        putWeight(ModAbilities.FOCUS_FLAME, i + 4);
        putWeight(ModAbilities.FIREBALL, i + 5);
        putWeight(ModAbilities.EXPLODE, i + 6);
    }

    /* Skill Info */
    public Map<Skill, SkillInfo> getAll() {
        return skillPlayerInfoMap;
    }

    public Optional<SkillInfo> get(Skill skill) {
        return Optional.ofNullable(skillPlayerInfoMap.get(skill));
    }

    public boolean owns(Skill skill) {
        return skillPlayerInfoMap.containsKey(skill);
    }

    public void add(Skill skill) {
        if (!owns(skill)) {
            skillPlayerInfoMap.put(skill, skill.createInfo(new NBTTagCompound()));
        }
    }

    public void remove(Skill skill) {
        skillPlayerInfoMap.remove(skill);
    }

    public void clear() {
        skillPlayerInfoMap.clear();
    }
    /* Skill Info */

    /* Skill Holders */
    public List<SkillHolder> getActives() {
        return skillHolders;
    }

    public Optional<SkillHolder> getActive(Skill skill) {
        return skillHolders.stream().filter(h -> h.data.skill == skill).findFirst();
    }

    public boolean isActive(Skill skill) {
        return skillHolders.stream().anyMatch(h -> h.data.skill == skill);
    }

    public void activate(SkillHolder holder) {
        if (holder.data.overrides.length > 0) {
            for (SkillHolder skillHolder : skillHolders) {
                boolean remove = Arrays.stream(holder.data.overrides).anyMatch(s -> s == skillHolder.data.skill);
                if (remove) {
                    skillHolder.setDead();
                }
            }
        }
        skillHolders.add(holder);
    }

    public void deactivate(Skill skill) {
        for (SkillHolder skillHolder : skillHolders) {
            if (skillHolder.data.skill == skill) {
                skillHolder.setDead();
            }
        }
    }

    public void deactivate(Skill skill, Function<SkillHolder, Boolean> function) {
        for (SkillHolder skillHolder : skillHolders) {
            if (skillHolder.data.skill == skill && function.apply(skillHolder)) {
                skillHolder.setDead();
            }
        }
    }
    /* Skill Holders */

    /* Skill Weights */
    public boolean hasWeight(Skill skill) {
        return skillWeightMap.containsKey(skill);
    }

    public void putWeight(Skill skill, int weight) {
        skillWeightMap.put(skill, weight);
    }

    public int getWeight(Skill skill) {
        return skillWeightMap.getOrDefault(skill, Integer.MAX_VALUE);
    }
    /* Skill Weights */

    @Deprecated
    public void tick(EntityLivingBase entity) {
        //Iterate Cooldowns
        for (Map.Entry<Skill, SkillInfo> entry : getAll().entrySet()) {
            SkillInfo skillInfo = entry.getValue();
            if (skillInfo instanceof IInfoCooldown && ((IInfoCooldown) skillInfo).hasCooldown()) {
                ((IInfoCooldown) skillInfo).setCooldown(((IInfoCooldown) skillInfo).getCooldown() - 1);
            }
        }
        //Iterate Entity-level SkillHolders
        Iterator<SkillHolder> iterator = getActives().iterator();
        while (iterator.hasNext()) {
            SkillHolder holder = iterator.next();
            holder.tick(entity);
            if (holder.isDead()) iterator.remove();
        }
    }

    public static void init() {
        CapabilityManager.INSTANCE.register(SkilledEntityCapability.class, new SkilledEntityCapability(), SkilledEntityCapability::new);
        MinecraftForge.EVENT_BUS.register(new Handler());
    }

    @Override
    public boolean hasCapability(@Nonnull Capability<?> capability, @Nullable EnumFacing facing) {
        return getCapability(capability, facing) != null;
    }

    @Override
    @Nullable
    public <T> T getCapability(@Nonnull Capability<T> capability, @Nullable EnumFacing facing) {
        return capability == Capabilities.SKILLED_ENTITY ? Capabilities.SKILLED_ENTITY.cast(this) : null;
    }

    @Override
    public NBTTagCompound serializeNBT() {
        return (NBTTagCompound) Capabilities.SKILLED_ENTITY.getStorage().writeNBT(Capabilities.SKILLED_ENTITY, this, null);
    }

    @Override
    public void deserializeNBT(NBTTagCompound nbt) {
        Capabilities.SKILLED_ENTITY.getStorage().readNBT(Capabilities.SKILLED_ENTITY, this, null, nbt);
    }

    //** NBT **//
    public static final String SKILL_LIST_NBT = "skill_list";
    public static final String HOLDER_LIST_NBT = "holder_list";
    public static final String WEIGHT_LIST_NBT = "weight_info";
    public static final String SKILL_NBT = "skill";
    public static final String SKILL_INFO_NBT = "skill_info";
    public static final String SKILL_WEIGHT_NBT = "skill_weight";

    @Override
    @Nullable
    public NBTBase writeNBT(Capability<SkilledEntityCapability> capability, SkilledEntityCapability instance, EnumFacing side) {
        NBTTagCompound tag = new NBTTagCompound();
        NBTTagList attributeList = new NBTTagList();
        NBTTagList skillHolderList = new NBTTagList();
        NBTTagList skillWeightList = new NBTTagList();
        //Write all Skills
        for (Entry<Skill, SkillInfo> set : instance.skillPlayerInfoMap.entrySet()) {
            NBTTagCompound compound = new NBTTagCompound();
            NBTHelper.setResourceLocation(compound, SKILL_NBT, set.getKey().getRegistryName());
            compound.setTag(SKILL_INFO_NBT, set.getValue().serializeNBT());
            attributeList.appendTag(compound);
        }
        //Write all Holders
        for (SkillHolder skillHolder : instance.skillHolders) {
            NBTTagCompound compound = skillHolder.serializeNBT();
            skillHolderList.appendTag(compound);
        }
        //Write all Weights
        for (Entry<Skill, Integer> set : instance.skillWeightMap.entrySet()) {
            NBTTagCompound compound = new NBTTagCompound();
            NBTHelper.setResourceLocation(compound, SKILL_NBT, set.getKey().getRegistryName());
            compound.setInteger(SKILL_WEIGHT_NBT, set.getValue());
            skillWeightList.appendTag(compound);
        }
        //Write tags
        tag.setTag(SKILL_LIST_NBT, attributeList);
        tag.setTag(HOLDER_LIST_NBT, skillHolderList);
        tag.setTag(WEIGHT_LIST_NBT, skillWeightList);
        return tag;
    }

    @Override
    public void readNBT(Capability<SkilledEntityCapability> capability, SkilledEntityCapability instance, EnumFacing side, NBTBase nbt) {
        instance.skillPlayerInfoMap.clear();
        instance.skillHolders.clear();
        instance.skillWeightMap.clear();
        NBTTagCompound tag = (NBTTagCompound) nbt;
        NBTTagList attributeList = tag.getTagList(SKILL_LIST_NBT, Constants.NBT.TAG_COMPOUND);
        NBTTagList skillHolderList = tag.getTagList(HOLDER_LIST_NBT, Constants.NBT.TAG_COMPOUND);
        NBTTagList skillWeightList = tag.getTagList(WEIGHT_LIST_NBT, Constants.NBT.TAG_COMPOUND);
        IForgeRegistry<Skill> registry = GameRegistry.findRegistry(Skill.class);
        //Read and add all Skills
        for (int i = 0; i < attributeList.tagCount(); i++) {
            NBTTagCompound compound = attributeList.getCompoundTagAt(i);
            ResourceLocation location = NBTHelper.getResourceLocation(compound, SKILL_NBT);
            Skill skill = registry.getValue(location);
            SkillInfo info = skill.createInfo(compound.getCompoundTag(SKILL_INFO_NBT));
            instance.skillPlayerInfoMap.put(skill, info);
        }
        //Read and add all Holders
        for (int i = 0; i < skillHolderList.tagCount(); i++) {
            NBTTagCompound compound = skillHolderList.getCompoundTagAt(i);
            instance.skillHolders.add(new SkillHolder(compound));
        }
        //Read and add all Weights
        for (int i = 0; i < skillWeightList.tagCount(); i++) {
            NBTTagCompound compound = skillWeightList.getCompoundTagAt(i);
            ResourceLocation location = NBTHelper.getResourceLocation(compound, SKILL_NBT);
            Skill skill = registry.getValue(location);
            instance.skillWeightMap.put(skill, compound.getInteger(SKILL_WEIGHT_NBT));
        }
    }

    public static class Handler {
        private static final ResourceLocation KEY = new ResourceLocation(LibMod.MOD_ID, "skilled_entity");

        @SubscribeEvent
        public void attachCapabilities(AttachCapabilitiesEvent<Entity> event) {
            if (event.getObject() instanceof EntityLivingBase)
                event.addCapability(KEY, Capabilities.SKILLED_ENTITY.getDefaultInstance());
        }

        @SubscribeEvent
        public void clonePlayer(PlayerEvent.Clone event) {
            event.getEntityPlayer().getCapability(Capabilities.SKILLED_ENTITY, null)
                    .deserializeNBT(event.getOriginal().getCapability(Capabilities.SKILLED_ENTITY, null).serializeNBT());
        }
    }
}