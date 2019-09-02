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
package org.spongepowered.api.command.parameter;

import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.api.CatalogType;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.Command;
import org.spongepowered.api.command.CommandExecutor;
import org.spongepowered.api.command.exception.ArgumentParseException;
import org.spongepowered.api.command.parameter.managed.ValueCompleter;
import org.spongepowered.api.command.parameter.managed.ValueParameter;
import org.spongepowered.api.command.parameter.managed.ValueParser;
import org.spongepowered.api.command.parameter.managed.ValueUsage;
import org.spongepowered.api.command.parameter.managed.standard.CatalogedValueParameters;
import org.spongepowered.api.command.parameter.managed.standard.VariableValueParameters;
import org.spongepowered.api.command.source.CommandSource;
import org.spongepowered.api.data.persistence.DataContainer;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.network.RemoteConnection;
import org.spongepowered.api.plugin.PluginContainer;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.util.Color;
import org.spongepowered.api.util.ResettableBuilder;
import org.spongepowered.api.world.Dimension;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.storage.WorldProperties;
import org.spongepowered.math.vector.Vector3d;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.InetAddress;
import java.net.URL;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

/**
 * Defines how an element of a command argument string should be parsed.
 *
 * <p>Parameters that parse input and return an object into the
 * {@link CommandContext} should be of type {@link Parameter.Value} in
 * order to maximize type safety. However, there are also other important
 * ways that parameters can be used:</p>
 *
 * <ul>
 *     <li>{@link #firstOf(Parameter)} allows for multiple parameters that
 *     do not have the same return type to attempt to parse an input
 *     successfully.</li>
 *     <li>{@link #seq(Parameter)} allows for the grouping of multiple
 *     parameters that will be executed one after another.</li>
 *     <li>{@link Subcommand}s can be placed anywhere in a parameter
 *     chain where a {@link Parameter} can be added, if successfully parsed,
 *     any containing {@link Command} would take precedence and its
 *     {@link Command#process(Cause, String)} method will be called instead
 *     of any parent.</li>
 * </ul>
 *
 * <p>{@link Parameter}s are intended for use with {@link org.spongepowered.api.command.Command.Builder}s.
 * </p>
 */
public interface Parameter {

    /**
     * Creates a {@link Parameter.Key} for storing values against.
     *
     * @param key The string key
     * @param valueClass The type of value that this key represents
     * @param <T> The type
     * @return The {@link Key}
     */
    static <T> Key<T> key(String key, Class<T> valueClass) {
        return Sponge.getRegistry().createBuilder(Key.Builder.class).build(key, valueClass);
    }

    /**
     * Gets a builder that builds a {@link Parameter.Value}.
     *
     * @return The {@link Value.Builder}
     */
    @SuppressWarnings("unchecked")
    static <T> Value.Builder<T> builder(Class<T> valueClass) {
        return ((Value.Builder<T>) Sponge.getRegistry().createBuilder(Value.Builder.class)).setValueClass(valueClass);
    }

    /**
     * Gets a builder that builds a {@link Parameter.Value}.
     *
     * @return The {@link Value.Builder}
     */
    @SuppressWarnings("unchecked")
    static <T> Value.Builder<T> builder(Class<T> valueClass, ValueParameter<T> parameter) {
        return ((Value.Builder<T>) Sponge.getRegistry().createBuilder(Value.Builder.class)).setValueClass(valueClass).parser(parameter);
    }

    /**
     * Gets a {@link Parameter} that represents a subcommand.
     *
     * <p>If a {@link Subcommand} alias in a parameter chain
     * is successfully matched, then element parsing will continue
     * with the parameters supplied to {@link Command}. It is
     * implementation dependent as to what happens if a subcommand
     * fails to parse.</p>
     *
     * @param subcommand The {@link Command} to execute
     * @param alias The first alias of the subcommand
     * @param aliases Subsequent aliases, if any
     * @return The {@link Subcommand} for use in a {@link Parameter} chain
     */
    static Subcommand subcommand(Command subcommand, String alias, String... aliases) {
        Subcommand.Builder builder = Sponge.getRegistry()
                .createBuilder(Subcommand.Builder.class)
                .setSubcommand(subcommand)
                .alias(alias);
        for (String a : aliases) {
            builder.alias(a);
        }

        return builder.build();
    }

    /**
     * Returns a {@link Parameter.FirstOfBuilder} that allows plugins to attempt
     * to parse an argument using the supplied parameters in order. Once a
     * parameter has parsed the argument successfully, no more parameters
     * supplied here will be attempted.
     *
     * @param parameter The first {@link Parameter}
     * @return The {@link Parameter.FirstOfBuilder} to continue chaining
     */
    static Parameter.FirstOfBuilder firstOf(Parameter parameter) {
        return Sponge.getRegistry().createBuilder(FirstOfBuilder.class).or(parameter);
    }

