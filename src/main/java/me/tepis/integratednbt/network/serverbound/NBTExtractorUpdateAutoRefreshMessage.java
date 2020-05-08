package me.tepis.integratednbt.network.serverbound;

import io.netty.buffer.ByteBuf;
import me.tepis.integratednbt.NBTExtractorTileEntity;
import net.minecraft.util.math.BlockPos;

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
            NBTExtractorTileEntity nbtExtractorTileEntity
        ) {
            nbtExtractorTileEntity.updateAutoRefresh(message.autoRefresh);
        }
    }

    private boolean autoRefresh;

    public NBTExtractorUpdateAutoRefreshMessage() {}

    public NBTExtractorUpdateAutoRefreshMessage(
        BlockPos blockPos,
        boolean autoRefresh
    ) {
        super(blockPos);
        this.autoRefresh = autoRefresh;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        super.fromBytes(buf);
        this.autoRefresh = buf.readBoolean();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        super.toBytes(buf);
        buf.writeBoolean(this.autoRefresh);
    }
}
