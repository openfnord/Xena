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

package au.gov.naa.digipres.xena.plugin.xml;

import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import au.gov.naa.digipres.xena.kernel.XenaException;
import au.gov.naa.digipres.xena.kernel.XenaInputSource;
import au.gov.naa.digipres.xena.kernel.guesser.FileTypeDescriptor;
import au.gov.naa.digipres.xena.kernel.guesser.Guess;
import au.gov.naa.digipres.xena.kernel.guesser.Guesser;
import au.gov.naa.digipres.xena.kernel.guesser.GuesserManager;
import au.gov.naa.digipres.xena.kernel.type.Type;

/**
 * Guesser for Xena files.
 *
 */
public class XenaGuesser extends Guesser {

	private Type type;

	/**
	 * @throws XenaException 
	 * 
	 */
	public XenaGuesser() {
		super();
	}

	@Override
	public void initGuesser(GuesserManager guesserManagerParam) throws XenaException {
		guesserManager = guesserManagerParam;
		type = getTypeManager().lookup(XenaXmlFileType.class);
	}

	@Override
	public Guess guess(XenaInputSource source) {
		Guess guess = new Guess(type);

		String topXmlTag = getTag(source);
		if (topXmlTag != null) {
			try {
				guess.setType(getTypeManager().lookup("Xena-" + topXmlTag));
				// Need that little bit more certainty of being like a magic number
				guess.setMagicNumber(true);
				guess.setDataMatch(true);
			} catch (XenaException x) {
				guess.setPossible(true);
			}
		}

		return guess;
	}

	private String getTag(InputSource is) {
		try {
			SAXParser parser = SAXParserFactory.newInstance().newSAXParser();
			parser.parse(is, new DefaultHandler() {
				@Override
				public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
					// Bail out early as soon as we've found what we want
					// for super efficiency.
					String tag = qName;
					if (tag == null || tag.equals("")) {
						tag = localName;
					}
					throw new SAXException(tag, new FoundException(tag));
				}
			});
		} catch (SAXException e) {
			if (e.getException() instanceof FoundException) {
				return e.getMessage();
			}
			// Nothing
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	class FoundException extends RuntimeException {
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		FoundException(String tag) {
			this.tag = tag;
		}

		String tag;
	}

	@Override
	public String getName() {
		return "XenaGuesser";
	}

	@Override
	protected Guess createBestPossibleGuess() {
		Guess guess = new Guess();
		guess.setMagicNumber(true);
		guess.setDataMatch(true);
		guess.setPossible(true);
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
		return new FileTypeDescriptor[0];
	}
}