    /**
     * Returns a {@link Parameter} that attempts to parse an argument using the
     * supplied parameters in order. Once a parameter has parsed the argument
     * successfully, no more parameters supplied here will be attempted.
     *
     * @param first The first {@link Parameter} that should be used for parsing
     * @param second The second {@link Parameter} that should be used for
     *               parsing, should the first {@link Parameter} fail to do so
     * @param parameters The remaining {@link Parameter}s
     * @return The {@link Parameter}
     */
    static Parameter firstOf(Parameter first, Parameter second, Parameter... parameters) {
        return Sponge.getRegistry().createBuilder(FirstOfBuilder.class).or(first).or(second).orFirstOf(parameters).build();
    }

    /**
     * Returns a {@link Parameter} that attempts to parse an argument using the
     * supplied parameters in order. Once a parameter has parsed the argument
     * successfully, no more parameters supplied here will be attempted.
     *
     * @param parameters The {@link Parameter}s
     * @return The {@link Parameter}
     */
    static Parameter firstOf(Iterable<Parameter> parameters) {
        return Sponge.getRegistry().createBuilder(FirstOfBuilder.class).orFirstOf(parameters).build();
    }

    /**
     * Returns a {@link Parameter.SequenceBuilder} that parses arguments using
     * the supplied parameters in order.
     *
     * @param parameter The first {@link Parameter} in the sequence
     * @return The {@link Parameter.SequenceBuilder}, to continue building the
     *         chain
     */
    static Parameter.SequenceBuilder seq(Parameter parameter) {
        return Sponge.getRegistry().createBuilder(SequenceBuilder.class).then(parameter);
    }

    /**
     * Returns a {@link Parameter} that parses arguments using the supplied
     * parameters in order.
     *
     * @param first The first {@link Parameter} that should be used for parsing
     * @param second The second {@link Parameter} that should be used for
     *               parsing
     * @param parameters The subsequent {@link Parameter}s to parse
     * @return The {@link Parameter}
     */
    static Parameter seq(Parameter first, Parameter second, Parameter... parameters) {
        return Sponge.getRegistry().createBuilder(SequenceBuilder.class).then(first).then(second).then(parameters).build();
    }

    /**
     * Returns a {@link Parameter} that parses arguments using the supplied
     * parameters in order.
     *
     * @param parameters The {@link Parameter}s
     * @return The {@link Parameter}
     */
    static Parameter seq(Iterable<Parameter> parameters) {
        return Sponge.getRegistry().createBuilder(SequenceBuilder.class).then(parameters).build();
    }

    // Convenience methods for getting the common parameter types - all in once place.
    // Modifiers (for the most part) are on the Parameter.Builder itself.

    /**
     * Creates a builder that has the {@link ValueParameter} set to
     * {@link CatalogedValueParameters#BIG_DECIMAL}.
     *
     * @return A {@link Parameter.Value.Builder}
     */
    static Parameter.Value.Builder<BigDecimal> bigDecimal() {
        return Parameter.builder(BigDecimal.class, CatalogedValueParameters.BIG_DECIMAL);
    }

    /**
     * Creates a builder that has the {@link ValueParameter} set to
     * {@link CatalogedValueParameters#BIG_INTEGER}.
     *
     * @return A {@link Parameter.Value.Builder}
     */
    static Parameter.Value.Builder<BigInteger> bigInteger() {
        return Parameter.builder(BigInteger.class, CatalogedValueParameters.BIG_INTEGER);
    }

    /**
     * Creates a builder that has the {@link ValueParameter} set to
     * {@link CatalogedValueParameters#BOOLEAN}.
     *
     * @return A {@link Parameter.Value.Builder}
     */
    static Parameter.Value.Builder<Boolean> bool() {
        return Parameter.builder(Boolean.class, CatalogedValueParameters.BOOLEAN);
    }

    /**
     * Creates a builder that has the {@link ValueParameter} set to
     * {@link CatalogedValueParameters#COLOR}.
     *
     * @return A {@link Parameter.Value.Builder}
     */
    static Parameter.Value.Builder<Color> color() {
        return Parameter.builder(Color.class, CatalogedValueParameters.COLOR);
    }

    /**
     * Creates a builder that has the {@link ValueParameter} set to
     * {@link CatalogedValueParameters#DATA_CONTAINER}.
     *
     * @return A {@link Parameter.Value.Builder}
     */
    static Parameter.Value.Builder<DataContainer> dataContainer() {
        return Parameter.builder(DataContainer.class, CatalogedValueParameters.DATA_CONTAINER);
    }

    /**
     * Creates a builder that has the {@link ValueParameter} set to
     * {@link CatalogedValueParameters#DATE_TIME}.
     *
     * @return A {@link Parameter.Value.Builder}
     */
    static Parameter.Value.Builder<LocalDateTime> dateTime() {
        return Parameter.builder(LocalDateTime.class, CatalogedValueParameters.DATE_TIME);
    }

    /**
     * Creates a builder that has the {@link ValueParameter} set to
     * {@link CatalogedValueParameters#DATE_TIME}, returning the current
     * {@link LocalDateTime}.
     *
     * @return A {@link Parameter.Value.Builder}
     */
    static Parameter.Value.Builder<LocalDateTime> dateTimeOrNow() {
        return dateTime().orDefault(cause -> LocalDateTime.now());
    }

