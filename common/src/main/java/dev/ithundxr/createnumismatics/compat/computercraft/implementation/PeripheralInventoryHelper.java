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

package dev.ithundxr.createnumismatics.compat.computercraft.implementation;

import dan200.computercraft.api.peripheral.IPeripheral;
import dev.architectury.injectables.annotations.ExpectPlatform;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

/**
 * Helpers mirroring ComputerCraft's standard {@code minecraft:inventory} peripheral. All target-peripheral lookups
 * are resolved via {@link IPeripheral#getTarget()} and the platform's item-handler capability, matching the behaviour
 * of {@code dan200.computercraft.shared.peripheral.generic.methods.InventoryMethods}.
 */
public class PeripheralInventoryHelper {

    /** Slot count of the peripheral's underlying inventory; -1 if it isn't an inventory. */
    @ExpectPlatform
    public static int slotCount(IPeripheral peripheral) {
        throw new AssertionError();
    }

    @ExpectPlatform
    public static Map<Integer, Map<String, ?>> list(IPeripheral peripheral) {
        throw new AssertionError();
    }

    @ExpectPlatform
    public static @Nullable Map<String, ?> getItemDetail(IPeripheral peripheral, int slot) {
        throw new AssertionError();
    }

    @ExpectPlatform
    public static long getSlotLimit(IPeripheral peripheral, int slot) {
        throw new AssertionError();
    }

    /** Move items between two peripherals. Slot args are 1-based; pass {@code <= 0} for {@code toSlot} to allow any slot. */
    @ExpectPlatform
    public static int moveItems(IPeripheral source, int sourceSlot, IPeripheral target, int targetSlot, int limit) {
        throw new AssertionError();
    }

    /** Move items from a vanilla {@link Container} slot into a target peripheral. */
    @ExpectPlatform
    public static int moveFromContainer(Container source, int sourceSlot, IPeripheral target, int targetSlot, int limit) {
        throw new AssertionError();
    }

    /** Move items from a source peripheral into a vanilla {@link Container} slot. */
    @ExpectPlatform
    public static int moveToContainer(IPeripheral source, int sourceSlot, Container target, int targetSlot, int limit) {
        throw new AssertionError();
    }

    /** Insert an item stack into a peripheral. Returns the count actually inserted. */
    @ExpectPlatform
    public static int insertItem(IPeripheral target, ItemStack stack) {
        throw new AssertionError();
    }
}
