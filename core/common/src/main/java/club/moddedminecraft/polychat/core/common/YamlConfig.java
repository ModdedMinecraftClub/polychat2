package club.moddedminecraft.polychat.core.common;

import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;

import java.io.BufferedReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.TreeMap;

/**
 * This class represents a key-value store of YAML where dots are used to refer to nested maps. E.g. "server.port" tells
 * this class to find a map entry with key "server" and a map for a value, and then in that value map find an entry with
 * key "port." All YAML documents that this class processes *must* have a map in their root.
 *
 * <b>This class is *NOT* thread-safe.</b>
 */
public final class YamlConfig {
    private final Optional<Path> file;
    private final TreeMap<String, Object> data;
    private boolean wasChanged = false;

    /**
     * Constructs a new YamlConfig from an in-memory string that contains its YAML.
     *
     * @param yaml The yaml to construct from
     * @return The newly-constructed <code>YamlConfig</code> instance
     */
    public static YamlConfig fromInMemoryString(String yaml) {
        return new YamlConfig(yaml, Optional.empty());
    }

    /**
     * Constructs a new <code>YamlConfig</code>. from a file on the filesystem. This method reads the file as a text file, and then
     * processes the contents as YAML to create a new <code>YamlConfig</code>.
     *
     * @param file The file to read
     * @return The new YamlConfig
     * @throws IOException If reading the file fails
     */
    public static YamlConfig fromFilesystem(Path file) throws IOException {
        BufferedReader reader = Files.newBufferedReader(file);
        StringBuilder content = new StringBuilder("");
        String line;
        while ((line = reader.readLine()) != null) {
            content.append(line).append("\n");
        }

        return new YamlConfig(content.toString(), Optional.of(file));
    }

    private YamlConfig(String fileContents, Optional<Path> file) throws YamlConfigException {
        try {
            this.file = file;

            Yaml yaml = new Yaml();
            Map<String, Object> rootMap = yaml.load(fileContents);
            if (rootMap == null) {
                rootMap = new TreeMap<>();
            }

            data = new TreeMap<>();
            recursivelyLoadMap(rootMap, "");
        } catch (ClassCastException e) {
            throw new RuntimeException("The root of any YAML configuration must be a map.", e);
        }
    }

    private void recursivelyLoadMap(Map<String, Object> currentMap, String prefix) {
        for (Map.Entry<String, Object> entry : currentMap.entrySet()) {
            if (entry.getValue() instanceof Map) {
                try {
                    @SuppressWarnings("unchecked")
                    Map<String, Object> subMap = (Map<String, Object>) entry.getValue();
                    recursivelyLoadMap(subMap, prefix + entry.getKey() + ".");
                } catch (ClassCastException e) {
                    data.put(prefix + entry.getKey(), entry.getValue());
                }
            } else {
                data.put(prefix + entry.getKey(), entry.getValue());
            }
        }
    }

    /**
     * Encodes the stored YAML data to a string that contains YAML
     *
     * @return A string that contains YAML
     * @throws YamlConfigException If the stored YAML data is malformed (a non-map entry has children, as if it was a map)
     */
    @SuppressWarnings("unchecked")
    public String saveToString() throws YamlConfigException {
        TreeMap<String, Object> rootMap = new TreeMap<>();

        for (Map.Entry<String, Object> entry : data.entrySet()) {
            String[] splitString = entry.getKey().split("\\.");
            TreeMap<String, Object> currentMap = rootMap;

            for (int i = 0; i < splitString.length - 1; ++i) {
                try {
                    currentMap = (TreeMap<String, Object>) currentMap.computeIfAbsent(splitString[i], (String key) -> new TreeMap<String, Object>());
                } catch (ClassCastException e) {
                    throw new YamlConfigException("A non-map entry has children as if it were a map", e);
                }
            }

            currentMap.put(splitString[splitString.length - 1], entry.getValue());
        }

        DumperOptions options = new DumperOptions();
        options.setIndent(2);
        options.setPrettyFlow(true);
        options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
        Yaml yaml = new Yaml(options);
        return yaml.dump(rootMap);
    }

    /**
     * Saves the stored YAML data to the file that this YAML data was loaded from
     *
     * @throws NoSuchElementException If the YAML data was not loaded from a file
     * @throws IOException            If the saving encounters an error
     */
    public void saveToFile() throws NoSuchElementException, IOException {
        saveToFile(file.orElseThrow(IOException::new));
    }

    /**
     * Saves the stored YAML data to the specified file
     *
     * @throws IOException If the saving encounters an error
     */
    public void saveToFile(Path file) throws IOException {
        FileWriter writer = new FileWriter(file.toFile());
        writer.write(saveToString());
        writer.close();
    }

    /**
     * Returns whether or not the data has been changed via one of the setter methods since hte last save. Note that
     * this method won't detect changes that happened via references to the data stored in this map (e.g. if one gets a
     * list and then changes it without calling any setters on this clas)
     *
     * @return whether or not the data has been changed via one of the setter methods since hte last save
     */
    public boolean isChanged() {
        return wasChanged;
    }

    /**
     * Gets the data specified by the given key
     *
     * @param key the given key
     * @param <T> the type of the data
     * @return The data specified by the given key
     * @throws ClassCastException If the data specified by the given key is not of the given type
     */
    @SuppressWarnings("unchecked")
    public <T> T get(String key) throws ClassCastException {
        return (T) data.get(key);
    }

    /**
     * Gets the data specified by the given key or default if not present
     *
     * @param key   the given key
     * @param other value to return if value not present in config
     * @param <T>   the type of the data
     * @return The data specified by the given key
     * @throws ClassCastException If the data specified by the given key is not of the given type
     */
    @SuppressWarnings("unchecked")
    public <T> T getOrDefault(String key, T other) throws ClassCastException {
        T value = (T) data.get(key);
        return value == null ? other : value;
    }

    /**
     * Sets the data specified by the given key, changing its type if needed.
     *
     * @param key      the given key
     * @param newValue the new data
     * @param <T>      the type of the new data
     */
    public <T> void set(String key, T newValue) {
        data.put(key, newValue);
        wasChanged = true;
    }

    /**
     * Sets the data specified by the given key if and only if the given key does not currently have data associated
     * with it. This should be used for setting defaults in a configuration.
     *
     * @param key      the given key
     * @param newValue the new data
     * @param <T>      the type of the new data
     */
    public <T> void setIfAbsent(String key, T newValue) {
        if (data.putIfAbsent(key, newValue) == null) {
            wasChanged = true;
        }
    }

    /**
     * This class is used to represent miscelaneous errors that may occur while using this class. See method
     * documentation on <code>YamlConfig</code> for details.
     */
    public static class YamlConfigException extends RuntimeException {
        public YamlConfigException() {
        }

        public YamlConfigException(String message) {
            super(message);
        }

        public YamlConfigException(String message, Throwable cause) {
            super(message, cause);
        }

        public YamlConfigException(Throwable cause) {
            super(cause);
        }

        public YamlConfigException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
            super(message, cause, enableSuppression, writableStackTrace);
        }
    }

}
