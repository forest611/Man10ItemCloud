package red.man10.man10itemcloud

import org.bukkit.command.Command
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.bukkit.plugin.java.JavaPlugin

class Man10ItemCloud : JavaPlugin() {

    lateinit var db : Database
    lateinit var inv : Inventory


    override fun onEnable() {

        db = Database(this)
        inv = Inventory(this)

        db.executeQueue()

        db.queue.add(db.table_item_data)
        db.queue.add(db.table_total_page)

        server.pluginManager.registerEvents(Event(this),this)


    }

    override fun onDisable() {

    }

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {

        if (sender !is Player)return false

        if (!sender.hasPermission("man10cloud.op"))return false



        return false
    }
}