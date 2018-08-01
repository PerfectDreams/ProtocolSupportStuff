package net.perfectdreams.protocolsupportstuff.hacks

import com.comphenix.protocol.PacketType
import com.comphenix.protocol.events.PacketContainer
import com.comphenix.protocol.wrappers.nbt.NbtCompound
import com.comphenix.protocol.wrappers.nbt.NbtFactory
import com.google.gson.JsonObject
import net.perfectdreams.protocolsupportstuff.ProtocolSupportStuff
import net.perfectdreams.protocolsupportstuff.utils.extensions.stripColorCode
import protocolsupport.api.Connection
import protocolsupport.api.ProtocolType
import protocolsupport.api.ProtocolVersion
import protocolsupport.api.chat.ChatAPI

class StripSignColorCodesHack(val m: ProtocolSupportStuff, val connection: Connection) : Connection.PacketListener() {
	companion object {
		val REQUIREMENTS = object: Requirements() {
			override fun checkVersion(version: ProtocolVersion) = version.protocolType == ProtocolType.PC && version.isBefore(ProtocolVersion.MINECRAFT_1_8)
			override fun getConfigPath() = "hacks.strip-colors-from-long-texts"
		}

		const val SIGN_TYPE = "minecraft:sign"
		const val UPDATE_TYPE_ID: Byte = 9
	}

	override fun onPacketSending(event: PacketEvent) {
		val packet = PacketContainer.fromPacket(event.packet)

		if (packet.type == PacketType.Play.Server.TILE_ENTITY_DATA) {
			val updateType = packet.bytes.read(0)

			if (updateType != UPDATE_TYPE_ID) // updateType 9 = Sign update
				return

			val compound = NbtFactory.asCompound(packet.nbtModifier.read(0).deepClone())
			stripIfNeeded(compound, 1)
			stripIfNeeded(compound, 2)
			stripIfNeeded(compound, 3)
			stripIfNeeded(compound, 4)
			packet.nbtModifier.write(0, compound)
			return
		}

		if (packet.type == PacketType.Play.Server.MAP_CHUNK) {
			val tagCompoundList = packet.listNbtModifier.read(0)

			for (nbtBase in tagCompoundList) {
				val compound = NbtFactory.asCompound(nbtBase)

				if (compound.getString("id") == SIGN_TYPE) {
					stripIfNeeded(compound, 1)
					stripIfNeeded(compound, 2)
					stripIfNeeded(compound, 3)
					stripIfNeeded(compound, 4)
				}
			}
		}
	}

	fun stripIfNeeded(compound: NbtCompound, index: Int) {
		val rawJson = compound.getString("Text$index")
		var line = ChatAPI.fromJSON(rawJson).toLegacyText()

		if (line.length > 16) { // okay, grande demais!
			line = line.stripColorCode()

			val jsonObject = JsonObject() // vamos criar um JSON Object com o nosso novo texto
			jsonObject.addProperty("text", line) // Colocar o nosso novo texto aqui dentro...

			compound.remove<String>("Text$index")
			compound.put("Text$index", jsonObject.toString()) // E substituir!
		}
	}
}