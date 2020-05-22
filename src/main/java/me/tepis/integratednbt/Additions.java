package me.tepis.integratednbt;

import me.tepis.integratednbt.NBTExtractor.NBTExtractorBlockItem;
import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.tileentity.TileEntityType;
import net.minecraftforge.common.extensions.IForgeContainerType;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

public abstract class Additions {
    public static final DeferredRegister<Block> BLOCKS = new DeferredRegister<>(
        ForgeRegistries.BLOCKS,
        IntegratedNBT.MODID
    );

    public static final RegistryObject<NBTExtractor> NBT_EXTRACTOR_BLOCK = BLOCKS.register(
        NBTExtractor.REGISTRY_NAME,
        () -> new NBTExtractor(Block.Properties.create(Material.ANVIL)
            .hardnessAndResistance(5.0F)
            .sound(SoundType.METAL))
    );

    public static final DeferredRegister<Item> ITEMS = new DeferredRegister<>(
        ForgeRegistries.ITEMS,
        IntegratedNBT.MODID
    );

    public static final RegistryObject<Item> NBT_EXTRACTOR_BLOCK_ITEM = ITEMS.register(
        NBTExtractor.REGISTRY_NAME,
        NBTExtractorBlockItem::new
    );

    public static final RegistryObject<NBTExtractorRemote> NBT_EXTRACTOR_REMOTE = ITEMS.register(
        NBTExtractorRemote.REGISTRY_NAME,
        NBTExtractorRemote::new
    );

    public static final DeferredRegister<ContainerType<?>> CONTAINER_TYPES = new DeferredRegister<>(
        ForgeRegistries.CONTAINERS,
        IntegratedNBT.MODID
    );

    public static final RegistryObject<ContainerType<NBTExtractorContainer>> NBT_EXTRACTOR_CONTAINER = CONTAINER_TYPES
        .register(
            NBTExtractor.REGISTRY_NAME,
            () -> IForgeContainerType.create(NBTExtractorContainer::new)
        );

    public static final DeferredRegister<TileEntityType<?>> TILE_ENTITY_TYPES =
        new DeferredRegister<>(
            ForgeRegistries.TILE_ENTITIES,
            IntegratedNBT.MODID
        );

    @SuppressWarnings("ConstantConditions")
    public static final RegistryObject<TileEntityType<NBTExtractorTileEntity>> NBT_EXTRACTOR_TILE_ENTITY = TILE_ENTITY_TYPES
        .register(NBTExtractor.REGISTRY_NAME, () ->
            TileEntityType.Builder.create(NBTExtractorTileEntity::new, NBT_EXTRACTOR_BLOCK.get())
                .build(null));
}
