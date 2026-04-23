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

package dev.ithundxr.createnumismatics.compat.computercraft.implementation.neoforge;

import dan200.computercraft.api.detail.VanillaDetailRegistries;
import dan200.computercraft.api.peripheral.IPeripheral;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemHandlerHelper;
import net.neoforged.neoforge.items.wrapper.InvWrapper;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

public final class PeripheralInventoryHelperImpl {

    public static int slotCount(IPeripheral peripheral) {
        IItemHandler handler = extractHandler(peripheral);
        return handler == null ? -1 : handler.getSlots();
    }

    public static Map<Integer, Map<String, ?>> list(IPeripheral peripheral) {
        Map<Integer, Map<String, ?>> result = new HashMap<>();
        IItemHandler handler = extractHandler(peripheral);
        if (handler == null) return result;

        int slots = handler.getSlots();
        for (int i = 0; i < slots; i++) {
            ItemStack stack = handler.getStackInSlot(i);
            if (!stack.isEmpty())
                result.put(i + 1, VanillaDetailRegistries.ITEM_STACK.getBasicDetails(stack));
        }
        return result;
    }

    public static @Nullable Map<String, ?> getItemDetail(IPeripheral peripheral, int slot) {
        IItemHandler handler = extractHandler(peripheral);
        if (handler == null) return null;
        if (slot < 1 || slot > handler.getSlots()) return null;

        ItemStack stack = handler.getStackInSlot(slot - 1);
        return stack.isEmpty() ? null : VanillaDetailRegistries.ITEM_STACK.getDetails(stack);
    }

    public static long getSlotLimit(IPeripheral peripheral, int slot) {
        IItemHandler handler = extractHandler(peripheral);
        if (handler == null) return 0;
        if (slot < 1 || slot > handler.getSlots()) return 0;
        return handler.getSlotLimit(slot - 1);
    }

    public static int moveItems(IPeripheral source, int sourceSlot, IPeripheral target, int targetSlot, int limit) {
        IItemHandler from = extractHandler(source);
        IItemHandler to = extractHandler(target);
        if (from == null || to == null) return 0;
        return moveItem(from, sourceSlot - 1, to, targetSlot - 1, limit);
    }

    public static int moveFromContainer(Container source, int sourceSlot, IPeripheral target, int targetSlot, int limit) {
        IItemHandler from = new InvWrapper(source);
        IItemHandler to = extractHandler(target);
        if (to == null) return 0;
        return moveItem(from, sourceSlot - 1, to, targetSlot - 1, limit);
    }

    public static int moveToContainer(IPeripheral source, int sourceSlot, Container target, int targetSlot, int limit) {
        IItemHandler from = extractHandler(source);
        IItemHandler to = new InvWrapper(target);
        if (from == null) return 0;
        return moveItem(from, sourceSlot - 1, to, targetSlot - 1, limit);
    }

    public static int insertItem(IPeripheral target, ItemStack stack) {
        if (stack.isEmpty()) return 0;
        IItemHandler to = extractHandler(target);
        if (to == null) return 0;

        int initial = stack.getCount();
        ItemStack remainder = ItemHandlerHelper.insertItemStacked(to, stack.copy(), false);
        return initial - remainder.getCount();
    }

    /**
     * Resolve a peripheral's underlying inventory the same way ComputerCraft's {@code InventoryMethods.extractHandler}
     * does: prefer the item-handler capability of the target object, then fall back to raw {@code IItemHandler} or
     * {@link Container} wrapping.
     */
    private static @Nullable IItemHandler extractHandler(IPeripheral peripheral) {
        Object object = peripheral.getTarget();
        if (object == null) return null;

        if (object instanceof BlockEntity be) {
            if (be.isRemoved()) return null;
            Level level = be.getLevel();
            if (level != null) {
                IItemHandler cap = level.getCapability(Capabilities.ItemHandler.BLOCK, be.getBlockPos(), null);
                if (cap != null) return cap;
            }
        }
        if (object instanceof IItemHandler handler) return handler;
        if (object instanceof Container container) return new InvWrapper(container);
        return null;
    }

    /**
     * Move an item from one handler to another. Mirrors CC's private {@code moveItem}: pass {@code toSlot < 0} for
     * "any slot".
     */
    private static int moveItem(IItemHandler from, int fromSlot, IItemHandler to, int toSlot, int limit) {
        if (limit <= 0 || fromSlot < 0 || fromSlot >= from.getSlots())
            return 0;

        ItemStack simulated = from.extractItem(fromSlot, limit, true);
        if (simulated.isEmpty()) return 0;

        ItemStack inserted;
        if (toSlot >= 0) {
            if (toSlot >= to.getSlots()) return 0;
            ItemStack rejected = to.insertItem(toSlot, simulated.copy(), false);
            int moved = simulated.getCount() - rejected.getCount();
            if (moved <= 0) return 0;
            from.extractItem(fromSlot, moved, false);
            return moved;
        } else {
            ItemStack rejected = ItemHandlerHelper.insertItemStacked(to, simulated.copy(), false);
            int moved = simulated.getCount() - rejected.getCount();
            if (moved <= 0) return 0;
            from.extractItem(fromSlot, moved, false);
            return moved;
        }
    }
}
