package dev.confusedalex.thegoldeconomy

import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockbukkit.mockbukkit.MockBukkit
import org.mockbukkit.mockbukkit.ServerMock
import org.mockbukkit.mockbukkit.entity.PlayerMock
import java.util.*

class BankCommandTest {
    private lateinit var server: ServerMock
    private lateinit var plugin: TheGoldEconomy
    private lateinit var bankCommand: BankCommand
    private lateinit var sender: PlayerMock
    private lateinit var target: PlayerMock
    private val prefix = "TheGoldEconomy"

    @BeforeEach
    fun setUp() {
        server = MockBukkit.mock()
        plugin = MockBukkit.load(TheGoldEconomy::class.java)
        bankCommand = BankCommand(plugin)
        plugin.eco.bank.playerAccounts.clear()
        plugin.eco.bank.fakeAccounts.clear()
        sender = server.addPlayer("sender")
        target = server.addPlayer("target")
    }

    @AfterEach
    fun tearDown() { // Stop the mock server
        MockBukkit.unmock()
    }

    @Test
    fun balance_will_return_correct_balance() {
        plugin.eco.bank.setAccountBalance(sender.uniqueId, 100)

        bankCommand.balance(sender, null)

        assertEquals(
            plugin.util.formatMessage(
                String.format(plugin.bundle.getString("info.balance"), 100, 100, 0), prefix
            ), sender.nextMessage()
        )
    }

    @Test
    fun balance_of_others_without_permission_not_allowed() {
        plugin.eco.bank.setAccountBalance(target.uniqueId, 50)
        sender.addAttachment(plugin, "thegoldeconomy.balance.others", false)

        bankCommand.balance(sender, target)

        assertEquals(
            plugin.util.formatMessage(plugin.bundle.getString("error.noPermission"), prefix), sender.nextMessage()
        )
    }

    @Test
    fun balance_of_others_with_permission_allowed() {
        plugin.eco.bank.setAccountBalance(target.uniqueId, 50)
        sender.addAttachment(plugin, "thegoldeconomy.balance.others", true)

        bankCommand.balance(sender, target)

        assertEquals(
            plugin.util.formatMessage(
                String.format(
                    plugin.bundle.getString("info.balance.other"), target.name, 50
                ), prefix
            ), sender.nextMessage()
        )
    }

    @Test
    fun pay_with_valid_amount_will_notify_target() {
        plugin.eco.bank.setAccountBalance(sender.uniqueId, 100)

        bankCommand.pay(sender, target, 50)

        assertEquals(
            plugin.util.formatMessage(
                String.format(plugin.bundle.getString("info.moneyReceived"), 50, sender.name), prefix
            ), target.nextMessage()
        )
    }

    @Test
    fun pay_with_valid_amount_will_deduct_from_sender() {
        plugin.eco.bank.setAccountBalance(sender.uniqueId, 100)

        bankCommand.pay(sender, target, 50)

        assertEquals(50, plugin.eco.bank.getAccountBalance(sender.uniqueId))
    }

    @Test
    fun pay_with_valid_amount_will_add_to_target() {
        plugin.eco.bank.setAccountBalance(sender.uniqueId, 100)

        bankCommand.pay(sender, target, 50)

        assertEquals(50, plugin.eco.bank.getAccountBalance(target.uniqueId))
    }

    @Test
    fun pay_with_valid_amount_will_notify_sender() {
        plugin.eco.bank.setAccountBalance(sender.uniqueId, 100)

        bankCommand.pay(sender, target, 50)

        assertEquals(
            plugin.util.formatMessage(
                String.format(plugin.bundle.getString("info.sendMoneyTo"), 50, target.name), prefix
            ), sender.nextMessage()
        )
    }

    @Test
    fun pay_of_zero_will_send_error() {
        plugin.eco.bank.setAccountBalance(sender.uniqueId, 100)

        bankCommand.pay(sender, target, 0)

        assertEquals(
            plugin.util.formatMessage(
                String.format(plugin.bundle.getString("error.zero")), prefix
            ), sender.nextMessage()
        )
    }

