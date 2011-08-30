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
 * This class represents the ImageMagick Convert command.  It is present to provide a common location for the reused
 * ImageCommand for the conversion and to abstract away details of setting up where the ImageMagick convert executable
 * resides.
 * 
 * This is currently just used statically but in the future Convert functionality of the Normalisers may be moved into
 * this class.
 *  
 * @author Terry O'Neill
 */

package au.gov.naa.digipres.xena.plugin.image;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Logger;

import org.im4java.core.CommandException;
import org.im4java.core.ConvertCmd;
import org.im4java.core.IM4JavaException;
import org.im4java.core.IMOperation;
import org.im4java.core.ImageCommand;
import org.im4java.process.ErrorConsumer;
import org.im4java.process.OutputConsumer;

import au.gov.naa.digipres.xena.kernel.XenaException;

public class ImageMagicConverter {
	private static final Logger logger = Logger.getLogger(ImageMagicConverter.class.getCanonicalName());
	
	private static String convertPath = null;
	private static ImageCommand convertCommand = null;
	private static final IMOperation convertOp;
	private static final IMOperation convertOpAlphaOff;
	// message suffix for when path set is invalid.  Note that this has some fairly user friendly type text which really belongs further
	// towards the LiteMainFrame rather than here.  Note also that currently this message text is just displayed as an exception message
	// with stack trace whereas it should really be shown as a nice clean error text.
	private static final String NO_IMAGE_MAGICK_MSG_SUFFIX = "\" is not a valid location for the Image Magick Convert Executable.  " +
			"Please ensure that the Image Magick convert executable is specified in your system path or Xena Plugin Preferences " +
			"(Tools - Plugin Preferences - Image).  Please view the log to find any error output from the specified convert command.";
	// variables for capturing error output to display on gui (all error output is logged regardless of these variables)
	private static boolean captureNextErrOutput = false;
	private static String capturedErrOutput;
	
	static {
		// set up the normal convert operation parameters, which just takes an input image and an output image to convert
		convertOp = new IMOperation();
		convertOp.addImage(2); // convert takes an input image file and an output image file
		// set up the alpha off convert operation parameters, which is just like convertOp but with the alpha parameter set to off
		convertOpAlphaOff = new IMOperation();
		convertOpAlphaOff.alpha("off");
		convertOpAlphaOff.addImage(2); // convert takes an input image file and and output image file
	}
	
	public static void setImageMagicConvertLocation(String path) throws IllegalArgumentException {
		if (convertPath == null || !convertPath.equals(path)) {
			// check if the path is valid by running the convert command with the version option only
			ImageCommand newConvertCommand;
			if (path.isEmpty()) {
				newConvertCommand = new ConvertCmd();
			} else {
				newConvertCommand = new ImageCommand();
				newConvertCommand.setCommand(path);
			}
			setConsumers(newConvertCommand);
			try {
				checkForImageMagickConvert(newConvertCommand);
			} catch (IOException e) {
				throw new IllegalArgumentException("\"" + path + NO_IMAGE_MAGICK_MSG_SUFFIX, e);
			} catch (InterruptedException e) {
				throw new IllegalArgumentException("\"" + path + NO_IMAGE_MAGICK_MSG_SUFFIX, e);
			} catch (IM4JavaException e) {
				throw new IllegalArgumentException("\"" + path + NO_IMAGE_MAGICK_MSG_SUFFIX, e);
			} catch (XenaException e) {
				// throw just passing the XenaException as this already has a descriptive message
				throw new IllegalArgumentException(e);
			}
			convertCommand = newConvertCommand;
			convertPath = path;
		}
	}
	
	private static void checkForImageMagickConvert() throws IOException, InterruptedException, IM4JavaException, XenaException {
		checkForImageMagickConvert(getConvertCommand());
	}
	
	private static void setCapturedErrMsg(String message) {
		// record the error message and disable capturing of error output (it is still logged)
		capturedErrOutput = message;
		captureNextErrOutput = false;
	}
	
