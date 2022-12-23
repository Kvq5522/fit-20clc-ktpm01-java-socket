import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import javax.swing.*;
import FileTracker.FileTracker;

public class test {
    static long getFolderSize(File folder) {
        long length = 0;

        for (File file : folder.listFiles()) {
            if (file.isFile()) {
                length += file.length();
                continue;
            }

            length += getFolderSize(file) + 4096;
        }

        return length;
    }

    public static void main(String[] args) {
        try {
            FileTracker fileTracker = new FileTracker();

            fileTracker.setMap(fileTracker.getFilesAndFolders(System.getProperty("user.dir")));

            for (String key : fileTracker.getMap().keySet()) {
                System.out.println(key + " " + fileTracker.getMap().get(key));
            }

            System.out.println("---------------");

            fileTracker.setMap(fileTracker.getFilesAndFolders("/home/khang/Desktop/java"));

            for (String key : fileTracker.getMap().keySet()) {
                System.out.println(key + " " + fileTracker.getMap().get(key));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}