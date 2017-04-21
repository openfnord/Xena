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

package au.gov.naa.digipres.xena.plugin.office.spreadsheet;

import au.gov.naa.digipres.xena.kernel.XenaException;
import au.gov.naa.digipres.xena.kernel.guesser.DefaultGuesser;
import au.gov.naa.digipres.xena.kernel.guesser.FileTypeDescriptor;
import au.gov.naa.digipres.xena.kernel.guesser.GuesserManager;
import au.gov.naa.digipres.xena.kernel.type.Type;

/**
 * Guesser for the SXC file type (spreadsheet format in early versions of LibreOffice.org)
 * 
 */
public class SxcGuesser extends DefaultGuesser {
	private static byte[][] sxcMagic = {{0x50, 0x4B, 0x03, 0x04, 0x14, 0x00}};
	private static final String[] sxcExtensions = {"sxc"};
	private static final String[] sxcMime = {"application/vnd.sun.xml.calc"};

	private FileTypeDescriptor[] descriptorArr;

	private Type type;

	/**
	 * @throws XenaException 
	 * 
	 */
	public SxcGuesser() {
		super();
	}

	@Override
	public void initGuesser(GuesserManager guesserManagerParam) throws XenaException {
		guesserManager = guesserManagerParam;
		type = getTypeManager().lookup(SxcFileType.class);
		FileTypeDescriptor[] tempFileDescriptors = {new FileTypeDescriptor(sxcExtensions, sxcMagic, sxcMime, type)};
		descriptorArr = tempFileDescriptors;
	}

	@Override
	public String getName() {
		return "SXC Guesser";
	}

	@Override
	public Type getType() {
		return type;
	}

	/**
	 * @see au.gov.naa.digipres.xena.kernel.guesser.Guesser#getFileTypeDescriptors()
	 */
	@Override
	protected FileTypeDescriptor[] getFileTypeDescriptors() {
		return descriptorArr;
	}

}
