package com.floye.itemprotector;

import com.floye.itemprotector.config.ModConfig;
import com.floye.itemprotector.util.ConfigLoader;
import net.fabricmc.api.ModInitializer;

public class ItemProtector implements ModInitializer {
	private static final ModConfig config;

	static {
		// Chargez la configuration depuis un fichier
		config = ConfigLoader.loadConfig();
	}

	@Override
	public void onInitialize() {
		// Initialisez la configuration pour le mixin

	}
}