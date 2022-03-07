package me.tepis.integratednbt.network.serverbound;

import me.tepis.integratednbt.NBTExtractorOutputMode;
import me.tepis.integratednbt.NBTExtractorBE;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.core.BlockPos;

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
            NBTExtractorBE nbtExtractorTileEntity
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
    public void fromBytes(FriendlyByteBuf buf) {
        super.fromBytes(buf);
        this.outputMode = NBTExtractorOutputMode.values()[buf.readByte()];
    }

    @Override
    public void toBytes(FriendlyByteBuf buf) {
        super.toBytes(buf);
        buf.writeByte(this.outputMode.ordinal());
    }
}
