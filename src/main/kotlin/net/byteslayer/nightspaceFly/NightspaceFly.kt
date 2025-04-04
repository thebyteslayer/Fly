package net.byteslayer.nightspacefly

import net.milkbowl.vault.economy.Economy
import org.bukkit.ChatColor
import org.bukkit.GameMode
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerQuitEvent
import org.bukkit.plugin.java.JavaPlugin

class NightspaceFly : JavaPlugin(), Listener {
    private var economy: Economy? = null
    val flyingPlayers = mutableSetOf<Player>()

    override fun onEnable() {
        saveDefaultConfig()
        if (setupEconomy()) {
            logger.info("Vault economy hooked.")
        } else {
            logger.warning("Vault not found. Economy features disabled.")
        }
        getCommand("fly")?.setExecutor(FlyCommand(this))
        getCommand("flyspeed")?.setExecutor(FlySpeedCommand(this))
        getCommand("nightspacefly")?.setExecutor(ReloadCommand(this))
        server.pluginManager.registerEvents(this, this)
        server.pluginManager.registerEvents(FlyListener(this), this)

        val chargeIntervalSeconds = config.getInt("economy.charge-interval-seconds", 60)
        val chargeIntervalTicks = chargeIntervalSeconds * 20L
        if (config.getBoolean("economy.enable-interval-cost", true)) {
            server.scheduler.runTaskTimer(this, Runnable {
                val costPerInterval = config.getDouble("economy.cost-per-interval", 5.0)
                for (player in flyingPlayers.toList()) {
                    if (player.isOnline && player.allowFlight && (player.gameMode == GameMode.SURVIVAL || player.gameMode == GameMode.ADVENTURE)) {
                        if (player.hasPermission("nightspace.fly.free") || player.isOp) {
                            // Free flight, no charge
                        } else {
                            val discount = getDiscount(player)
                            val actualCost = costPerInterval * (1 - discount / 100.0)
                            val economy = getEconomy()
                            if (economy != null && economy.has(player, actualCost)) {
                                economy.withdrawPlayer(player, actualCost)
                                player.sendMessage(getMessage("flight-interval-charged").replace("{cost}", actualCost.toString()))
                            } else {
                                player.allowFlight = false
                                player.isFlying = false
                                player.sendMessage(getMessage("flight-disabled-no-money"))
                                flyingPlayers.remove(player)
                            }
                        }
                    } else {
                        flyingPlayers.remove(player)
                    }
                }
            }, chargeIntervalTicks, chargeIntervalTicks)
        }
    }

    private fun setupEconomy(): Boolean {
        if (server.pluginManager.getPlugin("Vault") == null) return false
        val rsp = server.servicesManager.getRegistration(Economy::class.java) ?: return false
        economy = rsp.provider
        return economy != null
    }

    fun getEconomy(): Economy? = economy

    fun isWorldAllowed(worldName: String): Boolean = config.getStringList("allowed-worlds").contains(worldName)

    fun getCostToEnable(): Double = config.getDouble("economy.cost-per-activation", 10.0)

    fun getCostForSpeedChange(): Double = config.getDouble("economy.cost-per-speed-change", 2.0)

    fun isActivationCostEnabled(): Boolean = config.getBoolean("economy.enable-activation-cost", true)

    fun isSpeedCostEnabled(): Boolean = config.getBoolean("economy.enable-speed-cost", true)

    fun getDiscount(player: Player): Double {
        val discountClasses = config.getConfigurationSection("discount-classes")?.getKeys(false) ?: return 0.0
        var maxDiscount = 0.0
        for (className in discountClasses) {
            val permission = "nightspace.fly.discount.$className"
            if (player.hasPermission(permission)) {
                val discount = config.getDouble("discount-classes.$className.discount", 0.0)
                if (discount > maxDiscount) maxDiscount = discount
            }
        }
        return maxDiscount
    }

    fun getMessage(key: String): String {
        val rawMessage = config.getString("messages.$key", "") ?: ""
        val prefix = if (config.getBoolean("messages.use-prefix", true)) {
            config.getString("messages.prefix", "&7[&bNightspaceFly&7] ") ?: ""
        } else {
            ""
        }
        val fullMessage = prefix + rawMessage
        return ChatColor.translateAlternateColorCodes('&', fullMessage)
    }

    @EventHandler
    fun onPlayerQuit(event: PlayerQuitEvent) {
        flyingPlayers.remove(event.player)
    }
}