package com.candlk.common.security;

import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.util.Base64;
import javax.annotation.Nullable;
import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class AES {

	protected final byte[] key;
	protected final SecretKeySpec secretKeySpec;

	/** 密钥长度只能是 128（16个字节）、192（24个字节） 或 256位（32个字节） */
	public AES(byte[] key) {
		this.key = key;
		secretKeySpec = new SecretKeySpec(key, "AES");
	}

	/** 密钥长度只能是 128（16个字节）、192（24个字节） 或 256位（32个字节） */
	public AES(String secret) {
		this(secret.getBytes(StandardCharsets.UTF_8));
	}

	/**
	 * 初始向量的方法，全部为 0。 这里的写法适合于其它算法，针对AES算法的话，IV值一定是128位的(16字节)。
	 */
	static byte[] initIv(Cipher cipher) {
		int blockSize = cipher.getBlockSize();
		return new byte[blockSize];
	}

	/**
	 * 获取加密器
	 */
	public Cipher getCipher(int mode, @Nullable byte[] iv) throws GeneralSecurityException {
		return getCipher(secretKeySpec, mode, iv);
	}

	/**
	 * 获取加密器
	 */
	public static Cipher getCipher(SecretKeySpec secretKeySpec, int mode, @Nullable byte[] iv) throws GeneralSecurityException {
		final String algorithm = "AES/CBC/PKCS5Padding";
		Cipher cipher = Cipher.getInstance(algorithm);
		final IvParameterSpec ips = new IvParameterSpec(iv == null ? initIv(cipher) : iv);
		cipher.init(mode, secretKeySpec, ips);
		return cipher;
	}

	/**
	 * AES 加密操作
	 *
	 * @param data 待加密内容
	 * @return 字节数组
	 */
	public byte[] encrypt(byte[] data, @Nullable byte[] iv) throws GeneralSecurityException {
		Cipher cipher = getCipher(Cipher.ENCRYPT_MODE, iv);
		return cipher.doFinal(data);
	}

	/**
	 * AES 加密操作
	 *
	 * @param data 待加密内容
	 * @return 字节数组
	 */
	public byte[] encrypt(byte[] data) throws GeneralSecurityException {
		return encrypt(data, null);
	}

	/**
	 * AES解密
	 *
	 * @param data 待解密内容
	 * @return 字节数组
	 */
	public byte[] decrypt(byte[] data, @Nullable byte[] iv) throws GeneralSecurityException {
		Cipher cipher = getCipher(Cipher.DECRYPT_MODE, iv);
		return cipher.doFinal(data);
	}

	/**
	 * AES解密
	 *
	 * @param data 待解密内容
	 * @return 字节数组
	 */
	public byte[] decrypt(byte[] data) throws GeneralSecurityException {
		return decrypt(data, null);
	}

	/**
	 * AES 加密操作
	 *
	 * @param source 待加密内容
	 * @return Base64转码后的加密数据
	 */
	public String encrypt(String source, final boolean urlSafe) {
		byte[] bytes = source.getBytes(StandardCharsets.UTF_8);
		byte[] result;
		try {
			result = encrypt(bytes);// 加密
		} catch (GeneralSecurityException e) {
			throw new IllegalArgumentException("对数据进行AES加密时出错：", e);
		}
		return (urlSafe ? Base64.getUrlEncoder() : Base64.getEncoder()).encodeToString(result); // 通过Base64转码返回;
	}

	/**
	 * AES 加密操作
	 *
	 * @param source 待加密内容
	 * @return Base64转码后的加密数据
	 */
	public String encrypt(String source) {
		return encrypt(source, false);
	}

	/**
	 * AES 解密操作
	 */
	public String decrypt(String text, final boolean urlSafe) {
		byte[] bytes = (urlSafe ? Base64.getUrlDecoder() : Base64.getDecoder()).decode(text);
		byte[] result;
		try {
			result = decrypt(bytes);
		} catch (GeneralSecurityException e) {
			throw new IllegalArgumentException("对数据进行AES解密时出错：", e);
		}
		return new String(result, StandardCharsets.UTF_8);
	}

	/**
	 * AES 解密操作
	 */
	public String decrypt(String text) {
		return decrypt(text, false);
	}

}