    /**
     * Creates a builder that has the {@link ValueParameter} set to
     * {@link CatalogedValueParameters#DIMENSION}.
     *
     * @return A {@link Parameter.Value.Builder}
     */
    static Parameter.Value.Builder<Dimension> dimension() {
        return Parameter.builder(Dimension.class, CatalogedValueParameters.DIMENSION);
    }

    /**
     * Creates a builder that has the {@link ValueParameter} set to
     * {@link CatalogedValueParameters#DURATION}.
     *
     * @return A {@link Parameter.Value.Builder}
     */
    static Parameter.Value.Builder<Duration> duration() {
        return Parameter.builder(Duration.class, CatalogedValueParameters.DURATION);
    }

    /**
     * Creates a builder that has the {@link ValueParameter} set to
     * {@link CatalogedValueParameters#DOUBLE}.
     *
     * @return A {@link Parameter.Value.Builder}
     */
    static Parameter.Value.Builder<Double> doubleNumber() {
        return Parameter.builder(Double.class, CatalogedValueParameters.DOUBLE);
    }

    /**
     * Creates a builder that has the {@link ValueParameter} set to
     * {@link CatalogedValueParameters#ENTITY}.
     *
     * @return A {@link Parameter.Value.Builder}
     */
    static Parameter.Value.Builder<Entity> entity() {
        return Parameter.builder(Entity.class, CatalogedValueParameters.ENTITY);
    }

    /**
     * Creates a builder that has the {@link ValueParameter} set to
     * {@link CatalogedValueParameters#ENTITY}, using the {@link Cause#root()}
     * as the default if they are an {@link Entity}
     *
     * @return A {@link Parameter.Value.Builder}
     */
    static Parameter.Value.Builder<Entity> entityOrSource() {
        return entity().orDefault(cause -> cause.root() instanceof Entity ? (Entity) cause.root() : null);
    }

    /**
     * Creates a builder that has the {@link ValueParameter} set to
     * {@link CatalogedValueParameters#ENTITY}
     *
     * @return A {@link Parameter.Value.Builder}
     */
    static Parameter.Value.Builder<Entity> entityOrTarget() {
        return entity().parser(CatalogedValueParameters.TARGET_ENTITY);
    }

    /**
     * Creates a builder that has the {@link ValueParameter} set to
     * {@link CatalogedValueParameters#TEXT_FORMATTING_CODE}.
     *
     * @return A {@link Parameter.Value.Builder}
     */
    static Parameter.Value.Builder<Text> formattingCodeText() {
        return Parameter.builder(Text.class, CatalogedValueParameters.TEXT_FORMATTING_CODE);
    }

    /**
     * Creates a builder that has the {@link ValueParameter} set to
     * {@link CatalogedValueParameters#TEXT_FORMATTING_CODE_ALL}.
     *
     * @return A {@link Parameter.Value.Builder}
     */
    static Parameter.Value.Builder<Text> formattingCodeTextOfRemainingElements() {
        return Parameter.builder(Text.class, CatalogedValueParameters.TEXT_FORMATTING_CODE_ALL);
    }

    /**
     * Creates a builder that has the {@link ValueParameter} set to
     * {@link CatalogedValueParameters#INTEGER}.
     *
     * @return A {@link Parameter.Value.Builder}
     */
    static Parameter.Value.Builder<Integer> integerNumber() {
        return Parameter.builder(Integer.class, CatalogedValueParameters.INTEGER);
    }

    /**
     * Creates a builder that has the {@link ValueParameter} set to
     * {@link CatalogedValueParameters#IP}.
     *
     * @return A {@link Parameter.Value.Builder}
     */
    static Parameter.Value.Builder<InetAddress> ip() {
        return Parameter.builder(InetAddress.class, CatalogedValueParameters.IP);
    }

    /**
     * Creates a builder that has the {@link ValueParameter} set to
     * {@link CatalogedValueParameters#IP}, defaulting to the source's
     * {@link InetAddress}
     *
     * @return A {@link Parameter.Value.Builder}
     */
    static Parameter.Value.Builder<InetAddress> ipOrSource() {
        return ip().orDefault(cause -> cause.root() instanceof RemoteConnection ? ((RemoteConnection) cause.root()).getAddress().getAddress() : null);
    }

    /**
     * Creates a builder that has the {@link ValueParameter} set to
     * {@link CatalogedValueParameters#TEXT_JSON}.
     *
     * @return A {@link Parameter.Value.Builder}
     */
    static Parameter.Value.Builder<Text> jsonText() {
        return Parameter.builder(Text.class, CatalogedValueParameters.TEXT_JSON);
    }

    /**
     * Creates a builder that has the {@link ValueParameter} set to
     * {@link CatalogedValueParameters#TEXT_JSON_ALL}.
     *
     * @return A {@link Parameter.Value.Builder}
     */
    static Parameter.Value.Builder<Text> jsonTextOfRemainingElements() {
        return Parameter.builder(Text.class, CatalogedValueParameters.TEXT_JSON_ALL);
    }

