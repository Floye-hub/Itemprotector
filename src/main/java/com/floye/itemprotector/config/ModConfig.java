package com.floye.itemprotector.config;

import com.floye.itemprotector.util.ProtectedItem;
import java.util.ArrayList;
import java.util.List;

public class ModConfig {
    // Suppression de whitelistItems
    public List<ProtectedItem> protectedItems; // Nouvelle liste pour les items avec composants
    public int dropProtectionTimeout; // in seconds
    public String dropCancelMessage;

    // Constructeur modifié
    public ModConfig(List<ProtectedItem> protectedItems,
                     int dropProtectionTimeout, String dropCancelMessage) {
        this.protectedItems = protectedItems;
        this.dropProtectionTimeout = dropProtectionTimeout;
        this.dropCancelMessage = dropCancelMessage;
    }

    public List<ProtectedItem> getProtectedItems() {
        if (protectedItems == null) {
            protectedItems = new ArrayList<>();
        }
        return protectedItems;
    }

    public void setProtectedItems(List<ProtectedItem> protectedItems) {
        this.protectedItems = protectedItems;
    }
}