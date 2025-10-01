package com.mrcrayfish.vehicle.network.message;

import com.mrcrayfish.vehicle.entity.LandVehicleEntity;
import com.mrcrayfish.vehicle.entity.TrailerEntity;
import com.mrcrayfish.vehicle.entity.VehicleEntity;
import com.mrcrayfish.vehicle.entity.VehicleProperties;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.NetworkEvent.Context;

import java.util.List;
import java.util.function.Supplier;

/**
 * Author: MrCrayfish
 */
public class MessageHitchTrailer implements IMessage<MessageHitchTrailer>
{
    private boolean hitch;

    public MessageHitchTrailer() {}

    public MessageHitchTrailer(boolean hitch)
    {
        this.hitch = hitch;
    }

    @Override
    public void encode(MessageHitchTrailer message, FriendlyByteBuf buffer)
    {
        buffer.writeBoolean(message.hitch);
    }

    @Override
    public MessageHitchTrailer decode(FriendlyByteBuf buffer)
    {
        return new MessageHitchTrailer(buffer.readBoolean());
    }

    @Override
    public void handle(MessageHitchTrailer message, Supplier<Context> supplier)
    {
        supplier.get().enqueueWork(() ->
        {
            ServerPlayer player = supplier.get().getSender();
            if(player != null)
            {
                if(!(player.getVehicle() instanceof VehicleEntity))
                    return;

                VehicleEntity vehicle = (VehicleEntity) player.getVehicle();
                if(!vehicle.canTowTrailer())
                    return;

                if(!message.hitch)
                {
                    if(vehicle.getTrailer() != null)
                    {
                        vehicle.setTrailer(null);
                        player.level().playSound(null, vehicle.blockPosition(), SoundEvents.ITEM_BREAK, SoundSource.PLAYERS, 1.0F, 1.0F);
                    }
                }
                else
                {
                    VehicleProperties properties = vehicle.getProperties();
                    Vec3 vehicleVec = vehicle.position();
                    Vec3 towBarVec = properties.getTowBarPosition();
                    towBarVec = new Vec3(towBarVec.x * 0.0625, towBarVec.y * 0.0625, towBarVec.z * 0.0625 + properties.getBodyPosition().getZ());
                    if(vehicle instanceof LandVehicleEntity)
                    {
                        LandVehicleEntity landVehicle = (LandVehicleEntity) vehicle;
                        vehicleVec = vehicleVec.add(towBarVec.yRot((float) Math.toRadians(-vehicle.getYRot() + landVehicle.additionalYaw)));
                    }
                    else
                    {
                        vehicleVec = vehicleVec.add(towBarVec.yRot((float) Math.toRadians(-vehicle.getYRot())));
                    }

                    AABB towBarBox = new AABB(vehicleVec.x, vehicleVec.y, vehicleVec.z, vehicleVec.x, vehicleVec.y, vehicleVec.z).inflate(0.25);
                    List<TrailerEntity> trailers = player.level().getEntitiesOfClass(TrailerEntity.class, vehicle.getBoundingBox().inflate(5), input -> input.getPullingEntity() == null);
                    for(TrailerEntity trailer : trailers)
                    {
                        if(trailer.getPullingEntity() != null)
                            continue;

                        Vec3 trailerVec = trailer.position();
                        Vec3 hitchVec = new Vec3(0, 0, -trailer.getHitchOffset() / 16.0);
                        trailerVec = trailerVec.add(hitchVec.yRot((float) Math.toRadians(-trailer.getYRot())));
                        AABB hitchBox = new AABB(trailerVec.x, trailerVec.y, trailerVec.z, trailerVec.x, trailerVec.y, trailerVec.z).inflate(0.25);
                        if(towBarBox.intersects(hitchBox))
                        {
                            vehicle.setTrailer(trailer);
                            player.level().playSound(null, vehicle.blockPosition(), SoundEvents.ANVIL_PLACE, SoundSource.PLAYERS, 1.0F, 1.5F);
                            return;
                        }
                    }
                }
            }
        });
        supplier.get().setPacketHandled(true);
    }
}
