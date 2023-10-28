package me.tepis.integratednbt;

import me.tepis.integratednbt.NBTExtractorBE.NetworkElement;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;
import org.cyclops.cyclopscore.datastructure.DimPos;
import org.cyclops.cyclopscore.datastructure.EnumFacingMap;
import org.cyclops.cyclopscore.helper.MinecraftHelpers;
import org.cyclops.cyclopscore.persist.nbt.NBTClassType;
import org.cyclops.integrateddynamics.api.block.cable.ICable;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValue;
import org.cyclops.integrateddynamics.api.evaluate.variable.IVariable;
import org.cyclops.integrateddynamics.api.evaluate.variable.ValueDeseralizationContext;
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
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueTypes;
import org.cyclops.integrateddynamics.core.helper.CableHelpers;
import org.cyclops.integrateddynamics.core.helper.NetworkHelpers;
import org.cyclops.integrateddynamics.core.network.event.VariableContentsUpdatedEvent;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public class NBTExtractorBE extends BlockEntity implements ICapabilityProvider,
    INetworkEventListener<NetworkElement>, MenuProvider,
    Container {
    class CableCapability implements ICable {
        @Override
        public boolean canConnect(ICable connector, Direction side) {
            return true;
        }

        @Override
        public boolean isConnected(Direction side) {
            if (NBTExtractorBE.this.connected.isEmpty()) {
                this.updateConnections();
            }
            return NBTExtractorBE.this.connected.get(side);
        }

        @Override
        public void updateConnections() {
            NBTExtractorBE entity = NBTExtractorBE.this;
            Level world = entity.getLevel();
            if (world == null) {
                return;
            }
            for (Direction side : Direction.values()) {
                boolean cableConnected = CableHelpers.canCableConnectTo(
                    world,
                    entity.getBlockPos(),
                    side,
                    this
                );
                entity.connected.put(side, cableConnected);
            }
            world.blockEntityChanged(entity.getBlockPos());
            BlockState blockState = world.getBlockState(NBTExtractorBE.this.worldPosition);
            world.sendBlockUpdated(NBTExtractorBE.this.worldPosition, blockState, blockState, 3);
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

    class NetworkElement implements IEventListenableNetworkElement<NBTExtractorBE> {
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
            NBTExtractorBE.this.afterNetworkReAlive();
        }

        @Override
        public void addDrops(
            List<ItemStack> itemStacks,
            boolean dropMainElement,
            boolean saveState
        ) {}

        @Override
        public boolean onNetworkAddition(INetwork network) {
            if (NBTExtractorBE.this.level == null) {
                return false;
            }
            return NetworkHelpers.getPartNetwork(network).map(partNetwork -> partNetwork
                .addVariableContainer(DimPos.of(
                    NBTExtractorBE.this.level,
                    NBTExtractorBE.this.worldPosition
                ))
            ).orElse(false);
        }

        @Override
        public void onNetworkRemoval(INetwork network) {
            if (NBTExtractorBE.this.level == null) {
                return;
            }
            NetworkHelpers.getPartNetwork(network).ifPresent(partNetwork -> partNetwork
                .removeVariableContainer(DimPos.of(
                    NBTExtractorBE.this.level,
                    NBTExtractorBE.this.worldPosition
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
            BlockGetter world,
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
            if (NBTExtractorBE.this.level == null) {
                return false;
            }
            return NBTExtractorBE.this.level
                .hasChunkAt(NBTExtractorBE.this.getBlockPos());
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
        public Optional<NBTExtractorBE> getNetworkEventListener() {
            return Optional.of(NBTExtractorBE.this);
        }
    }

    public static final int SRC_NBT_SLOT = 0;
    public static final int VAR_OUT_SLOT = 1;
    private EnumFacingMap<Boolean> connected = EnumFacingMap.newMap();
    private CableCapability cableCapability = new CableCapability();
    private NetworkCarrierDefault networkCarrierCapability = new NetworkCarrierDefault();
    private PathElementTile<NBTExtractorBE> pathElementCapability = new PathElementTile<>(
        this,
        this.cableCapability
    );
    private VariableContainerDefault variableContainerCapability = new VariableContainerDefault();
    private NetworkElementProviderSingleton networkElementProviderCapability =
        new NetworkElementProviderSingleton() {
            @Override
            public INetworkElement createNetworkElement(Level world, BlockPos blockPos) {
                return new NetworkElement();
            }
        };
    private NonNullList<ItemStack> itemStacks = NonNullList.withSize(2, ItemStack.EMPTY);
    private InventoryVariableEvaluator<IValue> evaluator;
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
    private Tag lastEvaluatedNBT = null;
    // If null, then there is no frozen value available
    private Wrapper<Tag> frozenNBT = null;
    private boolean autoRefresh = true;
    /**
     * The item stack that yielded the current frozen NBT
     */
    private ItemStack frozenNBTItemStack = ItemStack.EMPTY;

    public NBTExtractorBE(BlockPos pos, BlockState state) {
        super(Additions.NBT_EXTRACTOR_BE.get(), pos, state);
        this.expandedPaths = new HashSet<>();
        this.expandedPaths.add(new NBTPath());

        this.evaluator = createEvaluator();
    }

    protected InventoryVariableEvaluator<IValue> createEvaluator() {
       return new InventoryVariableEvaluator<>(
                this,
                SRC_NBT_SLOT,
                ValueDeseralizationContext.of(getLevel()),
                ValueTypes.CATEGORY_ANY
        );
    }

    public NBTExtractorOutputMode getOutputMode() {
        return this.outputMode;
    }

    public void setOutputMode(NBTExtractorOutputMode outputMode) {
        this.outputMode = outputMode;
        this.setChanged();
    }

    @Override
    public void setChanged() {
        super.setChanged();
        if (this.level == null) {
            return;
        }
        if (!this.level.isClientSide) {
            this.refreshVariables(true);
            this.shouldUpdateOutVariable = true;
            if (!this.autoRefresh && !ItemStack.matches(
                this.getItem(SRC_NBT_SLOT),
                this.frozenNBTItemStack
            )) {
                this.frozenNBTItemStack = this.getItem(SRC_NBT_SLOT);
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
            sendVariablesUpdateEvent,
            ValueDeseralizationContext.of(getLevel())
        );
    }

    @Override
    @Nonnull
    public ItemStack getItem(int index) {
        return this.itemStacks.get(index);
    }

    public void setDefaultNBTId(byte defaultNBTId) {
        if (defaultNBTId < 1 || defaultNBTId > 12) {
            this.defaultNBTId = 1;
        } else {
            this.defaultNBTId = defaultNBTId;
        }
        this.setChanged();
    }

    public NBTPath getExtractionPath() {
        return this.extractionPath;
    }

    public void setExtractionPath(NBTPath extractionPath) {
        this.extractionPath = extractionPath;
        this.setChanged();
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
        this.setChanged();
    }

    public void updateLastEvaluatedNBT(Tag lastEvaluatedNBT) {
        this.lastEvaluatedNBT = lastEvaluatedNBT;
        if (!this.autoRefresh && this.frozenNBT == null) {
            this.frozenNBT = Wrapper.of(this.lastEvaluatedNBT);
            this.frozenNBTItemStack = this.getItem(SRC_NBT_SLOT).copy();
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

    public Component getFirstErrorMessage() {
        List<MutableComponent> errors = this.evaluator.getErrors();
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
    public int getContainerSize() {
        return this.itemStacks.size();
    }

    @Override
    public boolean isEmpty() {
        return false;
    }

    @Override
    @Nonnull
    public ItemStack removeItem(int index, int count) {
        return ContainerHelper.removeItem(this.itemStacks, index, count);
    }

    @Override
    @Nonnull
    public ItemStack removeItemNoUpdate(int index) {
        return ContainerHelper.takeItem(this.itemStacks, index);
    }

    @Override
    public void setItem(int index, @Nonnull ItemStack stack) {
        this.itemStacks.set(index, stack);
        if (stack.getCount() > this.getMaxStackSize()) {
            stack.setCount(this.getMaxStackSize());
        }
    }

    @Override
    public int getMaxStackSize() {
        return 1;
    }

    @Override
    public boolean stillValid(@Nonnull Player player) {
        if (this.level == null) {
            return false;
        }
        if (this.level.getBlockEntity(this.worldPosition) != this) {
            return false;
        } else {
            if (player.distanceToSqr(
                (double) this.worldPosition.getX() + 0.5D,
                (double) this.worldPosition.getY() + 0.5D,
                (double) this.worldPosition.getZ() + 0.5D
            ) <= 64.0D) {
                return true;
            }
            return (this.isRemote(player.getMainHandItem()) ||
                this.isRemote(player.getOffhandItem()));
        }
    }

    /**
     * Tests whether the given item stack is a remote for this NBT Extractor.
     */
    private boolean isRemote(ItemStack itemStack) {
        if (this.level == null) {
            return false;
        }
        if (itemStack.getItem() != Additions.NBT_EXTRACTOR_REMOTE.get()) {
            return false;
        }
        CompoundTag tag = Additions.NBT_EXTRACTOR_REMOTE.get().getModNBT(itemStack);
        return (tag.contains("world")) &&
            (tag.getString("world").equals(this.level.dimension().location().toString())) &&
            (tag.getInt("x") == this.worldPosition.getX()) &&
            (tag.getInt("y") == this.worldPosition.getY()) &&
            (tag.getInt("z") == this.worldPosition.getZ());
    }

    @Override
    public void startOpen(@Nonnull Player player) {

    }

    @Override
    public void stopOpen(@Nonnull Player player) {

    }

    @Override
    public boolean canPlaceItem(int index, @Nonnull ItemStack stack) {
        return Integration.isVariable(stack);
    }

    public Wrapper<Tag> getFrozenValue() {
        if (this.autoRefresh) {
            return null;
        } else {
            return this.frozenNBT;
        }
    }

    @Override
    public void clearContent() {
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
    public void saveAdditional(CompoundTag tag) {
        ListTag errorsList = new ListTag();
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
                CompoundTag compound = new CompoundTag();
                if (this.frozenNBT.get() != null) {
                    compound.put("value", this.frozenNBT.get());
                }
                tag.put("frozenNBT", compound);
            }
            tag.put(
                "frozenNBTItemStack",
                this.frozenNBTItemStack.save(new CompoundTag())
            );
        }
        ContainerHelper.saveAllItems(tag, this.itemStacks);
    }

    @Nonnull
    @Override
    public Component getDisplayName() {
        return Component.translatable("block.integratednbt.nbt_extractor");
    }

    @Override
    @SuppressWarnings("unchecked")
    public void load(CompoundTag tag) {
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
                this.frozenNBTItemStack = ItemStack.of(tag.getCompound("frozenNBTItemStack"));
            }
        }
        ContainerHelper.loadAllItems(tag, this.itemStacks);
        this.shouldRefreshVariable = true;
        super.load(tag);
    }

    public void afterNetworkReAlive() {
        this.shouldRefreshVariable = true;
        this.connected.clear();
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(
        int windowId,
        @Nonnull Inventory inventory,
        @Nonnull Player player
    ) {
        return new NBTExtractorContainer(windowId, inventory, this);
    }

    public static <T extends BlockEntity> void tick(Level level, BlockPos pos, BlockState state, T blockEntity) {
        if (level == null) {
            return;
        }
        if (!level.isClientSide) {
            NBTExtractorBE self = (NBTExtractorBE) blockEntity;
            if (self.shouldRefreshVariable && self.networkCarrierCapability.getNetwork() != null) {
                self.shouldRefreshVariable = false;
                self.refreshVariables(true);
            }
            if (self.shouldUpdateOutVariable) {
                self.updateOutVariable();
                self.shouldUpdateOutVariable = false;
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
                this.getLevel(),
                this.getBlockState()
            );
            if (result != null) {
                this.itemStacks.set(VAR_OUT_SLOT, result);
            }
        }
    }
}
