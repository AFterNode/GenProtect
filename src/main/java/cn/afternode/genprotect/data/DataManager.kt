package cn.afternode.genprotect.data

import cn.afternode.genprotect.GenProtect
import org.bukkit.configuration.ConfigurationSection
import org.bukkit.entity.Player
import java.io.File
import java.net.InetAddress
import java.net.InetSocketAddress
import java.sql.Connection
import java.sql.DriverManager
import java.util.UUID

class DataManager {
    private val dbFile = File(GenProtect.INSTANCE.dataFolder, "data.db")

    lateinit var connection: Connection
        private set

    fun connectH2() {
        Class.forName("org.h2.Driver")
        val setup = !dbFile.exists()
        connection = DriverManager.getConnection("jdbc:h2:./plugins/GenProtect/data.db")
        if (setup) setup()
    }

    fun connectMySql(cfg: ConfigurationSection) {
        connection = DriverManager.getConnection("jdbc:mysql://${cfg.getString("host")}:${cfg.getInt("port")}/${cfg.getString("name")}", cfg.getString("user"), cfg.getString("pass"))
        setup()
    }

    fun close() {
        if (connection.isClosed) connection.close()
    }

    fun setup() {
        val st = connection.createStatement()
        st.execute("CREATE TABLE IF NOT EXISTS accept_users(uuid VARCHAR(255), addr VARCHAR(255))")
        st.close()
    }

    fun addAccept(uuid: UUID, addr: InetSocketAddress) {
        if (!isAccept(uuid, addr)) {
            val st = connection.createStatement()
            st.execute("INSERT INTO accept_users VALUES('${uuid}', '${addr.address.hostAddress}')")
        }
    }

    fun isAccept(uuid: UUID, addr: InetSocketAddress): Boolean {
        val st = connection.createStatement()
        val qu = st.executeQuery("SELECT * FROM accept_users WHERE uuid='${uuid}'")
        while (qu.next()) {
            if (qu.getString("uuid") == uuid.toString()) {
                if (qu.getString("addr") == addr.address.hostAddress) {
                    qu.close()
                    st.close()
                    return true
                } else {
                    st.execute("DELETE FROM accept_users WHERE uuid='${uuid}'")
                    qu.close()
                    st.close()
                    return false
                }
            }
        }
        qu.close()
        st.close()
        return false
    }
}