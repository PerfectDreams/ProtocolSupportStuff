package net.perfectdreams.protocolsupportstuff.listeners

import net.perfectdreams.protocolsupportstuff.ProtocolSupportStuff
import net.perfectdreams.protocolsupportstuff.utils.extensions.translateColorCodes
import org.bukkit.Material
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import protocolsupport.api.TranslationAPI
import protocolsupport.api.chat.components.TextComponent
import protocolsupport.api.events.ItemStackWriteEvent

class ItemStackWriteListener(val m: ProtocolSupportStuff) : Listener {
	@EventHandler
	fun onItemStackWrite(ev: ItemStackWriteEvent) {
		val remapper = m.itemRemappers[ev.version] ?: return

		if (remapper.getRemap(ev.original.type) == ev.original.type) // Only if it is actually remapped
			return

		val translationKey: String by lazy { TranslationAPI.getTranslationString(ev.locale, getTranslationKey(ev.original.type)) }

		if (m.config.getBoolean("translate-display-name"))
			ev.forcedDisplayName = TextComponent("§f$translationKey")

		if (m.config.getBoolean("add-to-lore")) {
			var loreText = m.config.getString("newer-item-text-lore").translateColorCodes()

			loreText = loreText.replace("{name}", translationKey)

			ev.additionalLore.add(loreText)
		}
	}

	fun getTranslationKey(material: Material): String {
		val namespacedKey = material.key
		val root = if (material.isBlock) "block" else "item"
		return root + "." + namespacedKey.namespace + "." + namespacedKey.key
	}
}