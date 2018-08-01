package net.perfectdreams.protocolsupportstuff.listeners

import net.perfectdreams.protocolsupportstuff.ProtocolSupportStuff
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import protocolsupport.api.Connection
import protocolsupport.api.events.ConnectionHandshakeEvent

class ConnectionListener(val m: ProtocolSupportStuff) : Listener {
	@EventHandler
	fun onConnectionHandshake(e: ConnectionHandshakeEvent) {
		for ((hack, requirements) in m.hacks) {
			if (requirements.checkRequirements(m, e.connection))
				e.connection.addPacketListener(hack.getConstructor(ProtocolSupportStuff::class.java, Connection::class.java).newInstance(m, e.connection))
		}
	}
}