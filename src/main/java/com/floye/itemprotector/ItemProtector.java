package com.floye.itemprotector;

import com.floye.itemprotector.command.ItemProtectorCommands;
import com.floye.itemprotector.config.ModConfig;
import com.floye.itemprotector.config.SoulboundConfig;
import com.floye.itemprotector.util.ConfigLoader;
import com.floye.itemprotector.util.ModComponents;
import com.floye.itemprotector.util.ToolsConfigLoader;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ItemProtector implements ModInitializer {
	public static final String MOD_ID = "itemprotector";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
	private static final ModConfig config;

	static {
		config = ConfigLoader.loadConfig();
		ToolsConfigLoader.loadConfig();
		SoulboundConfig.load();
	}

	@Override
	public void onInitialize() {
		ItemProtectorCommands.register();
		ModComponents.register();
		SoulboundManager.register();
	}
}
