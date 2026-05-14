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
import dan200.computercraft.api.lua.LuaFunction;
import dan200.computercraft.api.peripheral.IComputerAccess;
import dan200.computercraft.api.peripheral.IPeripheral;
import dev.ithundxr.createnumismatics.content.backend.Coin;
import dev.ithundxr.createnumismatics.content.coins.DiscreteCoinBag;
import dev.ithundxr.createnumismatics.content.depositor.AndesiteDepositorBlockEntity;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.Optional;

public class AndesiteDepositorPeripheral implements IPeripheral {

    private final AndesiteDepositorBlockEntity blockEntity;

    public AndesiteDepositorPeripheral(AndesiteDepositorBlockEntity blockEntity) {
        this.blockEntity = blockEntity;
    }

    /* ----- Trigger coin ----- */

    @LuaFunction
    public final String getCoin() {
        return blockEntity.getCoin().getName();
    }

    @LuaFunction(mainThread = true)
    public final void setCoin(String coinName) throws LuaException {
        blockEntity.setCoin(PeripheralHelpers.coinFromName(coinName));
        blockEntity.notifyUpdate();
    }

    /* ----- Stored coins ----- */

    @LuaFunction
    public final int getStoredCoin(String coinName) throws LuaException {
        return blockEntity.getCoinBag().getDiscrete(PeripheralHelpers.coinFromName(coinName));
    }

    @LuaFunction
    public final Map<String, Integer> getStoredCoins() {
        return PeripheralHelpers.coinsAsMap(blockEntity.getCoinBag());
    }

    @LuaFunction
    public final int getStoredBalance() {
        return blockEntity.getCoinBag().getValue();
    }

    @LuaFunction(mainThread = true)
    public final int withdrawCoin(IComputerAccess computer, String toName, String coinName, Optional<Integer> amount) throws LuaException {
        IPeripheral target = PeripheralHelpers.lookupPeripheral(computer, toName, "Target");
        Coin coin = PeripheralHelpers.coinFromName(coinName);
        DiscreteCoinBag bag = blockEntity.getCoinBag();
        int requested = amount.orElse(bag.getDiscrete(coin));
        int moved = PeripheralHelpers.withdrawCoinTo(blockEntity, bag, coin, requested, target);
        if (moved > 0)
            blockEntity.notifyUpdate();
        return moved;
    }

    @LuaFunction(mainThread = true)
    public final int withdrawCoins(IComputerAccess computer, String toName) throws LuaException {
        IPeripheral target = PeripheralHelpers.lookupPeripheral(computer, toName, "Target");
        int moved = PeripheralHelpers.withdrawAllCoinsTo(blockEntity, blockEntity.getCoinBag(), target);
        if (moved > 0)
            blockEntity.notifyUpdate();
        return moved;
    }

    @Override
    public String getType() {
        return "Numismatics_AndesiteDepositor";
    }

    @Override
    public boolean equals(@Nullable IPeripheral other) {
        return other instanceof AndesiteDepositorPeripheral p && p.blockEntity == this.blockEntity;
    }
}
