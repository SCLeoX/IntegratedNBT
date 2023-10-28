package me.tepis.integratednbt;

import me.tepis.integratednbt.network.PacketHandler;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(IntegratedNBT.MODID)
public class IntegratedNBT {
    public static final String MODID = "integratednbt";
    public static final Logger LOGGER = LogManager.getLogger(MODID);

    public IntegratedNBT() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        Additions.BLOCKS.register(modEventBus);
        Additions.ITEMS.register(modEventBus);
        Additions.CREATIVE_MODE_TABS.register(modEventBus);
        Additions.CONTAINER_TYPES.register(modEventBus);
        Additions.BLOCK_ENTITIES.register(modEventBus);
        PacketHandler.register();
    }
}
