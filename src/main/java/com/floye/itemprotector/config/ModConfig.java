package com.floye.itemprotector.config;


import com.floye.itemprotector.util.ProtectedItem;
import java.util.ArrayList;
import java.util.List;

public class ModConfig {
    public List<String> whitelistItems; // Pour rétrocompatibilité
    public List<ProtectedItem> protectedItems; // Nouvelle liste pour les items avec composants
    public int dropProtectionTimeout; // in seconds
    public String dropCancelMessage;

    public ModConfig(List<String> whitelistItems, int dropProtectionTimeout, String dropCancelMessage) {
        this.whitelistItems = whitelistItems;
        this.dropProtectionTimeout = dropProtectionTimeout;
        this.dropCancelMessage = dropCancelMessage;
        this.protectedItems = new ArrayList<>();

        // Convertir les anciens éléments de la whitelist en ProtectedItems
        if (whitelistItems != null) {
            for (String item : whitelistItems) {
                this.protectedItems.add(new ProtectedItem(item, null));
            }
        }
    }

    // Constructeur complet pour la nouvelle version
    public ModConfig(List<String> whitelistItems, List<ProtectedItem> protectedItems,
                     int dropProtectionTimeout, String dropCancelMessage) {
        this.whitelistItems = whitelistItems;
        this.protectedItems = protectedItems;
        this.dropProtectionTimeout = dropProtectionTimeout;
        this.dropCancelMessage = dropCancelMessage;
    }

    public List<String> getWhitelistItems() {
        if (whitelistItems == null) {
            whitelistItems = new ArrayList<>();
        }
        return whitelistItems;
    }

    public List<ProtectedItem> getProtectedItems() {
        if (protectedItems == null) {
            protectedItems = new ArrayList<>();

            // Convertir les anciens éléments de la whitelist en ProtectedItems si nécessaire
            if (whitelistItems != null) {
                for (String item : whitelistItems) {
                    protectedItems.add(new ProtectedItem(item, null));
                }
            }
        }
        return protectedItems;
    }

    public void setProtectedItems(List<ProtectedItem> protectedItems) {
        this.protectedItems = protectedItems;

        // Mettre à jour whitelistItems pour la rétrocompatibilité
        this.whitelistItems = new ArrayList<>();
        for (ProtectedItem item : protectedItems) {
            if (!whitelistItems.contains(item.getItemId())) {
                whitelistItems.add(item.getItemId());
            }
        }
    }
}