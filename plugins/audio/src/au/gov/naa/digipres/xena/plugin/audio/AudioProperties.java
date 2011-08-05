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
 * Created on 14/02/2006 justinw5
 * 
 */
package au.gov.naa.digipres.xena.plugin.audio;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import au.gov.naa.digipres.xena.kernel.properties.InvalidPropertyException;
import au.gov.naa.digipres.xena.kernel.properties.PluginProperties;
import au.gov.naa.digipres.xena.kernel.properties.PropertyMessageException;
import au.gov.naa.digipres.xena.kernel.properties.XenaProperty;

public class AudioProperties extends PluginProperties {
	public static final String AUDIO_PLUGIN_NAME = "Audio";
	public static final String FLAC_LOCATION_PROP_NAME = "Flac Executable Location";

	private List<XenaProperty> properties;

	private Logger logger = Logger.getLogger(this.getClass().getName());

	public AudioProperties() {

	}

	@Override
	public String getName() {
		return AUDIO_PLUGIN_NAME;
	}

	@Override
	public List<XenaProperty> getProperties() {
		return properties;
	}

	@Override
	public void initialiseProperties() {
		properties = new ArrayList<XenaProperty>();

		// fLaC executable property
		XenaProperty flacLocationProperty =
		    new XenaProperty(FLAC_LOCATION_PROP_NAME, "Location of flac executable", XenaProperty.PropertyType.FILE_TYPE, this.getName()) {

			    /**
			     * Ensure selected executable file exists
			     * 
			     * @param newValue
			     * @throws InvalidPropertyException
			     * @throws PropertyMessageException 
			     */
			    @Override
			    public void validate(String newValue) throws InvalidPropertyException, PropertyMessageException {
				    super.validate(newValue);

				    File location = new File(newValue);
				    // Put this back in when Java 6 is used!
				    // if (location == null || !location.exists() || !location.isFile() || !location.canExecute())

				    if (location == null || !location.exists() || !location.isFile()) {
					    throw new InvalidPropertyException("Invalid location for flac!");
				    }
			    }
		    };

		this.getManager().loadProperty(flacLocationProperty);
		properties.add(flacLocationProperty);
	}

}
