# Vivecraft Spigot Extensions

## 🙏 Credits & Purpose of This Fork

This project is a **fork** of the original [Vivecraft Spigot Extensions](https://github.com/jrbudda/Vivecraft_Spigot_Extensions) by **jrbudda**. All credit for the original code, design, and features goes to jrbudda and the Vivecraft team.

**We are not trying to take over or replace the original project.** Our only goal is to get this amazing plugin working with the latest versions of Minecraft, because we love it and want to keep using it with new servers and VR players. If the original project is updated, we encourage everyone to use the official version!

---

VSE is a companion plugin for [Vivecraft](http://www.vivecraft.org), the VR mod for Java Minecraft. 
VSE is for [Spigot](https://www.spigotmc.org/) and [Paper](https://papermc.io/) servers and adds several enhancements for VR players.

**Latest Version:** `1.21.7-1.0.0`  
**Minecraft Compatibility:** 1.21.7 (Paper/Spigot)

---

> ⚠️ **WARNING:**
> 
> This fork has undergone basic functional testing on Paper 1.21.7. Core VR features work, but there are known issues (see below) affecting some advanced VR functionality. Please review the 'Known Issues' section before deploying in production.
> 
> Please report any issues you encounter on the [GitHub Issues](https://github.com/james-dwan/Vivecraft_Spigot_Extensions/issues) page.

---

# Installation
1. Download from the [Releases](https://github.com/james-dwan/Vivecraft_Spigot_Extensions/releases) page. **Ensure you download the correct version for your Minecraft server.**
2. Place the jar in your server's `/plugins` folder.
3. Restart the server.

> **Note:** This version (`1.21.7-1.0.0`) is **not backwards compatible** with previous Minecraft versions. Use the correct release for your server version.

---

# Migration Notes
- This release is updated for Minecraft 1.21.7 and Paper/Spigot API changes.
- NMS access and entity handling have been updated for Paperweight 1.21.7 compatibility.
- Vault is now an optional dependency; you will see a warning if it is not present, but all core VR features work without it.
- Please report any issues on the [GitHub Issues](https://github.com/james-dwan/Vivecraft_Spigot_Extensions/issues) page.

---

# How This Fork Was Migrated to 1.21.7

This fork was created to bring the original Vivecraft Spigot Extensions plugin up to date for Minecraft 1.21.7 (Paper/Spigot), following community best practices and extensive research. Our goal was to replicate the 1.20.6 functionality as closely as possible on the new version, while documenting all changes and challenges for future maintainers.

## Migration Approach
- **Systematic audit of all NMS (net.minecraft.server) access and entity casting.**
- **Replaced all direct NMS casts with reflection** (using `getHandle()`), as direct casting is broken in Paperweight 1.21.7.
- **Updated all NMS field accesses** using reflection and community-researched field names.
- **Implemented robust error handling and logging** for all reflection operations.
- **Removed all CraftBukkit imports** and replaced with Paperweight or Bukkit API alternatives.
- **Tested and validated each change iteratively** on a live Paper 1.21.7 server.

## Community Best Practices Followed
- Used official Mojang, MCP, and Fabric Yarn mappings for NMS field research.
- Cross-referenced with Paperweight documentation, Spigot/Paper forums, and Discord.
- Adopted the community-standard reflection pattern for NMS access in modern Paper.
- Maintained clear separation between Bukkit API and NMS code.
- Documented all known issues and workarounds for future updates.

## Key Technical Challenges
- **NMS Reflection:** All NMS access now requires reflection due to Paperweight's changes. Direct casting from Bukkit/CraftPlayer to NMS types is no longer possible.
- **Obfuscated Field Names:** Many NMS field names are obfuscated and change between versions. We used community mappings and runtime discovery where possible to resolve these.
- **Channel Access:** Successfully migrated Netty channel access for aim fix features using reflection (field `"f"`).
- **Pose System:** The general VR pose system (for sitting, standing, etc.) has been restored. This was achieved by reverse-engineering the client's `VR_PLAYER_STATE` network packet and creating a robust, server-side deserializer.
- **API Changes:** Updated for new method signatures, inventory access, and entity goal selectors as required by 1.21.7.

**This fork is a community-driven effort to keep Vivecraft Spigot Extensions alive and working for the latest Minecraft servers. If you have suggestions, fixes, or mapping updates, please contribute or open an issue!**

## Build System Modernization
- **Migrated the build system to Gradle with Paperweight.** This enables smoother, more reliable builds and dependency management, and is the community standard for modern Minecraft plugin development.
- **Automated builds and easier updates:** Using Gradle allows for automated builds (CI/CD), reproducible environments, and easier upgrades to future Minecraft/Paper versions.
- **See the `build.gradle` and migration plans for details on the build setup and how to build the plugin.**

---

# Known Issues (as of 1.21.7-1.0.0)

> ✅ **The VR Pose Override system for sitting/standing is now functional.**
>
> - **VR Crawling (Partially Functional):** The specific feature to crawl under 1-block gaps (using the swimming pose) has a known timing issue. Players can only pass under a block *immediately* after the pose change is initiated. If there is a delay, the player's collision appears to revert to its standing size, blocking them. This is a known bug we are investigating.
>
> Please report any other issues you discover on the [GitHub Issues](https://github.com/james-dwan/Vivecraft_Spigot_Extensions/issues) page.

---

# Support
For help, bug reports, or feature requests, please use the [GitHub Issues](https://github.com/james-dwan/Vivecraft_Spigot_Extensions/issues) page.

---

# License
See [LICENSE](LICENSE) for details.
