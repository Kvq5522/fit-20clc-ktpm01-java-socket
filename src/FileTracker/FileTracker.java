package FileTracker;

import java.io.File;
import java.io.Serializable;
import java.util.HashMap;

public class FileTracker {
    HashMap<String, Long> map;
    public FileTracker() {
        map = new HashMap<>();
    }

    public FileTracker(HashMap<String, Long> map) {
        this.map = map;
    }

    public HashMap<String, Long> getFilesAndFolders(String path) {
        HashMap<String, Long> filesAndFolders = new HashMap<>();

        File file = new File(path);
        File[] files = file.listFiles();
        for (File f : files) {
            if (f.isFile() || f.isDirectory()) {
                filesAndFolders.put(f.getName(), f.length());
            }
        }

        return filesAndFolders;
    }

    public void setMap(HashMap<String, Long> map) {
        this.map = map;
    }

    public HashMap<String, Long> getMap() {
        return map;
    }

    public Long getSize() {
        Long size = 0L;

        for (String key : map.keySet()) {
            size += map.get(key);
        }

        return size;
    }

    public boolean isChanged(String path) {
        HashMap<String, Long> newMap = getFilesAndFolders(path);

        if (map.equals(newMap)) {
            return false;
        }

        map = newMap;
        return false;
    }
}
