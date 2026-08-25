package dev.confusedalex.thegoldeconomy

import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerQuitEvent

class Events(private val bank: Bank) : Listener {
    @EventHandler
    fun onPlayerQuit(e: PlayerQuitEvent?) {
        bank.saveAll()
    }
}
