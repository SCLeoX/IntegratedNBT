package me.tepis.integratednbt.network.serverbound;

import me.tepis.integratednbt.NBTExtractorBE;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.core.BlockPos;

/**
 * From client to server;
 * Sets the output mode
 */
public class NBTExtractorUpdateAutoRefreshMessage extends NBTExtractorUpdateServerMessageBase {
    public static class NBTExtractorUpdateAutoRefreshMessageHandler extends
        NBTExtractorUpdateServerMessageHandlerBase<NBTExtractorUpdateAutoRefreshMessage> {

        @Override
        public void updateTileEntity(
            NBTExtractorUpdateAutoRefreshMessage message,
            NBTExtractorBE nbtExtractorTileEntity
        ) {
            nbtExtractorTileEntity.updateAutoRefresh(message.autoRefresh);
        }

        @Override
        protected Class<NBTExtractorUpdateAutoRefreshMessage> getMessageClass() {
            return NBTExtractorUpdateAutoRefreshMessage.class;
        }

        @Override
        protected NBTExtractorUpdateAutoRefreshMessage createEmpty() {
            return new NBTExtractorUpdateAutoRefreshMessage();
        }
    }

    private boolean autoRefresh;

    private NBTExtractorUpdateAutoRefreshMessage() {}

    public NBTExtractorUpdateAutoRefreshMessage(
        BlockPos blockPos,
        boolean autoRefresh
    ) {
        super(blockPos);
        this.autoRefresh = autoRefresh;
    }

    @Override
    public void fromBytes(FriendlyByteBuf buf) {
        super.fromBytes(buf);
        this.autoRefresh = buf.readBoolean();
    }

    @Override
    public void toBytes(FriendlyByteBuf buf) {
        super.toBytes(buf);
        buf.writeBoolean(this.autoRefresh);
    }
}
