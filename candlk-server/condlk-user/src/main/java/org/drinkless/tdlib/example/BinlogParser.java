package org.drinkless.tdlib.example;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.security.spec.KeySpec;
import java.util.Arrays;
import javax.crypto.Cipher;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.*;

public class BinlogParser {

	public static void main(String[] args) throws Exception {
		File file = new File("D:\\idea\\candlk\\tdlib\\td.binlog");
		String dbKey = ""; // 如果你启动 TDLib 时设置了 databaseEncryptionKey，请填这里，否则留空
		parseBinlog(file, dbKey);
		// b0816625229dabd0fbb71ec8b7de79ed9a07129f3f21c3eed5d6de98ac6d5c07
	}

	public static void parseBinlog(File binlogFile, String dbKey) throws Exception {
		try (DataInputStream dis = new DataInputStream(new BufferedInputStream(new FileInputStream(binlogFile)))) {

			byte[] allBytes = dis.readAllBytes();
			ByteBuffer buffer = ByteBuffer.wrap(allBytes).order(ByteOrder.LITTLE_ENDIAN);

			boolean encrypted = false;
			byte[] aesKey = null;
			byte[] iv = null;

			int eventCount = 0;
			while (buffer.remaining() > 8) {
				int len = buffer.getInt();
				if (len <= 0 || len > buffer.remaining()) {
					break;
				}

				byte[] eventBytes = new byte[len];
				buffer.get(eventBytes);

				ByteBuffer eventBuf = ByteBuffer.wrap(eventBytes).order(ByteOrder.LITTLE_ENDIAN);

				int type = eventBuf.getInt();
				long id = eventBuf.getLong();
				long time = eventBuf.getLong();
				int dataLen = len - 4 - 8 - 8;

				if (type == -1) { // AesCtrEncryptionEvent
					encrypted = true;
					byte[] salt = readBytes(eventBuf);
					iv = readBytes(eventBuf);
					byte[] keyHash = readBytes(eventBuf);
					aesKey = deriveKey(dbKey, salt);

					byte[] check = hmacSha256(aesKey, "cucumbers everywhere".getBytes());
					if (!Arrays.equals(Arrays.copyOf(check, keyHash.length), keyHash)) {
						System.err.println("⚠️ databaseEncryptionKey 验证失败");
					} else {
						System.out.println("✅ AES-CTR encryption detected, key verified.");
					}
				} else {
					System.out.printf("Event #%d → type=%d, id=%d, time=%d, data=%d bytes%n",
							++eventCount, type, id, time, dataLen);
				}
			}

			if (encrypted && aesKey != null && iv != null) {
				System.out.println("\n开始解密剩余内容…");
				byte[] decrypted = decryptAesCtr(aesKey, iv, allBytes);
				System.out.println("解密完成。");
				// 你可以在这里进一步解析 TL 格式数据
			}
		}
	}

	private static byte[] readBytes(ByteBuffer buf) {
		int len = buf.getInt();
		byte[] b = new byte[len];
		buf.get(b);
		return b;
	}

	private static byte[] deriveKey(String dbKey, byte[] salt) throws Exception {
		if (dbKey == null) {
			dbKey = "";
		}
		KeySpec spec = new PBEKeySpec(dbKey.toCharArray(), salt, 60002, 256);
		SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
		return factory.generateSecret(spec).getEncoded();
	}

	private static byte[] hmacSha256(byte[] key, byte[] message) throws Exception {
		javax.crypto.Mac mac = javax.crypto.Mac.getInstance("HmacSHA256");
		mac.init(new SecretKeySpec(key, "HmacSHA256"));
		return mac.doFinal(message);
	}

	private static byte[] decryptAesCtr(byte[] key, byte[] iv, byte[] ciphertext) throws Exception {
		Cipher cipher = Cipher.getInstance("AES/CTR/NoPadding");
		IvParameterSpec ivSpec = new IvParameterSpec(iv);
		cipher.init(Cipher.DECRYPT_MODE, new SecretKeySpec(key, "AES"), ivSpec);
		return cipher.doFinal(ciphertext);
	}

}
