package me.tepis.integratednbt;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class NBTExtractorUpdateExtractionPathMessage implements IMessage {
    public static class NBTExtractorUpdateExtractionPathMessageHandler
        implements IMessageHandler<NBTExtractorUpdateExtractionPathMessage, IMessage> {
        @Override
        public IMessage onMessage(
            NBTExtractorUpdateExtractionPathMessage message, MessageContext ctx
        ) {
            EntityPlayerMP player = ctx.getServerHandler().player;
            World world = player.world;
            if (!world.isBlockLoaded(message.blockPos)) {
                return null;
            }
            TileEntity tileEntity = world.getTileEntity(message.blockPos);
            if (tileEntity == null) {
                return null;
            }
            if (!(tileEntity instanceof NBTExtractorTileEntity)) {
                return null;
            }
            NBTExtractorTileEntity nbtExtractorTileEntity = (NBTExtractorTileEntity) tileEntity;
            if (!nbtExtractorTileEntity.isUsableByPlayer(player)) {
                return null;
            }
            nbtExtractorTileEntity.setExtractionPath(message.path);
            nbtExtractorTileEntity.setDefaultNBTId(message.defaultNBTId);
            return null;
        }
    }

    private BlockPos blockPos;
    private NBTPath path;
    private byte defaultNBTId;

    public NBTExtractorUpdateExtractionPathMessage(
        BlockPos blockPos,
        NBTPath path,
        byte defaultNBTId
    ) {
        this.blockPos = blockPos;
        this.path = path;
        this.defaultNBTId = defaultNBTId;
    }

    public NBTExtractorUpdateExtractionPathMessage() {}

    @Override
    public void fromBytes(ByteBuf buf) {
        this.blockPos = BlockPos.fromLong(buf.readLong());
        NBTTagCompound data = ByteBufUtils.readTag(buf);
        if (data == null) {
            this.path = new NBTPath();
            return;
        }
        this.path = NBTPath.fromNBT(data.getTag("path")).orElse(new NBTPath());
        this.defaultNBTId = buf.readByte();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeLong(this.blockPos.toLong());
        NBTTagCompound data = new NBTTagCompound();
        data.setTag("path", this.path.toNBT());
        ByteBufUtils.writeTag(buf, data);
        buf.writeByte(this.defaultNBTId);
    }
}
