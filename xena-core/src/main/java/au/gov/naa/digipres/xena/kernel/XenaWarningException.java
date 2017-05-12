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
 * @author Terry O'Neill
 */

package au.gov.naa.digipres.xena.kernel;

/**
 * Exception to represent a blocking event that should be seen by the user as a warning
 */
public class XenaWarningException extends XenaException {
	
	private static final long serialVersionUID = 1L;
	
	/** 
	 * Construct a Xena Warning Exception with the provided message.
	 * @param mesg The error message String to set for the exception.
	 */
	public XenaWarningException(String mesg) {
		super(mesg);
	}

	/**
	 * Construct a Xena Warning Exception with a specific error string and a parent
	 * exception.
	 * 
	 * @param mesg
	 * @param exception
	 */
	public XenaWarningException(String mesg, Exception exception) {
		super(mesg, exception);
	}

	/**
	 * Constructor with parent exception.
	 * @param exception The parent exception that caused this XenaWarningException.
	 */
	public XenaWarningException(Exception exception) {
		super(exception);
	}
}