    @Test
    fun pay_of_zero_will_not_deduct_from_sender() {
        plugin.eco.bank.setAccountBalance(sender.uniqueId, 100)

        bankCommand.pay(sender, target, 0)

        assertEquals(100, plugin.eco.bank.getAccountBalance(sender.uniqueId))
    }

    @Test
    fun pay_of_zero_will_not_add_to_target() {
        plugin.eco.bank.setAccountBalance(sender.uniqueId, 100)

        bankCommand.pay(sender, target, 0)

        assertEquals(0, plugin.eco.bank.getAccountBalance(target.uniqueId))
    }

    @Test
    fun pay_of_negative_amount_will_send_error() {
        plugin.eco.bank.setAccountBalance(sender.uniqueId, 100)

        bankCommand.pay(sender, target, -50)

        assertEquals(
            plugin.util.formatMessage(
                String.format(plugin.bundle.getString("error.negative")), prefix
            ), sender.nextMessage()
        )
    }

    @Test
    fun pay_of_negative_amount_will_not_deduct_from_sender() {
        plugin.eco.bank.setAccountBalance(sender.uniqueId, 100)

        bankCommand.pay(sender, target, -50)

        assertEquals(100, plugin.eco.bank.getAccountBalance(sender.uniqueId))
    }

    @Test
    fun pay_of_negative_amount_will_not_add_to_target() {
        plugin.eco.bank.setAccountBalance(sender.uniqueId, 100)

        bankCommand.pay(sender, target, -50)

        assertEquals(0, plugin.eco.bank.getAccountBalance(target.uniqueId))
    }

    @Test
    fun pay_of_insufficient_amount_will_send_error() {
        plugin.eco.bank.setAccountBalance(sender.uniqueId, 100)

        bankCommand.pay(sender, target, 200)

        assertEquals(
            plugin.util.formatMessage(
                String.format(plugin.bundle.getString("error.notEnough")), prefix
            ), sender.nextMessage()
        )
    }

    @Test
    fun pay_of_insufficient_amount_will_not_deduct_from_sender() {
        plugin.eco.bank.setAccountBalance(sender.uniqueId, 100)

        bankCommand.pay(sender, target, 200)

        assertEquals(100, plugin.eco.bank.getAccountBalance(sender.uniqueId))
    }

    @Test
    fun pay_of_insufficient_amount_will_not_add_to_target() {
        plugin.eco.bank.setAccountBalance(sender.uniqueId, 100)

        bankCommand.pay(sender, target, 200)

        assertEquals(0, plugin.eco.bank.getAccountBalance(target.uniqueId))
    }

    @Test
    fun paying_yourself_will_send_error() {
        plugin.eco.bank.setAccountBalance(sender.uniqueId, 100)

        bankCommand.pay(sender, sender, 50)

        assertEquals(
            plugin.util.formatMessage(
                String.format(plugin.bundle.getString("error.payYourself")), prefix
            ), sender.nextMessage()
        )
    }

    @Test
    fun paying_yourself_will_not_change_balance() {
        plugin.eco.bank.setAccountBalance(sender.uniqueId, 100)

        bankCommand.pay(sender, sender, 50)

        assertEquals(100, plugin.eco.bank.getAccountBalance(sender.uniqueId))
    }

    @Test
    fun paying_not_existing_player_will_send_error() {
        plugin.eco.bank.setAccountBalance(sender.uniqueId, 100)

        bankCommand.pay(sender, server.getOfflinePlayer(UUID.randomUUID()), 50)

        assertEquals(
            plugin.util.formatMessage(
                String.format(plugin.bundle.getString("error.noPlayer")), prefix
            ), sender.nextMessage()
        )
    }

