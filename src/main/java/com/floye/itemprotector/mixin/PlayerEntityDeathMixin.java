package com.floye.itemprotector.mixin;

import com.floye.itemprotector.SoulboundManager;
import com.floye.itemprotector.util.SoulboundHelper;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.List;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mixin(PlayerEntity.class)
public class PlayerEntityDeathMixin {
    private static final Logger LOGGER = LogManager.getLogger("ItemProtector/Mixin");

    @Inject(method = "dropInventory", at = @At("HEAD"))
    private void captureSoulboundItems(CallbackInfo ci) {
        PlayerEntity player = (PlayerEntity) (Object) this;

        LOGGER.info("[Capture] Début pour {}", player.getName().getString());

        if (player.getWorld().isClient) {
            LOGGER.warn("Côté client - ignoré");
            return;
        }

        if (!(player instanceof ServerPlayerEntity serverPlayer)) {
            LOGGER.error("Pas un ServerPlayerEntity!");
            return;
        }

        PlayerInventory inventory = player.getInventory();
        List<ItemStack> soulboundItems = new ArrayList<>();

        LOGGER.info("Scan de l'inventaire ({} slots)", inventory.size());
        for (int i = 0; i < inventory.size(); i++) {
            ItemStack stack = inventory.getStack(i);
            if (SoulboundHelper.hasSoulboundEnchantment(stack)) {
                LOGGER.info("Trouvé item soulbound en slot {}: {}", i, stack.getName().getString());
                soulboundItems.add(stack.copy());
                inventory.setStack(i, ItemStack.EMPTY);
            }
        }

        if (!soulboundItems.isEmpty()) {
            LOGGER.info("Stockage de {} items soulbound", soulboundItems.size());
            SoulboundManager.storeItems(serverPlayer, soulboundItems);
        } else {
            LOGGER.info("Aucun item soulbound trouvé");
        }
    }
}
