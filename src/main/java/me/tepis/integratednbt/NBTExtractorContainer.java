package me.tepis.integratednbt;

import me.tepis.integratednbt.network.PacketHandler;
import me.tepis.integratednbt.network.clientbound.NBTExtractorUpdateClientMessage;
import me.tepis.integratednbt.network.clientbound.NBTExtractorUpdateClientMessage.ErrorCode;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.INBT;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.World;
import net.minecraftforge.fml.network.PacketDistributor;
import org.cyclops.integrateddynamics.api.PartStateException;
import org.cyclops.integrateddynamics.api.evaluate.EvaluationException;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValue;
import org.cyclops.integrateddynamics.api.evaluate.variable.IVariable;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueTypeNbt.ValueNbt;

import javax.annotation.Nonnull;
import java.lang.reflect.Field;
import java.util.Objects;


public class NBTExtractorContainer extends Container {
    /**
     * A slot whose position is determined later.
     */
    private static class ResponsiveSlot extends Slot {
        private int baseX;
        private int baseY;

        public ResponsiveSlot(IInventory inventoryIn, int index, int baseX, int baseY) {
            super(inventoryIn, index, baseX, baseY);
            this.baseX = baseX;
            this.baseY = baseY;
        }

        public void setOffset(int xPos, int yPos) {
            this.xPos = this.baseX + xPos;
            this.yPos = this.baseY + yPos;
        }
    }

    private static class VariableSlot extends ResponsiveSlot {
        public VariableSlot(IInventory inventoryIn, int index, int baseX, int baseY) {
            super(inventoryIn, index, baseX, baseY);
        }

        @Override
        public boolean isItemValid(ItemStack stack) {
            return Integration.isVariable(stack);
        }

        @Override
        public int getItemStackLimit(ItemStack stack) {
            return 1;
        }
    }

    private static class VarOutSlot extends VariableSlot {
        public VarOutSlot(IInventory inventoryIn, int index, int baseX, int baseY) {
            super(inventoryIn, index, baseX, baseY);
        }
    }

    private static class SrcNBTSlot extends VariableSlot {
        public SrcNBTSlot(IInventory inventoryIn, int index, int baseX, int baseY) {
            super(inventoryIn, index, baseX, baseY);
        }
    }

    private static final int SRC_NBT = 0;
    private static final int VAR_OUT = 1;
    private static final int INVENTORY_START = 2;
    private static final int INVENTORY_END = 38; // Exclusive
    private PlayerInventory playerInventory;
    private NBTExtractorTileEntity nbtExtractorEntity;
    private ErrorCode clientErrorCode = null;
    private INBT clientNBT = null;
    private NBTPath clientPath = null;
    private NBTExtractorOutputMode clientOutputMode = null;
    private ITextComponent clientErrorMessage = null;
    private Boolean clientAutoRefresh = null;

    // Logic-client
    public NBTExtractorContainer(
        int windowId,
        PlayerInventory playerInventory,
        PacketBuffer data
    ) {
        this(windowId, playerInventory, getTileEntity(playerInventory, data));
    }

