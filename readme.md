# 🎫 PassportPlugin

<div align="center">

![Minecraft](https://img.shields.io/badge/Minecraft-1.21.1-brightgreen.svg)
![Paper](https://img.shields.io/badge/Paper-API-blue.svg)
![Java](https://img.shields.io/badge/Java-22-orange.svg)
![License](https://img.shields.io/badge/License-MIT-yellow.svg)
![Version](https://img.shields.io/badge/Version-2.0-red.svg)

**Modern passport system for Minecraft servers**

[Features](#-features) • [Installation](#-installation) • [Commands](#-commands) • [Configuration](#-configuration) • [API](#-api)

</div>

---

## 📋 Description

PassportPlugin is an advanced plugin for Paper 1.21.1 that adds a complete passport system to your server. Players can create personalized passports with name, surname, age, and country, which are displayed in a beautiful interactive GUI.

## ✨ Features

### 🎨 Modern Interface
- Beautiful GUI using MiniMessage gradients
- Interactive buttons and visual elements
- Country texture support (flags as player heads)
- Unified color palette for readability

### 📝 Passport Management
- Create passports with AnvilGUI for data input
- Edit existing passports
- View your own passport
- Request to view other players' passports
- Automatic display of name and age above player's head

### 🌍 Country System
- 9 preset countries (Russia, USA, Germany, France, UK, China, Japan, Canada, Italy)
- Unique passport series for each country
- Automatic generation of unique passport numbers
- Easy to add new countries through configuration

### 🔒 Request System
- Administrators can request to view player's passport
- Player can accept or deny the request
- Interactive accept/deny buttons
- Request timeout after 30 seconds

### 🌐 Multi-language
- Russian (ru-RU)
- English (en-EN)
- Simple system for adding new languages
- All messages are customizable

### 🛠️ Developer API
- Simple API for getting passport data
- Placeholder support
- Easy integration with other plugins

## 📦 Installation

1. Download the latest version from [Releases](https://github.com/Lolka12321/PassportPlugin-1.21/releases)
2. Place the JAR file in your server's `plugins/` folder
3. Restart the server
4. Configure the plugin in config files (optional)

### Requirements

- **Minecraft**: 1.21.1
- **Server**: Paper API (or forks: Purpur, Pufferfish, etc.)
- **Java**: 22+

## 🎮 Commands

| Command | Description | Permission |
|---------|-------------|------------|
| `/passport` | View your passport | `passport.view` |
| `/passport create` | Create a new passport | `passport.create` |
| `/passport edit` | Edit your passport | `passport.edit` |
| `/passport help` | Show help menu | `passport.help` |
| `/passport check <player>` | Request to view player's passport | `passport.check` |
| `/passport remove <player>` | Remove player's passport | `passport.remove` |
| `/passport reload` | Reload configuration | `passport.reload` |
| `/passport accept` | Accept view request | `passport.accept` |
| `/passport deny` | Deny view request | `passport.deny` |

## 🔑 Permissions

### Basic Permissions (for players)

| Permission | Description | Default |
|------------|-------------|---------|
| `passport.*` | Access to all basic commands | true |
| `passport.view` | View your own passport | true |
| `passport.create` | Create a new passport | true |
| `passport.edit` | Edit your passport | true |
| `passport.help` | View help menu | true |
| `passport.accept` | Accept view requests | true |
| `passport.deny` | Deny view requests | true |

### Admin Permissions

| Permission | Description | Default |
|------------|-------------|---------|
| `passport.admin` | Access to all admin commands | OP |
| `passport.check` | Request to view other players' passports | OP |
| `passport.remove` | Remove players' passports | OP |
| `passport.reload` | Reload plugin configuration | OP |

## ⚙️ Configuration

### config.yml

```yaml
# Plugin language (en-EN or ru-RU)
language: "en-EN"

# GUI settings
gui:
  create:
    name-slot: 10
    surname-slot: 12
    age-slot: 14
    country-slot: 16
    confirm-slot: 22
    cancel-slot: 31
  # ... other settings

# Countries
countries:
  russia:
    name: "Russia"
    series: "RU"
    texture: "base64_texture"
  # ... other countries
```

### Adding a New Country

```yaml
countries:
  your_country:
    name: "Your Country Name"
    series: "YC"  # 2-letter series
    texture: "base64_encoded_skull_texture"
```

## 🔌 API

### Using the API

```java
import fellangera.passport.api.PassportAPI;
import org.bukkit.entity.Player;

public class YourPlugin {
    public void example(Player player) {
        // Get player's name from passport
        String name = PassportAPI.get(player, "%name%");
        
        // Get surname
        String surname = PassportAPI.get(player, "%surname%");
        
        // Get age
        String age = PassportAPI.get(player, "%age%");
        
        // Get country
        String region = PassportAPI.get(player, "%region%");
        
        // Get passport ID (series + number)
        String passportId = PassportAPI.get(player, "%passport_id%");
    }
}
```

### Available Placeholders

- `%name%` - Name
- `%surname%` - Surname
- `%age%` - Age
- `%region%` - Country
- `%passport_id%` - Passport series and number

## 🎨 Screenshots

### Create Passport
![Create Passport GUI](https://via.placeholder.com/800x400?text=Create+Passport+GUI)

### View Passport
![View Passport GUI](https://via.placeholder.com/800x400?text=View+Passport+GUI)

### Country Selection
![Country Selection](https://via.placeholder.com/800x400?text=Country+Selection)

## 🏗️ Building from Source

```bash
# Clone the repository
git clone https://github.com/Lolka12321/PassportPlugin-1.21.git
cd PassportPlugin-1.21

# Build with Maven
mvn clean package

# The compiled JAR will be in the target/ folder
```

## 📝 Changelog

See [CHANGELOG.md](CHANGELOG.md) for a complete list of changes.

## 🤝 Contributing

We welcome contributions to the project! If you want to help:

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/AmazingFeature`)
3. Commit your changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to the branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request

## 📄 License

This project is distributed under the MIT License. See the [LICENSE](LICENSE) file for details.

## 📧 Contact

- **GitHub**: [@Lolka12321](https://github.com/Lolka12321)
- **Issues**: [GitHub Issues](https://github.com/Lolka12321/PassportPlugin-1.21/issues)

## 🙏 Credits

- **AnvilGUI** - [WesJD/AnvilGUI](https://github.com/WesJD/AnvilGUI)
- **Paper API** - [PaperMC](https://papermc.io/)
- **Adventure API** - [KyoriPowered](https://github.com/KyoriPowered/adventure)

---

<div align="center">

**Made with ❤️ for Minecraft community**

⭐ If you like this project, give it a star!

</div>
