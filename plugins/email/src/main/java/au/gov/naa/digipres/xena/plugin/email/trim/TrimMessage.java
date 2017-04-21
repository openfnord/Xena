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

package au.gov.naa.digipres.xena.plugin.email.trim;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.List;
import java.util.StringTokenizer;

import javax.activation.DataHandler;
import javax.mail.Address;
import javax.mail.Flags;
import javax.mail.Folder;
import javax.mail.Header;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.internet.InternetAddress;

import au.gov.naa.digipres.xena.javatools.IteratorToEnumeration;
import au.gov.naa.digipres.xena.util.FileUtils;

public class TrimMessage extends Message implements TrimPart {
	protected File file;

	protected byte[] body;

	protected List<Header> headers = new ArrayList<Header>();

	protected int nlines = 0;

	protected List<TrimAttachment> attachments = new ArrayList<TrimAttachment>();

	public TrimMessage(Folder folder, int msgnum, File file) throws MessagingException {
		super(folder, msgnum);
		load(file);
	}

	public TrimMessage(File file) throws MessagingException {
		load(file);
	}

	public void load(File file) throws MessagingException {
		this.file = file;

		try {
			BufferedReader reader = new BufferedReader(new FileReader(file));
			String line = null;
			while ((line = reader.readLine()) != null) {
				StringTokenizer tok = new StringTokenizer(line, ":");
				if (line.equals("") || !tok.hasMoreTokens()) {
					break;
				}
				String headerName = tok.nextToken();
				String headerValue = line.substring(headerName.length() + 1);
				if (headerName.equals("TRIM-Attachment")) {
					attachments.add(newAttachment(headerValue));
				} else {
					addHeader(headerName, headerValue);
				}
			}
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			int c;
			int cr = 0;
			while (0 <= (c = reader.read())) {
				baos.write(c);
				if (c == '\n') {
					nlines++;
				} else if (c == '\r') {
					cr++;
				}
			}
			if (nlines < cr) {
				nlines = cr;
			}
			body = baos.toByteArray();
		} catch (IOException x) {
			throw new MessagingException("TrimMessage: ", x);
		}
	}

	@Override
	public void setSentDate(Date parm1) throws javax.mail.MessagingException {
		throw new java.lang.UnsupportedOperationException("Method setSentDate() not yet implemented.");
	}

	public void setDisposition(String parm1) throws javax.mail.MessagingException {
		throw new java.lang.UnsupportedOperationException("Method setDisposition() not yet implemented.");
	}

	public boolean isMimeType(String parm1) throws javax.mail.MessagingException {
		return false;
	}

	@Override
	public Date getSentDate() throws javax.mail.MessagingException {
		return headToDate("Sent-Date", "Sent-Time");
	}

	public DataHandler getDataHandler() throws javax.mail.MessagingException {
		throw new java.lang.UnsupportedOperationException("Method getDataHandler() not yet implemented.");
	}

	public void setContent(Object parm1, String parm2) throws javax.mail.MessagingException {
		throw new java.lang.UnsupportedOperationException("Method setContent() not yet implemented.");
	}

	@Override
	public Address[] getFrom() throws javax.mail.MessagingException {
		String[] res = getHeader("From");
		return strToAdd(res);
	}

	public Enumeration getNonMatchingHeaders(String[] names) throws javax.mail.MessagingException {
		String[] lnames = new String[names.length];
		for (int i = 0; i < names.length; i++) {
			lnames[i] = names[i].toLowerCase();
		}
		List<Header> ls = new ArrayList<Header>();

		for (Header h : headers) {
			boolean match = false;
			for (String lname : lnames) {
				if (h.getName().toLowerCase().equals(lname)) {
					match = true;
					break;
				}
			}
			if (!match) {
				ls.add(h);
			}
		}

		return new IteratorToEnumeration(ls.iterator());
	}

	public List<TrimAttachment> getAttachments() {
		return attachments;
	}

	public Object getContent() throws java.io.IOException, javax.mail.MessagingException {
		if (attachments.size() == 0) {
			return new ByteArrayInputStream(body);
		} else {
			TrimMultiPart mp = new TrimMultiPart();
			mp.addBodyPart(new TrimBody(body, nlines));

			for (TrimAttachment attachment : attachments) {
				mp.addBodyPart(attachment);
			}

			return mp;
		}
	}

	@Override
	public void addRecipients(Message.RecipientType parm1, Address[] parm2) throws javax.mail.MessagingException {
		throw new java.lang.UnsupportedOperationException("Method addRecipients() not yet implemented.");
	}

	@Override
	public void saveChanges() throws javax.mail.MessagingException {
		throw new java.lang.UnsupportedOperationException("Method saveChanges() not yet implemented.");
	}

