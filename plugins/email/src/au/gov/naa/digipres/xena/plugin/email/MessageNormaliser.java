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

package au.gov.naa.digipres.xena.plugin.email;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.text.SimpleDateFormat;
import java.util.Enumeration;
import java.util.TimeZone;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.mail.BodyPart;
import javax.mail.Header;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Part;
import javax.mail.URLName;

import nu.xom.Element;

import org.xml.sax.ContentHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.ext.LexicalHandler;
import org.xml.sax.helpers.AttributesImpl;

import au.gov.naa.digipres.xena.kernel.ByteArrayInputSource;
import au.gov.naa.digipres.xena.kernel.XenaException;
import au.gov.naa.digipres.xena.kernel.XenaInputSource;
import au.gov.naa.digipres.xena.kernel.metadatawrapper.AbstractMetaDataWrapper;
import au.gov.naa.digipres.xena.kernel.normalise.AbstractNormaliser;
import au.gov.naa.digipres.xena.kernel.normalise.BinaryToXenaBinaryNormaliser;
import au.gov.naa.digipres.xena.kernel.normalise.NormaliserManager;
import au.gov.naa.digipres.xena.kernel.normalise.NormaliserResults;
import au.gov.naa.digipres.xena.kernel.type.Type;
import au.gov.naa.digipres.xena.util.DOMUtil;
import au.gov.naa.digipres.xena.util.UrlEncoder;

/**
 * Normaliser for individual email messages.
 *
 */
public class MessageNormaliser extends AbstractNormaliser {
	public final static String DATE_FORMAT_STRING = "yyyyMMdd'T'HHmmssZ";
	public final static String EMAIL_URI = "http://preservation.naa.gov.au/email/1.0";
	public final static String EMAIL_PREFIX = "email";
	public final static String PART_TAG = "part";
	public final static String FILENAME_ATTRIBUTE = "filename";

	private Message msg;

	private Logger logger;

	MessageNormaliser(Message msg, NormaliserManager normaliserManager) {
		this.msg = msg;
		logger = Logger.getLogger(this.getClass().getName());
		this.normaliserManager = normaliserManager;
	}

	@Override
	public String getName() {
		return "Message";
	}

	AbstractNormaliser lastNormaliser;

	XenaInputSource lastInputSource;

