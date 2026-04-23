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
import dev.ithundxr.createnumismatics.compat.computercraft.implementation.PeripheralInventoryHelper;
import dev.ithundxr.createnumismatics.content.backend.Coin;
import dev.ithundxr.createnumismatics.content.coins.DiscreteCoinBag;
import dev.ithundxr.createnumismatics.content.vendor.VendorBlockEntity;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.Optional;

public class VendorPeripheral implements IPeripheral {

    private final VendorBlockEntity blockEntity;

    public VendorPeripheral(VendorBlockEntity blockEntity) {
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

    /* ----- Selling slot ----- */

    @LuaFunction
    public final @Nullable Map<String, ?> getSellingItem() {
        return PeripheralInventoryHelper.getItemDetail(new SellingSlotView(blockEntity), 1);
    }

    @LuaFunction(mainThread = true)
    public final int pullSellingItem(IComputerAccess computer, String fromName, int fromSlot, Optional<Integer> limit) throws LuaException {
        IPeripheral source = PeripheralHelpers.lookupPeripheral(computer, fromName, "Source");
        int actualLimit = limit.orElse(Integer.MAX_VALUE);
        int moved = PeripheralInventoryHelper.moveToContainer(
            source, fromSlot, blockEntity.sellingContainer, 1, actualLimit
        );
        if (moved > 0)
            blockEntity.notifyUpdate();
        return moved;
    }

    @LuaFunction(mainThread = true)
    public final int pushSellingItem(IComputerAccess computer, String toName, Optional<Integer> toSlot) throws LuaException {
        IPeripheral target = PeripheralHelpers.lookupPeripheral(computer, toName, "Target");
        int moved = PeripheralInventoryHelper.moveFromContainer(
            blockEntity.sellingContainer, 1, target, toSlot.orElse(0), Integer.MAX_VALUE
        );
        if (moved > 0)
            blockEntity.notifyUpdate();
        return moved;
    }

    /* ----- Stock inventory (matches CC's standard `minecraft:inventory` API) ----- */

    @LuaFunction(mainThread = true)
    public final int size() {
        int slots = PeripheralInventoryHelper.slotCount(this);
        return slots < 0 ? 0 : slots;
    }

    @LuaFunction(mainThread = true)
    public final Map<Integer, Map<String, ?>> list() {
        return PeripheralInventoryHelper.list(this);
    }

    @LuaFunction(mainThread = true)
    public final @Nullable Map<String, ?> getItemDetail(int slot) throws LuaException {
        PeripheralHelpers.assertSlotInRange(slot, Math.max(1, size()), "Stock");
        return PeripheralInventoryHelper.getItemDetail(this, slot);
    }

    @LuaFunction(mainThread = true)
    public final long getItemLimit(int slot) throws LuaException {
        PeripheralHelpers.assertSlotInRange(slot, Math.max(1, size()), "Stock");
        return PeripheralInventoryHelper.getSlotLimit(this, slot);
    }

    @LuaFunction(mainThread = true)
    public final int pushItems(IComputerAccess computer, String toName, int fromSlot, Optional<Integer> limit, Optional<Integer> toSlot) throws LuaException {
        IPeripheral target = PeripheralHelpers.lookupPeripheral(computer, toName, "Target");
        int slots = size();
        PeripheralHelpers.assertSlotInRange(fromSlot, slots, "From");
        int actualLimit = limit.orElse(Integer.MAX_VALUE);
        if (actualLimit <= 0) return 0;
        int moved = PeripheralInventoryHelper.moveItems(this, fromSlot, target, toSlot.orElse(0), actualLimit);
        if (moved > 0)
            blockEntity.notifyUpdate();
        return moved;
    }

    @LuaFunction(mainThread = true)
    public final int pullItems(IComputerAccess computer, String fromName, int fromSlot, Optional<Integer> limit, Optional<Integer> toSlot) throws LuaException {
        IPeripheral source = PeripheralHelpers.lookupPeripheral(computer, fromName, "Source");
        int actualLimit = limit.orElse(Integer.MAX_VALUE);
        if (actualLimit <= 0) return 0;
        int moved = PeripheralInventoryHelper.moveItems(source, fromSlot, this, toSlot.orElse(0), actualLimit);
        if (moved > 0)
            blockEntity.notifyUpdate();
        return moved;
    }

    @Override
    public Object getTarget() {
        return blockEntity;
    }

    @Override
    public String getType() {
        return "Numismatics_Vendor";
    }

    @Override
    public boolean equals(@Nullable IPeripheral other) {
        return other instanceof VendorPeripheral p && p.blockEntity == this.blockEntity;
    }

    /**
     * Tiny peripheral view of the vendor's 1-slot selling container, so {@link PeripheralInventoryHelper#getItemDetail}
     * can be reused for the selling slot without a dedicated common-side helper.
     */
    private record SellingSlotView(VendorBlockEntity be) implements IPeripheral {
        @Override
        public String getType() { return "Numismatics_VendorSellingSlot"; }

        @Override
        public Object getTarget() { return be.sellingContainer; }

        @Override
        public boolean equals(@Nullable IPeripheral other) { return other == this; }
    }
}
