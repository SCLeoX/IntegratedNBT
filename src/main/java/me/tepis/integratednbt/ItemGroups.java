package me.tepis.integratednbt;

import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nonnull;
import java.util.function.Supplier;

public abstract class ItemGroups {
    public static class ModItemGroup extends CreativeModeTab {
        private final Supplier<ItemStack> iconSupplier;

        public ModItemGroup(@Nonnull String name, @Nonnull Supplier<ItemStack> iconSupplier) {
            super(name);
            this.iconSupplier = iconSupplier;
        }

        @Override
        @Nonnull
        public ItemStack makeIcon() {
            return this.iconSupplier.get();
        }
    }

    public static final CreativeModeTab ITEM_GROUP = new ModItemGroup(
        IntegratedNBT.MODID,
        () -> new ItemStack(Additions.NBT_EXTRACTOR_BLOCK_ITEM.get())
    );
}
