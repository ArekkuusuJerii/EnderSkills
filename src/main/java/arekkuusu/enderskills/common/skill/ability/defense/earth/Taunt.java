package arekkuusu.enderskills.common.skill.ability.defense.earth;

import arekkuusu.enderskills.api.capability.AdvancementCapability;
import arekkuusu.enderskills.api.capability.Capabilities;
import arekkuusu.enderskills.api.capability.data.SkillData;
import arekkuusu.enderskills.api.capability.data.SkillInfo;
import arekkuusu.enderskills.api.capability.data.SkillInfo.IInfoCooldown;
import arekkuusu.enderskills.api.capability.data.SkillInfo.IInfoUpgradeable;
import arekkuusu.enderskills.api.event.SkillActionableEvent;
import arekkuusu.enderskills.api.helper.ExpressionHelper;
import arekkuusu.enderskills.api.helper.NBTHelper;
import arekkuusu.enderskills.api.registry.Skill;
import arekkuusu.enderskills.client.util.helper.TextHelper;
import arekkuusu.enderskills.common.CommonConfig;
import arekkuusu.enderskills.common.entity.data.IExpand;
import arekkuusu.enderskills.common.entity.data.IFindEntity;
import arekkuusu.enderskills.common.entity.data.IScanEntities;
import arekkuusu.enderskills.common.entity.placeable.EntityPlaceableData;
import arekkuusu.enderskills.common.lib.LibMod;
import arekkuusu.enderskills.common.lib.LibNames;
import arekkuusu.enderskills.common.skill.ModAbilities;
import arekkuusu.enderskills.common.skill.ModAttributes;
import arekkuusu.enderskills.common.skill.ModEffects;
import arekkuusu.enderskills.common.skill.SkillHelper;
import arekkuusu.enderskills.common.skill.ability.AbilityInfo;
import arekkuusu.enderskills.common.skill.ability.BaseAbility;
import arekkuusu.enderskills.common.sound.ModSounds;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.SoundCategory;
import net.minecraft.world.WorldServer;
import net.minecraftforge.client.event.InputUpdateEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.Config;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class Taunt extends BaseAbility implements IScanEntities, IExpand, IFindEntity {

    public static final UUID TAUNT_UUID = UUID.fromString("c0fef459-78da-47df-8c6c-62c95c2f5609");

    public Taunt() {
        super(LibNames.TAUNT, new AbilityProperties());
        ((AbilityProperties) getProperties()).setCooldownGetter(this::getCooldown).setMaxLevelGetter(this::getMaxLevel);
        MinecraftForge.EVENT_BUS.register(this);
    }

    @Override
    public void use(EntityLivingBase owner, SkillInfo skillInfo) {
        if (((IInfoCooldown) skillInfo).hasCooldown() || isClientWorld(owner)) return;
        AbilityInfo abilityInfo = (AbilityInfo) skillInfo;

        if (isActionable(owner) && canActivate(owner)) {
            if (!(owner instanceof EntityPlayer) || !((EntityPlayer) owner).capabilities.isCreativeMode) {
                abilityInfo.setCooldown(getCooldown(abilityInfo));
            }
            double range = getRange(abilityInfo);
            int time = getTime(abilityInfo);
            NBTTagCompound compound = new NBTTagCompound();
            NBTHelper.setEntity(compound, owner, "owner");
            SkillData data = SkillData.of(this)
                    .by(TAUNT_UUID)
                    .with(time)
                    .put(compound)
                    .overrides(SkillData.Overrides.ID)
                    .create();
            EntityPlaceableData spawn = new EntityPlaceableData(owner.world, owner, data, EntityPlaceableData.MIN_TIME);
            spawn.setPosition(owner.posX, owner.posY + owner.height / 2, owner.posZ);
            spawn.setRadius(range);
            owner.world.spawnEntity(spawn);
            sync(owner);

            if (owner.world instanceof WorldServer) {
                ((WorldServer) owner.world).playSound(null, owner.posX, owner.posY, owner.posZ, ModSounds.TAUNT, SoundCategory.PLAYERS, 5.0F, (1.0F + (owner.world.rand.nextFloat() - owner.world.rand.nextFloat()) * 0.2F) * 0.7F);
            }
        }
    }

    //* Entity *//
    @Override
    public void onFound(Entity source, @Nullable EntityLivingBase owner, EntityLivingBase target, SkillData skillData) {
        apply(target, skillData);
        sync(target, skillData);
    }
    //* Entity *//

    @Override
    public void update(EntityLivingBase target, SkillData data, int tick) {
        if (isClientWorld(target) && !(target instanceof EntityPlayer)) return;
        if (isStunnedByAbility(target)) return;
        Optional.ofNullable(SkillHelper.getOwner(data)).ifPresent(owner -> {
            if (target instanceof EntityLiving) {
                ((EntityLiving) target).getNavigator().clearPath();
                if (((EntityLiving) target).getAttackTarget() != owner)
                    ((EntityLiving) target).setAttackTarget(owner);
            }
            if (target.collidedHorizontally) {
                if (target.onGround) {
                    target.motionY = 0.4F;
                }
            }
            target.motionX += (Math.signum(owner.posX - target.posX) * 0.5D - target.motionX) * 0.100000000372529;
            target.motionZ += (Math.signum(owner.posZ - target.posZ) * 0.5D - target.motionZ) * 0.100000000372529;
            if (target.isRiding()) {
                target.dismountRidingEntity();
            }
        });
    }

    public boolean isStunnedByAbility(EntityLivingBase entity) {
        return Capabilities.get(entity).map(c -> c.isActive(ModEffects.STUNNED)).orElse(false);
    }

    @SubscribeEvent
    public void onSkillShouldUse(SkillActionableEvent event) {
        if (isClientWorld(event.getEntityLiving())) return;
        if (SkillHelper.isActive(event.getEntityLiving(), this)) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    @SideOnly(Side.CLIENT)
    public void inputListener(InputUpdateEvent event) {
        if (SkillHelper.isActive(event.getEntityLiving(), this)) {
            event.getMovementInput().forwardKeyDown = false;
            event.getMovementInput().rightKeyDown = false;
            event.getMovementInput().backKeyDown = false;
            event.getMovementInput().leftKeyDown = false;
            event.getMovementInput().sneak = false;
            event.getMovementInput().jump = false;
            event.getMovementInput().moveForward = 0;
            event.getMovementInput().moveStrafe = 0;
        }
    }

    public int getLevel(IInfoUpgradeable info) {
        return info.getLevel();
    }

    public int getMaxLevel() {
        return Configuration.getSyncValues().maxLevel;
    }

    public double getRange(AbilityInfo info) {
        int level = getLevel(info);
        int levelMax = getMaxLevel();
        double func = ExpressionHelper.getExpression(this, Configuration.getSyncValues().range, level, levelMax);
        double result = (func * CommonConfig.getSyncValues().skill.globalRange);
        return (result * getEffectiveness());
    }

    public int getCooldown(AbilityInfo info) {
        int level = getLevel(info);
        int levelMax = getMaxLevel();
        double func = ExpressionHelper.getExpression(this, Configuration.getSyncValues().cooldown, level, levelMax);
        double result = (func * CommonConfig.getSyncValues().skill.globalCooldown);
        return (int) (result * getEffectiveness());
    }

    public int getTime(AbilityInfo info) {
        int level = getLevel(info);
        int levelMax = getMaxLevel();
        double func = ExpressionHelper.getExpression(this, Configuration.getSyncValues().time, level, levelMax);
        double result = (func * CommonConfig.getSyncValues().skill.globalTime);
        return (int) (result * getEffectiveness());
    }

    public double getEffectiveness() {
        return Configuration.getSyncValues().effectiveness * CommonConfig.getSyncValues().skill.globalEffectiveness;
    }

    /*Advancement Section*/
    @Override
    @SideOnly(Side.CLIENT)
    public void addDescription(List<String> description) {
        Capabilities.get(Minecraft.getMinecraft().player).ifPresent(c -> {
            if (c.isOwned(this)) {
                if (!GuiScreen.isShiftKeyDown()) {
                    description.add("");
                    description.add(TextHelper.translate("desc.stats.shift"));
                } else {
                    c.getOwned(this).ifPresent(skillInfo -> {
                        AbilityInfo abilityInfo = (AbilityInfo) skillInfo;
                        description.clear();
                        description.add(TextHelper.translate("desc.stats.endurance", String.valueOf(ModAttributes.ENDURANCE.getEnduranceDrain(this))));
                        description.add("");
                        if (abilityInfo.getLevel() >= getMaxLevel()) {
                            description.add(TextHelper.translate("desc.stats.level_max"));
                        } else {
                            description.add(TextHelper.translate("desc.stats.level_current", abilityInfo.getLevel(), abilityInfo.getLevel() + 1));
                        }
                        description.add(TextHelper.translate("desc.stats.cooldown", TextHelper.format2FloatPoint(getCooldown(abilityInfo) / 20D), TextHelper.getTextComponent("desc.stats.suffix_time")));
                        description.add(TextHelper.translate("desc.stats.range", TextHelper.format2FloatPoint(getRange(abilityInfo)), TextHelper.getTextComponent("desc.stats.suffix_blocks")));
                        description.add(TextHelper.translate("desc.stats.duration", TextHelper.format2FloatPoint(getTime(abilityInfo) / 20D), TextHelper.getTextComponent("desc.stats.suffix_time")));
                        if (abilityInfo.getLevel() < getMaxLevel()) { //Copy info and set a higher level...
                            AbilityInfo infoNew = new AbilityInfo(abilityInfo.serializeNBT());
                            infoNew.setLevel(infoNew.getLevel() + 1);
                            description.add("");
                            description.add(TextHelper.translate("desc.stats.level_next", abilityInfo.getLevel(), infoNew.getLevel()));
                            description.add(TextHelper.translate("desc.stats.cooldown", TextHelper.format2FloatPoint(getCooldown(infoNew) / 20D), TextHelper.getTextComponent("desc.stats.suffix_time")));
                            description.add(TextHelper.translate("desc.stats.range", TextHelper.format2FloatPoint(getRange(infoNew)), TextHelper.getTextComponent("desc.stats.suffix_blocks")));
                            description.add(TextHelper.translate("desc.stats.duration", TextHelper.format2FloatPoint(getTime(infoNew) / 20D), TextHelper.getTextComponent("desc.stats.suffix_time")));
                        }
                    });
                }
            }
        });
    }

    @Override
    public void onUpgrade(EntityLivingBase entity) {
        Capabilities.advancement(entity).ifPresent(c -> {
            Requirement requirement = getRequirement(entity);
            int tokens = requirement.getLevels();
            int xp = requirement.getXp();
            if (c.level >= tokens && c.getExperienceTotal(entity) >= xp) {
                Capabilities.get(entity).filter(a -> !a.isOwned(this)).ifPresent(a -> {
                    Skill[] skillUnlockOrder = Arrays.copyOf(c.skillUnlockOrder, c.skillUnlockOrder.length + 1);
                    skillUnlockOrder[skillUnlockOrder.length - 1] = this;
                    c.skillUnlockOrder = skillUnlockOrder;
                });
                c.consumeExperienceFromTotal(entity, xp);
            }
        });
    }

    @Override
    public int getCostIncrement(EntityLivingBase entity, int total) {
        Optional<AdvancementCapability> optional = Capabilities.advancement(entity);
        if (optional.isPresent()) {
            AdvancementCapability advancement = optional.get();
            List<Skill> skillUnlockOrder = Arrays.asList(advancement.skillUnlockOrder);
            int index = skillUnlockOrder.indexOf(ModAbilities.TAUNT);
            if (index == -1) {
                index = advancement.skillUnlockOrder.length;
            }
            return (int) (total * (1D + index * CommonConfig.getSyncValues().advancement.xp.costIncrement));
        }
        return total;
    }

    @Override
    public int getUpgradeCost(@Nullable AbilityInfo info) {
        int level = info != null ? getLevel(info) + 1 : 0;
        int levelMax = getMaxLevel();
        double func = ExpressionHelper.getExpression(this, Configuration.getSyncValues().advancement.upgrade, level, levelMax);
        return (int) (func * CommonConfig.getSyncValues().advancement.xp.globalCostMultiplier);
    }
    /*Advancement Section*/

    @Override
    public void initSyncConfig() {
        Configuration.getSyncValues().maxLevel = Configuration.getValues().maxLevel;
        Configuration.getSyncValues().cooldown = Configuration.getValues().cooldown;
        Configuration.getSyncValues().time = Configuration.getValues().time;
        Configuration.getSyncValues().range = Configuration.getValues().range;
        Configuration.getSyncValues().effectiveness = Configuration.getValues().effectiveness;
        Configuration.getSyncValues().advancement.upgrade = Configuration.getValues().advancement.upgrade;
    }

    @Override
    public void writeSyncConfig(NBTTagCompound compound) {
        compound.setInteger("maxLevel", Configuration.getValues().maxLevel);
        NBTHelper.setArray(compound, "cooldown", Configuration.getValues().cooldown);
        NBTHelper.setArray(compound, "time", Configuration.getValues().time);
        NBTHelper.setArray(compound, "range", Configuration.getValues().range);
        compound.setDouble("effectiveness", Configuration.getValues().effectiveness);
        NBTHelper.setArray(compound, "advancement.upgrade", Configuration.getValues().advancement.upgrade);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void readSyncConfig(NBTTagCompound compound) {
        Configuration.getSyncValues().maxLevel = compound.getInteger("maxLevel");
        Configuration.getSyncValues().cooldown = NBTHelper.getArray(compound, "cooldown");
        Configuration.getSyncValues().time = NBTHelper.getArray(compound, "time");
        Configuration.getSyncValues().range = NBTHelper.getArray(compound, "range");
        Configuration.getSyncValues().effectiveness = compound.getDouble("effectiveness");
        Configuration.getSyncValues().advancement.upgrade = NBTHelper.getArray(compound, "advancement.upgrade");
    }

    @Config(modid = LibMod.MOD_ID, name = LibMod.MOD_ID + "/Ability/" + LibNames.TAUNT)
    public static class Configuration {

        @Config.Comment("Ability Values")
        @Config.LangKey(LibMod.MOD_ID + ".config." + LibNames.TAUNT)
        public static Values CONFIG = new Values();

        public static Values getValues() {
            return CONFIG;
        }

        @Config.Ignore
        protected static Values CONFIG_SYNC = new Values();

        public static Values getSyncValues() {
            return CONFIG_SYNC;
        }

        public static class Values {
            @Config.Comment("Skill specific Advancement Configuration")
            public final Advancement advancement = new Advancement();

            @Config.Comment("Max level obtainable")
            @Config.RangeInt(min = 0)
            public int maxLevel = 50;

            @Config.Comment("Cooldown Function f(x,y)=? where 'x' is [Current Level] and 'y' is [Max Level]")
            public String[] cooldown = {
                    "(0+){20 * 20 + 15 * 20 * (1 - ((1 - (e^(-2.1 * (x/24)))) / (1 - e^(-2.1))))}",
                    "(25+){12 * 20 + 8 * 20 * (1- (((e^(0.1 * ((x-24) / (y-24))) - 1)/((e^0.1) - 1))))}",
                    "(50){10 * 20}"
            };

            @Config.Comment("Duration Function f(x,y)=? where 'x' is [Current Level] and 'y' is [Max Level]")
            public String[] time = {
                    "(0+){10 * 20 + 12 * 20 * (1 - (e^(-2.1 * (x/24)))) / (1 - e^(-2.1))}",
                    "(25+){22 * 20 + 6 * 20 * ((e^(0.1 * ((x - 24) / (y - 24))) - 1)/((e^0.1) - 1))}",
                    "(50){30 * 20}"
            };

            @Config.Comment("Range Function f(x,y)=? where 'x' is [Current Level] and 'y' is [Max Level]")
            public String[] range = {
                    "(0+){8 + 16 * (1 - (e^(-2.1 * (x/24)))) / (1 - e^(-2.1))}",
                    "(25+){24 + 6 * ((e^(0.1 * ((x - 24) / (y - 24))) - 1)/((e^0.1) - 1))}"
            };

            @Config.Comment("Effectiveness Modifier")
            @Config.RangeDouble
            public double effectiveness = 1D;

            public static class Advancement {
                @Config.Comment("Function f(x)=? where 'x' is [Next Level] and 'y' is [Max Level], XP Cost is in units [NOT LEVELS]")
                public String[] upgrade = {
                        "(0){170}",
                        "(1+){4 * x}",
                        "(50){4 * x + 4 * x * 0.1}"
                };
            }
        }
    }
}