    /**
     * Creates a builder that has the {@link ValueParameter} set to
     * {@link CatalogedValueParameters#LOCATION}.
     *
     * @return A {@link Parameter.Value.Builder}
     */
    static Parameter.Value.Builder<Location> location() {
        return Parameter.builder(Location.class, CatalogedValueParameters.LOCATION);
    }

    /**
     * Creates a builder that has the {@link ValueParameter} set to
     * {@link CatalogedValueParameters#LONG}.
     *
     * @return A {@link Parameter.Value.Builder}
     */
    static Parameter.Value.Builder<Long> longNumber() {
        return Parameter.builder(Long.class, CatalogedValueParameters.LONG);
    }

    /**
     * Creates a builder that has the {@link ValueParameter} set to
     * {@link CatalogedValueParameters#PLAYER}.
     *
     * @return A {@link Parameter.Value.Builder}
     */
    static Parameter.Value.Builder<Player> player() {
        return Parameter.builder(Player.class, CatalogedValueParameters.PLAYER);
    }

    /**
     * Creates a builder that has the {@link ValueParameter} set to
     * {@link CatalogedValueParameters#PLAYER}, defaulting to the
     * {@link Cause#root()} if it is a {@link Player}.
     *
     * @return A {@link Parameter.Value.Builder}
     */
    static Parameter.Value.Builder<Player> playerOrSource() {
        return player().orDefault(cause -> cause.root() instanceof Player ? (Player) cause.root() : null);
    }

    /**
     * Creates a builder that has the {@link ValueParameter} set to
     * {@link CatalogedValueParameters#PLAYER}, else the target
     *
     * @return A {@link Parameter.Value.Builder}
     */
    static Parameter.Value.Builder<Player> playerOrTarget() {
        return player().parser(CatalogedValueParameters.TARGET_PLAYER);
    }

    /**
     * Creates a builder that has the {@link ValueParameter} set to
     * {@link CatalogedValueParameters#PLUGIN}.
     *
     * @return A {@link Parameter.Value.Builder}
     */
    static Parameter.Value.Builder<PluginContainer> plugin() {
        return Parameter.builder(PluginContainer.class, CatalogedValueParameters.PLUGIN);
    }

    /**
     * Creates a builder that has the {@link ValueParameter} set to
     * {@link CatalogedValueParameters#REMAINING_JOINED_STRINGS}.
     *
     * @return A {@link Parameter.Value.Builder}
     */
    static Parameter.Value.Builder<String> remainingJoinedStrings() {
        return Parameter.builder(String.class, CatalogedValueParameters.REMAINING_JOINED_STRINGS);
    }

    /**
     * Creates a builder that has the {@link ValueParameter} set to
     * {@link CatalogedValueParameters#REMAINING_RAW_JOINED_STRINGS}.
     *
     * @return A {@link Parameter.Value.Builder}
     */
    static Parameter.Value.Builder<String> remainingRawJoinedStrings() {
        return Parameter.builder(String.class, CatalogedValueParameters.REMAINING_RAW_JOINED_STRINGS);
    }

    /**
     * Creates a builder that has the {@link ValueParameter} set to
     * {@link CatalogedValueParameters#STRING}.
     *
     * @return A {@link Parameter.Value.Builder}
     */
    static Parameter.Value.Builder<String> string() {
        return Parameter.builder(String.class, CatalogedValueParameters.STRING);
    }

    /**
     * Creates a builder that has the {@link ValueParameter} set to
     * {@link CatalogedValueParameters#URL}.
     *
     * @return A {@link Parameter.Value.Builder}
     */
    static Parameter.Value.Builder<URL> url() {
        return Parameter.builder(URL.class, CatalogedValueParameters.URL);
    }

    /**
     * Creates a builder that has the {@link ValueParameter} set to
     * {@link CatalogedValueParameters#USER}.
     *
     * @return A {@link Parameter.Value.Builder}
     */
    static Parameter.Value.Builder<User> user() {
        return Parameter.builder(User.class, CatalogedValueParameters.USER);
    }

    /**
     * Creates a builder that has the {@link ValueParameter} set to
     * {@link CatalogedValueParameters#USER}, reutrning the source
     * if necessary.
     *
     * @return A {@link Parameter.Value.Builder}
     */
    static Parameter.Value.Builder<User> userOrSource() {
        return user().orDefault(cause -> cause.root() instanceof User ? (User) cause.root() : null);
    }

    /**
     * Creates a builder that has the {@link ValueParameter} set to
     * {@link CatalogedValueParameters#UUID}.
     *
     * @return A {@link Parameter.Value.Builder}
     */
    static Parameter.Value.Builder<UUID> uuid() {
        return Parameter.builder(UUID.class, CatalogedValueParameters.UUID);
    }

    /**
     * Creates a builder that has the {@link ValueParameter} set to
     * {@link CatalogedValueParameters#VECTOR3D}.
     *
     * @return A {@link Parameter.Value.Builder}
     */
    static Parameter.Value.Builder<Vector3d> vector3d() {
        return Parameter.builder(Vector3d.class, CatalogedValueParameters.VECTOR3D);
    }

