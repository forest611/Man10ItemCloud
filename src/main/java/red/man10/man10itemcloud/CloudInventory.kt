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

class CloudInventory(val pl:Man10ItemCloud):Listener{


    val small = 1
    val normal = 5
    val big = 10



    //////////////////////
    //最初のメニュー
    //////////////////////
    fun openMenu(player: Player){
        val inv = Bukkit.createInventory(null , 27,pl.prefix+"§d§lMenu")

        inv.setItem(11,QIC(Material.CHEST,"§f§lOpen mCloud",0, mutableListOf("mCloudを開きます","現在のプランは${pl.db.getMember(player)}です")))

        inv.setItem(13,QIC(Material.PAPER,"§f§lUpgrade mCloud",0,
                mutableListOf("mCloudをアップグレードします")))
//        inv.setItem(15,QIC(Material.BARRIER,"§f§lDelete mCloud",0,
//                mutableListOf("mCloudを削除します","§4§lクラウドプランを変更する場合","§4§l以外では仕様非推奨です！")))


        player.openInventory(inv)
    }


    ///////////////////////////
    //指定ページのクラウドデータを開く
    /////////////////////////////
    fun openCloud(player: Player,page:Int){
        val inv = Bukkit.createInventory(null,54,pl.prefix+"§b§lCloud")

        Bukkit.getScheduler().runTask(pl){

            val member = pl.db.getMember(player)

            if (member == "none"){
                player.sendMessage(pl.prefix+"§e§l現在新規データを作成しています")
                pl.db.createGuestData(player)
                player.sendMessage(pl.prefix+"§e§l作成完了！")
                return@runTask
            }

            val map = pl.db.loadItemData(player,page)

            if (member == "guest"){
                val invGuest = Bukkit.createInventory(null,9,pl.prefix+"§b§lCloud§e§l(DEMO)")

                for (item in map){
                    if (item.type == Material.AIR)continue

                    invGuest.addItem(item)
                }
                invGuest.setItem(8,QIC(Material.PAPER,"§e§l有料版にアップグレードする",0,
                        mutableListOf("ここをクリックして有料版に","アップグレードしましょう！")))
                player.openInventory(invGuest)
                return@runTask
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
            when(member){
                "beginner" ->{
                    if (page != small){
                        inv.setItem(53,QIC(Material.PAPER,"§6§l次のページへ",0, mutableListOf("次のページに進みます")))
                    }

                }
                "expert" ->{
                    if (page != normal){
                        inv.setItem(53,QIC(Material.PAPER,"§6§l次のページへ",0, mutableListOf("次のページに進みます")))
                    }

                }
                "premium" ->{
                    if (page != big){
                        inv.setItem(53,QIC(Material.PAPER,"§6§l次のページへ",0, mutableListOf("次のページに進みます")))
                    }
                }
            }

            player.sendMessage(pl.prefix+"§e§lアイテムを持ったままページを切り替え内容に注意してください！" +
                    "アイテムを落としてしまいます！")
            player.openInventory(inv)
        }
    }


    /////////////////////////
    //アップグレード
    ////////////////////////
    fun upgradeCloud(player: Player){

        val type = pl.db.getMember(player)

        val inv = Bukkit.createInventory(null,27,"${pl.prefix}§e§lクラウドタイプを選択")

        when(type){

            "none" ->{
                inv.setItem(11,QIC(Material.DIAMOND_HOE,"§a§lBeginner",48,
                        mutableListOf("§e§l${small}ラージチェスト","エンダーチェストでは物足りない方におすすめです","§e§l￥500,000,000")))
                inv.setItem(13,QIC(Material.SILVER_SHULKER_BOX,"§5§lExpert",0,
                        mutableListOf("§e§l${normal}ラージチェスト","家の収納をmCloudで済ませたい方におすすめです","§e§l￥2,500,000,000")))
                inv.setItem(15,QIC(Material.CHEST,"§e§lPremium",0,
                        mutableListOf("§e§l${big}ラージチェスト","膨大なアイテムを持っている方におすすめです","§e§l￥5,000,000,000")))

            }
            "guest" ->{
                inv.setItem(11,QIC(Material.DIAMOND_HOE,"§a§lBeginner",48,
                        mutableListOf("§e§l${small}ラージチェスト","エンダーチェストでは物足りない方におすすめです","§e§l￥500,000,000")))
                inv.setItem(13,QIC(Material.SILVER_SHULKER_BOX,"§5§lExpert",0,
                        mutableListOf("§e§l${normal}ラージチェスト","家の収納をmCloudで済ませたい方におすすめです","§e§l￥2,500,000,000")))
                inv.setItem(15,QIC(Material.CHEST,"§e§lPremium",0,
                        mutableListOf("§e§l${big}ラージチェスト","膨大なアイテムを持っている方におすすめです","§e§l￥5,000,000,000")))

            }
            "beginner" ->{
                inv.setItem(13,QIC(Material.SILVER_SHULKER_BOX,"§5§lExpert",0,
                        mutableListOf("§e§l${normal}ラージチェスト","家の収納をmCloudで済ませたい方におすすめです","§e§l￥2,000,000,000")))
                inv.setItem(15,QIC(Material.CHEST,"§e§lPremium",0,
                        mutableListOf("§e§l${big}ラージチェスト","膨大なアイテムを持っている方におすすめです","§e§l￥4,500,000,000")))

            }
            "expert" ->{
                inv.setItem(15,QIC(Material.CHEST,"§e§lPremium",0,
                        mutableListOf("§e§l${big}ラージチェスト","膨大なアイテムを持っている方におすすめです","§e§l￥2,500,000,000")))

            }
            "premium" ->{
                player.sendMessage("§e§lあなたは既に最高ランクのプランをご利用です！")
                player.closeInventory()
                return
            }
        }

        player.openInventory(inv)
    }

    /////////////////////
    //削除確認
    ///////////////////////
    fun deleteCheck(player: Player){

        if (pl.db.getMember(player) == "none"){
            player.closeInventory()
            player.sendMessage("${pl.prefix}§e§lあなたはmCloudの登録をしていません！")
            return
        }


        val inv = Bukkit.createInventory(null,9,"${pl.prefix}§4§lmCloud登録削除")

        for (i in 0..3){
            inv.setItem(i,QIC(Material.STAINED_GLASS_PANE,"§a§l削除する",5, mutableListOf()))
        }
        for (i in 5..8){
            inv.setItem(i,QIC(Material.STAINED_GLASS_PANE,"§4§l削除しない",14, mutableListOf()))
        }
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
                15 ->{
                   // deleteCheck(p)
                }
            }

        }

