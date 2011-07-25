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
 * Created on 28/10/2005 andrek24
 * 
 */

package au.gov.naa.digipres.xena.kernel.normalise;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import au.gov.naa.digipres.xena.core.Xena;
import au.gov.naa.digipres.xena.kernel.XenaException;
import au.gov.naa.digipres.xena.kernel.XenaInputSource;
import au.gov.naa.digipres.xena.kernel.XenaWarningException;
import au.gov.naa.digipres.xena.kernel.filenamer.AbstractFileNamer;
import au.gov.naa.digipres.xena.kernel.metadatawrapper.AbstractMetaDataWrapper;
import au.gov.naa.digipres.xena.kernel.metadatawrapper.MetaDataWrapperManager;
import au.gov.naa.digipres.xena.kernel.metadatawrapper.MetaDataWrapperPlugin;
import au.gov.naa.digipres.xena.kernel.type.Type;
import au.gov.naa.digipres.xena.kernel.type.UnknownType;

/**
 * 
 * <p>
 * created 31/03/2006
 * </p>
 * <p>
 * xena
 * </p>
 * <p>
 * This class encapsulates the results of the normalisation process for a Xena
 * Input source.
 * </p>
 * <p>
 * When a XenaInputSource is to be normalised, a number of things may happen to
 * it:
 * <ul>
 * <li>It is normalised properly.</li>
 * <li>There is a problem during normalisation.</li>
 * <li>It is found to be a 'child' of another XenaInputSource</li>
 * </ul>
 * 
 * <p>
 * When a XenaInputSource is normalised correctly, in the
 * <code>normalised</code> flag will be set to <code>true</code>. In this
 * case, the following information is set in the results object:
 * <ul>
 * <li>normaliser information</li>
 * <li>the version of Xena</li>
 * <li>the type of the XenaInputSource</li>
 * <li>FileNamer</li>
 * <li>MetaDataWrapper</li>
 * <li>Xena ID</li>
 * </ul>
 * </p>
 * <p>
 * If an error occurs during normalising, then the normalised flag should be set
 * to false. In this case, if an exception has been thrown or an error condition
 * has arisen, these should be appended to the <code>errorList</code> or
 * <code>exceptionList</code> as appropriate. If this normaliser results
 * object corresponds to a XenaInputSource that will be embedded in the output
 * of another XenaInputSource when it is normalised, then this will be reflected
 * in the results object, by setting the <code>isChild</code> flag to true,
 * and setting the <code>parentSystemId</code>.
 * 
 * 
 * 
 */

public class NormaliserResults {

	private String xenaVersion = Xena.getVersion();

	private boolean normalised = false;

	private String normaliserName;

	private String inputSystemId;

	private Date inputLastModified;

	private Type inputType;

	private String outputFileName;

	private String destinationDirString;

	private AbstractFileNamer fileNamer;

	private String wrapperName;

	private String id;

	private String normaliserClassName = null;

	private String normaliserVersion;

	private boolean isChild = false;

	private boolean sourceIsOpenFormat = false;

	private String parentSystemId;

	private List<String> errorList = new ArrayList<String>();

	private List<Exception> exceptionList = new ArrayList<Exception>();
	
	private List<String> warningList = new ArrayList<String>();

	private List<NormaliserResults> childAIPResults = new ArrayList<NormaliserResults>();

	private List<NormaliserResults> dataObjectComponentResults = new ArrayList<NormaliserResults>();

	private boolean isMigrateOnly = false;

	/**
	 * Default Constructor - initialise values to null, unknown, or false.
	 * 
	 */
	public NormaliserResults() {
		normalised = false;
		normaliserName = null;
		inputSystemId = null;
		inputType = new UnknownType();
		fileNamer = null;
		wrapperName = null;
		id = null;
	}

