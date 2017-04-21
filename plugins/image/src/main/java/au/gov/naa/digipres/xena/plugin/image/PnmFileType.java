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

package au.gov.naa.digipres.xena.plugin.image;

import au.gov.naa.digipres.xena.kernel.type.FileType;

public class PnmFileType extends FileType {

	public static final String PNM_NAME = "PNM";
	public static final String PNM_MIME = "image/x-portable-pixmap";

	@Override
	public String getName() {
		return PNM_NAME;
	}

	@Override
	public String getMimeType() {
		return PNM_MIME;
	}

}
