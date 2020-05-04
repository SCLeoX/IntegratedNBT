package me.tepis.integratednbt;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.Explosion;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import org.cyclops.integrateddynamics.core.helper.CableHelpers;
import org.cyclops.integrateddynamics.core.helper.NetworkHelpers;

import javax.annotation.Nonnull;

public abstract class CabledBlock extends Block {
    public CabledBlock(Material materialIn) {
        super(materialIn);
    }

    public void destroyBlock(World world, BlockPos pos, boolean dropBlock) {
        this.onPreBlockDestroyed(world, pos);
        world.destroyBlock(pos, dropBlock);
        this.onPostBlockDestroyed(world, pos);
    }

    protected void onPreBlockDestroyed(World world, BlockPos pos) {
        CableHelpers.onCableRemoving(world, pos, true, false);
    }

    protected void onPostBlockDestroyed(World world, BlockPos pos) {
        CableHelpers.onCableRemoved(
            world,
            pos,
            CableHelpers.getExternallyConnectedCables(world, pos)
        );
    }

    @Override
    public void onBlockAdded(World world, BlockPos pos, IBlockState state) {
        super.onBlockAdded(world, pos, state);
        if (!world.isRemote) {
            CableHelpers.onCableAdded(world, pos);
        }
    }

    @Override
    public void onBlockPlacedBy(
        World world,
        BlockPos pos,
        IBlockState state,
        EntityLivingBase placer,
        ItemStack itemStack
    ) {
        if (!world.isRemote) {
            CableHelpers.onCableAddedByPlayer(world, pos, placer);
        }
    }

    @Override
    public boolean removedByPlayer(
        @Nonnull IBlockState blockState,
        World world,
        @Nonnull BlockPos pos,
        @Nonnull EntityPlayer player,
        boolean willHarvest
    ) {
        this.onPreBlockDestroyed(world, pos);
        return super.removedByPlayer(blockState, world, pos, player, willHarvest);
    }

    @Override
    public void onBlockExploded(
        World world,
        @Nonnull BlockPos blockPos,
        @Nonnull Explosion explosion
    ) {
        this.onPreBlockDestroyed(world, blockPos);
        super.onBlockExploded(world, blockPos, explosion);
        this.onPostBlockDestroyed(world, blockPos);
    }

    @Override
    public void breakBlock(
        @Nonnull World world,
        @Nonnull BlockPos blockPos,
        @Nonnull IBlockState state
    ) {
        this.onPreBlockDestroyed(world, blockPos);
        super.breakBlock(world, blockPos, state);
        this.onPostBlockDestroyed(world, blockPos);
    }

    @SuppressWarnings("deprecation")
    @Override
    public void neighborChanged(
        IBlockState state,
        World world,
        BlockPos pos,
        Block neighborBlock,
        BlockPos fromPos
    ) {
        super.neighborChanged(state, world, pos, neighborBlock, fromPos);
        NetworkHelpers.onElementProviderBlockNeighborChange(
            world,
            pos,
            neighborBlock,
            null,
            fromPos
        );
    }

    @Override
    public void onNeighborChange(IBlockAccess world, BlockPos pos, BlockPos neighbor) {
        super.onNeighborChange(world, pos, neighbor);
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
        IBlockState observerState,
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
}
