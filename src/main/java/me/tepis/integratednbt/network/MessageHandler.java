package me.tepis.integratednbt.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent.Context;
import net.minecraftforge.network.simple.SimpleChannel;

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

    private void encode(TMessage message, FriendlyByteBuf writeTo) {
        message.toBytes(writeTo);
    }

    private TMessage decode(FriendlyByteBuf readFrom) {
        TMessage message = this.createEmpty();
        message.fromBytes(readFrom);
        return message;
    }

    public abstract void onMessage(TMessage message, Context ctx);

    protected abstract TMessage createEmpty();
}
