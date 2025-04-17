package com.floye.itemprotector.mixin;

import com.floye.itemprotector.util.ToolsConfigManager;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.network.ServerPlayerInteractionManager;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ServerPlayerInteractionManager.class)
public abstract class PlayerInteractionManagerMixin {

    private static final Logger LOGGER = LogManager.getLogger("ItemProtector");

    // Shadow pour exposer le champ protégé "player".
    @Shadow
    protected ServerPlayerEntity player;

    /**
     * Intercepte la tentative de casse de bloc pour empêcher la destruction si l'outil est à 1 durabilité.
     */
    @Inject(method = "tryBreakBlock", at = @At("HEAD"), cancellable = true)
    private void onTryBreakBlock(BlockPos pos, CallbackInfoReturnable<Boolean> cir) {
        ItemStack stack = player.getMainHandStack();
        int maxDamage = stack.getMaxDamage();
        int remainingDurability = maxDamage - stack.getDamage();
        LOGGER.info("[tryBreakBlock] Held item: " + stack.getItem() + " - Durabilité restante: " + remainingDurability);

        if (maxDamage > 0 && remainingDurability <= 1) {
            String itemId = Registries.ITEM.getId(stack.getItem()).toString();
            if (ToolsConfigManager.CONFIG.getProtectedTools().contains(itemId)) {
                player.sendMessage(Text.literal(ToolsConfigManager.CONFIG.getRepairMessage()), true);
                cir.setReturnValue(false);
                cir.cancel();
                LOGGER.info("[tryBreakBlock] Casse du bloc annulée pour l'outil protégé: " + itemId);
            }
        }
    }
}