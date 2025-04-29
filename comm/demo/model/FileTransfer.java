package comm.demo.model;

import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class FileTransfer {
    private String fileName;
    private long fileSize;
    private byte[] fileData;

    public FileTransfer(String fileName, long fileSize, byte[] fileData) {
        this.fileName = fileName;
        this.fileSize = fileSize;
        this.fileData = fileData;
    }

    public static byte[] readFile(String path) throws IOException {
        Path filePath = Paths.get(path);
        return Files.readAllBytes(filePath);
    }

    public static void saveFile(byte[] data, String destinationPath) throws IOException {
        try (FileOutputStream fos = new FileOutputStream(destinationPath)) {
            fos.write(data);
        }
    }

    public String getFileName() {
        return fileName;
    }

    public long getFileSize() {
        return fileSize;
    }

    public byte[] getFileData() {
        return fileData;
    }
} 