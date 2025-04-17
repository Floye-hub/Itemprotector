package com.floye.itemprotector.mixin;

import com.floye.itemprotector.util.ToolsConfigManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.text.Text;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
public abstract class LivingEntityDamageMixin {

    private static final Logger LOGGER = LogManager.getLogger("ItemProtector");

    /**
     * Intercepte l'application des dégâts sur une entité.
     * Si l'attaquant est un joueur qui utilise un outil protégé (à 1 durabilité),
     * on annule les dégâts infligés à l'entité.
     */
    @Inject(method = "damage", at = @At("HEAD"), cancellable = true)
    private void onDamage(DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
        Entity attacker = source.getAttacker();

        if (attacker instanceof PlayerEntity player) {
            ItemStack stack = player.getMainHandStack();
            int maxDamage = stack.getMaxDamage();

            if (maxDamage > 0) {
                int remainingDurability = maxDamage - stack.getDamage();
                LOGGER.info("[LivingEntity.damage] Attacker " + player.getName().getString() +
                        " utilise " + stack.getItem() + " - Durabilité restante: " + remainingDurability);

                if (remainingDurability <= 1) {
                    String itemId = Registries.ITEM.getId(stack.getItem()).toString();

                    if (ToolsConfigManager.CONFIG.getProtectedTools().contains(itemId)) {
                        player.sendMessage(Text.literal(ToolsConfigManager.CONFIG.getRepairMessage()), true);
                        LOGGER.info("[LivingEntity.damage] Dégâts annulés pour l'outil protégé: " + itemId);
                        // Annulation du dommage infligé à l'entité
                        cir.setReturnValue(false);
                        cir.cancel();
                        return;
                    }
                }
            }
        }
    }
}