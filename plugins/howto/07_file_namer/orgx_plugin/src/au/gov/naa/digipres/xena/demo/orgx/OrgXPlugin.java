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
 */

package au.gov.naa.digipres.xena.demo.orgx;

import java.util.ArrayList;
import java.util.List;

import au.gov.naa.digipres.xena.kernel.filenamer.AbstractFileNamer;
import au.gov.naa.digipres.xena.kernel.plugin.XenaPlugin;

/**
 * @author Justin Waddell
 *
 */
public class OrgXPlugin extends XenaPlugin {

	public static final String ORGX_PLUGIN_NAME = "orgx";

	@Override
	public String getName() {
		return ORGX_PLUGIN_NAME;
	}

	@Override
	public String getVersion() {
		return "0.1";
	}

	@Override
	public List<AbstractFileNamer> getFileNamers() {
		List<AbstractFileNamer> fileNamerList = new ArrayList<AbstractFileNamer>();
		fileNamerList.add(new OrgXFileNamer());
		return fileNamerList;
	}

}
