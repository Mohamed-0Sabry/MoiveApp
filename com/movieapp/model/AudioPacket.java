package com.movieapp.model;

import java.io.*;

public class AudioPacket implements Serializable {
    private static final long serialVersionUID = 1L;
    private byte[] audioData;
    private String senderId;

    public AudioPacket(byte[] audioData, String senderId) {
        this.audioData = audioData;
        this.senderId = senderId;
    }

    public byte[] getAudioData() {
        return audioData;
    }

    public String getSenderId() {
        return senderId;
    }

    public byte[] toBytes() {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
             DataOutputStream dos = new DataOutputStream(baos)) {
            
            // Write sender ID length and data
            byte[] senderBytes = senderId.getBytes("UTF-8");
            dos.writeInt(senderBytes.length);
            dos.write(senderBytes);
            
            // Write audio data length and data
            dos.writeInt(audioData.length);
            dos.write(audioData);
            
            dos.flush();
            byte[] result = baos.toByteArray();
            System.out.println("[AudioPacket] Serialized packet size: " + result.length + 
                             " bytes (sender: " + senderBytes.length + 
                             ", audio: " + audioData.length + ")");
            return result;
        } catch (IOException e) {
            System.err.println("Error serializing audio packet: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    public static AudioPacket fromBytes(byte[] data, int length) {
        if (data == null || length <= 0) {
            System.err.println("Invalid audio packet data: null or empty");
            return null;
        }

        try (ByteArrayInputStream bais = new ByteArrayInputStream(data, 0, length);
             DataInputStream dis = new DataInputStream(bais)) {
            
            // Read sender ID
            int senderLength = dis.readInt();
            if (senderLength <= 0 || senderLength > 1024) {
                System.err.println("Invalid sender length: " + senderLength);
                return null;
            }
            byte[] senderBytes = new byte[senderLength];
            int read = dis.read(senderBytes, 0, senderLength);
            if (read != senderLength) {
                System.err.println("Failed to read complete sender ID. Expected: " + 
                                 senderLength + ", Read: " + read);
                return null;
            }
            String senderId = new String(senderBytes, "UTF-8");
            
            // Read audio data
            int audioLength = dis.readInt();
            if (audioLength <= 0 || audioLength > 8192) {
                System.err.println("Invalid audio length: " + audioLength);
                return null;
            }
            byte[] audioData = new byte[audioLength];
            read = dis.read(audioData, 0, audioLength);
            if (read != audioLength) {
                System.err.println("Failed to read complete audio data. Expected: " + 
                                 audioLength + ", Read: " + read);
                return null;
            }
            
            System.out.println("[AudioPacket] Deserialized packet: sender=" + senderId + 
                             ", audio size=" + audioLength);
            return new AudioPacket(audioData, senderId);
        } catch (IOException e) {
            System.err.println("Error deserializing audio packet: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
} 