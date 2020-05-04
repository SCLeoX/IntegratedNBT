package me.tepis.integratednbt;

import me.tepis.integratednbt.NBTExtractorUpdateTreeMessage.ErrorCode;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import org.cyclops.integrateddynamics.api.PartStateException;
import org.cyclops.integrateddynamics.api.evaluate.EvaluationException;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValue;
import org.cyclops.integrateddynamics.api.evaluate.variable.IVariable;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueTypeNbt.ValueNbt;

import javax.annotation.Nonnull;
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
    private InventoryPlayer playerInventory;
    private NBTExtractorTileEntity nbtExtractorEntity;
    private ErrorCode lastSentErrorCode = null;
    private NBTTagCompound lastSentNBTCompound = null;

    public NBTExtractorContainer(
        InventoryPlayer playerInventory,
        NBTExtractorTileEntity nbtExtractorEntity
    ) {
        this.playerInventory = playerInventory;
        this.nbtExtractorEntity = nbtExtractorEntity;
        this.addSlotToContainer(new SrcNBTSlot(nbtExtractorEntity, SRC_NBT, 9, 6));
        this.addSlotToContainer(new VarOutSlot(nbtExtractorEntity, VAR_OUT, 153, 6));
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 9; ++j) {
                this.addSlotToContainer(new ResponsiveSlot(
                    playerInventory,
                    j + i * 9 + 9,
                    9 + j * 18,
                    28 + i * 18
                ));
            }
        }
        for (int i = 0; i < 9; i++) {
            this.addSlotToContainer(new ResponsiveSlot(playerInventory, i, 9 + i * 18, 86));
        }
    }

    public NBTExtractorTileEntity getNbtExtractorEntity() {
        return this.nbtExtractorEntity;
    }

    @Override
    public void detectAndSendChanges() {
        super.detectAndSendChanges();
        if (!this.nbtExtractorEntity.getWorld().isRemote) {
            if (!this.getSlot(SRC_NBT).getHasStack()) {
                this.lastSentErrorCode = null;
            } else {
                if (!(this.playerInventory.player instanceof EntityPlayerMP)) {
                    IntegratedNBT.getLogger()
                        .error("Failed to cast EntityPlayer to EntityPlayerMP.");
                } else {
                    EntityPlayerMP playerMP = (EntityPlayerMP) this.playerInventory.player;
                    NBTTagCompound nbtTagCompound = null;
                    ErrorCode errorCode;
                    try {
                        IVariable<?> variable = this.nbtExtractorEntity.getSrcNBTVariable();
                        if (variable == null) {
                            errorCode = ErrorCode.EVAL_ERROR;
                        } else {
                            IValue value = variable.getValue();
                            if (value instanceof ValueNbt) {
                                nbtTagCompound = ((ValueNbt) value).getRawValue();
                                errorCode = ErrorCode.NO_ERROR;
                            } else {
                                errorCode = ErrorCode.TYPE_ERROR;
                            }
                        }
                    } catch (EvaluationException | PartStateException exception) {
                        exception.printStackTrace();
                        errorCode = ErrorCode.EVAL_ERROR;
                    } catch (Exception exception) {
                        errorCode = ErrorCode.UNEXPECTED_ERROR;
                        IntegratedNBT.getLogger()
                            .error(
                                "Unexpected error occurred while evaluating variable.",
                                exception
                            );
                    }
                    if (errorCode != this.lastSentErrorCode
                        || !Objects.equals(nbtTagCompound, this.lastSentNBTCompound)
                    ) {
                        this.lastSentErrorCode = errorCode;
                        this.lastSentNBTCompound = nbtTagCompound;
                        IntegratedNBT.getNetworkChannel()
                            .sendTo(new NBTExtractorUpdateTreeMessage(
                                errorCode,
                                nbtTagCompound
                            ), playerMP);
                    }
                }
            }
        }
    }

    public void setSlotOffset(int x, int y) {
        for (Slot slot : this.inventorySlots) {
            ((ResponsiveSlot) slot).setOffset(x, y);
        }
    }

    @Override
    public boolean canInteractWith(@Nonnull EntityPlayer playerIn) {
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
    public ItemStack transferStackInSlot(EntityPlayer playerIn, int index) {
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
                            varOutSlot.putStack(fromSlot.splitStack(1));
                            varOutSlot.onSlotChanged();
                        }
                    } else {
                        Slot srcNBTSlot = this.getSlot(SRC_NBT);
                        srcNBTSlot.putStack(fromSlot.splitStack(1));
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
