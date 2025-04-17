package com.floye.itemprotector.mixin;

import com.floye.itemprotector.config.ModConfig;
import com.floye.itemprotector.util.ConfigLoader;
import com.floye.itemprotector.util.ConfigManager;
import com.floye.itemprotector.util.ProtectedItem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PlayerInventory.class)
public class PlayerEntityMixin {

    // Dans PlayerEntityMixin
    // Dans PlayerEntityMixin
    @Inject(method = "dropSelectedItem(Z)Lnet/minecraft/item/ItemStack;", at = @At("HEAD"), cancellable = true)
    private void dropSelectedItem(boolean entireStack, CallbackInfoReturnable<ItemStack> cir) {
        ModConfig config = ConfigLoader.loadConfig();
        PlayerEntity player = ((PlayerInventory)(Object)this).player;
        PlayerInventory inventory = (PlayerInventory) (Object) this;
        ItemStack stack = inventory.getStack(inventory.selectedSlot);

        String itemRegistryName = Registries.ITEM.getId(stack.getItem()).toString();
        String componentsString = getRelevantComponentsString(stack); // Utiliser getRelevantComponentsString ici

        // Vérifier si l'item est protégé
        boolean isProtected = false;
        for (ProtectedItem protectedItem : config.getProtectedItems()) {
            if (protectedItem.matches(itemRegistryName, componentsString)) {
                isProtected = true;
                break;
            }
        }

        if (isProtected) {
            player.sendMessage(Text.literal(ConfigManager.CONFIG.dropCancelMessage), true);
            cir.setReturnValue(ItemStack.EMPTY); // Empêche le drop
            cir.cancel();
        }
    }

    // Ajouter la méthode getRelevantComponentsString dans PlayerEntityMixin
    private String getRelevantComponentsString(ItemStack stack) {
        StringBuilder builder = new StringBuilder();

        // Enchantements
        if (stack.hasEnchantments()) {
            builder.append("enchantments=").append(stack.getEnchantments().toString()).append(";");
        }

        // Vérifie si l'item est renommé (comparaison avec le nom par défaut)
        if (!stack.getName().equals(stack.getItem().getName())) {
            builder.append("custom_name=").append(stack.getName().getString()).append(";");
        }

        return builder.length() > 0 ? builder.toString() : null;
    }
}
