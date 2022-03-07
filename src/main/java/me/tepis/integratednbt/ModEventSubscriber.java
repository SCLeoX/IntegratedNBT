package me.tepis.integratednbt;

import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.DeferredWorkQueue;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import org.cyclops.integrateddynamics.core.evaluate.operator.OperatorRegistry;
import org.cyclops.integrateddynamics.core.item.VariableFacadeHandlerRegistry;

@EventBusSubscriber(modid = IntegratedNBT.MODID, bus = EventBusSubscriber.Bus.MOD)
public final class ModEventSubscriber {
    @SubscribeEvent
    @SuppressWarnings("deprecation")
    public static void onSetup(FMLCommonSetupEvent event) {
        event.enqueueWork(() -> {
            VariableFacadeHandlerRegistry.getInstance()
                .registerHandler(new NBTExtractedVariableFacadeHandler());
            OperatorRegistry.getInstance()
                .registerSerializer(new NBTExtractionOperatorSerializer());
        });
    }
}