    @Test
    fun paying_not_existing_player_will_not_deduct_from_sender() {
        plugin.eco.bank.setAccountBalance(sender.uniqueId, 100)

        bankCommand.pay(sender, server.getOfflinePlayer(UUID.randomUUID()), 50)

        assertEquals(100, plugin.eco.bank.getAccountBalance(sender.uniqueId))
    }

    @Test
    fun paying_offline_player_with_sufficient_funds_will_notify_sender() {
        plugin.eco.bank.setAccountBalance(sender.uniqueId, 100)
        target.disconnect()

        bankCommand.pay(sender, target, 50)

        assertEquals(
            plugin.util.formatMessage(
                String.format(plugin.bundle.getString("info.sendMoneyTo"), 50, target.name), prefix
            ), sender.nextMessage()
        )
    }


    @Test
    fun paying_offline_player_with_sufficient_funds_will_deduct_from_sender() {
        plugin.eco.bank.setAccountBalance(sender.uniqueId, 100)
        target.disconnect()

        bankCommand.pay(sender, target, 50)

        assertEquals(50, plugin.eco.bank.getAccountBalance(sender.uniqueId))
    }

    @Test
    fun paying_offline_player_with_sufficient_funds_will_add_to_target() {
        plugin.eco.bank.setAccountBalance(sender.uniqueId, 100)
        target.disconnect()

        bankCommand.pay(sender, target, 50)

        assertEquals(50, plugin.eco.bank.getAccountBalance(target.uniqueId))
    }

    @Test
    fun deposit_with_specific_sufficient_amount_will_notify_sender() {
        plugin.eco.converter.give(sender, 50, Base.NUGGETS)

        bankCommand.deposit(sender, "25")

        assertEquals(
            plugin.util.formatMessage(
                String.format(plugin.bundle.getString("info.deposit"), 25), prefix
            ), sender.nextMessage()
        )
    }

    @Test
    fun deposit_with_specific_sufficient_amount_will_add_to_account() {
        plugin.eco.converter.give(sender, 50, Base.NUGGETS)

        bankCommand.deposit(sender, "25")

        assertEquals(25, plugin.eco.bank.getAccountBalance(sender.uniqueId))
    }

    @Test
    fun deposit_with_specific_sufficient_amount_will_deduct_from_inventory() {
        plugin.eco.converter.give(sender, 50, Base.NUGGETS)

        bankCommand.deposit(sender, "25")

        assertEquals(25, plugin.eco.converter.getInventoryValue(sender, Base.NUGGETS))
    }

    @Test
    fun deposit_of_all_with_sufficient_amount_will_notify_sender() {
        plugin.eco.converter.give(sender, 50, Base.NUGGETS)

        bankCommand.deposit(sender, "all")

        assertEquals(
            plugin.util.formatMessage(
                String.format(plugin.bundle.getString("info.deposit"), 50), prefix
            ), sender.nextMessage()
        )
    }

    @Test
    fun deposit_of_all_with_sufficient_amount_will_add_to_account() {
        plugin.eco.converter.give(sender, 50, Base.NUGGETS)

        bankCommand.deposit(sender, "all")

        assertEquals(50, plugin.eco.bank.getAccountBalance(sender.uniqueId))
    }

    @Test
    fun deposit_of_all_with_sufficient_amount_will_deduct_from_inventory() {
        plugin.eco.converter.give(sender, 50, Base.NUGGETS)

        bankCommand.deposit(sender, "all")

        assertEquals(0, plugin.eco.converter.getInventoryValue(sender, Base.NUGGETS))
    }

    @Test
    fun deposit_with_no_amount_will_notify_sender() {
        plugin.eco.converter.give(sender, 278, Base.NUGGETS)

        bankCommand.deposit(sender, null)

        assertEquals(
            plugin.util.formatMessage(
                String.format(plugin.bundle.getString("info.deposit"), 278), prefix
            ), sender.nextMessage()
        )
    }

    @Test
    fun deposit_with_no_amount_will_add_all_gold_from_inventory_to_account() {
        plugin.eco.converter.give(sender, 278, Base.NUGGETS)

        bankCommand.deposit(sender, null)

        assertEquals(278, plugin.eco.bank.getAccountBalance(sender.uniqueId))
    }

