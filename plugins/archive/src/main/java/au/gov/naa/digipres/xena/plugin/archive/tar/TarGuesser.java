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

package au.gov.naa.digipres.xena.plugin.archive.tar;

import java.io.IOException;

import au.gov.naa.digipres.xena.javatools.FileName;
import au.gov.naa.digipres.xena.kernel.XenaException;
import au.gov.naa.digipres.xena.kernel.XenaInputSource;
import au.gov.naa.digipres.xena.kernel.guesser.FileTypeDescriptor;
import au.gov.naa.digipres.xena.kernel.guesser.Guess;
import au.gov.naa.digipres.xena.kernel.guesser.Guesser;
import au.gov.naa.digipres.xena.kernel.guesser.GuesserManager;
import au.gov.naa.digipres.xena.kernel.type.Type;

/**
 * 
 * created 28/03/2007
 * archive
 * Short desc of class:
 */
public class TarGuesser extends Guesser {
	private static final int TAR_MAGIC_OFFSET = 257;
	private static final int TAR_MAGIC_SIZE = 5;

	// Tar Format
	private static final byte[][] tarMagic = {{'u', 's', 't', 'a', 'r'}};
	private static final String[] tarExtensions = {"tar"};
	private static final String[] tarMime = {"application/tar"};

	private FileTypeDescriptor[] tarFileDescriptors;

	private Type type;

	/**
	 * @throws XenaException 
	 * 
	 */
	public TarGuesser() {
		super();
	}

	@Override
	public void initGuesser(GuesserManager guesserManagerParam) throws XenaException {
		guesserManager = guesserManagerParam;
		type = getTypeManager().lookup(TarFileType.class);
		FileTypeDescriptor[] tempFileDescriptors = {new FileTypeDescriptor(tarExtensions, tarMagic, tarMime, type)};
		tarFileDescriptors = tempFileDescriptors;
	}

	@Override
	public Guess guess(XenaInputSource source) throws IOException {
		FileTypeDescriptor[] descriptorArr = getFileTypeDescriptors();

		Guess guess = new Guess(getType());
		String mimeType = source.getMimeType();

		// get the mime type...
		if (mimeType != null && !mimeType.equals("")) {
			for (FileTypeDescriptor element : descriptorArr) {
				if (element.mimeTypeMatch(mimeType)) {
					guess.setMimeMatch(true);
					break;
				}
			}
		}

		// Get the extension...
		FileName name = new FileName(source.getSystemId());
		String extension = name.extenstionNotNull();

		boolean extMatch = false;
		if (!extension.equals("")) {
			for (FileTypeDescriptor element : descriptorArr) {
				if (element.extensionMatch(extension)) {
					extMatch = true;
					break;
				}
			}
		}
		guess.setExtensionMatch(extMatch);

		// Get the magic number.
		byte[] first = new byte[TAR_MAGIC_OFFSET + TAR_MAGIC_SIZE];
		source.getByteStream().read(first);
		byte[] sourceMagic = new byte[TAR_MAGIC_SIZE];
		System.arraycopy(first, TAR_MAGIC_OFFSET, sourceMagic, 0, sourceMagic.length);
		boolean magicMatch = false;

		for (FileTypeDescriptor element : descriptorArr) {
			if (element.magicNumberMatch(sourceMagic)) {

				magicMatch = true;
				break;
			}
		}
		guess.setMagicNumber(magicMatch);

		return guess;
	}

	@Override
	public String getName() {
		return "TarGuesser";
	}

	@Override
	protected FileTypeDescriptor[] getFileTypeDescriptors() {
		return tarFileDescriptors;
	}

	@Override
	protected Guess createBestPossibleGuess() {
		Guess guess = new Guess();
		guess.setMimeMatch(true);
		guess.setExtensionMatch(true);
		guess.setMagicNumber(true);
		return guess;
	}

	@Override
	public Type getType() {
		return type;
	}
}