    // Logic-server
    public NBTExtractorContainer(
        int windowId,
        PlayerInventory playerInventory,
        NBTExtractorTileEntity nbtExtractorEntity
    ) {
        super(Additions.NBT_EXTRACTOR_CONTAINER.get(), windowId);
        this.playerInventory = playerInventory;
        this.nbtExtractorEntity = nbtExtractorEntity;
        this.addSlot(new SrcNBTSlot(nbtExtractorEntity, SRC_NBT, 9, 6));
        this.addSlot(new VarOutSlot(nbtExtractorEntity, VAR_OUT, 153, 6));
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 9; ++j) {
                this.addSlot(new ResponsiveSlot(
                    playerInventory,
                    j + i * 9 + 9,
                    9 + j * 18,
                    28 + i * 18
                ));
            }
        }
        for (int i = 0; i < 9; i++) {
            this.addSlot(new ResponsiveSlot(playerInventory, i, 9 + i * 18, 86));
        }
    }

    private static NBTExtractorTileEntity getTileEntity(
        PlayerInventory playerInventory,
        PacketBuffer data
    ) {
        Objects.requireNonNull(playerInventory);
        Objects.requireNonNull(data);
        TileEntity tileAtPos = playerInventory.player.world.getTileEntity(data.readBlockPos());
        if (tileAtPos instanceof NBTExtractorTileEntity) {
            return (NBTExtractorTileEntity) tileAtPos;
        }
        throw new IllegalStateException("Tile entity is not correct: " + tileAtPos);
    }

    public NBTExtractorTileEntity getNbtExtractorEntity() {
        return this.nbtExtractorEntity;
    }

    @Override
    public void detectAndSendChanges() {
        super.detectAndSendChanges();
        World world = this.nbtExtractorEntity.getWorld();
        if (world != null && !world.isRemote) {
            ErrorCode errorCode;
            INBT nbt = this.clientNBT;
            ITextComponent errorMessage = null;
            if (!this.getSlot(SRC_NBT).getHasStack()) {
                // Client will do this automatically
                errorCode = this.clientErrorCode = null;
            } else {
                try {
                    INBT frozenValue = this.nbtExtractorEntity.getFrozenValue();
                    if (frozenValue == null) {
                        // If there is no frozen value (either frozen mode is not on, or it is
                        // on, but a value has not been evaluated yet.)
                        IVariable<?> variable = this.nbtExtractorEntity.getSrcNBTVariable();
                        if (variable == null) {
                            errorCode = ErrorCode.EVAL_ERROR;
                            errorMessage = this.nbtExtractorEntity.getFirstErrorMessage();
                        } else {
                            IValue value = variable.getValue();
                            if (value instanceof ValueNbt) {
                                nbt = ((ValueNbt) value).getRawValue().orElse(null);
                                errorCode = ErrorCode.NO_ERROR;
                            } else {
                                errorCode = ErrorCode.TYPE_ERROR;
                            }
                        }
                    } else {
                        errorCode = ErrorCode.NO_ERROR;
                        nbt = frozenValue;
                    }
                } catch (EvaluationException | PartStateException exception) {
                    exception.printStackTrace();
                    errorCode = ErrorCode.EVAL_ERROR;
                    errorMessage = new StringTextComponent(exception.getMessage());
                } catch (Exception exception) {
                    errorCode = ErrorCode.UNEXPECTED_ERROR;
                    IntegratedNBT.LOGGER.error(
                        "Unexpected error occurred while evaluating variable.",
                        exception
                    );
                }
            }
            NBTExtractorUpdateClientMessage message = new NBTExtractorUpdateClientMessage();
            if (!Objects.equals(this.clientNBT, nbt)) {
                message.updateNBT(nbt);
                this.clientNBT = nbt;
            }
            if (this.clientErrorCode != errorCode) {
                message.updateErrorCode(errorCode);
                this.clientErrorCode = errorCode;
            }
            NBTPath nbtPath = this.nbtExtractorEntity.getExtractionPath();
            if (this.clientPath != nbtPath) {
                message.updateExtractionPath(nbtPath);
                this.clientPath = nbtPath;
            }
            NBTExtractorOutputMode outputMode = this.nbtExtractorEntity.getOutputMode();
            if (this.clientOutputMode != outputMode) {
                message.updateOutputMode(outputMode);
                this.clientOutputMode = outputMode;
            }
            if (!Objects.equals(errorMessage, this.clientErrorMessage)) {
                message.updateErrorMessage(errorMessage);
                this.clientErrorMessage = errorMessage;
            }
            if (this.clientAutoRefresh == null ||
                this.nbtExtractorEntity.isAutoRefresh() != this.clientAutoRefresh) {
                message.updateAutoRefresh(this.nbtExtractorEntity.isAutoRefresh());
                this.clientAutoRefresh = this.nbtExtractorEntity.isAutoRefresh();
            }
            if (!message.isEmpty()) {
                ServerPlayerEntity playerMP = (ServerPlayerEntity) this.playerInventory.player;
                PacketHandler.INSTANCE.send(PacketDistributor.PLAYER.with(() -> playerMP), message);
            }
            if (errorCode == ErrorCode.NO_ERROR) {
                this.nbtExtractorEntity.updateLastEvaluatedNBT(nbt);
            } else {
                this.nbtExtractorEntity.updateLastEvaluatedNBT(null);
            }
        }
    }

    public void setSlotOffset(int x, int y) {
        for (Slot slot : this.inventorySlots) {
            ((ResponsiveSlot) slot).setOffset(x, y);
        }
    }

    @Override
    public boolean canInteractWith(@Nonnull PlayerEntity playerIn) {
        return true;
    }

    @Nonnull
    public Slot getSrcNBTSlot() {
        return this.getSlot(SRC_NBT);
    }

    @Nonnull
    public Slot getVarOutSlot() {
        return this.getSlot(VAR_OUT);
    }

    @Nonnull
    public ItemStack transferStackInSlot(PlayerEntity playerIn, int index) {
        ItemStack leftOver = ItemStack.EMPTY;
        Slot slot = this.inventorySlots.get(index);
        if (slot != null && slot.getHasStack()) {
            ItemStack fromSlot = slot.getStack();
            leftOver = fromSlot.copy();

            if (index == SRC_NBT || index == VAR_OUT) {
                // Transfer to inventory
                if (!this.mergeItemStack(fromSlot, INVENTORY_START, INVENTORY_END, true)) {
                    return ItemStack.EMPTY;
                }
            } else {
                if (Integration.isVariable(fromSlot)) {
                    if (this.inventorySlots.get(SRC_NBT).getHasStack()) {
                        if (this.inventorySlots.get(VAR_OUT).getHasStack()) {
                            return ItemStack.EMPTY;
                        } else {
                            Slot varOutSlot = this.getSlot(VAR_OUT);
                            varOutSlot.putStack(fromSlot.split(1));
                            varOutSlot.onSlotChanged();
                        }
                    } else {
                        Slot srcNBTSlot = this.getSlot(SRC_NBT);
                        srcNBTSlot.putStack(fromSlot.split(1));
                        srcNBTSlot.onSlotChanged();
                    }
                } else {
                    return ItemStack.EMPTY;
                }
            }
            if (fromSlot.isEmpty()) {
                slot.putStack(ItemStack.EMPTY);
            } else {
                slot.onSlotChanged();
            }
            if (fromSlot.getCount() == leftOver.getCount()) {
                return ItemStack.EMPTY;
            }
            slot.onTake(playerIn, fromSlot);
        }
        return leftOver;
    }
}
