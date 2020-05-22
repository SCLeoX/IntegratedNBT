package me.tepis.integratednbt;

import me.tepis.integratednbt.network.PacketHandler;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(IntegratedNBT.MODID)
public class IntegratedNBT {
    public static final String MODID = "integratednbt";
    public static final Logger LOGGER = LogManager.getLogger(MODID);

    public IntegratedNBT() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        Additions.BLOCKS.register(modEventBus);
        Additions.ITEMS.register(modEventBus);
        Additions.CONTAINER_TYPES.register(modEventBus);
        Additions.TILE_ENTITY_TYPES.register(modEventBus);
        PacketHandler.register();
    }

//    @EventHandler
//    public void postInit(FMLPostInitializationEvent event) {
//        VariableFacadeHandlerRegistry.getInstance()
//            .registerHandler(new NBTExtractedVariableFacadeHandler());
//        OperatorRegistry.getInstance().registerSerializer(new NBTExtractionOperatorSerializer());
//    }

//    private static SimpleNetworkWrapper networkChannel =
//        NetworkRegistry.INSTANCE.newSimpleChannel(MODID);
//
//    public static SimpleNetworkWrapper getNetworkChannel() {
//        return networkChannel;
//    }
//
//
//    @EventHandler
//    public void preInit(FMLPreInitializationEvent event) {
//        logger = event.getModLog();
//
//        NetworkRegistry.INSTANCE.registerGuiHandler(this, new NBTExtractorGuiHandler());
//        int discriminator = -1;
//        networkChannel.registerMessage(
//            NBTExtractorUpdateClientMessageHandler.class,
//            NBTExtractorUpdateClientMessage.class,
//            ++discriminator,
//            Dist.CLIENT
//        );
//        networkChannel.registerMessage(
//            NBTExtractorUpdateExtractionPathMessageHandler.class,
//            NBTExtractorUpdateExtractionPathMessage.class,
//            ++discriminator,
//            Side.SERVER
//        );
//        networkChannel.registerMessage(
//            NBTExtractorRemoteRequestMessageHandler.class,
//            NBTExtractorRemoteRequestMessage.class,
//            ++discriminator,
//            Side.SERVER
//        );
//        networkChannel.registerMessage(
//            NBTExtractorUpdateOutputModeMessageHandler.class,
//            NBTExtractorUpdateOutputModeMessage.class,
//            ++discriminator,
//            Side.SERVER
//        );
//        networkChannel.registerMessage(
//            NBTExtractorUpdateAutoRefreshMessageHandler.class,
//            NBTExtractorUpdateAutoRefreshMessage.class,
//            ++discriminator,
//            Side.SERVER
//        );
//        MinecraftForge.EVENT_BUS.register(this);
//    }
//
//    @EventHandler
//    public void init(FMLInitializationEvent event) {}
//

//
//    @SubscribeEvent
//    public void onModelRegistration(ModelRegistryEvent event) {
//        ModelLoader.setCustomModelResourceLocation(
//            NBTExtractor.getInstance().getItemBlock(),
//            0,
//            new ModelResourceLocation(NBTExtractor.REGISTRY_NAME, "inventory")
//        );
//        ModelLoader.setCustomModelResourceLocation(
//            NBTExtractorRemote.getInstance(),
//            0,
//            new ModelResourceLocation(NBTExtractorRemote.REGISTRY_NAME, "inventory")
//        );
//    }
//
//    @SubscribeEvent
//    @SuppressWarnings("deprecation")
//    public void registerBlock(RegistryEvent.Register<Block> event) {
//        IForgeRegistry<Block> registry = event.getRegistry();
//        registry.register(NBTExtractor.getInstance());
//        GameRegistry.registerTileEntity(NBTExtractorTileEntity.class, NBTExtractor.REGISTRY_NAME);
//    }
//
//    @SubscribeEvent
//    public void registerItem(RegistryEvent.Register<Item> event) {
//        IForgeRegistry<Item> registry = event.getRegistry();
//        registry.register(NBTExtractor.getInstance().getItemBlock());
//        registry.register(NBTExtractorRemote.getInstance());
//    }
}
