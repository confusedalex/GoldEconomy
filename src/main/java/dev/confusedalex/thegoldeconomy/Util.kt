package dev.confusedalex.thegoldeconomy

import com.palmergames.bukkit.towny.TownyAPI
import com.palmergames.bukkit.towny.`object`.TownBlockType
import net.milkbowl.vault.chat.Chat
import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.OfflinePlayer
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import java.util.*

class Util(private val plugin: TheGoldEconomy) {
    fun isOfflinePlayer(playerName: String): Optional<OfflinePlayer> {
        return Bukkit.getOfflinePlayers().firstOrNull { it.name == playerName }?.let { Optional.of(it) }
            ?: Optional.empty()
    }

    fun colorCurrency(gold: Number): String = "" + ChatColor.GOLD + gold + ChatColor.WHITE

    fun formatMessage(message: String): String =
        ChatColor.GOLD.toString() + "[" + (plugin.config.getString("prefix")
            ?: "TheGoldEconomy") + "]" + " " + ChatColor.WHITE + message

    fun isBankingRestrictedToPlot(player: Player): Boolean {
        if (plugin.config.getBoolean("restrictToBankPlot")) {
            val townBlock = TownyAPI.getInstance().getTownBlock(player.location)
            if (townBlock?.type != TownBlockType.BANK) {
                player.sendMessage(
                    formatMessage(
                        plugin.bundle.getString("error.bankplot")
                    )
                )
                return true
            }
        }
        return false
    }

    fun isPlayer(commandSender: CommandSender): Optional<Player> {
        return if (commandSender is Player) {
            Optional.of(commandSender)
        } else {
            commandSender.sendMessage(plugin.bundle.getString("error.notAPlayer"))
            Optional.empty()
        }
    }
}