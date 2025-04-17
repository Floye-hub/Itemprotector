package com.floye.itemprotector;

import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.*;
import java.util.stream.Collectors;

public class SoulboundManager {
    private static final Logger LOGGER = LogManager.getLogger("ItemProtector/Soulbound");
    private static final Map<UUID, List<ItemStack>> pendingItems = new HashMap<>();

    public static void register() {
        ServerPlayerEvents.AFTER_RESPAWN.register((oldPlayer, newPlayer, alive) -> {
            // Ne plus se fier au paramètre 'alive'
            if (!newPlayer.isAlive()) {  // Vérification directe
                LOGGER.error("ERREUR CRITIQUE: Le joueur {} est toujours mort après respawn!",
                        newPlayer.getName().getString());
                return;
            }

            UUID playerId = newPlayer.getUuid();
            List<ItemStack> items = pendingItems.remove(playerId);

            if (items == null) {
                LOGGER.debug("Aucun item à restaurer (normal si pas mort)");
                return;
            }

            LOGGER.info("Restauration de {} items pour {} (UUID: {})",
                    items.size(), newPlayer.getName().getString(), playerId);

            restoreItems(newPlayer, items);
        });

    }

    public static void storeItems(ServerPlayerEntity player, List<ItemStack> items) {
        if (items.isEmpty()) {
            LOGGER.warn("Tentative de stockage d'une liste vide pour {}", player.getName().getString());
            return;
        }

        pendingItems.put(player.getUuid(), items);
        LOGGER.debug("{} items stockés pour {} (UUID: {})",
                items.size(), player.getName().getString(), player.getUuid());
    }

    private static void restoreItems(ServerPlayerEntity player, List<ItemStack> items) {
        PlayerInventory inventory = player.getInventory();
        int restored = 0;
        int dropped = 0;

        for (ItemStack stack : items) {
            if (stack.isEmpty()) {
                LOGGER.warn("Item vide dans la liste de restauration");
                continue;
            }

            boolean placed = false;
            for (int i = 0; i < inventory.size(); i++) {
                if (inventory.getStack(i).isEmpty()) {
                    inventory.setStack(i, stack);
                    placed = true;
                    restored++;
                    LOGGER.debug("Item restauré dans le slot {}: {}", i, stack.getItem().getName().getString());
                    break;
                }
            }

            if (!placed) {
                player.dropItem(stack, false);
                dropped++;
                player.sendMessage(Text.literal("§cUn item soulbound a été dropé (inventaire plein)"), false);
                LOGGER.warn("Item dropé: {}", stack.getItem().getName().getString());
            }
        }

        LOGGER.info("Restauration terminée: {} items replacés, {} items dropés", restored, dropped);

        // Log critique si tout a été dropé
        if (restored == 0 && dropped > 0) {
            LOGGER.error("AUCUN ITEM N'A PU ÊTRE RESTAURÉ - INVENTAIRE PLEIN?");
            player.sendMessage(Text.literal("§4Erreur: Tous vos items soulbound ont été dropés!"), false);
        }
    }
}