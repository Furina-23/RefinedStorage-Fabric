package com.refinedmods.refinedstorage.transfer.energy;

import net.fabricmc.fabric.api.transfer.v1.storage.StoragePreconditions;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import net.fabricmc.fabric.api.transfer.v1.transaction.base.SnapshotParticipant;

public class EnergyStorage extends SnapshotParticipant<Integer>
    implements IEnergyStorage, team.reborn.energy.api.EnergyStorage {
    protected int energy;
    protected int capacity;
    protected int maxReceive;
    protected int maxExtract;

    public EnergyStorage(int capacity) {
        this(capacity, capacity, capacity);
    }

    public EnergyStorage(int capacity, int maxReceive, int maxExtract) {
        this.capacity = capacity;
        this.maxReceive = maxReceive;
        this.maxExtract = maxExtract;
    }

    @Override
    public int receiveEnergy(int maxReceive, boolean simulate) {
        if (!canReceive()) {
            return 0;
        }
        int received = Math.min(capacity - energy, Math.min(this.maxReceive, maxReceive));
        if (!simulate) {
            energy += received;
        }
        return received;
    }

    @Override
    public int extractEnergy(int maxExtract, boolean simulate) {
        if (!canExtract()) {
            return 0;
        }
        int extracted = Math.min(energy, Math.min(this.maxExtract, maxExtract));
        if (!simulate) {
            energy -= extracted;
        }
        return extracted;
    }

    @Override
    public int getEnergyStored() {
        return energy;
    }

    @Override
    public int getMaxEnergyStored() {
        return capacity;
    }

    @Override
    public boolean canExtract() {
        return maxExtract > 0;
    }

    @Override
    public boolean canReceive() {
        return maxReceive > 0;
    }

    @Override
    public boolean supportsInsertion() {
        return canReceive();
    }

    @Override
    public boolean supportsExtraction() {
        return canExtract();
    }

    @Override
    public long insert(long maxAmount, TransactionContext transaction) {
        StoragePreconditions.notNegative(maxAmount);
        int amount = receiveEnergy((int) Math.min(maxAmount, Integer.MAX_VALUE), true);
        if (amount > 0) {
            updateSnapshots(transaction);
            return receiveEnergy(amount, false);
        }
        return 0;
    }

    @Override
    public long extract(long maxAmount, TransactionContext transaction) {
        StoragePreconditions.notNegative(maxAmount);
        int amount = extractEnergy((int) Math.min(maxAmount, Integer.MAX_VALUE), true);
        if (amount > 0) {
            updateSnapshots(transaction);
            return extractEnergy(amount, false);
        }
        return 0;
    }

    @Override
    public long getAmount() {
        return getEnergyStored();
    }

    @Override
    public long getCapacity() {
        return getMaxEnergyStored();
    }

    @Override
    protected Integer createSnapshot() {
        return energy;
    }

    @Override
    protected void readSnapshot(Integer snapshot) {
        energy = snapshot;
    }
}


