package me.tepis.integratednbt;

import me.tepis.integratednbt.network.PacketHandler;
import me.tepis.integratednbt.network.serverbound.NBTExtractorRemoteRequestMessage;
import net.minecraft.client.Minecraft;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUseContext;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ActionResult;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraft.world.dimension.DimensionType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.fml.network.PacketDistributor;
import net.minecraftforge.fml.server.ServerLifecycleHooks;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

public class NBTExtractorRemote extends Item {
    public static final String REGISTRY_NAME = "nbt_extractor_remote";

    public NBTExtractorRemote() {
        super(new Item.Properties().group(ItemGroups.ITEM_GROUP).maxStackSize(1));
    }

    @Override
    @Nonnull
    public ActionResult<ItemStack> onItemRightClick(
        World world,
        PlayerEntity player,
        @Nonnull Hand hand
    ) {
        if (world.isRemote) {
            this.clientUse(player.getHeldItem(hand), player);
        }
        return super.onItemRightClick(world, player, hand);
    }

    @OnlyIn(Dist.CLIENT)
    private void clientUse(ItemStack itemStack, PlayerEntity player) {
        CompoundNBT nbt = this.getModNBT(itemStack);
        if (!nbt.contains("world")) {
            player.sendMessage(new TranslationTextComponent(
                "integratednbt:nbt_extractor_remote.need_bind"));
            return;
        }
        ClientWorld world = Minecraft.getInstance().world;
        if (world == null) {
            player.sendMessage(new TranslationTextComponent(
                "integratednbt:nbt_extractor_remote.invalid_bind"));
            return;
        }
        if (world.getDimension().getType().getId() != nbt.getInt("world")) {
            player.sendMessage(new TranslationTextComponent(
                "integratednbt:nbt_extractor_remote.require_dim"));
            return;
        }
        BlockPos pos = new BlockPos(
            nbt.getInt("x"),
            nbt.getInt("y"),
            nbt.getInt("z")
        );
        if (!world.isAreaLoaded(pos, 1)) {
            player.sendMessage(new TranslationTextComponent(
                "integratednbt:nbt_extractor_remote.require_load_client"));
            return;
        }
        if (world.getBlockState(pos).getBlock() != Additions.NBT_EXTRACTOR_BLOCK.get()) {
            player.sendMessage(new TranslationTextComponent(
                "integratednbt:nbt_extractor_remote.invalid_bind"));
            return;
        }
        PacketHandler.INSTANCE.send(
            PacketDistributor.SERVER.noArg(),
            new NBTExtractorRemoteRequestMessage()
        );
    }

    public CompoundNBT getModNBT(ItemStack itemStack) {
        return itemStack.getOrCreateChildTag(IntegratedNBT.MODID);
    }

    public void serverUse(ItemStack itemStack, ServerPlayerEntity player) {
        CompoundNBT nbt = this.getModNBT(itemStack);
        if (!nbt.contains("world")) {
            player.sendMessage(new TranslationTextComponent(
                "integratednbt:nbt_extractor_remote.need_bind"));
            return;
        }
        DimensionType dimension = DimensionType.getById(nbt.getInt("world"));
        if (dimension == null) {
            player.sendMessage(new TranslationTextComponent(
                "integratednbt:nbt_extractor_remote.invalid_bind"));
            return;
        }
        World world = DimensionManager.getWorld(
            ServerLifecycleHooks.getCurrentServer(),
            dimension,
            false,
            false
        );
        if (world == null) {
            player.sendMessage(new TranslationTextComponent(
                "integratednbt:nbt_extractor_remote.invalid_bind"));
            return;
        }
        BlockPos pos = new BlockPos(
            nbt.getInt("x"),
            nbt.getInt("y"),
            nbt.getInt("z")
        );
        if (!world.isAreaLoaded(pos, 1)) {
            player.sendMessage(new TranslationTextComponent(
                "integratednbt:nbt_extractor_remote.require_load_server"));
            return;
        }
        if (world.getBlockState(pos).getBlock() != Additions.NBT_EXTRACTOR_BLOCK.get()) {
            player.sendMessage(new TranslationTextComponent(
                "integratednbt:nbt_extractor_remote.invalid_bind"));
            return;
        }
        Additions.NBT_EXTRACTOR_BLOCK.get().playerAccess(world, pos, player);
    }

    @Override
    @Nonnull
    public ActionResultType onItemUse(ItemUseContext itemUseContext) {
        World world = itemUseContext.getWorld();
        BlockPos pos = itemUseContext.getPos();
        PlayerEntity player = itemUseContext.getPlayer();
        if (player == null) {
            return ActionResultType.FAIL;
        }
        Hand hand = itemUseContext.getHand();
        if (world.getBlockState(pos).getBlock() == Additions.NBT_EXTRACTOR_BLOCK.get()) {
            if (!world.isRemote) {
                Additions.NBT_EXTRACTOR_REMOTE.get()
                    .bindBlock(player.getHeldItem(hand), world, pos);
                player.sendMessage(new TranslationTextComponent(
                    "integratednbt:nbt_extractor_remote.bind_successful",
                    String.valueOf(pos.getX()),
                    String.valueOf(pos.getY()),
                    String.valueOf(pos.getZ())
                ));
            }
        } else if (world.isRemote) {
            this.clientUse(player.getHeldItem(hand), player);
        }
        return ActionResultType.SUCCESS;
    }

    public void bindBlock(ItemStack itemStack, World world, BlockPos pos) {
        CompoundNBT nbt = this.getModNBT(itemStack);
        nbt.putInt("world", world.getDimension().getType().getId());
        nbt.putInt("x", pos.getX());
        nbt.putInt("y", pos.getY());
        nbt.putInt("z", pos.getZ());
    }

    @Override
    public void addInformation(
        ItemStack itemStack,
        @Nullable World world,
        List<ITextComponent> tooltip,
        ITooltipFlag flag
    ) {
        super.addInformation(itemStack, world, tooltip, flag);
        CompoundNBT nbt = this.getModNBT(itemStack);
        if (nbt.contains("world")) {
            tooltip.add(new TranslationTextComponent(
                "integratednbt:nbt_extractor_remote.tooltip.bound",
                String.valueOf(nbt.getInt("x")),
                String.valueOf(nbt.getInt("y")),
                String.valueOf(nbt.getInt("z")),
                String.valueOf(nbt.getInt("world"))
            ));
        } else {
            tooltip.add(new TranslationTextComponent(
                "integratednbt:nbt_extractor_remote.tooltip.not_bound"));
        }
        tooltip.add(new TranslationTextComponent("integratednbt:nbt_extractor_remote.tooltip"));
    }
}
