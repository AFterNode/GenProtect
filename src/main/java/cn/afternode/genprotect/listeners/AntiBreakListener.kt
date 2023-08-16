package cn.afternode.genprotect.listeners

import cn.afternode.genprotect.GenProtect
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.BlockBreakEvent

class AntiBreakListener: Listener {
    private val worlds: List<String>

    init {
        worlds = GenProtect.INSTANCE.config.getStringList("anti-break.worlds")
    }

    @EventHandler
    fun onBlockBreak(e: BlockBreakEvent) {
        for (w in worlds) {
            if (w == e.block.world.name && !e.player.hasPermission("genprotect.allow-break")) {
                e.isCancelled = true
            }
        }
    }
}