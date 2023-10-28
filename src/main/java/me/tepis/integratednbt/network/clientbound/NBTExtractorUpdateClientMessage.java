package me.tepis.integratednbt.network.clientbound;

import me.tepis.integratednbt.ByteMaskMaker;
import me.tepis.integratednbt.NBTExtractorScreen;
import me.tepis.integratednbt.NBTExtractorOutputMode;
import me.tepis.integratednbt.NBTPath;
import me.tepis.integratednbt.network.Message;
import me.tepis.integratednbt.network.MessageHandler;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraftforge.network.NetworkEvent.Context;

/**
 * From server to client;
 * Updates NBT tree, error code, and/or extraction path for the client
 */
public class NBTExtractorUpdateClientMessage implements Message {
    public static class NBTExtractorUpdateClientMessageHandler
        extends MessageHandler<NBTExtractorUpdateClientMessage> {
        @Override
        protected Class<NBTExtractorUpdateClientMessage> getMessageClass() {
            return NBTExtractorUpdateClientMessage.class;
        }

        @Override
        public void onMessage(
            NBTExtractorUpdateClientMessage message,
            Context ctx
        ) {
            ctx.enqueueWork(() -> {
                if (message.isUpdated(MASK_NBT)) {
                    NBTExtractorScreen.updateNBT(message.nbt);
                }
                if (message.isUpdated(MASK_ERROR_CODE)) {
                    NBTExtractorScreen.updateError(message.errorCode);
                }
                if (message.isUpdated(MASK_EXTRACTION_PATH)) {
                    NBTExtractorScreen.updateExtractionPath(message.path);
                }
                if (message.isUpdated(MASK_OUTPUT_MODE)) {
                    NBTExtractorScreen.updateOutputMode(message.outputMode);
                }
                if (message.isUpdated(MASK_ERROR_MESSAGE)) {
                    NBTExtractorScreen.updateErrorMessage(message.errorMessage);
                }
                if (message.isUpdated(MASK_AUTO_REFRESH)) {
                    NBTExtractorScreen.updateAutoRefresh(message.autoRefresh);
                }
            });
        }

        @Override
        protected NBTExtractorUpdateClientMessage createEmpty() {
            return new NBTExtractorUpdateClientMessage();
        }
    }

    private static ByteMaskMaker maskMaker = new ByteMaskMaker();
    private static final byte MASK_NBT = maskMaker.nextMask();
    private static final byte MASK_ERROR_CODE = maskMaker.nextMask();
    private static final byte MASK_EXTRACTION_PATH = maskMaker.nextMask();
    private static final byte MASK_OUTPUT_MODE = maskMaker.nextMask();
    private static final byte MASK_ERROR_MESSAGE = maskMaker.nextMask();
    private static final byte MASK_AUTO_REFRESH = maskMaker.nextMask();

    private byte updated = 0;
    private ErrorCode errorCode;
    private Tag nbt;
    private NBTPath path;
    private NBTExtractorOutputMode outputMode;
    private Component errorMessage;
    private boolean autoRefresh;

    public void updateNBT(Tag nbt) {
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

    public void updateErrorMessage(Component errorMessage) {
        this.errorMessage = errorMessage;
        this.updated |= MASK_ERROR_MESSAGE;
    }

    public void updateAutoRefresh(boolean autoRefresh) {
        this.autoRefresh = autoRefresh;
        this.updated |= MASK_AUTO_REFRESH;
    }

    public boolean isEmpty() {
        return this.updated == 0;
    }

    @Override
    public void fromBytes(FriendlyByteBuf buf) {
        this.updated = buf.readByte();
        if (this.isUpdated(MASK_NBT)) {
            CompoundTag compound = buf.readNbt();
            assert compound != null;
            this.nbt = compound.get("nbt");
        }
        if (this.isUpdated(MASK_ERROR_CODE)) {
            this.errorCode = ErrorCode.values()[buf.readByte()];
        }
        if (this.isUpdated(MASK_EXTRACTION_PATH)) {
            this.path = NBTPath.fromNBT(buf.readNbt()).orElse(new NBTPath());
        }
        if (this.isUpdated(MASK_OUTPUT_MODE)) {
            this.outputMode = NBTExtractorOutputMode.values()[buf.readByte()];
        }
        if (this.isUpdated(MASK_ERROR_MESSAGE)) {
            if (buf.readBoolean()) { // Is null
                this.errorMessage = null;
            } else {
                this.errorMessage = buf.readComponent();
            }
        }
        if (this.isUpdated(MASK_AUTO_REFRESH)) {
            this.autoRefresh = buf.readBoolean();
        }
    }

    private boolean isUpdated(byte mask) {
        return (this.updated & mask) > 0;
    }

    @Override
    public void toBytes(FriendlyByteBuf buf) {
        buf.writeByte(this.updated);
        if (this.isUpdated(MASK_NBT)) {
            CompoundTag compound = new CompoundTag();
            if (this.nbt != null) {
                compound.put("nbt", this.nbt);
            }
            buf.writeNbt(compound);
        }
        if (this.isUpdated(MASK_ERROR_CODE)) {
            buf.writeByte(this.errorCode.ordinal());
        }
        if (this.isUpdated(MASK_EXTRACTION_PATH)) {
            buf.writeNbt(this.path.toNBTCompound());
        }
        if (this.isUpdated(MASK_OUTPUT_MODE)) {
            buf.writeByte(this.outputMode.ordinal());
        }
        if (this.isUpdated(MASK_ERROR_MESSAGE)) {
            if (this.errorMessage == null) { // Is null
                buf.writeBoolean(true);
            } else {
                buf.writeBoolean(false);
                buf.writeComponent(this.errorMessage);
            }
        }
        if (this.isUpdated(MASK_AUTO_REFRESH)) {
            buf.writeBoolean(this.autoRefresh);
        }
    }

    public enum ErrorCode {
        NO_ERROR,
        TYPE_ERROR,
        EVAL_ERROR,
        UNEXPECTED_ERROR,
    }
}
