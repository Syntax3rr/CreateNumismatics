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
import dev.ithundxr.createnumismatics.Numismatics;
import dev.ithundxr.createnumismatics.content.backend.BankAccount;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public enum BankTerminalPeripheral implements IPeripheral {
    INSTANCE
    ;

    @LuaFunction
    public final List<String> getAccounts() {
        List<String> output = new ArrayList<>();
        for (UUID uuid : Numismatics.BANK.accounts.keySet()) {
            output.add(uuid.toString());
        }
        return output;
    }

    @LuaFunction
    public final String getAccountLabel(String accountID) throws LuaException {
        BankAccount bankAccount = Numismatics.BANK.getAccount(parseUUID(accountID));
        if (bankAccount == null)
            throw new LuaException("Account not found");
        return bankAccount.getDisplayName().getString();
    }

    @LuaFunction
    public final boolean isPlayerOwned(String accountID) throws LuaException {
        BankAccount bankAccount = Numismatics.BANK.getAccount(parseUUID(accountID));
        if (bankAccount == null)
            throw new LuaException("Account not found");
        return bankAccount.type == BankAccount.Type.PLAYER;
    }

    @LuaFunction
    public final int getBalance(String accountID) throws LuaException {
        BankAccount bankAccount = Numismatics.BANK.getAccount(parseUUID(accountID));
        if (bankAccount == null)
            throw new LuaException("Account not found");
        return bankAccount.getBalance();
    }

    private static UUID parseUUID(String uuid) throws LuaException {
        try {
            return UUID.fromString(uuid);
        } catch (IllegalArgumentException e) {
            throw new LuaException("Invalid UUID");
        }
    }

    @Override
    public String getType() {
        return "Numismatics_BankTerminal";
    }

    @Override
    public void attach(IComputerAccess computer) {
        IPeripheral.super.attach(computer);
    }

    @Override
    public boolean equals(@Nullable IPeripheral other) {
        return other == INSTANCE;
    }
}
