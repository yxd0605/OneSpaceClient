package com.eli.oneos.model.oneos.backup.info.contact;

import android.util.Log;

import java.io.UnsupportedEncodingException;

public class QuotedPrintable {

	private static byte HT = 0x09; // \t
	private static byte LF = 0x0A; // \n
	private static byte CR = 0x0D; // \r

	/**
	 * A method to decode quoted printable encoded data. It overrides the same
	 * input byte array to save memory. Can be done because the result is surely
	 * smaller than the input.
	 * 
	 * @param qp
	 *            a byte array to decode.
	 * @return the length of the decoded array.
	 */
	public static int decode(byte[] qp) {
		int qplen = qp.length;
		int retlen = 0;

		for (int i = 0; i < qplen; i++) {
			if (qp[i] == '=') {
				if (qplen - i > 2) {
					if (qp[i + 1] == CR && qp[i + 2] == LF) {
						i += 2;
						continue;

					} else if (isHexDigit(qp[i + 1]) && isHexDigit(qp[i + 2])) {
						qp[retlen++] = (byte) (getHexValue(qp[i + 1]) * 16 + getHexValue(qp[i + 2]));

						i += 2;
						continue;

					} else {
						Log.i("asd", "decode: Invalid sequence = " + qp[i + 1] + qp[i + 2]);
					}
				}
			}
			if ((qp[i] >= 0x20 && qp[i] <= 0x7f) || qp[i] == HT || qp[i] == CR || qp[i] == LF) {
				qp[retlen++] = qp[i];
			}
		}

		return retlen;
	}

	private static boolean isHexDigit(byte b) {
		return ((b >= 0x30 && b <= 0x39) || (b >= 0x41 && b <= 0x46));
	}

	private static byte getHexValue(byte b) {
		return (byte) Character.digit((char) b, 16);
	}

	/**
	 * 
	 * @param qp
	 *            Byte array to decode
	 * @param enc
	 *            The character encoding of the returned string
	 * @return The decoded string.
	 */
	public static String decode(byte[] qp, String enc) {
		int len = decode(qp);
		try {
			return new String(qp, 0, len, enc);
		} catch (UnsupportedEncodingException e) {
			Log.i("quote", "qp.decode: " + enc + " not supported. " + e.toString());
			return new String(qp, 0, len);
		}
	}
}
