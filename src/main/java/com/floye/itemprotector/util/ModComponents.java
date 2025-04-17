package com.floye.itemprotector.util;

import com.mojang.serialization.Codec;
import net.minecraft.component.ComponentType;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

public class ModComponents {
    public static final ComponentType<Boolean> SOULBOUND =
            ComponentType.<Boolean>builder()
                    .codec(Codec.BOOL)
                    .build();

    public static void register() {
        Registry.register(Registries.DATA_COMPONENT_TYPE,
                Identifier.of("itemprotector", "soulbound"),
                SOULBOUND);
    }
}