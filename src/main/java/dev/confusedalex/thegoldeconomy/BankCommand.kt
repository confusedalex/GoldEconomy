package dev.confusedalex.thegoldeconomy

import co.aikar.commands.BaseCommand
import co.aikar.commands.CommandHelp
import co.aikar.commands.annotation.*
import co.aikar.commands.annotation.Optional
import dev.confusedalex.thegoldeconomy.TheGoldEconomy.base
import org.bukkit.Bukkit
import org.bukkit.OfflinePlayer
import org.bukkit.command.CommandSender
import org.bukkit.configuration.file.FileConfiguration
import java.util.*

@Suppress("DEPRECATION")
@CommandAlias("bank")
class BankCommand(plugin: TheGoldEconomy) : BaseCommand() {
    private var bundle: ResourceBundle = plugin.eco.bundle
    private var eco: EconomyImplementer = plugin.eco
    var util: Util = plugin.util
    var config: FileConfiguration = plugin.config

    @HelpCommand
    fun help(help: CommandHelp) {
        help.showHelp()
    }

    @Subcommand("balance")
    @CommandAlias("balance")
    @Description("{@@command.info.balance}")
    @CommandPermission("thegoldeconomy.balance")
    fun balance(commandSender: CommandSender, @Optional player: OfflinePlayer?) {
        val senderOptional = util.isPlayer(commandSender)
        if (senderOptional.isEmpty) return

        val sender = senderOptional.get()
        val uuid = sender.uniqueId

        if (player == null) {
            sender.sendMessage(
                util.formatMessage(
                    String.format(
                        bundle.getString("info.balance"),
                        eco.getBalance(uuid.toString()).toInt(),
                        eco.bank.getAccountBalance(uuid),
                        eco.converter.getInventoryValue(sender, base)
                    )
                )
            )
        } else if (sender.hasPermission("thegoldeconomy.balance.others")) {
            sender.sendMessage(
                util.formatMessage(
                    String.format(
                        bundle.getString("info.balance.other"), player.name, eco.getBalance(player).toInt()
                    )
                )
            )
        } else {
            sender.sendMessage(
                util.formatMessage(
                    bundle.getString("error.noPermission")
                )
            )
        }
    }

    @Subcommand("pay")
    @Description("{@@command.info.pay}")
    @CommandPermission("thegoldeconomy.pay")
    fun pay(commandSender: CommandSender, target: OfflinePlayer, amount: Int) {
        val playerOptional = util.isPlayer(commandSender)
        if (playerOptional.isEmpty) return
        val sender = playerOptional.get()
        val senderuuid = sender.uniqueId
        val targetuuid = target.uniqueId

        when {
            util.isBankingRestrictedToPlot(sender) -> return
            amount == 0 -> {
                sender.sendMessage(util.formatMessage(bundle.getString("error.zero")))
                return
            }

            amount < 0 -> {
                sender.sendMessage(util.formatMessage(bundle.getString("error.negative")))
                return
            }

            amount > eco.bank.getTotalPlayerBalance(senderuuid) -> {
                sender.sendMessage(util.formatMessage(bundle.getString("error.notEnough")))
                return
            }

            senderuuid == targetuuid -> {
                sender.sendMessage(util.formatMessage(bundle.getString("error.payYourself")))
                return
            }

            util.isOfflinePlayer(target.name.toString()).isEmpty -> {
                sender.sendMessage(util.formatMessage(bundle.getString("error.noPlayer")))
                return
            }

            else -> {
                eco.withdrawPlayer(sender, amount.toDouble())
                sender.sendMessage(
                    util.formatMessage(
                        String.format(bundle.getString("info.sendMoneyTo"), amount, target.name)
                    )
                )
                if (target.isOnline) {
                    Bukkit.getPlayer(target.uniqueId)?.sendMessage(
                        util.formatMessage(
                            String.format(
                                bundle.getString("info.moneyReceived"), amount, sender.name
                            )
                        )
                    )
                    eco.bank.setAccountBalance(target.uniqueId, eco.bank.getTotalPlayerBalance(targetuuid) + amount)
                } else {
                    eco.depositPlayer(target, amount.toDouble())
                }
            }
        }
    }