	@Override
	public Flags getFlags() throws javax.mail.MessagingException {
		return new Flags();
	}

	public void setFileName(String parm1) throws javax.mail.MessagingException {
		throw new java.lang.UnsupportedOperationException("Method setFileName() not yet implemented.");
	}

	protected Address[] strToAdd(String[] res) {
		Address[] rtn = new Address[res.length];
		for (int i = 0; i < res.length; i++) {
			StringTokenizer st = new StringTokenizer(res[i], "[]:");
			String name = null;
			String protocol = null;
			String address = null;
			if (st.hasMoreTokens()) {
				name = st.nextToken();
			}
			if (st.hasMoreTokens()) {
				protocol = st.nextToken();
			}
			if (st.hasMoreTokens()) {
				address = st.nextToken();
			}
			if (protocol != null && protocol.toUpperCase().equals("SMTP")) {
				try {
					rtn[i] = new InternetAddress(address, name);
				} catch (UnsupportedEncodingException ex) {
					rtn[i] = new TrimAddress(res[i]);
				}
			} else {
				rtn[i] = new TrimAddress(res[i]);
			}
		}
		return rtn;
	}

	@Override
	public Address[] getRecipients(Message.RecipientType type) throws javax.mail.MessagingException {
		String[] res = null;
		if (type == Message.RecipientType.TO) {
			res = getHeader("To");
		} else if (type == Message.RecipientType.CC) {
			res = getHeader("Cc");
		} else if (type == Message.RecipientType.BCC) {
			res = getHeader("Bcc");
		}
		return strToAdd(res);
	}

	public int getSize() throws javax.mail.MessagingException {
		return body.length;
	}

	public String[] getHeader(String name) throws javax.mail.MessagingException {
		name = name.toLowerCase();
		List<String> ls = new ArrayList<String>();

		for (Header h : headers) {
			if (h.getName().toLowerCase().equals(name)) {
				ls.add(h.getValue());
			}
		}

		String[] rtn = new String[ls.size()];
		ls.toArray(rtn);
		return rtn;
	}

	@Override
	public Message reply(boolean parm1) throws javax.mail.MessagingException {
		throw new java.lang.UnsupportedOperationException("Method reply() not yet implemented.");
	}

	@Override
	public void setRecipients(Message.RecipientType parm1, Address[] parm2) throws javax.mail.MessagingException {
		throw new java.lang.UnsupportedOperationException("Method setRecipients() not yet implemented.");
	}

	@Override
	public void addFrom(Address[] parm1) throws javax.mail.MessagingException {
		throw new java.lang.UnsupportedOperationException("Method addFrom() not yet implemented.");
	}

	@Override
	public void setFrom(Address parm1) throws javax.mail.MessagingException {
		throw new java.lang.UnsupportedOperationException("Method setFrom() not yet implemented.");
	}

	public Enumeration getMatchingHeaders(String[] names) throws javax.mail.MessagingException {
		String[] lnames = new String[names.length];
		for (int i = 0; i < names.length; i++) {
			lnames[i] = names[i].toLowerCase();
		}
		List<Header> ls = new ArrayList<Header>();

		for (Header h : headers) {
			for (String lname : lnames) {
				if (h.getName().toLowerCase().equals(lname)) {
					ls.add(h);
					break;
				}
			}
		}
		return new IteratorToEnumeration(ls.iterator());
	}

	@Override
	public void setFlags(Flags parm1, boolean parm2) throws javax.mail.MessagingException {
		throw new java.lang.UnsupportedOperationException("Method setFlags() not yet implemented.");
	}

	@Override
	public String getSubject() throws javax.mail.MessagingException {
		String[] v = getHeader("Subject");
		if (v.length <= 0) {
			return null;
		}
		return v[0];
	}

	public Enumeration getAllHeaders() throws javax.mail.MessagingException {
		return new IteratorToEnumeration(headers.iterator());
	}

	public void setDescription(String parm1) throws javax.mail.MessagingException {
		throw new java.lang.UnsupportedOperationException("Method setDescription() not yet implemented.");
	}

	public String getFileName() throws javax.mail.MessagingException {
		return file.getName();
	}

	public File getFile() {
		return file;
	}

	public void setDataHandler(DataHandler parm1) throws javax.mail.MessagingException {
		throw new java.lang.UnsupportedOperationException("Method setDataHandler() not yet implemented.");
	}

	public void setText(String parm1) throws javax.mail.MessagingException {
		throw new java.lang.UnsupportedOperationException("Method setText() not yet implemented.");
	}

	public InputStream getInputStream() throws java.io.IOException, javax.mail.MessagingException {
		return new ByteArrayInputStream(body);
	}

