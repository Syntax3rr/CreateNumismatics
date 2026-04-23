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

package dev.ithundxr.createnumismatics.compat.computercraft.implementation.peripherals;

import dan200.computercraft.api.lua.LuaException;
import dan200.computercraft.api.peripheral.IComputerAccess;
import dan200.computercraft.api.peripheral.IPeripheral;
import dev.ithundxr.createnumismatics.compat.computercraft.implementation.PeripheralInventoryHelper;
import dev.ithundxr.createnumismatics.content.backend.Coin;
import dev.ithundxr.createnumismatics.content.coins.DiscreteCoinBag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;

import java.util.HashMap;
import java.util.Map;

final class PeripheralHelpers {

    private PeripheralHelpers() {}

    static Coin coinFromName(String name) throws LuaException {
        for (Coin coin : Coin.values()) {
            if (coin.getName().equalsIgnoreCase(name)) return coin;
        }
        throw new LuaException("incorrect coin name");
    }

    static Map<String, Integer> coinsAsMap(DiscreteCoinBag bag) {
        Map<String, Integer> result = new HashMap<>();
        for (Coin coin : Coin.values()) {
            result.put(coin.getName(), bag.getDiscrete(coin));
        }
        return result;
    }

    static IPeripheral lookupPeripheral(IComputerAccess computer, String name, String role) throws LuaException {
        IPeripheral target = computer.getAvailablePeripheral(name);
        if (target == null)
            throw new LuaException(role + " '" + name + "' does not exist");
        return target;
    }

    static void assertSlotInRange(int slot, int max, String label) throws LuaException {
        if (slot < 1 || slot > max)
            throw new LuaException(label + " slot out of range (1-" + max + ")");
    }

    /**
     * Push up to {@code amount} of {@code coin} into {@code target}. Returns the number of coins actually moved.
     */
    static int withdrawCoinTo(BlockEntity be, DiscreteCoinBag bag, Coin coin, int amount, IPeripheral target) {
        if (amount <= 0) return 0;

        int available = bag.getDiscrete(coin);
        int toMove = Math.min(amount, available);
        if (toMove <= 0) return 0;

        // Coin items are stack-size limited; let insertItemStacked spread across slots.
        ItemStack stack = coin.asStack(toMove);
        int moved = PeripheralInventoryHelper.insertItem(target, stack);
        if (moved > 0) {
            bag.subtract(coin, moved);
            be.setChanged();
        }
        return moved;
    }

    /** Push every coin in {@code bag} into {@code target}. Returns the total spur value moved. */
    static int withdrawAllCoinsTo(BlockEntity be, DiscreteCoinBag bag, IPeripheral target) {
        int totalSpurs = 0;
        for (Coin coin : Coin.values()) {
            int amount = bag.getDiscrete(coin);
            int moved = withdrawCoinTo(be, bag, coin, amount, target);
            totalSpurs += coin.toSpurs(moved);
        }
        return totalSpurs;
    }
}
