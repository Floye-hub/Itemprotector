package com.floye.itemprotector.mixin;

import com.floye.itemprotector.config.ModConfig;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.registry.Registries;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Mixin(PlayerEntity.class)
public abstract class PlayerEntityMixin {
    private static final Map<UUID, Long> recentDropAttempts = new HashMap<>();
    private static ModConfig config;

    // Méthode pour initialiser la configuration (à appeler dans votre mod principal)
    private static void setConfig(ModConfig modConfig) {
        config = modConfig;
    }

    @Inject(method = "dropItem", at = @At("HEAD"), cancellable = true)
    private void onDropItem(ItemStack stack, boolean entireStack, CallbackInfoReturnable<Boolean> cir) {
        PlayerEntity player = (PlayerEntity) (Object) this;
        if (player instanceof ServerPlayerEntity serverPlayer && !player.getWorld().isClient()) {
            // Instead of getting the stack from getMainHandStack(),
            // use the injected "stack" parameter:
            if (stack.isEmpty()) return; // Do nothing if the stack is empty

            String itemId = Registries.ITEM.getId(stack.getItem()).toString();

            // If the item is in the whitelist, allow it to be dropped
            if (config.whitelistItems.contains(itemId)) {
                UUID playerId = serverPlayer.getUuid();
                long now = System.currentTimeMillis();

                // Check the drop protection delay
                if (recentDropAttempts.containsKey(playerId)) {
                    long lastAttempt = recentDropAttempts.get(playerId);
                    if ((now - lastAttempt) <= config.dropProtectionTimeout * 1000L) {
                        recentDropAttempts.remove(playerId);
                        return; // Allow the drop after a rapid second attempt
                    }
                }

                recentDropAttempts.put(playerId, now);
                cir.setReturnValue(true); // Allow the drop to occur
            } else {
                // Block the drop and send a message
                serverPlayer.sendMessage(Text.literal(config.dropCancelMessage), true);
                cir.setReturnValue(false); // Cancel the drop
            }
        }
    }
}

