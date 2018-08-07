package net.perfectdreams.protocolsupportstuff

import co.aikar.commands.PaperCommandManager
import com.destroystokyo.paper.PaperConfig
import net.perfectdreams.protocolsupportstuff.commands.ProtocolSupportStuffCommand
import net.perfectdreams.protocolsupportstuff.hacks.StripSignColorCodesHack
import net.perfectdreams.protocolsupportstuff.hacks.SwordBlockingHack
import net.perfectdreams.protocolsupportstuff.listeners.ConnectionListener
import net.perfectdreams.protocolsupportstuff.listeners.ItemStackWriteListener
import net.perfectdreams.protocolsupportstuff.listeners.PlayerListener
import net.perfectdreams.protocolsupportstuff.utils.Constants
import net.perfectdreams.protocolsupportstuff.utils.extensions.enumify
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
import java.lang.NoClassDefFoundError

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
		paper = try { PaperConfig::class.java.declaredFields; true } catch (e: NoClassDefFoundError) { false }

		val manager = PaperCommandManager(this)
		manager.registerCommand(ProtocolSupportStuffCommand(this))

		updateConfig()
		applyConfigChanges()

		Bukkit.getPluginManager().registerEvents(ItemStackWriteListener(this), this)
		Bukkit.getPluginManager().registerEvents(ConnectionListener(this), this)
		Bukkit.getPluginManager().registerEvents(PlayerListener(this), this)
	}

	fun updateConfig() {
		config.options().header(Constants.CONFIG_HEADER)

		if (config.contains("configVersion")) { // obsolete
			// Wipe configuration
			config.getKeys(false).forEach { config.set(it, null) }
		}

		val configVersion = config.getInt("config-version", -1)
		if (configVersion == -1) { // Fill with default values
			config.set("add-to-lore", true)
			config.set("translate-display-name", true)
			config.set("newer-item-text-lore", "&8This item is actually &7{name}&8 from newer versions of Minecraft")
			config.set("blocks",
					listOf(
							mapOf(
									"from" to "Bone Block",
									"to" to "Quartz Block",
									"before" to "Minecraft 1.10"
							),
							mapOf(
									"from" to "Hay Block",
									"to" to "Yellow Wool",
									"before" to "Minecraft 1.6.1"
							)
					)
			)
			config.set("items",
					listOf(
							mapOf(
									"from" to "Totem of Undying",
									"to" to "Armor Stand",
									"between" to "Minecraft 1.8, Minecraft 1.10"
							)
					)
			)
			config.set("hacks.strip-colors-from-long-texts", false)
			config.set("hacks.sword-blocking", false)
		}

		config.set("config-version", 1)

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
		var range = arrayOf<ProtocolVersion>()

		if (value["before"] != null) {
			range = ProtocolVersion.getAllBeforeE(ProtocolVersion.valueOf((value["before"] as String).enumify()))
		}
		if (value["after"] != null) {
			range = ProtocolVersion.getAllAfterE(ProtocolVersion.valueOf((value["after"] as String).enumify()))
		}
		if (value["before-inclusive"] != null) {
			range = ProtocolVersion.getAllBeforeI(ProtocolVersion.valueOf((value["before-inclusive"] as String).enumify()))
		}
		if (value["after-inclusive"] != null) {
			range = ProtocolVersion.getAllAfterI(ProtocolVersion.valueOf((value["after-inclusive"] as String).enumify()))
		}
		if (value["between"] != null) {
			val rangeAsString = value["between"] as String
			val rangeAsSplit = rangeAsString.split(", ")

			val version1 = ProtocolVersion.valueOf(rangeAsSplit[0].enumify())
			val version2 = ProtocolVersion.valueOf(rangeAsSplit[1].enumify())
			range = ProtocolVersion.getAllBetween(version1, version2)
		}

		if (range.isEmpty()) {
			logger.warning { "Your config has a invalid remap (No valid remap version found)! Please fix it!" }
			return
		}

		if (range.contains(version)) {
			if (remapper is BlockRemapperControl) {
				if (value["from"] != null && value["to"] != null) {
					val from = Material.valueOf((value["from"] as String).enumify())
					val to = Material.valueOf((value["to"] as String).enumify())

					registerRemapEntryForAllStates(from, remapper) { it: BlockData ->
						to.createBlockData()
					}
				} else if (value["from-state"] != null && value["to-state"] != null) {
					val fromState = value["from-state"] as String
					val toState = value["to-state"] as String

					remapper.setRemap(Bukkit.createBlockData(fromState), Bukkit.createBlockData(toState))
				} else {
					logger.warning { "Your config has a invalid block remap (Missing \"from\"/\"to\" or \"from-state\"/\"to-state\")! Please fix it!" }
				}
			} else if (remapper is ItemRemapperControl) {
				if (value["from"] != null && value["to"] != null) {
					val from = Material.valueOf((value["from"] as String).enumify())
					val to = Material.valueOf((value["to"] as String).enumify())

					remapper.setRemap(from, to)
				} else {
					logger.warning { "Your config has a invalid item remap (Missing \"from\"/\"to\" or \"from-state\"/\"to-state\")! Please fix it!" }
				}
			}
		}
	}

	fun <T> registerRemapEntryForAllStates(from: Material, remapper: BlockRemapperControl, remapFunc: (T) -> BlockData) {
		MaterialAPI.getBlockDataList(from).forEach {
			remapper.setRemap(it, remapFunc.invoke(it as T))
		}
	}
}
