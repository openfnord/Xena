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
 * @author Matthew Oliver
 * @author Jeff Stiff
 */

package au.gov.naa.digipres.xena.plugin.project;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;

import net.sf.mpxj.MPXJException;
import net.sf.mpxj.ProjectFile;
import net.sf.mpxj.mpp.MPPReader;
import net.sf.mpxj.mspdi.MSPDIWriter;

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLFilterImpl;

import au.gov.naa.digipres.xena.kernel.ByteArrayInputSource;
import au.gov.naa.digipres.xena.kernel.XenaInputSource;
import au.gov.naa.digipres.xena.kernel.normalise.AbstractNormaliser;
import au.gov.naa.digipres.xena.kernel.normalise.NormaliserResults;
import au.gov.naa.digipres.xena.util.FileUtils;

/**
 * Normalise Ms Project files to Xena Project files. For pragmatic reasons, the
 * Xena project format is identical to the MS Project XML export format. This is
 * perhaps not ideal, but it would be difficult to design another format.
 *
 */
public class MsProjectToXenaProjectNormaliser extends AbstractNormaliser {
	@Override
	public String getName() {
		return "Microsoft Project";
	}

	@Override
	public void parse(InputSource input, NormaliserResults results, boolean convertOnly) throws IOException, SAXException {
		ProjectFile projFile = null;
		try {
			MPPReader mppReader = new MPPReader();
			projFile = mppReader.read(input.getByteStream());
		} catch (MPXJException x) {
			throw new SAXException(x);
		}
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		MSPDIWriter mspdiWriter = new MSPDIWriter();
		mspdiWriter.write(projFile, baos);
		if (convertOnly) {
			// Copy the ByteArray to the output file
			FileUtils.fileCopy(baos.toByteArray(), results.getDestinationDirString() + File.separator + results.getOutputFileName(), true);
		} else {
			// Process into the Xena file
			XenaInputSource is = new ByteArrayInputSource(baos.toByteArray(), null);

			XMLReader reader = null;
			try {
				reader = SAXParserFactory.newInstance().newSAXParser().getXMLReader();
			} catch (ParserConfigurationException x) {
				throw new SAXException(x);
			}
			XMLFilterImpl filter = new XMLFilterImpl(reader) {
				// Without this our doc gets meta-data wrapped twice.
				@Override
				public void endDocument() {
					// Do nothing
				}

				@Override
				public void startDocument() {
					// Do nothing
				}
			};
			filter.setContentHandler(getContentHandler());
			reader.setContentHandler(filter);
			reader.parse(is);

			// Generate the export checksum of the MSPDI file.
			String checksum = generateChecksum(is.getByteStream());
			setExportedChecksum(checksum);
			setExportedChecksumComment("The export checksum of this file may differ as different operating systems use different line endings.");
		}
	}

	@Override
	public String getVersion() {
		return ReleaseInfo.getVersion() + "b" + ReleaseInfo.getBuildNumber();
	}

	@Override
	public boolean isConvertible() {
		return true;
	}

	@Override
	public String getOutputFileExtension() {
		return "xml";
	}
}
