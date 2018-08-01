package net.perfectdreams.protocolsupportstuff.utils.extensions

import net.md_5.bungee.api.chat.BaseComponent
import net.md_5.bungee.api.chat.TextComponent
import org.bukkit.ChatColor

fun String.translateColorCodes(ch: Char = '&'): String {
	return ChatColor.translateAlternateColorCodes(ch, this)
}

fun String.stripColorCode(ch: Char= '&'): String {
	return ChatColor.stripColor(translateColorCodes(ch))
}

fun String.toBaseComponent(): Array<out BaseComponent> {
	return TextComponent.fromLegacyText(this)
}

fun String.toTextComponent(): TextComponent {
	return TextComponent(*TextComponent.fromLegacyText(this))
}