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
			// Use getHandle() reflection to access NMS Player first
			try {
				Object nmsEntity = player.getClass().getMethod("getHandle").invoke(player);
				net.minecraft.world.entity.player.Player nmsPlayer = (net.minecraft.world.entity.player.Player) nmsEntity;
				
				// Discover the correct Entity_Data_Pose field name for 1.21.7
				EntityDataAccessor<Pose> poseObj = null;
				
				// List all fields on the Entity class to find the correct field name
				VSE.me.getLogger().info("Listing all fields on Entity class to find Entity_Data_Pose:");
				java.lang.reflect.Field[] allFields = nmsPlayer.getClass().getDeclaredFields();
				for (java.lang.reflect.Field field : allFields) {
					try {
						field.setAccessible(true);
						Object fieldValue = field.get(nmsPlayer);
						
						// Check if this field is an EntityDataAccessor
						if (fieldValue instanceof net.minecraft.network.syncher.EntityDataAccessor) {
							VSE.me.getLogger().info("Found EntityDataAccessor field: " + field.getName() + " | Type: " + fieldValue.getClass().getName());
							
							// Check if this is the pose field by looking at the accessor's ID
							// The pose accessor typically has a specific ID
							if (fieldValue instanceof net.minecraft.network.syncher.EntityDataAccessor) {
								net.minecraft.network.syncher.EntityDataAccessor<?> accessor = (net.minecraft.network.syncher.EntityDataAccessor<?>) fieldValue;
								VSE.me.getLogger().info("  Accessor ID: " + accessor.id());
								
								// The pose accessor typically has ID around 6-8
								if (accessor.id() >= 6 && accessor.id() <= 8) {
									poseObj = (EntityDataAccessor<Pose>) fieldValue;
									VSE.me.getLogger().info("Successfully found Entity_Data_Pose field: " + field.getName() + " (ID: " + accessor.id() + ")");
									break;
								}
							}
						}
					} catch (Exception e) {
						// Continue to next field
					}
				}
				
				if (poseObj == null) {
					VSE.me.getLogger().warning("Failed to find Entity_Data_Pose field in 1.21.7 - listing all EntityDataAccessor fields found:");
					for (java.lang.reflect.Field field : allFields) {
						try {
							field.setAccessible(true);
							Object fieldValue = field.get(nmsPlayer);
							if (fieldValue instanceof net.minecraft.network.syncher.EntityDataAccessor) {
								net.minecraft.network.syncher.EntityDataAccessor<?> accessor = (net.minecraft.network.syncher.EntityDataAccessor<?>) fieldValue;
								VSE.me.getLogger().warning("  EntityDataAccessor field: " + field.getName() + " (ID: " + accessor.id() + ")");
							}
						} catch (Exception e) {
							// Continue to next field
						}
					}
					return;
				}
				
				SynchedEntityData dataWatcher = nmsPlayer.getEntityData();
				SynchedEntityData.DataItem<?>[] entries = (DataItem<?>[]) Reflector.getFieldValue(Reflector.SynchedEntityData_itemsById, dataWatcher);
				InjectedDataWatcherItem item = new InjectedDataWatcherItem(poseObj, Pose.STANDING, player);
				if(entries.length-1 >= poseObj.id())
					entries[poseObj.id()] = item;		
			} catch (Exception ex) {
				VSE.me.getLogger().warning("Failed to access NMS Player for pose override: " + ex.getMessage());
			}
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
