package net.perfectdreams.protocolsupportstuff.listeners

import net.perfectdreams.protocolsupportstuff.ProtocolSupportStuff
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent

class PlayerListener(val m: ProtocolSupportStuff) : Listener {
	@EventHandler
	fun onJoin(e: PlayerJoinEvent) {
		if (m.paper && m.config.getBoolean("hacks.sword-blocking"))
			e.player.shieldBlockingDelay = 0
	}
}