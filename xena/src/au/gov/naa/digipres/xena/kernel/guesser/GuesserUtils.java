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
 * 
 * 
 * @author Andrew Keeling
 * @author Chris Bitmead
 * @author Justin Waddell
 */

/*
 * Created on 11/01/2006 andrek24
 * 
 */
package au.gov.naa.digipres.xena.kernel.guesser;

public class GuesserUtils {

	/**
	 * Returns true if the byte arrays match. The arrays do not have to be the same length; a comparison is
	 * made for each byte in the smaller array.
	 * 
	 * @param b1 the first byte array
	 * @param b2 the second byte array
	 * @return
	 */
	public static boolean compareByteArrays(byte[] b1, byte[] b2) {
		for (int i = 0; i < b2.length && i < b1.length; i++) {
			if (b2[i] != b1[i]) {
				return false;
			}
		}
		return true;
	}

}
