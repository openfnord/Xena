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

package au.gov.naa.digipres.xena.plugin.naa;

// SAX classes.
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import au.gov.naa.digipres.xena.kernel.metadatawrapper.AbstractMetaDataUnwrapper;

/**
 * For XML streaming through, strip off the package wrapper.
 *
 */
public class NaaSignedAipUnwrapFilter extends AbstractMetaDataUnwrapper {
	private int contentLevel = 0;

	@Override
	public String toString() {
		return "NAA Package - Unwrapper. Looking for wrapper:signed-aip";
	}

	@Override
	public void startElement(String namespaceURI, String localName, String qName, Attributes atts) throws SAXException {
		if (contentLevel > 0) {
			super.startElement(namespaceURI, localName, qName, atts);
		}

		if (qName.equals(NaaTagNames.PACKAGE_CONTENT)) {
			contentLevel++;
		}
	}

	@Override
	public void endElement(String namespaceURI, String localName, String qName) throws SAXException {
		if (qName.equals(NaaTagNames.PACKAGE_CONTENT)) {
			contentLevel--;

		}

		if (contentLevel > 0) {
			super.endElement(namespaceURI, localName, qName);
		}
	}

	@Override
	public void characters(char[] ch, int start, int length) throws SAXException {
		if (contentLevel > 0) {
			super.characters(ch, start, length);
		}
	}

}
