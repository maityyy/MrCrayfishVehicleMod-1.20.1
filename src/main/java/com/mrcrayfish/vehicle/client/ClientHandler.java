package com.mrcrayfish.vehicle.client;

import com.mrcrayfish.vehicle.client.handler.*;
import com.mrcrayfish.vehicle.client.model.SpecialModels;
import com.mrcrayfish.vehicle.client.render.AbstractVehicleRenderer;
import com.mrcrayfish.vehicle.client.render.EntityVehicleRenderer;
import com.mrcrayfish.vehicle.client.render.JackRenderer;
import com.mrcrayfish.vehicle.client.render.VehicleRenderRegistry;
import com.mrcrayfish.vehicle.client.render.tileentity.*;
import com.mrcrayfish.vehicle.client.render.vehicle.*;
import com.mrcrayfish.vehicle.client.screen.*;
import com.mrcrayfish.vehicle.entity.VehicleEntity;
import com.mrcrayfish.vehicle.entity.VehicleProperties;
import com.mrcrayfish.vehicle.init.*;
import com.mrcrayfish.vehicle.item.PartItem;
import com.mrcrayfish.vehicle.item.SprayCanItem;
import com.mrcrayfish.vehicle.util.FluidUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.color.item.ItemColor;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.nbt.Tag;
import net.minecraft.server.packs.resources.ReloadableResourceManager;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.Unit;
import net.minecraft.world.entity.EntityType;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.function.Function;

/**
 * Author: MrCrayfish
 */
public class ClientHandler
{
    private static boolean controllableLoaded = false;

    public static boolean isControllableLoaded()
    {
        return controllableLoaded;
    }

    public static void setup()
    {
        if(ModList.get().isLoaded("controllable"))
        {
            ClientHandler.controllableLoaded = true;
            MinecraftForge.EVENT_BUS.register(new ControllerHandler());
        }

        MinecraftForge.EVENT_BUS.register(EntityRayTracer.instance());
        MinecraftForge.EVENT_BUS.register(new CameraHandler());
        MinecraftForge.EVENT_BUS.register(new FuelingHandler());
        MinecraftForge.EVENT_BUS.register(new HeldVehicleHandler());
        MinecraftForge.EVENT_BUS.register(new InputHandler());
        MinecraftForge.EVENT_BUS.register(new OverlayHandler());
        MinecraftForge.EVENT_BUS.register(new PlayerModelHandler());
        MinecraftForge.EVENT_BUS.register(new SprayCanHandler());
        MinecraftForge.EVENT_BUS.register(new ClientEvents());

        setupCustomBlockModels();
        setupRenderLayers();
        setupScreenFactories();
        setupItemColors();

        ResourceManager manager = Minecraft.getInstance().getResourceManager();
        if(manager instanceof ReloadableResourceManager)
        {
            ((ReloadableResourceManager) manager).registerReloadListener((stage, resourceManager, preparationsProfiler, reloadProfiler, backgroundExecutor, gameExecutor) -> {
                return stage.wait(Unit.INSTANCE).thenRun(() -> {
                    FluidUtils.clearCacheFluidColor();
                    EntityRayTracer.instance().clearDataForReregistration();
                    SpecialModels.clearModelCache();
                });
            });
        }
    }

    private static void setupCustomBlockModels()
    {
        //TODO add custom loader
        //ModelLoaderRegistry.registerLoader(new CustomLoader());
        //ModelLoaderRegistry.registerLoader(new ResourceLocation(Reference.MOD_ID, "ramp"), new CustomLoader());
    }