	/**
	 * Constructor with XenaInputSource. Initialise results to default values.
	 */
	public NormaliserResults(XenaInputSource xis) {
		normalised = false;
		normaliserName = null;
		inputSystemId = xis.getSystemId();
		inputLastModified = xis.getLastModified();
		inputType = new UnknownType();
		fileNamer = null;
		wrapperName = null;
		id = null;
	}

	/**
	 * Constructor containing values to set results to. Fields still initialised
	 * as required.
	 * 
	 * @param xis
	 * @param normaliser
	 * @param destinationDir
	 * @param fileNamer
	 * @param wrapper
	 */
	public NormaliserResults(XenaInputSource xis, AbstractNormaliser normaliser, File destinationDir, AbstractFileNamer fileNamer,
	                         AbstractMetaDataWrapper wrapper) {
		normalised = false;
		normaliserName = normaliser.getName();
		normaliserVersion = normaliser.getVersion();
		normaliserClassName = normaliser.getClass().getName();
		inputSystemId = xis.getSystemId();
		inputType = xis.getType();
		inputLastModified = xis.getLastModified();
		destinationDirString = destinationDir.getAbsolutePath();
		this.fileNamer = fileNamer;
		wrapperName = wrapper.getName();
		id = null;
	}

	/**
	 * Return a verbose description of the current normaliser results.
	 * 
	 * @return String representation of these results.
	 */
	public String getResultsDetails() {
		if (normalised) {
			return "Normalisation successful." + System.getProperty("line.separator") + "The input source name " + inputSystemId
			       + System.getProperty("line.separator") + "normalised to: " + outputFileName + System.getProperty("line.separator")
			       + "with normaliser: \"" + normaliserName + "\"" + System.getProperty("line.separator") + "to the folder: " + destinationDirString
			       + System.getProperty("line.separator") + "and the Xena id is: " + id;
		} else if (exceptionList.size() != 0) {
			return "The following exceptions were registered: " + getStatusDetails();
		} else {
			if (inputSystemId != null) {
				return inputSystemId + " is NOT normalised, and no exceptions have been registered.";
			}
		}
		return "This results object is not initialised yet.";
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "Results for " + getInputSystemId();
	}

	/**
	 * @return Returns the inputSystemId.
	 */
	public String getInputSystemId() {
		return inputSystemId;
	}

	/**
	 * @param inputSystemId
	 *            The inputSystemId to set.
	 */
	public void setInputSystemId(String inputSystemId) {
		this.inputSystemId = inputSystemId;
	}

	/**
	 * @return Returns the inputType.
	 */
	public Type getInputType() {
		return inputType;
	}

	/**
	 * @param inputType
	 *            The inputType to set.
	 */
	public void setInputType(Type inputType) {
		this.inputType = inputType;
	}

	/**
	 * @return Returns the normalised flag.
	 */
	public boolean isNormalised() {
		return normalised;
	}

	/**
	 * @param normalised
	 *            Set the normalised flag.
	 */
	public void setNormalised(boolean normalised) {
		this.normalised = normalised;
	}

	/**
	 * @return Returns the normaliser.
	 */
	public String getNormaliserName() {
		return normaliserName;
	}

	/**
	 * @param normaliser
	 *            The normaliser to set.
	 */
	public void setNormaliser(AbstractNormaliser normaliser) {
		normaliserName = normaliser.getName();
		normaliserVersion = normaliser.getVersion();
		normaliserClassName = normaliser.getClass().getName();
	}

	public void setNormaliserName(String normaliserName) {
		this.normaliserName = normaliserName;
	}

	/**
	 * @return Returns the outputFileName.
	 */
	public String getOutputFileName() {
		return outputFileName;
	}

	/**
	 * @param outputFileName
	 *            The outputFileName to set.
	 */
	public void setOutputFileName(String outputFileName) {
		this.outputFileName = outputFileName;
	}

	/**
	 * @return Returns the destinationDirString.
	 */
	public String getDestinationDirString() {
		return destinationDirString;
	}

