package me.tepis.integratednbt;

import me.tepis.integratednbt.NBTExtractorTileEntity.NetworkElement;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.ItemStackHelper;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;
import org.cyclops.cyclopscore.datastructure.DimPos;
import org.cyclops.cyclopscore.datastructure.EnumFacingMap;
import org.cyclops.cyclopscore.helper.MinecraftHelpers;
import org.cyclops.cyclopscore.persist.nbt.NBTClassType;
import org.cyclops.integrateddynamics.IntegratedDynamics;
import org.cyclops.integrateddynamics.api.block.cable.ICable;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValue;
import org.cyclops.integrateddynamics.api.evaluate.variable.IVariable;
import org.cyclops.integrateddynamics.api.item.IValueTypeVariableFacade;
import org.cyclops.integrateddynamics.api.item.IVariableFacade;
import org.cyclops.integrateddynamics.api.item.IVariableFacadeHandlerRegistry;
import org.cyclops.integrateddynamics.api.item.IVariableFacadeHandlerRegistry.IVariableFacadeFactory;
import org.cyclops.integrateddynamics.api.network.IEventListenableNetworkElement;
import org.cyclops.integrateddynamics.api.network.INetwork;
import org.cyclops.integrateddynamics.api.network.INetworkElement;
import org.cyclops.integrateddynamics.api.network.INetworkEventListener;
import org.cyclops.integrateddynamics.api.network.IPartNetwork;
import org.cyclops.integrateddynamics.api.network.IPositionedAddonsNetwork;
import org.cyclops.integrateddynamics.api.network.event.INetworkEvent;
import org.cyclops.integrateddynamics.capability.network.NetworkCarrierDefault;
import org.cyclops.integrateddynamics.capability.networkelementprovider.NetworkElementProviderSingleton;
import org.cyclops.integrateddynamics.capability.path.PathElementTile;
import org.cyclops.integrateddynamics.capability.variablecontainer.VariableContainerDefault;
import org.cyclops.integrateddynamics.core.evaluate.InventoryVariableEvaluator;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueTypeOperator.ValueOperator;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueTypes;
import org.cyclops.integrateddynamics.core.helper.CableHelpers;
import org.cyclops.integrateddynamics.core.helper.NetworkHelpers;
import org.cyclops.integrateddynamics.core.item.ValueTypeVariableFacade;
import org.cyclops.integrateddynamics.core.network.event.VariableContentsUpdatedEvent;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public class NBTExtractorTileEntity extends TileEntity implements ICapabilityProvider,
    INetworkEventListener<NetworkElement>, ITickableTileEntity, INamedContainerProvider,
    IInventory {
    class CableCapability implements ICable {
        @Override
        public boolean canConnect(ICable connector, Direction side) {
            return true;
        }

        @Override
        public boolean isConnected(Direction side) {
            if (NBTExtractorTileEntity.this.connected.isEmpty()) {
                this.updateConnections();
            }
            return NBTExtractorTileEntity.this.connected.get(side);
        }

        @Override
        public void updateConnections() {
            NBTExtractorTileEntity entity = NBTExtractorTileEntity.this;
            World world = entity.getWorld();
            if (world == null) {
                return;
            }
            for (Direction side : Direction.values()) {
                boolean cableConnected = CableHelpers.canCableConnectTo(
                    world,
                    entity.getPos(),
                    side,
                    this
                );
                entity.connected.put(side, cableConnected);
            }
            world.markChunkDirty(entity.getPos(), entity);
            BlockState blockState = world.getBlockState(NBTExtractorTileEntity.this.pos);
            world.notifyBlockUpdate(NBTExtractorTileEntity.this.pos, blockState, blockState, 3);
        }

        @Override
        public void disconnect(Direction side) {}

        @Override
        public void reconnect(Direction side) {}

        @Override
        public ItemStack getItemStack() {
            return new ItemStack(Additions.NBT_EXTRACTOR_BLOCK_ITEM.get());
        }

        @Override
        public void destroy() {}
    }

    class NetworkElement implements IEventListenableNetworkElement<NBTExtractorTileEntity> {
        @Override
        public int getUpdateInterval() {
            return 0;
        }

        @Override
        public boolean isUpdate() {
            return false;
        }

        @Override
        public void update(INetwork network) {}

        @Override
        public void beforeNetworkKill(INetwork network) {}

        @Override
        public void afterNetworkAlive(INetwork network) {}

        @Override
        public void afterNetworkReAlive(INetwork network) {
            NBTExtractorTileEntity.this.afterNetworkReAlive();
        }

        @Override
        public void addDrops(
            List<ItemStack> itemStacks,
            boolean dropMainElement,
            boolean saveState
        ) {}

        @Override
        public boolean onNetworkAddition(INetwork network) {
            if (NBTExtractorTileEntity.this.world == null) {
                return false;
            }
            return NetworkHelpers.getPartNetwork(network).map(partNetwork -> partNetwork
                .addVariableContainer(DimPos.of(
                    NBTExtractorTileEntity.this.world,
                    NBTExtractorTileEntity.this.pos
                ))
            ).orElse(false);
        }

        @Override
        public void onNetworkRemoval(INetwork network) {
            if (NBTExtractorTileEntity.this.world == null) {
                return;
            }
            NetworkHelpers.getPartNetwork(network).ifPresent(partNetwork -> partNetwork
                .removeVariableContainer(DimPos.of(
                    NBTExtractorTileEntity.this.world,
                    NBTExtractorTileEntity.this.pos
                ))
            );
        }

        @Override
        public void onPreRemoved(INetwork network) {}

        @Override
        public void onPostRemoved(INetwork network) {}

        @Override
        public void onNeighborBlockChange(
            @Nullable INetwork network,
            IBlockReader world,
            Block neighbourBlock,
            BlockPos neighbourBlockPos
        ) {}

        @Override
        public void setPriorityAndChannel(INetwork network, int priority, int channel) {}

        @Override
        public int getPriority() {
            return 0;
        }

        @Override
        public int getChannel() {
            return IPositionedAddonsNetwork.DEFAULT_CHANNEL;
        }

        @Override
        public void invalidate(INetwork network) {
            network.invalidateElement(this);
        }

        @Override
        @SuppressWarnings("deprecation")
        public boolean canRevalidate(INetwork network) {
            if (NBTExtractorTileEntity.this.world == null) {
                return false;
            }
            return NBTExtractorTileEntity.this.world
                .isBlockLoaded(NBTExtractorTileEntity.this.getPos());
        }

        @Override
        public void revalidate(INetwork network) {
            network.revalidateElement(this);
        }

        @Override
        public int compareTo(INetworkElement o) {
            return this.getClass()
                .getCanonicalName()
                .compareTo(o.getClass().getCanonicalName());
        }

        @Nullable
        @Override
        public Optional<NBTExtractorTileEntity> getNetworkEventListener() {
            return Optional.of(NBTExtractorTileEntity.this);
        }
    }

    public static final int SRC_NBT_SLOT = 0;
    public static final int VAR_OUT_SLOT = 1;
    private EnumFacingMap<Boolean> connected = EnumFacingMap.newMap();
    private CableCapability cableCapability = new CableCapability();
    private NetworkCarrierDefault networkCarrierCapability = new NetworkCarrierDefault();
    private PathElementTile<NBTExtractorTileEntity> pathElementCapability = new PathElementTile<>(
        this,
        this.cableCapability
    );
    private VariableContainerDefault variableContainerCapability = new VariableContainerDefault();
    private NetworkElementProviderSingleton networkElementProviderCapability =
        new NetworkElementProviderSingleton() {
            @Override
            public INetworkElement createNetworkElement(World world, BlockPos blockPos) {
                return new NetworkElement();
            }
        };
    private NonNullList<ItemStack> itemStacks = NonNullList.withSize(2, ItemStack.EMPTY);
    private InventoryVariableEvaluator<IValue> evaluator = new InventoryVariableEvaluator<>(
        this,
        SRC_NBT_SLOT,
        ValueTypes.CATEGORY_ANY
    );
    /**
     * A set of expanded paths in this extractor;
     * <p>
     * This is client-side only and it is not persisted.
     */
    private HashSet<NBTPath> expandedPaths;
    /**
     * How much has the user scrolled in this extractor;
     * <p>
     * This is client-side only and it is not persisted.
     */
    private Wrapper<Integer> scrollTop = new Wrapper<>(0);
    /**
     * Whether should run refreshVariable on next tick
     */
    private boolean shouldRefreshVariable = false;
    /**
     * Whether should send update on next tick
     */
    private boolean shouldUpdateOutVariable = false;
    private NBTPath extractionPath = new NBTPath();
    private byte defaultNBTId = 1;
    private NBTExtractorOutputMode outputMode = NBTExtractorOutputMode.REFERENCE;
    private INBT lastEvaluatedNBT = null;
    // If null, then there is no frozen value available
    private Wrapper<INBT> frozenNBT = null;
    private boolean autoRefresh = true;
    /**
     * The item stack that yielded the current frozen NBT
     */
    private ItemStack frozenNBTItemStack = ItemStack.EMPTY;

    public NBTExtractorTileEntity() {
        super(Additions.NBT_EXTRACTOR_TILE_ENTITY.get());
        this.expandedPaths = new HashSet<>();
        this.expandedPaths.add(new NBTPath());
    }

    public NBTExtractorOutputMode getOutputMode() {
        return this.outputMode;
    }

    public void setOutputMode(NBTExtractorOutputMode outputMode) {
        this.outputMode = outputMode;
        this.markDirty();
    }

    @Override
    public void markDirty() {
        super.markDirty();
        if (this.world == null) {
            return;
        }
        if (!this.world.isRemote) {
            this.refreshVariables(true);
            this.shouldUpdateOutVariable = true;
            if (!this.autoRefresh && !ItemStack.areItemStacksEqual(
                this.getStackInSlot(SRC_NBT_SLOT),
                this.frozenNBTItemStack
            )) {
                this.frozenNBTItemStack = this.getStackInSlot(SRC_NBT_SLOT);
                this.frozenNBT = null;
            }
        }
    }

    public void refreshVariables(boolean sendVariablesUpdateEvent) {
        this.evaluator.refreshVariable(
            this.networkCarrierCapability.getNetwork(),
            sendVariablesUpdateEvent
        );
        this.variableContainerCapability.refreshVariables(
            this.networkCarrierCapability.getNetwork(),
            this,
            sendVariablesUpdateEvent
        );
    }

    @Override
    @Nonnull
    public ItemStack getStackInSlot(int index) {
        return this.itemStacks.get(index);
    }

    public void setDefaultNBTId(byte defaultNBTId) {
        if (defaultNBTId < 1 || defaultNBTId > 12) {
            this.defaultNBTId = 1;
        } else {
            this.defaultNBTId = defaultNBTId;
        }
        this.markDirty();
    }

    public NBTPath getExtractionPath() {
        return this.extractionPath;
    }

    public void setExtractionPath(NBTPath extractionPath) {
        this.extractionPath = extractionPath;
        this.markDirty();
    }

    public boolean isAutoRefresh() {
        return this.autoRefresh;
    }

    public void updateAutoRefresh(boolean autoRefresh) {
        if (this.autoRefresh == autoRefresh) {
            return;
        }
        this.autoRefresh = autoRefresh;
        if (!autoRefresh) {
            this.frozenNBT = null;
            this.frozenNBTItemStack = ItemStack.EMPTY;
        }
        this.markDirty();
    }

    public void updateLastEvaluatedNBT(INBT lastEvaluatedNBT) {
        this.lastEvaluatedNBT = lastEvaluatedNBT;
        if (!this.autoRefresh && this.frozenNBT == null) {
            this.frozenNBT = Wrapper.of(this.lastEvaluatedNBT);
            this.frozenNBTItemStack = this.getStackInSlot(SRC_NBT_SLOT).copy();
        }
    }

    public HashSet<NBTPath> getExpandedPaths() {
        return this.expandedPaths;
    }

    public Wrapper<Integer> getScrollTop() {
        return this.scrollTop;
    }

    @SuppressWarnings("ConstantConditions")
    public IVariable<?> getSrcNBTVariable() {
        IPartNetwork partNetwork =
            NetworkHelpers.getPartNetwork(this.networkCarrierCapability.getNetwork())
                .orElse(null);
        if (partNetwork == null) {
            return null;
        }
        return this.evaluator.getVariable(partNetwork);
    }

    public ITextComponent getFirstErrorMessage() {
        List<ITextComponent> errors = this.evaluator.getErrors();
        if (errors.isEmpty()) {
            return null;
        } else {
            return errors.get(0);
        }
    }

    @Nonnull
    @Override
    @SuppressWarnings("unchecked")
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> capability, Direction facing) {
        if (capability == Capabilities.CABLE_CAPABILITY) {
            return LazyOptional.of(() -> (T) this.cableCapability);
        } else if (capability == Capabilities.NETWORK_CARRIER_CAPABILITY) {
            return LazyOptional.of(() -> (T) this.networkCarrierCapability);
        } else if (capability == Capabilities.PATH_ELEMENT_CAPABILITY) {
            return LazyOptional.of(() -> (T) this.pathElementCapability);
        } else if (capability == Capabilities.VARIABLE_CONTAINER_CAPABILITY) {
            return LazyOptional.of(() -> (T) this.variableContainerCapability);
        } else if (capability == Capabilities.NETWORK_ELEMENT_PROVIDER) {
            return LazyOptional.of(() -> (T) this.networkElementProviderCapability);
        }
        return super.getCapability(capability, facing);
    }

    @Override
    public int getSizeInventory() {
        return this.itemStacks.size();
    }

    @Override
    public boolean isEmpty() {
        return false;
    }

    @Override
    @Nonnull
    public ItemStack decrStackSize(int index, int count) {
        return ItemStackHelper.getAndSplit(this.itemStacks, index, count);
    }

    @Override
    @Nonnull
    public ItemStack removeStackFromSlot(int index) {
        return ItemStackHelper.getAndRemove(this.itemStacks, index);
    }

    @Override
    public void setInventorySlotContents(int index, @Nonnull ItemStack stack) {
        this.itemStacks.set(index, stack);
        if (stack.getCount() > this.getInventoryStackLimit()) {
            stack.setCount(this.getInventoryStackLimit());
        }
    }

    @Override
    public int getInventoryStackLimit() {
        return 1;
    }

    @Override
    public boolean isUsableByPlayer(@Nonnull PlayerEntity player) {
        if (this.world == null) {
            return false;
        }
        if (this.world.getTileEntity(this.pos) != this) {
            return false;
        } else {
            if (player.getDistanceSq(
                (double) this.pos.getX() + 0.5D,
                (double) this.pos.getY() + 0.5D,
                (double) this.pos.getZ() + 0.5D
            ) <= 64.0D) {
                return true;
            }
            return (this.isRemote(player.getHeldItemMainhand()) ||
                this.isRemote(player.getHeldItemOffhand()));
        }
    }

    /**
     * Tests whether the given item stack is a remote for this NBT Extractor.
     */
    private boolean isRemote(ItemStack itemStack) {
        if (this.world == null) {
            return false;
        }
        if (itemStack.getItem() != Additions.NBT_EXTRACTOR_REMOTE.get()) {
            return false;
        }
        CompoundNBT tag = Additions.NBT_EXTRACTOR_REMOTE.get().getModNBT(itemStack);
        return (tag.contains("world")) &&
            (tag.getInt("world") == this.world.getDimension().getType().getId()) &&
            (tag.getInt("x") == this.pos.getX()) &&
            (tag.getInt("y") == this.pos.getY()) &&
            (tag.getInt("z") == this.pos.getZ());
    }

    @Override
    public void openInventory(@Nonnull PlayerEntity player) {

    }

    @Override
    public void closeInventory(@Nonnull PlayerEntity player) {

    }

    @Override
    public boolean isItemValidForSlot(int index, @Nonnull ItemStack stack) {
        return Integration.isVariable(stack);
    }

    public Wrapper<INBT> getFrozenValue() {
        if (this.autoRefresh) {
            return null;
        } else {
            return this.frozenNBT;
        }
    }

    @Override
    public void clear() {
        this.itemStacks.clear();
    }

    @Override
    public boolean hasEventSubscriptions() {
        return true;
    }

    @Override
    public Set<Class<? extends INetworkEvent>> getSubscribedEvents() {
        return Collections.singleton(VariableContentsUpdatedEvent.class);
    }

    @Override
    public void onEvent(
        INetworkEvent event, NetworkElement networkElement
    ) {
        if (event instanceof VariableContentsUpdatedEvent) {
            this.refreshVariables(false);
        }
    }

    @Override
    public void onLoad() {
        super.onLoad();
        if (!MinecraftHelpers.isClientSide()) {
            this.shouldRefreshVariable = true;
        }
    }

    @Override
    @Nonnull
    public CompoundNBT write(CompoundNBT tag) {
        super.write(tag);
        ListNBT errorsList = new ListNBT();
        NBTClassType.writeNbt(List.class, "errors", this.evaluator.getErrors(), tag);
        tag.put("errors", errorsList);
        tag.put("path", this.extractionPath.toNBT());
        tag.putByte("defaultNBTId", this.defaultNBTId);
        tag.putByte("outputMode", (byte) this.outputMode.ordinal());
        tag.putBoolean("isAutoRefresh", this.autoRefresh);
        if (!this.autoRefresh) {
            // frozenNBT = null:
            // { ... }
            //
            // frozenNBT = Wrapper.of(null):
            // { ..., frozenNBT: {} }
            //
            // frozenNBT = Wrapper.of(something):
            // {..., frozenNBT: { value: something } }

            if (this.frozenNBT != null) {
                CompoundNBT compound = new CompoundNBT();
                if (this.frozenNBT.get() != null) {
                    compound.put("value", this.frozenNBT.get());
                }
                tag.put("frozenNBT", compound);
            }
            tag.put(
                "frozenNBTItemStack",
                this.frozenNBTItemStack.write(new CompoundNBT())
            );
        }
        ItemStackHelper.saveAllItems(tag, this.itemStacks);
        return tag;
    }

    @Nonnull
    @Override
    public ITextComponent getDisplayName() {
        return new TranslationTextComponent("tile.integratednbt:nbt_extractor.name");
    }

    @Override
    @SuppressWarnings("unchecked")
    public void read(CompoundNBT tag) {
        if (tag.contains("errors")) {
            this.evaluator.setErrors(NBTClassType.readNbt(List.class, "errors", tag));
        }
        if (tag.contains("path")) {
            this.extractionPath = NBTPath.fromNBT(tag.get("path")).orElse(new NBTPath());
        }
        if (tag.contains("defaultNBTId")) {
            this.defaultNBTId = tag.getByte("defaultNBTId");
        }
        if (tag.contains("outputMode")) {
            this.outputMode = NBTExtractorOutputMode.values()[tag.getByte("outputMode")];
        }
        if (tag.contains("isAutoRefresh")) {
            this.autoRefresh = tag.getBoolean("isAutoRefresh");
            if (!this.autoRefresh) {
                if (tag.contains("frozenNBT")) {
                    this.frozenNBT = Wrapper.of(tag.getCompound("frozenNBT").get("value"));
                }
                this.frozenNBTItemStack = ItemStack.read(tag.getCompound("frozenNBTItemStack"));
            }
        }
        ItemStackHelper.loadAllItems(tag, this.itemStacks);
        this.shouldRefreshVariable = true;
        super.read(tag);
    }

    public void afterNetworkReAlive() {
        this.shouldRefreshVariable = true;
        this.connected.clear();
    }

    @Nullable
    @Override
    public Container createMenu(
        int windowId,
        @Nonnull PlayerInventory inventory,
        @Nonnull PlayerEntity player
    ) {
        return new NBTExtractorContainer(windowId, inventory, this);
    }

    @Override
    public void tick() {
        if (this.world == null) {
            return;
        }
        if (!this.world.isRemote) {
            if (this.shouldRefreshVariable && this.networkCarrierCapability.getNetwork() != null) {
                this.shouldRefreshVariable = false;
                this.refreshVariables(true);
            }
            if (this.shouldUpdateOutVariable) {
                this.updateOutVariable();
            }
        }
    }

    private void updateOutVariable() {
        if (!this.itemStacks.get(VAR_OUT_SLOT).isEmpty()) {
            ItemStack result = this.outputMode.writeItemStack(
                () -> {
                    this.refreshVariables(true);
                    return this.evaluator.getVariableFacade();
                },
                this.itemStacks.get(VAR_OUT_SLOT),
                (!this.autoRefresh && this.frozenNBT != null)
                    ? this.frozenNBT.get()
                    : this.lastEvaluatedNBT,
                this.extractionPath,
                this.defaultNBTId,
                this.getBlockState()
            );
            if (result != null) {
                this.itemStacks.set(VAR_OUT_SLOT, result);
            }
        }
    }
}
