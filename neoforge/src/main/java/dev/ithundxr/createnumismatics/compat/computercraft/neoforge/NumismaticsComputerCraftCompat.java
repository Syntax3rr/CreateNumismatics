/*
 * Numismatics
 * Copyright (c) 2024 The Railways Team
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 */

package dev.ithundxr.createnumismatics.compat.computercraft.neoforge;

import com.google.common.collect.ImmutableMap;
import dan200.computercraft.api.detail.VanillaDetailRegistries;
import dan200.computercraft.api.peripheral.PeripheralCapability;
import dev.ithundxr.createnumismatics.compat.computercraft.implementation.peripherals.AndesiteDepositorPeripheral;
import dev.ithundxr.createnumismatics.compat.computercraft.implementation.peripherals.BankTerminalPeripheral;
import dev.ithundxr.createnumismatics.compat.computercraft.implementation.peripherals.BrassDepositorPeripheral;
import dev.ithundxr.createnumismatics.compat.computercraft.implementation.peripherals.VendorPeripheral;
import dev.ithundxr.createnumismatics.content.bank.CardItem;
import dev.ithundxr.createnumismatics.content.bank.IDCardItem;
import dev.ithundxr.createnumismatics.content.depositor.AndesiteDepositorBlockEntity;
import dev.ithundxr.createnumismatics.content.depositor.BrassDepositorBlockEntity;
import dev.ithundxr.createnumismatics.content.vendor.VendorBlockEntity;
import dev.ithundxr.createnumismatics.registry.NumismaticsBlockEntities;
import dev.ithundxr.createnumismatics.registry.NumismaticsBlocks;
import dev.ithundxr.createnumismatics.registry.NumismaticsTags;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.UUID;
import java.util.WeakHashMap;

public class NumismaticsComputerCraftCompat {

    private static final WeakHashMap<BrassDepositorBlockEntity, BrassDepositorPeripheral> depositorPeripherals = new WeakHashMap<>();
    private static final WeakHashMap<AndesiteDepositorBlockEntity, AndesiteDepositorPeripheral> andesiteDepositorPeripherals = new WeakHashMap<>();
    private static final WeakHashMap<VendorBlockEntity, VendorPeripheral> vendorPeripherals = new WeakHashMap<>();

    public static void registerCapabilities(RegisterCapabilitiesEvent event) {
        event.registerBlock(
            PeripheralCapability.get(),
            (level, pos, state, be, side) -> BankTerminalPeripheral.INSTANCE,
            NumismaticsBlocks.BANK_TERMINAL.get()
        );

        event.registerBlockEntity(
            PeripheralCapability.get(),
            NumismaticsBlockEntities.BRASS_DEPOSITOR.get(),
            (be, side) -> depositorPeripherals.computeIfAbsent(be, BrassDepositorPeripheral::new)
        );

        event.registerBlockEntity(
            PeripheralCapability.get(),
            NumismaticsBlockEntities.ANDESITE_DEPOSITOR.get(),
            (be, side) -> andesiteDepositorPeripherals.computeIfAbsent(be, AndesiteDepositorPeripheral::new)
        );

        event.registerBlockEntity(
            PeripheralCapability.get(),
            NumismaticsBlockEntities.VENDOR.get(),
            (be, side) -> vendorPeripherals.computeIfAbsent(be, VendorPeripheral::new)
        );

        VanillaDetailRegistries.ITEM_STACK.addProvider((detailMap, stack) -> {
            Map<Object, @Nullable Object> cardDetails = null;

            if (NumismaticsTags.AllItemTags.CARDS.matches(stack)) {
                UUID accountID = CardItem.get(stack);
                if (accountID != null) {
                    cardDetails = Map.of("AccountID", accountID.toString());
                }
            } else if (NumismaticsTags.AllItemTags.ID_CARDS.matches(stack)) {
                UUID id = IDCardItem.get(stack);
                if (id != null) {
                    cardDetails = Map.of("ID", id.toString());
                }
            }

            if (cardDetails != null)
                detailMap.put("numismatics", ImmutableMap.of("card", cardDetails));
        });
    }
}
