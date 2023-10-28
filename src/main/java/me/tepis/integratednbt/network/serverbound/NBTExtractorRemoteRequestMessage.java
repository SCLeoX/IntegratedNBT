package me.tepis.integratednbt.network.serverbound;

import me.tepis.integratednbt.Additions;
import me.tepis.integratednbt.NBTExtractorRemote;
import me.tepis.integratednbt.network.Message;
import me.tepis.integratednbt.network.MessageHandler;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.InteractionHand;
import net.minecraftforge.network.NetworkEvent.Context;

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
                ServerPlayer player = ctx.getSender();
                NBTExtractorRemote remote = Additions.NBT_EXTRACTOR_REMOTE.get();
                assert player != null;
                if (player.getItemInHand(InteractionHand.MAIN_HAND).getItem() == remote) {
                    remote.serverUse(player.getItemInHand(InteractionHand.MAIN_HAND), player);
                } else if (player.getItemInHand(InteractionHand.OFF_HAND).getItem() == remote) {
                    remote.serverUse(player.getItemInHand(InteractionHand.OFF_HAND), player);
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
    public void fromBytes(FriendlyByteBuf buf) {}

    @Override
    public void toBytes(FriendlyByteBuf buf) {}
}
