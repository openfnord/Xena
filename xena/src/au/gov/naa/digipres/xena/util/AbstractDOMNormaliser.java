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

package au.gov.naa.digipres.xena.util;

import java.io.IOException;

import nu.xom.Element;

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import au.gov.naa.digipres.xena.kernel.normalise.AbstractNormaliser;
import au.gov.naa.digipres.xena.kernel.normalise.NormaliserResults;

/**
 * JDOM Normalisers may find it convenient to use this abstract class.
 * Allows normalisation using JDOM. This should be avoided because SAX is
 * more efficient and works with bigger files. However JDOM can be more
 * convenient to work with.
 */
abstract public class AbstractDOMNormaliser extends AbstractNormaliser {
	@Override
	public void parse(InputSource input, NormaliserResults results, boolean convertOnly) throws java.io.IOException, org.xml.sax.SAXException {
		// Call the JDOM normalisation procedure
		Element el = normalise(input);
		// Now write the tree as if it was SAX events.
		DOMUtil.writeElement(getContentHandler(), getLexicalHandler(), el);

		// Add the input file checksum as a normaliser property so it can be picked up when we write the metadata. 
		setExportedChecksum(generateChecksum(input.getByteStream()));
	}

	/**
	 * Subclasses should override this method and return the normalised result
	 * in the form of a JDOM Element tree.
	 * @param input InputSource corresponding to the raw data stream
	 * @return Element Normalised result tree
	 * @throws IOException read/write error
	 * @throws SAXException other error
	 */
	abstract public Element normalise(InputSource input) throws IOException, SAXException;
}
