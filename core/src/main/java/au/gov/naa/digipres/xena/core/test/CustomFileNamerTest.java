package au.gov.naa.digipres.xena.core.test;

import java.io.File;
import java.io.FileFilter;
import java.text.DecimalFormat;

import au.gov.naa.digipres.xena.core.Xena;
import au.gov.naa.digipres.xena.kernel.XenaException;
import au.gov.naa.digipres.xena.kernel.XenaInputSource;
import au.gov.naa.digipres.xena.kernel.filenamer.AbstractFileNamer;
import au.gov.naa.digipres.xena.kernel.filenamer.FileNamerManager;
import au.gov.naa.digipres.xena.kernel.normalise.AbstractNormaliser;
import au.gov.naa.digipres.xena.kernel.normalise.NormaliserResults;

// This is a custom file namer to test changing the file naming.  It just adds the prefix "myfile-" to the normal naming
public class CustomFileNamerTest {
	//TODO make this into a proper test case
	
	// Update these for testing
	public static File pluginsDir = new File("xena/dist/plugins");
	public static File outputDir = new File("../../Xena/Destination");
	public static File inputFile = new File("../../Xena/XenaTestFiles/Archives/test_png_doc.zip");
	
	public static final String FILE_PREFIX = "myfile";

	public static void main(String[] args) throws Exception {
		Xena xena = new Xena();
		xena.loadPlugins(pluginsDir);
		xena.setActiveFileNamer(new AbstractFileNamer() {
			public String getName() {
				return "My File Namer";
			}
			
			public FileFilter makeFileFilter() {
				return FileNamerManager.DEFAULT_FILE_FILTER;
			}
			
			public File makeNewOpenFile(XenaInputSource input, AbstractNormaliser normaliser, File destinationDir) throws XenaException {
				String extension = "";
				String id = FILE_PREFIX + "-" + input.getFile().getName();

				// Get the extension from the normaliser
				extension = normaliser.getOutputFileExtension();

				// some extensions already have a '.' based on who wrote the plugin, so lets sanitise it. 
				if (extension.startsWith(".")) {
					extension = extension.substring(1);
				}

				File newOpenFile = new File(destinationDir, id + "." + extension);

				// If the file already exists, add an incrementing numerical ID and check again
				int i = 1;
				DecimalFormat idFormatter = new DecimalFormat("0000");
				while (newOpenFile.exists()) {
					newOpenFile = new File(destinationDir, id + "." + idFormatter.format(i) + "." + extension);
					i++;
				}

				return newOpenFile;
			}
			
			public File makeNewXenaFile(XenaInputSource input, AbstractNormaliser normaliser, File destinationDir) throws XenaException {
				if (!destinationDir.exists() || !destinationDir.isDirectory()) {
					throw new XenaException("Could not create new file because there was an error with the destination directory ("
					                        + destinationDir.toString() + ").");
				}
				String fileName = FILE_PREFIX + "-" + input.getFile().getName() + ".xena";
				File newXenaFile = new File(destinationDir, fileName);
				return newXenaFile;
			}
			
			public File makeNewOpenFile(XenaInputSource input, File destinationDir) throws XenaException {
				return new File(destinationDir, FILE_PREFIX + "-" + input.getFile().getName());
			}
		});
		
		NormaliserResults res = xena.normalise(new XenaInputSource(inputFile), outputDir, false);
	}
}
