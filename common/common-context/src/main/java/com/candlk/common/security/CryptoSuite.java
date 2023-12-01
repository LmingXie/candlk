package com.candlk.common.security;

import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;

import lombok.Getter;
import lombok.Setter;
import me.codeplayer.util.Assert;

@Getter
public class CryptoSuite {

	AES aes;
	RSA2Cipher rsa;

	public CryptoSuite init(AES aes, RSA2Cipher rsa) {
		this.aes = aes;
		this.rsa = rsa;
		return this;
	}

	public CryptoSuite checkAndInit(CryptoSuiteAttrs attrs) {
		attrs.check();
		AES aes = new AES(attrs.getAesKey());
		RSA2Cipher rsa;
		try {
			rsa = RSA2Cipher.of(attrs.getPublicKey(), attrs.getPrivateKey());
		} catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
			throw new IllegalArgumentException("RSA2Cipher init error:", e);
		}
		init(aes, rsa);
		return this;
	}

	@Getter
	@Setter
	public static class CryptoSuiteAttrs {

		protected String privateKey;
		protected String publicKey;
		protected String aesKey;

		public void check() {
			Assert.notEmpty(privateKey, "PrivateKey of RSA2Cipher is required.");
			Assert.notEmpty(aesKey, "aesKey of AES is required.");
		}

	}

}
