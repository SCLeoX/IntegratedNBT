package me.tepis.integratednbt;

import net.minecraft.client.gui.ScreenManager;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.DeferredWorkQueue;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

@EventBusSubscriber(modid = IntegratedNBT.MODID, bus = EventBusSubscriber.Bus.MOD, value =
    Dist.CLIENT)
public final class ClientModEventSubscriber {
    @SubscribeEvent
    @SuppressWarnings("deprecation")
    public static void onFMLClientSetupEvent(final FMLClientSetupEvent event) {
        DeferredWorkQueue.runLater(() -> ScreenManager.registerFactory(
            Additions.NBT_EXTRACTOR_CONTAINER.get(),
            NBTExtractorScreen::new
        ));
    }
}