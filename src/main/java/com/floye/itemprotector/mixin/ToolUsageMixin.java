package com.floye.itemprotector.mixin;

import com.floye.itemprotector.util.ToolsConfigManager;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.block.BlockState;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Item.class)
public class ToolUsageMixin {

    private static final Logger LOGGER = LogManager.getLogger("ItemProtector");

    /**
     * Intercepte l’action d’usure lors du minage.
     */
    @Inject(method = "postMine", at = @At("HEAD"), cancellable = true)
    private void onPostMine(ItemStack stack, World world, BlockState state, BlockPos pos, LivingEntity miner, CallbackInfoReturnable<Boolean> cir) {
        int maxDamage = stack.getMaxDamage();
        int remainingDurability = maxDamage - stack.getDamage();
        LOGGER.info("[postMine] Item utilisé : " + stack.getItem() + " - Durabilité restante : " + remainingDurability);

        if (maxDamage > 0 && remainingDurability <= 1) {
            String itemId = Registries.ITEM.getId(stack.getItem()).toString();
            if (ToolsConfigManager.CONFIG.getProtectedTools().contains(itemId)) {
                if (miner instanceof PlayerEntity player) {
                    player.sendMessage(Text.literal(ToolsConfigManager.CONFIG.getRepairMessage()), true);
                }
                // Annule la consommation de durabilité
                cir.setReturnValue(false);
                cir.cancel();
                LOGGER.info("[postMine] Action annulée pour l'item protégé : " + itemId);
            }
        }
    }

    /**
     * Intercepte l’action d’usure lors d’une attaque.
     */
    @Inject(method = "postHit", at = @At("HEAD"), cancellable = true)
    private void onPostHit(ItemStack stack, LivingEntity target, LivingEntity attacker, CallbackInfoReturnable<Boolean> cir) {
        int maxDamage = stack.getMaxDamage();
        int remainingDurability = maxDamage - stack.getDamage();
        LOGGER.info("[postHit] Item utilisé : " + stack.getItem() + " - Durabilité restante : " + remainingDurability);

        if (maxDamage > 0 && remainingDurability <= 1) {
            String itemId = Registries.ITEM.getId(stack.getItem()).toString();
            if (ToolsConfigManager.CONFIG.getProtectedTools().contains(itemId)) {
                if (attacker instanceof PlayerEntity player) {
                    player.sendMessage(Text.literal(ToolsConfigManager.CONFIG.getRepairMessage()), true);
                }
                cir.setReturnValue(false);
                cir.cancel();
                LOGGER.info("[postHit] Action annulée pour l'item protégé : " + itemId);
            }
        }
    }
    @Inject(method = "postDamageEntity", at = @At("HEAD"), cancellable = true)
    private void onPostDamageEntity(ItemStack stack, LivingEntity target, LivingEntity attacker, CallbackInfo ci) {
        int maxDamage = stack.getMaxDamage();
        int remainingDurability = maxDamage - stack.getDamage();
        LOGGER.info("[postDamageEntity] Item : " + stack.getItem() + " - Durabilité restante : " + remainingDurability);

        if (maxDamage > 0 && remainingDurability <= 1) {
            String itemId = Registries.ITEM.getId(stack.getItem()).toString();
            if (ToolsConfigManager.CONFIG.getProtectedTools().contains(itemId)) {
                if (attacker instanceof PlayerEntity player) {
                    player.sendMessage(Text.literal(ToolsConfigManager.CONFIG.getRepairMessage()), true);
                }
                LOGGER.info("[postDamageEntity] Action annulée pour l'item protégé : " + itemId);
                ci.cancel();
            }
        }
    }
}