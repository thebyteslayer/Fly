package net.byteslayer.nightspacefly

import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

class FlySpeedCommand(private val plugin: NightspaceFly) : CommandExecutor {
    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<String>): Boolean {
        if (sender !is Player) {
            sender.sendMessage("This command can only be used by players.")
            return true
        }
        if (!sender.hasPermission("nightspace.fly.speed")) {
            sender.sendMessage(plugin.getMessage("no-permission"))
            return true
        }
        if (args.size != 1) {
            sender.sendMessage("Usage: /flyspeed <speed>")
            return true
        }
        val speedStr = args[0]
        val speed = speedStr.toFloatOrNull()
        val minSpeed = plugin.config.getDouble("flyspeed.min", 0.0)
        val maxSpeed = plugin.config.getDouble("flyspeed.max", 1.0)
        if (speed == null || speed < minSpeed || speed > maxSpeed) {
            sender.sendMessage("Invalid speed. Must be between $minSpeed and $maxSpeed")
            return true
        }

        if (!sender.hasPermission("nightspace.fly.speed.free") && !sender.isOp && plugin.isSpeedCostEnabled()) {
            val economy = plugin.getEconomy()
            if (economy == null) {
                sender.sendMessage(plugin.getMessage("economy-not-set-up"))
                return true
            }
            val cost = plugin.getCostForSpeedChange()
            val discount = plugin.getDiscount(sender)
            val actualCost = cost * (1 - discount / 100.0)
            if (!economy.has(sender, actualCost)) {
                sender.sendMessage(plugin.getMessage("not-enough-money").replace("{cost}", actualCost.toString()))
                return true
            }
            economy.withdrawPlayer(sender, actualCost)
            sender.sendMessage(plugin.getMessage("flyspeed-set-paid").replace("{speed}", speed.toString()).replace("{cost}", actualCost.toString()))
        } else {
            sender.sendMessage(plugin.getMessage("flyspeed-set").replace("{speed}", speed.toString()))
        }
        sender.flySpeed = speed
        return true
    }
}