package me.tepis.integratednbt;

import io.netty.buffer.ByteBuf;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class NBTExtractorUpdateTreeMessage implements IMessage {
    public static class NBTExtractorUpdateTreeMessageHandler
        implements IMessageHandler<NBTExtractorUpdateTreeMessage, IMessage> {
        @Override
        public IMessage onMessage(NBTExtractorUpdateTreeMessage message, MessageContext ctx) {
            NBTExtractorGui.updateError(message.errorCode);
            if (message.nbt != null) {
                NBTExtractorGui.updateNBT(message.nbt);
            }
            return null;
        }
    }

    private ErrorCode errorCode;
    private NBTTagCompound nbt;

    public NBTExtractorUpdateTreeMessage(
        ErrorCode errorCode,
        NBTTagCompound nbt
    ) {
        this.errorCode = errorCode;
        this.nbt = nbt;
    }

    public NBTExtractorUpdateTreeMessage() {}

    @Override
    public void fromBytes(ByteBuf buf) {
        this.errorCode = ErrorCode.values()[buf.readByte()];
        if (this.isSuccess()) {
            this.nbt = ByteBufUtils.readTag(buf);
        }
    }

    public boolean isSuccess() {
        return this.errorCode.equals(ErrorCode.NO_ERROR);
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeByte(this.errorCode.ordinal());
        if (this.isSuccess()) {
            ByteBufUtils.writeTag(buf, this.nbt);
        }
    }

    public enum ErrorCode {
        NO_ERROR,
        TYPE_ERROR,
        EVAL_ERROR,
        UNEXPECTED_ERROR,
    }
}
