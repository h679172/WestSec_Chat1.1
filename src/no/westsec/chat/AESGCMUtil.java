package no.westsec.chat;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.nio.file.Files;
import java.security.SecureRandom;
import java.security.spec.KeySpec;
import java.util.Base64;

public class AESGCMUtil {
    private static final int GCM_IV_LENGTH = 12;
    private static final int GCM_TAG_LENGTH = 128;
    private static final int SALT_LENGTH = 16;
    private static final int KEY_LENGTH = 256;
    
    public static byte[] generateSalt(int length) {
        byte[] salt = new byte[length];
        new SecureRandom().nextBytes(salt);
        return salt;
    }
    public static SecretKey getKeyFromPassword(String password, byte[] salt) throws Exception {
        SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
        KeySpec spec = new PBEKeySpec(password.toCharArray(), salt, 65536, KEY_LENGTH);
        byte[] encoded = factory.generateSecret(spec).getEncoded();
        return new SecretKeySpec(encoded, "AES");
    }
    public static String encryptBundled(String plainText, String password) throws Exception {
        byte[] salt = generateSalt(SALT_LENGTH);
        SecretKey key = getKeyFromPassword(password, salt);
        byte[] iv = new byte[GCM_IV_LENGTH];
        new SecureRandom().nextBytes(iv);

        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
        cipher.init(Cipher.ENCRYPT_MODE, key, new GCMParameterSpec(GCM_TAG_LENGTH, iv));
        byte[] cipherText = cipher.doFinal(plainText.getBytes());

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        outputStream.write(salt);
        outputStream.write(iv);
        outputStream.write(cipherText);

        return Base64.getEncoder().encodeToString(outputStream.toByteArray());
    }
    public static String decryptBundled(String base64Input, String password) throws Exception {
        byte[] input = Base64.getDecoder().decode(base64Input);

        byte[] salt = new byte[SALT_LENGTH];
        byte[] iv = new byte[GCM_IV_LENGTH];
        byte[] cipherText = new byte[input.length - SALT_LENGTH - GCM_IV_LENGTH];

        System.arraycopy(input, 0, salt, 0, SALT_LENGTH);
        System.arraycopy(input, SALT_LENGTH, iv, 0, GCM_IV_LENGTH);
        System.arraycopy(input, SALT_LENGTH + GCM_IV_LENGTH, cipherText, 0, cipherText.length);

        SecretKey key = getKeyFromPassword(password, salt);
        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
        cipher.init(Cipher.DECRYPT_MODE, key, new GCMParameterSpec(GCM_TAG_LENGTH, iv));
        byte[] plainText = cipher.doFinal(cipherText);
        return new String(plainText);
    }
    public static void encryptFile(File inputFile, File outputFile, String password) throws Exception {
        byte[] fileBytes = Files.readAllBytes(inputFile.toPath());

        byte[] salt = generateSalt(SALT_LENGTH);
        SecretKey key = getKeyFromPassword(password, salt);
        byte[] iv = new byte[GCM_IV_LENGTH];
        new SecureRandom().nextBytes(iv);

        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
        cipher.init(Cipher.ENCRYPT_MODE, key, new GCMParameterSpec(GCM_TAG_LENGTH, iv));
        byte[] cipherBytes = cipher.doFinal(fileBytes);

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        outputStream.write(salt);
        outputStream.write(iv);
        outputStream.write(cipherBytes);
        Files.write(outputFile.toPath(), outputStream.toByteArray());
    }
    public static void decryptFile(File inputFile, File outputFile, String password) throws Exception {
        byte[] input = Files.readAllBytes(inputFile.toPath());
        byte[] salt = new byte[SALT_LENGTH];
        byte[] iv = new byte[GCM_IV_LENGTH];
        byte[] cipherBytes = new byte[input.length - SALT_LENGTH - GCM_IV_LENGTH];

        System.arraycopy(input, 0, salt, 0, SALT_LENGTH);
        System.arraycopy(input, SALT_LENGTH, iv, 0, GCM_IV_LENGTH);
        System.arraycopy(input, SALT_LENGTH + GCM_IV_LENGTH, cipherBytes, 0, cipherBytes.length);

        SecretKey key = getKeyFromPassword(password, salt);
        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
        cipher.init(Cipher.DECRYPT_MODE, key, new GCMParameterSpec(GCM_TAG_LENGTH, iv));
        byte[] plainBytes = cipher.doFinal(cipherBytes);
        Files.write(outputFile.toPath(), plainBytes);
    }
    public static String AESMain(int a, String input, String password) {
        String result = "";
        File inputFile;
        File encryptedFile;
        File decryptedFile;
        try {
            switch (a) {
                case 0:
                    result = encryptBundled(input, password);
                    break;
                case 1:
                    result = decryptBundled(input, password);
                    break;
                case 2:
                    inputFile = new File(input);
                    encryptedFile = new File(inputFile.getAbsolutePath() + ".enc");
                    encryptFile(inputFile, encryptedFile, password);
                    result = encryptedFile.getAbsolutePath();
                    break;
                case 3:
                    inputFile = new File(input);
                    encryptedFile = new File(inputFile.getAbsolutePath());
                    decryptedFile = new File(inputFile.getAbsolutePath() + ".dec");
                    decryptFile(encryptedFile, decryptedFile, password);
                    result = decryptedFile.getAbsolutePath();
                    break;
                default:
                    result = "Error: Choose either 0 (Encrypt), 1 (Decrypt), 2 (Encrypt file), 3 (Decrypt file)";
                    break;
            }
        } catch (Exception e) {
            result = "INVALID SECRETKEY: " + e.getMessage();
        }
        return result;
    }
}