        if (data == "§b§lCloud"){

            if (e.slot >=45)e.isCancelled =true
            when(e.slot){
                45 ->{
                    if (e.currentItem.itemMeta.displayName == "§6§l前のページへ"){

                        val page = e.inventory.getItem(49).durability.toInt()

                        Thread(Runnable {pl.db.saveItemData(e.inventory,page,p)}).start()

                        openCloud(p,page-1)
                    }
                }
                53 ->{
                    if (e.currentItem.itemMeta.displayName == "§6§l次のページへ"){

                        val page =e.inventory.getItem(49).durability.toInt()

                        Thread(Runnable {pl.db.saveItemData(e.inventory,page,p)}).start()

                        openCloud(p,page+1)
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

        if(data == "§e§lクラウドタイプを選択"){
            e.isCancelled =true

            val type = pl.db.getMember(p)

            when(e.slot){

                //buy beginner
                11 ->{

                    if (type != "guest" && type != "none"){
                        return
                    }

                    //500,000,000


                    if (!withdraw(p,500000000.0)){
                        return
                    }
                    Bukkit.getLogger().info("withdraw mCloud")

                    p.sendMessage(pl.prefix+"§e§l新規データを作成中...")
                    Thread(Runnable {
                        pl.db.createNewData(p,"beginner")
                        p.sendMessage(pl.prefix+"§e§l作成完了！")
                    }).start()
                    p.closeInventory()
                }

                //buy expert
                13->{
                    if (type == "expert"){
                        return
                    }

                    //2,500,000,000
                    when(type){
                        "none" ->{
                            if (!withdraw(p,2500000000.0)){
                                return
                            }
                        }
                        "guest" ->{
                            if (!withdraw(p,2500000000.0)){
                                return
                            }
                        }
                        "beginner" ->{
                            if (!withdraw(p,2000000000.0)){
                                return
                            }
                        }
                    }


                    p.sendMessage(pl.prefix+"§e§l新規データを作成中...")
                    Thread(Runnable {
                        pl.db.createNewData(p,"expert")
                        p.sendMessage(pl.prefix+"§e§l作成完了！")
                    }).start()
                    p.closeInventory()
                }

                //buy premium
                15->{

                    //5,000,000,000

                    when(type){
                        "none" ->{
                            if (!withdraw(p,5000000000.0)){
                                return
                            }
                        }
                        "guest" ->{
                            if (!withdraw(p,5000000000.0)){
                                return
                            }
                        }
                        "beginner" ->{
                            if (!withdraw(p,4500000000.0)){
                                return
                            }
                        }
                        "expert" ->{
                            if (!withdraw(p,2500000000.0)){
                                return
                            }
                        }
                    }
                    Bukkit.getLogger().info("withdraw mCloud")

                    p.sendMessage(pl.prefix+"§e§l新規データを作成中...")
                    Thread(Runnable {
                        pl.db.createNewData(p,"premium")
                        p.sendMessage(pl.prefix+"§e§l作成完了！")
                    }).start()
                    p.closeInventory()
                }
            }

        }

        if (data == "§4§lmCloud登録削除"){
            e.isCancelled = true

            if (e.slot <=3){
                p.closeInventory()
                p.sendMessage(pl.prefix+"§e§l削除中...")
                Thread(Runnable {
                    pl.db.deleteData(p)
                    p.sendMessage(pl.prefix+"§e§l削除完了！")
                }).start()
            }
            if (e.slot >=5){
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