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

package au.gov.naa.digipres.xena.kernel.view;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.swing.JComponent;
import javax.swing.JMenu;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.sax.SAXTransformerFactory;
import javax.xml.transform.sax.TransformerHandler;
import javax.xml.transform.stream.StreamResult;

import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.ext.LexicalHandler;

import au.gov.naa.digipres.xena.kernel.XenaException;
import au.gov.naa.digipres.xena.kernel.XenaInputSource;

/**
 *
 * @created    January 2005
 */
abstract public class XenaView extends JPanel implements Cloneable {
	/**
	 *  View types
	 */
	public final static int REGULAR_VIEW = 0;

	public final static int THUMBNAIL_VIEW = 1;

	protected int level;

	protected int viewType = REGULAR_VIEW;

	protected Map<JComponent, XenaView> subViewsMap = new HashMap<JComponent, XenaView>();

	protected java.util.List<XenaView> subViewsList = new ArrayList<XenaView>();

	protected XenaView parentView;

	protected BorderLayout borderLayout = new BorderLayout();

	boolean listenersInited = false;

	private XenaInputSource tmpFile;

	protected ViewManager viewManager;

	protected String topTag;

	// The directory of the xena file we are viewing
	protected File sourceDir;

	protected TransformerHandler tempFileHandler = null;

	public XenaView() {
		setLayout(borderLayout);
	}

	/**
	 * @return Returns the viewManager.
	 */
	public ViewManager getViewManager() {
		return viewManager;
	}

	/**
	 * @param viewManager The new value to set viewManager to.
	 */
	public void setViewManager(ViewManager viewManager) {
		this.viewManager = viewManager;
	}

	public String getTopTag() {
		return topTag;
	}

	public void setTopTag(String topTag) {
		this.topTag = topTag;
	}

	public XenaInputSource getTmpFile() {
		return tmpFile;
	}

	public void setTmpFile(XenaInputSource file) {
		tmpFile = file;
	}

	public void rewind() throws SAXException, IOException, XenaException, ParserConfigurationException {
		XMLReader reader = SAXParserFactory.newInstance().newSAXParser().getXMLReader();
		reader.setFeature("http://xml.org/sax/features/namespaces", true);
		reader.setFeature("http://xml.org/sax/features/namespace-prefixes", true);
		// newView.setInternalFrame(oldview.getInternalFrame());
		// XenaInputSource is = new XenaInputSource(getTmpFile(), null);
		XenaInputSource is = getTmpFile();
		reader.setContentHandler(getContentHandler());
		reader.parse(is);
		parse();
		is.close();
	}

	public java.util.List getSubViews() {
		return subViewsList;
	}

	public XenaView getSubView(int n) {
		if (subViewsList.size() <= n) {
			return subViewsList.get(n);
		} else {
			return null;
		}
	}

	public void doClose() {
		Iterator it = subViewsList.iterator();
		while (it.hasNext()) {
			XenaView v = (XenaView) it.next();
			v.doClose();
		}
		close();
	}

	protected void close() {
		if (tmpFile != null) {
			tmpFile.delete();
		}
	}

	public static MouseListener addPopupListener(final JPopupMenu popup, final Component component) {
		MouseListener rtn = null;
		if (popup != null) {
			component.addMouseListener(rtn = new MouseListener() {
				public void mouseClicked(MouseEvent e) {
					checkPopup(e);
				}

				public void mousePressed(MouseEvent e) {
					checkPopup(e);
				}

				public void mouseReleased(MouseEvent e) {
					checkPopup(e);
				}

				public void mouseEntered(MouseEvent e) {
				}

				public void mouseExited(MouseEvent e) {
				}

				private void checkPopup(MouseEvent e) {
					if (e.isPopupTrigger()) {
						popup.show(component, e.getX(), e.getY());
					}
				}
			});
		}
		return rtn;
	}

	public XenaView getSubView(JComponent addToThisComponent) {
		return subViewsMap.get(addToThisComponent);
	}

	public void setSubView(JComponent addToThisComponent, XenaView view) throws XenaException {
		view.setParentView(this);
		view.setSourceDir(sourceDir);
		XenaView oldView = subViewsMap.get(addToThisComponent);
		subViewsMap.put(addToThisComponent, view);
		if (oldView != null) {
			addToThisComponent.remove(oldView);
			oldView.doClose();
			int ind = subViewsList.indexOf(oldView);
			subViewsList.set(ind, view);
		} else {
			subViewsList.add(view);
		}
		addToThisComponent.add(view);
		addToThisComponent.invalidate();
		addToThisComponent.validate();
	}

	public void clearSubViews() {
		subViewsList.clear();
		subViewsMap.clear();
	}

	public void setViewType(int viewType) {
		assert isValidViewType(viewType) : viewType;
		this.viewType = viewType;
	}

