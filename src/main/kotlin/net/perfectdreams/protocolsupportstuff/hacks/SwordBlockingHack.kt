package net.perfectdreams.protocolsupportstuff.hacks

import com.comphenix.protocol.PacketType
import com.comphenix.protocol.events.PacketContainer
import com.comphenix.protocol.wrappers.EnumWrappers
import net.perfectdreams.protocolsupportstuff.ProtocolSupportStuff
import org.bukkit.Material
import org.bukkit.entity.LivingEntity
import protocolsupport.api.Connection
import protocolsupport.api.ProtocolType
import protocolsupport.api.ProtocolVersion

class SwordBlockingHack(val m: ProtocolSupportStuff, val connection: Connection) : Connection.PacketListener() {
	companion object {
		val REQUIREMENTS = object: Requirements() {
			override fun checkVersion(version: ProtocolVersion) = version.protocolType == ProtocolType.PC && version.isBefore(ProtocolVersion.MINECRAFT_1_9)
			override fun getConfigPath() = "hacks.sword-blocking"
		}
	}

	override fun onPacketReceiving(event: PacketEvent) {
		val packet = PacketContainer.fromPacket(event.packet)

		if (packet.type != PacketType.Play.Client.BLOCK_PLACE)
			return

		val hand = packet.hands.read(0)

		if (hand == EnumWrappers.Hand.OFF_HAND) // Ignore if it is a off hand packet
			return

		val itemInMainHandType = connection.player.inventory?.itemInMainHand?.type ?: return
		val holdingSword = itemInMainHandType == Material.WOODEN_SWORD ||
				itemInMainHandType == Material.STONE_SWORD ||
				itemInMainHandType == Material.GOLDEN_SWORD ||
				itemInMainHandType == Material.IRON_SWORD ||
				itemInMainHandType == Material.DIAMOND_SWORD

		if (!holdingSword)
			return

		val offHandCopy = packet.deepClone() // Copy the original packet
		offHandCopy.hands.write(0, EnumWrappers.Hand.OFF_HAND) // and change it to off hand (to make the server think "hey, this client is blocking with his shield!!")

		event.addPacketAfter(offHandCopy.handle)

		// When holding a sword for shield blocking, the client sends the main hand packet twice
		event.addPacketAfter(packet.deepClone())
	}

	override fun onPacketSending(event: PacketEvent) {
		val packet = PacketContainer.fromPacket(event.packet)

		if (packet.type != PacketType.Play.Server.ENTITY_METADATA)
			return

		val player = connection.player

		if (packet.getEntityModifier(player.world).read(0) !is LivingEntity)
			return // Ignore this event if it isn't an entity, if we don't... exceptions will happen (see http://wiki.vg/Entities#Living)

		// phew, now we can do our dirty work ;)

		// Deep clone this packet, we need to do this or else explosions will happen!
		// ...okay, I don't actually remember what happens if we don't deep clone, but IIRC everything gets wonky
		val clonedPacket = try { packet.deepClone() } catch (e: RuntimeException) { return; /* oof - https://github.com/dmulloy2/ProtocolLib/issues/498 */ } // Deep clone the packet!

		clonedPacket.watchableCollectionModifier.read(0).forEach { wwo ->
			if (wwo.index == 6) { // Index 6 = Hand States
				// Value 3 = Off Hand, Value 1 = Main Hand
				// We are going to set it to 1 (main hand) if the value is 3 (off hand) so we can fake a sword blocking animation for older MC clients
				val value = wwo.value
				if (value is Byte && value == 3.toByte()) {
					wwo.value = 1.toByte()
				}
			}
		}

		event.packet = clonedPacket.handle // Then change our original packet to the newly edited packet
	}
}