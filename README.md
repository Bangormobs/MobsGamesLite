The aim of this plugin is to fill in the few gaps left for Bukkit when we host event days.

- Allows for loading a world from a ZIP
- disposes of world after game.
  - saved player data is destroyed
  - changes to map are deleted
  - Some worlds can use command blocks to automatically close their own world (optional)
- With a correctly set up lobby world, player can choose teams before the world is loaded
- Allows some players to enter spectate mode and teleport between contestants. If the spectator is on a team they can only view their teammates
  - Spectators are creative, unable to interact at all, and invisible.
  - Creative mode is used so that command block games know that Spectators are not involved in the game.
- Transports players
  - Moves all players into a world on load and out of a world on unload
  - Automatic respawn - since a world cannot unload while a player is on the Death Screen, a temporary hack to automate respawning is in use. Once this is fixed the automatic respawn will be removed
  - Spectators left or right click to teleport between active players.

Features will be removed as a suitable replacement is implemented by Bukkit or Minecraft. I expect this plugin will eventually be merely a world loader/unloader before very long.

Features will be added only in cases where alternatives (shutting down a server, unzipping the world, restarting the server) cannot be automated in any sensible way with Vanilla or Bukkit Servers.
