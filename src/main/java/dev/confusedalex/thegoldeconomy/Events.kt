package dev.confusedalex.thegoldeconomy

import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerQuitEvent
import org.bukkit.plugin.Plugin

class Events(private val plugin: Plugin, private val bank: Bank) : Listener {
    @EventHandler
    fun onPlayerQuit(event: PlayerQuitEvent) {
        plugin.server.asyncScheduler.runNow(plugin) { writeToFiles(bank.playerAccounts) }
    }
}
