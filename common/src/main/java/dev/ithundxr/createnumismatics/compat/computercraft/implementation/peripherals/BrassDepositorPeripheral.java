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
import dev.ithundxr.createnumismatics.content.depositor.BrassDepositorBlockEntity;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.Optional;

public class BrassDepositorPeripheral implements IPeripheral {

    private final BrassDepositorBlockEntity blockEntity;

    public BrassDepositorPeripheral(BrassDepositorBlockEntity blockEntity) {
        this.blockEntity = blockEntity;
    }

    /* ----- Price ----- */

    @LuaFunction(mainThread = true)
    public final void setCoinAmount(String coinName, int amount) throws LuaException {
        Coin coin = PeripheralHelpers.coinFromName(coinName);
        blockEntity.setPrice(coin, amount);
        blockEntity.notifyUpdate();
    }

    @LuaFunction(mainThread = true)
    public final void setTotalPrice(int spurAmount) {
        int remaining = spurAmount;
        Coin[] coins = Coin.values();
        for (int i = coins.length - 1; i >= 0; i--) {
            Coin coin = coins[i];
            int count = remaining / coin.value;
            blockEntity.setPrice(coin, count);
            remaining -= count * coin.value;
        }
        blockEntity.notifyUpdate();
    }

    @LuaFunction
    public final int getTotalPrice() {
        return blockEntity.getTotalPrice();
    }

    @LuaFunction
    public final int getPrice(String coinName) throws LuaException {
        return blockEntity.getPrice(PeripheralHelpers.coinFromName(coinName));
    }

    /* ----- Stored coins ----- */

    @LuaFunction
    public final int getStoredCoin(String coinName) throws LuaException {
        return blockEntity.getCoinCount(PeripheralHelpers.coinFromName(coinName));
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
        return "Numismatics_Depositor";
    }

    @Override
    public boolean equals(@Nullable IPeripheral other) {
        return other instanceof BrassDepositorPeripheral p && p.blockEntity == this.blockEntity;
    }
}
