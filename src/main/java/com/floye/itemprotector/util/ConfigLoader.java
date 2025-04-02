package com.floye.itemprotector.util;

import com.floye.itemprotector.config.ModConfig;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.util.List;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class ConfigLoader {
    private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();
    private static final Path configPath = Paths.get("config/dropmod.json");

    public static ModConfig loadConfig() {
        try {
            if (!Files.exists(configPath)) {
                ModConfig defaultConfig = new ModConfig(
                        List.of("minecraft:diamond", "minecraft:netherite_ingot"),
                        5,
                        "§cYou can’t drop that item yet! Please try again."
                );
                saveConfig(defaultConfig);
                return defaultConfig;
            }
            String json = Files.readString(configPath);
            return gson.fromJson(json, ModConfig.class);
        } catch (IOException e) {
            throw new RuntimeException("Failed to load config", e);
        }
    }

    public static void saveConfig(ModConfig config) {
        try {
            if (!Files.exists(configPath.getParent()))
                Files.createDirectories(configPath.getParent());
            Files.writeString(configPath, gson.toJson(config));
        } catch (IOException e) {
            throw new RuntimeException("Failed to save config", e);
        }
    }
}