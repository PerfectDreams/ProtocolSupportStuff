package net.pocketdreams.protocolsupportstuff.protolib

import com.comphenix.protocol.PacketType
import com.comphenix.protocol.ProtocolLibrary
import com.comphenix.protocol.events.PacketAdapter
import com.comphenix.protocol.events.PacketEvent
import com.comphenix.protocol.wrappers.EnumWrappers
import net.pocketdreams.protocolsupportstuff.ProtocolSupportStuff
import protocolsupport.api.ProtocolSupportAPI
import protocolsupport.api.ProtocolVersion
import org.bukkit.entity.LivingEntity

class SwordBlockingPacketAdapter(m: ProtocolSupportStuff): PacketAdapter(m) {
	override fun onPacketReceiving(event: PacketEvent) {
		super.onPacketReceiving(event)

		if (!ProtocolSupportAPI.getProtocolVersion(event.player).isBefore(ProtocolVersion.MINECRAFT_1_9))
			return // Ignore this event if the player is not from a pre-1.9 version

		if (event.packetType!= PacketType.Play.Client.BLOCK_PLACE)
			return // Ignore this event if it isn't a block place packet

		// Everything good? Great! Then...
		event.isCancelled = true // ...we are going to cancel the event
		val shallowCopy = event.packet.shallowClone() // ...then shallow copy the original packet
		shallowCopy.hands.write(0, EnumWrappers.Hand.OFF_HAND) // ...change it to off hand (to make the server think "hey, this client is blocking with his shield!)

		val protocolManager = ProtocolLibrary.getProtocolManager()
		protocolManager.recieveClientPacket(event.player, event.packet, false); // ...now we are going to resend the original packet
		protocolManager.recieveClientPacket(event.player, shallowCopy, false); // ...and then send our modified "fake shield block" packet

		// And that's it!
	}

	override fun onPacketSending(event: PacketEvent) {
		super.onPacketSending(event)

		if (!ProtocolSupportAPI.getProtocolVersion(event.player).isBefore(ProtocolVersion.MINECRAFT_1_9))
			return // Ignore this event if the player is not from a pre-1.9 version

		if (event.packetType!= PacketType.Play.Server.ENTITY_METADATA)
			return // Ignore this event if it isn't an entity metadata packet

		if (event.packet.getEntityModifier(event.player.world).read(0) !is LivingEntity)
			return // Ignore this event if it isn't an entity, if we don't... exceptions will happen (see http://wiki.vg/Entities#Living)

		// phew, now we can do our dirty work ;)

		// Deep clone this packet, we need to do this or else explosions will happen!
		// ...okay, I don't actually remember what happens if we don't deep clone, but IIRC everything gets wonky
		val clonedPacket = event.packet.deepClone() // Deep clone the packet!

		clonedPacket.watchableCollectionModifier.read(0).forEach { wwo ->
			if (wwo.index == 6) { // Index 6 = Hand States
				// Value 3 = Off Hand, Value 1 = Main Hand
				// We are going to set it to 1 (main hand) if the value is 3 (off hand) so we can fake a sword blocking animation for older MC clients
				if (wwo.handle is Byte && (wwo.value as Byte).toInt() == 3) {
					wwo.value = 1.toByte()
				}
			}
		}

		event.packet = clonedPacket // Then change our original packet to the newly edited packet
	}
}