    /**
     * Creates a builder that has the {@link ValueParameter} set to
     * {@link CatalogedValueParameters#WORLD_PROPERTIES}.
     *
     * @return A {@link Parameter.Value.Builder}
     */
    static Parameter.Value.Builder<WorldProperties> worldProperties() {
        return Parameter.builder(WorldProperties.class, CatalogedValueParameters.WORLD_PROPERTIES);
    }

    /**
     * Creates a builder that has the {@link ValueParameter} that allows you to
     * choose from cataloged types.
     *
     * <p>The parameter will be set to allow users to omit the "minecraft:" or
     * "sponge:" namespace.</p>
     *
     * @param type The {@link CatalogType} class to check for choices
     * @param <T> The type of {@link CatalogType}
     * @return A {@link Parameter.Value.Builder}
     */
    static <T extends CatalogType> Parameter.Value.Builder<T> catalogedElement(Class<T> type) {
        return Parameter.builder(type, VariableValueParameters.catalogedElementParameterBuilder().setCatalogedType(type)
                .prefix("minecraft")
                .prefix("sponge")
                .build());
    }

    /**
     * Creates a builder that has a {@link ValueParameter} that allows you to
     * specify a set of choices that must be chosen from
     *
     * @param choices The choices
     * @return A {@link Parameter.Value.Builder}
     */
    static Parameter.Value.Builder<String> choices(String... choices) {
        VariableValueParameters.StaticChoicesBuilder<String> builder = VariableValueParameters
                .staticChoicesBuilder()
                .setReturnType(String.class)
                .setShowInUsage(true);
        for (String choice : choices) {
            builder.choice(choice, choice);
        }

        return Parameter.builder(String.class, builder.build());
    }

    /**
     * Creates a builder that has a {@link ValueParameter} that allows you to
     * specify a set of choices that must be chosen from. These choices map to
     * objects that will get put in the {@link CommandContext} when a choice is
     * selected.
     *
     * @param choices The choices
     * @param returnType The type of the values returned by the parameter
     * @return A {@link Parameter.Value.Builder}
     */
    static <T> Parameter.Value.Builder<T> choices(Map<String, ? extends T> choices, Class<T> returnType) {
        return Parameter.builder(returnType, VariableValueParameters.staticChoicesBuilder()
                .setReturnType(returnType)
                .choices(choices)
                .setShowInUsage(true).build());
    }

    /**
     * Creates a builder that has a {@link ValueParameter} that allows you to
     * specify a set of choices that must be chosen from. These choices map to
     * objects that will get put in the {@link CommandContext} when a choice is
     * selected, through the use of the {@code valueFunction}.
     *
     * @param choices The choices
     * @param valueFunction The function that returns an object to put in the
     *      {@link CommandContext} based on the supplied choice
     * @return A {@link Parameter.Value.Builder}
     */
    static <T> Parameter.Value.Builder<T> choices(Supplier<Iterable<String>> choices, Function<String, ? extends T> valueFunction, Class<T> returnType) {
        return Parameter.builder(returnType,
                VariableValueParameters
                        .dynamicChoicesBuilder()
                        .setShowInUsage(true)
                        .setChoices(choices)
                        .setReturnType(returnType)
                        .setResults(valueFunction).build());
    }

    /**
     * Creates a builder that has a {@link ValueParameter} that requires an
     * argument that matches the name of a supplied {@link Enum} type
     *
     * @param enumClass The {@link Enum} class type
     * @param <T> The type of {@link Enum}
     * @return A {@link Parameter.Value.Builder}
     */
    static <T extends Enum<T>> Parameter.Value.Builder<T> enumValue(Class<T> enumClass) {
        return Parameter.builder(enumClass, VariableValueParameters.enumBuilder().setEnumClass(enumClass).build());
    }

    /**
     * Creates a builder that has a {@link ValueParameter} that requires an
     * argument to be a literal specified
     *
     * @param returnedValue The object to put in the {@link CommandContext} if
     *      the literal matches
     * @param literal The literal to match
     * @return A {@link Parameter.Value.Builder}
     */
    static <T> Parameter.Value.Builder<T> literal(T returnedValue, String... literal) {
        Iterable<String> iterable = Arrays.asList(literal);
        return literal(returnedValue, () -> iterable);
    }

    /**
     * Creates a builder that has a {@link ValueParameter} that requires an
     * argument to be a literal specified
     *
     * @param returnedValue The object to put in the {@link CommandContext} if
     *      the literal matches
     * @param literalSupplier A function that provies the literal to match at
     *      invocation
     * @return A {@link Parameter.Value.Builder}
     */
    @SuppressWarnings("unchecked")
    static <T> Parameter.Value.Builder<T> literal(T returnedValue, Supplier<Iterable<String>> literalSupplier) {
        return Parameter.builder((Class<T>) returnedValue.getClass(),
                VariableValueParameters.literalBuilder().setReturnValue(returnedValue).setLiteral(literalSupplier).build());
    }