    @Test
    fun deposit_with_no_amount_will_deduct_all_gold_from_inventory() {
        plugin.eco.converter.give(sender, 278, Base.NUGGETS)

        bankCommand.deposit(sender, null)

        assertEquals(0, plugin.eco.converter.getInventoryValue(sender, Base.NUGGETS))
    }

    @Test
    fun deposit_with_insufficient_amount_will_send_error() {
        plugin.eco.converter.give(sender, 50, Base.NUGGETS)

        bankCommand.deposit(sender, "100")

        assertEquals(
            plugin.util.formatMessage(
                String.format(plugin.bundle.getString("error.notEnough")), prefix
            ), sender.nextMessage()
        )
    }

    @Test
    fun deposit_with_insufficient_amount_will_not_add_to_account() {
        plugin.eco.converter.give(sender, 50, Base.NUGGETS)

        bankCommand.deposit(sender, "100")

        assertEquals(0, plugin.eco.bank.getAccountBalance(sender.uniqueId))
    }

    @Test
    fun deposit_with_insufficient_amount_will_not_deduct_from_inventory() {
        plugin.eco.converter.give(sender, 50, Base.NUGGETS)

        bankCommand.deposit(sender, "100")

        assertEquals(50, plugin.eco.converter.getInventoryValue(sender, Base.NUGGETS))
    }

    @Test
    fun deposit_of_zero_will_send_error() {
        plugin.eco.converter.give(sender, 50, Base.INGOTS)

        bankCommand.deposit(sender, "0")

        assertEquals(
            plugin.util.formatMessage(
                String.format(plugin.bundle.getString("error.zero")), prefix
            ), sender.nextMessage()
        )
    }

    @Test
    fun deposit_of_zero_will_not_add_to_account() {
        plugin.eco.converter.give(sender, 50, Base.INGOTS)

        bankCommand.deposit(sender, "0")

        assertEquals(0, plugin.eco.bank.getAccountBalance(sender.uniqueId))
    }

    @Test
    fun deposit_of_zero_will_not_remove_from_inventory() {
        plugin.eco.converter.give(sender, 50, Base.INGOTS)

        bankCommand.deposit(sender, "0")

        assertEquals(50, plugin.eco.converter.getInventoryValue(sender, Base.INGOTS))
    }

    @Test
    fun deposit_of_negative_amount_will_send_error() {
        plugin.eco.converter.give(sender, 50, Base.INGOTS)

        bankCommand.deposit(sender, "-50")

        assertEquals(
            plugin.util.formatMessage(
                String.format(plugin.bundle.getString("error.negative")), prefix
            ), sender.nextMessage()
        )
    }

    @Test
    fun deposit_of_negative_amount_will_not_change_account() {
        plugin.eco.converter.give(sender, 50, Base.INGOTS)
        plugin.eco.bank.setAccountBalance(sender.uniqueId, 20)

        bankCommand.deposit(sender, "-50")

        assertEquals(20, plugin.eco.bank.getAccountBalance(sender.uniqueId))
    }

    @Test
    fun deposit_of_negative_amount_will_not_remove_from_inventory() {
        plugin.eco.converter.give(sender, 50, Base.INGOTS)

        bankCommand.deposit(sender, "-50")

        assertEquals(50, plugin.eco.converter.getInventoryValue(sender, Base.INGOTS))
    }

    @Test
    fun withdraw_with_specific_sufficient_amount_will_notify_sender() {
        plugin.eco.bank.setAccountBalance(sender.uniqueId, 50)

        bankCommand.withdraw(sender, "25")

        assertEquals(
            plugin.util.formatMessage(
                String.format(plugin.bundle.getString("info.withdraw"), 25), prefix
            ), sender.nextMessage()
        )
    }

