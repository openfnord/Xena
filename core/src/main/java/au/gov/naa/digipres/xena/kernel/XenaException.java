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

package au.gov.naa.digipres.xena.kernel;

/**
 * Exception class for Xena specific errors. Extends the Exception class.
 * 
 * @version 2.0
 * @see Exception
 */
public class XenaException extends Exception {
	/**
	 * Default serial.
	 */
	private static final long serialVersionUID = 1L;

	/** 
	 * Construct a xena exception with the provided message.
	 * @param mesg The error message String to set for the exception.
	 */
	public XenaException(String mesg) {
		super(mesg);
	}

	/**
	 * Construct a Xean Exception with a specific error string and a parent
	 * exception.
	 * 
	 * @param mesg
	 * @param exception
	 */
	public XenaException(String mesg, Exception exception) {
		super(mesg, exception);
	}

	/**
	 * Constructor with parent exception.
	 * @param exception The parent exception that caused this XenaException.
	 */
	public XenaException(Exception exception) {
		super(exception);
	}
}
