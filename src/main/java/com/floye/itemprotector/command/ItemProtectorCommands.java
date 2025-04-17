package com.floye.itemprotector.command;

import com.floye.itemprotector.config.ModConfig;
import com.floye.itemprotector.util.ConfigLoader;
import com.floye.itemprotector.util.ProtectedItem;
import com.floye.itemprotector.util.SoulboundHelper;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.List;

public class ItemProtectorCommands {

    public static void register() {
        CommandRegistrationCallback.EVENT.register(ItemProtectorCommands::registerCommands);
    }

    private static void registerCommands(CommandDispatcher<ServerCommandSource> dispatcher,
                                         CommandRegistryAccess registryAccess,
                                         CommandManager.RegistrationEnvironment environment) {

        dispatcher.register(CommandManager.literal("ItemProtector")
                .executes(context -> protectHeldItem(context, false))
                .then(CommandManager.literal("withcomponents")
                        .executes(context -> protectHeldItem(context, true)))

        );
    }

    private static String getRelevantComponentsString(ItemStack stack) {
        StringBuilder builder = new StringBuilder();

        // Enchantements
        if (stack.hasEnchantments()) {
            builder.append("enchantments=").append(stack.getEnchantments().toString()).append(";");
        }

        // Vérifie si l'item est renommé
        if (!stack.getName().equals(stack.getItem().getName())) {
            builder.append("custom_name=").append(stack.getName().getString()).append(";");
        }

        return builder.length() > 0 ? builder.toString() : null;
    }

    private static int protectHeldItem(CommandContext<ServerCommandSource> context, boolean includeComponents) {
        ServerCommandSource source = context.getSource();

        if (source.getEntity() instanceof PlayerEntity player) {
            ItemStack heldItem = player.getMainHandStack();

            if (heldItem.isEmpty()) {
                source.sendFeedback(() -> Text.literal("§cYou have to hold the item in your hand"), false);
                return 0;
            }

            String itemId = Registries.ITEM.getId(heldItem.getItem()).toString();
            String componentsString = includeComponents ? getRelevantComponentsString(heldItem) : null;

            ModConfig config = ConfigLoader.loadConfig();
            List<ProtectedItem> protectedItems = config.getProtectedItems();

            // Vérifier si l'item est déjà protégé
            boolean alreadyProtected = protectedItems.stream()
                    .anyMatch(item -> item.getItemId().equals(itemId) &&
                            (!includeComponents ||
                                    (componentsString != null && componentsString.equals(item.getComponents()))));

            if (alreadyProtected) {
                source.sendFeedback(() -> Text.literal("§eCet objet est déjà protégé!"), false);
                return 0;
            }

            // Ajouter l'item à la liste des objets protégés
            ProtectedItem newProtectedItem = new ProtectedItem(
                    itemId,
                    includeComponents ? componentsString : null
            );

            List<ProtectedItem> newProtectedItems = new ArrayList<>(protectedItems);
            newProtectedItems.add(newProtectedItem);

            config.setProtectedItems(newProtectedItems);
            ConfigLoader.saveConfig(config);

            String componentsInfo = includeComponents ? (componentsString != null ? " avec composants" : " (sans composants)") : "";
            source.sendFeedback(() -> Text.literal("§aL'objet " + heldItem.getName().getString() + componentsInfo + " §a(" + itemId + ") a été ajouté à la liste des objets protégés!"), true);
            return 1;
        } else {
            source.sendFeedback(() -> Text.literal("§cCette commande doit être exécutée par un joueur!"), false);
            return 0;
        }
    }

}

    /**
     * Convertit un nombre en chiffres romains (pour l'affichage des niveaux)
     */

