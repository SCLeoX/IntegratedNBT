package me.tepis.integratednbt;

import me.tepis.integratednbt.network.serverbound.NBTExtractorRemoteRequestMessage;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

public class NBTExtractorRemote extends Item {
    public static final String REGISTRY_NAME = "integratednbt:nbt_extractor_remote";
    private static NBTExtractorRemote instance;

    public NBTExtractorRemote() {
        this.setRegistryName(REGISTRY_NAME);
        this.setTranslationKey(REGISTRY_NAME);
        this.setCreativeTab(IntegratedNBTCreativeTab.getInstance());
        this.setMaxStackSize(1);
    }

    @Override
    @Nonnull
    public ActionResult<ItemStack> onItemRightClick(
        World world,
        EntityPlayer player,
        @Nonnull EnumHand hand
    ) {
        if (world.isRemote) {
            this.clientUse(player.getHeldItem(hand), player);
        }
        return super.onItemRightClick(world, player, hand);
    }

    @SideOnly(Side.CLIENT)
    private void clientUse(ItemStack itemStack, EntityPlayer player) {
        NBTTagCompound nbt = this.getModNBT(itemStack);
        if (!nbt.hasKey("world")) {
            player.sendMessage(new TextComponentTranslation(
                "integratednbt:nbt_extractor_remote.need_bind"));
            return;
        }
        if (Minecraft.getMinecraft().world.provider.getDimension() != nbt.getInteger("world")) {
            player.sendMessage(new TextComponentTranslation(
                "integratednbt:nbt_extractor_remote.require_dim"));
            return;
        }
        World world = Minecraft.getMinecraft().world;
        BlockPos pos = new BlockPos(
            nbt.getInteger("x"),
            nbt.getInteger("y"),
            nbt.getInteger("z")
        );
        if (!world.isBlockLoaded(pos, false)) {
            player.sendMessage(new TextComponentTranslation(
                "integratednbt:nbt_extractor_remote.require_load_client"));
            return;
        }
        if (world.getBlockState(pos).getBlock() != NBTExtractor.getInstance()) {
            player.sendMessage(new TextComponentTranslation(
                "integratednbt:nbt_extractor_remote.invalid_bind"));
            return;
        }
        IntegratedNBT.getNetworkChannel().sendToServer(new NBTExtractorRemoteRequestMessage());
    }

    public NBTTagCompound getModNBT(ItemStack itemStack) {
        return itemStack.getOrCreateSubCompound(IntegratedNBT.MODID);
    }

    public void serverUse(ItemStack itemStack, EntityPlayerMP player) {
        NBTTagCompound nbt = this.getModNBT(itemStack);
        if (!nbt.hasKey("world")) {
            player.sendMessage(new TextComponentTranslation(
                "integratednbt:nbt_extractor_remote.need_bind"));
            return;
        }
        World world = DimensionManager.getWorld(nbt.getInteger("world"));
        BlockPos pos = new BlockPos(
            nbt.getInteger("x"),
            nbt.getInteger("y"),
            nbt.getInteger("z")
        );
        if (!world.isBlockLoaded(pos)) {
            player.sendMessage(new TextComponentTranslation(
                "integratednbt:nbt_extractor_remote.require_load_server"));
            return;
        }
        if (world.getBlockState(pos).getBlock() != NBTExtractor.getInstance()) {
            player.sendMessage(new TextComponentTranslation(
                "integratednbt:nbt_extractor_remote.invalid_bind"));
            return;
        }
        NBTExtractor.getInstance().playerAccess(world, pos, player);
    }

    @Override
    @Nonnull
    public EnumActionResult onItemUse(
        EntityPlayer player,
        World world,
        BlockPos pos,
        EnumHand hand,
        EnumFacing facing,
        float hitX,
        float hitY,
        float hitZ
    ) {
        if (world.getBlockState(pos).getBlock() == NBTExtractor.getInstance()) {
            if (!world.isRemote) {
                NBTExtractorRemote.getInstance().bindBlock(player.getHeldItem(hand), world, pos);
                player.sendMessage(new TextComponentTranslation(
                    "integratednbt:nbt_extractor_remote.bind_successful",
                    String.valueOf(pos.getX()),
                    String.valueOf(pos.getY()),
                    String.valueOf(pos.getZ())
                ));
            }
        } else if (world.isRemote) {
            this.clientUse(player.getHeldItem(hand), player);
        }
        return EnumActionResult.SUCCESS;
    }

    public void bindBlock(ItemStack itemStack, World world, BlockPos pos) {
        NBTTagCompound nbt = this.getModNBT(itemStack);
        nbt.setInteger("world", world.provider.getDimension());
        nbt.setInteger("x", pos.getX());
        nbt.setInteger("y", pos.getY());
        nbt.setInteger("z", pos.getZ());
    }

    public static NBTExtractorRemote getInstance() {
        if (instance == null) {
            instance = new NBTExtractorRemote();
        }
        return instance;
    }

    @Override
    public void addInformation(
        ItemStack itemStack,
        @Nullable World world,
        List<String> tooltip,
        ITooltipFlag flag
    ) {
        super.addInformation(itemStack, world, tooltip, flag);
        NBTTagCompound nbt = this.getModNBT(itemStack);
        if (nbt.hasKey("world")) {
            tooltip.add(I18n.format(
                "integratednbt:nbt_extractor_remote.tooltip.bound",
                String.valueOf(nbt.getInteger("x")),
                String.valueOf(nbt.getInteger("y")),
                String.valueOf(nbt.getInteger("z")),
                String.valueOf(nbt.getInteger("world"))
            ));
        } else {
            tooltip.add(I18n.format("integratednbt:nbt_extractor_remote.tooltip.not_bound"));
        }
        tooltip.add(I18n.format("integratednbt:nbt_extractor_remote.tooltip"));
    }
}