    @Subcommand("deposit")
    @Description("{@@command.info.deposit}")
    @CommandPermission("thegoldeconomy.deposit")
    fun deposit(commandSender: CommandSender, @Optional nuggets: String?) {
        val playerOptional = util.isPlayer(commandSender)
        if (playerOptional.isEmpty) return

        val player = playerOptional.get()
        val inventoryValue = eco.converter.getInventoryValue(player, base)

        if (util.isBankingRestrictedToPlot(player)) {
            return
        }
        if (nuggets == null || nuggets == "all") {
            if (inventoryValue <= 0) {
                player.sendMessage(util.formatMessage(bundle.getString("error.zero")))
                return
            }
            player.sendMessage(util.formatMessage(String.format(bundle.getString("info.deposit"), inventoryValue)))
            eco.converter.deposit(player, inventoryValue, base)
            return
        }

        val amount: Int
        try {
            amount = nuggets.toInt()
        } catch (e: NumberFormatException) {
            commandHelp.showHelp()
            return
        }

        if (amount == 0) {
            player.sendMessage(util.formatMessage(bundle.getString("error.zero")))
        } else if (amount < 0) {
            player.sendMessage(util.formatMessage(bundle.getString("error.negative")))
        } else if (amount > inventoryValue) {
            player.sendMessage(util.formatMessage(bundle.getString("error.notEnough")))
        } else {
            player.sendMessage(util.formatMessage(String.format(bundle.getString("info.deposit"), amount)))
            eco.converter.deposit(player, nuggets.toInt(), base)
        }
    }

    @Subcommand("withdraw")
    @Description("{@@command.info.withdraw}")
    @CommandPermission("thegoldeconomy.withdraw")
    fun withdraw(commandSender: CommandSender, @Optional nuggets: String?) {
        val playerOptional = util.isPlayer(commandSender)
        if (playerOptional.isEmpty) return

        val player = playerOptional.get()

        if (util.isBankingRestrictedToPlot(player)) return
        if (nuggets == null || nuggets == "all") {
            val accountBalance = eco.bank.getAccountBalance(player.uniqueId)
            player.sendMessage(util.formatMessage(String.format(bundle.getString("info.withdraw"), accountBalance)))
            eco.converter.withdraw(player, eco.bank.getAccountBalance(player.uniqueId), base)
            return
        }

        val amount: Int
        try {
            amount = nuggets.toInt()
        } catch (e: NumberFormatException) {
            commandHelp.showHelp()
            return
        }

        if (amount == 0) {
            player.sendMessage(util.formatMessage(bundle.getString("error.zero")))
        } else if (amount < 0) {
            player.sendMessage(util.formatMessage(bundle.getString("error.negative")))
        } else if (amount > eco.bank.getAccountBalance(player.uniqueId)) {
            player.sendMessage(util.formatMessage(bundle.getString("error.notEnough")))
        } else {
            player.sendMessage(util.formatMessage(String.format(bundle.getString("info.withdraw"), amount)))
            eco.converter.withdraw(player, amount, base)
        }
    }


    @Subcommand("set")
    @CommandPermission("thegoldeconomy.set")
    @Description("{@@command.info.set}")
    fun set(commandSender: CommandSender?, target: OfflinePlayer, gold: Int) {
        if (gold < 0) {
            commandSender?.sendMessage(
                util.formatMessage(
                    String.format(bundle.getString("error.negative"), target.name, gold),
                )
            )
            return;
        }

        commandSender?.sendMessage(
            util.formatMessage(
                String.format(bundle.getString("info.sender.moneyset"), target.name, gold)
            )
        )

        eco.bank.setAccountBalance(target.uniqueId, gold)
        Bukkit.getPlayer(target.uniqueId)?.sendMessage(
            util.formatMessage(
                String.format(bundle.getString("info.target.moneySet"), gold),
            )
        )
    }

    @Subcommand("add")
    @CommandPermission("thegoldeconomy.add")
    @Description("{@@command.info.add}")
    fun add(commandSender: CommandSender?, target: OfflinePlayer, gold: Int) {
        commandSender?.sendMessage(
            util.formatMessage(
                String.format(bundle.getString("info.sender.addmoney"), gold, target.name)
            )
        )

        eco.depositPlayer(target, gold.toDouble())
        Bukkit.getPlayer(target.uniqueId)?.sendMessage(
            util.formatMessage(

                String.format(bundle.getString("info.target.addMoney"), gold)
            )
        )
    }

    @Subcommand("remove")
    @CommandPermission("thegoldeconomy.remove")
    @Description("{@@command.info.remove}")
    fun remove(commandSender: CommandSender?, target: OfflinePlayer, gold: Int) {
        commandSender?.sendMessage(
            util.formatMessage(
                String.format(bundle.getString("info.sender.remove"), gold, target.name)
            )
        )

        eco.withdrawPlayer(target, gold.toDouble())
        Bukkit.getPlayer(target.uniqueId)?.sendMessage(
            util.formatMessage(
                String.format(bundle.getString("info.target.remove"), gold)
            )
        )
    }
}