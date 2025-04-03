package com.floye.itemprotector.util;

import com.floye.itemprotector.config.ModConfig;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.util.List;
import java.util.ArrayList;
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
                // Créer la configuration par défaut
                List<String> defaultWhitelist = List.of("minecraft:diamond", "minecraft:netherite_ingot");
                List<ProtectedItem> defaultProtectedItems = new ArrayList<>();

                // Convertir les éléments de la whitelist en ProtectedItems
                for (String item : defaultWhitelist) {
                    defaultProtectedItems.add(new ProtectedItem(item, null));
                }

                ModConfig defaultConfig = new ModConfig(
                        defaultWhitelist,
                        defaultProtectedItems,
                        5,
                        "§cYou can't drop that item yet! Please try again."
                );
                saveConfig(defaultConfig);
                return defaultConfig;
            }

            String json = Files.readString(configPath);
            ModConfig config = gson.fromJson(json, ModConfig.class);

            // Si c'est une ancienne configuration sans protectedItems, initialiser cette liste
            if (config.protectedItems == null) {
                config.protectedItems = new ArrayList<>();
                if (config.whitelistItems != null) {
                    for (String item : config.whitelistItems) {
                        config.protectedItems.add(new ProtectedItem(item, null));
                    }
                }
            }

            return config;
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