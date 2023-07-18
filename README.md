# ResourcePackSelector

ResourcePackSelector is a Bukkit plugin that allows players to select from a list of server-approved resource packs. Players can choose their preferred resource pack, and the plugin will apply it to enhance their gameplay experience.

## Features

- GUI-based resource pack selection: Players can use the `/changerep` command to open a graphical user interface and choose a resource pack from the available options.
- Permission-based access: Resource packs can be associated with specific permission nodes, allowing server administrators to control which players can access each resource pack.
- Random resource pack selection: The `/randomrep` command selects a random resource pack from the available options for the player, provided they have the necessary permission.
- Configuration flexibility: The plugin's configuration file allows easy customization of resource packs, their associated URLs, and other settings.
- Reload configuration: The `/reloadrpsconf` command allows server administrators to dynamically reload the plugin's configuration file without restarting the server.
- Cooldowns: Server administrators can configure a cooldown period between resource pack changes to control how frequently players can switch resource packs.

## Permissions

To use this plugin, the following permission nodes are available:

- `resourcepackselector.pack.default`: Allows the player to select the "Default" resource pack.
- `resourcepackselector.pack.custom1`: Allows the player to select the "Custom1" resource pack.
- `resourcepackselector.pack.custom2`: Allows the player to select the "Custom2" resource pack.
- `resourcepackselector.command.changerep`: Allows the player to use the `/changerep` command.
- `resourcepackselector.command.randomrep`: Allows the player to use the `/randomrep` command.
- `resourcepackselector.reloadconf`: Allows server administrators to use the `/reloadrpsconf` command to reload the plugin's configuration file.

Make sure to assign the appropriate permissions to the respective resource packs and commands in your permission manager.

## Commands

- `/changerep`: Opens a GUI for the player to select a resource pack from the available options. Requires the player to have the necessary permission node for the chosen resource pack (`resourcepackselector.pack.<pack>`).
  - Permission: `resourcepackselector.command.changerep`

- `/randomrep`: Selects a random resource pack from the available options for the player. Requires the player to have the necessary permission node for at least one of the available resource packs (`resourcepackselector.pack.<pack>`).
  - Permission: `resourcepackselector.command.randomrep`

- `/reloadrpsconf`: Reloads the ResourcePackSelector configuration file dynamically without restarting the server. Requires the `resourcepackselector.reloadconf` permission.
  - Permission: `resourcepackselector.reloadconf`

## Configuration

The configuration file `ResourcePackSelector/config.yml` allows you to specify the resource packs, their associated URLs, and various plugin settings.

The structure of the configuration file is as follows:

```yaml
resourcePacks:
  Default:
    url: <resource_pack_url>
    permission: resourcepackselector.pack.default
  Custom1:
    url: <resource_pack_url>
    permission: resourcepackselector.pack.custom1
  Custom2:
    url: <resource_pack_url>
    permission: resourcepackselector.pack.custom2

Settings:
  promptAtJoin: true

cooldown:
  enabled: true
  duration: 300 # Cooldown duration in seconds
```

- `resourcePacks`: This section contains the resource packs available for selection. Each resource pack should have a unique key (e.g., "Default", "Custom1", "Custom2"). For each resource pack, specify the URL where the resource pack can be downloaded and the permission node required to use that resource pack.

- `Settings.promptAtJoin`: If set to `true`, players will be prompted to select a resource pack upon joining the server. If set to `false`, the prompt will be skipped, and players will use the previously selected resource pack or the default resource pack.

- `cooldown`: This section allows server administrators to configure the cooldown settings for changing resource packs.
  - `cooldown.enabled`: Set to `true` to enable the cooldown feature, or `false` to disable it.
  - `cooldown.duration`: The duration of the cooldown in seconds.

Ensure that you have the correct URLs and permission nodes for the resource packs in the configuration file.

## Usage

1. Place the ResourcePackSelector.jar file in the `plugins` directory of your Bukkit server.
2. Start the server to generate the default configuration file.
3. Edit the `ResourcePackSelector/config.yml` file to add your desired resource packs, their URLs, and permission nodes. Customize other plugin settings as needed.
4. Restart the server for the changes to take effect.
5. Players with the appropriate permissions can use the `/changerep` command to open the resource pack selection GUI and choose a resource pack.
6. Players with the appropriate permissions can use the `/randomrep` command to select a random resource pack from the available options.
7. Server administrators with the `resourcepackselector.reloadconf` permission can use the `/reloadrpsconf` command to dynamically reload the plugin's configuration file.

That's it! With ResourcePackSelector, you can provide players with a curated selection of resource packs to enhance their gameplay experience on your server.