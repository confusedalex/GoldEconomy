package dev.confusedalex.thegoldeconomy

import org.bukkit.Material
import org.bukkit.entity.EntityType
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityDeathEvent

class RemoveGoldDrops : Listener {
    private fun shouldRemove(material: Material) = material.name.startsWith("GOLD")

    @EventHandler
    fun entityDeathEvent(e: EntityDeathEvent) {
        if (e.entityType != EntityType.PLAYER) {
            e.drops.removeIf { shouldRemove(it.type) }
        }
    }
}
