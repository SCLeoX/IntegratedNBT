package me.tepis.integratednbt.network.serverbound;

import me.tepis.integratednbt.NBTExtractorTileEntity;
import me.tepis.integratednbt.network.Message;
import me.tepis.integratednbt.network.MessageHandler;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.network.NetworkEvent.Context;

import java.util.function.Supplier;

/**
 * From client to server;
 * Updates information on server
 */
public abstract class NBTExtractorUpdateServerMessageBase implements Message {
    public static abstract class NBTExtractorUpdateServerMessageHandlerBase<
        T extends NBTExtractorUpdateServerMessageBase>
        extends MessageHandler<T> {

        @Override
        @SuppressWarnings("deprecation")
        public final void onMessage(T message, Context ctx) {
            ctx.enqueueWork(() -> {
                ServerPlayerEntity player = ctx.getSender();
                assert player != null;
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
    public void fromBytes(PacketBuffer buf) {
        this.blockPos = BlockPos.fromLong(buf.readLong());
    }

    @Override
    public void toBytes(PacketBuffer buf) {
        buf.writeLong(this.blockPos.toLong());
    }
}
