package dev.confusedalex.thegoldeconomy.vault

import dev.confusedalex.thegoldeconomy.Bank
import dev.confusedalex.thegoldeconomy.Converter
import dev.confusedalex.thegoldeconomy.TheGoldEconomy.base
import dev.confusedalex.thegoldeconomy.Util
import net.milkbowl.vault2.economy.AccountPermission
import net.milkbowl.vault2.economy.Economy
import net.milkbowl.vault2.economy.EconomyResponse
import org.bukkit.Bukkit
import java.math.BigDecimal
import java.util.Optional
import java.util.ResourceBundle
import java.util.UUID

class VaultUnlockedEconomyImplementer(private val bank: Bank, private val util: Util, private val bundle: ResourceBundle) : Economy {
    val CURRENCY = "Gold"

    override fun isEnabled(): Boolean = true

    override fun getName(): String = "TheGoldEconomy"

    override fun hasSharedAccountSupport(): Boolean = false

    override fun hasMultiCurrencySupport(): Boolean = false

    override fun fractionalDigits(pluginName: String): Int = 0

    override fun format(amount: BigDecimal): String = "$amount $CURRENCY"

    override fun format(pluginName: String, amount: BigDecimal): String = format(amount)

    override fun format(amount: BigDecimal, currency: String): String = format(amount)

    override fun format(
        pluginName: String,
        amount: BigDecimal,
        currency: String
    ): String = format(amount)

    override fun hasCurrency(currency: String): Boolean = currency == CURRENCY

    override fun getDefaultCurrency(pluginName: String): String = CURRENCY

    override fun defaultCurrencyNamePlural(pluginName: String): String = CURRENCY

    override fun defaultCurrencyNameSingular(pluginName: String): String = CURRENCY

    override fun currencies(): Collection<String?> = listOf(CURRENCY)

    override fun createAccount(accountID: UUID, name: String): Boolean = true

    override fun createAccount(accountID: UUID, name: String, player: Boolean): Boolean = true

    override fun createAccount(
        accountID: UUID,
        name: String,
        worldName: String
    ): Boolean = true

    override fun createAccount(
        accountID: UUID,
        name: String,
        worldName: String,
        player: Boolean
    ): Boolean = true

    override fun getUUIDNameMap(): Map<UUID?, String?> =
        bank.playerAccounts.keys.associate { val uuid = UUID.fromString(it); uuid to Bukkit.getOfflinePlayer(uuid).name }

    override fun getAccountName(accountID: UUID): Optional<String> = Optional.ofNullable(Bukkit.getOfflinePlayer(accountID).name)

    override fun hasAccount(accountID: UUID): Boolean = true

    override fun hasAccount(accountID: UUID, worldName: String): Boolean = hasAccount(accountID)

    override fun renameAccount(accountID: UUID, name: String): Boolean = false

    override fun renameAccount(
        pluginName: String,
        accountID: UUID,
        name: String
    ): Boolean = false

    override fun deleteAccount(pluginName: String, accountID: UUID): Boolean = false

    override fun accountSupportsCurrency(
        pluginName: String,
        accountID: UUID,
        currency: String
    ): Boolean = currency == CURRENCY

    override fun accountSupportsCurrency(
        pluginName: String,
        accountID: UUID,
        currency: String,
        world: String
    ): Boolean = accountSupportsCurrency(pluginName, accountID, currency)

    override fun getBalance(pluginName: String, accountID: UUID): BigDecimal =
        BigDecimal(bank.getTotalPlayerBalance(accountID))

    override fun getBalance(
        pluginName: String,
        accountID: UUID,
        world: String
    ): BigDecimal = getBalance(pluginName, accountID)

    override fun getBalance(
        pluginName: String,
        accountID: UUID,
        world: String,
        currency: String
    ): BigDecimal = getBalance(pluginName, accountID)

    override fun has(
        pluginName: String,
        accountID: UUID,
        amount: BigDecimal
    ): Boolean = amount.toInt() < bank.getTotalPlayerBalance(accountID)

    override fun has(
        pluginName: String,
        accountID: UUID,
        worldName: String,
        amount: BigDecimal
    ): Boolean = has(pluginName, accountID, amount)

    override fun has(
        pluginName: String,
        accountID: UUID,
        worldName: String,
        currency: String,
        amount: BigDecimal
    ): Boolean = has(pluginName, accountID, amount)

