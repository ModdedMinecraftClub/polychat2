# polychat2

Polychat is a messaging protcol to exchange chat messages between Discord and multiple game servers. This repo contains the following:
* Core Network and Messaging libraries
* The Polychat server (which exchanges messages between Discord and all clients)
* A base client interface
* Minecraft Clients:
    * Spigot 1.4.7
    * Forge 1.12.2
    * Forge 1.16.4

## Quickstart
1. Download server and appropriate client jars from [releases page](https://github.com/ModdedMinecraftClub/polychat2/releases)
2. Create a new directory for your server and run the jar (`java -jar server.jar`)
3. Edit the yml file with the appropriate values for your Discord bot
4. Install your client mod and run the game
5. Change values in client config as appropriate

You should now have communication between your Polychat server and client.

## Commands
### General commands
* `!help`: Direct messages the user who executed the command with a list of all commands
* `!online`: Shows all currently online servers and players
### In-game commands
All in-game commands take the form `<command prefix><command name> <client prefix> <arguments>`. For simplicity, it will be assumed that your command prefix is the default of `!` and your client prefix is `MC`. Note that you may also use `<ALL>` in place of a client prefix to run the command on all clients.
* `!exec`: Executes in-game Minecraft command on the first client prefix specified. Example: `!exec MC give Steve diamond 10`
* `!restart`: Runs `/stop` on the client specified. (The name "restart" assumes the server auto-restarts upon being stopped).
* `!tps`: Executes `/forge tps` on client by default. Example: `!tps MC`

For in game commands, if you wish to override the default effect of a command you can use the `overrides` section of client config to do so. Example snippet of client config to replace the default `/forge tps` command with `/tps`¹:
```yml
overrides:
    - tps: "tps"
```

## Member bot integration
Polychat also has integration with [MMCC's MemberBot](https://github.com/ModdedMinecraftClub/Mmcc.MemberBot). Polychat can automatically accept promote and execute commands from MemberBot. You can also override this using `promote` in the overrides section. See MemberBot README for setup details.

----
¹: This feature may not seem very useful currently, but configurable custom commands are planned so this feature is mostly intended for that and the member bot promoting.
