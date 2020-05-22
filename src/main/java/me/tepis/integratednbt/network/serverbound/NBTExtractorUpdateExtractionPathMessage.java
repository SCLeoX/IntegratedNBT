package me.tepis.integratednbt.network.serverbound;

import me.tepis.integratednbt.NBTExtractorTileEntity;
import me.tepis.integratednbt.NBTPath;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.BlockPos;

/**
 * From client to server;
 * Sets the extraction path
 */
public class NBTExtractorUpdateExtractionPathMessage extends NBTExtractorUpdateServerMessageBase {
    public static class NBTExtractorUpdateExtractionPathMessageHandler extends
        NBTExtractorUpdateServerMessageHandlerBase<NBTExtractorUpdateExtractionPathMessage> {
        @Override
        public void updateTileEntity(
            NBTExtractorUpdateExtractionPathMessage message,
            NBTExtractorTileEntity nbtExtractorTileEntity
        ) {
            nbtExtractorTileEntity.setExtractionPath(message.path);
            nbtExtractorTileEntity.setDefaultNBTId(message.defaultNBTId);
        }

        @Override
        protected Class<NBTExtractorUpdateExtractionPathMessage> getMessageClass() {
            return NBTExtractorUpdateExtractionPathMessage.class;
        }

        @Override
        protected NBTExtractorUpdateExtractionPathMessage createEmpty() {
            return new NBTExtractorUpdateExtractionPathMessage();
        }
    }

    private NBTPath path;
    private byte defaultNBTId;

    public NBTExtractorUpdateExtractionPathMessage(
        BlockPos blockPos,
        NBTPath path,
        byte defaultNBTId
    ) {
        super(blockPos);
        this.path = path;
        this.defaultNBTId = defaultNBTId;
    }

    private NBTExtractorUpdateExtractionPathMessage() {}

    @Override
    public void fromBytes(PacketBuffer buf) {
        super.fromBytes(buf);
        this.path = NBTPath.fromNBT(buf.readCompoundTag()).orElse(new NBTPath());
        this.defaultNBTId = buf.readByte();
    }

    @Override
    public void toBytes(PacketBuffer buf) {
        super.toBytes(buf);
        buf.writeCompoundTag(this.path.toNBTCompound());
        buf.writeByte(this.defaultNBTId);
    }
}