    override fun withdraw(
        pluginName: String,
        accountID: UUID,
        amount: BigDecimal
    ): EconomyResponse {
        // if amount is negative return
        if (amount.signum() < 0) return EconomyResponse(amount, BigDecimal.ZERO, EconomyResponse.ResponseType.FAILURE, "error")

        val amountInt = amount.toInt()
        val offlinePlayer = Bukkit.getOfflinePlayer(accountID)
        val player = offlinePlayer.player

        // if player is online
        if (player != null) {
            // get balance and InventoryValue from Player
            val oldBankBalance = bank.getAccountBalance(accountID)
            val oldInventoryBalance = Converter.getInventoryValue(player, base)

            // If balance + InventoryValue is < amount, return
            if (amountInt > oldBankBalance + oldInventoryBalance)
                return EconomyResponse(amount, BigDecimal.ZERO, EconomyResponse.ResponseType.FAILURE, "Not enough money!")

            // If bank balance is enough to cover amount
            if (oldBankBalance - amountInt > 0) {
                bank.setAccountBalance(accountID, oldBankBalance - amountInt)
                return EconomyResponse(amount, BigDecimal(oldBankBalance - amountInt), EconomyResponse.ResponseType.SUCCESS, "")
            } else {
                // Set balance to 0 and cover rest of the costs with Inventory Funds
                val diff = amountInt - oldBankBalance
                bank.setAccountBalance(accountID, 0)
                Converter.remove(util, bundle)(player, diff, base)

                return EconomyResponse(amount, BigDecimal(oldInventoryBalance - amountInt), EconomyResponse.ResponseType.SUCCESS, "")
            }
        } else {
            // When player is offline
            val oldBalance = bank.getTotalPlayerBalance(accountID)
            val newBalance = oldBalance - amountInt
            bank.setAccountBalance(accountID, newBalance)
            return EconomyResponse(amount, BigDecimal(newBalance), EconomyResponse.ResponseType.SUCCESS, "")
        }
    }

    override fun withdraw(
        pluginName: String,
        accountID: UUID,
        worldName: String,
        amount: BigDecimal
    ): EconomyResponse = withdraw(pluginName, accountID, amount)

    override fun withdraw(
        pluginName: String,
        accountID: UUID,
        worldName: String,
        currency: String,
        amount: BigDecimal
    ): EconomyResponse = withdraw(pluginName, accountID, amount)

    override fun deposit(
        pluginName: String,
        accountID: UUID,
        amount: BigDecimal
    ): EconomyResponse {
        if (amount.signum() < 0) return EconomyResponse(amount, BigDecimal.ZERO, EconomyResponse.ResponseType.FAILURE, "error")

        val amountInt = amount.toInt()

        // Getting balance and calculating new Balance
        val oldBalance = bank.getAccountBalance(accountID)
        val newBalance = oldBalance + amountInt
        bank.setAccountBalance(accountID, newBalance)
        return EconomyResponse(amount, BigDecimal(newBalance), EconomyResponse.ResponseType.SUCCESS, "")
    }

    override fun deposit(
        pluginName: String,
        accountID: UUID,
        worldName: String,
        amount: BigDecimal
    ): EconomyResponse = deposit(pluginName, accountID, amount)

    override fun deposit(
        pluginName: String,
        accountID: UUID,
        worldName: String,
        currency: String,
        amount: BigDecimal
    ): EconomyResponse = deposit(pluginName, accountID, amount)

    override fun createSharedAccount(
        pluginName: String,
        accountID: UUID,
        name: String,
        owner: UUID
    ): Boolean = false

    override fun isAccountOwner(
        pluginName: String,
        accountID: UUID,
        uuid: UUID
    ): Boolean = false

    override fun setOwner(pluginName: String, accountID: UUID, uuid: UUID): Boolean = false

    override fun isAccountMember(
        pluginName: String,
        accountID: UUID,
        uuid: UUID
    ): Boolean = false

    override fun addAccountMember(
        pluginName: String,
        accountID: UUID,
        uuid: UUID
    ): Boolean = false

    override fun addAccountMember(
        pluginName: String,
        accountID: UUID,
        uuid: UUID,
        vararg initialPermissions: AccountPermission
    ): Boolean = false

    override fun removeAccountMember(
        pluginName: String,
        accountID: UUID,
        uuid: UUID
    ): Boolean = false

    override fun hasAccountPermission(
        pluginName: String,
        accountID: UUID,
        uuid: UUID,
        permission: AccountPermission
    ): Boolean = false

    override fun updateAccountPermission(
        pluginName: String,
        accountID: UUID,
        uuid: UUID,
        permission: AccountPermission,
        value: Boolean
    ): Boolean = false
}
