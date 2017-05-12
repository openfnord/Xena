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
import java.util.List;
import java.util.Map;
import java.util.Vector;
import java.util.logging.Logger;

import org.xml.sax.ContentHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import au.gov.naa.digipres.xena.kernel.XenaException;
import au.gov.naa.digipres.xena.kernel.plugin.PluginManager;

/**
 * Manager for XMLReader instances. These are primarily used to insert extra metadata into a xena file.
 * 
 * @see org.xml.sax.XMLReader
 */
public class MetaDataManager {
	public final static String META_DATA_PREF_NAME = "MetaData";

	// built in metadata objects
	public final static String DEFAULT_METADATA_NAME = "Default Meta Data";
	public final static String XENA_METADATA_NAME = "Xena Meta Data";

	private final Map<String, AbstractMetaData> metadataMap = new HashMap<String, AbstractMetaData>();
	private final Logger logger = Logger.getLogger(this.getClass().getName());
	private PluginManager pluginManager;

	private AbstractMetaData xenaMetaData;
	private AbstractMetaData defaultMetaData;

	public MetaDataManager(PluginManager pluginManager) {
		this.pluginManager = pluginManager;

		// set create the default and xena metadata objects.
		xenaMetaData = new XenaMetaData();
		xenaMetaData.setMetaDataManager(this);
		defaultMetaData = new DefaultMetaData();
		defaultMetaData.setMetaDataManager(this);

	}

	/**
	 * Parses all the MetaData objects to generate all the meta data.
	 * NOTE: This does not call the Xena or the Default MetaData objects, only the MetaData objects from plugins.
	 * @param handler The contentHandler used
	 * @param xis The input source we normalised
	 * @throws XenaException
	 */
	public void parseMetaDataObjects(ContentHandler handler, InputSource xis) throws XenaException {
		for (String key : metadataMap.keySet()) {
			AbstractMetaData metaData = metadataMap.get(key);
			metaData.setContentHandler(handler);
			try {
				metaData.parse(xis);
			} catch (IOException e) {
				throw new XenaException(e);
			} catch (SAXException e) {
				throw new XenaException(e);
			}
		}
	}

	public void addMetaDataObjects(List<AbstractMetaData> metaDataList) {
		for (AbstractMetaData metaData : metaDataList) {
			metadataMap.put(metaData.getName(), metaData);
			metaData.setMetaDataManager(this);
		}
	}

	public void addMetaDataObject(AbstractMetaData metaData) {
		metadataMap.put(metaData.getName(), metaData);
	}

	public AbstractMetaData getMetaData(String name) {
		return metadataMap.get(name);
	}

	public AbstractMetaData getXenaMetaData() {
		return xenaMetaData;
	}

	public AbstractMetaData getDefaultMetaData() {
		return defaultMetaData;
	}

	public PluginManager getPluginManager() {
		return pluginManager;
	}

	public void setPluginManager(PluginManager pluginManager) {
		this.pluginManager = pluginManager;
	}

	public List<String> getMetaDataTags() {
		List<String> metaDataTags = new Vector<String>();

		metaDataTags.add(getXenaMetaData().getTag());
		metaDataTags.add(getDefaultMetaData().getTag());

		for (AbstractMetaData item : metadataMap.values()) {
			metaDataTags.add(item.getTag());
		}

		return metaDataTags;
	}

}
