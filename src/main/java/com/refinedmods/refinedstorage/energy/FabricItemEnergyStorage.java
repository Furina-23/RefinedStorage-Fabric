package com.refinedmods.refinedstorage.energy;

import com.refinedmods.refinedstorage.transfer.energy.IEnergyStorage;
import net.fabricmc.fabric.api.transfer.v1.context.ContainerItemContext;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.StoragePreconditions;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import team.reborn.energy.api.EnergyStorage;

/** Energy access through the containing slot, preserving the existing Energy NBT. */
final class FabricItemEnergyStorage implements EnergyStorage {
    private final Item item;
    private final ContainerItemContext context;

    FabricItemEnergyStorage(Item item, ContainerItemContext context) {
        this.item = item;
        this.context = context;
    }

    private IEnergyStorage getStorage() {
        ItemVariant variant = context.getItemVariant();
        if (variant.isBlank() || variant.getItem() != item || !acceptsCount(context.getAmount())) {
            return null;
        }
        return ItemEnergyStorageFactory.get(variant.toStack());
    }

    @Override
    public boolean supportsInsertion() {
        IEnergyStorage storage = getStorage();
        return storage != null && storage.canReceive();
    }

    @Override
    public boolean supportsExtraction() {
        IEnergyStorage storage = getStorage();
        return storage != null && storage.canExtract();
    }

    @Override
    public long insert(long maxAmount, TransactionContext transaction) {
        return transfer(maxAmount, transaction, true);
    }

    @Override
    public long extract(long maxAmount, TransactionContext transaction) {
        return transfer(maxAmount, transaction, false);
    }

    private long transfer(long maxAmount, TransactionContext transaction, boolean insert) {
        StoragePreconditions.notNegative(maxAmount);
        if (getStorage() == null) {
            return 0;
        }
        ItemVariant original = context.getItemVariant();
        ItemStack updated = original.toStack();
        IEnergyStorage storage = ItemEnergyStorageFactory.get(updated);
        int limit = (int) Math.min(maxAmount, Integer.MAX_VALUE);
        int transferred = insert ? storage.receiveEnergy(limit, false) : storage.extractEnergy(limit, false);
        if (transferred <= 0) {
            return 0;
        }
        // Replace the single item atomically, rolling back if insertion fails.
        try (Transaction nested = transaction.openNested()) {
            if (context.extract(original, 1, nested) == 1
                && context.insert(ItemVariant.of(updated), 1, nested) == 1) {
                nested.commit();
                return transferred;
            }
        }
        return 0;
    }

    static boolean acceptsCount(long count) {
        return count == 1;
    }

    @Override
    public long getAmount() {
        IEnergyStorage storage = getStorage();
        return storage == null ? 0 : storage.getEnergyStored();
    }

    @Override
    public long getCapacity() {
        IEnergyStorage storage = getStorage();
        return storage == null ? 0 : storage.getMaxEnergyStored();
    }
}
