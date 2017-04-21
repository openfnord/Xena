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
 * Created on 10/01/2006 andrek24
 * 
 */
package au.gov.naa.digipres.xena.kernel.guesser;

import java.io.IOException;

import au.gov.naa.digipres.xena.kernel.XenaException;
import au.gov.naa.digipres.xena.kernel.XenaInputSource;
import au.gov.naa.digipres.xena.kernel.type.Type;
import au.gov.naa.digipres.xena.kernel.type.TypeManager;

public abstract class Guesser implements Comparable<Guesser> {

	protected Guess bestPossibleGuess;
	protected GuesserManager guesserManager;

	public Guesser(GuesserManager guesserManager) {
		this.guesserManager = guesserManager;
		bestPossibleGuess = createBestPossibleGuess();
	}

	public Guesser() {
		bestPossibleGuess = createBestPossibleGuess();
	}

	public abstract void initGuesser(GuesserManager guesserManagerParam) throws XenaException;

	abstract protected FileTypeDescriptor[] getFileTypeDescriptors();

	/**
	 * @return Returns the guesserManager.
	 */
	public GuesserManager getGuesserManager() {
		return guesserManager;
	}

	/**
	 * @param guesserManager The new value to set guesserManager to.
	 */
	public void setGuesserManager(GuesserManager guesserManager) {
		this.guesserManager = guesserManager;
	}

	public TypeManager getTypeManager() {
		return guesserManager.getPluginManager().getTypeManager();
	}

	public abstract String getName();

	public abstract Guess guess(XenaInputSource xenaInputSource) throws XenaException, IOException;

	public abstract Type getType();

	protected abstract Guess createBestPossibleGuess();

	public int getMaximumRanking() {
		return guesserManager.getGuessRanker().getRanking(bestPossibleGuess);
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		boolean retVal = false;
		if (obj instanceof Guesser) {
			Guesser compareGuesser = (Guesser) obj;
			if (compareTo(compareGuesser) == 0) {
				retVal = getName().equals(compareGuesser.getName());
			}
		}
		return retVal;
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Comparable#compareTo(T)
	 */
	public int compareTo(Guesser otherGuesser) {
		int result;
		if (otherGuesser == null) {
			result = 1;
		} else {
			result = getMaximumRanking() - otherGuesser.getMaximumRanking();
			if (result == 0) {
				result = getName().compareTo(otherGuesser.getName());
			}
		}

		return result;
	}

}
