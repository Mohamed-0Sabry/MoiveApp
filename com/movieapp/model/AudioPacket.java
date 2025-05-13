package com.movieapp.model;

import java.io.*;

public class AudioPacket implements Serializable {
    private static final long serialVersionUID = 1L;
    private static final String SYSTEM_AUDIO_SUFFIX = "_system";
    private static final int MAX_SENDER_LENGTH = 1024;
    private static final int MAX_AUDIO_LENGTH = 8192;
    
    private byte[] audioData;
    private String senderId;
    private boolean isSystemAudio;

    public AudioPacket(byte[] audioData, String senderId) {
        this.audioData = audioData;
        this.senderId = senderId;
        this.isSystemAudio = isSystemAudioSender(senderId);
    }

    public static AudioPacket createSystemAudioPacket(byte[] audioData, String baseSenderId) {
        String systemSenderId = baseSenderId + SYSTEM_AUDIO_SUFFIX;
        return new AudioPacket(audioData, systemSenderId);
    }

    private static boolean isSystemAudioSender(String senderId) {
        return senderId != null && senderId.endsWith(SYSTEM_AUDIO_SUFFIX);
    }

    public byte[] getAudioData() {
        return audioData;
    }

    public String getSenderId() {
        return senderId;
    }
    
    public boolean isSystemAudio() {
        return isSystemAudio;
    }

    public String getBaseSenderId() {
        if (isSystemAudio && senderId != null) {
            return senderId.substring(0, senderId.length() - SYSTEM_AUDIO_SUFFIX.length());
        }
        return senderId;
    }

    public byte[] toBytes() {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
             DataOutputStream dos = new DataOutputStream(baos)) {
            
            // Write sender ID length and data
            byte[] senderBytes = senderId.getBytes("UTF-8");
            if (senderBytes.length > MAX_SENDER_LENGTH) {
                throw new IOException("Sender ID too long: " + senderBytes.length + " bytes");
            }
            dos.writeInt(senderBytes.length);
            dos.write(senderBytes);
            
            // Write audio data length and data
            if (audioData.length > MAX_AUDIO_LENGTH) {
                throw new IOException("Audio data too long: " + audioData.length + " bytes");
            }
            dos.writeInt(audioData.length);
            dos.write(audioData);
            
            dos.flush();
            byte[] result = baos.toByteArray();
            System.out.println("[AudioPacket] Serialized packet size: " + result.length + 
                             " bytes (sender: " + senderBytes.length + 
                             ", audio: " + audioData.length + 
                             ", system: " + isSystemAudio + ")");
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
            if (senderLength <= 0 || senderLength > MAX_SENDER_LENGTH) {
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
            if (audioLength <= 0 || audioLength > MAX_AUDIO_LENGTH) {
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
                             ", audio size=" + audioLength +
                             ", system=" + isSystemAudioSender(senderId));
            return new AudioPacket(audioData, senderId);
        } catch (IOException e) {
            System.err.println("Error deserializing audio packet: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
} 