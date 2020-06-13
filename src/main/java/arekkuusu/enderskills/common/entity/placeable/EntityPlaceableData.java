package arekkuusu.enderskills.common.entity.placeable;

import arekkuusu.enderskills.api.capability.data.SkillData;
import arekkuusu.enderskills.common.entity.data.*;
import com.google.common.base.Optional;
import com.google.common.collect.Sets;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.server.management.PreYggdrasilConverter;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class EntityPlaceableData extends Entity {

    public static final DataParameter<Optional<UUID>> OWNER_ID = EntityDataManager.createKey(EntityPlaceableData.class, DataSerializers.OPTIONAL_UNIQUE_ID);
    public static final DataParameter<SkillExtendedData> DATA = EntityDataManager.createKey(EntityPlaceableData.class, SkillExtendedData.SERIALIZER);
    public static final DataParameter<Integer> LIFE_TIME = EntityDataManager.createKey(EntityPlaceableData.class, DataSerializers.VARINT);
    public static final DataParameter<Float> SIZE = EntityDataManager.createKey(EntityPlaceableData.class, DataSerializers.FLOAT);
    public static final int MIN_TIME = 10;
    public Set<Entity> affectedEntities = Sets.newHashSet();
    public int growTicks = MIN_TIME;
    public int tick;

    public EntityPlaceableData(World worldIn) {
        super(worldIn);
        this.noClip = true;
        setSize(0F, 0F);
    }

    public EntityPlaceableData(World worldIn, @Nullable EntityLivingBase owner, SkillData skillData, int lifeTime) {
        this(worldIn);
        if (owner != null) {
            this.rotationPitch = owner.rotationPitch;
            this.rotationYaw = owner.rotationYaw;
        }
        setOwner(owner);
        setData(skillData);
        setLifeTime(lifeTime);
    }

    @Override
    protected void entityInit() {
        this.dataManager.register(OWNER_ID, Optional.absent());
        this.dataManager.register(DATA, new SkillExtendedData(null));
        this.dataManager.register(LIFE_TIME, 0);
        this.dataManager.register(SIZE, 1F);
    }

    @Override
    public void onUpdate() {
        if (firstUpdate && world.isRemote) {
            if (getData().skill instanceof ILoopSound) {
                ((ILoopSound) getData().skill).makeSound(this);
            }
        }
        super.onUpdate();
        SkillData data = getData();
        if (!world.isRemote) {
            if (this.getLifeTime() > this.tick) {
                if (data.skill instanceof IScanEntities) {
                    EntityLivingBase owner = getOwner();
                    List<Entity> entities = ((IScanEntities) data.skill).getScan(this, owner, data.copy(), getRadius() * ((float) tick / (float) getLifeTime()));
                    if (!entities.isEmpty()) {
                        for (Entity entity : entities) {
                            if (entity instanceof EntityLivingBase) {
                                if (affectedEntities.add(entity)) {
                                    if (data.skill instanceof IFindEntity) {
                                        ((IFindEntity) data.skill).onFound(this, owner, (EntityLivingBase) entity, data.copy());
                                    }
                                }
                                if (data.skill instanceof IScanEntities) {
                                    ((IScanEntities) data.skill).onScan(this, owner, (EntityLivingBase) entity, data.copy());
                                }
                            }
                        }
                    }
                }
            } else {
                setDead();
            }
        }
        if (growTicks > this.tick && data.skill instanceof IExpand) {
            setEntityBoundingBox(((IExpand) data.skill).expand(this, getOwner(), getEntityBoundingBox(), getRadius() / (float) growTicks));
        }
        this.tick++;
    }

    @Override
    public void setEntityBoundingBox(AxisAlignedBB bb) {
        super.setEntityBoundingBox(bb);
        this.width = (float) Math.max(Math.abs(bb.maxX - bb.minX), Math.abs(bb.maxZ - bb.minZ));
        this.height = (float) Math.abs(bb.maxY - bb.minY);
    }

    @Override
    protected void setSize(float width, float height) {
        AxisAlignedBB axisalignedbb = this.getEntityBoundingBox();
        double w = width / 2D;
        double h = height / 2D;
        this.width = width;
        this.height = height;
        setEntityBoundingBox(new AxisAlignedBB(axisalignedbb.minX - w, axisalignedbb.minY - h, axisalignedbb.minZ - w, axisalignedbb.minX + w, axisalignedbb.minY + h, axisalignedbb.minZ + w));
    }

    @Override
    public void setPosition(double x, double y, double z) {
        this.posX = x;
        this.posY = y;
        this.posZ = z;
        if (this.isAddedToWorld() && !this.world.isRemote)
            this.world.updateEntityWithOptionalForce(this, false); // Forge - Process chunk registration after moving.
        float f = this.width / 2F;
        float f1 = this.height / 2F;
        this.setEntityBoundingBox(new AxisAlignedBB(x - (double) f, y - f1, z - (double) f, x + (double) f, y + (double) f1, z + (double) f));
    }

    @Override
    public void resetPositionToBB() {
        AxisAlignedBB axisalignedbb = this.getEntityBoundingBox();
        this.posX = (axisalignedbb.minX + axisalignedbb.maxX) / 2.0D;
        this.posY = (axisalignedbb.minY + axisalignedbb.maxY) / 2.0D;
        this.posZ = (axisalignedbb.minZ + axisalignedbb.maxZ) / 2.0D;
        if (this.isAddedToWorld() && !this.world.isRemote)
            this.world.updateEntityWithOptionalForce(this, false); // Forge - Process chunk registration after moving.
    }

    @Override
    public boolean shouldRenderInPass(int pass) {
        return pass == 0 || pass == 1;
    }

    @Override
    public boolean canRenderOnFire() {
        return false;
    }

    @Override
    public float getEyeHeight() {
        return 0F;
    }

    public void setLifeTime(int age) {
        dataManager.set(LIFE_TIME, age);
    }

    public int getLifeTime() {
        return dataManager.get(LIFE_TIME);
    }

    public void setRadius(double size) {
        this.dataManager.set(SIZE, (float) size);
    }

    public float getRadius() {
        return this.dataManager.get(SIZE);
    }

    public void setData(SkillData data) {
        this.dataManager.set(DATA, new SkillExtendedData(data));
    }

    public SkillData getData() {
        return this.dataManager.get(DATA).data;
    }

    @SuppressWarnings("Guava")
    public void setOwner(@Nullable EntityLivingBase owner) {
        this.dataManager.set(OWNER_ID, owner != null ? Optional.of(owner.getUniqueID()) : Optional.absent());
    }

    @Nullable
    public EntityLivingBase getOwner() {
        EntityLivingBase owner = null;
        if (this.dataManager.get(OWNER_ID).isPresent()) {
            UUID uuid = this.dataManager.get(OWNER_ID).get();
            owner = this.world.getPlayerEntityByUUID(uuid);

            if (owner == null && this.world instanceof WorldServer) {
                Entity entity = ((WorldServer) this.world).getEntityFromUuid(uuid);

                if (entity instanceof EntityLivingBase) {
                    owner = (EntityLivingBase) entity;
                }
            }
        }

        return owner;
    }

    @Override
    @SuppressWarnings({"Guava", "ConstantConditions"})
    protected void readEntityFromNBT(NBTTagCompound compound) {
        String uuidString;
        if (compound.hasKey("OwnerUUID", 8)) {
            uuidString = compound.getString("OwnerUUID");
        } else {
            String ownerString = compound.getString("Owner");
            uuidString = PreYggdrasilConverter.convertMobOwnerIfNeeded(this.getServer(), ownerString);
        }
        if (!uuidString.isEmpty()) {
            this.dataManager.set(OWNER_ID, Optional.of(UUID.fromString(uuidString)));
        }
        if (compound.hasKey("data")) {
            setData(new SkillData(compound.getCompoundTag("data")));
        }
        setLifeTime(compound.getInteger("lifeTime"));
        setRadius(compound.getDouble("radius"));
        growTicks = compound.getInteger("growTicks");
        tick = compound.getInteger("tick");
    }

    @Override
    protected void writeEntityToNBT(NBTTagCompound compound) {
        if (!this.dataManager.get(OWNER_ID).isPresent()) {
            compound.setString("OwnerUUID", "");
        } else {
            compound.setString("OwnerUUID", this.dataManager.get(OWNER_ID).get().toString());
        }
        if (getData() != null) {
            compound.setTag("data", getData().serializeNBT());
        }
        compound.setInteger("lifeTime", getLifeTime());
        compound.setDouble("radius", getRadius());
        compound.setInteger("growTicks", growTicks);
        compound.setInteger("tick", tick);
    }
}
