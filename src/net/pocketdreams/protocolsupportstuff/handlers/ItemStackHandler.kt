package net.pocketdreams.protocolsupportstuff.handlers

import net.pocketdreams.protocolsupportstuff.ProtocolSupportStuff
import net.pocketdreams.protocolsupportstuff.getItemName
import net.pocketdreams.protocolsupportstuff.translateColorCodes
import org.bukkit.Bukkit
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import protocolsupport.api.TranslationAPI
import protocolsupport.api.events.ItemStackWriteEvent

class ItemStackHandler(val m: ProtocolSupportStuff) : Listener {
	init {
		Bukkit.getPluginManager().registerEvents(this, m)
	}

	@EventHandler
	fun onItemStackWrite(ev: ItemStackWriteEvent) {
		if (ev.original.type != ev.result.type) { // Only if it is actually remapped
			if (m.config.getBoolean("translateDisplayName")) {
				if (!ev.original.itemMeta.hasDisplayName()) { // We should only change the display name if it doesn't has one
					val itemMeta = ev.result.itemMeta
					itemMeta.displayName = "§f" + ev.original.getItemName(m, ev.locale)
					ev.result.itemMeta = itemMeta
				}
			}

			if (m.config.getBoolean("addToLore")) {
				val list = if (!ev.result.itemMeta.hasLore()) mutableListOf<String>() else ev.original.itemMeta.lore

				val itemMeta = ev.result.itemMeta

				var loreText = m.config.getString("newerItemTextLore").translateColorCodes()

				loreText = loreText.replace("{name}", ev.original.getItemName(m, ev.locale))

				list.add(loreText)
				itemMeta.lore = list
				ev.result.itemMeta = itemMeta
			}
		}
	}
}