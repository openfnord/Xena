/**
 * This file is part of Xena.
 * 
 * Xena is free software; you can redistribute it and/or modify it under the terms of the GNU General Public License as
 * published by the Free Software Foundation; either version 2 of the License, or (at your option) any later version.
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
 * Created on 8/12/2005 justinw5
 * 
 */
package au.gov.naa.digipres.xena.litegui;

import java.util.EventListener;

/**
 * Simple listener class to enable the normalisation thread to
 * broadcast changes to its state, eg RUNNING, PAUSED, STOPPED.
 * 
 * created 12/12/2005
 * xena
 * Short desc of class:
 */
public interface NormalisationStateChangeListener extends EventListener {
	public void normalisationStateChanged(int newState, int totalItems, int normalisedItems, int errorItems, int warningItems, String currentFile);

	public void normalisationError(String message, Exception e);

}
