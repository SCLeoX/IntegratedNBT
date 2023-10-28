package me.tepis.integratednbt;

import me.tepis.integratednbt.network.PacketHandler;
import me.tepis.integratednbt.network.clientbound.NBTExtractorUpdateClientMessage;
import me.tepis.integratednbt.network.clientbound.NBTExtractorUpdateClientMessage.ErrorCode;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.nbt.Tag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.Level;
import net.minecraftforge.network.PacketDistributor;
import org.cyclops.integrateddynamics.api.PartStateException;
import org.cyclops.integrateddynamics.api.evaluate.EvaluationException;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValue;
import org.cyclops.integrateddynamics.api.evaluate.variable.IVariable;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueTypeNbt.ValueNbt;

import javax.annotation.Nonnull;
import java.util.Objects;


public class NBTExtractorContainer extends AbstractContainerMenu {
    /**
     * A slot whose position is determined later.
     */
    private static class ResponsiveSlot extends Slot {
        private int baseX;
        private int baseY;

        public ResponsiveSlot(Container inventoryIn, int index, int baseX, int baseY) {
            super(inventoryIn, index, baseX, baseY);
            this.baseX = baseX;
            this.baseY = baseY;
        }

        public void setOffset(int xPos, int yPos) {
            this.x = this.baseX + xPos;
            this.y = this.baseY + yPos;
        }
    }

    private static class VariableSlot extends ResponsiveSlot {
        public VariableSlot(Container inventoryIn, int index, int baseX, int baseY) {
            super(inventoryIn, index, baseX, baseY);
        }

        @Override
        public boolean mayPlace(ItemStack stack) {
            return Integration.isVariable(stack);
        }

        @Override
        public int getMaxStackSize(ItemStack stack) {
            return 1;
        }
    }

    private static class VarOutSlot extends VariableSlot {
        public VarOutSlot(Container inventoryIn, int index, int baseX, int baseY) {
            super(inventoryIn, index, baseX, baseY);
        }
    }

    private static class SrcNBTSlot extends VariableSlot {
        public SrcNBTSlot(Container inventoryIn, int index, int baseX, int baseY) {
            super(inventoryIn, index, baseX, baseY);
        }
    }

    private static final int SRC_NBT = 0;
    private static final int VAR_OUT = 1;
    private static final int INVENTORY_START = 2;
    private static final int INVENTORY_END = 38; // Exclusive
    private Inventory playerInventory;
    private NBTExtractorBE nbtExtractorEntity;
    private ErrorCode clientErrorCode = null;
    private Wrapper<Tag> clientNBT = null;
    private NBTPath clientPath = null;
    private NBTExtractorOutputMode clientOutputMode = null;
    private Component clientErrorMessage = null;
    private Boolean clientAutoRefresh = null;

    // Logic-client
    public NBTExtractorContainer(
        int windowId,
        Inventory playerInventory,
        FriendlyByteBuf data
    ) {
        this(windowId, playerInventory, getTileEntity(playerInventory, data));
    }

    // Logic-server
    public NBTExtractorContainer(
        int windowId,
        Inventory playerInventory,
        NBTExtractorBE nbtExtractorEntity
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

    private static NBTExtractorBE getTileEntity(
        Inventory playerInventory,
        FriendlyByteBuf data
    ) {
        Objects.requireNonNull(playerInventory);
        Objects.requireNonNull(data);
        BlockEntity tileAtPos = playerInventory.player.level().getBlockEntity(data.readBlockPos());
        if (tileAtPos instanceof NBTExtractorBE) {
            return (NBTExtractorBE) tileAtPos;
        }
        throw new IllegalStateException("Tile entity is not correct: " + tileAtPos);
    }

    public NBTExtractorBE getNbtExtractorEntity() {
        return this.nbtExtractorEntity;
    }

    @Override
    public void broadcastChanges() {
        super.broadcastChanges();
        Level world = this.nbtExtractorEntity.getLevel();
        if (world != null && !world.isClientSide) {
            ErrorCode errorCode;
            Wrapper<Tag> newNBT = this.clientNBT;
            Component errorMessage = null;
            if (!this.getSlot(SRC_NBT).hasItem()) {
                // Client will do this automatically
                errorCode = this.clientErrorCode = null;
            } else {
                try {
                    Wrapper<Tag> frozenValue = this.nbtExtractorEntity.getFrozenValue();
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
                                newNBT = Wrapper.of(((ValueNbt) value).getRawValue().orElse(null));
                                errorCode = ErrorCode.NO_ERROR;
                            } else {
                                errorCode = ErrorCode.TYPE_ERROR;
                            }
                        }
                    } else {
                        errorCode = ErrorCode.NO_ERROR;
                        newNBT = frozenValue;
                    }
                } catch (EvaluationException | PartStateException exception) {
                    exception.printStackTrace();
                    errorCode = ErrorCode.EVAL_ERROR;
                    errorMessage = Component.literal(exception.getMessage());
                } catch (Exception exception) {
                    errorCode = ErrorCode.UNEXPECTED_ERROR;
                    IntegratedNBT.LOGGER.error(
                        "Unexpected error occurred while evaluating variable.",
                        exception
                    );
                }
            }
            NBTExtractorUpdateClientMessage message = new NBTExtractorUpdateClientMessage();
            if (!Objects.equals(this.clientNBT, newNBT)) {
                message.updateNBT(newNBT.get());
                this.clientNBT = newNBT;
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
                ServerPlayer playerMP = (ServerPlayer) this.playerInventory.player;
                PacketHandler.INSTANCE.send(PacketDistributor.PLAYER.with(() -> playerMP), message);
            }
            if (errorCode == ErrorCode.NO_ERROR) {
                this.nbtExtractorEntity.updateLastEvaluatedNBT(newNBT.get());
            } else {
                this.nbtExtractorEntity.updateLastEvaluatedNBT(null);
            }
        }
    }

    public void setSlotOffset(int x, int y) {
        for (Slot slot : this.slots) {
            ((ResponsiveSlot) slot).setOffset(x, y);
        }
    }

    @Override
    public boolean stillValid(@Nonnull Player playerIn) {
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
    public ItemStack quickMoveStack(Player playerIn, int index) {
        ItemStack leftOver = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);
        if (slot != null && slot.hasItem()) {
            ItemStack fromSlot = slot.getItem();
            leftOver = fromSlot.copy();

            if (index == SRC_NBT || index == VAR_OUT) {
                // Transfer to inventory
                if (!this.moveItemStackTo(fromSlot, INVENTORY_START, INVENTORY_END, true)) {
                    return ItemStack.EMPTY;
                }
            } else {
                if (Integration.isVariable(fromSlot)) {
                    if (this.slots.get(SRC_NBT).hasItem()) {
                        if (this.slots.get(VAR_OUT).hasItem()) {
                            return ItemStack.EMPTY;
                        } else {
                            Slot varOutSlot = this.getSlot(VAR_OUT);
                            varOutSlot.set(fromSlot.split(1));
                            varOutSlot.setChanged();
                        }
                    } else {
                        Slot srcNBTSlot = this.getSlot(SRC_NBT);
                        srcNBTSlot.set(fromSlot.split(1));
                        srcNBTSlot.setChanged();
                    }
                } else {
                    return ItemStack.EMPTY;
                }
            }
            if (fromSlot.isEmpty()) {
                slot.set(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }
            if (fromSlot.getCount() == leftOver.getCount()) {
                return ItemStack.EMPTY;
            }
            slot.onTake(playerIn, fromSlot);
        }
        return leftOver;
    }
}
