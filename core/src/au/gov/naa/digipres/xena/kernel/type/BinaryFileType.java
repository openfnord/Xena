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

package au.gov.naa.digipres.xena.kernel.type;

/**
 * A type to represent unknown binary file types. In real life it probably means
 * we don't know what type of file it is.
 *
 */
public class BinaryFileType extends FileType {

	public static final String BINARY_FILE_TYPE_NAME = "Binary";

	@Override
	public String getName() {
		return BINARY_FILE_TYPE_NAME;
	}

	@Override
	public String getMimeType() {
		return "application/octet-stream";
	}
}
