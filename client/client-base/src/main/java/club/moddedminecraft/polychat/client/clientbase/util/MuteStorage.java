package club.moddedminecraft.polychat.client.clientbase.util;

import java.io.*;
import java.nio.file.Path;
import java.util.*;

public class MuteStorage {
    private final String FILENAME = "polychat-mutelist.txt";
    private final Path path;
    private ArrayList<UUID> muteList;

    public MuteStorage(Path path) {
        this.path = path;
        loadMutelist();
    }

    private void loadMutelist() {
        muteList = new ArrayList<UUID>();
        try {
            Object unparsed = new ObjectInputStream(new FileInputStream(path.resolve(FILENAME).toFile())).readObject();
            if (!(unparsed instanceof UUID[])) {
                System.err.println("Failed to parse mutelist!");
                return;
            }
            UUID[] uuids = (UUID[]) unparsed;
            List<UUID> tempList = Arrays.asList(uuids);
            Collections.addAll(muteList, uuids);
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("Failed to access mutelist!");
        }
    }

    public boolean checkPlayer(UUID uuid) {
        return muteList.contains(uuid);
    }

    public void addPlayer(UUID uuid) {
        muteList.add(uuid);
        writeList(muteList);
    }

    public List<UUID> getMuteList() {
        return muteList;
    }

    public void removePlayer(UUID uuid) {
        muteList.remove(uuid);
        writeList(muteList);
    }

    private void writeList(ArrayList<UUID> uuids) {
        try {
            ObjectOutputStream outputStream = new ObjectOutputStream(new FileOutputStream(path.resolve(FILENAME).toFile()));
            UUID[] uuidArray = uuids.toArray(new UUID[0]);
            outputStream.writeObject(uuidArray);
        } catch (IOException e) {
            System.err.println("Failed to access mutelist");
        }
    }

}
