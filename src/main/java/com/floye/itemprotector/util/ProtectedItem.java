package com.floye.itemprotector.util;

public class ProtectedItem {
    private String itemId;
    private String components;

    public ProtectedItem(String itemId, String components) {
        this.itemId = itemId;
        this.components = components;
    }

    public String getItemId() {
        return itemId;
    }

    public String getComponents() {
        return components;
    }

    public boolean matches(String itemId, String components) {
        if (!this.itemId.equals(itemId)) {
            return false;
        }

        if (this.components == null) {
            return true; // Protège tous les items de ce type, quels que soient les composants
        }

        return this.components.equals(components); // Vérifie que les composants correspondent
    }
}