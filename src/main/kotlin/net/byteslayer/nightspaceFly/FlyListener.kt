package net.byteslayer.nightspacefly

import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerChangedWorldEvent

class FlyListener(private val plugin: NightspaceFly) : Listener {
    @EventHandler
    fun onPlayerChangedWorld(event: PlayerChangedWorldEvent) {
        val player = event.player
        if (!plugin.isWorldAllowed(player.world.name) && player.allowFlight) {
            player.allowFlight = false
            player.isFlying = false
            player.sendMessage(plugin.getMessage("flight-disabled"))
            plugin.flyingPlayers.remove(player)
        }
    }
}