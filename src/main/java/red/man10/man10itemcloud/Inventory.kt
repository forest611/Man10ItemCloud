package red.man10.man10itemcloud

import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.entity.Player

class Inventory (private val pl:Man10ItemCloud){

    val util = Utility()

    ////////////////////////
    //クラウドのホーム画面を開く
    ////////////////////////
    fun openMenu(p:Player){

        val inv = Bukkit.createInventory(null , 27,util.prefix+"§d§lMenu")

        inv.setItem(11,util.QIC(Material.CHEST,"§f§lOpen mCloud", mutableListOf("mCloudを開きます")))

        inv.setItem(13,util.QIC(Material.PAPER,"§f§lUpgrade mCloud",
                mutableListOf("mCloudをアップグレードします")))


        p.openInventory(inv)

    }

    /////////////////////////
    //クラウドデータを表示
    /////////////////////////
    fun openCloud(p:Player,page:Int){
        val inv = Bukkit.createInventory(null,54,util.prefix+"§b§lCloud")

        val data = pl.db.cloudData[p]

        if (data == null){
            util.send(p, "§3§l現在データの読み込み中です。もう少しお待ち下さい。")
            return
        }

        ////デモモード
        if (data.total_page == 0){

            for (item in data.inv[0]!!){
                if (item.type == Material.AIR)continue
                inv.addItem(item)
            }

            inv.setItem(8,util.QIC(Material.PAPER,"§e§l有料版にアップグレードする",
                    mutableListOf("ここをクリックして有料版に","アップグレードしましょう！")))

            p.openInventory(inv)
            return
        }

        for (item in data.inv[page]?:return){
            if (item.type == Material.AIR)continue
            inv.addItem(item)
        }

        for (i in 45..53){
            inv.setItem(i,util.QIC(Material.GRAY_STAINED_GLASS_PANE,"", mutableListOf()))
        }

        //page表示
        inv.setItem(49,util.QIC(Material.GRAY_STAINED_GLASS_PANE,page.toString(), mutableListOf()))

        if (page != 1){
            inv.setItem(45,util.QIC(Material.PAPER,"§6§l前のページへ", mutableListOf("前のページへ戻ります")))
        }

        if (page < data.total_page){
            inv.setItem(53,util.QIC(Material.PAPER,"§6§l次のページへ", mutableListOf("次のページに進みます")))
        }

        p.openInventory(inv)

    }

}