	public String getDescription() throws javax.mail.MessagingException {
		return null;
	}

	public String getDisposition() throws javax.mail.MessagingException {
		return null;
	}

	public void setContent(Multipart parm1) throws javax.mail.MessagingException {
		throw new java.lang.UnsupportedOperationException("Method setContent() not yet implemented.");
	}

	public void writeTo(OutputStream os) throws java.io.IOException, javax.mail.MessagingException {
		os.write(body);
	}

	@Override
	public Date getReceivedDate() throws javax.mail.MessagingException {
		return headToDate("Received-Date", "Received-Time");
	}

	protected Date headToDate(String date, String time) throws MessagingException {
		SimpleDateFormat fm = new SimpleDateFormat("yyyyMMddhh:mm:ss aa");
		String[] datehs = getHeader(date);
		String[] timehs = getHeader(time);
		if (datehs.length <= 0 || timehs.length <= 0) {
			return null;
		}
		try {
			return fm.parse(datehs[0].trim() + timehs[0].trim());
		} catch (java.text.ParseException x) {
			throw new MessagingException("Cannot parse " + date + " or " + time, x);
		}
	}

	public void setHeader(String parm1, String parm2) throws javax.mail.MessagingException {
		throw new java.lang.UnsupportedOperationException("Method setHeader() not yet implemented.");
	}

	@Override
	public void setSubject(String parm1) throws javax.mail.MessagingException {
		throw new java.lang.UnsupportedOperationException("Method setSubject() not yet implemented.");
	}

	public String getContentType() throws javax.mail.MessagingException {
		return "text/plain";
	}

	@Override
	public void setFrom() throws javax.mail.MessagingException {
		throw new java.lang.UnsupportedOperationException("Method setFrom() not yet implemented.");
	}

	public int getLineCount() throws javax.mail.MessagingException {
		return nlines;
	}

	public void addHeader(String name, String value) throws javax.mail.MessagingException {
		headers.add(new Header(name, value));
	}

	public void removeHeader(String parm1) throws javax.mail.MessagingException {
		throw new java.lang.UnsupportedOperationException("Method removeHeader() not yet implemented.");
	}

	protected TrimAttachment newAttachment(String attachmentHeader) throws IOException {

		TrimAttachmentTokenizer tokenizer = new TrimAttachmentTokenizer(attachmentHeader);
		List<String> nvPairList = tokenizer.nextRecord();
		String attachmentName = null;
		String attachmentPath = null;
		for (String tok : nvPairList) {
			String pairName = nameOfAttachment(tok);
			String pairValue = valueOfAttachment(tok);
			if (pairName.equals("Name")) {
				attachmentName = pairValue;
			} else if (pairName.equals("Path")) {
				attachmentPath = pairValue;

				// Extension-less files seem to have the '.' left on when
				// specified in the TRIM header, so we'll strip them off
				if (attachmentPath.endsWith(".")) {
					attachmentPath = attachmentPath.substring(0, attachmentPath.length() - 1);
				}
			}
		}

		if (attachmentName == null || attachmentPath == null) {
			throw new IOException("Malformed TRIM Attachment header: " + attachmentHeader);
		}

		// The path retrieved from the attachment specification in the TRIM header might use a different path separator
		// to the one used on the current OS. This causes the File getName method to return the entire path rather than
		// just the filename, and thus the attachment file is not found. To get around this, we'll assume the path
		// separator is
		// either a slash or a backslash, and the filename is everything after the last slash/backslash in the path.
		int backslashIndex = attachmentPath.lastIndexOf("\\");
		int slashIndex = attachmentPath.lastIndexOf("/");
		int lastIndex = backslashIndex > slashIndex ? backslashIndex : slashIndex;
		String attachmentFilename = attachmentPath.substring(lastIndex + 1);
		File parentDir = file.getParentFile();

		try {
			File attachmentFile = FileUtils.getFileIgnoreCase(parentDir, attachmentFilename);
			return new TrimAttachment(attachmentFile, attachmentName);
		} catch (IOException iex) {
			throw new IOException("Could not find the attachment file referred to by a TRIM email.");
		}
	}

	protected String nameOfAttachment(String tok) {
		int ind = tok.indexOf('=');
		if (0 < ind) {
			return tok.substring(0, ind);
		} else {
			return null;
		}
	}

	protected String valueOfAttachment(String tok) {
		int ind = tok.indexOf('=');
		if (ind > 0 && ind < tok.length() - 1) {
			String s = tok.substring(ind + 1);
			if (s.charAt(0) == '"') {
				s = s.substring(1, s.length() - 1);
			}
			return s;
		} else {
			return "";
		}
	}

}
