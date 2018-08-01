package net.perfectdreams.protocolsupportstuff

import co.aikar.commands.PaperCommandManager
import com.destroystokyo.paper.PaperConfig
import net.perfectdreams.protocolsupportstuff.commands.ProtocolSupportStuffCommand
import net.perfectdreams.protocolsupportstuff.hacks.StripSignColorCodesHack
import net.perfectdreams.protocolsupportstuff.hacks.SwordBlockingHack
import net.perfectdreams.protocolsupportstuff.listeners.ConnectionListener
import net.perfectdreams.protocolsupportstuff.listeners.ItemStackWriteListener
import net.perfectdreams.protocolsupportstuff.listeners.PlayerListener
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.block.data.BlockData
import org.bukkit.block.data.type.Chest
import org.bukkit.plugin.java.JavaPlugin
import protocolsupport.api.MaterialAPI
import protocolsupport.api.ProtocolSupportAPI
import protocolsupport.api.ProtocolVersion
import protocolsupport.api.remapper.BlockRemapperControl
import protocolsupport.api.remapper.ItemRemapperControl

class ProtocolSupportStuff : JavaPlugin() {
	val blockRemappers = mutableMapOf<ProtocolVersion, BlockRemapperControl>()
	val itemRemappers = mutableMapOf<ProtocolVersion, ItemRemapperControl>()
	val hacks = mutableListOf(
			Pair(
					StripSignColorCodesHack::class.java,
					StripSignColorCodesHack.REQUIREMENTS
			),
			Pair(
					SwordBlockingHack::class.java,
					SwordBlockingHack.REQUIREMENTS
			)
	)
	var paper = false

	override fun onEnable() {
		paper = try { PaperConfig::class.java.declaredFields; true } catch (e: Exception) { false }

		val manager = PaperCommandManager(this)
		manager.registerCommand(ProtocolSupportStuffCommand(this))

		updateConfig()
		applyConfigChanges()

		Bukkit.getPluginManager().registerEvents(ItemStackWriteListener(this), this)
		Bukkit.getPluginManager().registerEvents(ConnectionListener(this), this)
		Bukkit.getPluginManager().registerEvents(PlayerListener(this), this)
	}

	fun updateConfig() {
		// Add missing versions to the config
		for (version in ProtocolVersion.getAllSupported()) {
			val configPath = "versions.${version.name}"
			if (!config.contains(configPath))
				config.set(configPath, true)
		}

		saveConfig()
	}

	fun applyConfigChanges() {
		reloadConfig()

		// ===[ VERSION ENABLING/DISABLING ]===
		for (version in ProtocolVersion.getAllSupported()) {
			val enabled =  config.getBoolean("versions.${version.name}")
			if (enabled) ProtocolSupportAPI.enableProtocolVersion(version) else ProtocolSupportAPI.disableProtocolVersion(version)
		}

		// ===[ BLOCK/ITEM REMAPPING ]===
		BlockRemapperControl.resetToDefault()
		ItemRemapperControl.resetToDefault()

		blockRemappers.clear()
		itemRemappers.clear()

		ProtocolVersion.getAllSupported().forEach { version ->
			val blockRemapper = BlockRemapperControl(version)
			val itemRemapper = ItemRemapperControl(version)
			blockRemappers[version] = blockRemapper
			itemRemappers[version] = itemRemapper

			if (config.contains("blocks")) {
				for (value in config.getList("blocks")) {
					readAndApply(version, value as Map<String, Any>, blockRemapper)
				}
			}

			if (config.contains("items")) {
				for (value in config.getList("items")) {
					readAndApply(version, value as Map<String, Any>, itemRemapper)
				}
			}

			if (config.getBoolean("hacks.legacy-chest-hack") && version.isBefore(ProtocolVersion.MINECRAFT_1_13)) {
				// Legacy Chest Hack
				// Remove ProtocolSupport's default chest remap
				registerRemapEntryForAllStates(Material.CHEST, blockRemapper) { it: Chest ->
					val chest = Material.CHEST.createBlockData() as Chest
					chest.isWaterlogged = false
					chest.facing = it.facing
					chest
				}

				registerRemapEntryForAllStates(Material.TRAPPED_CHEST, blockRemapper) { it: Chest ->
					val chest = Material.TRAPPED_CHEST.createBlockData() as Chest
					chest.isWaterlogged = false
					chest.facing = it.facing
					chest
				}
			}
		}
	}

	fun readAndApply(version: ProtocolVersion, value: Map<String, Any>, remapper: Any) {
		val readFrom = if (remapper is BlockRemapperControl) "blocks" else "items"
		val from = Material.valueOf(value["from"] as String)
		val to = Material.valueOf(value["to"] as String)
		var range = arrayOf<ProtocolVersion>()

		if (value["before"] != null) {
			range = ProtocolVersion.getAllBeforeE(ProtocolVersion.valueOf(value["before"] as String))
		}
		if (value["after"] != null) {
			range = ProtocolVersion.getAllAfterE(ProtocolVersion.valueOf(value["after"] as String))
		}
		if (value["before-inclusive"] != null) {
			range = ProtocolVersion.getAllBeforeI(ProtocolVersion.valueOf(value["before-inclusive"] as String))
		}
		if (value["after-inclusive"] != null) {
			range = ProtocolVersion.getAllAfterI(ProtocolVersion.valueOf(value["after-inclusive"] as String))
		}
		if (value["between"] != null) {
			val rangeAsString = value["between"] as String
			val rangeAsSplit = rangeAsString.split(", ")

			val version1 = ProtocolVersion.valueOf(rangeAsSplit[0])
			val version2 = ProtocolVersion.valueOf(rangeAsSplit[1])
			range = ProtocolVersion.getAllBetween(version1, version2)
		}

		if (range.contains(version)) {
			if (remapper is BlockRemapperControl) {
				registerRemapEntryForAllStates(from, remapper) { it: BlockData -> it }
				remapper.setRemap(from.createBlockData(), to.createBlockData())
			} else if (remapper is ItemRemapperControl) {
				remapper.setRemap(from, to)
			}
		}
	}

	fun <T> registerRemapEntryForAllStates(from: Material, remapper: BlockRemapperControl, remapFunc: (T) -> BlockData) {
		MaterialAPI.getBlockDataList(from).forEach {
			remapper.setRemap(it, remapFunc.invoke(it as T))
		}
	}
}