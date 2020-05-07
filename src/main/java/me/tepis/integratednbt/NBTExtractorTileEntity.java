package me.tepis.integratednbt;

import me.tepis.integratednbt.NBTExtractorTileEntity.NetworkElement;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.ItemStackHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import org.cyclops.cyclopscore.datastructure.DimPos;
import org.cyclops.cyclopscore.datastructure.EnumFacingMap;
import org.cyclops.cyclopscore.helper.L10NHelpers.UnlocalizedString;
import org.cyclops.cyclopscore.helper.MinecraftHelpers;
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
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public class NBTExtractorTileEntity extends TileEntity implements ICapabilityProvider, IInventory,
    INetworkEventListener<NetworkElement>, ITickable {
    class CableCapability implements ICable {
        @Override
        public boolean canConnect(ICable connector, EnumFacing side) {
            return true;
        }

        @Override
        public boolean isConnected(EnumFacing side) {
            if (NBTExtractorTileEntity.this.connected.isEmpty()) {
                this.updateConnections();
            }
            return NBTExtractorTileEntity.this.connected.get(side);
        }

        @Override
        public void updateConnections() {
            NBTExtractorTileEntity entity = NBTExtractorTileEntity.this;
            World world = entity.getWorld();
            for (EnumFacing side : EnumFacing.VALUES) {
                boolean cableConnected = CableHelpers.canCableConnectTo(
                    world,
                    entity.getPos(),
                    side,
                    this
                );
                entity.connected.put(side, cableConnected);
            }
            entity.getWorld().markChunkDirty(entity.getPos(), entity);
            IBlockState blockState = world.getBlockState(NBTExtractorTileEntity.this.pos);
            world.notifyBlockUpdate(NBTExtractorTileEntity.this.pos, blockState, blockState, 3);
        }

        @Override
        public void disconnect(EnumFacing side) {}

        @Override
        public void reconnect(EnumFacing side) {}

        @Override
        public ItemStack getItemStack() {
            return new ItemStack(NBTExtractor.getInstance().getItemBlock());
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
            return Objects.requireNonNull(NetworkHelpers.getPartNetwork(network))
                .addVariableContainer(DimPos.of(
                    NBTExtractorTileEntity.this.world,
                    NBTExtractorTileEntity.this.pos
                ));
        }

        @Override
        public void onNetworkRemoval(INetwork network) {
            Objects.requireNonNull(NetworkHelpers.getPartNetwork(network))
                .removeVariableContainer(DimPos.of(
                    NBTExtractorTileEntity.this.world,
                    NBTExtractorTileEntity.this.pos
                ));
        }

        @Override
        public void onPreRemoved(INetwork network) {}

        @Override
        public void onPostRemoved(INetwork network) {}

        @Override
        public void onNeighborBlockChange(
            @Nullable INetwork network,
            IBlockAccess world,
            Block neighbourBlock,
            BlockPos neighbourBlockPos
        ) {

        }

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
        public boolean canRevalidate(INetwork network) {
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
        public NBTExtractorTileEntity getNetworkEventListener() {
            return NBTExtractorTileEntity.this;
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
    private NBTTagCompound lastEvaluatedNBT = null;

    public NBTExtractorTileEntity() {
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
        if (!this.world.isRemote) {
            this.refreshVariables(true);
            this.shouldUpdateOutVariable = true;
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

    public void setLastEvaluatedNBT(NBTTagCompound lastEvaluatedNBT) {
        this.lastEvaluatedNBT = lastEvaluatedNBT;
    }

    public HashSet<NBTPath> getExpandedPaths() {
        return this.expandedPaths;
    }

    public Wrapper<Integer> getScrollTop() {
        return this.scrollTop;
    }

    public IVariable<?> getSrcNBTVariable() {
        return this.evaluator.getVariable(
            NetworkHelpers.getPartNetwork(this.networkCarrierCapability.getNetwork())
        );
    }

    @Override
    public boolean hasCapability(@Nonnull Capability<?> capability, EnumFacing facing) {
        return (
            capability == Capabilities.CABLE_CAPABILITY
                || capability == Capabilities.NETWORK_CARRIER_CAPABILITY
                || capability == Capabilities.PATH_ELEMENT_CAPABILITY
                || capability == Capabilities.VARIABLE_CONTAINER_CAPABILITY
                || capability == Capabilities.NETWORK_ELEMENT_PROVIDER);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T getCapability(@Nonnull Capability<T> capability, EnumFacing facing) {
        if (capability == Capabilities.CABLE_CAPABILITY) {
            return (T) this.cableCapability;
        } else if (capability == Capabilities.NETWORK_CARRIER_CAPABILITY) {
            return (T) this.networkCarrierCapability;
        } else if (capability == Capabilities.PATH_ELEMENT_CAPABILITY) {
            return (T) this.pathElementCapability;
        } else if (capability == Capabilities.VARIABLE_CONTAINER_CAPABILITY) {
            return (T) this.variableContainerCapability;
        } else if (capability == Capabilities.NETWORK_ELEMENT_PROVIDER) {
            return (T) this.networkElementProviderCapability;
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
    public ItemStack getStackInSlot(int index) {
        return this.itemStacks.get(index);
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
    public boolean isUsableByPlayer(@Nonnull EntityPlayer player) {
        if (this.world.getTileEntity(this.pos) != this) {
            return false;
        } else {
            return player.getDistanceSq(
                (double) this.pos.getX() + 0.5D,
                (double) this.pos.getY() + 0.5D,
                (double) this.pos.getZ() + 0.5D
            ) <= 64.0D;
        }
    }

    @Override
    public void openInventory(@Nonnull EntityPlayer player) {

    }

    @Override
    public void closeInventory(@Nonnull EntityPlayer player) {

    }

    @Override
    public boolean isItemValidForSlot(int index, @Nonnull ItemStack stack) {
        return Integration.isVariable(stack);
    }

    @Override
    public int getField(int id) {
        return 0;
    }

    @Override
    public void setField(int id, int value) {

    }

    @Override
    public int getFieldCount() {
        return 0;
    }

    @Override
    public void clear() {
        this.itemStacks.clear();
    }

    @Override
    @Nonnull
    public String getName() {
        return NBTExtractor.TRANSLATION_KEY;
    }

    @Override
    public boolean hasCustomName() {
        return false;
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
    public void update() {
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
            ItemStack result = null;
            switch (this.outputMode) {
                case REFERENCE:
                    result = this.getVariableByReferenceMode();
                    break;
                case OPERATOR:
                    result = this.getVariableByOperatorMode();
                    break;
                case VALUE:
                    result = this.getVariableByValueMode();
                    break;
            }
            if (result != null) {
                this.itemStacks.set(VAR_OUT_SLOT, result);
            }
        }
    }

    @Nullable
    private ItemStack getVariableByReferenceMode() {
        this.refreshVariables(true);
        IVariableFacadeHandlerRegistry registry =
            IntegratedDynamics._instance.getRegistryManager()
                .getRegistry(IVariableFacadeHandlerRegistry.class);
        IVariableFacade variableFacade =
            NBTExtractorTileEntity.this.evaluator.getVariableFacade();
        if (variableFacade != null) {
            int sourceNBTId = variableFacade.getId();
            IVariableFacadeFactory<NBTExtractedVariableFacade> factory =
                new IVariableFacadeFactory<NBTExtractedVariableFacade>() {
                    @Override
                    public NBTExtractedVariableFacade create(boolean generateId) {
                        return new NBTExtractedVariableFacade(
                            generateId,
                            sourceNBTId,
                            NBTExtractorTileEntity.this.extractionPath,
                            NBTExtractorTileEntity.this.defaultNBTId
                        );
                    }

                    @Override
                    public NBTExtractedVariableFacade create(int id) {
                        return new NBTExtractedVariableFacade(
                            id,
                            sourceNBTId,
                            NBTExtractorTileEntity.this.extractionPath,
                            NBTExtractorTileEntity.this.defaultNBTId
                        );
                    }
                };
            return registry.writeVariableFacadeItem(
                true,
                this.itemStacks.get(VAR_OUT_SLOT),
                NBTExtractedVariableFacadeHandler.getInstance(),
                factory,
                null,
                this.getBlockType()
            );
        } else {
            return null;
        }
    }

    @Nullable
    @SuppressWarnings( {"rawtypes", "unchecked"})
    private ItemStack getVariableUsingValue(IValue value) {
        IVariableFacadeHandlerRegistry registry = IntegratedDynamics._instance.getRegistryManager()
            .getRegistry(IVariableFacadeHandlerRegistry.class);
        NBTBase extractedNBT = this.extractionPath.extract(this.lastEvaluatedNBT);
        if (value == null) {
            return null;
        }
        return registry.writeVariableFacadeItem(
            true,
            this.itemStacks.get(VAR_OUT_SLOT),
            ValueTypes.REGISTRY,
            new IVariableFacadeHandlerRegistry.IVariableFacadeFactory<IValueTypeVariableFacade>() {
                @Override
                public IValueTypeVariableFacade create(boolean generateId) {
                    return new ValueTypeVariableFacade(generateId, value.getType(), value);
                }

                @Override
                public IValueTypeVariableFacade create(int id) {
                    return new ValueTypeVariableFacade(id, value.getType(), value);
                }
            },
            null,
            this.getBlockType()
        );
    }

    @Nullable
    private ItemStack getVariableByValueMode() {
        this.refreshVariables(true);
        NBTBase extractedNBT = this.extractionPath.extract(this.lastEvaluatedNBT);
        IValue value = extractedNBT == null
            ? NBTValueConverter.getDefaultValue(this.defaultNBTId)
            : NBTValueConverter.mapNBTToValue(extractedNBT);
        return this.getVariableUsingValue(value);
    }

    @Nullable
    private ItemStack getVariableByOperatorMode() {
        return this.getVariableUsingValue(ValueOperator.of(new NBTExtractionOperator(
            this.extractionPath,
            this.defaultNBTId
        )));
    }

    @Override
    @Nonnull
    public NBTTagCompound writeToNBT(NBTTagCompound tag) {
        NBTTagList errorsList = new NBTTagList();
        for (UnlocalizedString error : this.evaluator.getErrors()) {
            errorsList.appendTag(error.toNBT());
        }
        tag.setTag("errors", errorsList);
        tag.setTag("path", this.extractionPath.toNBT());
        tag.setByte("defaultNBTId", this.defaultNBTId);
        ItemStackHelper.saveAllItems(tag, this.itemStacks);
        return super.writeToNBT(tag);
    }

    @Nonnull
    @Override
    public ITextComponent getDisplayName() {
        ITextComponent superDisplayName = super.getDisplayName();
        return superDisplayName == null ? new TextComponentString("") : superDisplayName;
    }

    @Override
    public void readFromNBT(NBTTagCompound tag) {
        if (tag.hasKey("errors")) {
            NBTTagList errorsList = tag.getTagList("errors", 10 /* Compound */);
            this.evaluator.setErrors(StreamSupport.stream(errorsList.spliterator(), false)
                .map(nbtBase -> {
                    UnlocalizedString string = new UnlocalizedString();
                    string.fromNBT((NBTTagCompound) nbtBase);
                    return string;
                })
                .collect(Collectors.toList()));
        }
        if (tag.hasKey("path")) {
            this.extractionPath = NBTPath.fromNBT(tag.getTag("path")).orElse(new NBTPath());
        }
        if (tag.hasKey("defaultNBTId")) {
            this.defaultNBTId = tag.getByte("defaultNBTId");
        }
        ItemStackHelper.loadAllItems(tag, this.itemStacks);
        this.shouldRefreshVariable = true;
        super.readFromNBT(tag);
    }

    public void afterNetworkReAlive() {
        this.shouldRefreshVariable = true;
        this.connected.clear();
    }
}
