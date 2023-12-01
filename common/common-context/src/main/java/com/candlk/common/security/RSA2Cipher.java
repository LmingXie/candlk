package com.candlk.common.security;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.security.spec.*;
import java.util.Base64;
import javax.annotation.Nullable;
import javax.crypto.*;

import lombok.Getter;
import me.codeplayer.util.Assert;
import me.codeplayer.util.StringUtil;

/**
 * 基于 RSA2 算法的非对称加密工具实例，支持加密/解密 + 签名/验签等
 */
@Getter
public class RSA2Cipher {

	PublicKey publicKey;
	PrivateKey privateKey;
	boolean publicSide;

	public static final String KEY_ALGORITHM = "RSA";
	public static final String SIGNATURE_ALGORITHM = "SHA256withRSA";
	public static final String RSA_TYPE = "RSA/ECB/PKCS1Padding";
	public static final int KEY_SIZE = 2048; // 设置长度

	public RSA2Cipher init(String publicKeyStr, String privateKeyStr, boolean publicSide) throws NoSuchAlgorithmException, InvalidKeySpecException {
		return init(publicSide || StringUtil.notEmpty(publicKeyStr) ? getPublicKey(publicKeyStr) : null, !publicSide || StringUtil.notEmpty(privateKeyStr) ? getPrivateKey(privateKeyStr) : null, publicSide);
	}

	public RSA2Cipher init(String publicKeyStr, String privateKeyStr) throws NoSuchAlgorithmException, InvalidKeySpecException {
		return init(publicKeyStr, privateKeyStr, false);
	}

	public RSA2Cipher init(PublicKey publicKey, PrivateKey privateKey, boolean publicSide) {
		Assert.notNull(publicSide ? publicKey : privateKey);
		this.publicKey = publicKey;
		this.privateKey = privateKey;
		this.publicSide = publicSide;
		return this;
	}

	/**
	 * 基于 SHA256withRSA 对指定文本进行签名，并返回签名
	 */
	public static String sign(PrivateKey privateKey, String source) throws NoSuchAlgorithmException, InvalidKeyException, SignatureException {
		Signature sign = Signature.getInstance(SIGNATURE_ALGORITHM);
		sign.initSign(privateKey);
		sign.update(source.getBytes());
		byte[] signed = sign.sign();
		return Base64.getEncoder().encodeToString(signed);
	}

	/**
	 * 基于 SHA256withRSA 对指定文本进行签名，并返回签名
	 */
	public String sign(String source) throws NoSuchAlgorithmException, InvalidKeyException, SignatureException {
		return sign(privateKey, source);
	}

	/**
	 * 基于 SHA256withRSA 对指定文本和签名进行验签，并返回验签结果
	 *
	 * @return true=验签通过，false=验签失败
	 */
	public static boolean verifySign(PublicKey publicKey, String source, String signature) throws NoSuchAlgorithmException, InvalidKeyException, SignatureException {
		Signature verifySign = Signature.getInstance(SIGNATURE_ALGORITHM);
		verifySign.initVerify(publicKey);
		verifySign.update(source.getBytes(StandardCharsets.UTF_8));
		return verifySign.verify(Base64.getDecoder().decode(signature));
	}

	/**
	 * 基于 SHA256withRSA 对指定文本和签名进行验签，并返回验签结果
	 *
	 * @return true=验签通过，false=验签失败
	 */
	public boolean verifySign(String source, String signature) throws NoSuchAlgorithmException, InvalidKeyException, SignatureException {
		return verifySign(publicKey, source, signature);
	}

