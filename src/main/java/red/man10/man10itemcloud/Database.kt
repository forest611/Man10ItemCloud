package red.man10.man10itemcloud

import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack
import org.bukkit.util.io.BukkitObjectInputStream
import org.bukkit.util.io.BukkitObjectOutputStream
import org.yaml.snakeyaml.external.biz.base64Coder.Base64Coder
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.LinkedBlockingQueue

class Database(private val pl:Man10ItemCloud){

    val table_item_data = "CREATE TABLE if not exists `item_data`("+
	"`key` INT(11) unsigned NOT NULL AUTO_INCREMENT,"+
	"`player` VARCHAR(20) NULL DEFAULT NULL,"+
	"`uuid` VARCHAR(50) NULL DEFAULT NULL,"+
    "`base64` TEXT NULL DEFAULT NULL,"+
    "`page` INT NULL DEFAULT NULL,PRIMARY KEY (`key`);"

    val table_total_page = "CREATE TABLE if not exists `total_page`(\n"+
            "\t`player` VARCHAR(20) NULL DEFAULT NULL,\n"+
            "\t`uuid` VARCHAR(50) NULL DEFAULT NULL,\n"+
            "\t`page_total` INT NULL DEFAULT NULL,\n"+
            "\t`join_date` DATE NULL DEFAULT NULL);"

    val queue = LinkedBlockingQueue<String>()
    val cloudData = ConcurrentHashMap<Player,CloudData>()
    val mysql = MySQLManager(pl,"cloudload")

    /////////////////////////////
    //ログイン時にデータを読み込む
    /////////////////////////////
    @Synchronized
    fun loadCloudData(p: Player){

        var rs = mysql.query("SELECT * FROM total_page WHERE uuid='${p.uniqueId}';")

        if (rs == null){

            queue.add("INSERT INTO `total_page` (`player`, `uuid`, `page_total`, `join_date`)" +
                    " VALUES ('${p.name}', '${p.uniqueId}', '0', now());")

            createNewPage(p)
            return
        }

        rs.next()

        val data = CloudData()

        data.total_page = rs.getInt("page")
        rs.close()
        mysql.close()

        rs = mysql.query("SELECT * FROM `item_data` WHERE `uuid`='${p.uniqueId}';")!!

        while (rs.next()){
            data.inv[rs.getInt("page")] = Utility().itemStackArrayFromBase64(rs.getString("base64"))
        }

        cloudData[p] = data

    }

    fun createNewPage(p:Player){

        val data = cloudData[p]?: CloudData()

        val items = mutableListOf<ItemStack>()

        for (i in 0..44){
            items.add(ItemStack(Material.AIR))
        }

        queue.add("INSERT INTO `item_data` (`player`, `uuid`, `base64`, `page`) " +
                "VALUE('${p.name}', '${p.uniqueId}', " +
                "'${Utility().itemStackArrayToBase64(items.toTypedArray())}', '${data.total_page+1}');")

        data.total_page ++
        data.inv[data.total_page] = items

        cloudData[p] = data
    }

    //////////////////////////////
    //インベを閉じたとき、ページ切替時にデータを保存する
    //////////////////////////////
    fun saveCloudData(p:Player,inv:Inventory,page:Int){

        val items = mutableListOf<ItemStack>()

        if (page == -1){
            for (i in 0..7){
                if (inv.getItem(i) == null){
                    items.add(ItemStack(Material.AIR))
                    continue
                }
                items.add(inv.getItem(i))
            }
        }else{
            for (i in 0..44){
                if (inv.getItem(i) == null){
                    items.add(ItemStack(Material.AIR))
                    continue
                }
                items.add(inv.getItem(i))
            }
        }

        val data = cloudData[p]!!
        data.inv[page] = items
        cloudData[p] = data

        queue.add("UPDATE `item_data` `base64`='${Utility().itemStackArrayToBase64(items.toTypedArray())}'," +
                " WHERE=`page`=$page AND `uuid`='${p.uniqueId}';")
        Bukkit.getLogger().info("saved cloud data.")
    }

    ///////////////////////////////
    //データを保存
    ///////////////////////////////
    fun executeQueue(){
        Thread(Runnable {
            try {
                val sql = MySQLManager(pl,"cloudExecute")
                while (true){
                    val take = queue.take()
                    sql.execute(take)
                }

            }catch (e:InterruptedException){
            }
        }).start()
    }

    class CloudData{
        var inv = HashMap<Int,MutableList<ItemStack>>()//page, inv
        var total_page = -1 //cloudを作ってない人は -1page
    }
}