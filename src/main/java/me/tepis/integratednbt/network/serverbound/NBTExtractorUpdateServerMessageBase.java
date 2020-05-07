package me.tepis.integratednbt.network.serverbound;

import io.netty.buffer.ByteBuf;
import me.tepis.integratednbt.NBTExtractorTileEntity;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

/**
 * From client to server;
 * Sets the extraction path
 */
public abstract class NBTExtractorUpdateServerMessageBase implements IMessage {
    public static abstract class NBTExtractorUpdateServerMessageHandlerBase<
        T extends NBTExtractorUpdateServerMessageBase>
        implements IMessageHandler<T, IMessage> {

        @Override
        public final IMessage onMessage(T message, MessageContext ctx) {
            ((WorldServer) ctx.getServerHandler().player.world).addScheduledTask(() -> {
                EntityPlayerMP player = ctx.getServerHandler().player;
                World world = player.world;
                if (!world.isBlockLoaded(message.blockPos)) {
                    return;
                }
                TileEntity tileEntity = world.getTileEntity(message.blockPos);
                if (tileEntity == null) {
                    return;
                }
                if (!(tileEntity instanceof NBTExtractorTileEntity)) {
                    return;
                }
                NBTExtractorTileEntity nbtExtractorTileEntity = (NBTExtractorTileEntity) tileEntity;
                if (!nbtExtractorTileEntity.isUsableByPlayer(player)) {
                    return;
                }
                this.updateTileEntity(message, nbtExtractorTileEntity);
            });
            return null;
        }

        public abstract void updateTileEntity(
            T message,
            NBTExtractorTileEntity nbtExtractorTileEntity
        );
    }

    protected BlockPos blockPos;

    public NBTExtractorUpdateServerMessageBase(BlockPos blockPos) {
        this.blockPos = blockPos;
    }

    public NBTExtractorUpdateServerMessageBase() {}

    @Override
    public void fromBytes(ByteBuf buf) {
        this.blockPos = BlockPos.fromLong(buf.readLong());
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeLong(this.blockPos.toLong());
    }
}
