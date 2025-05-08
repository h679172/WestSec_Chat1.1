/*
 * West Security AS,
 * Chat app.
 * 
 * Password creator.
 * 
 */
package no.westsec.chat;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.Scanner;

public class Password {
	public static void passwordMain() {
		@SuppressWarnings("resource")
		Scanner in = new Scanner(System.in);
		System.out.println("Password:");
		String password = in.nextLine();
		System.out.println(createSHA256Hash(password));
	}
	public static String createSHA256Hash(String password) {
		MessageDigest digest = null;
		try {
			digest = MessageDigest.getInstance("SHA-256");
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		byte[] encodedhash = digest.digest(password.getBytes(StandardCharsets.UTF_8));
		for (int i = 0; i < 10000; i++) {
			encodedhash = digest.digest(encodedhash);
		}
		StringBuilder hexString = new StringBuilder();
		for (byte b : encodedhash) {
			String hex = Integer.toHexString(0xff & b);
			if (hex.length() > 0) {
				hexString.append(hex);
			}
		}
		return hexString.toString();
	}
	public static String hashPassword(String password, byte[] salt) {
	    try {
	        MessageDigest digest = MessageDigest.getInstance("SHA-256");
	        byte[] input = new byte[salt.length + password.getBytes(StandardCharsets.UTF_8).length];
	        System.arraycopy(salt, 0, input, 0, salt.length);
	        System.arraycopy(password.getBytes(StandardCharsets.UTF_8), 0, input, salt.length, password.getBytes(StandardCharsets.UTF_8).length);
	        byte[] hash = digest.digest(input);
	        for (int i = 0; i < 10000; i++) {
	            hash = digest.digest(hash);
	        }
	        return Base64.getEncoder().encodeToString(hash);
	    } catch (NoSuchAlgorithmException e) {
	        throw new RuntimeException("SHA-256 not available", e);
	    }
	}
	public static byte[] generateSalt() {
	    SecureRandom random = new SecureRandom();
	    byte[] salt = new byte[16];
	    random.nextBytes(salt);
	    return salt;
	}

}
