package me.tepis.integratednbt.network.serverbound;

import me.tepis.integratednbt.NBTExtractorOutputMode;
import me.tepis.integratednbt.NBTExtractorTileEntity;
import net.minecraft.network.PacketBuffer;
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

        @Override
        protected Class<NBTExtractorUpdateOutputModeMessage> getMessageClass() {
            return NBTExtractorUpdateOutputModeMessage.class;
        }

        @Override
        protected NBTExtractorUpdateOutputModeMessage createEmpty() {
            return new NBTExtractorUpdateOutputModeMessage();
        }
    }

    private NBTExtractorOutputMode outputMode;

    private NBTExtractorUpdateOutputModeMessage() {}

    public NBTExtractorUpdateOutputModeMessage(
        BlockPos blockPos,
        NBTExtractorOutputMode outputMode
    ) {
        super(blockPos);
        this.outputMode = outputMode;
    }

    @Override
    public void fromBytes(PacketBuffer buf) {
        super.fromBytes(buf);
        this.outputMode = NBTExtractorOutputMode.values()[buf.readByte()];
    }

    @Override
    public void toBytes(PacketBuffer buf) {
        super.toBytes(buf);
        buf.writeByte(this.outputMode.ordinal());
    }
}