	/**
	 * 基于 "RSA/ECB/PKCS1Padding" 对指定密钥 + 指定文本进行加密<br>
	 * 【注意】使用公钥加密，则必须使用私钥解密，反之亦然
	 */
	public static String encrypt(Key withKey, String text) throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
		final Cipher cipher = Cipher.getInstance(RSA_TYPE);
		cipher.init(Cipher.ENCRYPT_MODE, withKey);
		byte[] encryptedBytes = cipher.doFinal(text.getBytes(StandardCharsets.UTF_8));
		return Base64.getEncoder().encodeToString(encryptedBytes);
	}

	/**
	 * 基于 指定算法 + 对应密钥 对 指定数据进行加密
	 */
	public static byte[] encryptWith(String algorithm, Key withKey, byte[] data) throws NoSuchPaddingException, NoSuchAlgorithmException, ShortBufferException, IllegalBlockSizeException, BadPaddingException, InvalidKeyException {
		final Cipher cipher = Cipher.getInstance(algorithm);
		cipher.init(Cipher.ENCRYPT_MODE, withKey);
		final int length = data.length;
		final int outputSize = cipher.getOutputSize(0);
		final int maxBlockSize = outputSize - 11;
		int sliceNum = length / maxBlockSize;
		if (length % maxBlockSize != 0) {
			sliceNum++;
		}
		final ByteArrayOutputStream out = new ByteArrayOutputStream(sliceNum * outputSize);
		// 对数据分段加密
		final byte[] buffer = new byte[outputSize];
		int offset = 0;
		while (offset < length) {
			int sliceSize = Math.min(length - offset, maxBlockSize);
			int len = cipher.doFinal(data, offset, sliceSize, buffer);
			out.write(buffer, 0, len);
			offset += sliceSize;
		}
		return out.toByteArray();
	}

	/**
	 * 基于 "RSA/ECB/PKCS1Padding" 对指定密钥 + 指定文本进行解密<br/>
	 * 【注意】使用公钥加密，则必须使用私钥解密，反之亦然
	 */
	public static String decrypt(Key withKey, String encryptedBase64) throws IllegalBlockSizeException, BadPaddingException, NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException {
		final Cipher cipher = Cipher.getInstance(RSA_TYPE);
		cipher.init(Cipher.DECRYPT_MODE, withKey);
		byte[] encryptedBytes = Base64.getDecoder().decode(encryptedBase64);
		byte[] decryptedBytes = cipher.doFinal(encryptedBytes);
		return new String(decryptedBytes);
	}

	/**
	 * 创建 RSA2 公钥/私钥对
	 */
	public static KeyPair createKeyPair() throws NoSuchAlgorithmException {
		KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance(KEY_ALGORITHM);
		keyPairGenerator.initialize(KEY_SIZE, new SecureRandom());
		return keyPairGenerator.generateKeyPair();
	}

	/**
	 * 将公钥字符串转为 PublicKey 对象
	 */
	public static PublicKey getPublicKey(String publicKeyStr) throws NoSuchAlgorithmException, InvalidKeySpecException {
		final byte[] bytes = Base64.getDecoder().decode(publicKeyStr);
		X509EncodedKeySpec x509EncodedKeySpec = new X509EncodedKeySpec(bytes);
		KeyFactory keyFactory = KeyFactory.getInstance(KEY_ALGORITHM);
		return keyFactory.generatePublic(x509EncodedKeySpec);
	}

	/**
	 * 将私钥字符串转为 PrivateKey 对象
	 */
	public static PrivateKey getPrivateKey(String privateKey) throws NoSuchAlgorithmException, InvalidKeySpecException {
		final byte[] bytes = Base64.getDecoder().decode(privateKey);
		PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(bytes);
		KeyFactory keyFactory = KeyFactory.getInstance(KEY_ALGORITHM);
		return keyFactory.generatePrivate(keySpec);
	}

	@Nullable
	public static RSA2Cipher of(@Nullable String publicKey, @Nullable String privateKey) throws NoSuchAlgorithmException, InvalidKeySpecException {
		return StringUtil.notEmpty(publicKey) || StringUtil.notEmpty(privateKey) ? new RSA2Cipher().init(publicKey, privateKey) : null;
	}

}
