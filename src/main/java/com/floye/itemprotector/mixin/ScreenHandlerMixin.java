package com.floye.itemprotector.mixin;

import com.floye.itemprotector.config.ModConfig;
import com.floye.itemprotector.util.ConfigLoader;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.server.MinecraftServer;
import net.minecraft.text.Text;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ScreenHandler.class)
public abstract class ScreenHandlerMixin {

    @Unique
    private boolean itemprotector_confirmDrop = false;

    @Unique
    private static final Text PROTECTED_ITEM_MESSAGE = Text.literal("§c[ItemProtector] §rThis item is protected! Click again to confirm the drop.");

    @Unique
    private static String getRelevantComponentsString(ItemStack stack) {
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

    @Unique
    private boolean isItemProtected(ModConfig config, ItemStack stack) {
        String itemId = Registries.ITEM.getId(stack.getItem()).toString();
        String componentsString = getRelevantComponentsString(stack);
        return config.getProtectedItems().stream().anyMatch(p -> p.matches(itemId, componentsString));
    }

    @Inject(
            method = "onSlotClick(IILnet/minecraft/screen/slot/SlotActionType;Lnet/minecraft/entity/player/PlayerEntity;)V",
            at = @At("HEAD"),
            cancellable = true
    )
    private void protectAllDrops(int slotIndex, int button, SlotActionType actionType, PlayerEntity player, CallbackInfo ci) {
        ScreenHandler handler = (ScreenHandler) (Object) this;

        ItemStack stack = null;

        boolean isPotentialDrop = false;

        System.out.println("[ItemProtector] SlotActionType: " + actionType);
        System.out.println("[ItemProtector] Button: " + button);
        System.out.println("[ItemProtector] SlotIndex: " + slotIndex);

        if (actionType == SlotActionType.THROW ) {
            isPotentialDrop = true;
        } else if (actionType == SlotActionType.PICKUP && (slotIndex == -999 || button == 1)) {
            isPotentialDrop = true;
        }

        if (isPotentialDrop) {
            if (slotIndex == -999) {
                stack = handler.getCursorStack();
            } else if (slotIndex >= 0 && slotIndex < handler.slots.size()) {
                stack = handler.getSlot(slotIndex).getStack();
            }

            if (stack != null && !stack.isEmpty()) {
                // Vérification côté serveur :
                World world = player.getWorld();
                if (!world.isClient()) { // C'est l'équivalent de `world instanceof ServerWorld`
                    ModConfig config = ConfigLoader.loadConfig();
                    boolean isProtected = isItemProtected(config, stack);

                    if (isProtected) {
                        if (!itemprotector_confirmDrop) {
                            player.sendMessage(PROTECTED_ITEM_MESSAGE, false);
                            itemprotector_confirmDrop = true;
                            ci.cancel();
                            System.out.println("[ItemProtector] Drop annulé (première confirmation) - SERVEUR"); // Log
                        } else {
                            itemprotector_confirmDrop = false;
                            System.out.println("[ItemProtector] Drop confirmé - SERVEUR"); // Log
                        }
                    } else {
                        itemprotector_confirmDrop = false;
                    }
                }
            } else {
                itemprotector_confirmDrop = false;
            }
        } else {
            itemprotector_confirmDrop = false;
        }
    }

    @Inject(
            method = "onClosed(Lnet/minecraft/entity/player/PlayerEntity;)V",
            at = @At("HEAD")
    )
    private void resetConfirmation(PlayerEntity player, CallbackInfo ci) {
        itemprotector_confirmDrop = false;
        System.out.println("[ItemProtector] Confirmation réinitialisée lors de la fermeture de l'inventaire"); // Log
    }
}