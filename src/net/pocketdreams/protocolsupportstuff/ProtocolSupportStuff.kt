package net.pocketdreams.protocolsupportstuff

import com.mrpowergamerbr.sparklycore.utils.commands.AbstractCommand
import net.pocketdreams.protocolsupportstuff.handlers.ItemStackHandler
import org.apache.commons.lang3.text.WordUtils
import org.bukkit.ChatColor
import org.bukkit.Material
import org.bukkit.command.Command
import org.bukkit.command.CommandSender
import org.bukkit.inventory.ItemStack
import org.bukkit.plugin.java.JavaPlugin
import protocolsupport.api.ProtocolSupportAPI
import protocolsupport.api.ProtocolVersion
import protocolsupport.api.remapper.BlockRemapperControl
import protocolsupport.api.remapper.ItemRemapperControl

class ProtocolSupportStuff : JavaPlugin() {
	override fun onEnable() {
		super.onEnable()

		// Dynamic config because why not?
		// (Actually for future proofing)
		val config = config
		if (config.getInt("configVersion", -1) == -1) {
			for (version in ProtocolVersion.getAllSupported()) {
				val configPath = "versions.${version.name}";
				if (!config.contains(configPath)) {
					config.set("versions.${version.name}", true) // Default: enabled
				}
			}

			// Now we set the default example remappers
			config.set("blocks.remap.from", "Concrete")
			config.set("blocks.remap.to", "Stained Clay")
			config.set("blocks.remap.before", "Minecraft 1.12")

			config.set("items.remap.from", "Totem")
			config.set("items.remap.to", "Armor Stand")
			config.set("items.remap.between", "Minecraft 1.8, Minecraft 1.10")

			config.set("translateDisplayName", false)
			config.set("addToLore", false)
			config.set("newerItemTextLore", "&8This item is actually &7{name}&8 from newer versions of Minecraft")
			config.set("configVersion", 1)

			config.options().header("ProtocolSupportStuff Configuration File\n" +
					"A plugin that does... stuff, I guess. Disable Minecraft versions (even the current server!), remap blocks/items for older versions and much more!\n\n" +
					"===[ ENABLING AND DISABLING VERSIONS ]===\n" +
					"Just flip the switch to \"false\" and then ProtocolSupport will just flat out forget that version exists!\n\n" +
					"===[ BLOCK/ITEM REMAPPING ]===\n" +
					"You don't like concrete being bricks in pre-1.12? Then why not change it to something else!\n\n" +
					"How it works?\n" +
					"blocks/items:\n" +
					"    remapX: (Don't forget to change X to a different number! You must not have any duplicate remap keys!)\n" +
					"       from: Concrete\n" +
					"       to: Stained Clay\n" +
					"       fromData: 0 (Optional, you can omit this)\n" +
					"       toData: 0 (Optional too)\n" +
			        "Now you need to choose one of the three following options! They are self explanatory and you can only choose ONE, not three, not two, just ONE.\n" +
					"Also, you should see what is a better fit for your use case\n" +
					"       before: Minecraft 1.12 (Every version (but not including) before 1.12)\n" +
					"       after: Minecraft 1.8 (Every version (but not including) after 1.8)\n" +
					"       range: \"Minecraft 1.8, Minecraft 1.9\" (Every version between 1.8 and 1.9)\n\n" +
			        "===[ MISC STUFF ]===\n" +
					"translateDisplayName: Automatically changes the item name for older version to the proper name\n" +
					"addToLore: Adds a small text to the item lore explaining that this is an item from a newer version\n" +
					"newerItemTextLore: Customize the text in the lore... if you want to, idk.\n" +
					"configVersion: plz don't change this k thx bye\n\n" +
					"===[ MORE MISC STUFF ]===\n" +
			        "thx to MrPowerGamerBR, Shevchik and 7kasper\n\n" +
					"GitHub: https://github.com/PocketDreams/ProtocolSupportStuff (report issues to me!)");
			saveConfig()
		}

		object: AbstractCommand("psstuff") {
			override fun onCommand(p0: CommandSender, p1: Command, p2: String, p3: Array<String>): Boolean {
				if (p0.hasPermission("protocolsupportstuff.reload")) {
					applyConfigChanges()
					p0.sendMessage("§aReload complete!")
				} else {
					p0.sendMessage("§cYou don't have permission!")
				}
				return true
			}
		}.register()

		applyConfigChanges()
		ItemStackHandler(this)
	}

