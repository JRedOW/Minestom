package net.minestom.server.tag;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jglrxavpok.hephaistos.nbt.NBTCompound;

/**
 * Represents an element which can read {@link Tag tags}.
 */
public interface TagReadable {

    /**
     * Reads the specified tag.
     *
     * @param tag the tag to read
     * @param <T> the tag type
     * @return the read tag, null if not present
     */
    <T> @Nullable T getTag(@NotNull Tag<T> tag);

    /**
     * Returns if a tag is present.
     *
     * @param tag the tag to check
     * @return true if the tag is present, false otherwise
     */
    default boolean hasTag(@NotNull Tag<?> tag) {
        return getTag(tag) != null;
    }

    /**
     * Reads the specified tag, give a default value in not found
     *
     * @param tag the tag to check
     * @param defaultValue - the value to return if the key is not found
     * @param <T> the tag type
     * @return true if the tag is present, false otherwise
     */
    default <T> @NotNull T getTagOrDefault(@NotNull Tag<T> tag, @NotNull T defaultValue) {
        T value;
        return (value = getTag(tag)) != null ? value : defaultValue;
    }

    /**
     * Converts an nbt compound to a tag reader.
     *
     * @param compound the compound to convert
     * @return a {@link TagReadable} capable of reading {@code compound}
     */
    static @NotNull TagReadable fromCompound(@NotNull NBTCompound compound) {
        return new TagReadable() {
            @Override
            public <T> @Nullable T getTag(@NotNull Tag<T> tag) {
                return tag.read(compound);
            }
        };
    }
}
