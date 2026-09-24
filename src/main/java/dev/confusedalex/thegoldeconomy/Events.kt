package dev.confusedalex.thegoldeconomy

import org.bukkit.event.EventHandler
import org.bukkit.event.Listener

class Events(var bank: Bank) : Listener {
    @EventHandler
    fun onPlayerQuit() {
        writeToFiles(bank.playerAccounts)
    }
}