	/**
	 * @param destinationDirString
	 *            The destinationDirString to set.
	 */
	public void setDestinationDirString(String destinationDirString) {
		this.destinationDirString = destinationDirString;
	}

	/**
	 * @param e
	 *            The exception to add to these results (as an exception that has occurred when attempting to normalise).
	 */
	public void addException(Exception e) {
		exceptionList.add(e);
	}
	
	/**
	 * @param message
	 *            A warning message to add to these results.
	 */
	public void addWarning(String message) {
		warningList.add(message);
	}
	
	/**
	 * @return Returns true if there are any exceptions or errors associated with these results else false
	 */
	public boolean hasError() {
		return !(exceptionList.isEmpty() && errorList.isEmpty());
	}
	
	/**
	 * @return Returns true if there are any warnings associated with these results else false
	 */
	public boolean hasWarning() {
		return !warningList.isEmpty();
	}

	/**
	 * @return Returns a String containing exceptions, errors and warnings associated with these results.
	 *         Each item occurs on a new line although individual items may be spread over more than one
	 *         line.  Exceptions contain trace information (which is shown over multiple lines).
	 */
	public String getStatusDetails() {
		// find all our exception messages
		StringBuffer exceptions = new StringBuffer();
		for (Exception e : exceptionList) {
			exceptions.append(e.getMessage() + "\n");

			StackTraceElement[] steArr = e.getStackTrace();
			if (steArr.length > 0) {
				exceptions.append("Trace:\n");
				for (StackTraceElement element : steArr) {
					exceptions.append(element.toString() + "\n");
				}
			}
		}

		// find all our error messages
		StringBuffer errors = new StringBuffer();
		for (String errorMesg : errorList) {
			errors.append(errorMesg + "\n");
		}
		
		// find all our warning messages
		StringBuffer warnings = new StringBuffer();
		for (String warningMesg : warningList) {
			warnings.append(warningMesg + "\n");
		}
		
		StringBuffer returnStringBuffer = new StringBuffer();

		if (exceptions.length() != 0) {
			returnStringBuffer.append(exceptions + "\n");
		}
		if (errors.length() != 0) {
			returnStringBuffer.append(errors + "\n");
		}
		if (warnings.length() != 0) {
			returnStringBuffer.append(warnings);
		}
		return new String(returnStringBuffer);
	}

	/**
	 * @return Returns the message for the first exception, error or warning, or an empty message 
	 * 		   if none of these have occurred.
	 */
	public StatusMessage getStatusMessage() {
		StatusMessage statusMessage = new StatusMessage();
		if (!exceptionList.isEmpty()) {
			statusMessage.setType(StatusMessage.ERROR);
			statusMessage.setMessage(exceptionList.get(0).getMessage());
		} else if (!errorList.isEmpty()) {
			statusMessage.setType(StatusMessage.ERROR);
			statusMessage.setMessage(errorList.get(0));
		} else if (!warningList.isEmpty()) {
			statusMessage.setType(StatusMessage.WARNING);
			statusMessage.setMessage(warningList.get(0));
		} else {
			statusMessage.setMessage("");
		}
		return statusMessage;
	}

	/**
	 * @return Returns the errorList.
	 */
	public List<String> getErrorList() {
		return errorList;
	}

	/**
	 * @return Returns the exceptionList.
	 */
	public List<Exception> getExceptionList() {
		return exceptionList;
	}
	
	/**
	 * @return Returns the warningList.
	 */
	public List<String> getWarningList() {
		return warningList;
	}

	/**
	 * @return Returns the fileNamer.
	 */
	public AbstractFileNamer getFileNamer() {
		return fileNamer;
	}

	/**
	 * @return Returns the wrapper.
	 */
	public String getWrapperName() {
		return wrapperName;
	}

	/**
	 * @return Returns the id.
	 */
	public String getId() {
		return id;
	}

	/**
	 * @param id
	 *            The id to set.
	 */
	public void setId(String id) {
		this.id = id;
	}

