package org.vivecraft.utils;

// Removed CraftBukkit import - using Paperweight NMS access instead
import org.bukkit.entity.Player;
import org.vivecraft.Reflector;
import org.vivecraft.VSE;
import org.vivecraft.VivePlayer;

import net.minecraft.world.entity.Pose;

public class PoseOverrider {
	public static void setPlayerPose(Player player, Pose pose) {
		try {
			Object nmsPlayer = player.getClass().getMethod("getHandle").invoke(player);
			Reflector.invoke(Reflector.setPose, nmsPlayer, pose);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
