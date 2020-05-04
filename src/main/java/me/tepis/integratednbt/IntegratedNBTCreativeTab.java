package me.tepis.integratednbt;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.ItemStack;

import javax.annotation.Nonnull;

public class IntegratedNBTCreativeTab extends CreativeTabs {
    private static IntegratedNBTCreativeTab instance;

    private IntegratedNBTCreativeTab() {
        super(IntegratedNBT.MODID);
    }

    public static IntegratedNBTCreativeTab getInstance() {
        if (instance == null) {
            instance = new IntegratedNBTCreativeTab();
        }
        return instance;
    }

    @Override
    @Nonnull
    public ItemStack createIcon() {
        return new ItemStack(NBTExtractor.getInstance());
    }
}