    @Test
    fun withdraw_with_specific_sufficient_amount_will_remove_from_account() {
        plugin.eco.bank.setAccountBalance(sender.uniqueId, 50)

        bankCommand.withdraw(sender, "25")

        assertEquals(25, plugin.eco.bank.getAccountBalance(sender.uniqueId))
    }

    @Test
    fun withdraw_with_specific_sufficient_amount_will_add_to_inventory() {
        plugin.eco.bank.setAccountBalance(sender.uniqueId, 50)

        bankCommand.withdraw(sender, "25")

        assertEquals(25, plugin.eco.converter.getInventoryValue(sender, Base.NUGGETS))
    }

    @Test
    fun withdraw_of_all_with_sufficient_amount_will_notify_sender() {
        plugin.eco.bank.setAccountBalance(sender.uniqueId, 50)

        bankCommand.withdraw(sender, "all")

        assertEquals(
            plugin.util.formatMessage(
                String.format(plugin.bundle.getString("info.withdraw"), 50), prefix
            ), sender.nextMessage()
        )
    }

    @Test
    fun withdraw_of_all_with_sufficient_amount_will_remove_from_account() {
        plugin.eco.bank.setAccountBalance(sender.uniqueId, 50)

        bankCommand.withdraw(sender, "all")

        assertEquals(0, plugin.eco.bank.getAccountBalance(sender.uniqueId))
    }

    @Test
    fun withdraw_of_all_with_sufficient_amount_will_add_to_inventory() {
        plugin.eco.bank.setAccountBalance(sender.uniqueId, 50)

        bankCommand.withdraw(sender, "all")

        assertEquals(50, plugin.eco.converter.getInventoryValue(sender, Base.NUGGETS))
    }

    @Test
    fun withdraw_of_no_amount_with_sufficient_amount_will_notify_sender() {
        plugin.eco.bank.setAccountBalance(sender.uniqueId, 50)

        bankCommand.withdraw(sender, null)

        assertEquals(
            plugin.util.formatMessage(
                String.format(plugin.bundle.getString("info.withdraw"), 50), prefix
            ), sender.nextMessage()
        )
    }

    @Test
    fun withdraw_of_no_amount_with_sufficient_amount_will_remove_from_account() {
        plugin.eco.bank.setAccountBalance(sender.uniqueId, 50)

        bankCommand.withdraw(sender, null)

        assertEquals(0, plugin.eco.bank.getAccountBalance(sender.uniqueId))
    }

    @Test
    fun withdraw_of_no_amount_with_sufficient_amount_will_add_to_inventory() {
        plugin.eco.bank.setAccountBalance(sender.uniqueId, 50)

        bankCommand.withdraw(sender, null)

        assertEquals(50, plugin.eco.converter.getInventoryValue(sender, Base.NUGGETS))
    }


    @Test
    fun withdraw_with_insufficient_amount_will_send_error() {
        plugin.eco.bank.setAccountBalance(sender.uniqueId, 50)

        bankCommand.withdraw(sender, "100")

        assertEquals(
            plugin.util.formatMessage(
                String.format(plugin.bundle.getString("error.notEnough")), prefix
            ), sender.nextMessage()
        )
    }

    @Test
    fun withdraw_with_insufficient_amount_will_not_change_account() {
        plugin.eco.bank.setAccountBalance(sender.uniqueId, 50)

        bankCommand.withdraw(sender, "100")

        assertEquals(50, plugin.eco.bank.getAccountBalance(sender.uniqueId))
    }

    @Test
    fun withdraw_with_insufficient_amount_will_not_add_to_inventory() {
        plugin.eco.bank.setAccountBalance(sender.uniqueId, 50)

        bankCommand.withdraw(sender, "100")

        assertEquals(0, plugin.eco.converter.getInventoryValue(sender, Base.NUGGETS))
    }

    @Test
    fun withdraw_of_zero_will_send_error() {
        plugin.eco.bank.setAccountBalance(sender.uniqueId, 50)

        bankCommand.withdraw(sender, "0")

        assertEquals(
            plugin.util.formatMessage(
                String.format(plugin.bundle.getString("error.zero")), prefix
            ), sender.nextMessage()
        )
    }