    /**
     * Parses the next element(s) in the {@link CommandContext}
     *
     * @param reader The {@link ArgumentReader.Mutable} containing the strings
     *               that need to be parsed
     * @param context The {@link CommandContext.Builder} that contains the
     *                current state of the execution
     * @throws ArgumentParseException thrown if the parameter could not be
     *      parsed
     */
    void parse(ArgumentReader.Mutable reader, CommandContext.Builder context) throws ArgumentParseException;

    /**
     * Returns potential completions of the current tokenized argument.
     *
     * @param reader The {@link ArgumentReader} containing the strings that need
     *               to be parsed
     * @param context The {@link CommandContext} that contains the
     *                current state of the execution.
     * @return The potential completions.
     * @throws ArgumentParseException thrown if the parameter could not be
     *      parsed
     */
    List<String> complete(ArgumentReader.Immutable reader, CommandContext context) throws ArgumentParseException;

    /**
     * Gets the usage of this parameter.
     *
     * @param cause The {@link Cause} that requested the usage
     * @return The usage
     */
    Text getUsage(Cause cause);

    /**
     * A {@link Key}
     *
     * @param <T> The type.
     */
    interface Key<T> {

        /**
         * Gets the string key associated with this parameter.
         *
         * @return The key.
         */
        String key();

        /**
         * Gets the {@link Class} of the type of object that this parameter
         * should return from parsing.
         *
         * @return The {@link Class}
         */
        Class<T> getValueClass();

        /**
         * A "builder" that allows for keys to be built.
         */
        interface Builder extends ResettableBuilder<Key<?>, Builder> {

            /**
             * Creates a key with the provided key and value class it
             * represents.
             *
             * @param key The key
             * @param valueClass The {@link Class} that represents the
             *                   type of value it stores
             * @param <T> The type of the value represented by the key
             * @return The built {@link Key}
             */
            <T> Key<T> build(String key, Class<T> valueClass);
        }

    }

    /**
     * Represents a {@link Parameter} that attempts to parse an argument to
     * obtain a value of type {@code T}.
     *
     * <p>This type of {@link Parameter} will attempt to parse an input
     * using the {@link ValueParser}s in the order that they are returned in
     * {@link #getParsers()}. If a {@link ValueParser} fails to parse an
     * argument, the next in the list will be tried, if the final
     * {@link ValueParser} cannot parse the argument, this element will
     * throw a {@link ArgumentParseException}.</p>
     *
     * @param <T> The type of value returned from the {@link ValueParser}s
     */
    interface Value<T> extends Parameter {

        /**
         * The key that a parameter result is stored under.
         *
         * @return The key.
         */
        Key<T> getKey();

        /**
         * The {@link ValueParser}s to use when parsing an argument. They will be
         * tried in this order.
         *
         * <p>There must always be at least one {@link ValueParser}. If this element
         * is optional and has a default result, it will be the last element in the
         * returned {@link Collection}.</p>
         *
         * @return The parameters.
         */
        Collection<ValueParser<? extends T>> getParsers();

        /**
         * Gets the {@link ValueCompleter} associated with this {@link Value}.
         *
         * @return The {@link ValueCompleter}.
         */
        ValueCompleter getCompleter();

        /**
         * Gets a {@link Predicate} that indicates whether a given {@link Cause}
         * should parse this {@link Parameter}.
         *
         * @return the predicate
         */
        Predicate<Cause> getRequirement();

        /**
         * Gets whether this parameter is optional.
         *
         * @return true if optional, else false.
         */
        boolean isOptional();

        /**
         * Builds a {@link Parameter} from constituent components.
         */
        interface Builder<T> extends ResettableBuilder<Value<T>, Builder<T>> {

            /**
             * The key that the parameter will place parsed values into.
             *
             * <p>This is a mandatory element.</p>
             *
             * @param key The key.
             * @return This builder, for chaining
             */
            Builder<T> setKey(String key);

            /**
             * The key that the parameter will place parsed values into.
             *
             * <p>This is a mandatory element.</p>
             *
             * @param key The key.
             * @return This builder, for chaining
             */
            Builder<T> setKey(Parameter.Key<T> key);

            /**
             * Sets the type of object that this {@link Parameter.Value} will
             * return.
             *
             * @param valueClass The {@link Class}
             * @return This builder, for chaining.
             * @throws IllegalStateException if the class is attempted to be set
             *     more than once.
             */
            Builder<T> setValueClass(Class<T> valueClass) throws IllegalStateException;

            /**
             * The {@link ValueParser} that will extract the value(s) from the
             * parameters. If this is a {@link ValueParameter}, the object's
             * complete and usage methods will be used for completion and usage
             * unless this builder's {@link #setSuggestions(ValueCompleter)}} and
             * {@link #setUsage(ValueUsage)} methods are specified.
             *
             * @param parser The {@link ValueParameter} to use
             * @return This builder, for chaining
             */
            Builder<T> parser(ValueParser<? extends T> parser);

