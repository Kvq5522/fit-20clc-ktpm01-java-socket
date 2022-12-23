import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import javax.swing.*;

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
        System.out.println(getFolderSize(new File("/home/khang/Desktop/java")));
    }
}