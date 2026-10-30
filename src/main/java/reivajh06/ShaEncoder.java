package reivajh06;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

public class ShaEncoder {

	private static final MessageDigest ENCODER;

	static {
		try {
			ENCODER = MessageDigest.getInstance("SHA-256");
		} catch (NoSuchAlgorithmException e) {
			throw new RuntimeException(e);
		}
	}

	public static String encode(String text) {
		byte[] encodedHash = ENCODER.digest(text.getBytes(StandardCharsets.UTF_8));

		return HexFormat.of().formatHex(encodedHash);
	}
}
