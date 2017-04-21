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

/*
 * Created on 16/02/2006 andrek24
 * 
 */
package au.gov.naa.digipres.xena.plugin.csv;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;

import org.xml.sax.ContentHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

import au.gov.naa.digipres.xena.kernel.CharsetDetector;
import au.gov.naa.digipres.xena.kernel.normalise.AbstractNormaliser;
import au.gov.naa.digipres.xena.kernel.normalise.NormaliserResults;
import au.gov.naa.digipres.xena.util.XMLCharacterValidator;

public class CsvToXenaCsvNormaliser extends AbstractNormaliser {

	public final static String URI = "http://preservation.naa.gov.au/dataset/1.0";

	public final static String PREFIX = "csv";

	public CsvToXenaCsvNormaliser() {
		// Nothing to do
	}

	@Override
	public String getName() {
		// TODO Auto-generated method stub
		return "CSV";
	}

	@Override
	public void parse(InputSource input, NormaliserResults results, boolean convertOnly) throws IOException, SAXException {
		InputStream is = input.getByteStream();
		is.mark(Integer.MAX_VALUE);
		if (input.getEncoding() == null) {
			try {
				input.setEncoding(CharsetDetector.guessCharSet(is));
			} catch (IOException iox) {
				input.setEncoding("US-ASCII");
			}
		}
		is.reset();
		ContentHandler contentHandler = getContentHandler();
		AttributesImpl topAttribute = new AttributesImpl();
		topAttribute
		        .addAttribute(URI, "description", PREFIX + ":description", "CDATA", "The following elements each represent a line of a CSV file.");
		AttributesImpl attribute = new AttributesImpl();
		contentHandler.startElement(URI, "csv", PREFIX + ":csv", topAttribute);
		BufferedReader br = new BufferedReader(input.getCharacterStream());
		String linetext = null;
		attribute.clear();
		attribute.addAttribute("http://www.w3.org/XML/1998/namespace", "space", "xml:space", null, "preserve");
		while ((linetext = br.readLine()) != null) {
			contentHandler.startElement(URI, "line", PREFIX + ":line", attribute);
			char[] arr = linetext.toCharArray();
			for (char c : arr) {
				if (!XMLCharacterValidator.isValidCharacter(c)) {
					throw new SAXException("CSV normalisation - Cannot use character in XML: 0x" + Integer.toHexString(c)
					                       + ". This is probably not a CSV file.");
				}
			}
			contentHandler.characters(arr, 0, arr.length);
			contentHandler.endElement(URI, "line", PREFIX + ":line");
		}
		contentHandler.endElement(URI, "csv", PREFIX + ":csv");

		// Add the input file checksum as a normaliser property so it can be picked up when we write the metadata. 
		setExportedChecksum(generateChecksum(input.getByteStream()));
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
		return "csv";
	}

}
