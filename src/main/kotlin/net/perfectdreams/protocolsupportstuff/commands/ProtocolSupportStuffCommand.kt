package net.perfectdreams.protocolsupportstuff.commands

import co.aikar.commands.BaseCommand
import co.aikar.commands.annotation.CommandAlias
import co.aikar.commands.annotation.CommandPermission
import co.aikar.commands.annotation.Default
import net.perfectdreams.protocolsupportstuff.ProtocolSupportStuff
import org.bukkit.command.CommandSender

@CommandAlias("protocolsupportstuff|psstuff")
@CommandPermission("protocolsupportstuff.reload")
class ProtocolSupportStuffCommand(val m: ProtocolSupportStuff) : BaseCommand() {
	@Default
	fun reload(sender: CommandSender) {
		m.applyConfigChanges()
		sender.sendMessage("§aReload complete!")
	}
}