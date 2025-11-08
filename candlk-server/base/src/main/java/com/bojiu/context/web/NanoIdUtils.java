/**
 * Copyright (c) 2017 The JNanoID Authors
 * Copyright (c) 2017 Aventrix LLC
 * Copyright (c) 2017 Andrey Sitnik
 * <p>
 * Permission is hereby granted, free of charge, to any person obtaining a copy of
 * this software and associated documentation files (the "Software"), to deal in
 * the Software without restriction, including without limitation the rights to
 * use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of
 * the Software, and to permit persons to whom the Software is furnished to do so,
 * subject to the following conditions:
 * <p>
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * <p>
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS
 * FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR
 * COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER
 * IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN
 * CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package com.bojiu.context.web;

import java.security.SecureRandom;
import java.util.Random;

import me.codeplayer.util.JavaUtil;

/**
 * A class for generating unique String IDs.
 * <p>
 * The implementations of the core logic in this class are based on NanoId, a JavaScript
 * library by Andrey Sitnik released under the MIT license. (https://github.com/ai/nanoid)
 *
 * @author David Klebanoff
 */
public final class NanoIdUtils {

	/**
	 * <code>NanoIdUtils</code> instances should NOT be constructed in standard programming.
	 * Instead, the class should be used as <code>NanoIdUtils.randomNanoId();</code>.
	 */
	private NanoIdUtils() {
		//Do Nothing
	}

	/**
	 * The default random number generator used by this class.
	 * Creates cryptographically strong NanoId Strings.
	 */
	public static final SecureRandom DEFAULT_NUMBER_GENERATOR = new SecureRandom();

	/**
	 * The default alphabet used by this class.
	 * Creates url-friendly NanoId Strings using 64 unique symbols.
	 */
	public static final byte[] DEFAULT_ALPHABET = JavaUtil.STRING_VALUE.apply("_-0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ");

	/**
	 * The default size used by this class.
	 * Creates NanoId Strings with slightly more unique values than UUID v4.
	 */
	public static final int DEFAULT_SIZE = 21;

	/**
	 * Static factory to retrieve an url-friendly, pseudo randomly generated, NanoId String.
	 * <p>
	 * The generated NanoId String will have 21 symbols.
	 * <p>
	 * The NanoId String is generated using a cryptographically strong pseudo random number
	 * generator.
	 *
	 * @return A randomly generated NanoId String.
	 */
	public static String randomNanoId() {
		return randomNanoId(DEFAULT_NUMBER_GENERATOR, DEFAULT_ALPHABET, DEFAULT_SIZE);
	}

	/**
	 * Static factory to retrieve an url-friendly, pseudo randomly generated, NanoId String.
	 * <p>
	 * The generated NanoId String will have 21 symbols.
	 * <p>
	 * The NanoId String is generated using a cryptographically strong pseudo random number
	 * generator.
	 *
	 * @return A randomly generated NanoId String.
	 */
	public static String randomNanoId(final int size) {
		return randomNanoId(DEFAULT_NUMBER_GENERATOR, DEFAULT_ALPHABET, size);
	}

	/**
	 * Static factory to retrieve a NanoId String.
	 * <p>
	 * The string is generated using the given random number generator.
	 *
	 * @param random The random number generator.
	 * @param alphabet The symbols used in the NanoId String.
	 * @param size The number of symbols in the NanoId String.
	 * @return A randomly generated NanoId String.
	 */
	public static String randomNanoId(final Random random, final char[] alphabet, final int size) {
		if (random == null) {
			throw new IllegalArgumentException("random cannot be null.");
		}

		if (alphabet == null) {
			throw new IllegalArgumentException("alphabet cannot be null.");
		}

		if (alphabet.length == 0 || alphabet.length >= 256) {
			throw new IllegalArgumentException("alphabet must contain between 1 and 255 symbols.");
		}

		if (size <= 0) {
			throw new IllegalArgumentException("size must be greater than zero.");
		}

		final int mask = -1 >>> Integer.numberOfLeadingZeros(alphabet.length - 1);
		final int step = (int) Math.ceil(1.6 * mask * size / alphabet.length);

		final StringBuilder idBuilder = new StringBuilder(size);
		final byte[] bytes = new byte[step];
		int pos = 0;
		while (true) {
			random.nextBytes(bytes);
			for (int i = 0; i < step; i++) {
				final int idx = bytes[i] & mask;
				if (idx < alphabet.length) {
					idBuilder.append(alphabet[idx]);
					if (++pos == size) {
						return idBuilder.toString();
					}
				}
			}
		}
	}

	/**
	 * Static factory to retrieve a NanoId String.
	 * <p>
	 * The string is generated using the given random number generator.
	 *
	 * @param random The random number generator.
	 * @param alphabet The symbols used in the NanoId String.
	 * @param size The number of symbols in the NanoId String.
	 * @return A randomly generated NanoId String.
	 */
	public static String randomNanoId(final Random random, final byte[] alphabet, final int size) {
		if (size <= 0) {
			throw new IllegalArgumentException("size must be greater than zero.");
		}
		if (alphabet.length <= 1 || alphabet.length >= 256) {
			throw new IllegalArgumentException("alphabet must contain between 2 and 255 symbols.");
		}

		final int mask = -1 >>> Integer.numberOfLeadingZeros(alphabet.length - 1);
		final byte[] chars = new byte[size];
		if (alphabet.length == mask + 1) {
			random.nextBytes(chars);
			for (int i = 0; i < size; i++) {
				chars[i] = alphabet[chars[i] & mask];
			}
			return JavaUtil.STRING_CREATOR_JDK11.apply(chars, JavaUtil.LATIN1);
		}
		return fallback(random, alphabet, size, mask, chars);
	}

	private static String fallback(Random random, byte[] alphabet, int size, int mask, byte[] chars) {
		final int step = (int) Math.ceil(1.6 * mask * size / alphabet.length);
		final byte[] buf = new byte[step];
		int pos = 0;
		do {
			random.nextBytes(buf);
			for (int i = 0; i < step; i++) {
				final int idx = buf[i] & mask;
				if (idx < alphabet.length) {
					chars[pos++] = alphabet[idx];
					if (pos == size) {
						return JavaUtil.STRING_CREATOR_JDK11.apply(chars, JavaUtil.LATIN1);
					}
				}
			}
		} while (true);
	}

}