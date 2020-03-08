package red.man10.man10itemcloud

import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemFlag
import org.bukkit.inventory.ItemStack
import org.bukkit.util.io.BukkitObjectInputStream
import org.bukkit.util.io.BukkitObjectOutputStream
import org.yaml.snakeyaml.external.biz.base64Coder.Base64Coder
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.IOException

class Utility {

    val prefix = "§3§l[§f§lm§b§lCloud§3§l]§r"

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

    fun send(p:Player,msg:String){
        p.sendMessage(prefix+msg)
    }

    ////////////////////////
    //簡易アイテム作成
    //////////////////////
    fun QIC(material : Material, title:String, lore:MutableList<String>):ItemStack{

        val item = ItemStack(material,1)
        val meta = item.itemMeta
        meta.displayName = title
        meta.lore = lore
        meta.isUnbreakable = true
        meta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE)
        meta.addItemFlags(ItemFlag.HIDE_DESTROYS)
        meta.addItemFlags(ItemFlag.HIDE_PLACED_ON)
        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES)
        item.itemMeta = meta
        return  item
    }
}