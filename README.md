# polychat2

Polychat is a messaging protocol to exchange chat messages and console commands between Discord and multiple game servers. Polychat is made up of two main components: the Polychat server and the Polychat client installed on a game server. The server handles messages from Discord and exchanges messages between clients. In the context of Polychat, "server" refers to the Polychat server, and "client" refers to either the Polychat client installed on a game server, or the game server itself (not a game client).

This repo contains the following:
* Core network and messaging libraries
* A base client interface
* Minecraft Clients:
    * Spigot 1.4.7
    * Forge 1.7.10
    * Forge 1.12.2
    * Forge 1.16.4
    * Fabric 1.17.1
    * Forge 1.18.2
* The legacy Java server (see below)

## Server
Polychat requires a server which exchanges messages between Discord and each client. The following servers are available:
* [Mmcc.Bot](https://github.com/ModdedMinecraftClub/Mmcc.Bot)
* [polychat.js](https://github.com/flaszuu/polychat.js) (unofficial)
* [Legacy Java server](/server/) (unsupported)

<details>
<summary>Legacy server details</summary>

## Notes
* Run the server using: `java -jar server-2.x.x.jar`
* You must run the server once in order to generate the template configuration file

## Commands
### General commands
* `!help`: Direct messages the user who executed the command with a list of all commands
* `!online`: Shows all currently online servers and players
### In-game commands
All in-game commands take the form `<command prefix><command name> <client prefix> <arguments>`. For example, . Note that you may also use `<ALL>` in place of a client prefix to run the command on all clients.
* `!exec`: Executes in-game Minecraft command on the first client prefix specified. Example: `!exec MC give Steve diamond 10`
* `!restart`: Runs `/stop` on the client specified. (The name "restart" assumes the server auto-restarts upon being stopped).
* `!tps`: Executes `/forge tps` on client by default. Example: `!tps MC`

For in game commands, if you wish to override the default effect of a command you can use the `overrides` section of client config to do so. Example snippet of client config to replace the default `/forge tps` command with `/tps`:
```yml
overrides:
    - tps: "tps"
```

</details>

## Quickstart
1. Choose server and download appropriate client jars from [releases page](https://github.com/ModdedMinecraftClub/polychat2/releases)
2. Configure and run your server
3. Install your client mod and run the game server
4. Change values in client config as appropriate

You should now have communication between your Polychat server and client.


## Compiling from source
<details><summary>See more</summary>

### Compiling

To compile from source, you must first compile the ``client-base``, ``message-libary``,
``common``, and ``network-library`` subprojects.

NOTE: These require Java 11 to compile. However, some modern Minecraft clients will need newer versions of java.

This can be done by going to their respective folders and running ``./gradlew build``.

Then, copy the jars to `/client/<version>/libs`, and run ``./gradlew build`` in the folder of the client you want to compile.
</details>

## Contributing
If you're interested in writing a Polychat client, feel free to create a pull request on this repo. Non-Minecraft clients are encouraged, however the server will not expanded to support extra capabilities exclusive to other games. If you implement a Polychat server, send us the link and we will add it to this README. If you want to implement your own client or server, here are some resources:

* The [Mmcc.Bot](https://github.com/ModdedMinecraftClub/Mmcc.Bot/tree/main/src/Mmcc.Bot.Polychat) server implementation
* Polychat's networking protocol, SSMP
    * [C# implementation](https://github.com/TraceLD/BetterSsmp)
    * [Java implementation](/core/network-library)
* [Polychat protobufs](https://github.com/ModdedMinecraftClub/protos) repo
* The [Java client base](/client/client-base)

If you're writing a client or server, it's recommended to directly use the `protos` repo as a submodule or similar. If you're writing a client in Java, it is highly recommended to use message-library and network-library from this repo, and implement the PolychatClient interface in client-base. If you have any questions about how to implement something, feel free to ask in our [Discord](https://discord.gg/8EgWdQC).
