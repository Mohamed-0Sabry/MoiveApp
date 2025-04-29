package comm.demo.utils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class FileUtils {
    public static byte[] readFileToBytes(String path) throws IOException {
        Path filePath = Paths.get(path);
        return Files.readAllBytes(filePath);
    }

    public static void writeBytesToFile(byte[] data, String path) throws IOException {
        try (FileOutputStream fos = new FileOutputStream(path)) {
            fos.write(data);
        }
    }

    public static String getFileNameFromPath(String path) {
        return new File(path).getName();
    }

    public static String getFileExtension(String fileName) {
        int lastDotIndex = fileName.lastIndexOf('.');
        if (lastDotIndex > 0) {
            return fileName.substring(lastDotIndex + 1);
        }
        return "";
    }

    public static boolean isVideoFile(String fileName) {
        String extension = getFileExtension(fileName).toLowerCase();
        return extension.equals("mp4") || extension.equals("avi") || 
               extension.equals("mkv") || extension.equals("mov");
    }

    public static String createUniqueFileName(String originalName) {
        String baseName = originalName.substring(0, originalName.lastIndexOf('.'));
        String extension = getFileExtension(originalName);
        return baseName + "_" + System.currentTimeMillis() + "." + extension;
    }
} 