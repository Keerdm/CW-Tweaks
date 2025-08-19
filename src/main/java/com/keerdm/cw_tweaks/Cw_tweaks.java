package com.keerdm.cw_tweaks;

import com.keerdm.cw_tweaks.setup.CwTweaksSetup;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.InterModEnqueueEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(Cw_tweaks.MODID)
public class Cw_tweaks {
    public static final String MODID = "cw_tweaks";

    public Cw_tweaks() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

        modEventBus.addListener(this::enqueueIMC);
    }
    
    private void enqueueIMC(final InterModEnqueueEvent event) {
        CwTweaksSetup.setupInterModComms();
    }
}
