package me.tepis.integratednbt.network.clientbound;

import io.netty.buffer.ByteBuf;
import me.tepis.integratednbt.ByteMaskMaker;
import me.tepis.integratednbt.NBTExtractorGui;
import me.tepis.integratednbt.NBTExtractorOutputMode;
import me.tepis.integratednbt.NBTPath;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import org.cyclops.cyclopscore.helper.L10NHelpers.UnlocalizedString;

import java.util.Objects;

/**
 * From server to client;
 * Updates NBT tree, error code, and/or extraction path for the client
 */
public class NBTExtractorUpdateClientMessage implements IMessage {
    public static class NBTExtractorUpdateClientMessageHandler
        implements IMessageHandler<NBTExtractorUpdateClientMessage, IMessage> {
        @Override
        public IMessage onMessage(NBTExtractorUpdateClientMessage message, MessageContext ctx) {
            Minecraft.getMinecraft().addScheduledTask(() -> {
                if (message.isUpdated(MASK_NBT)) {
                    NBTExtractorGui.updateNBT(message.nbt);
                }
                if (message.isUpdated(MASK_ERROR_CODE)) {
                    NBTExtractorGui.updateError(message.errorCode);
                }
                if (message.isUpdated(MASK_EXTRACTION_PATH)) {
                    NBTExtractorGui.updateExtractionPath(message.path);
                }
                if (message.isUpdated(MASK_OUTPUT_MODE)) {
                    NBTExtractorGui.updateOutputMode(message.outputMode);
                }
                if (message.isUpdated(MASK_ERROR_MESSAGE)) {
                    NBTExtractorGui.updateErrorMessage(message.errorMessage);
                }
            });
            return null;
        }
    }

    private static ByteMaskMaker maskMaker = new ByteMaskMaker();
    private static final byte MASK_NBT = maskMaker.nextMask();
    private static final byte MASK_ERROR_CODE = maskMaker.nextMask();
    private static final byte MASK_EXTRACTION_PATH = maskMaker.nextMask();
    private static final byte MASK_OUTPUT_MODE = maskMaker.nextMask();
    private static final byte MASK_ERROR_MESSAGE = maskMaker.nextMask();

    private byte updated = 0;
    private ErrorCode errorCode;
    private NBTTagCompound nbt;
    private NBTPath path;
    private NBTExtractorOutputMode outputMode;
    private UnlocalizedString errorMessage;

    public NBTExtractorUpdateClientMessage() {}

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

    public void updateOutputMode(NBTExtractorOutputMode outputMode) {
        this.outputMode = outputMode;
        this.updated |= MASK_OUTPUT_MODE;
    }

    public void updateErrorMessage(UnlocalizedString errorMessage) {
        this.errorMessage = errorMessage;
        this.updated |= MASK_ERROR_MESSAGE;
    }

    public boolean isEmpty() {
        return this.updated == 0;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        this.updated = buf.readByte();
        if (this.isUpdated(MASK_NBT)) {
            this.nbt = ByteBufUtils.readTag(buf);
        }
        if (this.isUpdated(MASK_ERROR_CODE)) {
            this.errorCode = ErrorCode.values()[buf.readByte()];
        }
        if (this.isUpdated(MASK_EXTRACTION_PATH)) {
            this.path = NBTPath.fromNBT(ByteBufUtils.readTag(buf)).orElse(new NBTPath());
        }
        if (this.isUpdated(MASK_OUTPUT_MODE)) {
            this.outputMode = NBTExtractorOutputMode.values()[buf.readByte()];
        }
        if (this.isUpdated(MASK_ERROR_MESSAGE)) {
            if (buf.readBoolean()) { // Is null
                this.errorMessage = null;
            } else {
                this.errorMessage = new UnlocalizedString();
                this.errorMessage.fromNBT(Objects.requireNonNull(ByteBufUtils.readTag(buf)));
            }
        }
    }

    private boolean isUpdated(byte mask) {
        return (this.updated & mask) > 0;
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeByte(this.updated);
        if (this.isUpdated(MASK_NBT)) {
            ByteBufUtils.writeTag(buf, this.nbt);
        }
        if (this.isUpdated(MASK_ERROR_CODE)) {
            buf.writeByte(this.errorCode.ordinal());
        }
        if (this.isUpdated(MASK_EXTRACTION_PATH)) {
            ByteBufUtils.writeTag(buf, this.path.toNBTCompound());
        }
        if (this.isUpdated(MASK_OUTPUT_MODE)) {
            buf.writeByte(this.outputMode.ordinal());
        }
        if (this.isUpdated(MASK_ERROR_MESSAGE)) {
            if (this.errorMessage == null) { // Is null
                buf.writeBoolean(true);
            } else {
                buf.writeBoolean(false);
                ByteBufUtils.writeTag(buf, this.errorMessage.toNBT());
            }
        }
    }

    public enum ErrorCode {
        NO_ERROR,
        TYPE_ERROR,
        EVAL_ERROR,
        UNEXPECTED_ERROR,
    }
}
