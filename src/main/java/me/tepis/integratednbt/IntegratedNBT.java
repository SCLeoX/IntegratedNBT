package me.tepis.integratednbt;

import me.tepis.integratednbt.network.clientbound.NBTExtractorUpdateClientMessage;
import me.tepis.integratednbt.network.clientbound.NBTExtractorUpdateClientMessage.NBTExtractorUpdateClientMessageHandler;
import me.tepis.integratednbt.network.serverbound.NBTExtractorRemoteRequestMessage;
import me.tepis.integratednbt.network.serverbound.NBTExtractorRemoteRequestMessage.NBTExtractorRemoteRequestMessageHandler;
import me.tepis.integratednbt.network.serverbound.NBTExtractorUpdateExtractionPathMessage;
import me.tepis.integratednbt.network.serverbound.NBTExtractorUpdateExtractionPathMessage.NBTExtractorUpdateExtractionPathMessageHandler;
import me.tepis.integratednbt.network.serverbound.NBTExtractorUpdateOutputModeMessage;
import me.tepis.integratednbt.network.serverbound.NBTExtractorUpdateOutputModeMessage.NBTExtractorUpdateOutputModeMessageHandler;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.item.Item;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.Mod.Instance;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.registries.IForgeRegistry;
import org.apache.logging.log4j.Logger;
import org.cyclops.integrateddynamics.core.evaluate.operator.OperatorRegistry;
import org.cyclops.integrateddynamics.core.item.VariableFacadeHandlerRegistry;

@Mod(modid = IntegratedNBT.MODID, name = IntegratedNBT.NAME, version = IntegratedNBT.VERSION,
    dependencies = "required-after:integrateddynamics@[1.1.2,);")
public class IntegratedNBT {
    public static final String MODID = "integratednbt";
    public static final String NAME = "Integrated NBT";
    public static final String VERSION = "1.1.1";
    private static SimpleNetworkWrapper networkChannel =
        NetworkRegistry.INSTANCE.newSimpleChannel(MODID);
    @Instance(MODID)
    private static IntegratedNBT instance;
    private static Logger logger;

    public static Logger getLogger() {
        return logger;
    }

    public static SimpleNetworkWrapper getNetworkChannel() {
        return networkChannel;
    }

    public static IntegratedNBT getInstance() {
        return instance;
    }

    @EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        logger = event.getModLog();

        NetworkRegistry.INSTANCE.registerGuiHandler(this, new NBTExtractorGuiHandler());
        int discriminator = -1;
        networkChannel.registerMessage(
            NBTExtractorUpdateClientMessageHandler.class,
            NBTExtractorUpdateClientMessage.class,
            ++discriminator,
            Side.CLIENT
        );
        networkChannel.registerMessage(
            NBTExtractorUpdateExtractionPathMessageHandler.class,
            NBTExtractorUpdateExtractionPathMessage.class,
            ++discriminator,
            Side.SERVER
        );
        networkChannel.registerMessage(
            NBTExtractorRemoteRequestMessageHandler.class,
            NBTExtractorRemoteRequestMessage.class,
            ++discriminator,
            Side.SERVER
        );
        networkChannel.registerMessage(
            NBTExtractorUpdateOutputModeMessageHandler.class,
            NBTExtractorUpdateOutputModeMessage.class,
            ++discriminator,
            Side.SERVER
        );
        MinecraftForge.EVENT_BUS.register(this);
    }

    @EventHandler
    public void init(FMLInitializationEvent event) {}

    @EventHandler
    public void postInit(FMLPostInitializationEvent event) {
        VariableFacadeHandlerRegistry.getInstance()
            .registerHandler(new NBTExtractedVariableFacadeHandler());
        OperatorRegistry.getInstance().registerSerializer(new NBTExtractionOperatorSerializer());
    }

    @SubscribeEvent
    public void onModelRegistration(ModelRegistryEvent event) {
        ModelLoader.setCustomModelResourceLocation(
            NBTExtractor.getInstance().getItemBlock(),
            0,
            new ModelResourceLocation(NBTExtractor.REGISTRY_NAME, "inventory")
        );
        ModelLoader.setCustomModelResourceLocation(
            NBTExtractorRemote.getInstance(),
            0,
            new ModelResourceLocation(NBTExtractorRemote.REGISTRY_NAME, "inventory")
        );
    }

    @SubscribeEvent
    @SuppressWarnings("deprecation")
    public void registerBlock(RegistryEvent.Register<Block> event) {
        IForgeRegistry<Block> registry = event.getRegistry();
        registry.register(NBTExtractor.getInstance());
        GameRegistry.registerTileEntity(NBTExtractorTileEntity.class, NBTExtractor.REGISTRY_NAME);
    }

    @SubscribeEvent
    public void registerItem(RegistryEvent.Register<Item> event) {
        IForgeRegistry<Item> registry = event.getRegistry();
        registry.register(NBTExtractor.getInstance().getItemBlock());
        registry.register(NBTExtractorRemote.getInstance());
    }
}
