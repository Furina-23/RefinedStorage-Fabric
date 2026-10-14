package com.refinedmods.refinedstorage.energy;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FabricItemStackEnergyTest {
    @Test
    void acceptsExactlyOneItem() {
        assertTrue(FabricItemEnergyStorage.acceptsCount(1));
        assertFalse(FabricItemEnergyStorage.acceptsCount(0));
        assertFalse(FabricItemEnergyStorage.acceptsCount(2));
        assertFalse(FabricItemEnergyStorage.acceptsCount(64));
        assertFalse(FabricItemEnergyStorage.acceptsCount(Long.MAX_VALUE));
    }
}
