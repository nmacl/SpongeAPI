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
package org.spongepowered.api.world.generation.feature;

import org.spongepowered.api.ResourceKey;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.registry.DefaultedRegistryReference;
import org.spongepowered.api.registry.Registry;
import org.spongepowered.api.registry.RegistryKey;
import org.spongepowered.api.registry.RegistryScope;
import org.spongepowered.api.registry.RegistryScopes;
import org.spongepowered.api.registry.RegistryTypes;

/**
 * <!-- This file is automatically generated. Any manual changes will be overwritten. -->
 */
@SuppressWarnings("unused")
@RegistryScopes(scopes = RegistryScope.ENGINE)
public final class PlacementModifierTypes {

    public static final DefaultedRegistryReference<PlacementModifierType> BIOME = PlacementModifierTypes.key(ResourceKey.minecraft("biome"));

    public static final DefaultedRegistryReference<PlacementModifierType> BLOCK_PREDICATE_FILTER = PlacementModifierTypes.key(ResourceKey.minecraft("block_predicate_filter"));

    public static final DefaultedRegistryReference<PlacementModifierType> CARVING_MASK = PlacementModifierTypes.key(ResourceKey.minecraft("carving_mask"));

    public static final DefaultedRegistryReference<PlacementModifierType> COUNT = PlacementModifierTypes.key(ResourceKey.minecraft("count"));

    public static final DefaultedRegistryReference<PlacementModifierType> COUNT_ON_EVERY_LAYER = PlacementModifierTypes.key(ResourceKey.minecraft("count_on_every_layer"));

    public static final DefaultedRegistryReference<PlacementModifierType> ENVIRONMENT_SCAN = PlacementModifierTypes.key(ResourceKey.minecraft("environment_scan"));

    public static final DefaultedRegistryReference<PlacementModifierType> FIXED_PLACEMENT = PlacementModifierTypes.key(ResourceKey.minecraft("fixed_placement"));

    public static final DefaultedRegistryReference<PlacementModifierType> HEIGHT_RANGE = PlacementModifierTypes.key(ResourceKey.minecraft("height_range"));

    public static final DefaultedRegistryReference<PlacementModifierType> HEIGHTMAP = PlacementModifierTypes.key(ResourceKey.minecraft("heightmap"));

    public static final DefaultedRegistryReference<PlacementModifierType> IN_SQUARE = PlacementModifierTypes.key(ResourceKey.minecraft("in_square"));

    public static final DefaultedRegistryReference<PlacementModifierType> NOISE_BASED_COUNT = PlacementModifierTypes.key(ResourceKey.minecraft("noise_based_count"));

    public static final DefaultedRegistryReference<PlacementModifierType> NOISE_THRESHOLD_COUNT = PlacementModifierTypes.key(ResourceKey.minecraft("noise_threshold_count"));

    public static final DefaultedRegistryReference<PlacementModifierType> RANDOM_OFFSET = PlacementModifierTypes.key(ResourceKey.minecraft("random_offset"));

    public static final DefaultedRegistryReference<PlacementModifierType> RARITY_FILTER = PlacementModifierTypes.key(ResourceKey.minecraft("rarity_filter"));

    public static final DefaultedRegistryReference<PlacementModifierType> SURFACE_RELATIVE_THRESHOLD_FILTER = PlacementModifierTypes.key(ResourceKey.minecraft("surface_relative_threshold_filter"));

    public static final DefaultedRegistryReference<PlacementModifierType> SURFACE_WATER_DEPTH_FILTER = PlacementModifierTypes.key(ResourceKey.minecraft("surface_water_depth_filter"));

    private PlacementModifierTypes() {
    }

    public static Registry<PlacementModifierType> registry() {
        return Sponge.server().registry(RegistryTypes.PLACEMENT_MODIFIER);
    }

    private static DefaultedRegistryReference<PlacementModifierType> key(final ResourceKey location) {
        return RegistryKey.of(RegistryTypes.PLACEMENT_MODIFIER, location).asDefaultedReference(Sponge::server);
    }
}
