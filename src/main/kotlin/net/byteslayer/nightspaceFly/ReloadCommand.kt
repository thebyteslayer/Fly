package net.byteslayer.nightspacefly

import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender

class ReloadCommand(private val plugin: NightspaceFly) : CommandExecutor {
    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<String>): Boolean {
        if (!sender.hasPermission("nightspace.fly.reload")) {
            sender.sendMessage(plugin.getMessage("no-permission"))
            return true
        }
        if (args.isEmpty() || args[0].lowercase() != "reload") {
            sender.sendMessage("Usage: /nightspacefly reload")
            return true
        }
        plugin.reloadConfig()
        sender.sendMessage(plugin.getMessage("reload-success"))
        return true
    }
}