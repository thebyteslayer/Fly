package net.byteslayer.nightspacefly

import org.bukkit.GameMode
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

class FlyCommand(private val plugin: NightspaceFly) : CommandExecutor {
    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<String>): Boolean {
        if (args.isEmpty()) {
            if (sender !is Player) {
                sender.sendMessage("Usage: /fly <player>")
                return true
            }
            if (!sender.hasPermission("nightspace.fly.use")) {
                sender.sendMessage(plugin.getMessage("no-permission"))
                return true
            }
            toggleFlight(sender, sender)
        } else {
            if (!sender.hasPermission("nightspace.fly.others")) {
                sender.sendMessage(plugin.getMessage("no-permission"))
                return true
            }
            val targetName = args[0]
            val target = plugin.server.getPlayer(targetName)
            if (target == null) {
                sender.sendMessage("Player not found: $targetName")
                return true
            }
            toggleFlight(target, sender)
        }
        return true
    }

    private fun toggleFlight(player: Player, sender: CommandSender) {
        if (player.gameMode == GameMode.CREATIVE || player.gameMode == GameMode.SPECTATOR) {
            sender.sendMessage(plugin.getMessage("cannot-use-in-gamemode").replace("{player}", player.name))
            return
        }
        val worldName = player.world.name
        if (!plugin.isWorldAllowed(worldName)) {
            sender.sendMessage(plugin.getMessage("flight-not-allowed-in-world").replace("{player}", player.name))
            return
        }
        if (player.allowFlight) {
            player.allowFlight = false
            player.isFlying = false
            player.sendMessage(plugin.getMessage("flight-disabled"))
            if (sender != player) {
                sender.sendMessage(plugin.getMessage("flight-disabled-for").replace("{player}", player.name))
            }
            plugin.flyingPlayers.remove(player)
        } else {
            if (player.hasPermission("nightspace.fly.free") || player.isOp) {
                player.allowFlight = true
                player.sendMessage(plugin.getMessage("flight-enabled-free"))
                if (sender != player) {
                    sender.sendMessage(plugin.getMessage("flight-enabled-free-for").replace("{player}", player.name))
                }
                plugin.flyingPlayers.add(player)
            } else {
                val economy = plugin.getEconomy()
                if (economy == null && plugin.isActivationCostEnabled()) {
                    sender.sendMessage(plugin.getMessage("economy-not-set-up"))
                    return
                }
                val cost = if (plugin.isActivationCostEnabled()) plugin.getCostToEnable() else 0.0
                val discount = plugin.getDiscount(player)
                val actualCost = cost * (1 - discount / 100.0)
                if (actualCost > 0 && economy != null && !economy.has(player, actualCost)) {
                    sender.sendMessage(plugin.getMessage("not-enough-money").replace("{cost}", actualCost.toString()))
                    return
                }
                if (actualCost > 0 && economy != null) {
                    economy.withdrawPlayer(player, actualCost)
                }
                player.allowFlight = true
                val message = if (actualCost > 0) "flight-enabled-paid" else "flight-enabled-free"
                player.sendMessage(plugin.getMessage(message).replace("{cost}", actualCost.toString()))
                if (sender != player) {
                    val senderMessage = if (actualCost > 0) "flight-enabled-paid-for" else "flight-enabled-free-for"
                    sender.sendMessage(plugin.getMessage(senderMessage).replace("{player}", player.name).replace("{cost}", actualCost.toString()))
                }
                plugin.flyingPlayers.add(player)
            }
        }
    }
}