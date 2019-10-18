package red.man10.man10itemcloud

import org.bukkit.Bukkit
import org.bukkit.command.Command
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.bukkit.plugin.java.JavaPlugin

class Man10ItemCloud : JavaPlugin() {

    val prefix = "§3§l[§f§lm§b§lCloud§3§l]"


    var vault : VaultManager? = null
    val db = CloudDataBase(this)
    val inv = CloudInventory(this)
    override fun onEnable() {
        // Plugin startup logic
        saveDefaultConfig()

        vault = VaultManager(this)

        server.pluginManager.registerEvents(inv,this)
    }

    override fun onDisable() {
        // Plugin shutdown logic
    }

    override fun onCommand(sender: CommandSender?, command: Command?, label: String?, args: Array<out String>?): Boolean {

        if (sender !is Player){
            return false
        }

        if (args!= null && args.isEmpty()){
            inv.openMenu(sender)
            return true
        }


        return false
    }
}
