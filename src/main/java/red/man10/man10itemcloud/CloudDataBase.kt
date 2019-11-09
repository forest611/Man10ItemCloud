package red.man10.man10itemcloud

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

class CloudDataBase(val pl:Man10ItemCloud){

    val total_pages = ConcurrentHashMap<Player,Int>()
    val player_item_data = ConcurrentHashMap<Player,HashMap<Int,MutableList<ItemStack>>>()

    fun getTotal(player: Player):Int{
        var m = total_pages[player]
        if(m == null){

            m = getTotalPage(player)
            total_pages[player] = m
        }
        return m
    }

    fun saveItemData(inv : Inventory,page:Int,player:Player){

        val items = mutableListOf<ItemStack>()
        if (getTotal(player) == 0){
            for (i in 0..7){
                if(inv.getItem(i) == null){
                    items.add(ItemStack(Material.AIR))
                    continue
                }
                items.add(inv.getItem(i))
            }

        }else{
            for (i in 0..44){
                if(inv.getItem(i) == null){
                    items.add(ItemStack(Material.AIR))
                    continue
                }
                items.add(inv.getItem(i))
            }
        }

        player_item_data[player]!![page] = items

        val mysql = MySQLManagerV2(pl,"mcloud_saving")

        mysql.execute("UPDATE `item_data` SET `base64`='${itemStackArrayToBase64(items.toTypedArray())}'" +
                " WHERE `page`=$page and uuid='${player.uniqueId}';")


    }


    /**
     * @return inventory data mutableList(ItemStack)
     */
    fun getItemData(player: Player, page : Int):MutableList<ItemStack>{

        if (player_item_data[player] == null){
            player_item_data[player] = HashMap()
        }

        var pageData = player_item_data[player]!![page]

        if (pageData == null){
            val mysql = MySQLManagerV2(pl,"mcloud_getting")

            val q = mysql.query("SELECT * FROM item_data WHERE uuid='${player.uniqueId}' and page='$page';")

            val rs = q.rs

            rs.next()

            pageData = itemStackArrayFromBase64(rs.getString("base64"))
            player_item_data[player]!![page] = pageData
            rs.close()
            q.close()

        }

        return pageData
    }

    ////////////////////////////
    //指定ページのデータを新規作成
    ////////////////////////////
    fun insertData(player: Player,page:Int){

        val items = mutableListOf<ItemStack>()

        for (i in 0..44){
            items.add(ItemStack(Material.AIR))
        }

        val mysql = MySQLManagerV2(pl,"mcloud_insert")

        mysql.execute("INSERT INTO `item_data` (`player`, `uuid`, `base64`, `page`)" +
                "SELECT * FROM (SELECT '${player.name}', '${player.uniqueId}', '${itemStackArrayToBase64(items.toTypedArray())}', '$page')" +
                " AS TMP WHERE NOT EXISTS (SELECT * FROM `item_data` WHERE uuid='${player.uniqueId}' and page='$page');")

    }

    fun createGuestData(player: Player){
        val items = mutableListOf<ItemStack>()

        for (i in 0..7){
            items.add(ItemStack(Material.AIR))
        }

        val mysql = MySQLManagerV2(pl,"mcloud_create")

        mysql.execute("INSERT INTO `item_data` (`player`, `uuid`, `base64`, `page`)" +
                " VALUES ('${player.name}', '${player.uniqueId}', '${itemStackArrayToBase64(items.toTypedArray())}', '1');")
        setTotalPage(player,0)
    }


    fun deleteData(player: Player){
        val mysql = MySQLManagerV2(pl,"mcloud")

        mysql.execute("DELETE FROM `item_data` WHERE `uuid`='${player.uniqueId}';")
        mysql.execute("DELETE FROM `total_page_list` WHERE `uuid`='${player.uniqueId}';")
        total_pages[player] = -1
    }


    /////////////////////////////
    //ページ数をチェック
    /////////////////////////////
    fun getTotalPage(player: Player):Int{
        val mysql = MySQLManagerV2(pl,"mcloud_getpage")

        val q = mysql.query("SELECT * FROM total_page_list WHERE uuid='${player.uniqueId}';")

        val rs = q.rs

        if (!rs.next()){
            rs.close()
            q.close()

            return -1
        }

        val page = rs.getInt("page_total")

        rs.close()
        q.close()
        return page
    }

    fun setTotalPage(player:Player,page:Int){
        val mysql = MySQLManagerV2(pl,"mcloud")

        if (getTotal(player) != -1){
            mysql.execute("UPDATE `total_page_list` SET page_total='$page',join_date=now() WHERE uuid='${player.uniqueId}';")
            total_pages[player] = page
            return
        }
        mysql.execute("INSERT INTO `total_page_list` (`player`, `uuid`, `page_total`, `join_date`)" +
                " VALUES ('${player.name}', '${player.uniqueId}', '$page', now());")
        total_pages[player] = page
    }



    // Below made by @takatronix

    @Throws(IllegalStateException::class)
    fun itemStackArrayToBase64(items: Array<ItemStack>): String {
        try {
            val outputStream = ByteArrayOutputStream()
            val dataOutput = BukkitObjectOutputStream(outputStream)

            // Write the size of the inventory
            dataOutput.writeInt(items.size)

            // Save every element in the list
            for (i in items.indices) {
                dataOutput.writeObject(items[i])
            }

            // Serialize that array
            dataOutput.close()
            return Base64Coder.encodeLines(outputStream.toByteArray())
        } catch (e: Exception) {
            throw IllegalStateException("Unable to save item stacks.", e)
        }
    }

    @Throws(IOException::class)
    fun itemStackArrayFromBase64(data: String): MutableList<ItemStack> {
        try {
            val inputStream = ByteArrayInputStream(Base64Coder.decodeLines(data))
            val dataInput = BukkitObjectInputStream(inputStream)
            val items = arrayOfNulls<ItemStack>(dataInput.readInt())

            // Read the serialized inventory
            for (i in items.indices) {
                items[i] = dataInput.readObject() as ItemStack
            }

            dataInput.close()
            return unwrapItemStackMutableList(items.toMutableList())
        } catch (e: ClassNotFoundException) {
            throw IOException("Unable to decode class type.", e)
        }

    }


    fun unwrapItemStackMutableList(list: MutableList<ItemStack?>): MutableList<ItemStack>{
        val unwrappedList = mutableListOf<ItemStack>()
        for (item in list) {
            if (item != null) {
                unwrappedList.add(item)
            }
        }
        return unwrappedList
    }
}