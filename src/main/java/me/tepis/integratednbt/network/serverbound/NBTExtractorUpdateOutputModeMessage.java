package me.tepis.integratednbt.network.serverbound;

import io.netty.buffer.ByteBuf;
import me.tepis.integratednbt.NBTExtractorOutputMode;
import me.tepis.integratednbt.NBTExtractorTileEntity;
import net.minecraft.util.math.BlockPos;

/**
 * From client to server;
 * Sets the output mode
 */
public class NBTExtractorUpdateOutputModeMessage extends NBTExtractorUpdateServerMessageBase {
    public static class NBTExtractorUpdateOutputModeMessageHandler extends
        NBTExtractorUpdateServerMessageHandlerBase<NBTExtractorUpdateOutputModeMessage> {

        @Override
        public void updateTileEntity(
            NBTExtractorUpdateOutputModeMessage message,
            NBTExtractorTileEntity nbtExtractorTileEntity
        ) {
            nbtExtractorTileEntity.setOutputMode(message.outputMode);
        }
    }

    private NBTExtractorOutputMode outputMode;

    public NBTExtractorUpdateOutputModeMessage() {}

    public NBTExtractorUpdateOutputModeMessage(
        BlockPos blockPos,
        NBTExtractorOutputMode outputMode
    ) {
        super(blockPos);
        this.outputMode = outputMode;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        super.fromBytes(buf);
        this.outputMode = NBTExtractorOutputMode.values()[buf.readByte()];
    }

    @Override
    public void toBytes(ByteBuf buf) {
        super.toBytes(buf);
        buf.writeByte(this.outputMode.ordinal());
    }
}
