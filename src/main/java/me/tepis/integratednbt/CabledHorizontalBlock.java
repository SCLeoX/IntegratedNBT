package me.tepis.integratednbt;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.core.Direction;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.Level;
import org.cyclops.cyclopscore.helper.InventoryHelpers;
import org.cyclops.cyclopscore.helper.BlockEntityHelpers;
import org.cyclops.integrateddynamics.core.helper.CableHelpers;
import org.cyclops.integrateddynamics.core.helper.NetworkHelpers;
import org.cyclops.integrateddynamics.core.blockentity.BlockEntityCableConnectableInventory;

import javax.annotation.Nonnull;
import java.util.Collection;

import net.minecraft.world.level.block.state.BlockBehaviour.Properties;

public abstract class CabledHorizontalBlock extends HorizontalDirectionalBlock {
    public CabledHorizontalBlock(Properties properties) {
        super(properties);
    }

    @Override
    @SuppressWarnings("deprecation")
    public void onPlace(
        BlockState blockState,
        Level world,
        BlockPos blockPos,
        BlockState oldState,
        boolean isMoving
    ) {
        super.onPlace(blockState, world, blockPos, oldState, isMoving);
        if (!world.isClientSide()) {
            CableHelpers.onCableAdded(world, blockPos);
        }
    }

    @Override
    public void setPlacedBy(
        Level world,
        BlockPos pos,
        BlockState state,
        LivingEntity placer,
        ItemStack itemStack
    ) {
        if (!world.isClientSide) {
            CableHelpers.onCableAddedByPlayer(world, pos, placer);
        }
    }

    @Override
    public void destroy(
        LevelAccessor world,
        @Nonnull BlockPos blockPos,
        @Nonnull BlockState blockState
    ) {
        CableHelpers.onCableRemoving((Level) world, blockPos, true, false);
        Collection<Direction> connectedCables = CableHelpers.getExternallyConnectedCables(
            (Level) world,
            blockPos
        );
        super.destroy(world, blockPos, blockState);
        CableHelpers.onCableRemoved((Level) world, blockPos, connectedCables);
    }

    @Override
    public void onBlockExploded(
        BlockState blockState,
        Level world,
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
        Level world,
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
        LevelReader world,
        BlockPos pos,
        BlockPos neighbor
    ) {
        super.onNeighborChange(state, world, pos, neighbor);
        if (world instanceof Level) {
            NetworkHelpers.onElementProviderBlockNeighborChange(
                (Level) world,
                pos,
                world.getBlockState(neighbor).getBlock(),
                null,
                neighbor
            );
        }
    }

    @Override
    @SuppressWarnings("deprecation")
    public void onRemove(
        BlockState oldState,
        @Nonnull Level world,
        @Nonnull BlockPos blockPos,
        BlockState newState,
        boolean isMoving
    ) {
        if (oldState.getBlock() != newState.getBlock()) {
            BlockEntityHelpers.get(world, blockPos, BlockEntityCableConnectableInventory.class)
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
                super.onRemove(oldState, world, blockPos, newState, isMoving);
                CableHelpers.onCableRemoved(world, blockPos, connectedCables);
            } else {
                super.onRemove(oldState, world, blockPos, newState, isMoving);
            }
        }
    }
}
