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
 */

package au.gov.naa.digipres.xena.kernel.metadata;

import java.io.IOException;

import org.xml.sax.ContentHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.AttributesImpl;

import au.gov.naa.digipres.xena.core.Xena;
import au.gov.naa.digipres.xena.kernel.XenaInputSource;
import au.gov.naa.digipres.xena.kernel.metadatawrapper.TagNames;
import au.gov.naa.digipres.xena.util.SourceURIParser;

public class XenaMetaData extends AbstractMetaData {

	public static final String XENA_METADATA_NAME = "Xena Meta Data";

	public static final String XENA_VERSION_TAG = "version";
	public static final String NORMALISER_NAME_TAG = "normaliser_name";
	public static final String INPUT_SOURCE_URI_TAG = "input_source_uri";

	public XenaMetaData() {
		super();

		name = XENA_METADATA_NAME;
	}

	@Override
	public void parse(InputSource input) throws IOException, SAXException {
		ContentHandler handler = getContentHandler();
		AttributesImpl att = new AttributesImpl();

		// The Xena metadata tag is handled by the default XML wrapper. 

		// Xena version
		handler.startElement(TagNames.XENA_URI, XENA_VERSION_TAG, XENA_VERSION_TAG, att);
		handler.characters(Xena.getVersion().toCharArray(), 0, Xena.getVersion().length());
		handler.endElement(TagNames.XENA_URI, XENA_VERSION_TAG, XENA_VERSION_TAG);

		// give the class name of the normaliser
		XMLReader normaliser = (XMLReader) getProperty("http://xena/normaliser");
		if (normaliser == null) {
			throw new SAXException("http://xena/normaliser is not set for Package Wrapper");
		}
		handler.startElement(TagNames.XENA_URI, NORMALISER_NAME_TAG, NORMALISER_NAME_TAG, att);
		handler.characters(normaliser.getClass().getName().toCharArray(), 0, normaliser.getClass().getName().length());
		handler.endElement(TagNames.XENA_URI, NORMALISER_NAME_TAG, NORMALISER_NAME_TAG);

		// give the input source uri of the current xis
		XenaInputSource xis = (XenaInputSource) getProperty("http://xena/input");
		handler.startElement(TagNames.XENA_URI, INPUT_SOURCE_URI_TAG, INPUT_SOURCE_URI_TAG, att);

		// TODO: aak - defaultWrapper - This throws a SAXException - is that really necessary?!?
		String xisRelativeSystemId = SourceURIParser.getRelativeSystemId(xis, metaDataManager.getPluginManager());
		handler.characters(xisRelativeSystemId.toCharArray(), 0, xisRelativeSystemId.length());
		handler.endElement(TagNames.XENA_URI, INPUT_SOURCE_URI_TAG, INPUT_SOURCE_URI_TAG);
	}

	@Override
	public String getTag() {
		return TagNames.XENA_META;
	}

}