    @Test
    fun withdraw_of_zero_will_not__account() {
        plugin.eco.bank.setAccountBalance(sender.uniqueId, 50)

        bankCommand.withdraw(sender, "0")

        assertEquals(50, plugin.eco.bank.getAccountBalance(sender.uniqueId))
    }

    @Test
    fun withdraw_of_zero_will_not_add_to_inventory() {
        plugin.eco.bank.setAccountBalance(sender.uniqueId, 50)

        bankCommand.withdraw(sender, "0")

        assertEquals(0, plugin.eco.converter.getInventoryValue(sender, Base.INGOTS))
    }

    @Test
    fun withdraw_of_negative_amount_will_send_error() {
        plugin.eco.bank.setAccountBalance(sender.uniqueId, 50)

        bankCommand.withdraw(sender, "-50")

        assertEquals(
            plugin.util.formatMessage(
                String.format(plugin.bundle.getString("error.negative")), prefix
            ), sender.nextMessage()
        )
    }

    @Test
    fun withdraw_of_negative_amount_will_not_change_account() {
        plugin.eco.bank.setAccountBalance(sender.uniqueId, 20)

        bankCommand.withdraw(sender, "-50")

        assertEquals(20, plugin.eco.bank.getAccountBalance(sender.uniqueId))
    }

    @Test
    fun withdraw_of_negative_amount_will_not_add_to_inventory() {
        plugin.eco.bank.setAccountBalance(sender.uniqueId, 20)
        plugin.eco.converter.give(sender, 70, Base.INGOTS)

        bankCommand.withdraw(sender, "-50")

        assertEquals(70, plugin.eco.converter.getInventoryValue(sender, Base.INGOTS))
    }

    @Test
    fun set_with_valid_amount_and_player_and_permission_will_succeed() {
        plugin.eco.bank.playerAccounts[target.uniqueId.toString()] = 150

        bankCommand.set(sender, target, 100)

        assertEquals(100, plugin.eco.bank.getAccountBalance(target.uniqueId))
    }

    @Test
    fun set_with_valid_amount_and_player_and_permission_will_notify_sender() {
        bankCommand.set(sender, target, 100)

        assertEquals(
            plugin.util.formatMessage(
                String.format(plugin.bundle.getString("info.sender.moneyset"), target.name, 100), prefix
            ), sender.nextMessage()
        )
    }

    @Test
    fun set_with_valid_amount_and_player_and_permission_will_notify_target() {
        bankCommand.set(sender, target, 100)

        assertEquals(
            plugin.util.formatMessage(
                String.format(plugin.bundle.getString("info.target.moneySet"), 100)
            ), target.nextMessage().toString()
        )
    }

    @Test
    fun set_with_negative_amount_will_not_work() {
        plugin.eco.bank.playerAccounts[target.uniqueId.toString()] = 50

        bankCommand.set(null, target, -50)

        assertEquals(
            50, plugin.eco.bank.getAccountBalance(target.uniqueId)
        )
    }

    @Test
    fun add_works() {
        plugin.eco.bank.playerAccounts[target.uniqueId.toString()] = 50

        bankCommand.add(null, target, 50)

        assertEquals(
            100, plugin.eco.bank.getAccountBalance(target.uniqueId)
        )
    }

    @Test
    fun add_with_negative_amount_wont_work() {
        plugin.eco.bank.playerAccounts[target.uniqueId.toString()] = 75

        bankCommand.add(null, target, -25)

        assertEquals(
            75, plugin.eco.bank.getAccountBalance(target.uniqueId)
        )
    }

    @Test
    fun remove_works() {
        plugin.eco.bank.playerAccounts[target.uniqueId.toString()] = 75

        bankCommand.remove(null, target, 25)

        assertEquals(
            50, plugin.eco.bank.getAccountBalance(target.uniqueId)
        )
    }
}