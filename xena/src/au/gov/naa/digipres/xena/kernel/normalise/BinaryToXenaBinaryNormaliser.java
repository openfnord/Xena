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
 * @author Matthew Oliver
 * @author Jeff Stiff
 */

package au.gov.naa.digipres.xena.kernel.normalise;

import java.io.IOException;
import java.io.InputStream;

import org.xml.sax.ContentHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

import au.gov.naa.digipres.xena.util.InputStreamEncoder;

/**
 * Normalise a binary file to a Xena binary-object file.
 *
 * @created    1 July 2002
 */
public class BinaryToXenaBinaryNormaliser extends AbstractNormaliser {
	// WARNING: When changing any getters in this class check that these are passed through correctly
	//          to OggAudioNormaliser which may use this class to do the actual normalisation.
	
	public final static String BINARY_NORMALISER_NAME = "Binary";

	final static String PREFIX = "binary-object";
	final static String PROCESS_DESCRIPTION_TAG_NAME = "description";
	final static String DESCRIPTION =
	    "The following data is a MIME-compliant (RFC 2045) PEM base64 (RFC 1421) representation of the original file contents.";

	final static String URI = "http://preservation.naa.gov.au/binary-object/1.0";

	@Override
	public String getName() {
		return BINARY_NORMALISER_NAME;
	}

	@Override
	public void parse(InputSource input, NormaliserResults nr, boolean convertOnly) throws IOException, SAXException {
		AttributesImpl attributes = new AttributesImpl();
		attributes.addAttribute(URI, PROCESS_DESCRIPTION_TAG_NAME, PREFIX + ":" + PROCESS_DESCRIPTION_TAG_NAME, "CDATA", DESCRIPTION);
		ContentHandler ch = getContentHandler();
		InputStream inputStream = input.getByteStream();

		ch.startElement(URI, PREFIX, PREFIX + ":" + PREFIX, attributes);
		InputStreamEncoder.base64Encode(inputStream, ch);
		ch.endElement(URI, PREFIX, PREFIX + ":" + PREFIX);

		// A binary normalised files exported file is the same as the initial file so we can just generate a checksum of the original file.
		String checksum = generateChecksum(input.getByteStream());
		setExportedChecksum(checksum);

	}

	@Override
	public String getOutputFileExtension() {
		// This method is only for the conversion only functionality.  This code should not be called.
		throw new IllegalStateException("Attempt to get the Output File Extension for the Binary Normaliser.  This function is not supported");
	}

	@Override
	public boolean isConvertible() {
		return false;
	}
}
