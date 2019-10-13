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


    fun openMenu(player: Player){
        val inv = Bukkit.createInventory(null , 27,pl.prefix+"§d§lMenu")

        inv.setItem(11,QIC(Material.CHEST,"§f§lOpen mCloud",0, mutableListOf("mCloudを開きます")))

        inv.setItem(13,QIC(Material.PAPER,"§f§lCreate mCloud",0,
                mutableListOf("新規でmCloudを登録します","§4§l一度登録すると、削除しない限り、変えることは出来ません!")))
        inv.setItem(15,QIC(Material.BARRIER,"§f§lDelete mCloud",0,
                mutableListOf("mCloudを削除します","§4§lクラウドタイプを変更する場合以外では仕様非推奨です！")))


        player.openInventory(inv)
    }


    ///////////////////////////
    //指定ページのクラウドデータを開く
    /////////////////////////////
    fun openCloud(player: Player,page:Int){
        val inv = Bukkit.createInventory(null,54,pl.prefix+"§b§lCloud")

        Bukkit.getScheduler().runTask(pl){
            val map = pl.db.loadItemData(player)

            if (map.isEmpty() || map[page]== null){
                Bukkit.getLogger().info("no cloud data")
                return@runTask
            }

            for (item in map[page]!!){

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

            if (page != map.size){
                inv.setItem(53,QIC(Material.PAPER,"§6§l次のページへ",0, mutableListOf("次のページに進みます")))
            }

            player.openInventory(inv)
        }
    }


    /////////////////////////
    //新規登録
    ////////////////////////
    fun createCloud(player: Player){

        if (pl.db.getMemberType(player) != "none"){
            player.closeInventory()
            player.sendMessage("${pl.prefix}§e§lあなたは既にmCloudの登録をしています！")
            return
        }

        val inv = Bukkit.createInventory(null,27,"${pl.prefix}§e§lクラウドタイプを選択")

        inv.setItem(11,QIC(Material.DIAMOND_HOE,"§a§lBeginner",48, mutableListOf("§e§l1ラージチェスト","エンダーチェストでは物足りない方におすすめです")))
        inv.setItem(13,QIC(Material.SILVER_SHULKER_BOX,"§5§lExpert",0, mutableListOf("§e§l5ラージチェスト","家の収納をmCloudで済ませたい方におすすめです")))
        inv.setItem(15,QIC(Material.CHEST,"§e§lPremium",0, mutableListOf("§e§l10ラージチェスト","膨大なアイテムを持っている方におすすめです")))
        player.openInventory(inv)
    }

    /////////////////////
    //削除確認
    ///////////////////////
    fun deleteCheck(player: Player){

        if (pl.db.getMemberType(player) == "none"){
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
                    createCloud(p)
                }
                15 ->{
                    deleteCheck(p)
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

        if(data == "§e§lクラウドタイプを選択"){
            e.isCancelled =true

            when(e.slot){


                11 ->{
                    p.sendMessage(pl.prefix+"§e§l新規データを作成中...")
                    Thread(Runnable {
                        pl.db.createNewData(p,"beginner")
                        p.sendMessage(pl.prefix+"§e§l作成完了！")
                    }).start()
                    p.closeInventory()
                }

                13->{
                    p.sendMessage(pl.prefix+"§e§l新規データを作成中...")
                    Thread(Runnable {
                        pl.db.createNewData(p,"expert")
                        p.sendMessage(pl.prefix+"§e§l作成完了！")
                    }).start()
                    p.closeInventory()
                }

                15->{
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

    @EventHandler
    fun closeEvent(e:InventoryCloseEvent){
        if (e.inventory.title == pl.prefix+"§b§lCloud"){
            pl.db.saveItemData(e.inventory,e.inventory.getItem(49).durability.toInt(),e.player as Player)
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