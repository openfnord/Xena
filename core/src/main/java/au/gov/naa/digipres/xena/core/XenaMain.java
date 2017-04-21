/**

- * This file is part of Xena.
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

package au.gov.naa.digipres.xena.core;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;

import au.gov.naa.digipres.xena.kernel.XenaException;
import au.gov.naa.digipres.xena.kernel.XenaInputSource;
import au.gov.naa.digipres.xena.kernel.normalise.NormaliserResults;
import au.gov.naa.digipres.xena.litegui.LiteMainFrame;

/**
 * Main Xena invocation class. This acts as a wrapper around Xena invocation to parse command-line
 * arguments for automated conversion and to display the Xena GUI if no command-line arguments
 * are provided.
 *
 * <p>
 * <em>Note:</em> Command-line functionality is currently minimal and should be expanded. Additionally,
 * this class was developed for in-house purposes and should not be considered production-ready.
 * </p>
 *
 * @author Matt Painter <matthew.painter@archives.govt.nz>
 */
public class XenaMain {

	public static void main(String[] args) throws Exception {

		// If no command-line arguments are provided, assume that the user is wishing to invoke the GUI
		if (args.length == 0) {
			LiteMainFrame liteMainFrame = new LiteMainFrame();
			liteMainFrame.setVisible(true);
			return;
		}

		// Parse command-line options
		XenaMain xenaMain = new XenaMain();
		Options options = xenaMain.constructOptions();
		BasicParser parser = new BasicParser();
		CommandLine commandLine = parser.parse(options, args);

		if (commandLine.hasOption('h') || !(commandLine.hasOption('f') && commandLine.hasOption('o'))) {
			HelpFormatter f = new HelpFormatter();
			f.printHelp("xena [--pluginsDirectory <arg>] [--outputDirectory <arg> --files <args>]", options);
			System.exit(1);
		}

		String[] files = commandLine.getOptionValues("f");
		String destinationPath = commandLine.getOptionValue("outputDirectory");
		String pluginsPath = commandLine.getOptionValue("pluginsDirectory");

		File destinationDirectory = xenaMain.getDestinationDirectory(destinationPath);
		File pluginsDirectory = xenaMain.getPluginsDirectory(pluginsPath);

		// Check if this is a convertOnly run
		boolean convertOnly = commandLine.hasOption('c');

		xenaMain.processNormalisation(files, destinationDirectory, pluginsDirectory, convertOnly);
	}

	/**
	 * Returns a file handle to the Xena plugins directory.
	 *
	 * @param  pluginsPath path to plugins directory
	 * @return file handle to plugins directory
	 */
	private File getPluginsDirectory(String pluginsPath) {

		if ((pluginsPath == null) || (pluginsPath.equals(""))) {
			pluginsPath = System.getProperty("user.dir") + System.getProperty("file.separator") + "plugins";
		}

		// Validate plugins directory
		File pluginsDirectory = new File(pluginsPath);
		if (!pluginsDirectory.exists()) {
			System.err.println("Unable to find plugins directory");
			System.exit(1);
		}

		return pluginsDirectory;
	}

	/**
	 * Returns a file handle to the normalisation destination directory. If the path
	 * references a non-existent directory, the directory is created.
	 *
	 * @param destinationPath path to destination directory
	 * @return file handle to destination directory
	 */
	private File getDestinationDirectory(String destinationPath) {

		// Create the destination directory
		File destinationDirectory = new File(destinationPath);

		if (!destinationDirectory.mkdir()) {
			if (!destinationDirectory.exists() || !destinationDirectory.isDirectory()) {
				System.err.println("Unable to create destination directory. Exiting.");
				System.exit(1);
			}
		}

		return destinationDirectory;
	}

	/**
	 * Perform normalisation on a set of files
	 *
	 * @param files list of files to perform normalisation on
	 * @param destinationDirectory destination directory for normalised files
	 * @param pluginsDirectory directory of Xena plugins
	 * @param convertOnly true if converting source to open format files only
	 */
	private void processNormalisation(String[] files, File destinationDirectory, File pluginsDirectory, boolean convertOnly) throws XenaException,
	        FileNotFoundException, IOException {
		Xena xena = new Xena();
		System.out.println(pluginsDirectory);
		xena.loadPlugins(pluginsDirectory);

		int failureCount = 0;
		for (String file : files) {
			System.out.print(file);
			XenaInputSource xenaInputSource = new XenaInputSource(new File(file));

			// Normalise file using best guess
			NormaliserResults results = xena.normalise(xenaInputSource, destinationDirectory, convertOnly);
			if (!results.isNormalised()) {
				failureCount++;
				System.out.println(" FAIL");
			} else {
				System.out.println(" OK");
			}
		}

		System.out.println("-----------------------");

		if (failureCount > 0) {
			System.out.println("Normalisation failures: " + failureCount);
			System.exit(1);
		} else {
			System.out.println("Normalisation OK");
		}
	}

	/**
	 * Constructs command-line options
	 */
	private static Options constructOptions() {
		Options options = new Options();

		Option option = new Option("f", "file", true, "Input files");
		option.setArgs(Option.UNLIMITED_VALUES);
		options.addOption(option);

		options.addOption("p", "pluginsDirectory", true, "Path to plugins directory");
		options.addOption("o", "outputDirectory", true, "Output directory");
		options.addOption("h", "help", false, "Print usage information");

		options.addOption("c", "convertOnly", false, "Convert source to open format only (do not normalise into xena file)");

		return options;
	}
}
