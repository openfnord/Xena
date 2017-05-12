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
 * Created on 15/03/2006 justinw5
 * 
 */
package au.gov.naa.digipres.xena.litegui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Frame;
import java.awt.Toolkit;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;

import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.ScrollPaneConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;

import au.gov.naa.digipres.xena.kernel.IconFactory;

public class SplashScreen {
	private JDialog splashDialog;
	private JTextArea logTextArea;
	private Handler logHandler;

	public SplashScreen(String title, String version) {
		initGUI(title, version);
		logHandler = new SplashLogHandler();
	}

	private void initGUI(String title, String version) {
		JLabel xenaLogoLabel = new JLabel(IconFactory.getIconByName("images/xena-icon.png"));
		xenaLogoLabel.setOpaque(false);
		JLabel headerLabel = new JLabel(IconFactory.getIconByName("images/naaheader-blue.png"));
		JLabel footerLabel = new JLabel(IconFactory.getIconByName("images/naafooter-blue.png"));

		logTextArea = new JTextArea(8, 10);
		logTextArea.setEditable(false);
		logTextArea.setBorder(new EmptyBorder(0, 0, 0, 0));
		logTextArea.setBackground(new Color(255, 255, 255));
		logTextArea.setWrapStyleWord(true);
		logTextArea.setLineWrap(true);
		JScrollPane logSP =
		    new JScrollPane(logTextArea, ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		logSP.setBorder(new EmptyBorder(0, 0, 0, 0));

		JLabel versionLabel = new JLabel(title + " " + version);
		versionLabel.setBackground(logTextArea.getBackground());
		versionLabel.setForeground(logTextArea.getForeground());
		versionLabel.setFont(versionLabel.getFont().deriveFont(Font.BOLD));
		versionLabel.setOpaque(true);

		JPanel textPanel = new JPanel(new BorderLayout());
		textPanel.add(versionLabel, BorderLayout.NORTH);
		textPanel.add(logSP, BorderLayout.CENTER);
		textPanel.setBorder(new LineBorder(logTextArea.getBackground(), 6));

		JPanel infoPanel = new JPanel(new BorderLayout());
		infoPanel.setOpaque(false);
		infoPanel.add(xenaLogoLabel, BorderLayout.WEST);
		infoPanel.add(textPanel, BorderLayout.CENTER);

		JPanel mainPanel = new JPanel(new BorderLayout());
		mainPanel.setBorder(new LineBorder(Color.BLACK));
		mainPanel.setBackground(logTextArea.getBackground());
		mainPanel.add(headerLabel, BorderLayout.NORTH);
		mainPanel.add(infoPanel, BorderLayout.CENTER);
		mainPanel.add(footerLabel, BorderLayout.SOUTH);

		splashDialog = new JDialog((Frame) null, "", false);
		splashDialog.setUndecorated(true);
		splashDialog.add(mainPanel, BorderLayout.CENTER);
		splashDialog.pack();
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		Dimension splashSize = splashDialog.getSize();
		splashDialog.setLocation((screenSize.width - splashSize.width) / 2, (screenSize.height - splashSize.height) / 2);
	}

	public void setVisible(boolean show) {
		splashDialog.setVisible(show);
	}

	public void dispose() {
		splashDialog.dispose();
		splashDialog = null;
	}

	public Handler getLogHandler() {
		return logHandler;
	}

	private class SplashLogHandler extends Handler {
		private boolean handlerClosed = false;

		public SplashLogHandler() {
			setLevel(Level.ALL);
		}

		@Override
		public void publish(LogRecord record) {
			if (!handlerClosed) {
				logTextArea.append(record.getMessage() + "\n");
				logTextArea.setCaretPosition(logTextArea.getDocument().getLength() - 1);
				logTextArea.repaint();
			}
		}

		@Override
		public void flush() {
			logTextArea.validate();
		}

		@Override
		public void close() throws SecurityException {
			handlerClosed = true;
		}

	}

}
