// SoulboundHelper.java
package com.floye.itemprotector.util;

import com.floye.itemprotector.config.SoulboundConfig;
import net.minecraft.item.ItemStack;
import net.minecraft.component.type.ItemEnchantmentsComponent;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.util.Identifier;

public class SoulboundHelper {
    static {
        SoulboundConfig.load();
    }

    public static boolean hasSoulboundEnchantment(ItemStack stack) {
        if (stack == null || stack.isEmpty() || !stack.hasEnchantments()) return false;

        try {
            ItemEnchantmentsComponent enchantments = stack.getEnchantments();
            String targetId = String.valueOf(SoulboundConfig.getEnchantmentId());

            for (RegistryEntry<Enchantment> entry : enchantments.getEnchantments()) {
                RegistryKey<Enchantment> key = entry.getKey().orElse(null);
                if (key != null && targetId.equals(key.getValue().toString())) {
                    return true;
                }
            }
        } catch (Exception e) {
            System.err.println("Error checking soulbound: " + e.getMessage());
        }

        return false;
    }

    public static int getSoulboundLevel(ItemStack stack) {
        if (stack == null || stack.isEmpty() || !stack.hasEnchantments()) return 0;

        try {
            ItemEnchantmentsComponent enchantments = stack.getEnchantments();
            String targetId = String.valueOf(SoulboundConfig.getEnchantmentId());

            for (RegistryEntry<Enchantment> entry : enchantments.getEnchantments()) {
                RegistryKey<Enchantment> key = entry.getKey().orElse(null);
                if (key != null && targetId.equals(key.getValue().toString())) {
                    return enchantments.getLevel(entry);
                }
            }
        } catch (Exception e) {
            System.err.println("Error getting soulbound level: " + e.getMessage());
        }

        return 0;
    }
}