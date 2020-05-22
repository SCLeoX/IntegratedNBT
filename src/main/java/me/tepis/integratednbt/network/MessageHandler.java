package me.tepis.integratednbt.network;

import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent.Context;
import net.minecraftforge.fml.network.simple.SimpleChannel;

import java.util.function.Supplier;

public abstract class MessageHandler<TMessage extends Message> {
    public void register(SimpleChannel channel, int id) {
        channel.registerMessage(
            id,
            this.getMessageClass(),
            this::encode,
            this::decode,
            ((message, contextSupplier) -> {
                Context ctx = contextSupplier.get();
                this.onMessage(message, ctx);
                ctx.setPacketHandled(true);
            })
        );
    }

    protected abstract Class<TMessage> getMessageClass();

    private void encode(TMessage message, PacketBuffer writeTo) {
        message.toBytes(writeTo);
    }

    private TMessage decode(PacketBuffer readFrom) {
        TMessage message = this.createEmpty();
        message.fromBytes(readFrom);
        return message;
    }

    public abstract void onMessage(TMessage message, Context ctx);

    protected abstract TMessage createEmpty();
}