	// check for the existence of a valid executable for Convert (of Image Magick)
	private static void checkForImageMagickConvert(ImageCommand convertCmd) throws IOException, InterruptedException, IM4JavaException, XenaException {
		// run the convert command with an operation to get the version.  This is done as the only real exception that should
		// occur in this case is if the executable is not present (note that this presents itself as a CommandException,
		// which is why this step is done separately rather than looking for a CommandException on our actual conversion,
		// for which case a CommandException could indicate other problems)
		// TODO really should look at the output to check that this actually is ImageMagick
		IMOperation getVersion = new IMOperation();
		getVersion.version();
		captureNextErrOutput = true;
		try {
			convertCmd.run(getVersion);
		} catch (CommandException e) {
			// Command Exception should only be caused by the convert executable not being present in this case
			// Provide a nice error message to the user
			logger.warning("not a valid location for the Image Magick Convert Executable: " + e.getMessage());
			String msg = "Image Magick Convert cannot be found or is invalid.  Please ensure that the Image Magick Convert executable is " +
					"specified in your system path or in Xena Plugin Preferences (in Tools - Plugin Preferences - Image).";
			if (capturedErrOutput != null && !capturedErrOutput.isEmpty()) {
				msg += "\n" + capturedErrOutput;
				capturedErrOutput = null;
			}
			throw new XenaException(msg, e);
		} finally {
			// disable the capture for the case in which nothing was captured
			captureNextErrOutput = false;
		}
	}
	
	private static ImageCommand getConvertCommand() throws IOException, InterruptedException, IM4JavaException, XenaException {
		if (convertCommand == null) {
			// no convert command exists create one
			convertCommand = new ConvertCmd(); // this sets the command to just use convert which will only work if this is on the system path
			setConsumers(convertCommand);
			// check to see if it is valid
			checkForImageMagickConvert();
		}
		return convertCommand;
	}
	
	private static void setConsumers(ImageCommand imageCommand) {
		// Create consumers of Error and Output for the conversion process
		// Note that these are essential as without consuming output from the process it is possible for the process to hang
		// (as its buffers for output can fill up)
		// TODO The output from these consumers particularly the error consumer should really be displayed to the user more obviously.
		//      Currently the error consumer output goes to the log, but would be better to have both go to the log and the error consumer
		//      go to an actual error or warning message in the table showing the conversion results).
		imageCommand.setErrorConsumer(new ErrorConsumer() {
			public void consumeError(InputStream pInputStream) throws IOException {
				// Log error output
				// TODO should use buffer rather than doing one character at a time
				String msg = "";
				int nextByte = pInputStream.read();
				while (nextByte != -1) {
					msg += String.valueOf((char) nextByte); // no possibility of a different encoding here
					nextByte = pInputStream.read();
				}
				msg = msg.trim();
				if (msg.length() > 0) {
					msg = "Image Magick error/warning output for conversion:\n" + msg;
					logger.warning(msg); // TODO should really check if this is a warning or an error and use the appropriate call
				}
				// set captured output if necessary (for use in GUI Messages)
				if (captureNextErrOutput) {
					setCapturedErrMsg(msg);
				}
			}
		});
		imageCommand.setOutputConsumer(new OutputConsumer() {
			private final boolean isWindowsOs = System.getProperty("os.name").startsWith("Windows");
			
			public void consumeOutput(InputStream pInputStream) throws IOException {
				if (pInputStream.available() == 0) {
					// Return if there is no available data when running under Windows.  This is a dirty hack to get around a problem
					// where when running under windows the -1 for end of stream when using the read command never occurs.  This looks
					// like it may be some problem in the im4java library but difficult to tell.  The problem with this is that it
					// makes it possible to not get our output if the inputstream has not yet output it.  Based on the im4java code
					// that calls this this seems unlikely in the extreme.
					// Note that this has only been put on the output consumer.  Have not had any occurrence of this being necessary
					// for the error consumer thus far although it would seem reasonable to think that it is possible.
					// Note also that it is probably not a big deal if we miss some of this output; the error output is far more
					// important to us.
					// TODO Try and find some way around using this hack
					if (isWindowsOs) {
						return;
					}
				}
				// Log output
				// TODO should use buffer rather than doing one character at a time
				String msg = "";
				int nextByte = pInputStream.read();
				while (nextByte != -1) {
					msg += String.valueOf((char) nextByte); // no possibility of a different encoding here
					nextByte = pInputStream.read();
				}
				msg = msg.trim();
				if (msg.length() > 0) {
					msg = "Image Magick standard output for conversion:\n" + msg;
					logger.finer(msg);
				}		
			}
		});
	}
	
	private static void clearConsumers() {
		convertCommand.setErrorConsumer(null);
		convertCommand.setOutputConsumer(null);
	}
	
	public static void convert(final File inputImage, final File outputImage) throws IOException, InterruptedException, IM4JavaException, XenaException {
		// run the command
		getConvertCommand().run(convertOp, inputImage.getAbsolutePath(), outputImage.getAbsolutePath());
	}
	
	public static void convertAlphaOff(File inputImage, File outputImage) throws IOException, InterruptedException, IM4JavaException, XenaException {
		// run the command
		getConvertCommand().run(convertOpAlphaOff, inputImage.getAbsolutePath(), outputImage.getAbsolutePath());
	}
}