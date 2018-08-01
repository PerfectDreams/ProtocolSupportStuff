package net.perfectdreams.protocolsupportstuff.utils

object Constants {
	const val CONFIG_HEADER = """(ﾉ◕ヮ◕)ﾉ*:･ﾟ✧ ProtocolSupportStuff ✧ﾟ･: *ヽ(◕ヮ◕ヽ)
         Enable / Disable / Configure
          ProtocolSupport features

= Enabling And Disabling Versions =
versions:
    MINECRAFT_1_13: true
    ...
    MINECRAFT_1_4_7: true

Toggling a version to "false" will disable that version on your server, this disabled the
version at network level, so it will just reject the version when trying to connect/ping,
like what happens when you try to connect to a outdated server version with your client.

Disabling at network level has its advantages, like disabling unused remaps, causing
ProtocolSupport to use less memory!

= Block/Item Remapping =
blocks/items:
- from: Block that will be remapped (use this or "from-state")
  to: New remap (use this or "to-state")
  from-state: Block state that will be remapped (use this or "from")
  to-state: New block state (use this or "to")
  before/after/before-inclusive/after-inclusive/range: Minecraft Version(s)

If you don't like ProtocolSupport's default remaps, you can change them here!

However keep in mind that changing remaps may cause issues when breaking blocks if the time
to break block is drastically different between the original block vs new block.

The from/to values uses the material enum names, check them here:
https://hub.spigotmc.org/javadocs/spigot/org/bukkit/Material.html

The from-state/to-state uses block states, here's an example:
minecraft:chest[facing=north,type=single,waterlogged=false]

You should use from-state/to-state if you need to remap specific states (example: specific rotations).

from-state/to-state only work with blocks, not items!

The version value uses the ProtocolVersion enum name, check them here:
https://github.com/ProtocolSupport/ProtocolSupport/blob/master/src/protocolsupport/api/ProtocolVersion.java

Here is how you can specify what versions the new remap should affect:
  before: Minecraft 1.12 (Every version (but not including) before 1.12)
  after: Minecraft 1.8 (Every version (but not including) after 1.8)
  before-including: Minecraft 1.12 (Every version (but including) before 1.12)
  after-including: Minecraft 1.8 (Every version (but including) after 1.8)
  range: "Minecraft 1.8, Minecraft 1.9" (Every version between 1.8 and 1.9)

= Hacks =
Hacks can be used to fix features for older versions or to improve player experience on your server while using
older versions.

They are named "hacks" because they are unstable!

sword-blocking               - Allows pre-1.9 clients to sword block and plays the "sword blocking" animation for
                               pre-1.9 clients when a 1.9+ client shield blocks.
                               Requires ProtocolLib and a auto shield block plugin (like OldCombatMechanics).
                               While not required, using Paper will decrease the shield block delay to 0 ticks,
                               having a better "old PvP" experience!
strip-colors-from-long-texts - If a sign has more than 16 characters (including color codes), the colors will be
                               stripped from the sign for pre-1.8 clients, this allows pre-1.8 clients to see the
                               entirety of the sign contents, instead of only being able to see until the text
                               cuts off. Example with a pre-1.8 client: "&a&lThis is too big!"
                               False: "&a&lThis is too "
                               True:  "This is too big!"

= Miscellaneous Stuff =
add-to-lore            - Appends a text to the item lore if the item is remapped by ProtocolSupport
newer-item-text-lore   - Text that will be appended to the item lore, if "add-to-lore" is enabled, {name} will be replaced by the item name.
translate-display-name - Changes the remapped item name to the "proper" item name

= Credits  =
Thanks to MrPowerGamerBR, Shevchik and 7kasper

GitHub:        https://github.com/PerfectDreams/ProtocolSupportStuff (report issues to me!)
PerfectDreams: https://perfectdreams.net/
My website:    https://mrpowergamerbr.com/
Loritta:       https://loritta.website/

!Do not change the config-version unless you know what you are doing!"""
}