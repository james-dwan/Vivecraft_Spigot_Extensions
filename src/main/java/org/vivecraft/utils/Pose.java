package org.vivecraft.utils;

import org.vivecraft.utils.lwjgl.Quaternion;
import org.vivecraft.utils.lwjgl.Vector3f;

public class Pose {
    public Vector3f position;
    public Quaternion orientation;

    public Pose(Vector3f position, Quaternion orientation) {
        this.position = position;
        this.orientation = orientation;
    }
} 