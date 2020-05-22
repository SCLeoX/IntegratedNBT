package me.tepis.integratednbt.network;

import net.minecraft.network.PacketBuffer;

public interface Message {
    void fromBytes(PacketBuffer buf);

    void toBytes(PacketBuffer buf);
}
