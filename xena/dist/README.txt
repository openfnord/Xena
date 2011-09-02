Xena 6.0.0
==========

Thank you for downloading Xena 6.0.0!


Release Date
------------
??????? Sep 2011


License
-------
Xena is released under the General Public License version 3. A full copy of this license can be found in the accompanying file, 'COPYING.txt'.


Requirements
------------
To run this pre-compiled version of Xena you will require Sun's Java Runtime Environment 1.6, which is available for free at http://java.com.
Xena can also be compiled using the open source Java stack, OpenJDK (http://openjdk.org).

To normalise certain file formats Xena relies on external programs, such as
OpenOffice.org for Office documents. Please see Xena help from inside the
program for more details.


Run Xena
--------
The Xena program itself is called 'xena.jar' and you should be able to start it by either executing the script for your operating system ('xena.bat' for Windows or 'xena.sh' for Linux and other Unix variants).

Alternatively, Windows users who have used the installer should have a shortcut to Xena under the Start Menu.

Else, you can also run Xena manually from the Xena directory, via the command line with:
  java -jar xena.jar
(note that audio playback will not be available when run like this, use xena.bat or xena.sh instead for this)

Please note that if running the script under Linux, first ensure that it is executable with:
  chmod a+x xena.sh

This should kick up the program.
Have fun!


Run Xena Viewer
---------------
Xena files you create will be 'plain text' XML files, with the metadata and content of the original file stored in Base64 encoding.

Use any text editor to view the raw content of the Xena file itself (XML). To view and extract the original or normalised content stored in the Xena files you have created, use the Xena Viewer.

To start the Xena Viewer, simply run the respective execution script for your operating system (i.e. viewer.bat or viewer.sh), similar to the Xena instructions above.

Alternatively, Windows users who have used the installer should have a shortcut to Xena Viewer under ther Start Menu.

Else, you can start it manually from the Xena directory by running the following via the command line:
  java -cp xena.jar au.gov.naa.digipres.xena.viewer.ViewerMainFrame $1

For Windows, run:
  java -cp xena.jar au.gov.naa.digipres.xena.viewer.ViewerMainFrame %1

Have fun!


Xena Plugins
------------
Xena 6.0.0 includes the following plugins:
  archive    v1.3.0
  audio      v1.0.0
  cvs        v2.3.0
  email      v3.3.0
  html       v2.5.0
  image      v5.0.0
  metadata   v1.0.0
  multipage  v1.3.0
  office     v3.5.0
  pdf        v2.4.0
  plaintext  v3.5.0
  project    v2.3.0
  website    v1.0.0
  xml        v2.3.0


Feedback
--------
We would love to hear your feedback on Xena!

Please visit our website at http://xena.sourceforge.net for further details. Thank you!

Support
-------
Help with Xena is available online at our website (http://xena.sourceforge.net) and within the Xena program under the 'Help' menu.
