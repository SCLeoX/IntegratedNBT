package me.tepis.integratednbt.network.serverbound;

import me.tepis.integratednbt.Additions;
import me.tepis.integratednbt.NBTExtractorRemote;
import me.tepis.integratednbt.network.Message;
import me.tepis.integratednbt.network.MessageHandler;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.Hand;
import net.minecraftforge.fml.network.NetworkEvent.Context;

import java.util.function.Supplier;

/**
 * From client to server;
 * Requests to open the GUI for a NBT Extractor at location
 */
public class NBTExtractorRemoteRequestMessage implements Message {
    public static class NBTExtractorRemoteRequestMessageHandler
        extends MessageHandler<NBTExtractorRemoteRequestMessage> {
        @Override
        public void onMessage(NBTExtractorRemoteRequestMessage message, Context ctx) {
            ctx.enqueueWork(() -> {
                ServerPlayerEntity player = ctx.getSender();
                NBTExtractorRemote remote = Additions.NBT_EXTRACTOR_REMOTE.get();
                assert player != null;
                if (player.getHeldItem(Hand.MAIN_HAND).getItem() == remote) {
                    remote.serverUse(player.getHeldItem(Hand.MAIN_HAND), player);
                } else if (player.getHeldItem(Hand.OFF_HAND).getItem() == remote) {
                    remote.serverUse(player.getHeldItem(Hand.OFF_HAND), player);
                }
            });
        }

        @Override
        protected Class<NBTExtractorRemoteRequestMessage> getMessageClass() {
            return NBTExtractorRemoteRequestMessage.class;
        }

        @Override
        protected NBTExtractorRemoteRequestMessage createEmpty() {
            return new NBTExtractorRemoteRequestMessage();
        }
    }

    @Override
    public void fromBytes(PacketBuffer buf) {}

    @Override
    public void toBytes(PacketBuffer buf) {}
}