	/**
	 * Get the id of a given file.
	 * to find the Id.
	 * 
	 * @param outputFile
	 * 
	 * @deprecated
	 */
	@Deprecated
	public void initialiseId(File outputFile, MetaDataWrapperManager metaDataWrapperManager) {
		if (wrapperName == null || normalised == false) {
			return;
		}
		MetaDataWrapperPlugin wrapperPlugin = metaDataWrapperManager.getMetaDataWrapperPluginByName(wrapperName);
		if (wrapperPlugin != null) {
			try {
				AbstractMetaDataWrapper xenaWrapper = wrapperPlugin.getWrapper();
				id = xenaWrapper.getSourceId(new XenaInputSource(outputFile));
			} catch (XenaException xe) {
				id = null;
				exceptionList.add(xe);
				errorList.add("Could not get the ID from the normalised file.");
			} catch (FileNotFoundException fnfe) {
				id = null;
				exceptionList.add(fnfe);
				errorList.add("Could not open the normalised file to get the ID.");
			}
		} else {
			id = null;
			errorList.add("Could not get the ID from the normalised file.");
		}
	}

	public void setNormaliserVersion(String normaliserVersion) {
		this.normaliserVersion = normaliserVersion;
	}

	/**
	 * @return Returns the normaliserVersion.
	 */
	public String getNormaliserVersion() {
		return normaliserVersion;
	}

	/**
	 * @return Returns the xenaVersion.
	 */
	public String getXenaVersion() {
		return xenaVersion;
	}

	/**
	 * @return Returns the inputLastModified.
	 */
	public Date getInputLastModified() {
		return inputLastModified;
	}

	/**
	 * @return Returns the isChild.
	 */
	public boolean isChild() {
		return isChild;
	}

	/**
	 * @param isChild
	 *            The new value to set isChild to.
	 */
	public void setChild(boolean isChild) {
		this.isChild = isChild;
	}

	/**
	 * @return Returns the parentSystemId.
	 */
	public String getParentSystemId() {
		return parentSystemId;
	}

	/**
	 * @param parentSystemId
	 *            The new value to set parentSystemId to.
	 */
	public void setParentSystemId(String parentSystemId) {
		this.parentSystemId = parentSystemId;
	}

	/**
	 * @return Returns the childAIPResults.
	 */
	public List<NormaliserResults> getChildAIPResults() {
		return childAIPResults;
	}

	/**
	 * @return Returns the dataObjectComponentResults.
	 */
	public List<NormaliserResults> getDataObjectComponentResults() {
		return dataObjectComponentResults;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.List#add(E)
	 */
	public boolean addChildAIPResult(NormaliserResults o) {
		return childAIPResults.add(o);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.List#add(E)
	 */
	public boolean addDataObjectComponentResult(NormaliserResults o) {
		return dataObjectComponentResults.add(o);
	}

	/**
	 * @return Returns the normaliserClassName.
	 */
	public String getNormaliserClassName() {
		return normaliserClassName;
	}

	/**
	 * @param normaliserClassName The normaliserClassName to set.
	 */
	public void setNormaliserClassName(String normaliserClassName) {
		this.normaliserClassName = normaliserClassName;
	}

	/**
	 * @return the sourceIsOpenFormat
	 */
	public boolean sourceIsOpenFormat() {
		return sourceIsOpenFormat;
	}

	/**
	 * @param sourceIsOpenFormat the sourceIsOpenFormat to set
	 */
	public void setSourceIsOpenFormat(boolean sourceIsOpenFormat) {
		this.sourceIsOpenFormat = sourceIsOpenFormat;
	}

	/**
	 * @return Returns the isMigrateOnly.
	 */
	public boolean isMigrateOnly() {
		return isMigrateOnly;
	}

	/**
	 * @param isMigrateOnly
	 *            The new value to set isMigrateOnly to.
	 */
	public void setMigrateOnly(boolean isMigrateOnly) {
		this.isMigrateOnly = isMigrateOnly;
	}

}
