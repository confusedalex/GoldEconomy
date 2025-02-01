package dev.confusedalex.thegoldeconomy

import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerQuitEvent

class Events(var bank: Bank) : Listener {
    @EventHandler
    fun onPlayerQuit(e: PlayerQuitEvent?) {
        writeToFiles(bank.playerAccounts, bank.fakeAccounts)
    }
}