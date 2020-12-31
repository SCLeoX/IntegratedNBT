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
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ActionResult;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.RegistryKey;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Util;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.network.PacketDistributor;

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
                "integratednbt:nbt_extractor_remote.need_bind"), Util.DUMMY_UUID);
            return;
        }
        ClientWorld world = Minecraft.getInstance().world;
        if (world == null) {
            player.sendMessage(new TranslationTextComponent(
                "integratednbt:nbt_extractor_remote.invalid_bind"), Util.DUMMY_UUID);
            return;
        }
        if (!world.getDimensionKey().getLocation().toString().equals(nbt.getString("world"))) {
            player.sendMessage(new TranslationTextComponent(
                "integratednbt:nbt_extractor_remote.require_dim"), Util.DUMMY_UUID);
            return;
        }
        BlockPos pos = new BlockPos(
            nbt.getInt("x"),
            nbt.getInt("y"),
            nbt.getInt("z")
        );
        if (!world.isAreaLoaded(pos, 1)) {
            player.sendMessage(new TranslationTextComponent(
                "integratednbt:nbt_extractor_remote.require_load_client"), Util.DUMMY_UUID);
            return;
        }
        if (world.getBlockState(pos).getBlock() != Additions.NBT_EXTRACTOR_BLOCK.get()) {
            player.sendMessage(new TranslationTextComponent(
                "integratednbt:nbt_extractor_remote.invalid_bind"), Util.DUMMY_UUID);
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
                "integratednbt:nbt_extractor_remote.need_bind"), Util.DUMMY_UUID);
            return;
        }
        RegistryKey<World> dimensionKey = RegistryKey.getOrCreateKey(Registry.WORLD_KEY, new ResourceLocation(nbt.getString("world")));
        MinecraftServer server = player.getServer();
        if (server == null) {
            player.sendMessage(new TranslationTextComponent(
                "integratednbt:nbt_extractor_remote.invalid_bind"), Util.DUMMY_UUID);
            return;
        }
        World world = server.getWorld(dimensionKey);
        if (world == null) {
            player.sendMessage(new TranslationTextComponent(
                "integratednbt:nbt_extractor_remote.invalid_bind"), Util.DUMMY_UUID);
            return;
        }
        BlockPos pos = new BlockPos(
            nbt.getInt("x"),
            nbt.getInt("y"),
            nbt.getInt("z")
        );
        if (!world.isAreaLoaded(pos, 1)) {
            player.sendMessage(new TranslationTextComponent(
                "integratednbt:nbt_extractor_remote.require_load_server"), Util.DUMMY_UUID);
            return;
        }
        if (world.getBlockState(pos).getBlock() != Additions.NBT_EXTRACTOR_BLOCK.get()) {
            player.sendMessage(new TranslationTextComponent(
                "integratednbt:nbt_extractor_remote.invalid_bind"), Util.DUMMY_UUID);
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
                ), Util.DUMMY_UUID);
            }
        } else if (world.isRemote) {
            this.clientUse(player.getHeldItem(hand), player);
        }
        return ActionResultType.SUCCESS;
    }

    public void bindBlock(ItemStack itemStack, World world, BlockPos pos) {
        CompoundNBT nbt = this.getModNBT(itemStack);
        nbt.putString("world", world.getDimensionKey().getLocation().toString());
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
                nbt.getString("world")
            ).modifyStyle(style -> style.setFormatting(TextFormatting.GREEN)));
        } else {
            tooltip.add(new TranslationTextComponent(
                "integratednbt:nbt_extractor_remote.tooltip.not_bound"));
        }
        tooltip.add(new TranslationTextComponent("integratednbt:nbt_extractor_remote.tooltip"));
    }
}
