# Quill - Minecraft Plugin

An in-game submission plugin for Minecraft servers that lets players submit signed books and allows staff members to review them later through dedicated GUIs, status management, MySQL storage, and optional Discord notifications.

I created this plugin because, when I was younger, the first Minecraft server I truly spent time on had a long-term progression system based on applications written in books. Players would write their application in-game, drop the signed book into a hopper, and admins would read it afterwards. Quill is the same core idea, rebuilt in a more modern, organized, and practical way for current servers.

Even though its main use case is player applications, Quill can also be used for many other book-based workflows: whitelist forms, staff recruitment, roleplay backstories, reports, event registrations, lore submissions, or any system where players need to submit written content directly from Minecraft.

## 📋 Features

### 📖 Submission System
- Submit signed books directly with `/quill apply`
- Save book title, content, author UUID, date, and status in MySQL
- Read submitted books back in-game through the written book interface
- Preserve compatibility with legacy stored content while using JSON encoding for newer entries

### 🧭 Review & Moderation
- Browse all submissions in a paginated GUI
- Browse a specific player's submissions with `/quill look <player>`
- Let players review their own submissions with `/quill self`
- Change submission status from a dedicated management GUI
- Filter the global list by status using GUI toggle buttons

### 🔧 Advanced Features
- Full permission management
- Async database access for submission loading and browsing
- Reload configuration without restarting the server
- Staff notification on join when pending submissions exist
- Fully customizable messages, GUI titles, item names, and status labels

### 🤖 Discord Integration
- Optional webhook notification for new submissions
- Includes author name, title, and page count
- Safe fallback if webhook sending fails

## 🚀 Installation

### Requirements
- A Spigot-compatible Minecraft server
- Plugin `api-version: 1.20`
- Java `25`
- MySQL database access
- Maven (if you want to build from source)

### Steps
1. Build the plugin JAR or use the generated one from `target/`.
2. Place the JAR file in your server's `plugins` folder.
3. Start or restart the server once to generate `plugins/Quill/config.yml`.
4. Configure the MySQL connection settings.
5. Optionally set a Discord webhook URL.
6. Restart the server.

## ⚙️ Configuration

### Main Files
- `plugins/Quill/config.yml` — General configuration for messages, GUI text, statuses, database connection, and Discord webhook

### Commands
| Command | Permission | Description |
|---------|------------|-------------|
| `/quill` | `quill.look` | Opens the global submissions GUI |
| `/quill apply` | `quill.apply` | Submits the signed book in the player's main hand |
| `/quill look <player>` | `quill.look` | Opens the submissions of a specific player |
| `/quill self` | `quill.self` | Displays the player's own submitted books |
| `/quill reload` | `quill.reload` | Reloads the configuration |

### Permissions
| Permission | Description | Default |
|------------|-------------|---------|
| `quill.*` | Grants all plugin permissions | OP |
| `quill.apply` | Allows submitting a signed book | OP |
| `quill.look` | Allows browsing submissions | OP |
| `quill.notify` | Allows receiving pending/new submission notifications | OP |
| `quill.reload` | Allows reloading the plugin configuration | OP |
| `quill.status` | Allows changing the status of a submission | OP |
| `quill.self` | Allows viewing your own submissions | OP |

## 🎨 Customization

### Menus & Messages
- Customizable messages under `messages`
- Customizable GUI labels under `menus`
- Customizable status names under `settings.status`
- Custom status colors in the application management menu

### Status Workflow
Quill currently supports four submission states:

- `Pending`
- `Accepted`
- `Refused`
- `Archived`

### Typical Uses
- Player applications
- Staff applications
- Whitelist requests
- Faction or guild recruitment forms
- Roleplay character sheets and backstories
- Player reports
- Event participation forms
- Lore or story submissions

## 📊 Settings Reference

```yaml
messages:
  configuration-reload: '&7Configuration has been reloaded.'
  apply-done: '&7Application &d%title%&7 has been submitted.'

settings:
  status:
    waiting: 'Pending'
    accepted: 'Accepted'
    refused: 'Refused'
    archived: 'Archived'

database:
  username: 'your_username'
  password: 'your_password'
  name: 'your_database'
  url: 'localhost:3306'

discord:
  webhook-url: ''
```

> [!IMPORTANT]
> Do not publish real database credentials in a public repository. Replace sensitive values in `config.yml` before sharing the project.

## 🔗 Discord Webhook Setup

1. Create a webhook in your Discord channel settings.
2. Paste its URL into `discord.webhook-url`.
3. Restart the server or run `/quill reload`.
4. Submit a signed book with `/quill apply`.

### Webhook Message Content
When enabled, Quill sends a message containing:

- the author name
- the book title
- the page count

### Example Configuration

```yaml
discord:
  webhook-url: 'https://discord.com/api/webhooks/your_webhook_here'
```

## 🗂️ Project Structure

```
src/main/java/fr/loot1/quill/
├── Quill.java                          # Main plugin class
├── commands/
│   └── QuillCommand.java               # /quill command handler
├── guis/
│   ├── GuiApplicationManager.java      # Status management GUI
│   ├── GuiApplications.java            # Global applications GUI
│   ├── GuiHolder.java                  # Shared GUI utilities
│   ├── GuiPaginatedApplications.java   # Paginated GUI base class
│   └── GuiPlayerApplications.java      # Player-specific applications GUI
├── listeners/
│   ├── GuiListener.java                # GUI event listener
│   └── PlayerJoinListener.java         # Pending applications notification on join
├── managers/
│   ├── ConfigManager.java              # Config file manager
│   ├── DatabaseManager.java            # MySQL access and persistence
│   └── PlayerCacheManager.java         # Cached player names for tab completion
├── objects/
│   ├── Application.java                # Submission data model
│   └── ApplicationList.java            # Paginated result wrapper
└── utils/
    ├── DiscordWebhook.java             # Discord webhook HTTP client
    └── GlowHelper.java                 # GUI glow helper
```

## Dependencies

| Dependency | Type | Notes |
|------------|------|-------|
| Spigot API `1.21.11-R0.1-SNAPSHOT` | Provided | Build-time API dependency |
| Java `25` | Runtime | Defined in `pom.xml` |
| HikariCP `7.0.2` | Library | Database connection pooling |
| Gson `2.10.1` | Library | Book content serialization |
| MySQL | External service | Required for storage |

## Authors

| Role | Author |
|------|--------|
| Plugin design, development, and implementation | **Loot1** |

> This plugin is a modern Minecraft adaptation of an older in-game book submission workflow: write a book, submit it, and let staff review it later.