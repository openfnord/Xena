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
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import org.xml.sax.ContentHandler;
import org.xml.sax.DTDHandler;
import org.xml.sax.EntityResolver;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXNotRecognizedException;
import org.xml.sax.SAXNotSupportedException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

import au.gov.naa.digipres.xena.core.Xena;

public abstract class AbstractMetaData implements XMLReader {
	public static final String ABSTRACT_NAME = "Abstract MetaData";

	protected MetaDataManager metaDataManager;
	protected Map<String, Object> properties = new HashMap<String, Object>();
	protected Logger logger = Logger.getLogger(this.getClass().getName());
	protected String name = ABSTRACT_NAME;

	ContentHandler contentHandler;

	/**
	 * Return the version of Xena for this metedata.
	 * @return
	 */
	public String getVersion() {
		return Xena.getVersion();
	}

	/**
	 * @return Returns the metaDataManager.
	 */
	public MetaDataManager getMetaDataManager() {
		return metaDataManager;
	}

	/**
	 * @return Returns the metaDataManager.
	 */
	public void setMetaDataManager(MetaDataManager metaDataManager) {
		this.metaDataManager = metaDataManager;
	}

	@Override
	public boolean getFeature(String name) throws SAXNotRecognizedException, SAXNotSupportedException {
		throw new java.lang.UnsupportedOperationException("Method getFeature() not yet implemented.");
	}

	@Override
	public void setFeature(String name, boolean value) throws SAXNotRecognizedException, SAXNotSupportedException {
		// We are not going to let external code (eg XOM) change the way our normaliser works, so just log the attempt
		logger.finest("Attempted to set feature " + name + " to " + value + ". At present, this is ignored.");
	}

	@Override
	public Object getProperty(String name) throws SAXNotRecognizedException, SAXNotSupportedException {
		Object rtn = properties.get(name);
		return rtn;
	}

	@Override
	public void setProperty(String name, Object value) throws SAXNotRecognizedException, SAXNotSupportedException {
		properties.put(name, value);
	}

	@Override
	public void setEntityResolver(EntityResolver resolver) {
		// Nothing to do

	}

	@Override
	public EntityResolver getEntityResolver() {
		throw new java.lang.UnsupportedOperationException("Method getEntityResolver() not yet implemented.");
	}

	@Override
	public void setDTDHandler(DTDHandler handler) {
		// Nothing to do

	}

	@Override
	public DTDHandler getDTDHandler() {
		throw new java.lang.UnsupportedOperationException("Method getDTDHandler() not yet implemented.");
	}

	@Override
	public void setContentHandler(ContentHandler handler) {
		contentHandler = handler;

	}

	@Override
	public ContentHandler getContentHandler() {
		return contentHandler;
	}

	@Override
	public void setErrorHandler(ErrorHandler handler) {
		// Nothing to do
	}

	@Override
	public ErrorHandler getErrorHandler() {
		return new DefaultHandler();
	}

	public String getName() {
		return name;
	}

	@Override
	public abstract void parse(InputSource input) throws IOException, SAXException;

	@Override
	public void parse(String systemId) throws IOException, SAXException {
		parse(new InputSource(systemId));
	}

	/**
	 * Return the outter most tag for this peice of MetaData.
	 * @return
	 */
	public abstract String getTag();
}
