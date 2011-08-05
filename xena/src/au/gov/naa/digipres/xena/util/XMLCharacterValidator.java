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
 * Created on 16/02/2006 andrek24
 * 
 */
package au.gov.naa.digipres.xena.util;

import org.apache.xerces.util.XMLChar;

public class XMLCharacterValidator {

	private static final int INVALID_CHARS_ALLOWED_PERCENTAGE = 2;
	private static final int INVALID_CHARS_ALLOWED_MINIMUM = 1;

	/**
	 * Return true if the given character is valid as defined by Character.isDefined,
	 * and is also a character which is allowable in an XML string.
	 * @param c
	 */
	public static boolean isValidCharacter(char c) {
		int intVal = c;
		return Character.isDefined(c) && XMLChar.isValid(intVal);
	}

	/**
	 * Return true if the given character block is probably plaintext. As a rough start we are defining plaintext
	 * as a block of characters containing less than 1% invalid characters, and we'll see how that goes.
	 * 
	 * @param chars characters to test for validity
	 */
	public static boolean isValidBlock(char[] chars, int count) {
		int charsAllowed = count * INVALID_CHARS_ALLOWED_PERCENTAGE / 100;
		if (charsAllowed < INVALID_CHARS_ALLOWED_MINIMUM) {
			charsAllowed = INVALID_CHARS_ALLOWED_MINIMUM;
		}

		int index = 0;
		int invalidCharsFound = 0;
		while (index < count && invalidCharsFound <= charsAllowed) {
			if (!isValidCharacter(chars[index])) {
				invalidCharsFound++;
			}
			index++;
		}

		return invalidCharsFound <= charsAllowed;
	}

	/**
	 * Strip out all characters which are invalid in XML from the given character array.
	 * @param chars input characters
	 * @return the input characters minus any XML-invalid characters
	 */
	public static char[] cleanBlock(char[] chars) {
		StringBuilder cleanedChars = new StringBuilder();

		for (char c : chars) {
			if (isValidCharacter(c)) {
				cleanedChars.append(c);
			}
		}

		return cleanedChars.toString().toCharArray();
	}

}
