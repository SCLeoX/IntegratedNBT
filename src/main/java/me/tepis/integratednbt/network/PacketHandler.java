package me.tepis.integratednbt.network;

import me.tepis.integratednbt.IntegratedNBT;
import me.tepis.integratednbt.network.clientbound.NBTExtractorUpdateClientMessage.NBTExtractorUpdateClientMessageHandler;
import me.tepis.integratednbt.network.serverbound.NBTExtractorRemoteRequestMessage.NBTExtractorRemoteRequestMessageHandler;
import me.tepis.integratednbt.network.serverbound.NBTExtractorUpdateAutoRefreshMessage.NBTExtractorUpdateAutoRefreshMessageHandler;
import me.tepis.integratednbt.network.serverbound.NBTExtractorUpdateExtractionPathMessage.NBTExtractorUpdateExtractionPathMessageHandler;
import me.tepis.integratednbt.network.serverbound.NBTExtractorUpdateOutputModeMessage.NBTExtractorUpdateOutputModeMessageHandler;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.network.NetworkRegistry;
import net.minecraftforge.fml.network.simple.SimpleChannel;

public abstract class PacketHandler {
    private static final String PROTOCOL_VERSION = "1";

    public static final SimpleChannel INSTANCE = NetworkRegistry.newSimpleChannel(
        new ResourceLocation(IntegratedNBT.MODID, "main"),
        () -> PROTOCOL_VERSION,
        PROTOCOL_VERSION::equals,
        PROTOCOL_VERSION::equals
    );

    public static void register() {
        int id = -1;
        new NBTExtractorUpdateClientMessageHandler().register(INSTANCE, ++id);
        new NBTExtractorRemoteRequestMessageHandler().register(INSTANCE, ++id);
        new NBTExtractorUpdateAutoRefreshMessageHandler().register(INSTANCE, ++id);
        new NBTExtractorUpdateExtractionPathMessageHandler().register(INSTANCE, ++id);
        new NBTExtractorUpdateOutputModeMessageHandler().register(INSTANCE, ++id);
    }
}
