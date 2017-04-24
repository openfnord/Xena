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
 * @author Jeff Stiff
 */

package au.gov.naa.digipres.xena.plugin.plaintext;

// JAXP 1.1
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;

import org.xml.sax.ContentHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

import au.gov.naa.digipres.xena.kernel.CharsetDetector;
import au.gov.naa.digipres.xena.kernel.normalise.AbstractNormaliser;
import au.gov.naa.digipres.xena.kernel.normalise.NormaliserResults;
import au.gov.naa.digipres.xena.util.XMLCharacterValidator;

/**
 * Normalise plaintext documents to Xena plaintext instances.
 *
 */
public class PlainTextToXenaPlainTextNormaliser extends AbstractNormaliser {
	public final static String PLAIN_TEXT_NORMALISER_NAME = "Plaintext";

	final static String PREFIX = "plaintext";

	final static String URI = "http://preservation.naa.gov.au/plaintext/1.0";

	private String tabSizeString = null;

	protected boolean found = false;

	@Override
	public String getName() {
		return PLAIN_TEXT_NORMALISER_NAME;
	}

	public String encoding;

	public void setTabSize(Integer tabSizeInteger) {
		tabSizeString = tabSizeInteger.toString();
	}

	public Integer getTabSizeString() {
		return Integer.valueOf(tabSizeString);
	}

	@Override
	public void parse(InputSource input, NormaliserResults results, boolean convertOnly) throws java.io.IOException, org.xml.sax.SAXException {
		InputStream inputStream = input.getByteStream();
		inputStream.mark(Integer.MAX_VALUE);
		if (input.getEncoding() == null) {
			input.setEncoding(CharsetDetector.guessCharSet(inputStream));
		}
		inputStream.reset();
		ContentHandler contentHandler = getContentHandler();
		AttributesImpl topAttribute = new AttributesImpl();
		AttributesImpl attribute = new AttributesImpl();
		tabSizeString =
		    normaliserManager.getPluginManager().getPropertiesManager()
		            .getPropertyValue(PlainTextProperties.PLUGIN_NAME, PlainTextProperties.TAB_SIZE);

		if (tabSizeString != null) {
			topAttribute.addAttribute(URI, "tabsize", PREFIX + ":" + "tabsize", "CDATA", tabSizeString.toString());
		}
		contentHandler.startElement(URI, "plaintext", PREFIX + ":" + "plaintext", topAttribute);

		// Create a buffered reader
		Reader streamReader = input.getCharacterStream();
		if (streamReader == null) {
			InputStream byteStream = input.getByteStream();
			streamReader = new InputStreamReader(byteStream, input.getEncoding());
		}

		BufferedReader bufferedReader = new BufferedReader(streamReader);
		String linetext = null;
		attribute.clear();
		attribute.addAttribute("http://www.w3.org/XML/1998/namespace", "space", "xml:space", "CDATA", "preserve");

		// here we spec whether we are going by line or char.
		// TODO: aak - my feeling is, if it is guessed at plain text, to hell with it, we just do it this way.
		// the only question is do we add an enclosing tag?
		// XXX - aak - according to field marshal carden, we will go char by char, and put an enclosing tag around bad
		// chars.

		boolean goingByLine = false;
		boolean enclosingTagRoundBadChars = true;

		while ((linetext = bufferedReader.readLine()) != null) {
			contentHandler.startElement(URI, "line", PREFIX + ":" + "line", attribute);
			char[] arr = linetext.toCharArray();
			for (char c : arr) {
				if (goingByLine) {
					// going by line, we just check each char to make sure it is valid.
					if (!XMLCharacterValidator.isValidCharacter(c)) {
						contentHandler.startElement(URI, "line", PREFIX + ":" + "line", attribute);
						throw new SAXException("PlainText normalisation - Cannot use character in XML: 0x" + Integer.toHexString(c)
						                       + ". This is probably not a PlainText file");
					}
				} else {
					// not going by line, we check each char, if valid give it to the content handler, otherwise give
					// the content handler an escaped string with the hex value of our bad char.
					char[] singleCharArray = {c};
					if (c == '\uFEFF') {
						// This is the code point for the Byte Order Mark. We want to skip it, so do nothing.
					} else if (XMLCharacterValidator.isValidCharacter(c)) {
						contentHandler.characters(singleCharArray, 0, singleCharArray.length);
					} else {
						if (enclosingTagRoundBadChars) {
							// write out the bad character from within a tag.
							contentHandler.startElement(URI, "bad_char", PREFIX + ":" + "bad_char", attribute);
							String badCharString = Integer.toHexString(c);
							contentHandler.characters(badCharString.toCharArray(), 0, badCharString.toCharArray().length);
							contentHandler.endElement(URI, "bad_char", PREFIX + ":" + "bad_char");
						} else {
							// write out the bad character escaped...
							String badCharString = "\\" + Integer.toHexString(c);
							contentHandler.characters(badCharString.toCharArray(), 0, badCharString.toCharArray().length);
						}
					}
				}
			}
			// if going by line don't forget to write our line!!!
			if (goingByLine) {
				contentHandler.characters(arr, 0, arr.length);
			}
			contentHandler.endElement(URI, "line", PREFIX + ":" + "line");
		}
		contentHandler.endElement(URI, "plaintext", PREFIX + ":" + "plaintext");

		// As text is one of our accepted formats this file's exported checksum is the same as the original file.
		String checksum = generateChecksum(input.getByteStream());
		setExportedChecksum(checksum);
		setExportedChecksumComment("The export checksum of this file may differ as different operating systems use different line endings.");
	}

	public String getEncoding() {
		return encoding;
	}

	public void setEncoding(String encoding) {
		this.encoding = encoding;
	}

	@Override
	public String getVersion() {
		return ReleaseInfo.getVersion() + "b" + ReleaseInfo.getBuildNumber();
	}

	@Override
	public boolean isConvertible() {
		return false;
	}

	@Override
	public String getOutputFileExtension() {
		return "txt";
	}

}
