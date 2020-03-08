package red.man10.man10itemcloud

import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent
import javax.xml.bind.Marshaller

class Event(private val pl:Man10ItemCloud) : Listener{

    @EventHandler
    fun login(e:PlayerJoinEvent){
        val p = e.player
        Thread(Runnable {
            pl.db.loadCloudData(p)
        }).start()
    }



}