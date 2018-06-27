package org.wickedsource.budgeteer.service.user;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.apache.commons.codec.binary.Hex;
import org.springframework.stereotype.Component;

@Component
public class PasswordHasher {

	public String hash(String plain){
		try {
			MessageDigest digest = MessageDigest.getInstance("SHA-512");
			byte[] hash = digest.digest(plain.getBytes());
			return new String(Hex.encodeHex(hash));
		} catch (NoSuchAlgorithmException e) {
			throw new RuntimeException(e);
		}
	}

}
