/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client/).
 * Copyright (c) 2022 Meteor Development.
 */

package RewardedIvan.asteroidclient.events.entity.player;

import RewardedIvan.asteroidclient.events.Cancellable;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;

public class CobwebEntityCollisionEvent extends Cancellable {
    private static final CobwebEntityCollisionEvent INSTANCE = new CobwebEntityCollisionEvent();

    public BlockState state;
    public BlockPos blockPos;

    public static CobwebEntityCollisionEvent get(BlockState state, BlockPos blockPos) {
        INSTANCE.setCancelled(false);
        INSTANCE.state = state;
        INSTANCE.blockPos = blockPos;
        return INSTANCE;
    }
}
