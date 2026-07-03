package com.redfox.ret;

import net.fabricmc.api.ModInitializer;

public final class MainEntryPoint implements ModInitializer {
    @Override
    public void onInitialize() {
        ServerTools.registerFlight();
    }
}
