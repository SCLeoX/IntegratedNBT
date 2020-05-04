package me.tepis.integratednbt;

import net.minecraft.block.BlockHorizontal;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyDirection;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.InventoryHelper;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.cyclops.commoncapabilities.api.capability.wrench.WrenchTarget;
import org.cyclops.integrateddynamics.Capabilities;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.Objects;

public class NBTExtractor extends CabledBlock {
    private class NBTExtractorItem extends ItemBlock {
        public NBTExtractorItem() {
            super(NBTExtractor.this);
            this.setRegistryName(REGISTRY_NAME);
        }

        @Override
        public void addInformation(
            @Nonnull ItemStack itemStack,
            @Nullable World world,
            @Nonnull List<String> tooltip,
            @Nonnull ITooltipFlag flag
        ) {
            super.addInformation(itemStack, world, tooltip, flag);
            tooltip.add(I18n.format("integratednbt:nbt_extractor.tooltip"));
        }
    }

    public static final String REGISTRY_NAME = "integratednbt:nbt_extractor";
    public static final String TRANSLATION_KEY = "tile." + REGISTRY_NAME + ".name";
    private static final PropertyDirection FACING = BlockHorizontal.FACING;
    private static NBTExtractor instance;
    private Item itemBlock = new NBTExtractorItem();

    private NBTExtractor() {
        super(Material.ANVIL);
        this.setDefaultState(this.blockState.getBaseState().withProperty(FACING, EnumFacing.NORTH));
        this.setCreativeTab(IntegratedNBTCreativeTab.getInstance());
        this.setRegistryName(REGISTRY_NAME);
        this.setTranslationKey(REGISTRY_NAME);
        this.setHardness(5.0F);
        this.setSoundType(SoundType.METAL);
    }

    public static NBTExtractor getInstance() {
        if (instance == null) {
            instance = new NBTExtractor();
        }
        return instance;
    }

    @Override
    public void breakBlock(
        @Nonnull World world,
        @Nonnull BlockPos blockPos,
        @Nonnull IBlockState state
    ) {
        TileEntity tileentity = world.getTileEntity(blockPos);
        if (tileentity instanceof NBTExtractorTileEntity) {
            InventoryHelper.dropInventoryItems(
                world,
                blockPos,
                (NBTExtractorTileEntity) tileentity
            );
        }
        super.breakBlock(world, blockPos, state);
    }

    @Override
    @Nonnull
    protected BlockStateContainer createBlockState() {
        return new BlockStateContainer(this, FACING);
    }

    @Override
    public int getMetaFromState(IBlockState state) {
        return state.getValue(FACING).getHorizontalIndex();
    }

    @Override
    @Nonnull
    @SuppressWarnings("deprecation")
    public IBlockState getStateFromMeta(int meta) {
        return this.getDefaultState().withProperty(FACING, EnumFacing.byHorizontalIndex(meta));
    }

    @Override
    public boolean hasTileEntity(IBlockState state) {
        return true;
    }

    @Override
    public TileEntity createTileEntity(@Nonnull World world, @Nonnull IBlockState state) {
        return new NBTExtractorTileEntity();
    }

    public Item getItemBlock() {
        return this.itemBlock;
    }

    @Override
    public void onBlockPlacedBy(
        World world,
        BlockPos pos,
        IBlockState state,
        EntityLivingBase placer,
        ItemStack itemStack
    ) {
        world.setBlockState(pos, state.withProperty(
            FACING,
            placer.getHorizontalFacing().getOpposite()
        ), 2);
        super.onBlockPlacedBy(world, pos, state, placer, itemStack);
    }

    @Override
    public boolean onBlockActivated(
        World world,
        BlockPos pos,
        IBlockState state,
        EntityPlayer player,
        EnumHand hand,
        EnumFacing side,
        float hitX,
        float hitY,
        float hitZ
    ) {
        ItemStack heldItem = player.getHeldItem(hand);
        if (!world.isRemote) {
            EntityPlayerMP playerMP = (EntityPlayerMP) player;
            if (heldItem.hasCapability(me.tepis.integratednbt.Capabilities.WRENCH_CAPABILITY, null)
                && Objects.requireNonNull(heldItem.getCapability(Capabilities.WRENCH, null))
                .canUse(player, WrenchTarget.forBlock(world, pos, side)) && player.isSneaking()
            ) {
                this.destroyBlock(world, pos, true);
                return true;
            }
            if (heldItem.getItem() == NBTExtractorRemote.getInstance()) {
                return false;
            }
            if (!player.isSneaking()) {
                this.playerAccess(world, pos, playerMP);
            }
        }
        return true;
    }

    public void playerAccess(World world, BlockPos pos, EntityPlayerMP playerMP) {
        TileEntity tileentity = world.getTileEntity(pos);
        if (tileentity instanceof NBTExtractorTileEntity) {
            NBTExtractorTileEntity nbtExtractorTileEntity = (NBTExtractorTileEntity) tileentity;
            nbtExtractorTileEntity.refreshVariables(true);
            playerMP.connection.sendPacket(nbtExtractorTileEntity.getUpdatePacket());
            playerMP.openGui(
                IntegratedNBT.getInstance(),
                0,
                world,
                pos.getX(),
                pos.getY(),
                pos.getZ()
            );
        }
    }
}
