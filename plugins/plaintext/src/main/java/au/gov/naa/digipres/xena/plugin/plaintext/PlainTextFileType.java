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

package au.gov.naa.digipres.xena.plugin.plaintext;

import au.gov.naa.digipres.xena.kernel.type.FileType;

/**
 * Type to represent plaintext files.
 *
 */
public class PlainTextFileType extends FileType {
	public static final String PLAIN_TEXT_FILE_TYPE_NAME = "PlainText";

	@Override
	public String getName() {
		return PLAIN_TEXT_FILE_TYPE_NAME;
	}

	@Override
	public String getMimeType() {
		return "text/plain";
	}

	// We do not want to give everything the .txt extension when exporting.
	// The original extension may have been important and we haven't actually converted the file's structure.
	//	@Override
	//	public String fileExtension() {
	//		return "txt";
	//	}
}
