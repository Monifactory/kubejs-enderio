package com.almostreliable.kubeio.enderio.conduit;

import net.minecraft.world.item.Item;

public record CustomConduitEntry(String id, String name, int transferRate, Item item) {
}
