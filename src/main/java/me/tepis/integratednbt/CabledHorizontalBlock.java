package me.tepis.integratednbt;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.HorizontalBlock;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.Explosion;
import net.minecraft.world.IWorld;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.World;
import org.cyclops.cyclopscore.helper.InventoryHelpers;
import org.cyclops.cyclopscore.helper.TileHelpers;
import org.cyclops.integrateddynamics.core.helper.CableHelpers;
import org.cyclops.integrateddynamics.core.helper.NetworkHelpers;
import org.cyclops.integrateddynamics.core.tileentity.TileCableConnectableInventory;

import javax.annotation.Nonnull;
import java.util.Collection;

public abstract class CabledHorizontalBlock extends HorizontalBlock {
    public CabledHorizontalBlock(Properties properties) {
        super(properties);
    }

    @Override
    @SuppressWarnings("deprecation")
    public void onBlockAdded(
        BlockState blockState,
        World world,
        BlockPos blockPos,
        BlockState oldState,
        boolean isMoving
    ) {
        super.onBlockAdded(blockState, world, blockPos, oldState, isMoving);
        if (!world.isRemote()) {
            CableHelpers.onCableAdded(world, blockPos);
        }
    }

    @Override
    public void onBlockPlacedBy(
        World world,
        BlockPos pos,
        BlockState state,
        LivingEntity placer,
        ItemStack itemStack
    ) {
        if (!world.isRemote) {
            CableHelpers.onCableAddedByPlayer(world, pos, placer);
        }
    }

    @Override
    public void onPlayerDestroy(
        IWorld world,
        @Nonnull BlockPos blockPos,
        @Nonnull BlockState blockState
    ) {
        CableHelpers.onCableRemoving((World) world, blockPos, true, false);
        Collection<Direction> connectedCables = CableHelpers.getExternallyConnectedCables(
            (World) world,
            blockPos
        );
        super.onPlayerDestroy(world, blockPos, blockState);
        CableHelpers.onCableRemoved((World) world, blockPos, connectedCables);
    }

    @Override
    public void onBlockExploded(
        BlockState blockState,
        World world,
        @Nonnull BlockPos blockPos,
        @Nonnull Explosion explosion
    ) {
        CableHelpers.onCableRemoving(world, blockPos, true, false);
        Collection<Direction> connectedCables = CableHelpers.getExternallyConnectedCables(
            world,
            blockPos
        );
        super.onBlockExploded(blockState, world, blockPos, explosion);
        CableHelpers.onCableRemoved(world, blockPos, connectedCables);
    }

    @SuppressWarnings("deprecation")
    @Override
    public void neighborChanged(
        BlockState state,
        World world,
        BlockPos pos,
        Block neighborBlock,
        BlockPos fromPos,
        boolean isMoving
    ) {
        super.neighborChanged(state, world, pos, neighborBlock, fromPos, isMoving);
        NetworkHelpers.onElementProviderBlockNeighborChange(
            world,
            pos,
            neighborBlock,
            null,
            fromPos
        );
    }

    @Override
    public void onNeighborChange(
        BlockState state,
        IWorldReader world,
        BlockPos pos,
        BlockPos neighbor
    ) {
        super.onNeighborChange(state, world, pos, neighbor);
        if (world instanceof World) {
            NetworkHelpers.onElementProviderBlockNeighborChange(
                (World) world,
                pos,
                world.getBlockState(neighbor).getBlock(),
                null,
                neighbor
            );
        }
    }

    @Override
    public void observedNeighborChange(
        BlockState observerState,
        World world,
        BlockPos observerPos,
        Block changedBlock,
        BlockPos changedBlockPos
    ) {
        super.observedNeighborChange(
            observerState,
            world,
            observerPos,
            changedBlock,
            changedBlockPos
        );
        NetworkHelpers.onElementProviderBlockNeighborChange(
            world,
            observerPos,
            changedBlock,
            null,
            changedBlockPos
        );
    }

    @Override
    @SuppressWarnings("deprecation")
    public void onReplaced(
        BlockState oldState,
        @Nonnull World world,
        @Nonnull BlockPos blockPos,
        BlockState newState,
        boolean isMoving
    ) {
        if (oldState.getBlock() != newState.getBlock()) {
            TileHelpers.getSafeTile(world, blockPos, TileCableConnectableInventory.class)
                .ifPresent(tile -> InventoryHelpers.dropItems(
                    world,
                    tile.getInventory(),
                    blockPos
                ));
            if (newState.isAir()) {
                CableHelpers.onCableRemoving(world, blockPos, true, false);
                Collection<Direction> connectedCables = CableHelpers.getExternallyConnectedCables(
                    world,
                    blockPos
                );
                super.onReplaced(oldState, world, blockPos, newState, isMoving);
                CableHelpers.onCableRemoved(world, blockPos, connectedCables);
            } else {
                super.onReplaced(oldState, world, blockPos, newState, isMoving);
            }
        }
    }
}
