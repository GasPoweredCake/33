/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package com.gaspoweredcake.client33.mixininterface;

import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;
import org.joml.Vector3d;

@SuppressWarnings("UnusedReturnValue")
public interface IVec3d {
    Vec3d client33$set(double x, double y, double z);

    default Vec3d client33$set(Vec3i vec) {
        return client33$set(vec.getX(), vec.getY(), vec.getZ());
    }

    default Vec3d client33$set(Vector3d vec) {
        return client33$set(vec.x, vec.y, vec.z);
    }

    default Vec3d client33$set(Vec3d pos) {
        return client33$set(pos.x, pos.y, pos.z);
    }

    Vec3d client33$setXZ(double x, double z);

    Vec3d client33$setY(double y);
}
