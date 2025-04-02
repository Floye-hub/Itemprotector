package com.floye.itemprotector.mixin;

import com.floye.itemprotector.config.ModConfig;
import com.floye.itemprotector.util.ConfigManager;
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
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Mixin(PlayerEntity.class)
public abstract class PlayerEntityMixin {
    private static final Logger LOGGER = LogManager.getLogger("ItemProtector");
    private static final Map<UUID, Long> recentDropAttempts = new HashMap<>();

    @Inject(method = "dropItem", at = @At("HEAD"), cancellable = true)
    private void onDropItem(ItemStack stack, boolean entireStack, CallbackInfoReturnable<Boolean> cir) {
        PlayerEntity player = (PlayerEntity) (Object) this;

        LOGGER.debug("onDropItem called for player: {}", player.getName().getString());

        if (player instanceof ServerPlayerEntity serverPlayer && !player.getWorld().isClient()) {
            if (stack.isEmpty()) {
                LOGGER.debug("Stack is empty, canceling drop.");
                cir.setReturnValue(false);
                return;
            }

            // Vérification alternative pour les permissions de drop
            if (player.getAbilities().creativeMode) {
                LOGGER.debug("Player is in creative mode, allowing drop.");
                cir.setReturnValue(true); // Les joueurs en creative peuvent toujours drop
                return;
            }

            String itemId = Registries.ITEM.getId(stack.getItem()).toString();

            if (ConfigManager.CONFIG.whitelistItems.contains(itemId)) {
                UUID playerId = serverPlayer.getUuid();
                long now = System.currentTimeMillis();

                if (recentDropAttempts.containsKey(playerId)) {
                    long lastAttempt = recentDropAttempts.get(playerId);
                    if ((now - lastAttempt) <= ConfigManager.CONFIG.dropProtectionTimeout * 1000L) {
                        LOGGER.debug("Drop allowed due to recent attempt.");
                        recentDropAttempts.remove(playerId);
                        cir.setReturnValue(true);
                        return;
                    }
                }
                recentDropAttempts.put(playerId, now);
                cir.setReturnValue(true);
            } else {
                LOGGER.debug("Item {} is not whitelisted, canceling drop.", itemId);
                serverPlayer.sendMessage(Text.literal(ConfigManager.CONFIG.dropCancelMessage), true);
                cir.setReturnValue(false);
            }
        }
    }
}