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
package org.spongepowered.api.statistic;

import org.spongepowered.api.entity.living.player.server.ServerPlayer;
import org.spongepowered.api.scoreboard.criteria.Criterion;
import org.spongepowered.api.util.annotation.CatalogedBy;

/**
 * Represents some statistic in minecraft.
 *
 * To access them from {@link ServerPlayer players}, use {@link ServerPlayer#statistics()}
 */
@CatalogedBy(Statistics.class)
public interface Statistic<T> extends Criterion {

    /**
     * Gets the {@link StatisticCategory category} this statistic resides under.
     *
     * @return The category
     */
    StatisticCategory<T> category();

    /**
     * Gets the {@link T value} this statistic is an expression of.
     *
     * @return The value
     */
    T value();

    /**
     * Returns the {@link StatisticFormatter formatter} used to format the value of this
     * statistic.
     *
     * @return The formatter
     */
    StatisticFormatter formatter();
}
