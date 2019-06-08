/*
 * This file is part of SpongeAPI, licensed under the MIT License (MIT).
 *
 * Copyright (c) SpongePowered <https://www.spongepowered.org>
 * Copyright (c) contributors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package org.spongepowered.api.world.volume.composite;

import org.spongepowered.api.Game;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.block.entity.BlockEntity;
import org.spongepowered.api.fluid.FluidState;
import org.spongepowered.api.world.volume.Volume;
import org.spongepowered.math.vector.Vector3i;

import java.util.Optional;

/**
 * A very primitive rudimentary volume that can be used by the {@link Game}
 * without impunity, but no guarantees on the provider type of what this
 * primitive volume is based on.
 */
public interface PrimitiveGameVolume extends Volume {

    default Optional<BlockEntity> getBlockEntity(Vector3i position) {
        return getBlockEntity(position.getX(), position.getY(), position.getZ());
    }

    Optional<BlockEntity> getBlockEntity(int x, int y, int z);

    default BlockState getBlock(Vector3i pos) {
        return getBlock(pos.getX(), pos.getY(), pos.getZ());
    }

    BlockState getBlock(int x, int y, int z);

    default FluidState getFluidState(Vector3i pos) {
        return getFluidState(pos.getX(), pos.getY(), pos.getZ());
    }

    FluidState getFluidState(int x, int y, int z);

    default int getMaxLightLevel() {
        return 15;
    }
}