	/**
	 * Note: mail.jar must be earlier in the classpath than the GNU mail providers
	 * otherwise weird things happen with attachments not being recognised.
	 * @param input
	 * @throws java.io.IOException
	 * @throws org.xml.sax.SAXException
	 */
	@SuppressWarnings("unchecked")
	@Override
	public void parse(InputSource input, NormaliserResults results, boolean convertOnly) throws org.xml.sax.SAXException {
		try {
			URLName msgurl = new URLName(input.getSystemId());
			AttributesImpl empty = new AttributesImpl();
			ContentHandler ch = getContentHandler();
			LexicalHandler lexicalHandler = getLexicalHandler();
			ch.startElement(EMAIL_URI, EMAIL_PREFIX, EMAIL_PREFIX + ":email", empty);
			ch.startElement(EMAIL_URI, "headers", EMAIL_PREFIX + ":headers", empty);
			Enumeration<Header> en = msg.getAllHeaders();
			SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT_STRING);
			Pattern tzpat = Pattern.compile(".*([+-][0-9]{4})[^0-9]*");
			while (en.hasMoreElements()) {
				Header head = en.nextElement();
				AttributesImpl hatt = new AttributesImpl();
				hatt.addAttribute(EMAIL_URI, "name", EMAIL_PREFIX + ":name", "CDATA", head.getName());
				ch.startElement(EMAIL_URI, "header", EMAIL_PREFIX + ":header", hatt);
				String hstring = null;
				if (head.getName().equals("Date") || head.getName().equals("Sent-Date")) {
					Matcher mat = tzpat.matcher(head.getValue());
					sdf.setTimeZone(TimeZone.getDefault());
					if (mat.matches()) {
						TimeZone tz = TimeZone.getTimeZone("GMT" + mat.group(1));
						sdf.setTimeZone(tz);
					}
					hstring = sdf.format(msg.getSentDate());
				} else if (head.getName().equals("Received-Date")) {
					Matcher mat = tzpat.matcher(head.getValue());
					sdf.setTimeZone(TimeZone.getDefault());
					if (mat.matches()) {
						TimeZone tz = TimeZone.getTimeZone("GMT" + mat.group(1));
						sdf.setTimeZone(tz);
					}
					hstring = sdf.format(msg.getReceivedDate());
				} else {
					hstring = head.getValue();
				}
				char[] hvalue = hstring.toCharArray();
				ch.characters(hvalue, 0, hvalue.length);
				ch.endElement(EMAIL_URI, "header", EMAIL_PREFIX + ":header");
			}
			ch.endElement(EMAIL_URI, "headers", EMAIL_PREFIX + ":headers");

			logger.finest("Normalisation successful - " + "input: " + input.getSystemId() + ", " + "subject: " + msg.getSubject());
			ch.startElement(EMAIL_URI, "parts", EMAIL_PREFIX + ":parts", empty);
			Object content = msg.getContent();
			if (content instanceof Multipart) {
				Multipart mp = (Multipart) content;
				for (int j = 0; j < mp.getCount(); j++) {
					BodyPart bp = mp.getBodyPart(j);
					AttributesImpl partatt = new AttributesImpl();
					if (bp.getFileName() != null) {
						partatt.addAttribute(EMAIL_URI, FILENAME_ATTRIBUTE, EMAIL_PREFIX + ":" + FILENAME_ATTRIBUTE, "CDATA", bp.getFileName());
					}
					if (bp.getDescription() != null) {
						partatt.addAttribute(EMAIL_URI, "description", EMAIL_PREFIX + ":description", "CDATA", bp.getDescription());
					}
					ch.startElement(EMAIL_URI, "part", EMAIL_PREFIX + ":" + PART_TAG, partatt);
					Type type = null;
					AbstractNormaliser normaliser = null;
					if (bp.getContent() instanceof Message) {
						Message msgatt = (Message) bp.getContent();
						normaliser = new MessageNormaliser(msgatt, normaliserManager);
						type = normaliserManager.getPluginManager().getTypeManager().lookup(MsgFileType.class);
					}
					Element part = getPart(msgurl, bp, j + 1, (XenaInputSource) input, type, normaliser);

					// TODO - aak 2005/10/06 removed level from wrapTheNormaliser call...
					// ContentHandler wrap = NormaliserManager.singleton().wrapTheNormaliser(lastNormaliser,
					// lastInputSource,log.getLevel() + 1);

					AbstractMetaDataWrapper wrap = normaliserManager.wrapEmbeddedNormaliser(lastNormaliser, lastInputSource, ch, lexicalHandler);

					/*
					 * if (bp instanceof xena.util.trim.TrimAttachment) {
					 * ((XMLReader)wrap).setProperty("http://xena/file", ((xena.util.trim.TrimAttachment)bp).getFile()); }
					 */
					wrap.startDocument();
					DOMUtil.writeElement(wrap, wrap, part);
					wrap.endDocument();
					// }
					ch.endElement(EMAIL_URI, "part", EMAIL_PREFIX + ":" + PART_TAG);
				}
			} else if (content instanceof String || content instanceof InputStream) {
				Element part = getPart(msgurl, msg, 1, (XenaInputSource) input, null, null);
				// TODO - aak 2005/10/06 removed level from wrapTheNormaliser call...
				// ContentHandler wrap = NormaliserManager.singleton().wrapTheNormaliser(lastNormaliser,
				// lastInputSource, log.getLevel() + 1);

				AbstractMetaDataWrapper wrap = normaliserManager.wrapEmbeddedNormaliser(lastNormaliser, lastInputSource, ch, lexicalHandler);

				ch.startElement(EMAIL_URI, "part", EMAIL_PREFIX + ":part", empty);
				wrap.startDocument();
				DOMUtil.writeElement(wrap, wrap, part);
				wrap.endDocument();
				ch.endElement(EMAIL_URI, "part", EMAIL_PREFIX + ":part");
			} else {
				throw new SAXException("Unknown email mime type");
			}
			ch.endElement(EMAIL_URI, "parts", EMAIL_PREFIX + ":parts");
			ch.endElement(EMAIL_URI, EMAIL_PREFIX, EMAIL_PREFIX + ":email");
		} catch (MessagingException x) {
			throw new SAXException(x);
		} catch (MalformedURLException x) {
			throw new SAXException(x);
		} catch (IOException x) {
			throw new SAXException(x);
		} catch (XenaException x) {
			throw new SAXException(x);
		}
	}

	Element getPart(URLName url, Part bp, int n, XenaInputSource parent, Type type, AbstractNormaliser normaliser) throws MessagingException,
	        IOException, XenaException, SAXException {
		AbstractNormaliser localNormaliser = normaliser;
		Type localType = type;
		String nuri = url.toString();
		if (!(bp instanceof Message) || bp.getInputStream() != null) {
			if (bp.getFileName() != null) {
				nuri += "/" + UrlEncoder.encode(bp.getFileName());
			} else {
				nuri += "/" + Integer.toString(n);
			}
		}
		XenaInputSource xis = null;

		// There is a requirement for the meta-data to contain the real source location of the attachment,
		// thus this hack specially for Trim where attachments are separate files.
		// We check for the existence of the file to avoid trying to use a separate file in cases such as
		// embedded emails where there is no separate email.
		//TODO The protection against using a separate file for the attachment should be based on more than just the existence of 
		//     a file with the correct name as this could result in some sort of false positive.  We really should still be trying
		//     to read from the getInputStream and only using a separate file if we cannot use the stream or the stream and the file
		//     contents match up (possibly only testing a small part at the start).
		if (bp.getContent() instanceof Message && new File(nuri).exists()) {
			xis = lastInputSource = new XenaInputSource(nuri, localType);
		} else if (bp instanceof au.gov.naa.digipres.xena.plugin.email.trim.TrimAttachment) {
			xis = lastInputSource = new XenaInputSource(((au.gov.naa.digipres.xena.plugin.email.trim.TrimPart) bp).getFile(), null);
		} else {
			xis = lastInputSource = new ByteArrayInputSource(bp.getInputStream(), null);
			xis.setSystemId(nuri);
		}

		xis.setParent(parent);

		String partContentType = bp.getContentType();
		if (partContentType != null) {
			// Make sure we only have the mime type - for plaintext the character encoding may appear after a ';'
			String mimeType = partContentType;
			int semiColonIndex = mimeType.indexOf(";");
			if (semiColonIndex != -1) {
				mimeType = mimeType.substring(0, semiColonIndex);
				// set the character encoding if one is given
				String attributes = partContentType.substring(semiColonIndex + 1);
				final String charsetParamStr = "charset=";
				int charsetIndex = attributes.indexOf(charsetParamStr);
				if (charsetIndex != -1) {
					int startIndex = charsetIndex + charsetParamStr.length();
					int endIndex;
					if (attributes.charAt(startIndex) == '"') {
						startIndex++;
						endIndex = attributes.indexOf('"', startIndex);
						if (endIndex != -1) {
							xis.setEncoding(attributes.substring(startIndex, endIndex));
						}
					} else {
						endIndex = attributes.indexOf(' ', startIndex);
						if (endIndex == -1) {
							xis.setEncoding(attributes.substring(startIndex));
						} else {
							xis.setEncoding(attributes.substring(startIndex, endIndex));
						}
					}
				}
			}
			xis.setMimeType(mimeType);
		}

		if (localType == null) {
			if (xis.getMimeType().startsWith("text/") ||
				xis.getMimeType().startsWith("multipart/") ||
				(xis.getEncoding() != null && ! xis.getEncoding().contains("ascii")))
			{
				//TODO This section is a workaround to data loss due to encoding issues.  Fix the guessing and normalising so that this
				//     workaround is not necessary.
				//
				// Use the PlainText normaliser for all text mimetypes for multipart mime-type or for any situation where
				// the character encoding has been specified and is not ascii.  We do this because errors in other normalisers have been
				// seen to result in data loss due to using the wrong encoding in these cases.  This workaround may still cause issues and
				// will probably not cover all situations of data loss due to using the wrong character encoding.
				//
				// TODO Note that for multipart mimetypes we really should use the bp.getContent and then individually process each type.
				// The problem here is that this code only currently handles a single level of multitype (processed in the parse function)
				// rather than any arbitrary number of levels.
				localType = normaliserManager.getPluginManager().getTypeManager().lookup("PlainText");
				if (localType == null) {
					// could not get the plaintext normaliser, just guess the type
					String logmsg = "Could not find PlainText normaliser for processing mail part in file \"" +
					                xis.getSystemId() + "\" (part mime type = \"" + xis.getMimeType();
					if (xis.getEncoding() != null) {
						logmsg += ", part encoding = \"" + xis.getEncoding();
					}
					logmsg += "\")";
					logger.warning(logmsg);
					localType = normaliserManager.getPluginManager().getGuesserManager().mostLikelyType(xis);
				}
			} else {
				// guess the type
				localType = normaliserManager.getPluginManager().getGuesserManager().mostLikelyType(xis);
			}
		}

		Element el = null;
		try {
			if (localNormaliser == null) {
				localNormaliser = normaliserManager.lookup(localType); // XXX XYZ
			}
			lastNormaliser = localNormaliser;
			xis.setType(localType);
			localNormaliser.setContentHandler(getContentHandler());
			localNormaliser.setLexicalHandler(getLexicalHandler());
			el = DOMUtil.parseToElement(localNormaliser, xis);
		} catch (Exception x) {
			logger.log(Level.FINER,
			           "No Normaliser found, falling back to Binary Normalisation. file: " + bp.getFileName() + " subject: " + msg.getSubject(),
			           x);
			el = getBinary(xis);
		}

		return el;
	}

	Element getBinary(XenaInputSource xis) throws IOException, SAXException, XenaException {
		AbstractNormaliser binaryNormaliser =
		    normaliserManager.getPluginManager().getXena().getNormaliser(BinaryToXenaBinaryNormaliser.BINARY_NORMALISER_NAME);
		if (binaryNormaliser == null) {
			throw new XenaException("Binary normaliser not found");
		}

		return DOMUtil.parseToElement(binaryNormaliser, xis);
	}

	@Override
	public String getVersion() {
		return ReleaseInfo.getVersion() + "b" + ReleaseInfo.getBuildNumber();
	}

	@Override
	public boolean isConvertible() {
		return true;
	}

	@Override
	public String getOutputFileExtension() {
		// The main part of most email messages are exported as XML, attachments will be handled by the appropriate normaliser
		return "xml";
	}

}
