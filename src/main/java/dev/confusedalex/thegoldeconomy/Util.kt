package dev.confusedalex.thegoldeconomy

import com.palmergames.bukkit.towny.TownyAPI
import com.palmergames.bukkit.towny.`object`.TownBlockType
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

    fun formatMessage(message: String, prefix: String): String =
        ChatColor.GOLD.toString() + "[$prefix" + "] " + ChatColor.WHITE + ChatColor.translateAlternateColorCodes(
            '&', message
        )

    fun sendMessageToPlayer(message: String, player: Player?) =
        player?.sendMessage(formatMessage(message, plugin.config.getString("prefix") ?: "TheGoldEconomy"))

    fun isBankingRestrictedToPlot(player: Player): Boolean {
        if (plugin.config.getBoolean("restrictToBankPlot")) {
            val townBlock = TownyAPI.getInstance().getTownBlock(player.location)
            if (townBlock?.type != TownBlockType.BANK) {
                sendMessageToPlayer(plugin.bundle.getString("error.bankplot"), player)
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