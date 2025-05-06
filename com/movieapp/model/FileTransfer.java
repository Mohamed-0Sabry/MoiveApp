package com.movieapp.model;
import java.io.IOException;
import com.movieapp.utils.FileUtils;
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
        return FileUtils.readFileToBytes(path);
    }
    
    public static void saveFile(byte[] data, String destinationPath) throws IOException {
        FileUtils.writeBytesToFile(data, destinationPath);
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