package red.man10.man10itemcloud

import org.apache.commons.lang.mutable.Mutable
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

class CloudDataBase(val pl:Man10ItemCloud){


    fun saveItemData(inv : Inventory,page:Int,player:Player){

        val items = mutableListOf<ItemStack>()
        for (i in 0..44){
            if(inv.getItem(i) == null){
                items.add(ItemStack(Material.AIR))
                continue
            }
            items.add(inv.getItem(i))
        }

        val mysql = MySQLManagerV2(pl,"mcloud")

        mysql.execute("UPDATE `item_data` SET `base64`='${itemStackArrayToBase64(items.toTypedArray())}'" +
                " WHERE `page`=$page and uuid='${player.uniqueId}';")


        Bukkit.getLogger().info("player:${player.name},page:$page save cloud data.")
    }


    /**
     * @return inventory data <page(int),mutableList(ItemStack)
     */
    fun loadItemData(player: Player):ConcurrentHashMap<Int,MutableList<ItemStack>>{

        val mysql = MySQLManagerV2(pl,"mcloud")

        val q = mysql.query("SELECT * FROM item_data WHERE uuid='${player.uniqueId}';")

        val rs = q.rs

        val map = ConcurrentHashMap<Int,MutableList<ItemStack>>()

        while (rs.next()){
            map[rs.getInt("page")] = itemStackArrayFromBase64(rs.getString("base64"))
        }

        rs.close()
        q.close()

        return map
    }

    ////////////////////////////////
    //新規プレイヤー
    ////////////////////////////////
    fun createNewData(player:Player,member:String){

        when(member){
            "beginner" ->{
                insertData(player,1)
            }
            "expert" -> {
                for (i in 1..5){
                    insertData(player,i)
                }
            }
            "premium" ->{
                for (i in 1..10){
                    insertData(player,i)
                }
            }
        }

        setMemberType(player, member)

    }

    fun deleteData(player: Player){
        val mysql = MySQLManagerV2(pl,"mcloud")

        mysql.execute("DELETE FROM `item_data` WHERE  `uuid`='${player.uniqueId}';")
        mysql.execute("DELETE FROM `member_list` WHERE `uuid`='${player.uniqueId}';")
    }

    ////////////////////////////
    //指定ページのデータを新規作成
    ////////////////////////////
    fun insertData(player: Player,page:Int){

        val items = mutableListOf<ItemStack>()

        for (i in 0..44){
            items.add(ItemStack(Material.AIR))
        }

        val mysql = MySQLManagerV2(pl,"mcloud")

        mysql.execute("INSERT INTO `item_data` (`player`, `uuid`, `base64`, `page`)" +
                " VALUES ('${player.name}', '${player.uniqueId}', '${itemStackArrayToBase64(items.toTypedArray())}', '$page');")

    }

    /////////////////////////////
    //beginner
    //expert
    //premium
    /////////////////////////////
    fun getMemberType(player: Player):String{
        val mysql = MySQLManagerV2(pl,"mcloud")

        val q = mysql.query("SELECT * FROM member_list WHERE uuid='${player.uniqueId}';")

        val rs = q.rs

        if (!rs.next()){
            rs.close()
            q.close()

            return "none"
        }

        val type = rs.getString("type")

        rs.close()
        q.close()
        return type
    }

    fun setMemberType(player:Player,member: String){
        val mysql = MySQLManagerV2(pl,"mcloud")

        mysql.execute("INSERT INTO `member_list` (`player`, `uuid`, `type`, `join_date`)" +
                " VALUES ('${player.name}', '${player.uniqueId}', '$member', now());")
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