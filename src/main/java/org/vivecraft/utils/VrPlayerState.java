package org.vivecraft.utils;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;

import org.vivecraft.VSE;

// This is a server-side representation of the client's VrPlayerState class.
public class VrPlayerState {
    public boolean isSeated;
    public Pose hmd;
    public Pose mainHand;
    public Pose offHand;
    public boolean isLeftHanded;
    public boolean isLeftHandedLegacy;

    public static VrPlayerState deserialize(byte[] data) {
        VrPlayerState state = new VrPlayerState();
        ByteArrayInputStream stream = new ByteArrayInputStream(data);
        DataInputStream dstream = new DataInputStream(stream);

        try {
            state.isSeated = dstream.readBoolean();
            state.hmd = new Pose(NetworkHelper.deserializeFVec3(dstream), NetworkHelper.deserializeVivecraftQuaternion(dstream));
            state.isLeftHanded = dstream.readBoolean();
            state.mainHand = new Pose(NetworkHelper.deserializeFVec3(dstream), NetworkHelper.deserializeVivecraftQuaternion(dstream));
            state.isLeftHandedLegacy = dstream.readBoolean();
            state.offHand = new Pose(NetworkHelper.deserializeFVec3(dstream), NetworkHelper.deserializeVivecraftQuaternion(dstream));
            
            // We are not yet reading the FBT data.

        } catch (IOException e) {
            System.err.println("Could not deserialize VRPlayerState packet!");
            e.printStackTrace();
            return null;
        }

        return state;
    }
} 