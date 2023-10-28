package me.tepis.integratednbt.network.serverbound;

import me.tepis.integratednbt.NBTExtractorBE;
import me.tepis.integratednbt.network.Message;
import me.tepis.integratednbt.network.MessageHandler;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraftforge.network.NetworkEvent.Context;

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
                ServerPlayer player = ctx.getSender();
                assert player != null;
                Level world = player.level();
                if (!world.hasChunkAt(message.blockPos)) {
                    return;
                }
                BlockEntity tileEntity = world.getBlockEntity(message.blockPos);
                if (tileEntity == null) {
                    return;
                }
                if (!(tileEntity instanceof NBTExtractorBE)) {
                    return;
                }
                NBTExtractorBE nbtExtractorTileEntity = (NBTExtractorBE) tileEntity;
                if (!nbtExtractorTileEntity.stillValid(player)) {
                    return;
                }
                this.updateTileEntity(message, nbtExtractorTileEntity);
            });
        }

        public abstract void updateTileEntity(
            T message,
            NBTExtractorBE nbtExtractorTileEntity
        );
    }

    protected BlockPos blockPos;

    public NBTExtractorUpdateServerMessageBase(BlockPos blockPos) {
        this.blockPos = blockPos;
    }

    public NBTExtractorUpdateServerMessageBase() {}

    @Override
    public void fromBytes(FriendlyByteBuf buf) {
        this.blockPos = BlockPos.of(buf.readLong());
    }

    @Override
    public void toBytes(FriendlyByteBuf buf) {
        buf.writeLong(this.blockPos.asLong());
    }
}