            /**
             * Provides a function that provides tab completions
             *
             * <p>Optional. If this is <code>null</code> (or never set),
             * completions will either be done via the supplied
             * {@link #parser(ValueParser)} or will just return an empty
             * list. If this is supplied, no modifiers will run on completion.</p>
             *
             * @param completer The {@link ValueCompleter}
             * @return This builder, for chaining
             */
            Builder<T> setSuggestions(@Nullable ValueCompleter completer);

            /**
             * Sets the usage. The {@link Function} accepts the parameter key
             * and the calling {@link Cause}.
             *
             * <p>Optional. If this is <code>null</code> (or never set),
             * the usage string will either be provided via the supplied
             * {@link #parser(ValueParser)} or will just return
             * the parameter's key. If this is supplied, no modifiers will run on
             * usage.</p>
             *
             * @param usage The function
             * @return This builder, for chaining
             */
            Builder<T> setUsage(@Nullable ValueUsage usage);

            /**
             * Sets the permission that the executing {@link CommandSource} is
             * required to have in order for this parameter to be parsed.
             *
             * <p>If the source does not have this permission, this parameter
             * will simply be skipped. Consider combining this with
             * {@link #optional()} so that those with permission can also skip
             * this parameter.</p>
             *
             * @param permission The permission to check for, or {@code null} for
             *                   no check.
             * @return This builder, for chaining
             */
            Builder<T> setRequiredPermission(@Nullable String permission);

            /**
             * Sets a function that determines what is required of the provided
             * {@link Cause} before this parameter attempts to parse.
             *
             * <p>If this is set to {@code null}, this parameter will always
             * attempt to parse, subject to other modifiers.</p>
             *
             * <p><strong>Note:</strong> this will overwrite any requirements set
             * using {@link #setRequiredPermission(String)}}.</p>
             *
             * @param executionRequirements A function that sets the
             * @return This builder, for chaining
             */
            Builder<T> setRequirements(@Nullable Predicate<Cause> executionRequirements);

            /**
             * If set, this parameter will repeat until the argument string has
             * been parsed.
             *
             * <p>For example, if you have the argument string,</p>
             *
             * <pre>
             *     1 2 3 4
             * </pre>
             *
             * <p>and you use {@link CatalogedValueParameters#INTEGER} without
             * setting this method, this parameter will parse the first element,
             * 1, and the remaining elements will be left for the next parameter
             * in the chain. If you call this method, the resulting
             * {@link Parameter.Value} will continuously parse the argument string
             * until either:</p>
             * <ul>
             *     <li>The entire argument string has been parsed, in which case
             *     the parsing is considered complete and command execution will
             *     continue</li>
             *     <li>A part of the argument string could not be parsed, in which
             *     case an exception will be raised. If this element is also marked
             *     as {@link #optional()} or with one of the
             *     {@link #orDefault(Object)} methods, then parsing will continue
             *     as if nothing has been parsed.</li>
             * </ul>
             *
             * <p>Unless marked as optional, this element must be able to consume
             * at least one argument.</p>
             *
             * @return This builder, for chaining
             */
            Builder<T> consumeAllRemaining();

            /**
             * Marks this parameter as optional, such that if an argument does not
             * exist <em>or</em> cannot be parsed, an exception is not thrown, and
             * no value is returned.
             *
             * @return This builder, for chaining
             */
            Builder<T> optional();

            /**
             * Marks this parameter as optional, such that if an argument does not
             * exist <em>or</em> cannot be parsed, an exception is not thrown, and
             * the value provided is inserted into the context instead.
             *
             * @param defaultValue The default value if this parameter does not
             *                     enter a value into the {@link CommandContext}
             * @return This builder, for chaining
             */
            default Builder<T> orDefault(T defaultValue) {
                return orDefault(cause -> defaultValue);
            }

            /**
             * Marks this parameter as optional, such that if an argument does not
             * exist <em>or</em> cannot be parsed, the supplier is executed instead.
             * If this returns a value, it is inserted into the context under the
             * parameter's key, otherwise, an {@link ArgumentParseException} is
             * thrown.
             *
             * <p>If a default value is inserted into the context, the command
             * parser will attempt to parse the argument this element couldn't
             * parse using the next parser in the chain.</p>
             *
             * @param defaultValueSupplier A {@link Supplier} that returns an object
             *                             to insert into the context if this
             *                             parameter cannot parse the argument. If
             *                             the supplier returns a null,
             *                             the parameter will throw an exception, as
             *                             if the parameter is not optional.
             * @return This builder, for chaining
             */
            Builder<T> orDefault(Supplier<T> defaultValueSupplier);

            /**
             * Marks this parameter as optional, such that if an argument does not
             * exist <em>or</em> cannot be parsed, the function is executed instead.
             * If this returns a value, it is inserted into the context under the
             * parameter's key, otherwise, an {@link ArgumentParseException} is
             * thrown.
             *
             * @param defaultValueFunction A {@link Function} that returns an object
             *                             to insert into the context if this
             *                             parameter cannot parse the argument. If
             *                             the supplier returns a null,
             *                             the parameter will throw an exception, as
             *                             if the parameter is not optional.
             * @return This builder, for chaining
             */
            Builder<T> orDefault(Function<Cause, T> defaultValueFunction);

