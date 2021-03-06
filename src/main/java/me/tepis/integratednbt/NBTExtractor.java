package me.tepis.integratednbt;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.InventoryHelper;
import net.minecraft.item.BlockItem;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.state.StateContainer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.Mirror;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import net.minecraftforge.fml.network.NetworkHooks;
import org.cyclops.integrateddynamics.core.helper.WrenchHelpers;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

import static me.tepis.integratednbt.Additions.NBT_EXTRACTOR_BLOCK;

public class NBTExtractor extends CabledHorizontalBlock {
    public static class NBTExtractorBlockItem extends BlockItem {
        public NBTExtractorBlockItem() {
            super(NBT_EXTRACTOR_BLOCK.get(), new Item.Properties().group(ItemGroups.ITEM_GROUP));
        }

        @Override
        public void addInformation(
            @Nonnull ItemStack itemStack,
            @Nullable World world,
            @Nonnull List<ITextComponent> tooltip,
            @Nonnull ITooltipFlag flag
        ) {
            super.addInformation(itemStack, world, tooltip, flag);
            tooltip.add(new TranslationTextComponent("integratednbt:nbt_extractor.tooltip"));
        }
    }

    public static final String REGISTRY_NAME = "nbt_extractor";

    public NBTExtractor(Properties properties) {
        super(properties);
        this.setDefaultState(this.getDefaultState().with(HORIZONTAL_FACING, Direction.NORTH));
    }

    @Override
    protected void fillStateContainer(StateContainer.Builder<Block, BlockState> builder) {
        super.fillStateContainer(builder);
        builder.add(HORIZONTAL_FACING);
    }

    @Override
    public void onReplaced(
        BlockState oldState,
        @Nonnull World worldIn,
        @Nonnull BlockPos pos,
        BlockState newState,
        boolean isMoving
    ) {
        if (oldState.getBlock() != newState.getBlock()) {
            TileEntity tileEntity = worldIn.getTileEntity(pos);
            if (tileEntity instanceof NBTExtractorTileEntity) {
                IInventory inventory = (NBTExtractorTileEntity) tileEntity;
                for (int slot = 0; slot < inventory.getSizeInventory(); ++slot) {
                    InventoryHelper.spawnItemStack(
                        worldIn,
                        pos.getX(),
                        pos.getY(),
                        pos.getZ(),
                        inventory.getStackInSlot(slot)
                    );
                }
            }
        }
        super.onReplaced(oldState, worldIn, pos, newState, isMoving);
    }

    @Override
    public boolean hasTileEntity(BlockState state) {
        return true;
    }

    @Override
    public TileEntity createTileEntity(@Nonnull BlockState state, @Nonnull IBlockReader world) {
        return new NBTExtractorTileEntity();
    }

    @Override
    public BlockState getStateForPlacement(BlockItemUseContext context) {
        return this.getDefaultState()
            .with(HORIZONTAL_FACING, context.getPlacementHorizontalFacing().getOpposite());
    }

    @Override
    @Nonnull
    @SuppressWarnings("deprecation")
    public ActionResultType onBlockActivated(
        BlockState blockState,
        World world,
        BlockPos blockPos,
        PlayerEntity player,
        Hand hand,
        BlockRayTraceResult rayTraceResult
    ) {
        ItemStack heldItem = player.getHeldItem(hand);
        if (!world.isRemote) {
            ServerPlayerEntity playerMP = (ServerPlayerEntity) player;
            if (WrenchHelpers.isWrench(player, heldItem, world, blockPos, rayTraceResult.getFace())
                && player.isCrouching()
            ) {
                Block.spawnDrops(
                    blockState,
                    world,
                    blockPos,
                    blockState.hasTileEntity() ? world.getTileEntity(blockPos) : null,
                    player,
                    heldItem
                );
                world.destroyBlock(blockPos, false);
                return ActionResultType.SUCCESS;
            }
            if (heldItem.getItem() == Additions.NBT_EXTRACTOR_REMOTE.get()) {
                return ActionResultType.PASS;
            }
            if (!player.isCrouching()) {
                this.playerAccess(world, blockPos, playerMP);
                return ActionResultType.SUCCESS;
            }
        }
        return ActionResultType.SUCCESS;
    }

    public void playerAccess(World world, BlockPos pos, ServerPlayerEntity playerMP) {
        TileEntity tileentity = world.getTileEntity(pos);
        if (tileentity instanceof NBTExtractorTileEntity) {
            NBTExtractorTileEntity nbtExtractorTileEntity = (NBTExtractorTileEntity) tileentity;
            nbtExtractorTileEntity.refreshVariables(true);
            NetworkHooks.openGui(playerMP, nbtExtractorTileEntity, pos);
        }
    }

    @Override
    @Nonnull
    @SuppressWarnings("deprecation")
    public BlockState rotate(BlockState state, Rotation rot) {
        return state.with(HORIZONTAL_FACING, rot.rotate(state.get(HORIZONTAL_FACING)));
    }

    @Override
    @Nonnull
    @SuppressWarnings("deprecation")
    public BlockState mirror(BlockState state, Mirror mirrorIn) {
        return state.rotate(mirrorIn.toRotation(state.get(HORIZONTAL_FACING)));
    }
}
