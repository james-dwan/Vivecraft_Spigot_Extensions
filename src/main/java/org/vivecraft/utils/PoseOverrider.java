package org.vivecraft.utils;

// Removed CraftBukkit import - using Paperweight NMS access instead
import org.bukkit.entity.Player;
import org.vivecraft.Reflector;
import org.vivecraft.VSE;
import org.vivecraft.VivePlayer;

import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.network.syncher.SynchedEntityData.DataItem;
import net.minecraft.world.entity.Pose;


public class PoseOverrider {
	@SuppressWarnings("unchecked")
	public static void injectPlayer(Player player) {
		// TODO: VR crawling and pose override are disabled in 1.21.7 due to NMS field mapping changes.
		// See README and NMS_MIGRATION_PLAN.md for details.
		// When a working mapping for Entity_Data_Pose is found, restore pose override logic here.
		VSE.me.getLogger().warning("[Vivecraft] VR crawling and pose override are currently disabled due to NMS field mapping changes in 1.21.7. See README for details.");
	}

	public static class InjectedDataWatcherItem extends SynchedEntityData.DataItem<Pose> {
		protected final Player player;

		public InjectedDataWatcherItem(EntityDataAccessor<Pose> datawatcherobject, Pose t0, Player player) {
			super(datawatcherobject, t0);
			this.player = player;
		}

		@Override
		public void setValue(Pose pose) {
			VivePlayer vp = VSE.vivePlayers.get(player.getUniqueId());
			if (vp != null && vp.isVR() && vp.crawling)
				super.setValue(Pose.SWIMMING);
			else
				super.setValue(pose);
		}
	}
}
