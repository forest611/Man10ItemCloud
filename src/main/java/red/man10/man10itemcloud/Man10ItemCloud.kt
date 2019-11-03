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
        if (!sender.hasPermission("cloud.use")){ return false }

        if (args== null || args.isEmpty()){
            inv.openMenu(sender)
            return true
        }

        //クラウドを開く
        if (args[0] == "open"){
            inv.openCloud(sender,sender,1)
            return true
        }

        if (!sender.hasPermission("cloud.op"))return false

        if (args[0] == "create" && args.size == 3){
            db.createNewData(Bukkit.getPlayer(args[1]),args[2])
            sender.sendMessage("$prefix§a§l作成完了")
            return true
        }

        if (args[0] == "delete" && args.size == 2){
            db.deleteData(Bukkit.getPlayer(args[1]))
            sender.sendMessage("$prefix§a§l削除完了")
            return true
        }

        if (args[0] == "open" && args.size == 2){
            inv.openCloud(Bukkit.getPlayer(args[1]),sender,1)
            return true
        }

        if (args[0] == "help"){
            sender.sendMessage("§b§lMan10ItemCloud HELP")
            sender.sendMessage("§b§l=====================")
            sender.sendMessage("§a§l/mcloud クラウドのメニューを開きます")
            sender.sendMessage("§a§l/mcloud open クラウドを開きます")
            sender.sendMessage("§a§l/mcloud create [player] [plan] 指定プレイヤーのクラウドを作成します" +
                    "plan: beginner,expert,premium ")
            sender.sendMessage("§a§l/mcloud delete [player] 指定プレイヤーのクラウドを削除します")
            sender.sendMessage("§a§l/mcloud open [player] 指定プレイヤーのクラウドを開きます")
            sender.sendMessage("§b§l=====================")

            return true
        }


        return false
    }
}
