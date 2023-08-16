package cn.afternode.genprotect.listeners

import cn.afternode.genprotect.GenProtect
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.AsyncPlayerChatEvent
import org.bukkit.scheduler.BukkitRunnable

class AntiSpamListener: Listener {
    private val threshold: Int
    private val records = HashMap<Player, String>()
    private val vl = HashMap<Player, Int>()
    private val max: Int
    private val operations: List<String>

    init {
        threshold = GenProtect.INSTANCE.config.getInt("anti-spam.threshold", 10)
        max = GenProtect.INSTANCE.config.getInt("anti-spam.vl", 5)
        operations = GenProtect.INSTANCE.config.getStringList("anti-spam.operations")
    }

    @EventHandler
    fun onChat(e: AsyncPlayerChatEvent) {
        if (!records.containsKey(e.player)) {
            records[e.player] = e.message
            return
        }

        if (records[e.player]!!.lowercase() == e.message.lowercase()) {
            doOperation(e.player)
            return
        }

        val rec = records[e.player]!!.lowercase().toCharArray()
        val cur = e.message.lowercase().toCharArray()
        records[e.player] = e.message
        var t = 0
        val ind = if (rec.size < cur.size) rec.size else cur.size
        for (i in 0 until ind) {
            if (rec[i] == cur[i]) t ++
            if (t >= threshold) break
        }
        if (t >= threshold) {
            val vl = (this.vl[e.player] ?: 0) + 1
            this.vl[e.player] = vl
            if (vl >= max) {
                doOperation(e.player)
                this.vl[e.player] = 0
            }
            return
        }
    }

    private fun doOperation(p: Player) {
        object: BukkitRunnable() {
            override fun run() {
                for (o in operations) {
                    val cmd = o.replace("%player%", p.name)
                    Bukkit.dispatchCommand(Bukkit.getConsoleSender(), cmd)
                }
            }
        }.runTask(GenProtect.INSTANCE)
    }
}