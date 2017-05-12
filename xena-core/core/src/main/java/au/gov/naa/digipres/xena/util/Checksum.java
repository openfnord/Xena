/**
 * This file is part of Xena.
 * 
 * Xena is free software; you can redistribute it and/or modify it under the terms of the GNU General Public License as
 * published by the Free Software Foundation; either version 3 of the License, or (at your option) any later version.
 * 
 * Xena is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with Xena; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 */

package au.gov.naa.digipres.xena.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class Checksum {
	/**
	 * Get the checksum for this file. 
	 * Checksum is returned as a hex String
	 * @param algorithm 
	 * @param file
	 * @return
	 * @throws IOException
	 * @throws NoSuchAlgorithmException
	 */

	public static String getChecksum(String algorithm, File file) throws IOException {
		FileInputStream stream = new FileInputStream(file);

		return getChecksum(algorithm, stream);
	}

	/**
	 * Get the checksum for this stream. 
	 * Checksum is returned as a hex String 
	 * @param algorithm
	 * @param stream
	 * @return
	 * @throws IOException
	 */
	public static String getChecksum(String algorithm, InputStream stream) throws IOException {

		byte[] buffer = new byte[1024];
		MessageDigest checksum = null;
		try {
			checksum = MessageDigest.getInstance(algorithm);
		} catch (NoSuchAlgorithmException e) {
			// Should never happen as MD5 is built-in in Java... just throw an IOException
			throw new IOException(e.getMessage());
		}

		int numRead;
		do {
			numRead = stream.read(buffer);
			if (numRead > 0) {
				checksum.update(buffer, 0, numRead);
			}
		} while (numRead != -1);
		stream.close();
		return convertToHex(checksum.digest());
	}

	/**
	 * 
	 * Converts byte array to printable hexadecimal string.
	 *  eg convert created MD5 checksum to MD5 file form.
	 * @param byteArray
	 * @return String representing byte array.
	 */
	private static String convertToHex(byte[] byteArray) {
		/*
		 * ------------------------------------------------------ Converts byte array to printable hexadecimal string.
		 * eg convert created MD5 checksum to MD5 file form. ------------------------------------------------------
		 */
		String s; // work string for single byte translation
		String hexString = ""; // the output string being built

		for (int i = 0; i < byteArray.length; i++) {
			s = Integer.toHexString(byteArray[i] & 0xFF); // mask removes 'ffff' prefix from -ive numbers
			if (s.length() == 1)
				s = "0" + s;
			hexString = hexString + s;
		}
		return hexString;
	}
}
