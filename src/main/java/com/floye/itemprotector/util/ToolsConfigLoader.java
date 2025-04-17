package com.floye.itemprotector.util;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import com.floye.itemprotector.config.ToolsConfig;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class ToolsConfigLoader {
    private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();
    private static final Path configPath = Paths.get("config/tools_protection.json");

    public static ToolsConfig loadConfig() {
        try {
            if (!Files.exists(configPath)) {
                // Créer la configuration par défaut
                List<String> defaultTools = List.of(
                        "minecraft:diamond_pickaxe",
                        "minecraft:netherite_pickaxe",
                        "minecraft:diamond_axe",
                        "minecraft:netherite_axe"
                );

                ToolsConfig defaultConfig = new ToolsConfig(
                        defaultTools,
                        "§cYou need to repair your tool before using it!"
                );
                saveConfig(defaultConfig);
                return defaultConfig;
            }

            String json = Files.readString(configPath);
            return gson.fromJson(json, ToolsConfig.class);
        } catch (IOException e) {
            throw new RuntimeException("Failed to load tools config", e);
        }
    }

    public static void saveConfig(ToolsConfig config) {
        try {
            if (!Files.exists(configPath.getParent()))
                Files.createDirectories(configPath.getParent());
            Files.writeString(configPath, gson.toJson(config));
        } catch (IOException e) {
            throw new RuntimeException("Failed to save tools config", e);
        }
    }
}