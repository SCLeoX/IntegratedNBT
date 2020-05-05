package me.tepis.integratednbt;

import io.netty.buffer.ByteBuf;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

/**
 * From server to client;
 * Updates NBT tree, error code, and/or extraction path for the client
 */
public class NBTExtractorUpdateClientMessage implements IMessage {
    public static class NBTExtractorUpdateClientMessageHandler
        implements IMessageHandler<NBTExtractorUpdateClientMessage, IMessage> {
        @Override
        public IMessage onMessage(NBTExtractorUpdateClientMessage message, MessageContext ctx) {
            if (message.nbt != null) {
                NBTExtractorGui.updateNBT(message.nbt);
            }
            if (message.errorCode != null) {
                NBTExtractorGui.updateError(message.errorCode);
            }
            if (message.path != null) {
                NBTExtractorGui.updateExtractionPath(message.path);
            }
            return null;
        }
    }

    private static ByteMaskMaker maskMaker = new ByteMaskMaker();
    private static final byte MASK_NBT = maskMaker.nextMask();
    private static final byte MASK_ERROR_CODE = maskMaker.nextMask();
    private static final byte MASK_EXTRACTION_PATH = maskMaker.nextMask();

    private byte updated = 0;
    private ErrorCode errorCode;
    private NBTTagCompound nbt;
    private NBTPath path;

    public void updateNBT(NBTTagCompound nbt) {
        this.nbt = nbt;
        this.updated |= MASK_NBT;
    }

    public void updateErrorCode(ErrorCode errorCode) {
        this.errorCode = errorCode;
        this.updated |= MASK_ERROR_CODE;
    }

    public void updateExtractionPath(NBTPath path) {
        this.path = path;
        this.updated |= MASK_EXTRACTION_PATH;
    }

    public boolean isEmpty() {
        return this.updated == 0;
    }

    public NBTExtractorUpdateClientMessage() {}

    @Override
    public void fromBytes(ByteBuf buf) {
        this.updated = buf.readByte();
        if ((this.updated & MASK_NBT) > 0) {
            this.nbt = ByteBufUtils.readTag(buf);
        }
        if ((this.updated & MASK_ERROR_CODE) > 0) {
            this.errorCode = ErrorCode.values()[buf.readByte()];
        }
        if ((this.updated & MASK_EXTRACTION_PATH) > 0) {
            this.path = NBTPath.fromNBT(ByteBufUtils.readTag(buf)).orElse(new NBTPath());
        }
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeByte(this.updated);
        if ((this.updated & MASK_NBT) > 0) {
            ByteBufUtils.writeTag(buf, this.nbt);
        }
        if ((this.updated & MASK_ERROR_CODE) > 0) {
            buf.writeByte(this.errorCode.ordinal());
        }
        if ((this.updated & MASK_EXTRACTION_PATH) > 0) {
            ByteBufUtils.writeTag(buf, this.path.toNBTCompound());
        }
    }

    public enum ErrorCode {
        NO_ERROR,
        TYPE_ERROR,
        EVAL_ERROR,
        UNEXPECTED_ERROR,
    }
}
