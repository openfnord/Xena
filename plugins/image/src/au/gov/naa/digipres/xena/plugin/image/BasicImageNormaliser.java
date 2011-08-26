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

package au.gov.naa.digipres.xena.plugin.image;

import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.xml.sax.ContentHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

import au.gov.naa.digipres.xena.kernel.XenaException;
import au.gov.naa.digipres.xena.kernel.XenaInputSource;
import au.gov.naa.digipres.xena.kernel.normalise.AbstractNormaliser;
import au.gov.naa.digipres.xena.kernel.normalise.NormaliserResults;
import au.gov.naa.digipres.xena.kernel.type.Type;
import au.gov.naa.digipres.xena.kernel.type.UnknownType;
import au.gov.naa.digipres.xena.util.InputStreamEncoder;

/**
 * Normaliser for the core Xena types of PNG and JPEG.
 *
 *
 * AAK: THIS IS VERY POORLY DONE!
 * XXX - image normaliser expects xena input source but only requires input source
 * FIXME - image normaliser expects xena input source but only requires input source
 * In the code, as at october 2005, the parse function needs significant rework.
 *  Of note, the parse excepts an InputSource, and then assumes that it is a xena input source.
 *  There is now basic error handling, however this entire code block needs to be redesigned.
 *  Perhaps all normalisers should implement parse(XenaInputSource xis)  - this would seem BETTER...
 *  
 *
 *
 */
abstract public class BasicImageNormaliser extends AbstractNormaliser {
	public final static String PNG_PREFIX = "png";
	public final static String PNG_TAG = "png";

	public final static String PNG_URI = "http://preservation.naa.gov.au/png/1.0";

	public final static String JPEG_PREFIX = "jpeg";

	public final static String JPEG_URI = "http://preservation.naa.gov.au/jpeg/1.0";

	public final static String DESCRIPTION_TAG_NAME = "description";

	public final static String JPEG_DESCRIPTION_CONTENT =
	    "The following data represents a Base64 encoding of a JPEG image file ( ISO Standard 10918-1 )";

	public final static String PNG_DESCRIPTION_CONTENT =
	    "The following data represents a Base64 encoding of a PNG image file ( ISO Standard 15948 ).";

	public final static String EXTENSION_TAG_NAME = "extension";

	public final static String JPEG_EXTENSION = "jpg";

	public final static String PNG_EXTENSION = "png";

	private Logger logger = Logger.getLogger(this.getClass().getName());

	@Override
	public void parse(InputSource input, NormaliserResults results, boolean convertOnly) throws IOException, SAXException {
		try {
			// TODO: The parse method should ONLY accept xena input sources. The Abstract normaliser should handle this
			// appropriately.
			// ie - this method should be parse(XenaInputSource xis)
			if (!(input instanceof XenaInputSource)) {
				throw new XenaException("Can only normalise XenaInputSource objects.");
			}
			Type type = ((XenaInputSource) input).getType();
			if (type == null) {
				// guess the type!
				try {
					type = normaliserManager.getPluginManager().getGuesserManager().mostLikelyType((XenaInputSource) input);
				} catch (IOException e) {
					// sysout
					logger.log(Level.FINER, "There was an IOException guessing the type.", e);
				}
				if (type == null) {
					type = new UnknownType();
				}
			}

			String prefix;
			String uri;
			String description;
			String extension;
			ContentHandler ch = getContentHandler();
			if (type.equals(normaliserManager.getPluginManager().getTypeManager().lookup(PngFileType.class))) {
				uri = PNG_URI;
				prefix = PNG_PREFIX;
				description = PNG_DESCRIPTION_CONTENT;
				extension = PNG_EXTENSION;
			} else if (type.equals(normaliserManager.getPluginManager().getTypeManager().lookup(JpegFileType.class))) {
				uri = JPEG_URI;
				prefix = JPEG_PREFIX;
				description = JPEG_DESCRIPTION_CONTENT;
				extension = JPEG_EXTENSION;
			} else {
				throw new SAXException("Image Normaliser - not sure about the type");
			}
			AttributesImpl att = new AttributesImpl();
			att.addAttribute(uri, DESCRIPTION_TAG_NAME, prefix + ":" + DESCRIPTION_TAG_NAME, "CDATA", description);
			att.addAttribute(uri, EXTENSION_TAG_NAME, prefix + ":" + EXTENSION_TAG_NAME, "CDATA", extension);

			InputStream is = input.getByteStream();
			ch.startElement(uri, prefix, prefix + ":" + prefix, att);
			InputStreamEncoder.base64Encode(is, ch);
			ch.endElement(uri, prefix, prefix + ":" + prefix);

			// Add the input file checksum as a normaliser property so it can be picked up when we write the metadata. 
			setExportedChecksum(generateChecksum(input.getByteStream()));

		} catch (XenaException x) {
			throw new SAXException(x);
		}
	}

	@Override
	public String getVersion() {
		return ReleaseInfo.getVersion() + "b" + ReleaseInfo.getBuildNumber();
	}

}
