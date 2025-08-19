package com.keerdm.cw_tweaks.setup;

import mcjty.lostcities.api.ILostCities;
import net.minecraftforge.fml.InterModComms;

import java.util.function.Function;

public class CwTweaksSetup {

    private static ILostCities lostCitiesAPI = null;

    public static void setupInterModComms() {
        InterModComms.sendTo("lostcities", "getLostCities", GetLostCities::new);
    }

    public static ILostCities getLostCitiesAPI() {
        return lostCitiesAPI;
    }

    public static class GetLostCities implements Function<ILostCities, Void> {
        @Override
        public Void apply(ILostCities lostCities) {
            lostCitiesAPI = lostCities;
            return null;
        }
    }
}