This directory contains executables for third-party applications that are used by Xena.

The provided executables are not suitable for all Apple Macintosh distributions and may result in errors.  You may be required to download and install these programs separately if this is the case.  Also note that the included versions are not necessarily the most up to date versions.  It may be worth considering updating to the latest versions if you have problems.

The versions included here are:
flac         version 1.2.1
readpst      version 0.6.44
tesseract    version 3.0.1

At the time of writing of this readme there is no disk image for Xena 6.1.0.

If following the instructions from the Xena 6.0.1 disk image you should also have:
exiftool     version 8.6.3    /usr/bin/exiftool
ImageMagick  version 6.5.8    /ImageMagick-6.5.8/bin/convert

New versions may be found at the following locations:
flac         https://xiph.org/flac/download.html
readpst      http://www.five-ten-sg.com/libpst/                  *
tesseract    http://code.google.com/p/tesseract-ocr/             *
exiftool     http://www.sno.phy.queensu.ca/~phil/exiftool/
imagemagick  http://imagemagick.org/script/binary-releases.php   **

* Note that regular users may find these difficult to install as they may require building from source (which will require the installation of XCode)

** For ImageMagick please note that convert is the only functionality used by Xena although ImageMagick also has other functions.  Also note that if you wish to upgrade ImageMagick you may find that the ImageMagick has only a single pre-built binary package for Mac OS.  If this package is not suitable for your machine they recommend building from source.  This method requires the installation of XCode and MacPorts and may not be suitable for all users.  In such a case you may find it worth trying the installation provided by CactusLab under www.cactuslab.com/imagemagick/.  This version is for 6.8.6-3 rather than the most recent version but is a much simpler install process.