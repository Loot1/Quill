<div align="center">

# 📜 Quill — In-game book submission plugin

*Write it, submit it, let the staff review it.*

![Applications GUI](https://raw.githubusercontent.com/Loot1/quill/master/.github/assets/gui-applications.png)

</div>

---

## 📖 What is Quill?

Quill modernizes a **very old-school server workflow**: players write something in a signed book, submit it with a single command, and the staff team reviews it later through a dedicated in-game interface.

The original workflow was simple but awkward — write a book, physically drop it into a hopper, and hope an admin reads it eventually. Quill keeps the charm of that system while replacing everything that made it painful.

Even if its main use case is player applications, the system works for any book-based submission: whitelist forms, staff applications, roleplay character sheets, player reports, event registrations, lore entries, and much more.

---

## ✨ Features

- **One-command submission** — players submit their signed book directly with `/quill apply`
- **MySQL storage** — every submission saves title, content, author UUID, date and status
- **Paginated review GUI** — staff can browse all submissions in a clean, scrollable interface
- **Status management** — change submission status (Pending / Accepted / Refused / Archived) from a dedicated GUI

<div align="center">

![Status management GUI](https://raw.githubusercontent.com/Loot1/quill/master/.github/assets/gui-application-manager.png)

</div>

- **Status filters** — filter the global list by one or multiple statuses using toggle buttons
- **Per-player view** — browse a specific player's submissions with `/quill look <player>`
- **Self-review** — players can check their own submissions with `/quill self`
- **Staff notification on join** — staff members are notified when pending submissions are waiting

<div align="center">
<img src="https://raw.githubusercontent.com/Loot1/quill/master/.github/assets/notifications-applications-pending-on-join.png" alt="Pending applications on join" width="420"/>
</div>

- **Discord webhook** — optional notification for every new submission (author, title, page count)
- **Live reload** — reload the full configuration including the database connection with `/quill reload`
- **Async database access** — all DB queries run off the main thread to avoid any server lag
- **Legacy compatibility** — reads and converts old book data formats transparently
- **Fully customizable** — every message, GUI title, item name and status label is configurable in `config.yml`

<div align="center">

![Application in book](https://raw.githubusercontent.com/Loot1/quill/master/.github/assets/application-in-book.png)

</div>

---

## 🚀 Installation

1. Download the **Quill** JAR and place it in your `plugins/` folder
2. Start or restart your server to generate `plugins/Quill/config.yml`
3. Edit the file and fill in your MySQL credentials
4. Optionally paste a Discord webhook URL
5. Restart the server or run `/quill reload`

---

## ⚙️ Commands & Permissions

**Commands:**

| Command | Permission | Description |
|---------|------------|-------------|
| `/quill` | `quill.look` | Opens the global submissions GUI |
| `/quill apply` | `quill.apply` | Submits the signed book in the player's main hand |
| `/quill look <player>` | `quill.look` | Opens the submissions of a specific player |
| `/quill self` | `quill.self` | Displays the player's own submitted books |
| `/quill reload` | `quill.reload` | Reloads the configuration |

**Permissions:**

| Permission | Description |
|------------|-------------|
| `quill.apply` | Allows submitting a signed book |
| `quill.look` | Allows browsing all submissions |
| `quill.self` | Allows viewing own submissions |
| `quill.status` | Allows changing the status of a submission |
| `quill.notify` | Allows receiving pending/new submission notifications |
| `quill.reload` | Allows reloading the plugin configuration |
| `quill.*` | Grants all plugin permissions (OP by default) |

---

## 🔗 Discord Webhook (optional)

Quill can send a message to a Discord channel whenever a new book is submitted.

1. Create a webhook in your Discord channel settings
2. Paste its URL into `discord.webhook-url` in `config.yml`
3. Restart the server or run `/quill reload`
4. Submit a signed book with `/quill apply`

The notification includes the author's name, the book title, and the page count.

---

## 📦 Requirements

| Requirement | Details |
|-------------|---------|
| Server | Spigot / Paper 1.20.6 or higher |
| Java | 21 or higher |
| Database | MySQL — **Required** |

---

## 👤 Author

| Role | Author |
|------|--------|
| Plugin design, development & implementation | [Loot1](https://github.com/Loot1) |

