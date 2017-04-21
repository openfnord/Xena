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

/*
 * Created on 14/03/2006 andrek24
 * 
 */
package au.gov.naa.digipres.xena.kernel.metadatawrapper;

import org.xml.sax.SAXException;
import org.xml.sax.ext.LexicalHandler;
import org.xml.sax.helpers.XMLFilterImpl;

import au.gov.naa.digipres.xena.kernel.XenaException;
import au.gov.naa.digipres.xena.kernel.XenaInputSource;

/**
 * 
 * created 24/04/2006
 * xena
 * Short desc of class:
 * 
 * 
 * @see org.xml.sax.XMLFilterImpl
 */
public abstract class AbstractMetaDataWrapper extends XMLFilterImpl implements LexicalHandler {

	protected MetaDataWrapperManager metaDataWrapperManager;
	protected boolean embedded = false;
	protected LexicalHandler outputLexicalHandler;

	public void setMetaDataWrapperManager(MetaDataWrapperManager metaDataWrapperManager) {
		this.metaDataWrapperManager = metaDataWrapperManager;
	}

	public MetaDataWrapperManager getMetaDataWrapperManager() {
		return metaDataWrapperManager;
	}

	public AbstractMetaDataWrapper() {
		super();
	}

	public AbstractMetaDataWrapper(MetaDataWrapperManager metaDataWrapperManager) {
		super();
		this.metaDataWrapperManager = metaDataWrapperManager;
	}

	public void finishNormaliserXMLSection() throws org.xml.sax.SAXException {
		// do nothing
	}

	public abstract String getName();

	public abstract String getOpeningTag();

	public abstract String getSourceId(XenaInputSource input) throws XenaException;

	public abstract String getSourceName(XenaInputSource input) throws XenaException;

	/**
	 * @return the lexicalHandler
	 */
	public LexicalHandler getLexicalHandler() {
		return outputLexicalHandler;
	}

	/**
	 * @param lexicalHandler the lexicalHandler to set
	 */
	public void setLexicalHandler(LexicalHandler lexicalHandler) {
		outputLexicalHandler = lexicalHandler;
	}

	/**
	 * @return the embedded
	 */
	public boolean isEmbedded() {
		return embedded;
	}

	/**
	 * @param embedded the embedded to set
	 */
	public void setEmbedded(boolean embedded) {
		this.embedded = embedded;
	}

	/*
	 ****************************
	 * LEXICAL HANDLER METHODS
	 ****************************
	 */

	/* (non-Javadoc)
	 * @see org.xml.sax.ext.LexicalHandler#comment(char[], int, int)
	 */
	@Override
	public void comment(char[] ch, int start, int length) throws SAXException {
		outputLexicalHandler.comment(ch, start, length);
	}

	/* (non-Javadoc)
	 * @see org.xml.sax.ext.LexicalHandler#endCDATA()
	 */
	@Override
	public void endCDATA() throws SAXException {
		outputLexicalHandler.endCDATA();
	}

	/* (non-Javadoc)
	 * @see org.xml.sax.ext.LexicalHandler#endDTD()
	 */
	@Override
	public void endDTD() throws SAXException {
		outputLexicalHandler.endDTD();
	}

	/* (non-Javadoc)
	 * @see org.xml.sax.ext.LexicalHandler#endEntity(java.lang.String)
	 */
	@Override
	public void endEntity(String name) throws SAXException {
		outputLexicalHandler.endEntity(name);
	}

	/* (non-Javadoc)
	 * @see org.xml.sax.ext.LexicalHandler#startCDATA()
	 */
	@Override
	public void startCDATA() throws SAXException {
		outputLexicalHandler.startCDATA();
	}

	/* (non-Javadoc)
	 * @see org.xml.sax.ext.LexicalHandler#startDTD(java.lang.String, java.lang.String, java.lang.String)
	 */
	@Override
	public void startDTD(String name, String publicId, String systemId) throws SAXException {
		outputLexicalHandler.startDTD(name, publicId, systemId);
	}

	/* (non-Javadoc)
	 * @see org.xml.sax.ext.LexicalHandler#startEntity(java.lang.String)
	 */
	@Override
	public void startEntity(String name) throws SAXException {
		outputLexicalHandler.startEntity(name);
	}

}
