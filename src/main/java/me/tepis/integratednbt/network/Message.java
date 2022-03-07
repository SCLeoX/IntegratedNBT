package me.tepis.integratednbt.network;

import net.minecraft.network.FriendlyByteBuf;

public interface Message {
    void fromBytes(FriendlyByteBuf buf);

    void toBytes(FriendlyByteBuf buf);
}