    private static void setupRenderLayers()
    {
        ItemBlockRenderTypes.setRenderLayer(ModBlocks.WORKSTATION.get(), RenderType.cutout());
        ItemBlockRenderTypes.setRenderLayer(ModBlocks.FLUID_EXTRACTOR.get(), RenderType.cutout());
        ItemBlockRenderTypes.setRenderLayer(ModBlocks.GAS_PUMP.get(), RenderType.cutout());
        ItemBlockRenderTypes.setRenderLayer(ModFluids.FUELIUM.get(), RenderType.translucent());
        ItemBlockRenderTypes.setRenderLayer(ModFluids.FLOWING_FUELIUM.get(), RenderType.translucent());
        ItemBlockRenderTypes.setRenderLayer(ModFluids.ENDER_SAP.get(), RenderType.translucent());
        ItemBlockRenderTypes.setRenderLayer(ModFluids.FLOWING_ENDER_SAP.get(), RenderType.translucent());
        ItemBlockRenderTypes.setRenderLayer(ModFluids.BLAZE_JUICE.get(), RenderType.translucent());
        ItemBlockRenderTypes.setRenderLayer(ModFluids.FLOWING_BLAZE_JUICE.get(), RenderType.translucent());
        ItemBlockRenderTypes.setRenderLayer(ModBlocks.FUEL_DRUM.get(), RenderType.cutout());
        ItemBlockRenderTypes.setRenderLayer(ModBlocks.INDUSTRIAL_FUEL_DRUM.get(), RenderType.cutout());
        ItemBlockRenderTypes.setRenderLayer(ModBlocks.TRAFFIC_CONE.get(), RenderType.cutout());
    }

    @SubscribeEvent
    public static void registerSomeRenderers(EntityRenderersEvent.RegisterRenderers event)
    {
        event.registerBlockEntityRenderer(ModTileEntities.FLUID_EXTRACTOR.get(), FluidExtractorRenderer::new);
        event.registerBlockEntityRenderer(ModTileEntities.FUEL_DRUM.get(), FuelDrumRenderer::new);
        event.registerBlockEntityRenderer(ModTileEntities.INDUSTRIAL_FUEL_DRUM.get(), FuelDrumRenderer::new);
        event.registerBlockEntityRenderer(ModTileEntities.VEHICLE_CRATE.get(), VehicleCrateRenderer::new);
        event.registerBlockEntityRenderer(ModTileEntities.JACK.get(), JackBlockRenderer::new);
        event.registerBlockEntityRenderer(ModTileEntities.GAS_PUMP.get(), GasPumpRenderer::new);
        event.registerBlockEntityRenderer(ModTileEntities.GAS_PUMP_TANK.get(), GasPumpTankRenderer::new);
        event.registerBlockEntityRenderer(ModTileEntities.FLUID_PUMP.get(), FluidPumpRenderer::new);

        event.registerEntityRenderer(ModEntities.JACK.get(), JackRenderer::new);

        /* Register Vehicles */
        registerVehicleRenderer(event, ModEntities.ATV.get(), ATVRenderer::new);
        registerVehicleRenderer(event, ModEntities.DUNE_BUGGY.get(), DuneBuggyRenderer::new);
        registerVehicleRenderer(event, ModEntities.GO_KART.get(), GoKartRenderer::new);
        registerVehicleRenderer(event, ModEntities.SHOPPING_CART.get(), ShoppingCartRenderer::new);
        registerVehicleRenderer(event, ModEntities.MINI_BIKE.get(), MiniBikeRenderer::new);
        registerVehicleRenderer(event, ModEntities.BUMPER_CAR.get(), BumperCarModel::new);
        registerVehicleRenderer(event, ModEntities.JET_SKI.get(), JetSkiRenderer::new);
        registerVehicleRenderer(event, ModEntities.SPEED_BOAT.get(), SpeedBoatRenderer::new);
        registerVehicleRenderer(event, ModEntities.ALUMINUM_BOAT.get(), AluminumBoatRenderer::new);
        registerVehicleRenderer(event, ModEntities.SMART_CAR.get(), SmartCarRenderer::new);
        registerVehicleRenderer(event, ModEntities.LAWN_MOWER.get(), LawnMowerRenderer::new);
        registerVehicleRenderer(event, ModEntities.MOPED.get(), MopedRenderer::new);
        registerVehicleRenderer(event, ModEntities.SPORTS_PLANE.get(), SportsPlaneRenderer::new);
        registerVehicleRenderer(event, ModEntities.GOLF_CART.get(), GolfCartRenderer::new);
        registerVehicleRenderer(event, ModEntities.OFF_ROADER.get(), OffRoaderRenderer::new);
        registerVehicleRenderer(event, ModEntities.TRACTOR.get(), TractorRenderer::new);
        registerVehicleRenderer(event, ModEntities.MINI_BUS.get(), MiniBusRenderer::new);
        registerVehicleRenderer(event, ModEntities.DIRT_BIKE.get(), DirtBikeRenderer::new);

        /* Register Trailers */
        registerVehicleRenderer(event, ModEntities.VEHICLE_TRAILER.get(), VehicleTrailerRenderer::new);
        registerVehicleRenderer(event, ModEntities.STORAGE_TRAILER.get(), StorageTrailerRenderer::new);
        registerVehicleRenderer(event, ModEntities.FLUID_TRAILER.get(), FluidTrailerRenderer::new);
        registerVehicleRenderer(event, ModEntities.SEEDER.get(), SeederTrailerRenderer::new);
        registerVehicleRenderer(event, ModEntities.FERTILIZER.get(), FertilizerTrailerRenderer::new);

        /* Register Mod Exclusive Vehicles */
        if(ModList.get().isLoaded("cfm"))
        {
            registerVehicleRenderer(event, ModEntities.SOFA.get(), SofaCarRenderer::new);
            registerVehicleRenderer(event, ModEntities.BATH.get(), BathModel::new);
            registerVehicleRenderer(event, ModEntities.SOFACOPTER.get(), SofaHelicopterRenderer::new);
        }
    }

