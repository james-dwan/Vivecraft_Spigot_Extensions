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
> This fork is **untested beyond verifying that it starts up on Paper 1.21.7**. It has not been thoroughly tested in production or with real VR players. **Use with caution!**
> 
> Please report any issues you encounter on the [GitHub Issues](https://github.com/james-dwan/Vivecraft_Spigot_Extensions/issues) page.

---

# Features
- Vivecraft players will see other Vivecraft players' head and arm movements.
- Support for Vivecraft 2-handed archery.
- Assign permission groups for VR players (Vault optional).
- Fixes projectiles and dropped items from VR players.
- Shrinks Creeper explosion radius for VR players from 3 to 1.75m (Configurable).
- Option to limit server to Vivecraft players only.

See the `config.yml` for all available configuration options.

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

# Known Issues (as of 1.21.7-1.0.0)

> ⚠️ **VR Crawling and Pose Override are currently broken in this version.**
>
> - **VR Crawling:** VR players cannot crawl (switch to swimming pose) due to changes in Minecraft's internal field mappings. This does not affect basic VR play, but disables crawling in VR.
> - **VR Pose Override:** Custom pose handling for VR players is not functional. This means some VR-specific animations or pose changes may not work as intended.
>
> These issues are due to changes in Minecraft 1.21.7's NMS (net.minecraft.server) internals. We are actively researching a fix. All other core VR features are working.

---

# Developer Information

## Metadata
VSE provides Spigot metadata on `Player` objects so other plugins can provide special support for handed interactions or somesuch. If you aren't sure what metadata is, check the [Spigot documentation](https://hub.spigotmc.org/javadocs/spigot/org/bukkit/metadata/Metadatable.html). The API supports multiple plugins using the same metadata key, so make sure you filter to our specific plugin name (`Vivecraft-Spigot-Extensions`).

Every player has a head and two hands (obviously), each of which have a 6DOF position and rotation. There are also some tertiary values so you can determine how to properly handle a particular player. The full set of available keys is as follows:

Key(s) | Value
--- | -----
`head.pos`, `righthand.pos`, `lefthand.pos` | `Location` representing the absolute position in the world of the VR object. Also includes the direction for convenience.
`head.dir`, `righthand.dir`, `lefthand.dir` | `Vector` representing the forward direction of the VR object. This is gimbal locked; if you want up or right vectors, use the `rot` value below.
`head.rot`, `righthand.rot`, `lefthand.rot` | Array of 4 floats, representing a quaternion with the order `w,x,y,z`. You'll need a `Quaternion` class to deal with this properly, but it's much more flexible than the `dir` value. Feel free to use the one in this repository.
`seated` | `Boolean` representing the player is in seated mode. This mode disables hand tracking and places the VR hands to the sides of the head, to allow for keyboard and mouse play in VR.
`height` | `Float` representing the player's calibrated height, which mainly affects how tall they appear to other players.
`activehand` | `String` representing which hand (left or right) last performed some actions. Currently throwing projectiles such as snowballs.

---

# Support
For help, bug reports, or feature requests, please use the [GitHub Issues](https://github.com/james-dwan/Vivecraft_Spigot_Extensions/issues) page.

---

# License
See [LICENSE](LICENSE) for details.
