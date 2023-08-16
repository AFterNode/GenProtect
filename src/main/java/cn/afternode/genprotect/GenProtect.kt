package cn.afternode.genprotect

import cn.afternode.genprotect.data.DataManager
import cn.afternode.genprotect.listeners.*
import org.bukkit.Bukkit
import org.bukkit.configuration.file.FileConfiguration
import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.plugin.java.JavaPlugin
import java.io.File
import java.lang.IllegalArgumentException

class GenProtect: JavaPlugin() {
    companion object {
        @JvmStatic lateinit var INSTANCE: GenProtect
            private set
    }

    init {
        INSTANCE = this
    }

    lateinit var messages: FileConfiguration
        private set

    lateinit var data: DataManager
        private set

    lateinit var playerJoinListener: PlayerJoinListener
        private set
    var antiSpamListener: AntiSpamListener? = null
        private set
    var antiBreakListener: AntiBreakListener? = null
        private set

    fun restart() {
        Bukkit.getPluginManager().disablePlugin(this)
        Bukkit.getPluginManager().enablePlugin(this)
    }

    override fun onLoad() {
        saveDefaultConfig()
        saveResource("messages.yml", false)
    }

    override fun onEnable() {
        messages = YamlConfiguration.loadConfiguration(File(dataFolder, "messages.yml"))

        data = DataManager()
        when (val it = (config.getString("database.type") ?: "h2").lowercase()) {
            "h2" -> data.connectH2()
            "mysql" -> data.connectMySql(config.getConfigurationSection("database")!!)
            else -> throw IllegalArgumentException("Unknown database type $it")
        }

        playerJoinListener = PlayerJoinListener()
        Bukkit.getPluginManager().registerEvents(playerJoinListener, this)

        if (config.getBoolean("anti-spam.enabled")) {
            antiSpamListener = AntiSpamListener()
            Bukkit.getPluginManager().registerEvents(antiSpamListener!!, this)
        }
        if (config.getBoolean("anti-break.enabled")) {
            antiBreakListener = AntiBreakListener()
            Bukkit.getPluginManager().registerEvents(antiBreakListener!!, this)
        }
    }

    override fun onDisable() {
        data.close()
    }

    override fun saveResource(resourcePath: String, replace: Boolean) {
        val f = File(dataFolder, resourcePath)
        if (replace) {
            super.saveResource(resourcePath, true)
        } else {
            if (!f.exists()) super.saveResource(resourcePath, false)
        }
    }
}