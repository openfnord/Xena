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
import au.gov.naa.digipres.xena.kernel.guesser.FileTypeDescriptor;
import au.gov.naa.digipres.xena.kernel.guesser.GuesserManager;
import au.gov.naa.digipres.xena.kernel.type.Type;
import au.gov.naa.digipres.xena.plugin.office.ODFGuesser;

/**
 * Guesser for the the ODS file type (ODF spreadsheet format in later versions of LibreOffice.org)
 * 
 */
public class OdsGuesser extends ODFGuesser {
	private static final String[] odsExtensions = {"ods", "ots"};
	private static final String[] odsMime =
	    {"application/vnd.oasis.opendocument.spreadsheet", "application/vnd.oasis.opendocument.spreadsheet-template"};

	private FileTypeDescriptor[] descriptorArr;

	private Type type;

	/**
	 * @throws XenaException 
	 * 
	 */
	public OdsGuesser() {
		super();
	}

	@Override
	public void initGuesser(GuesserManager guesserManagerParam) throws XenaException {
		guesserManager = guesserManagerParam;
		type = getTypeManager().lookup(OdsFileType.class);
		FileTypeDescriptor[] tempFileDescriptors = {new FileTypeDescriptor(odsExtensions, ZIP_MAGIC, odsMime, type)};
		descriptorArr = tempFileDescriptors;
	}

	@Override
	public String getName() {
		return "ODS Guesser";
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

	/* (non-Javadoc)
	 * @see au.gov.naa.digipres.xena.plugin.office.OOXMLGuesser#getIdentifyingFilename()
	 */
	@Override
	protected String[] getIdentifyingMimetypes() {
		return odsMime;
	}
}
