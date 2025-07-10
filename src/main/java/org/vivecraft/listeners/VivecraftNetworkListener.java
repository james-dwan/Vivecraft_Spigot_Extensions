package org.vivecraft.listeners;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.Arrays;

// Removed CraftBukkit import - using Paperweight NMS access instead
import org.bukkit.entity.Player;
import org.bukkit.plugin.messaging.PluginMessageListener;
import org.vivecraft.Reflector;
import org.vivecraft.VSE;
import org.vivecraft.VivePlayer;
import org.vivecraft.utils.MetadataHelper;
import org.vivecraft.utils.PoseOverrider;
import org.vivecraft.utils.VrPlayerState;

import net.minecraft.network.protocol.game.ClientboundPlayerInfoUpdatePacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public class VivecraftNetworkListener implements PluginMessageListener {
	public VSE vse;

	public VivecraftNetworkListener(VSE vse){
		this.vse = vse;
	}

	public enum PacketDiscriminators {
		VERSION,
		REQUESTDATA,
		HEADDATA,
		CONTROLLER0DATA,
		CONTROLLER1DATA,
		WORLDSCALE,
		DRAW,
		MOVEMODE,
		UBERPACKET,
		TELEPORT,
		CLIMBING,
		SETTING_OVERRIDE,
		HEIGHT,
		ACTIVEHAND,
		CRAWL,
		NETWORK_VERSION,
		VR_SWITCHING,
		IS_VR_ACTIVE,
		VR_PLAYER_STATE
	}

	@Override
	public void onPluginMessageReceived(String channel, Player sender, byte[] payload) {

		if(!channel.equalsIgnoreCase(VSE.CHANNEL)) return;

		if(payload == null || payload.length==0) return;

		VivePlayer vp = VSE.vivePlayers.get(sender.getUniqueId());

		int packetId = payload[0] & 0xFF;
		if (packetId >= PacketDiscriminators.values().length) {
			// Invalid packet ID
			return;
		}

		PacketDiscriminators disc = PacketDiscriminators.values()[packetId];

		if(vp == null && disc != PacketDiscriminators.VERSION) {
			//how?
					return;
		}

		byte[] data = Arrays.copyOfRange(payload, 1, payload.length);
		switch (disc){
		case CONTROLLER0DATA:
			vp.controller0data = data;
			MetadataHelper.updateMetdata(vp);
			break;
		case CONTROLLER1DATA:
			vp.controller1data = data;
			MetadataHelper.updateMetdata(vp);
			break;
		case DRAW:
			vp.draw = data;
			break;
		case HEADDATA:
			vp.hmdData = data;
			MetadataHelper.updateMetdata(vp);
			break;
		case MOVEMODE:
			break;
		case REQUESTDATA:
			//only we can use that word.
			break;
		case VERSION:
			vp = new VivePlayer(sender);
			ByteArrayInputStream byin = new ByteArrayInputStream(data);
			DataInputStream da = new DataInputStream(byin);
			InputStreamReader is = new InputStreamReader(da);
			BufferedReader br = new BufferedReader(is);
			VSE.vivePlayers.put(sender.getUniqueId(), vp);

			sender.sendPluginMessage(vse, VSE.CHANNEL, StringToPayload(PacketDiscriminators.VERSION, vse.getDescription().getFullName()));

			try {
				String version = br.readLine();
				vp.version = version;
				if(version.contains("NONVR")){
					vp.setVR(false);
				}
				else{
					vp.setVR(true);
				}

				if(vse.getConfig().getBoolean("SendPlayerData.enabled") == true)
					sender.sendPluginMessage(vse, VSE.CHANNEL, new byte[]{(byte) PacketDiscriminators.REQUESTDATA.ordinal()});

				if(vse.getConfig().getBoolean("general.vive-only") == false)
					sender.sendPluginMessage(vse, VSE.CHANNEL, new byte[]{(byte) PacketDiscriminators.VR_SWITCHING.ordinal(), 1});
				else
					sender.sendPluginMessage(vse, VSE.CHANNEL, new byte[]{(byte) PacketDiscriminators.VR_SWITCHING.ordinal(), 0});

				if(vse.getConfig().getBoolean("climbey.enabled") == true){

					final ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

					byteArrayOutputStream.write(PacketDiscriminators.CLIMBING.ordinal());
					byteArrayOutputStream.write(1); // climbey allowed

					String mode = vse.getConfig().getString("climbey.blockmode","none");
					if(!sender.hasPermission(vse.getConfig().getString("permissions.climbperm"))){
						if(mode.trim().equalsIgnoreCase("include"))
							byteArrayOutputStream.write(1);
						else if(mode.trim().equalsIgnoreCase("exclude"))
							byteArrayOutputStream.write(2);
						else
							byteArrayOutputStream.write(0);
					} else {
						byteArrayOutputStream.write(0);
					}

					for (String block : vse.blocklist) {
						if (!writeString(byteArrayOutputStream, block))
							vse.getLogger().warning("Block name too long: " + block);
					}

					final byte[] p = byteArrayOutputStream.toByteArray();
					sender.sendPluginMessage(vse, VSE.CHANNEL, p);
				}

				if (vse.getConfig().getBoolean("teleport.limitedsurvival")) {
					final ByteArrayOutputStream baos = new ByteArrayOutputStream();

					baos.write(PacketDiscriminators.SETTING_OVERRIDE.ordinal());

					writeSetting(baos, "limitedTeleport", true); // do it
					writeSetting(baos, "teleportLimitUp", Mth.clamp(vse.getConfig().getInt("teleport.uplimit"), 0, 4));
					writeSetting(baos, "teleportLimitDown", Mth.clamp(vse.getConfig().getInt("teleport.downlimit"), 0, 16));
					writeSetting(baos, "teleportLimitHoriz", Mth.clamp(vse.getConfig().getInt("teleport.horizontallimit"), 0, 32));

					final byte[] p = baos.toByteArray();
					sender.sendPluginMessage(vse, VSE.CHANNEL, p);
				}

				if (vse.getConfig().getBoolean("worldscale.limitrange")) {
					final ByteArrayOutputStream baos = new ByteArrayOutputStream();

					baos.write(PacketDiscriminators.SETTING_OVERRIDE.ordinal());

					writeSetting(baos, "worldScale.min", Mth.clamp(vse.getConfig().getDouble("worldscale.min"), 0.1, 100));
					writeSetting(baos, "worldScale.max", Mth.clamp(vse.getConfig().getDouble("worldscale.max"), 0.1, 100));

					final byte[] p = baos.toByteArray();
					sender.sendPluginMessage(vse, VSE.CHANNEL, p);
				}

				if (vse.getConfig().getBoolean("settingOverrides.thirdPersonItems")) {
					final ByteArrayOutputStream baos = new ByteArrayOutputStream();

					baos.write(PacketDiscriminators.SETTING_OVERRIDE.ordinal());

					writeSetting(baos, "thirdPersonItems", true);

					final byte[] p = baos.toByteArray();
					sender.sendPluginMessage(vse, VSE.CHANNEL, p);
				}

				if (vse.getConfig().getBoolean("teleport.enabled"))
					sender.sendPluginMessage(vse, VSE.CHANNEL, new byte[]{(byte) PacketDiscriminators.TELEPORT.ordinal()});

				boolean crawlingEnabled = vse.getConfig().getBoolean("crawling.enabled");
				if (crawlingEnabled) {
					sender.sendPluginMessage(vse, VSE.CHANNEL, new byte[]{(byte) PacketDiscriminators.CRAWL.ordinal()});
				}

			} catch (IOException e) {
				e.printStackTrace();
			}
			break;
		case WORLDSCALE:
			ByteArrayInputStream a = new ByteArrayInputStream(data);
			DataInputStream b = new DataInputStream(a);
			try {
				vp.worldScale = b.readFloat();
			} catch (IOException e2) {
				e2.printStackTrace();
			}
			break;
		case HEIGHT:
			ByteArrayInputStream a1 = new ByteArrayInputStream(data);
			DataInputStream b1 = new DataInputStream(a1);
			try {
				vp.heightScale = b1.readFloat();
			} catch (IOException e1) {
				e1.printStackTrace();
			}
			break;
		case TELEPORT:
			if (!vse.getConfig().getBoolean("teleport.enabled"))
				break;

			ByteArrayInputStream in = new ByteArrayInputStream(data);
			DataInputStream d = new DataInputStream(in);
			try {
				float x = d.readFloat();
				float y = d.readFloat();
				float z = d.readFloat();
				// Use getHandle() reflection to access NMS ServerPlayer
				try {
					Object nmsEntity = sender.getClass().getMethod("getHandle").invoke(sender);
					ServerPlayer nms = (ServerPlayer) nmsEntity;
					nms.setPos(x, y, z);
				} catch (Exception ex) {
					vse.getLogger().warning("Failed to access NMS ServerPlayer for teleport: " + ex.getMessage());
				}
			} catch (IOException e) {
				e.printStackTrace();
			}

			break;
		case CLIMBING:			
			// Use getHandle() reflection to access NMS ServerPlayer
			try {
				Object nmsEntity = sender.getClass().getMethod("getHandle").invoke(sender);
				ServerPlayer nms = (ServerPlayer) nmsEntity;
			nms.fallDistance = 0;
			Reflector.setFieldValue(Reflector.aboveGroundTickCount, nms.connection, 0);
			} catch (Exception ex) {
				vse.getLogger().warning("Failed to access NMS ServerPlayer for climbing: " + ex.getMessage());
			}
			break;
		case CRAWL:
			boolean isCrawlingEnabled = vse.getConfig().getBoolean("crawling.enabled", true);

			if (!isCrawlingEnabled) {
				return; // Do nothing if crawling is disabled on the server
			}

			ByteArrayInputStream crawlByin = new ByteArrayInputStream(data);
			DataInputStream crawlDa = new DataInputStream(crawlByin);
			try {
				boolean isCrawling = crawlDa.readBoolean();
				// Use getHandle() to get the NMS player
				Object nmsPlayer = sender.getClass().getMethod("getHandle").invoke(sender);
				if (nmsPlayer instanceof ServerPlayer) {
					ServerPlayer serverPlayer = (ServerPlayer) nmsPlayer;
					if (isCrawling) {
						// Set the SWIMMING pose for crawling
						PoseOverrider.setPlayerPose(sender, Pose.SWIMMING);
					} else {
						// Only reset to STANDING if the current pose is SWIMMING
						if (serverPlayer.getPose() == Pose.SWIMMING) {
							PoseOverrider.setPlayerPose(sender, Pose.STANDING);
						}
					}

					// Notify the client of the pose change
					serverPlayer.connection.send(new ClientboundPlayerInfoUpdatePacket(
						ClientboundPlayerInfoUpdatePacket.Action.ADD_PLAYER, serverPlayer));
				}
			} catch (IOException | ReflectiveOperationException e) {
				vse.getLogger().warning("Failed to process crawl packet: " + e.getMessage());
			}
			break;
		case ACTIVEHAND:
			ByteArrayInputStream a2 = new ByteArrayInputStream(data);
			DataInputStream b2 = new DataInputStream(a2);
			try {
				vp.activeHand = b2.readByte();
				if (vp.isSeated()) vp.activeHand = 0;
			} catch (IOException e2) {
				e2.printStackTrace();
			}
			break;
		case IS_VR_ACTIVE:
			ByteArrayInputStream vrb = new ByteArrayInputStream(data);
			DataInputStream vrd = new DataInputStream(vrb);
			boolean vr;
			try {
				vr = vrd.readBoolean();
				if(vp.isVR()==vr) break;
				if (!vr) {
					vp.setVR(false);
				} else {
					vp.setVR(true);
				}
				vse.sendVRActiveUpdate(vp);
				vse.setPermissionsGroup(sender);
			} catch (IOException e) {
				e.printStackTrace();
			}
			break;
		case VR_PLAYER_STATE:
			VrPlayerState state = VrPlayerState.deserialize(data);
			if (state != null) {
				// Determine pose based on state
				if (state.isSeated) {
					PoseOverrider.setPlayerPose(sender, Pose.SITTING);
				} else if (state.hmd.position.y < 1.2) { // Infer crouching from HMD height
					PoseOverrider.setPlayerPose(sender, Pose.CROUCHING);
				} else {
					PoseOverrider.setPlayerPose(sender, Pose.STANDING);
				}
			}
			break;
		case NETWORK_VERSION:
			// Sent by client on join.
			break;
		case UBERPACKET:
			ByteArrayInputStream ubin = new ByteArrayInputStream(data);
			// ... existing code ...
		default:
			break;
		}
	}

	public void writeSetting(ByteArrayOutputStream output, String name, Object value) {
		try{
			if(value instanceof Boolean)
				writeSetting(output, name, (Boolean) value);
			else if(value instanceof Integer)
				writeSetting(output, name, (Integer) value);
			else if(value instanceof Double)
				writeSetting(output, name, (Double) value);
			else if(value instanceof String)
				writeSetting(output, name, (String) value);
		} catch (Exception e) {
			vse.getLogger().warning("Failed to write setting: " + e.getMessage());
		}
	}

	public static byte[] StringToPayload(PacketDiscriminators version, String input){
		ByteArrayOutputStream output = new ByteArrayOutputStream();

		output.write((byte)version.ordinal());
		if(!writeString(output, input)) {
			output.reset();
			return output.toByteArray();
		}

		return output.toByteArray();

	}

	public static boolean writeString(ByteArrayOutputStream output, String str) {
		byte[] bytes = str.getBytes(Charset.forName("UTF-8"));
		int len = bytes.length;
		try {
			if(!writeVarInt(output, len, 2))
				return false;
			output.write(bytes);
		} catch (IOException e) {
			return false;
		}

		return true;
	}

	public static int varIntByteCount(int toCount)
	{
		return (toCount & 0xFFFFFF80) == 0 ? 1 : ((toCount & 0xFFFFC000) == 0 ? 2 : ((toCount & 0xFFE00000) == 0 ? 3 : ((toCount & 0xF0000000) == 0 ? 4 : 5)));
	}

	public static boolean writeVarInt(ByteArrayOutputStream to, int toWrite, int maxSize)
	{
		if (varIntByteCount(toWrite) > maxSize) return false;
		while ((toWrite & -128) != 0)
		{
			to.write(toWrite & 127 | 128);
			toWrite >>>= 7;
		}

		to.write(toWrite);
		return true;
	}
}
