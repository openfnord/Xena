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

package au.gov.naa.digipres.xena.kernel.guesser;

import java.io.IOException;
import java.io.InputStream;

import au.gov.naa.digipres.xena.kernel.XenaException;
import au.gov.naa.digipres.xena.kernel.XenaInputSource;
import au.gov.naa.digipres.xena.kernel.type.BinaryFileType;
import au.gov.naa.digipres.xena.kernel.type.Type;
import au.gov.naa.digipres.xena.util.XMLCharacterValidator;

/**
 * Guesser for binary (non-character) files.
 *
 */
public class BinaryGuesser extends Guesser {

	private Type type;

	/**
	 * @throws XenaException 
	 * 
	 */
	public BinaryGuesser() {
		super();
	}

	@Override
	public void initGuesser(GuesserManager manager) throws XenaException {
		guesserManager = manager;
		type = getTypeManager().lookup(BinaryFileType.class);
	}

	@Override
	public Guess guess(XenaInputSource source) throws IOException {
		Guess guess = new Guess(type);
		InputStream in = source.getByteStream();
		int c = -1;
		int total = 0;
		while (total < 65536 && 0 <= (c = in.read())) {
			total++;

			// If a "control character" is found, then this file is probably not a plaintext file.
			if (!XMLCharacterValidator.isValidCharacter((char) c)) {
				guess.setDataMatch(GuessIndicator.TRUE);
				break;
			}

		}

		// If no characters were found, this is an empty document, so we want to give priority to the Binary
		// Guesser and stop it being guessed as something else random.
		if (total == 0) {
			guess.setDataMatch(GuessIndicator.TRUE);
		}

		guess.setPriority(GuessPriority.LOW);
		return guess;
	}

	@Override
	public String getName() {
		return "BinaryGuesser";
	}

	@Override
	protected Guess createBestPossibleGuess() {
		Guess bestGuess = new Guess();
		bestGuess.setDataMatch(true);
		return bestGuess;
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
		return new FileTypeDescriptor[0];
	}

}
