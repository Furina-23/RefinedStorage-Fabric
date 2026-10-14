package com.refinedmods.refinedstorage.transfer.energy;

import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class FabricEnergyStorageTest {
    @Test
    void committedTransferRespectsRateAndLongInput() {
        EnergyStorage storage = new EnergyStorage(100, 30, 20);
        try (Transaction tx = Transaction.openOuter()) {
            assertEquals(30, storage.insert(Long.MAX_VALUE, tx));
            tx.commit();
        }
        try (Transaction tx = Transaction.openOuter()) {
            assertEquals(20, storage.extract(Long.MAX_VALUE, tx));
            tx.commit();
        }
        assertEquals(10, storage.getAmount());
    }

    @Test
    void nestedCommitRollsBackWithOuterTransaction() {
        EnergyStorage storage = new EnergyStorage(100);
        storage.receiveEnergy(40, false);
        try (Transaction outer = Transaction.openOuter()) {
            storage.insert(60, outer);
            try (Transaction nested = outer.openNested()) {
                storage.extract(100, nested);
                nested.commit();
            }
            assertEquals(0, storage.getAmount());
        }
        assertEquals(40, storage.getAmount());
    }

    @Test
    void respectsCapacityDisabledExtractionAndNegativeInput() {
        EnergyStorage storage = new EnergyStorage(100, 100, 0);
        assertFalse(storage.supportsExtraction());
        try (Transaction tx = Transaction.openOuter()) {
            assertEquals(100, storage.insert(Long.MAX_VALUE, tx));
            assertEquals(0, storage.insert(1, tx));
            assertEquals(0, storage.extract(1, tx));
            assertThrows(IllegalArgumentException.class, () -> storage.insert(-1, tx));
            assertThrows(IllegalArgumentException.class, () -> storage.extract(-1, tx));
            tx.commit();
        }
        assertEquals(100, storage.getAmount());
    }
}