	/**
	 *  Mouse listener, implementing popup menus for given components
	 *
	 * @param  viewType  Description of Parameter
	 * @return           MouseListeneer for given component
	 */
	public boolean isValidViewType(int viewType) {
		switch (viewType) {
		case REGULAR_VIEW:
		case THUMBNAIL_VIEW:
			return true;
		default:
			return false;
		}
	}

	public boolean isPrintable() {
		return isPrintable();
	}

	/**
	 * @return    The name value for the view
	 */
	abstract public String getViewName();

	public int getViewType() {
		return viewType;
	}

	public void PrintView() {
	}

	abstract public boolean canShowTag(String tag) throws XenaException;

	/**
	 *  Method to initialise listeners associated with the view
	 *
	 * @exception  XenaException
	 */

	public void initListeners() throws XenaException {
	}

	public void initListenersAndSubViews() throws XenaException {
		if (!listenersInited) {
			initListeners();
		}
		Iterator it = getSubViews().iterator();
		while (it.hasNext()) {
			XenaView subview = (XenaView) it.next();
			subview.initListenersAndSubViews();
		}
		listenersInited = true;
	}

	/**
	 *  Method to construct menus associated with the view
	 *
	 * @param  menu  Description of Parameter
	 */
	public void makeMenu(JMenu menu) {
	}

	/**
	 *  Method to remove listeners from view
	 *
	 * @exception  XenaException  Description of Exception
	 */
	public void removeListeners() throws XenaException {
	}

	/**
	 *  Resolves the view name as a String
	 *
	 * @return    view name as a string
	 */
	@Override
	public String toString() {
		return getViewName();
	}

	public XenaView getParentView() {
		return parentView;
	}

	public void setParentView(XenaView parentView) {
		this.parentView = parentView;
	}

	public int getLevel() {
		return level;
	}

	public void setLevel(int level) {
		this.level = level;
	}

	@Override
	public void updateUI() {
		if (getSubViews() != null) {
			Iterator it = getSubViews().iterator();
			while (it.hasNext()) {
				JComponent c = (JComponent) it.next();
				c.updateUI();
			}
		}
		super.updateUI();
	}

	public void updateViewFromElement() throws XenaException {
	}

	public ContentHandler getContentHandler() throws XenaException {
		return null;
	}

	public LexicalHandler getLexicalHandler() throws XenaException {
		return null;
	}

	private Writer fw;

	public void closeContentHandler() {
		if (fw != null) {
			try {
				fw.close();
				fw = null;
			} catch (IOException x) {
				x.printStackTrace();
			}
		}
	}

	public ContentHandler getTmpMemContentHandler() throws XenaException {
		SAXTransformerFactory tf = (SAXTransformerFactory) TransformerFactory.newInstance();
		TransformerHandler writer = null;
		try {
			writer = tf.newTransformerHandler();
			assert fw == null;
			ByteArrayOutputStream baos;
			baos = new ByteArrayOutputStream();
			Writer fw = new OutputStreamWriter(baos, "UTF-8");
			XenaInputSource is = new au.gov.naa.digipres.xena.kernel.OutputArrayInputSource(baos, null, null, "UTF-8");
			setTmpFile(is);
			StreamResult streamResult = new StreamResult(fw);
			writer.setResult(streamResult);
		} catch (TransformerConfigurationException x) {
			throw new XenaException(x);
		} catch (UnsupportedEncodingException x) {
			throw new XenaException(x);
		}
		return writer;
	}

	public TransformerHandler getTmpFileContentHandler() throws XenaException {
		if (tempFileHandler == null) {
			SAXTransformerFactory tf = (SAXTransformerFactory) TransformerFactory.newInstance();
			try {
				tempFileHandler = tf.newTransformerHandler();
				File file = File.createTempFile("xenaview", ".xenatmp");
				XenaInputSource is = new XenaInputSource(file);
				is.setEncoding("UTF-8");
				setTmpFile(is);
				file.deleteOnExit();
				OutputStream fos = new FileOutputStream(file);
				Writer outputWriter = new OutputStreamWriter(fos, "UTF-8");
				StreamResult streamResult = new StreamResult(outputWriter);
				tempFileHandler.setResult(streamResult);
			} catch (TransformerConfigurationException x) {
				throw new XenaException(x);
			} catch (IOException x) {
				throw new XenaException(x);
			}
		}
		return tempFileHandler;
	}

	public void parse() throws java.io.IOException, org.xml.sax.SAXException, XenaException {
		closeContentHandler();
	}

	/**
	 * @return the sourceDir
	 */
	public File getSourceDir() {
		return sourceDir;
	}

	/**
	 * @param sourceDir the sourceDir to set
	 */
	public void setSourceDir(File sourceDir) {
		this.sourceDir = sourceDir;
	}

	/**
	 * If a number of views are able to display the same tag, priority is used to determine which is displayed by default.
	 * The higher the priority, the more likely it will be the view displayed. A priority of 0 is returned by default.
	 * @return priority of the view
	 */
	public int getPriority() {
		return 0;
	}
}
