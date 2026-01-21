# 📘 PassportPlugin

Modern passport system for Paper / Spigot Minecraft servers.

PassportPlugin forces players to create a personal passport on first join, assigns a unique passport ID, replaces the player nickname with real data, and displays the passport as a customizable in-game book.

---

## ✨ Features

- 📖 Passport displayed as a **written book**
- 🧾 **Unique passport series and number** (never duplicates)
- 👤 Player nickname replaced with **Name, Age**
- 🔒 Player movement blocked until passport is created
- 🗂 Fully customizable passport book via `config.yml`
- 🌍 **Multi-language system**
  - English (`en-EN.yml`) — default
  - Russian (`ru-RU.yml`)
- 🎨 Advanced color support:
  - `&` legacy colors
  - `&#RRGGBB` HEX colors
  - `<gradient>` MiniMessage gradients
- 🔄 Reload config and language **without server restart**
- 🔍 Admin command to view other players’ passports
- 🧠 Smart tab-completion based on permissions

---

## 📜 Commands

| Command | Description | Permission |
|------|------------|-----------|
| `/passport` | Open your passport | — |
| `/passport help` | Show help menu | — |
| `/passport check <player>` | View another player’s passport | `passport.check` |
| `/passport reload` | Reload config and language | `passport.reload` |

---

## 🔐 Permissions

| Permission | Description | Default |
|-----------|------------|---------|
| `passport.check` | View other players’ passports | OP |
| `passport.reload` | Reload plugin configuration | OP |

---

## 🌍 Language System

Language is selected in `config.yml`:

```yml
language: "en-EN"
```

Available languages:
- `en-EN.yml`
- `ru-RU.yml`

You can freely edit or add your own language files.

Reload language in-game:
```
/passport reload
```

---

## 📖 Passport Book Customization

The passport book is fully configurable:

- Title
- Author
- Pages
- Placeholders
- Colors and gradients

### Available placeholders

```
%name%
%surname%
%age%
%region%
%series%
%number%
%passport_id%
```

---

## ⚙ Requirements

- **Paper / Spigot 1.21+**
- **Java 21 or newer**

---

## 📦 Installation

1. Download the plugin `.jar`
2. Place it into the `/plugins` directory
3. Start the server
4. Configure `config.yml`
5. Restart the server or use `/passport reload`

---

## 🛠 Planned Features

- GUI passport
- Admin passport editor
- MySQL support
- PlaceholderAPI hook
- Player photo (skin head)
- Multiple passport templates

---

## 📄 License

MIT License

---

## ⭐ Support

If you like this project, please ⭐ the repository  
Bug reports and feature requests are welcome via GitHub Issues.
