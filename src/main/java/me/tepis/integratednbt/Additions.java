package me.tepis.integratednbt;

import me.tepis.integratednbt.NBTExtractor.NBTExtractorBlockItem;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.common.extensions.IForgeMenuType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public abstract class Additions {
    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(
        ForgeRegistries.BLOCKS,
        IntegratedNBT.MODID
    );

    public static final RegistryObject<NBTExtractor> NBT_EXTRACTOR_BLOCK = BLOCKS.register(
        NBTExtractor.REGISTRY_NAME,
        () -> new NBTExtractor(Block.Properties.of(Material.HEAVY_METAL)
            .strength(5.0F)
            .sound(SoundType.METAL))
    );

    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(
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

    public static final DeferredRegister<MenuType<?>> CONTAINER_TYPES = DeferredRegister.create(
        ForgeRegistries.CONTAINERS,
        IntegratedNBT.MODID
    );

    public static final RegistryObject<MenuType<NBTExtractorContainer>> NBT_EXTRACTOR_CONTAINER = CONTAINER_TYPES
        .register(
            NBTExtractor.REGISTRY_NAME,
            () -> IForgeMenuType.create(NBTExtractorContainer::new)
        );

    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(
        ForgeRegistries.BLOCK_ENTITIES,
        IntegratedNBT.MODID
    );

    public static final RegistryObject<BlockEntityType<NBTExtractorBE>> NBT_EXTRACTOR_BE = BLOCK_ENTITIES.register(
        NBTExtractor.REGISTRY_NAME,
        () -> BlockEntityType.Builder.of(NBTExtractorBE::new, NBT_EXTRACTOR_BLOCK.get()).build(null)
    );
//    public static final RegistryObject<BlockEntityType<?>> TILE_ENTITY_TYPES =
//        DeferredRegister.create(
//            ForgeRegistries.BLOCK_ENTITIES,
//            IntegratedNBT.MODID
//        );
//
//    @SuppressWarnings("ConstantConditions")
//    public static final RegistryObject<BlockEntityType<NBTExtractorBE>> NBT_EXTRACTOR_TILE_ENTITY = TILE_ENTITY_TYPES
//        .register(NBTExtractor.REGISTRY_NAME, () ->
//            BlockEntityType.Builder.of(NBTExtractorBE::new, NBT_EXTRACTOR_BLOCK.get())
//                .build(null));
}
