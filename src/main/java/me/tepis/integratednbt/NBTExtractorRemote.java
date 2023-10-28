package me.tepis.integratednbt;

import me.tepis.integratednbt.network.PacketHandler;
import me.tepis.integratednbt.network.serverbound.NBTExtractorRemoteRequestMessage;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.network.PacketDistributor;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

public class NBTExtractorRemote extends Item {
    public static final String REGISTRY_NAME = "nbt_extractor_remote";

    public NBTExtractorRemote() {
        super(new Item.Properties().stacksTo(1));
    }

    @Override
    @Nonnull
    public InteractionResultHolder<ItemStack> use(
        Level world,
        Player player,
        @Nonnull InteractionHand hand
    ) {
        if (world.isClientSide) {
            this.clientUse(player.getItemInHand(hand), player);
        }
        return super.use(world, player, hand);
    }

    @OnlyIn(Dist.CLIENT)
    private void clientUse(ItemStack itemStack, Player player) {
        CompoundTag nbt = this.getModNBT(itemStack);
        if (!nbt.contains("world")) {
            player.sendSystemMessage(Component.translatable(
                "integratednbt:nbt_extractor_remote.need_bind"));
            return;
        }
        ClientLevel world = Minecraft.getInstance().level;
        if (world == null) {
            player.sendSystemMessage(Component.translatable(
                "integratednbt:nbt_extractor_remote.invalid_bind"));
            return;
        }
        if (!world.dimension().location().toString().equals(nbt.getString("world"))) {
            player.sendSystemMessage(Component.translatable(
                "integratednbt:nbt_extractor_remote.require_dim"));
            return;
        }
        BlockPos pos = new BlockPos(
            nbt.getInt("x"),
            nbt.getInt("y"),
            nbt.getInt("z")
        );
        if (!world.isAreaLoaded(pos, 1)) {
            player.sendSystemMessage(Component.translatable(
                "integratednbt:nbt_extractor_remote.require_load_client"));
            return;
        }
        if (world.getBlockState(pos).getBlock() != Additions.NBT_EXTRACTOR_BLOCK.get()) {
            player.sendSystemMessage(Component.translatable(
                "integratednbt:nbt_extractor_remote.invalid_bind"));
            return;
        }
        PacketHandler.INSTANCE.send(
            PacketDistributor.SERVER.noArg(),
            new NBTExtractorRemoteRequestMessage()
        );
    }

    public CompoundTag getModNBT(ItemStack itemStack) {
        return itemStack.getOrCreateTagElement(IntegratedNBT.MODID);
    }

    public void serverUse(ItemStack itemStack, ServerPlayer player) {
        CompoundTag nbt = this.getModNBT(itemStack);
        if (!nbt.contains("world")) {
            player.sendSystemMessage(Component.translatable(
                "integratednbt:nbt_extractor_remote.need_bind"));
            return;
        }
        ResourceKey<Level> dimensionKey = ResourceKey.create(Registries.DIMENSION, new ResourceLocation(nbt.getString("world")));
        MinecraftServer server = player.getServer();
        if (server == null) {
            player.sendSystemMessage(Component.translatable(
                "integratednbt:nbt_extractor_remote.invalid_bind"));
            return;
        }
        Level world = server.getLevel(dimensionKey);
        if (world == null) {
            player.sendSystemMessage(Component.translatable(
                "integratednbt:nbt_extractor_remote.invalid_bind"));
            return;
        }
        BlockPos pos = new BlockPos(
            nbt.getInt("x"),
            nbt.getInt("y"),
            nbt.getInt("z")
        );
        if (!world.isAreaLoaded(pos, 1)) {
            player.sendSystemMessage(Component.translatable(
                "integratednbt:nbt_extractor_remote.require_load_server"));
            return;
        }
        if (world.getBlockState(pos).getBlock() != Additions.NBT_EXTRACTOR_BLOCK.get()) {
            player.sendSystemMessage(Component.translatable(
                "integratednbt:nbt_extractor_remote.invalid_bind"));
            return;
        }
        Additions.NBT_EXTRACTOR_BLOCK.get().playerAccess(world, pos, player);
    }

    @Override
    @Nonnull
    public InteractionResult useOn(UseOnContext itemUseContext) {
        Level world = itemUseContext.getLevel();
        BlockPos pos = itemUseContext.getClickedPos();
        Player player = itemUseContext.getPlayer();
        if (player == null) {
            return InteractionResult.FAIL;
        }
        InteractionHand hand = itemUseContext.getHand();
        if (world.getBlockState(pos).getBlock() == Additions.NBT_EXTRACTOR_BLOCK.get()) {
            if (!world.isClientSide) {
                Additions.NBT_EXTRACTOR_REMOTE.get()
                    .bindBlock(player.getItemInHand(hand), world, pos);
                player.sendSystemMessage(Component.translatable(
                    "integratednbt:nbt_extractor_remote.bind_successful",
                    String.valueOf(pos.getX()),
                    String.valueOf(pos.getY()),
                    String.valueOf(pos.getZ())
                ));
            }
        } else if (world.isClientSide) {
            this.clientUse(player.getItemInHand(hand), player);
        }
        return InteractionResult.SUCCESS;
    }

    public void bindBlock(ItemStack itemStack, Level world, BlockPos pos) {
        CompoundTag nbt = this.getModNBT(itemStack);
        nbt.putString("world", world.dimension().location().toString());
        nbt.putInt("x", pos.getX());
        nbt.putInt("y", pos.getY());
        nbt.putInt("z", pos.getZ());
    }

    @Override
    public void appendHoverText(
        ItemStack itemStack,
        @Nullable Level world,
        List<Component> tooltip,
        TooltipFlag flag
    ) {
        super.appendHoverText(itemStack, world, tooltip, flag);
        CompoundTag nbt = this.getModNBT(itemStack);
        if (nbt.contains("world")) {
            tooltip.add(Component.translatable(
                "integratednbt:nbt_extractor_remote.tooltip.bound",
                String.valueOf(nbt.getInt("x")),
                String.valueOf(nbt.getInt("y")),
                String.valueOf(nbt.getInt("z")),
                nbt.getString("world")
            ).withStyle(style -> style.withColor(ChatFormatting.GREEN)));
        } else {
            tooltip.add(Component.translatable(
                "integratednbt:nbt_extractor_remote.tooltip.not_bound"));
        }
        tooltip.add(Component.translatable("integratednbt:nbt_extractor_remote.tooltip"));
    }
}
