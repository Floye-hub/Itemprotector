package com.floye.itemprotector.config;

import java.util.List;

public class ModConfig {
    public List<String> whitelistItems;
    public int dropProtectionTimeout; // in seconds
    public String dropCancelMessage;

    public ModConfig(List<String> whitelistItems, int dropProtectionTimeout, String dropCancelMessage) {
        this.whitelistItems = whitelistItems;
        this.dropProtectionTimeout = dropProtectionTimeout;
        this.dropCancelMessage = dropCancelMessage;
    }
}