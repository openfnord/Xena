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
 */

package au.gov.naa.digipres.xena.kernel.metadatawrapper;

public class TagNames {

	// Though the checksum algorithm isn't actually a tag, I added it so i can be accessed.
	public static final String DEFAULT_CHECKSUM_ALGORITHM = "SHA-512";

	public final static String NAA_PACKAGE = "NAA Package";

	public final static String XENA_URI = "http://preservation.naa.gov.au/xena/1.0";
	public final static String PACKAGE_URI = "http://preservation.naa.gov.au/package/1.0";
	public final static String WRAPPER_URI = "http://preservation.naa.gov.au/wrapper/1.0";
	public final static String METADATA_URI = "http://preservation.naa.gov.au/metadata/1.0";

	public final static String METADATA_PREFIX = "metadata";
	public final static String PACKAGE_PREFIX = "package";
	public final static String WRAPPER_PREFIX = "wrapper";

	public final static String XENA = "xena";
	public final static String PACKAGE = "package";
	public final static String SIGNED_AIP = "signed-aip";
	public final static String PACKAGE_PACKAGE = PACKAGE_PREFIX + ":" + PACKAGE;
	public final static String WRAPPER_SIGNED_AIP = WRAPPER_PREFIX + ":" + SIGNED_AIP;

	public final static String META = "meta";
	public final static String PACKAGE_META = PACKAGE_PREFIX + ":" + META;
	public final static String WRAPPER_META = WRAPPER_PREFIX + ":" + META;
	public final static String XENA_META = XENA + ":" + META;

	public final static String AIP = "aip";
	public final static String WRAPPER_AIP = WRAPPER_PREFIX + ":" + AIP;

	public final static String SIGNATURE = "signature";
	public final static String WRAPPER_SIGNATURE = WRAPPER_PREFIX + ":" + SIGNATURE;

	public final static String CONTENT = "content";
	public final static String PACKAGE_CONTENT = PACKAGE_PREFIX + ":" + CONTENT;

	public final static String IDENTIFIER = "identifier";
	public final static String SOURCE = "source";

	public final static String IDENTIFIER_URI = "http://preservation.naa.gov.au/identifier/1.0";

	public final static String DC_URI = "http://purl.org/dc/elements/1.1/";

	public final static String DC_PREFIX = "dc";

	public final static String DCIDENTIFIER = DC_PREFIX + ":" + IDENTIFIER;

	public final static String DCSOURCE = DC_PREFIX + ":" + SOURCE;

	public final static String DCTERMS_URI = "http://purl.org/dc/terms/";
	public final static String DCTERMS_PREFIX = "dcterms";

	public final static String CREATED = "created";
	public final static String DCCREATED = DCTERMS_PREFIX + ":" + CREATED;

	public final static String NAA_URI = "http://preservation.naa.gov.au/naa/1.0";
	public final static String NAA_PREFIX = "naa";

	public final static String DATASOURCE = "datasource";
	public final static String NAA_DATASOURCE = NAA_PREFIX + ":" + DATASOURCE;

	public final static String DATASOURCES = "datasources";
	public final static String NAA_DATASOURCES = NAA_PREFIX + ":" + DATASOURCES;

	public final static String LASTMODIFIED = "last-modified";
	public final static String NAA_LASTMODIFIED = NAA_PREFIX + ":" + LASTMODIFIED;

	public final static String SOURCEID = "source-id";
	public final static String NAA_SOURCEID = NAA_PREFIX + ":" + SOURCEID;

	public final static String TYPE = "type";
	public final static String NAA_TYPE = NAA_PREFIX + ":" + TYPE;

	public final static String WRAPPER = "wrapper";
	public final static String NAA_WRAPPER = NAA_PREFIX + ":" + WRAPPER;

}
