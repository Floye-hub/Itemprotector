package com.floye.itemprotector.mixin;

import com.floye.itemprotector.config.ModConfig;
import com.floye.itemprotector.util.ConfigLoader;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.text.Text;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ScreenHandler.class)
public abstract class ScreenHandlerMixin {
    @Unique
    private static final Logger LOGGER = LogManager.getLogger("ItemProtector");

    @Unique
    private boolean itemprotector_confirmDrop = false;

    @Inject(
            method = "onSlotClick(IILnet/minecraft/screen/slot/SlotActionType;Lnet/minecraft/entity/player/PlayerEntity;)V",
            at = @At("HEAD"),
            cancellable = true
    )
    private void protectAllDrops(int slotIndex, int button, SlotActionType actionType, PlayerEntity player, CallbackInfo ci) {
        ScreenHandler handler = (ScreenHandler) (Object) this;

        // Logs pour déboguer l'index du slot et le type d'action
        LOGGER.info("Slot clicked: index={}, button={}, actionType={}, player={}", slotIndex, button, actionType, player.getName().getString());

        // Ignore l'action PICKUP car elle ne concerne pas les drops
        if (actionType == SlotActionType.PICKUP) {
            LOGGER.info("Ignoring PICKUP action.");
            return;
        }

        // Vérifie si l'action est un drop (THROW ou QUICK_MOVE)
        if (actionType == SlotActionType.THROW || actionType == SlotActionType.QUICK_MOVE) {
            LOGGER.info("Detected potential drop action: {}", actionType);

            ItemStack stack = null;

            // Vérifie si l'action vient du curseur
            if (slotIndex == -999) { // Index du curseur dans 1.21.1
                stack = handler.getCursorStack();
                LOGGER.info("Cursor stack detected: {}", stack);
            }
            // Vérifie si l'action provient d'un slot classique de l'inventaire
            else if (slotIndex >= 0 && slotIndex < handler.slots.size()) {
                stack = handler.getSlot(slotIndex).getStack();
                LOGGER.info("Slot stack detected: {}", stack);
            }

            // Vérifie si l'item existe et n'est pas vide
            if (stack != null && !stack.isEmpty()) {
                LOGGER.info("Item stack is not empty: {}", stack);

                ModConfig config = ConfigLoader.loadConfig();
                String itemId = Registries.ITEM.getId(stack.getItem()).toString();
                LOGGER.info("Item ID: {}", itemId);

                // Si l'item est protégé (dans la whitelist)
                if (config.getWhitelistItems().contains(itemId)) {
                    LOGGER.info("Item is protected: {}", itemId);

                    // Demande une confirmation immédiate pour le drop
                    if (!itemprotector_confirmDrop) {
                        player.sendMessage(Text.literal("§cItem protégé! §7Cliquez à nouveau pour confirmer le drop."), true);
                        LOGGER.info("Drop confirmation required for protected item: {}", itemId);
                        itemprotector_confirmDrop = true;
                        ci.cancel(); // Annule le drop pour attendre la confirmation
                    } else {
                        LOGGER.info("Drop confirmed for protected item: {}", itemId);
                        itemprotector_confirmDrop = false; // Réinitialise après confirmation
                    }
                } else {
                    LOGGER.info("Item is not protected: {}", itemId);
                    itemprotector_confirmDrop = false; // Réinitialise si l'item n'est pas protégé
                }
            } else {
                LOGGER.info("Item stack is empty or null.");
                itemprotector_confirmDrop = false; // Réinitialise si aucun item n'est détecté
            }
        } else {
            // Réinitialise la confirmation pour les autres actions
            itemprotector_confirmDrop = false;
        }
    }

    @Inject(
            method = "onClosed(Lnet/minecraft/entity/player/PlayerEntity;)V",
            at = @At("HEAD")
    )
    private void resetConfirmation(PlayerEntity player, CallbackInfo ci) {
        LOGGER.info("Inventory closed for player: {}", player.getName().getString());
        itemprotector_confirmDrop = false; // Réinitialise la confirmation quand l'inventaire est fermé
    }
}