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
package org.spongepowered.api.event.block.entity;

import net.kyori.adventure.text.Component;
import org.spongepowered.api.block.entity.Sign;
import org.spongepowered.api.data.value.ListValue;
import org.spongepowered.api.event.Cancellable;
import org.spongepowered.api.event.Event;

public interface ChangeSignEvent extends Event, Cancellable {

    /**
     * Gets the {@link Sign}.
     *
     * @return The sign
     */
    Sign sign();

    /**
     * Gets the original {@link ListValue} before event changes.
     *
     * @return The immutable SignData
     */
    ListValue.Immutable<Component> originalText();

    /**
     * Gets the {@link ListValue} to be applied to the {@link Sign} after event resolution.
     *
     * @return The SignData
     */
    ListValue.Mutable<Component> text();

    /**
     * Gets whether this event is for the front side or back side of the sign.
     *
     * @return true if this event fired for the front side.
     */
    boolean isFrontSide();

}