	fun applyConfigChanges() {
		reloadConfig()
		val config = config
		for (version in ProtocolVersion.getAllSupported()) {
			val enabled = config.getBoolean("versions.${version.name}", true)
			
			if (enabled) ProtocolSupportAPI.enableProtocolVersion(version) else ProtocolSupportAPI.disableProtocolVersion(version)
		}


		ProtocolVersion.getAllSupported().forEach { version ->
			val blockRemapper = BlockRemapperControl(version)
			val itemRemapper = ItemRemapperControl(version)
			if (config.contains("blocks")) {
				for (value in config.getConfigurationSection("blocks").getKeys(false)) {
					readAndApply(version, value, blockRemapper)
				}
			}
			if (config.contains("items")) {
				for (value in config.getConfigurationSection("items").getKeys(false)) {
					readAndApply(version, value, itemRemapper)
				}
			}
		}
	}

	fun readAndApply(version: ProtocolVersion, value: String, remapper: Any) {
		val readFrom = if (remapper is BlockRemapperControl) "blocks" else "items"
		val from = Material.valueOf(config.getString("$readFrom.$value.from").enumify())
		val to = Material.valueOf(config.getString("$readFrom.$value.to").enumify())
		val fromData = config.getInt("$readFrom.$value.fromData", -1)
		val toData = config.getInt("$readFrom.$value.toData", -1)
		var range = arrayOf<ProtocolVersion>()

		if (config.contains("$readFrom.$value.before")) {
			range = ProtocolVersion.getAllBeforeE(ProtocolVersion.valueOf(config.getString("$readFrom.$value.before").enumify()))
		}
		if (config.contains("$readFrom.$value.after")) {
			range = ProtocolVersion.getAllAfterE(ProtocolVersion.valueOf(config.getString("$readFrom.$value.after").enumify()))
		}
		if (config.contains("$readFrom.$value.between")) {
			val rangeAsString = config.getString("$readFrom.$value.between")
			val rangeAsSplit = rangeAsString.split(", ")

			val version1 = ProtocolVersion.valueOf(rangeAsSplit[0].enumify())
			val version2 = ProtocolVersion.valueOf(rangeAsSplit[1].enumify())
			range = ProtocolVersion.getAllBetween(version1, version2)
		}

		if (range.contains(version)) {
			if (fromData != -1 && toData != -1) {
				if (remapper is BlockRemapperControl) {
					remapper.setRemap(from, fromData, to, toData)
				} else if (remapper is ItemRemapperControl) {
					remapper.setRemap(from, fromData, to, toData)
				}
			} else {
				if (remapper is BlockRemapperControl) {
					remapper.setRemap(from, to)
				} else if (remapper is ItemRemapperControl) {
					remapper.setRemap(from, to)
				}
			}
		}
	}
}

fun ItemStack.getItemName(m: ProtocolSupportStuff, locale: String): String {
	if (m.config.contains("translations.${this.type.name}.$locale")) { // If the locale is set, return that
		return m.config.getString("translations.${this.type.name}.$locale").translateColorCodes()
	}

	// If else, let's beautify the material enum
	return WordUtils.capitalizeFully(this.type.name.toLowerCase().replace("_", " "))
}

fun String.translateColorCodes(ch: Char = '&'): String {
	return ChatColor.translateAlternateColorCodes(ch, this)
}

fun String.enumify(): String {
	return this.replace(" ", "_").replace(".", "_").toUpperCase()
}