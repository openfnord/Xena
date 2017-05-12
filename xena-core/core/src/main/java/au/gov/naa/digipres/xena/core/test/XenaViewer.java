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
 * Created on 21/11/2005 andrek24
 * 
 * Simple demonstration application to show viewing of Xena Files using the View Factory.
 * 
 * This is written as a demonstration only.
 * 
 * Can view plain text, image and basic file types.
 * 
 * The file to be viewed is hard coded but can be replaced by one specified on the command line.
 */
package au.gov.naa.digipres.xena.core.test;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.util.Vector;

import javax.swing.JFrame;
import javax.swing.JPanel;

import au.gov.naa.digipres.xena.core.NormalisedObjectViewFactory;
import au.gov.naa.digipres.xena.core.Xena;
import au.gov.naa.digipres.xena.kernel.XenaException;

public class XenaViewer extends JFrame {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public XenaViewer(File viewFile) throws XenaException {
		super("XenaTester View");
		addWindowListener(new WindowAdapter() {
			@Override
            public void windowClosing(WindowEvent e) {
				System.exit(0);
			}
		});

		Xena xena = new Xena();
		// create a list of plugin names (these plugins mus exist in the class path)
		Vector<String> pluginList = new Vector<String>();
		pluginList.add("au/gov/naa/digipres/xena/plugin/plaintext");
		pluginList.add("au/gov/naa/digipres/xena/plugin/naa");
		pluginList.add("au/gov/naa/digipres/xena/plugin/image");
		// load the plugins named in the list
		xena.loadPlugins(pluginList);
		// create our view factory
		NormalisedObjectViewFactory novf = new NormalisedObjectViewFactory(xena);
		// create our view panel
		JPanel view = novf.getView(viewFile);
		// set up the content pane and pack our
		setBounds(200, 250, 300, 200);
		getContentPane().add(view);
		pack();
	}

	public static void main(String[] args) {
		// Here's one I prepared earlier!
		String fileName = "d:/xena_data/destination/test.xena";
		// otherwise, try and get the name of the file from the command line
		if (args.length > 0) {
			fileName = args[0];
		}
		// create our viewer!
		try {
			XenaViewer theViewer = new XenaViewer(new File(fileName));
			theViewer.setVisible(true);
		} catch (XenaException xe) {
			System.err.println("An error attempting to create the view.");
			xe.printStackTrace();
			return;
		}
	}
}
