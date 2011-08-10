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
 * @author Jeff Stiff
 */

/*
 * Created on 28/03/2007 justinw5
 * 
 */
package au.gov.naa.digipres.xena.plugin.archive.gzip;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.GZIPInputStream;

import org.xml.sax.ContentHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import au.gov.naa.digipres.xena.kernel.XenaException;
import au.gov.naa.digipres.xena.kernel.XenaInputSource;
import au.gov.naa.digipres.xena.kernel.normalise.AbstractNormaliser;
import au.gov.naa.digipres.xena.kernel.normalise.NormaliserResults;
import au.gov.naa.digipres.xena.kernel.type.Type;
import au.gov.naa.digipres.xena.plugin.archive.ReleaseInfo;

/**
 * Normaliser for .zip and .jar files
 * 
 * created 28/03/2007
 * archive
 * Short desc of class:
 */
public class GZipNormaliser extends AbstractNormaliser {

	@Override
	public String getName() {
		return "GZip";
	}

	@Override
	public void parse(InputSource input, NormaliserResults results, boolean convertOnly) throws IOException, SAXException {
		ContentHandler contentHandler = getContentHandler();

		// Just decompress the gzip file to a temporary file, and normalise the decompressed file as normal

		// Get temp file. Life is much easier if the original source is a filename. Still need support the case where
		// the original is stream-based though...
		String tempFilename;
		XenaInputSource xis = (XenaInputSource) input;
		if (xis.getFile() == null) {
			tempFilename = ".tmp";
		} else {
			tempFilename = xis.getFile().getName();

			// Remove gzip extension (if it exists). .tgz file should produce a .tar output
			if (tempFilename.toLowerCase().endsWith(".gzip") || tempFilename.toLowerCase().endsWith(".gz")) {
				tempFilename = tempFilename.substring(0, tempFilename.lastIndexOf("."));
			} else if (tempFilename.toLowerCase().endsWith(".tgz")) {
				tempFilename = tempFilename.substring(0, tempFilename.lastIndexOf(".")) + ".tar";
			}
		}

		File extractedTempFile = File.createTempFile("gzip", tempFilename);
		extractedTempFile.deleteOnExit();
		FileOutputStream tempFileOS = new FileOutputStream(extractedTempFile);
		GZIPInputStream gzStream = new GZIPInputStream(input.getByteStream());

		// 10k buffer
		byte[] buffer = new byte[10 * 1024];

		// Extract compressed file
		int bytesRead = gzStream.read(buffer);
		while (bytesRead > 0) {
			tempFileOS.write(buffer, 0, bytesRead);
			bytesRead = gzStream.read(buffer);
		}

		// Close streams
		gzStream.close();
		tempFileOS.flush();
		tempFileOS.close();

		XenaInputSource extractedXis = new XenaInputSource(extractedTempFile);

		try {
			// Get the type and associated normaliser for this entry
			Type fileType = normaliserManager.getPluginManager().getGuesserManager().mostLikelyType(extractedXis);

			extractedXis.setType(fileType);
			AbstractNormaliser entryNormaliser = normaliserManager.lookup(fileType);

			// Set up the normaliser and wrapper for this entry
			entryNormaliser.setProperty("http://xena/url", extractedXis.getSystemId());
			entryNormaliser.setContentHandler(contentHandler);
			entryNormaliser.setProperty("http://xena/file", getProperty("http://xena/file"));
			entryNormaliser.setProperty("http://xena/normaliser", entryNormaliser);
			entryNormaliser.setProperty("http://xena/input", extractedXis);

			// IF this is a convertOnly, we need to set the Output File correctly in xis
			// and results now we know what the GZip contained
			if (convertOnly) {
				// Change the file extension
				String extension = "";

				// Get the extension from the normaliser
				extension = entryNormaliser.getOutputFileExtension();

				// Switch the extension
				String outName = results.getOutputFileName();
				// Trim the .gzip that we placed on the string earlier
				outName = outName.substring(0, outName.lastIndexOf("."));
				// Set the name
				results.setOutputFileName(outName + "." + extension);
				extractedXis.setOutputFileName(outName + "." + extension);

			}

			// Normalise the entry
			entryNormaliser.parse(extractedXis, results, convertOnly);
		} catch (XenaException ex) {
			throw new SAXException("Problem normalising the compressed file contained within a GZIP archive", ex);
		}
	}

	@Override
	public String getVersion() {
		return ReleaseInfo.getVersion() + "b" + ReleaseInfo.getBuildNumber();
	}

	@Override
	public boolean isConvertible() {
		// While the archive is not strictly convertible, the files within may be
		return true;
	}

	@Override
	public String getOutputFileExtension() {
		return "gzip";
	}

}
