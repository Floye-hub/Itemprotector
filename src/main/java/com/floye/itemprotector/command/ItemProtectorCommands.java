package com.floye.itemprotector.command;

import com.floye.itemprotector.config.ModConfig;
import com.floye.itemprotector.util.ConfigLoader;
import com.floye.itemprotector.util.ProtectedItem;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.List;

public class ItemProtectorCommands {

    public static void register() {
        CommandRegistrationCallback.EVENT.register(ItemProtectorCommands::registerCommands);
    }

    private static void registerCommands(CommandDispatcher<ServerCommandSource> dispatcher,
                                         CommandRegistryAccess registryAccess,
                                         CommandManager.RegistrationEnvironment environment) {

        dispatcher.register(CommandManager.literal("protectitem")
                .executes(context -> protectHeldItem(context, false))
                .then(CommandManager.literal("withcomponents")
                        .executes(context -> protectHeldItem(context, true)))
        );
    }

    private static int protectHeldItem(CommandContext<ServerCommandSource> context, boolean includeComponents) {
        ServerCommandSource source = context.getSource();

        if (source.getEntity() instanceof PlayerEntity player) {
            ItemStack heldItem = player.getMainHandStack();

            if (heldItem.isEmpty()) {
                source.sendFeedback(() -> Text.literal("§cVous devez tenir un objet en main pour le protéger!"), false);
                return 0;
            }

            String itemId = Registries.ITEM.getId(heldItem.getItem()).toString();
            String componentsString = includeComponents ? heldItem.getComponents().toString() : null;

            ModConfig config = ConfigLoader.loadConfig();
            List<ProtectedItem> protectedItems = config.getProtectedItems();

            // Vérifier si l'item est déjà protégé
            boolean alreadyProtected = false;
            for (ProtectedItem item : protectedItems) {
                if (item.getItemId().equals(itemId)) {
                    if (!includeComponents || item.getComponents() == null) {
                        alreadyProtected = true;
                        break;
                    } else if (componentsString != null && componentsString.equals(item.getComponents())) {

                    alreadyProtected = true;
                        break;
                    }
                }
            }

            if (alreadyProtected) {
                source.sendFeedback(() -> Text.literal("§eCet objet est déjà protégé!"), false);
                return 0;
            }

            // Ajouter l'item à la liste des objets protégés
            ProtectedItem newProtectedItem = new ProtectedItem(
                    itemId,
                    includeComponents && !heldItem.getComponents().isEmpty() ? componentsString : null
            );

            List<ProtectedItem> newProtectedItems = new ArrayList<>(protectedItems);
            newProtectedItems.add(newProtectedItem);

            config.setProtectedItems(newProtectedItems);
            ConfigLoader.saveConfig(config);

            String componentsInfo = includeComponents && !heldItem.getComponents().isEmpty() ? " avec composants" : (includeComponents ? " (sans composants)" : "");
            source.sendFeedback(() -> Text.literal("§aL'objet " + heldItem.getName().getString() + componentsInfo + " §a(" + itemId + ") a été ajouté à la liste des objets protégés!"), true);
            return 1;
        } else {
            source.sendFeedback(() -> Text.literal("§cCette commande doit être exécutée par un joueur!"), false);
            return 0;
        }
    }
}