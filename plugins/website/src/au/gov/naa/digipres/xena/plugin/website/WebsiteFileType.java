/**
 * This file is part of Xena.
 * 
 * Xena is free software; you can redistribute it and/or modify it under the terms of the GNU General Public License as
 * published by the Free Software Foundation; either version 3 of the License, or (at your option) any later version.
 * 
 * Xena is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with website; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 * 
 */

package au.gov.naa.digipres.xena.plugin.website;

import au.gov.naa.digipres.xena.kernel.type.FileType;

/**
 * Type to represent a complete website. The file will be a zipped archive of the website's files,
 * given the custom extension of 'wsx'.
 *  
 * @author Justin Waddell
 *
 */
public class WebsiteFileType extends FileType {

	/* (non-Javadoc)
	 * @see au.gov.naa.digipres.xena.kernel.type.Type#getName()
	 */
	@Override
	public String getName() {
		return "Website";
	}

	@Override
	public String fileExtension() {
		return "wsx";
	}

	@Override
	public String getMimeType() {
		return "application/zip";
	}

}
