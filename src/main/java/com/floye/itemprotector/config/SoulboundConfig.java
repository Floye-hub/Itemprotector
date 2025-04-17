package com.floye.itemprotector.config;

import com.google.gson.*;
import java.nio.file.*;

public class SoulboundConfig {
    private static String enchantmentId = "senessax:soulbound";
    private static final Path CONFIG_PATH = Paths.get("config/itemprotector_soulbound.json");

    public static void load() {
        try {
            // Crée le dossier config s'il n'existe pas
            Files.createDirectories(CONFIG_PATH.getParent());

            // Si le fichier n'existe pas, on le crée avec la valeur par défaut
            if (!Files.exists(CONFIG_PATH)) {
                String defaultConfig = "{\n" +
                        "  \"enchantmentId\": \"senessax:soulbound\"\n" +
                        "}";

                Files.writeString(CONFIG_PATH, defaultConfig);
                System.out.println("[ItemProtector] Fichier de configuration créé: " + CONFIG_PATH);
            }

            // Lit et parse la configuration
            JsonObject json = JsonParser.parseString(Files.readString(CONFIG_PATH)).getAsJsonObject();
            if (json.has("enchantmentId")) {
                enchantmentId = json.get("enchantmentId").getAsString();
                System.out.println("[ItemProtector] Enchantement configuré: " + enchantmentId);
            }
        } catch (Exception e) {
            System.err.println("[ItemProtector] Erreur de configuration: " + e.getMessage());
            // Continue avec la valeur par défaut
        }
    }

    public static String getEnchantmentId() {
        return enchantmentId;
    }
}