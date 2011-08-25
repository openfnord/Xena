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

package au.gov.naa.digipres.xena.plugin.metadata;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import au.gov.naa.digipres.xena.kernel.metadata.AbstractMetaData;
import au.gov.naa.digipres.xena.kernel.plugin.XenaPlugin;
import au.gov.naa.digipres.xena.kernel.properties.PluginProperties;

public class MetadataPlugin extends XenaPlugin {

	public static final String METADATA_PLUGIN_NAME = "metadata";

	/* (non-Javadoc)
	 * @see au.gov.naa.digipres.xena.kernel.plugin.XenaPlugin#getVersion()
	 */
	@Override
	public String getVersion() {
		return ReleaseInfo.getVersion() + "b" + ReleaseInfo.getBuildNumber();
	}

	/* (non-Javadoc)
	 * @see au.gov.naa.digipres.xena.kernel.plugin.XenaPlugin#getName()
	 */
	@Override
	public String getName() {
		return METADATA_PLUGIN_NAME;
	}

	@Override
	public List<PluginProperties> getPluginPropertiesList() {
		List<PluginProperties> propertiesList = new ArrayList<PluginProperties>();
		propertiesList.add(new MetadataProperties());
		return propertiesList;
	}

	@Override
	public List<AbstractMetaData> getMetaDataObjects() {
		List<AbstractMetaData> metadataList = new Vector<AbstractMetaData>();
		metadataList.add(new ExiftoolMetaData());
		return metadataList;
	}

}
