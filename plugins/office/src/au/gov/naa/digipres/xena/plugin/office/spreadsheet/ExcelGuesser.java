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
package au.gov.naa.digipres.xena.plugin.office.spreadsheet;

import java.io.IOException;

import au.gov.naa.digipres.xena.kernel.XenaException;
import au.gov.naa.digipres.xena.kernel.XenaInputSource;
import au.gov.naa.digipres.xena.kernel.guesser.FileTypeDescriptor;
import au.gov.naa.digipres.xena.kernel.guesser.Guess;
import au.gov.naa.digipres.xena.kernel.guesser.GuessPriority;
import au.gov.naa.digipres.xena.kernel.guesser.GuesserManager;
import au.gov.naa.digipres.xena.kernel.type.Type;
import au.gov.naa.digipres.xena.plugin.office.MicrosoftOfficeGuesser;

public class ExcelGuesser extends MicrosoftOfficeGuesser {

	private static final String EXCEL_TYPE_STRING = "Microsoft Excel";
	private static final String EXCEL_OFFICE_TYPE_STRING = "Microsoft Office Excel";

	private static final String[] xlExtensions = {"xls", "xlt", "xlw"};
	private static final String[] xlMime = {"application/ms-excel"};

	private Type type;

	private FileTypeDescriptor[] fileTypeDescriptors;

	/**
	 * @throws XenaException 
	 * 
	 */
	public ExcelGuesser() {
		super();
	}

	@Override
	public void initGuesser(GuesserManager guesserManagerParam) throws XenaException {
		guesserManager = guesserManagerParam;
		type = getTypeManager().lookup(ExcelFileType.class);
		FileTypeDescriptor[] tempFileDescriptors = {new FileTypeDescriptor(xlExtensions, officeMagic, xlMime, type)};
		fileTypeDescriptors = tempFileDescriptors;
	}

	@Override
	public String getName() {
		return "Excel Guesser";
	}

	@Override
	public Guess guess(XenaInputSource source) throws XenaException, IOException {

		Guess guess = guess(source, type);
		guess.setPriority(GuessPriority.DEFAULT);

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
		String[] typeStrings = {EXCEL_TYPE_STRING, EXCEL_OFFICE_TYPE_STRING};
		return typeStrings;
	}

}
