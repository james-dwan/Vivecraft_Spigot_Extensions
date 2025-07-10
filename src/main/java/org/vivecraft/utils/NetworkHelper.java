package org.vivecraft.utils;

import java.io.DataInputStream;
import java.io.IOException;

import org.vivecraft.utils.lwjgl.Quaternion;
import org.vivecraft.utils.lwjgl.Vector3f;

public class NetworkHelper {
    public static Vector3f deserializeFVec3(DataInputStream buffer) throws IOException {
        return new Vector3f(buffer.readFloat(), buffer.readFloat(), buffer.readFloat());
    }

    public static Quaternion deserializeVivecraftQuaternion(DataInputStream buffer) throws IOException {
        float w = buffer.readFloat();
        return new Quaternion(buffer.readFloat(), buffer.readFloat(), buffer.readFloat(), w);
    }
} 