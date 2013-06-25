Xena 6.1.0
==========

Thank you for downloading Xena 6.1.0!


Release Date
------------
28 Jul 2013


License
-------
Xena is released under the General Public License version 3. A full copy of this license can be found in the Help pages from within Xena and also in the accompanying file, 'COPYING.txt'.


Requirements
------------
To run this pre-compiled version of Xena you will require Sun's Java Runtime Environment 1.7, which is available for free at http://java.com.
Xena can also be compiled using the open source Java stack, OpenJDK (http://openjdk.java.net).

To normalise certain file formats Xena relies on external programs, such as LibreOffice.org for Office documents. All users will need to install LibreOffice.org separately and configure Xena for its use if functionality for Office documents is needed. Windows users who have installed from the installer and Mac OS users who have installed from the Mac OS Disk Image will find all other external programs included.  Mac OS users will need to configure Xena to use these external programs. Other users will need to download and install required external programs and configure Xena for their use. Please see Xena help from inside the program for more details. Note that where external programs are included they may not be the most up to date versions of these programs.


Run Xena
--------
Windows:
  Windows users who have used the installer will have a shortcut to Xena under the Start Menu.
  Windows users who have installed from source can run the xena.bat script.

Mac OS X:
  Mac OS X users who have installed using the disk image should find Xena in their Applications.  

Linux:
  Xena can be run by executing the xena.sh script.

All:
  The Xena program itself is called 'xena.jar' and can be run using java with the following command (from the directory of the xena.jar file):
    java -jar xena.jar
  (note that audio playback will not be available when run like this, see the scripts xena.bat or xena.sh instead for this)


Xena Output
-----------
The output directory that Xena is using can be found and set from within the Xena Program by going to the Tools menu and choosing Xena Preferences.  It is then shown under the Xena destination directory preference.  Note that for Windows users who have used the installer then the default ouput directory is Xena Output in My Documents.


Run Xena Viewer
---------------
Xena files you create will be 'plain text' XML files containing metadata and the content of the original/converted file stored in Base64 encoding.  The Xena Viewer can be used to view the contents of these files and to extract the contained original or converted file.

Windows:
  Windows users who have used the installer will have a shortcut to the Xena Viewer under the start menu.
  Windows users who have installed from source can run the viewer.bat script.

Mac OS X:
  Mac OS X users will find the Xena Viewer under Xena in their Applications.  Note that currently opening the Xena Viewer from a file (rather than the file from the Xena Viewer) will only open the Xena Viewer and not the file.

Linux:
  The Xena Viewer can be run by executing the viewer.sh script.

All:
  The Xena Viewer program can be run using java with the following command (from the directory of the xena.jar file):
    java -cp xena.jar au.gov.naa.digipres.xena.viewer.ViewerMainFrame


Xena Plugins
------------
Xena 6.1.0 includes the following plugins:
  archive    v1.3.0
  audio      v1.0.0
  cvs        v2.3.0
  email      v3.3.1
  html       v2.5.0
  image      v5.0.0
  metadata   v1.0.0
  multipage  v1.3.0
  office     v3.5.1
  pdf        v2.4.0
  plaintext  v3.5.0
  project    v2.3.1
  website    v1.0.0
  xml        v2.3.0


Support
-------
Help with Xena is available online at our website (http://xena.sourceforge.net) and within the Xena program under the 'Help' menu.


Feedback
--------
We would love to hear your feedback on Xena!

Please visit our website at http://xena.sourceforge.net for further details. Thank you!
