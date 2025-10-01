package com.mrcrayfish.vehicle.entity.vehicle;

import com.mrcrayfish.vehicle.entity.LandVehicleEntity;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

/**
 * Author: MrCrayfish
 */
public class ShoppingCartEntity extends LandVehicleEntity
{
    private Player pusher;

    public ShoppingCartEntity(EntityType<? extends ShoppingCartEntity> type, Level worldIn)
    {
        super(type, worldIn);
        this.setMaxTurnAngle(90);
        this.setTurnSensitivity(15);
        this.setFuelCapacity(0F);
        this.setFuelConsumption(0F);
    }

    @Override
    public void tick()
    {
        if(this.pusher != null)
        {
            this.yRotO = this.getYRot();
            this.xo = this.getX();
            this.yo = this.getY();
            this.zo = this.getZ();
            float x = Mth.sin(-pusher.getYRot() * 0.017453292F) * 1.3F;
            float z = Mth.cos(-pusher.getYRot() * 0.017453292F) * 1.3F;
            this.setPos(pusher.getX() + x, pusher.getY(), pusher.getZ() + z);
            this.xOld = this.getX();
            this.yOld = this.getY();
            this.zOld = this.getZ();
            this.setYRot(pusher.getYRot());
        }
        else
        {
            super.tick();
        }
    }

    @Override
    public SoundEvent getEngineSound()
    {
        return null;
    }

    @Override
    public boolean isLockable()
    {
        return false;
    }

    @Override
    public boolean canBeColored()
    {
        return true;
    }
}
