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
 * Created on 11/01/2006 andrek24
 * 
 */
package au.gov.naa.digipres.xena.plugin.office.presentation;

import java.io.IOException;

import au.gov.naa.digipres.xena.kernel.XenaException;
import au.gov.naa.digipres.xena.kernel.XenaInputSource;
import au.gov.naa.digipres.xena.kernel.guesser.FileTypeDescriptor;
import au.gov.naa.digipres.xena.kernel.guesser.Guess;
import au.gov.naa.digipres.xena.kernel.guesser.GuessPriority;
import au.gov.naa.digipres.xena.kernel.guesser.GuesserManager;
import au.gov.naa.digipres.xena.kernel.type.Type;
import au.gov.naa.digipres.xena.plugin.office.MicrosoftOfficeGuesser;

public class PowerpointGuesser extends MicrosoftOfficeGuesser {

	// NOTE: Powerpoint files don't appear to have a CompObj in the header information,
	// and thus the getOfficeTypeString is fairly useless... just putting it in for completeness.
	private static final String POWERPOINT_TYPE_STRING = "Microsoft PowerPoint";
	private static final String POWERPOINT_OFFICE_TYPE_STRING = "Microsoft Office PowerPoint";

	private static final String[] pptExtensions = {"ppt", "pot", "pps"};
	private static final String[] pptMime = {"application/ms-powerpoint"};

	private Type type;

	private FileTypeDescriptor[] fileTypeDescriptors;

	/**
	 * @throws XenaException 
	 * 
	 */
	public PowerpointGuesser() {
		super();
	}

	@Override
	public void initGuesser(GuesserManager guesserManagerParam) throws XenaException {
		guesserManager = guesserManagerParam;
		type = getTypeManager().lookup(PowerpointFileType.class);
		FileTypeDescriptor[] tempFileDescriptors = {new FileTypeDescriptor(pptExtensions, officeMagic, pptMime, type)};
		fileTypeDescriptors = tempFileDescriptors;
	}

	@Override
	public String getName() {
		return "PresentationGuesser";
	}

	@Override
	public Guess guess(XenaInputSource source) throws XenaException, IOException {

		Guess guess = guess(source, type);
		guess.setPriority(GuessPriority.LOW);

		return guess;
	}

	/**
	 * @return Returns the fileTypeDescriptors.
	 */
	@Override
	public FileTypeDescriptor[] getFileTypeDescriptors() {
		return fileTypeDescriptors;
	}

	@Override
	public Type getType() {
		return type;
	}

	@Override
	protected String[] getOfficeTypeStrings() {
		String[] typeStrings = {POWERPOINT_TYPE_STRING, POWERPOINT_OFFICE_TYPE_STRING};
		return typeStrings;
	}

}
