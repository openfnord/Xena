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

package au.gov.naa.digipres.xena.plugin.image;

import au.gov.naa.digipres.xena.kernel.XenaException;
import au.gov.naa.digipres.xena.kernel.guesser.DefaultGuesser;
import au.gov.naa.digipres.xena.kernel.guesser.FileTypeDescriptor;
import au.gov.naa.digipres.xena.kernel.guesser.GuesserManager;
import au.gov.naa.digipres.xena.kernel.type.Type;

public class PnmGuesser extends DefaultGuesser {

	public static final String PNM_GUESSER_NAME = "PNM Guesser";

	private static final byte[][] pnmMagic = { {'P', '1'}, {'P', '2'}, {'P', '3'}, {'P', '4'}, {'P', '5'}, {'P', '6'}};
	private static final String[] pnmExtensions = {"ppm", "pgm", "pbm", "pnm"};
	private static final String[] pnmMime =
	    {"image/x-portable-pixmap", "image/x-portable-graymap", "image/x-portable-bitmap", "image/x-portable-anymap"};

	private FileTypeDescriptor[] descriptorArr;

	private Type type;

	public PnmGuesser() {
		super();
	}

	@Override
	protected FileTypeDescriptor[] getFileTypeDescriptors() {
		return descriptorArr;
	}

	@Override
	public String getName() {
		return PNM_GUESSER_NAME;
	}

	@Override
	public Type getType() {
		return type;
	}

	@Override
	public void initGuesser(GuesserManager guesserManagerParam) throws XenaException {
		guesserManager = guesserManagerParam;
		type = getTypeManager().lookup(PnmFileType.class);
		FileTypeDescriptor[] tempFileDescriptors = {new FileTypeDescriptor(pnmExtensions, pnmMagic, pnmMime, type)};
		descriptorArr = tempFileDescriptors;
	}

}
