package com.refinedmods.refinedstorage.energy;

import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.minecraft.SharedConstants;
import net.minecraft.server.Bootstrap;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.junit.jupiter.api.Test;
import team.reborn.energy.api.EnergyStorageUtil;
import team.reborn.energy.api.base.SimpleEnergyStorage;

import static org.junit.jupiter.api.Assertions.assertEquals;

class FabricItemEnergyIntegrationTest {
    @Test
    void apiTransferPersistsItemNbtAndRollsBackCancelledTransfer() {
        SharedConstants.tryDetectVersion();
        Bootstrap.bootStrap();
        ItemStack stack = new ItemStack(Items.DIAMOND);
        stack.getOrCreateTag().putInt("Energy", 20);
        ItemEnergyStorage target = new ItemEnergyStorage(stack, 100);
        SimpleEnergyStorage source = new SimpleEnergyStorage(100, 100, 100);
        source.amount = 100;
        try (Transaction tx = Transaction.openOuter()) {
            assertEquals(80, EnergyStorageUtil.move(source, target, Long.MAX_VALUE, tx));
            assertEquals(100, stack.getTag().getInt("Energy"));
        }
        assertEquals(100, source.getAmount());
        assertEquals(20, target.getAmount());
        assertEquals(20, stack.getTag().getInt("Energy"));
        assertEquals(30, EnergyStorageUtil.move(source, target, 30, null));
        assertEquals(70, source.getAmount());
        assertEquals(50, new ItemEnergyStorage(stack, 100).getAmount());
    }

    @Test
    void controllerBridgeRespectsInputLimitAndDisallowsOutput() {
        BaseEnergyStorage controller = new BaseEnergyStorage(100, 30, 0);
        SimpleEnergyStorage source = new SimpleEnergyStorage(100, 100, 100);
        source.amount = 100;
        assertEquals(30, EnergyStorageUtil.move(source, controller, Long.MAX_VALUE, null));
        assertEquals(70, source.getAmount());
        assertEquals(30, controller.getEnergyStored());
        assertEquals(0, EnergyStorageUtil.move(controller, source, 30, null));
        assertEquals(30, controller.getEnergyStored());
    }
}
