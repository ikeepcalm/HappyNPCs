# HappyNPCs

A Minecraft plugin for Paper 1.21.4 that allows you to create and manage interactive NPCs with dialogues.

## Features

- Create NPCs with custom names and entity types
- NPCs with interactive dialogues that use an Undertale-style typing animation
- Protect NPCs from damage and death
- Configure dialogues with MiniMessage formatting
- Add actions to dialogues (commands, sounds, titles)
- Hot-reload configuration files
- Support for ModelEngine and MythicMobs integration
- Per-player dialogue progress tracking
- Customize dialogue advancement key (F button by default)

## Requirements

- Paper 1.21.4
- Optional: ModelEngine (for custom 3D models)
- Optional: MythicMobs (for animations)

## Installation

1. Download the plugin JAR
2. Place it in your server's `plugins` folder
3. Restart your server
4. Edit the configuration files in `plugins/HappyNPCs/`

## Commands

- `/happynpc create <id> [name] [entityType]` - Create a new NPC
- `/happynpc spawnmm <id> [mythic-type] [name]` - Create a new Mythic Mobs NPC
- `/happynpc remove <id>` - Remove an NPC
- `/happynpc list` - List all NPCs
- `/happynpc move <id>` - Move an NPC to your location
- `/happynpc hide <id>` - Hide an NPC
- `/happynpc show <id>` - Show a hidden NPC
- `/happynpc protect <id>` - Protect an NPC from damage
- `/happynpc unprotect <id>` - Make an NPC vulnerable to damage
- `/happynpc setdialogue <id> <dialogueId>` - Set an NPC's dialogue
- `/happynpc reload` - Reload the plugin configuration

## Configuration

### config.yml

```yaml
# HappyNPCs Configuration

# Whether NPCs should be protected from damage by default
npc-protection: true

# Key to press to advance dialogue
# Supported keys: F (more may be added in the future)
dialogue-advance-key: "F"

# Sound to play when typing dialogue
# Use any Minecraft sound name
typing-sound: "ui.button.click"

# Speed of typing animation (ticks between characters)
# Lower values = faster typing
# Default: 2 (10 characters per second)
typing-speed: 1

# Debug mode
debug: false
```

### dialogues/example.yml

```yaml
example:
  # The line to start from when replaying the dialogue after completion
  restart-line: 1
  # Dialogue lines
  lines:
    1: "<gold>Hello there!</gold>"
    2: "<yellow>I'm an example NPC.</yellow>"
    3: "<green>Press F to continue the dialogue.</green>"
    
    # Actions to execute at specific lines
  actions:
    # When line 3 is completed, execute this command
    3: "command:tell %player% Dialogue finished!"
```

### Action Types

- `command:` - Execute a console command
- `player_command:` - Make the player execute a command
- `sound:` - Play a sound (format: `sound:name,volume,pitch`)
- `title:` - Show a title (format: `title:main,subtitle,duration`)
- `message:` - Send a message to the player

## MythicMobs Integration

To use MythicMobs with your NPCs:

1. Create an NPC with `/happynpc create <id> [name]`
2. Add the MythicMob ID to the NPC in `npcs.yml`:
```yaml
npcs:
  example:
    mythicMobId: "example_mob"
```

## License

This plugin is released under the MIT License.