    @SuppressWarnings("unchecked")
    private static <T extends VehicleEntity & EntityRayTracer.IEntityRayTraceable> void registerVehicleRenderer(EntityRenderersEvent.RegisterRenderers event, EntityType<T> type, Function<VehicleProperties, AbstractVehicleRenderer<T>> rendererFunction)
    {
        VehicleProperties properties = VehicleProperties.get(type);
        AbstractVehicleRenderer<T> renderer = rendererFunction.apply(properties);
        event.registerEntityRenderer(type, manager -> new EntityVehicleRenderer<>(manager, renderer));
        VehicleRenderRegistry.registerVehicleRendererFunction(type, rendererFunction, renderer);

        EntityRayTracer.IRayTraceTransforms transforms = renderer.getRayTraceTransforms();
        if(transforms != null)
        {
            EntityRayTracer.instance().registerTransforms(type, transforms);
        }
    }

    private static void setupScreenFactories()
    {
        MenuScreens.register(ModContainers.FLUID_EXTRACTOR.get(), FluidExtractorScreen::new);
        MenuScreens.register(ModContainers.FLUID_MIXER.get(), FluidMixerScreen::new);
        MenuScreens.register(ModContainers.EDIT_VEHICLE.get(), EditVehicleScreen::new);
        MenuScreens.register(ModContainers.WORKSTATION.get(), WorkstationScreen::new);
        MenuScreens.register(ModContainers.STORAGE.get(), StorageScreen::new);
    }

    private static void setupItemColors()
    {
        ItemColor color = (stack, index) ->
        {
            if(index == 0 && stack.hasTag() && stack.getTag().contains("Color", Tag.TAG_INT))
            {
                return stack.getTag().getInt("Color");
            }
            return 0xFFFFFF;
        };

        ForgeRegistries.ITEMS.forEach(item ->
        {
            if(item instanceof SprayCanItem || (item instanceof PartItem && ((PartItem) item).isColored()))
            {
                Minecraft.getInstance().getItemColors().register(color, item);
            }
        });
    }

    @SubscribeEvent
    public void registerKeyBinds(RegisterKeyMappingsEvent event) {
        event.register(KeyBinds.KEY_HORN);
        event.register(KeyBinds.KEY_CYCLE_SEATS);
        event.register(KeyBinds.KEY_HITCH_TRAILER);
    }

    public static class PropertiesSupplier
    {
        private VehicleProperties properties;

        private PropertiesSupplier(VehicleProperties properties)
        {
            this.properties = properties;
        }

        public VehicleProperties get()
        {
            return this.properties;
        }

        private static PropertiesSupplier of(VehicleProperties properties)
        {
            return new PropertiesSupplier(properties);
        }
    }
}
