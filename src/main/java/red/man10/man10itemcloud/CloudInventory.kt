package red.man10.man10itemcloud

import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryCloseEvent
import org.bukkit.inventory.ItemFlag
import org.bukkit.inventory.ItemStack
import kotlin.math.pow

class CloudInventory(val pl:Man10ItemCloud):Listener{


    //////////////////////
    //最初のメニュー
    //////////////////////
    fun openMenu(player: Player){
        val inv = Bukkit.createInventory(null , 27,pl.prefix+"§d§lMenu")

        inv.setItem(11,QIC(Material.CHEST,"§f§lOpen mCloud",0, mutableListOf("mCloudを開きます","現在のプランは${pl.db.getTotal(player)}です")))

        inv.setItem(13,QIC(Material.PAPER,"§f§lUpgrade mCloud",0,
                mutableListOf("mCloudをアップグレードします")))


        player.openInventory(inv)
    }


    ///////////////////////////
    //指定ページのクラウドデータを開く
    /////////////////////////////
    fun openCloud(p:Player, page:Int){
        val inv = Bukkit.createInventory(null,54,pl.prefix+"§b§lCloud")

        val total = pl.db.getTotal(p)

        if (total == -1){
            p.sendMessage(pl.prefix+"§e§l現在新規データを作成しています")
            pl.db.createGuestData(p)
            p.sendMessage(pl.prefix+"§e§l作成完了！")
            p.sendMessage(pl.prefix+"§e§lもう一度開いてください！")
            return
        }

        val map = pl.db.loadItemData(p,page)

        if (total == 0){
            val invGuest = Bukkit.createInventory(null,9,pl.prefix+"§b§lCloud§e§l(DEMO)")

            for (item in map){
                if (item.type == Material.AIR)continue

                invGuest.addItem(item)
            }
            invGuest.setItem(8,QIC(Material.PAPER,"§e§l有料版にアップグレードする",0,
                    mutableListOf("ここをクリックして有料版に","アップグレードしましょう！")))
            p.openInventory(invGuest)
            return
        }

        for (item in map){

            if (item.type == Material.AIR)continue

            inv.addItem(item)
        }

        for (i in 45..53){
            inv.setItem(i,QIC(Material.STAINED_GLASS_PANE,"",15, mutableListOf()))
        }

        inv.setItem(49,QIC(Material.STAINED_GLASS_PANE,"",page, mutableListOf()))

        if (page != 1){
            inv.setItem(45,QIC(Material.PAPER,"§6§l前のページへ",0, mutableListOf("前のページへ戻ります")))
        }

        ///////
        //next page
        if (page < total){
            inv.setItem(53,QIC(Material.PAPER,"§6§l次のページへ",0, mutableListOf("次のページに進みます")))
        }

        p.sendMessage(pl.prefix+"§e§lアイテムを持ったままページを切り替え内容に注意してください！" +
                "アイテムを落としてしまいます！")
        p.openInventory(inv)
    }



    /////////////////////////
    //アップグレード
    ////////////////////////
    fun upgradeCloud(player: Player){

        val page = pl.db.getTotal(player)

        if (page >= 16){
            player.sendMessage("${pl.prefix}§a§lこれ以上ページを追加できません")
            return
        }

        val inv = Bukkit.createInventory(null,9,"${pl.prefix}§e§lクラウドをアップグレード")

        val amount =(2.0.pow(page.toDouble()))*50000000

        inv.setItem(4,QIC(Material.CHEST,"§a§lアップグレード",0, mutableListOf("§e§lインベントリを1ページ増やします","§e§l$amount$")))

        player.openInventory(inv)
    }


    @EventHandler
    fun clickEvent(e:InventoryClickEvent){
        val p = e.whoClicked as Player
        if (e.inventory.title.indexOf(pl.prefix) == -1)return

        val data = e.inventory.title.replace(pl.prefix,"")

        if (data ==  "§d§lMenu"){
            e.isCancelled = true

            when(e.slot){
                11 ->{
                    openCloud(p,1)
                }
                13 ->{
                    upgradeCloud(p)
                }
            }

        }

        if (data == "§b§lCloud"){

            if (e.slot >=45)e.isCancelled =true
            when(e.slot){
                45 ->{
                    if (e.currentItem.itemMeta.displayName == "§6§l前のページへ"){

                        val page = e.inventory.getItem(49).durability.toInt()

                        Bukkit.getScheduler().runTask(pl){
                            pl.db.saveItemData(e.inventory,page,p)
                            openCloud(p,page-1)
                        }
                    }
                }
                53 ->{
                    if (e.currentItem.itemMeta.displayName == "§6§l次のページへ"){

                        val page =e.inventory.getItem(49).durability.toInt()

                        Bukkit.getScheduler().runTask(pl){
                            pl.db.saveItemData(e.inventory,page,p)
                            openCloud(p,page+1)
                        }
                    }
                }
            }

        }

        if (data == "§b§lCloud§e§l(DEMO)"){
            if (e.slot == 8){
                e.isCancelled = true
                upgradeCloud(p)
            }
        }

        if(data == "§e§lクラウドをアップグレード"){
            e.isCancelled =true

            val total = pl.db.getTotal(p)

            if (e.slot == 4){
                if (!withdraw(p,((2.0.pow(total.toDouble()))*50000000))){
                    return
                }
                p.sendMessage(pl.prefix+"§e§l新規データを作成中...")
                Thread(Runnable {
                    pl.db.insertData(p,total+1)
                    p.sendMessage(pl.prefix+"§e§l作成完了！")
                }).start()
                p.closeInventory()
            }

        }

    }

    fun withdraw(player: Player,money:Double):Boolean{
        if (pl.vault!!.getBalance(player.uniqueId)<money){
            player.sendMessage(pl.prefix+"§e§l所持金が足りません！")
            return false
        }

        pl.vault!!.withdraw(player.uniqueId,money)
        Bukkit.getLogger().info("withdraw mCloud")
        return true
    }

    @EventHandler
    fun closeEvent(e:InventoryCloseEvent){
        if (e.inventory.title == pl.prefix+"§b§lCloud"){
            pl.db.saveItemData(e.inventory,e.inventory.getItem(49).durability.toInt(),e.player as Player)
            return
        }
        if (e.inventory.title == pl.prefix+"§b§lCloud§e§l(DEMO)"){
            pl.db.saveItemData(e.inventory,1,e.player as Player)
        }
    }

    ////////////////////////
    //簡易アイテム作成
    //////////////////////
    fun QIC(material : Material,title:String,damage:Int,lore:MutableList<String>):ItemStack{

        val item = ItemStack(material,1,damage.toShort())
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