            /**
             * Creates a {@link Parameter} from the builder.
             *
             * @return The {@link Parameter}
             */
            Parameter.Value<T> build();

        }

    }

    /**
     * A {@link Subcommand} represents a literal argument where, if parsed, should
     * indicate to the command processor that the
     * {@link CommandExecutor} of the command
     * should change.
     */
    interface Subcommand extends Parameter {

        /**
         * The command that is parsed and potentially run when this subcommand
         * is parsed.
         *
         * @return The command to run.
         */
        Command getCommand();

        interface Builder extends ResettableBuilder<Subcommand, Builder> {

            /**
             * Sets an alias for the subcommand. This can be executed more
             * than once.
             *
             * @param alias The alias
             * @return This builder, for chaining
             */
            Builder alias(String alias);

            /**
             * Sets the {@link Command} to execute for this subcommand.
             *
             * @param command The {@link Command}
             * @return This builder, for chaining.
             */
            Builder setSubcommand(Command command);

            /**
             * Builds this subcommand parameter.
             *
             * <p>An alias and the command must be set, else a
             * {@link IllegalStateException} will be thrown.</p>
             *
             * @return The {@link Subcommand}
             */
            Subcommand build();

        }

    }

    /**
     * Specifies a builder for creating a {@link Parameter} that returns a
     * parameter that concatenates all parameters into a single
     * parameter to be executed one by one.
     */
    interface SequenceBuilder extends ResettableBuilder<Parameter, SequenceBuilder> {

        /**
         * Sets that this parameter is optional, and will be ignored if it isn't
         * specified - but will throw an error if this is passed something to
         * parse that it cannot parse.
         *
         * @return This builder, for chaining
         */
        SequenceBuilder optional();

        /**
         * Sets that this parameter is weak optional and will be ignored if it
         * cannot parse the next element(s).
         *
         * @return This builder, for chaining
         */
        SequenceBuilder optionalWeak();

        /**
         * Defines the next parameter in the parameter sequence.
         *
         * @param parameter The parameter
         * @return This builder, for chaining
         */
        SequenceBuilder then(Parameter parameter);

        /**
         * Adds a set of {@link Parameter}s to this builder.
         *
         * <p>The parameters will be parsed in the provided order.</p>
         *
         * @param parameters The parameters to add
         * @return This builder, for chaining
         */
        default SequenceBuilder then(Parameter... parameters) {
            return then(Arrays.asList(parameters));
        }

        /**
         * Adds a set of {@link Parameter}s to this builder.
         *
         * <p>The parameters will be parsed in the provided order.</p>
         *
         * @param parameters The parameters to add
         * @return This builder, for chaining
         */
        default SequenceBuilder then(Iterable<Parameter> parameters) {
            for (Parameter parameter : parameters) {
                then(parameter);
            }

            return this;
        }

        /**
         * Creates a {@link Parameter} from the builder.
         *
         * @return The {@link Parameter}
         */
        Parameter build();

    }

    /**
     * Specifies a builder for creating a {@link Parameter} that returns a
     * parameter that concatenates all parameters into a single
     * parameter to be executed one by one.
     */
    interface FirstOfBuilder extends ResettableBuilder<Parameter, FirstOfBuilder> {

        /**
         * Sets that this parameter is optional, and will be ignored if it isn't
         * specified - but will throw an error if this is passed something to
         * parse that it cannot parse.
         *
         * @return This builder, for chaining
         */
        FirstOfBuilder optional();

        /**
         * Sets that this parameter is weak optional and will be ignored if it
         * cannot parse the next element(s).
         *
         * @return This builder, for chaining
         */
        FirstOfBuilder optionalWeak();

        /**
         * Adds a parameter that can be used to parse an argument. Parameters
         * are checked in the order they are added to the builder.
         *
         * @param parameter The parameter
         * @return This builder, for chaining
         */
        FirstOfBuilder or(Parameter parameter);

        /**
         * Adds a set of {@link Parameter}s to this builder.
         *
         * <p>The parameters will be parsed in the provided order until one
         * succeeds.</p>
         *
         * @param parameters The parameters to add
         * @return This builder, for chaining
         */
        default FirstOfBuilder orFirstOf(Parameter... parameters) {
            return orFirstOf(Arrays.asList(parameters));
        }

        /**
         * Adds a set of {@link Parameter}s to this builder.
         *
         * <p>The parameters will be parsed in the provided order until one
         * succeeds.</p>
         *
         * @param parameters The parameters to add
         * @return This builder, for chaining
         */
        default FirstOfBuilder orFirstOf(Iterable<Parameter> parameters) {
            for (Parameter parameter : parameters) {
                or(parameter);
            }

            return this;
        }

        /**
         * Creates a {@link Parameter} from the builder.
         *
         * @return The {@link Parameter}
         */
        Parameter build();

    }

}