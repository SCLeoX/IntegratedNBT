package me.tepis.integratednbt;

import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

@EventBusSubscriber(modid = IntegratedNBT.MODID, bus = EventBusSubscriber.Bus.MOD, value =
    Dist.CLIENT)
public final class ClientModEventSubscriber {
    @SubscribeEvent
    public static void onFMLClientSetupEvent(final FMLClientSetupEvent event) {
        event.enqueueWork(() -> MenuScreens.register(
            Additions.NBT_EXTRACTOR_CONTAINER.get(),
            NBTExtractorScreen::new
        ));
    }
}