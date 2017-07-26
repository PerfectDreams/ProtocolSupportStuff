```yaml
# ProtocolSupportStuff Configuration File
# A plugin that does... stuff, I guess. Disable Minecraft versions (even the current server!), remap blocks/items for older versions and much more!
# 
# ===[ ENABLING AND DISABLING VERSIONS ]===
# Just flip the switch to "false" and then ProtocolSupport will just flat out forget that version exists!
# 
# ===[ BLOCK/ITEM REMAPPING ]===
# You don't like concrete being bricks in pre-1.12? Then why not change it to something else!
# 
# How it works?
# blocks/items:
#     remapX: (Don't forget to change X to a different number! You must not have any duplicate remap keys!)
#        from: Concrete
#        to: Stained Clay
#        fromData: 0 (Optional, you can omit this)
#        toData: 0 (Optional too)
# Now you need to choose one of the three following options! They are self explanatory and you can only choose ONE, not three, not two, just ONE.
# Also, you should see what is a better fit for your use case
#        before: Minecraft 1.12 (Every version (but not including) before 1.12)
#        after: Minecraft 1.8 (Every version (but not including) after 1.8)
#        range: "Minecraft 1.8, Minecraft 1.9" (Every version between 1.8 and 1.9)
# 
# ===[ MISC STUFF ]===
# translateDisplayName: Automatically changes the item name for older version to the proper name
# addToLore: Adds a small text to the item lore explaining that this is an item from a newer version
# newerItemTextLore: Customize the text in the lore... if you want to, idk.
# swordBlocking: Allows pre-1.9 clients to sword block like the good old days, requires ProtocolLib and a auto shield block plugin (like OldCombatMechanics)
# configVersion: plz don't change this k thx bye
# 
# ===[ MORE MISC STUFF ]===
# thx to MrPowerGamerBR, Shevchik and 7kasper
# 
# GitHub: https://github.com/PocketDreams/ProtocolSupportStuff (report issues to me!)
versions:
  MINECRAFT_1_12: true
  MINECRAFT_1_11_1: true
  MINECRAFT_1_11: true
  MINECRAFT_1_10: true
  MINECRAFT_1_9_4: true
  MINECRAFT_1_9_2: true
  MINECRAFT_1_9_1: true
  MINECRAFT_1_9: true
  MINECRAFT_1_8: true
  MINECRAFT_1_7_10: true
  MINECRAFT_1_7_5: true
  MINECRAFT_1_6_4: true
  MINECRAFT_1_6_2: true
  MINECRAFT_1_6_1: true
  MINECRAFT_1_5_2: true
  MINECRAFT_1_5_1: true
  MINECRAFT_1_4_7: true
blocks:
  remap:
    from: Concrete
    to: Stained Clay
    before: Minecraft 1.12
items:
  remap:
    from: Totem
    to: Armor Stand
    between: Minecraft 1.8, Minecraft 1.10
  remap1:
    from: Concrete
    to: Stained Clay
    before: Minecraft 1.12
translateDisplayName: true
addToLore: true
newerItemTextLore: '&8This item is actually &7{name}&8 from newer versions of Minecraft'
swordBlocking: true
configVersion: 2
```