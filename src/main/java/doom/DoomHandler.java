package doom;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.javacord.api.entity.message.embed.EmbedBuilder;

import java.awt.*;
import java.io.*;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class DoomHandler {

    public static final String DOOM = "Doom!";
    private static final DoomHandler instance = new DoomHandler();
    private DoomConfigs doomConfigs;

    private DoomHandler() {
        try {
            doomConfigs = new Gson().fromJson(new BufferedReader(new FileReader("resources/doom.json")), new TypeToken<DoomConfigs>() {
            }.getType());
        } catch (FileNotFoundException e) {
            doomConfigs = new DoomConfigs();
        }
    }

    /**
     * Adds the specified amount of doom to a specified doom pool and then serialize it. If the doom pool does not
     * exist, create a new doom pool and set the number of doom points to be added as the doom pool's value.
     *
     * @param pool    The name of the doom pool to add the doom to
     * @param doomVal The number of doom points to be added
     * @return An embed that shows the change in doom points for the specified doom pool
     */
    public static EmbedBuilder addDoom(String pool, int doomVal) {
        final int oldDoom = getDoom(pool);
        final int newDoom = instance.doomConfigs.getDoomPools().compute(pool, (s, integer) -> integer != null ? integer + doomVal : doomVal);
        serializePools();
        return generateDoomEmbed(pool, oldDoom, newDoom);
    }

    /**
     * Overloaded version of addDoom that adds doom to the currently active pool
     *
     * @param doomVal The number of doom points to add to the pool
     * @return An embed that shows the change in doom points for the active doom pool
     */
    public static EmbedBuilder addDoom(int doomVal) {
        return addDoom(getActivePool(), doomVal);
    }

    /**
     * Gets the number of doom points in a doom pool
     *
     * @param pool The pool to check
     * @return The number of doom points in that pool
     */
    public static int getDoom(String pool) {
        return instance.doomConfigs.getDoomPools().getOrDefault(pool, 0);
    }

    /**
     * Gets the number of doom points in the active doom pool
     *
     * @return The number of doom points in the active doom pool
     */
    public static int getDoom() {
        return getDoom(getActivePool());
    }

    /**
     * Sets the doom for a specific doom pool and then serialize it. Creates a new doom pool if it does already exist.
     *
     * @param newDoom The new value of the doom pool
     * @return The embed with a new doom value
     */
    public static EmbedBuilder setDoom(String pool, int newDoom) {
        int oldDoom = getDoom(pool);
        instance.doomConfigs.getDoomPools().put(pool, newDoom);
        serializePools();
        return generateDoomEmbed(pool, oldDoom, newDoom);
    }

    /**
     * Generates an embed that shows the amount of doom points in the specified doom pool.
     *
     * @param pool The name of the doom pool to check
     * @return The an embed containing the value of the doom pool
     */
    public static EmbedBuilder generateDoomEmbed(String pool) {
        final int doom = getDoom(pool);
        return new EmbedBuilder()
                .setTitle(pool)
                .setDescription(String.valueOf(doom))
                .setColor(new Color((int) (doom % 101 * (2.55))));
    }

    /**
     * Generates an embed to show a change in doom points.
     *
     * @param pool    The name of the doom pool to display
     * @param oldDoom The original amount of doom in the pool before it was modified
     * @param newDoom The new amount of doom in the pool
     * @return An embed that shows the name of the pool, and change in doom. The color of the embed gets darker as the
     * number of points in the pool approaches the next multiple of 100.
     */
    public static EmbedBuilder generateDoomEmbed(String pool, int oldDoom, int newDoom) {
        return new EmbedBuilder()
                .setTitle(pool)
                .setDescription(oldDoom + " → " + newDoom)
                .setColor(new Color((int) (newDoom % 101 * (2.55))));
    }

    /**
     * Serializes the values of the various doom pools and writes it to doom.json
     */
    private static void serializePools() {
        try {
            final BufferedWriter writer = new BufferedWriter(new FileWriter("resources/doom.json"));
            new Gson().toJson(instance.doomConfigs, writer);
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Deletes a doom pool and serialize the change.
     *
     * @param pool The name of the doom pool to delete
     * @return An embed containing information about the deleted doom pool and the number of doom points it had
     */
    public static EmbedBuilder deletePool(String pool) {
        final Integer removedDoom = instance.doomConfigs.getDoomPools().remove(pool);
        final String description = Optional.ofNullable(removedDoom).map(doom -> MessageFormat.format("I''ve removed the ''**{0}**'' doom pool, which contained {1} doom points.", pool, doom)).orElseGet(() -> MessageFormat.format("I was unable to find the ''**{0}**'' doom pool", pool));
        serializePools();
        return new EmbedBuilder()
                .setTitle(DOOM)
                .setDescription(description);
    }

    /**
     * Generates an embed that provides a summary of all of the doom pools.
     *
     * @return Am embed with information on the names and values of all available doom pools and the name and value of
     * the active doom pool
     */
    public static EmbedBuilder generateDoomEmbed() {
        final EmbedBuilder embedBuilder = new EmbedBuilder().setTitle(DOOM).setDescription(MessageFormat.format("Here are the values of all doom pools.\nThe current active doom pool is ''**{0}**'' with {1} doom points", getActivePool(), getDoom(getActivePool())));
        instance.doomConfigs.getDoomPools().forEach((key, value) -> embedBuilder.addField(key, String.valueOf(value)));
        return embedBuilder;
    }

    /**
     * Getter for the value of the active pool
     *
     * @return The name of the active pool
     */
    public static String getActivePool() {
        return instance.doomConfigs.getActivePool();
    }

    /**
     * Sets the active pool, serialize it, and return an embed with information about the pool.
     *
     * @param pool The doom pool to be set as the active pool
     * @return An embed with that includes the name and value of the new active doom pool
     */
    public static EmbedBuilder setActivePool(String pool) {
        instance.doomConfigs.setActivePool(pool);
        serializePools();
        return new EmbedBuilder().setTitle(DOOM).setDescription(MessageFormat.format("I''ve set the active doom pool to ''**{0}**'', which contains {1} doom points.", pool, getDoom(pool)));
    }

    /**
     * Inner class that stores the representation of doom.json
     */
    private static class DoomConfigs {
        private final Map<String, Integer> doomPools;
        private String activePool;

        public DoomConfigs() {
            doomPools = new HashMap<>();
            activePool = "Doom!";
        }

        public Map<String, Integer> getDoomPools() {
            return doomPools;
        }

        public String getActivePool() {
            return activePool;
        }

        public void setActivePool(String activePool) {
            this.activePool = activePool;
        }
    }
}