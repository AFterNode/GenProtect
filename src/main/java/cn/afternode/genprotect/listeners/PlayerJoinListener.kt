package cn.afternode.genprotect.listeners

import cn.afternode.genprotect.GenProtect
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerLoginEvent
import java.lang.NullPointerException

class PlayerJoinListener: Listener {
    val usePrefix: Boolean
    val namePrefix: List<String>

    val useSuffix: Boolean
    val nameSuffix: List<String>

    val useContains: Boolean
    val nameContains: List<String>

    val rejoinAuth: Boolean

    init {
        val config = GenProtect.INSTANCE.config

        val abSec = config.getConfigurationSection("anti-bot") ?: throw NullPointerException("Invalid configuration")
        usePrefix = abSec.getBoolean("use-prefix")
        namePrefix = abSec.getStringList("name-prefix")
        useSuffix = abSec.getBoolean("use-suffix")
        nameSuffix = abSec.getStringList("name-suffix")
        useContains = abSec.getBoolean("use-contains")
        nameContains = abSec.getStringList("name-contains")
        rejoinAuth = abSec.getBoolean("rejoin-auth")
    }

    @EventHandler
    fun onPlayerLogin(e: PlayerLoginEvent) {
        var accept = true
        var reason = ""

        if (usePrefix) {
            for (n in namePrefix) if (e.player.name.startsWith(n)) {
                accept = false
                reason = GenProtect.INSTANCE.messages.getString("unaccepted-kick.reasons.prefix") ?: "Illegal Name"
                break
            }
        }
        if (useSuffix) {
            for (n in nameSuffix) if (e.player.name.endsWith(n)) {
                accept = false
                reason = GenProtect.INSTANCE.messages.getString("unaccepted-kick.reasons.suffix") ?: "Illegal Name"
                break
            }
        }
        if (useContains) {
            for (n in nameContains) if (e.player.name.contains(n)) {
                accept = false
                reason = GenProtect.INSTANCE.messages.getString("unaccepted-kick.reasons.contains") ?: "Illegal Name"
                break
            }
        }

        if (!accept) {
            val sb = StringBuilder()
            for (s in GenProtect.INSTANCE.messages.getStringList("unaccepted-kick.messages")) {
                sb.append(s
                    .replace("%reason%", reason)
                    .replace("%name%", e.player.name)
                    .replace("%addr", e.player.address!!.address.hostAddress)).append("\n")
            }

            e.kickMessage = sb.toString()
            e.result = PlayerLoginEvent.Result.KICK_BANNED
        }
    }

    @EventHandler
    fun onPlayerJoin(e: PlayerJoinEvent) {
        try {
            if (rejoinAuth && !GenProtect.INSTANCE.data.isAccept(e.player.uniqueId, e.player.address!!)) {
                GenProtect.INSTANCE.data.addAccept(e.player.uniqueId, e.player.address!!)

                val sb = StringBuilder()
                for (s in GenProtect.INSTANCE.messages.getStringList("unaccepted-kick.messages")) {
                    sb.append(s
                        .replace("%reason%", GenProtect.INSTANCE.messages.getString("unaccepted-kick.reasons.rejoin") ?: "Please join again")
                        .replace("%name%", e.player.name)
                        .replace("%addr", e.player.address!!.address.hostAddress)).append("\n")
                }

                e.player.kickPlayer(sb.toString())
            }
        } catch (t: Throwable) {
            val sb = StringBuilder()
            for (s in GenProtect.INSTANCE.messages.getStringList("unaccepted-kick.messages")) {
                sb.append(s
                    .replace("%reason%", GenProtect.INSTANCE.messages.getString("unaccepted-kick.reasons.error") ?: "Please join again")
                    .replace("%name%", e.player.name)
                    .replace("%addr", e.player.address!!.address.hostAddress)).append("\n")
            }
            e.player.kickPlayer(sb.toString())
            t.printStackTrace()
        }
    }
}