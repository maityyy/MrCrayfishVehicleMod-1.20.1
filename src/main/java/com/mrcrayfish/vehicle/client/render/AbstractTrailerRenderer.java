package com.mrcrayfish.vehicle.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.mrcrayfish.vehicle.entity.TrailerEntity;
import com.mrcrayfish.vehicle.entity.VehicleProperties;
import com.mrcrayfish.vehicle.init.ModItems;
import com.mrcrayfish.vehicle.util.RenderUtil;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nullable;

/**
 * Author: MrCrayfish
 */
public abstract class AbstractTrailerRenderer<T extends TrailerEntity> extends AbstractVehicleRenderer<T>
{
    public AbstractTrailerRenderer(VehicleProperties defaultProperties)
    {
        super(defaultProperties);
    }

    //TODO Eventually converted to the wheel system. Consider it a pulled vehicle rather than powered
    protected void renderWheel(@Nullable T vehicle, PoseStack matrixStack, MultiBufferSource renderTypeBuffer, boolean right, float offsetX, float offsetY, float offsetZ, float wheelScale, float partialTicks, int light)
    {
        matrixStack.pushPose();
        matrixStack.translate(offsetX, offsetY, offsetZ);
        if(right)
        {
            matrixStack.mulPose(Axis.YP.rotationDegrees(180F));
        }
        if(vehicle != null)
        {
            float wheelRotation = vehicle.prevWheelRotation + (vehicle.wheelRotation - vehicle.prevWheelRotation) * partialTicks;
            matrixStack.mulPose(Axis.XP.rotationDegrees(right ? wheelRotation : -wheelRotation));
        }
        matrixStack.scale(wheelScale, wheelScale, wheelScale);
        RenderUtil.renderColoredModel(RenderUtil.getModel(new ItemStack(ModItems.STANDARD_WHEEL.get())), ItemDisplayContext.NONE, false, matrixStack, renderTypeBuffer, -1, light, OverlayTexture.NO_OVERLAY);
        matrixStack.popPose();
    }
}
