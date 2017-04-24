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
 * Created on 09/05/2007 justinw5
 * 
 */
package au.gov.naa.digipres.xena.plugin.naa.unsupported;

import au.gov.naa.digipres.xena.kernel.normalise.BinaryToXenaBinaryNormaliser;

/**
 * This class is used to mark files as unsupported by the NAA, and just binary normalise them.
 * This will enable them to be more easily found at a later stage and renormalised when an appropriate normaliser exists.
 * created 09/05/2007
 * naa
 * Short desc of class:
 */
public class UnsupportedTypeNormaliser extends BinaryToXenaBinaryNormaliser {

	@Override
	public String getName() {
		return "Unsupported Type Normaliser";
	}

}
