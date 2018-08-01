package net.perfectdreams.protocolsupportstuff.hacks

import net.perfectdreams.protocolsupportstuff.ProtocolSupportStuff
import protocolsupport.api.Connection
import protocolsupport.api.ProtocolVersion

abstract class Requirements {
	fun checkRequirements(m: ProtocolSupportStuff, connection: Connection): Boolean {
		return checkConfigEnabled(m) && checkVersion(connection.version)
	}

	abstract fun checkVersion(version: ProtocolVersion): Boolean

	fun checkConfigEnabled(m: ProtocolSupportStuff): Boolean {
		return m.config.getBoolean(getConfigPath())
	}

	abstract fun getConfigPath(): String
}