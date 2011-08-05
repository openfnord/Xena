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

package au.gov.naa.digipres.xena.plugin.project;

import java.io.IOException;

import org.apache.poi.poifs.filesystem.DirectoryEntry;
import org.apache.poi.poifs.filesystem.DocumentEntry;
import org.apache.poi.poifs.filesystem.DocumentInputStream;
import org.apache.poi.poifs.filesystem.OfficeXmlFileException;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;

import au.gov.naa.digipres.xena.javatools.FileName;
import au.gov.naa.digipres.xena.kernel.XenaException;
import au.gov.naa.digipres.xena.kernel.XenaInputSource;
import au.gov.naa.digipres.xena.kernel.guesser.FileTypeDescriptor;
import au.gov.naa.digipres.xena.kernel.guesser.Guess;
import au.gov.naa.digipres.xena.kernel.guesser.Guesser;
import au.gov.naa.digipres.xena.kernel.guesser.GuesserManager;
import au.gov.naa.digipres.xena.kernel.type.Type;

/**
 * Guesser for MS Project files.
 *
 */
public class MsProjectGuesser extends Guesser {

	private static final byte[][] mppMagic =
	    {{(byte) 0xD0, (byte) 0xCF, 0x11, (byte) 0xE0, (byte) 0xA1, (byte) 0xB1, 0x1A, (byte) 0xE1, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
	      0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x3E, 0x00, 0x03, 0x00, (byte) 0xFE, (byte) 0xFF, 0x09, 0x00, 0x06, 0x00, 0x00, 0x00, 0x00,
	      0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00}};
	private static final String[] mppExtensions = {"mpp"};
	private static final String[] mppMime = {"application/msproject"};

	private FileTypeDescriptor[] descriptorArr;

	private Type type;

	/**
	 * @throws XenaException 
	 * 
	 */
	public MsProjectGuesser() {
		super();
	}

	@Override
	public void initGuesser(GuesserManager guesserManagerParam) throws XenaException {
		guesserManager = guesserManagerParam;
		type = getTypeManager().lookup(MsProjectFileType.class);
		FileTypeDescriptor[] tempFileDescriptors = {new FileTypeDescriptor(mppExtensions, mppMagic, mppMime, type)};
		descriptorArr = tempFileDescriptors;
	}

	@Override
	public Guess guess(XenaInputSource source) throws IOException {
		Guess guess = new Guess(type);
		FileName name = new FileName(source.getSystemId());
		String extension = name.extenstionNotNull().toLowerCase();
		if (extension.equals("mpp")) {
			guess.setExtensionMatch(true);
		}

		byte[] first = new byte[44];
		source.getByteStream().read(first);

		boolean magicMatch = false;
		for (FileTypeDescriptor element : getFileTypeDescriptors()) {
			if (element.magicNumberMatch(first)) {
				magicMatch = true;
				break;
			}
		}
		guess.setMagicNumber(magicMatch);

		try {
			POIFSFileSystem fs = new POIFSFileSystem(source.getByteStream());
			DirectoryEntry root = fs.getRoot();

			//
			// Retrieve the CompObj data and validate the file format
			//
			ProjectCompObj compObj = new ProjectCompObj(new DocumentInputStream((DocumentEntry) root.getEntry("\1CompObj")));
			guess.setDataMatch(compObj.isProjectFile());

		} catch (OfficeXmlFileException oxfex) {
			// This is a runtime exception for some reason!
			// We'll just set "possible" to false.
			guess.setPossible(false);
		} catch (IOException x) {
			// an I/O error occurred, or the InputStream did not provide a compatible
			// POIFS data structure
			guess.setPossible(false);
		}

		return guess;
	}

	@Override
	public String getName() {
		return "ProjectGuesser";
	}

	@Override
	protected Guess createBestPossibleGuess() {
		Guess guess = new Guess();
		guess.setMagicNumber(true);
		guess.setDataMatch(true);
		guess.setExtensionMatch(true);
		return guess;
	}

	@Override
	public Type getType() {
		return type;
	}

	/* (non-Javadoc)
	 * @see au.gov.naa.digipres.xena.kernel.guesser.Guesser#getFileTypeDescriptors()
	 */
	@Override
	protected FileTypeDescriptor[] getFileTypeDescriptors() {
		return descriptorArr;
	}
}
