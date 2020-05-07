package me.tepis.integratednbt.network.serverbound;

import io.netty.buffer.ByteBuf;
import me.tepis.integratednbt.NBTExtractorTileEntity;
import me.tepis.integratednbt.NBTPath;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.network.ByteBufUtils;

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

    public NBTExtractorUpdateExtractionPathMessage() {}

    @Override
    public void fromBytes(ByteBuf buf) {
        super.fromBytes(buf);
        NBTTagCompound data = ByteBufUtils.readTag(buf);
        this.path = data == null
            ? new NBTPath()
            : NBTPath.fromNBT(data.getTag("path")).orElse(new NBTPath());
        this.defaultNBTId = buf.readByte();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        super.toBytes(buf);
        ByteBufUtils.writeTag(buf, this.path.toNBTCompound());
        buf.writeByte(this.defaultNBTId);
    }
}
