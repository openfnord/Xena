/**
 * This file is part of Xena.
 * 
 * Xena is free software; you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 3 of the License, or (at your option) any later version.
 * 
 * Xena is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with Xena; if not, write to the Free Software Foundation, Inc., 59 Temple
 * Place, Suite 330, Boston, MA 02111-1307 USA
 * 
 * 
 * @author Andrew Keeling
 * @author Chris Bitmead
 * @author Justin Waddell
 * @author Jeff Stiff
 */

package au.gov.naa.digipres.xena.kernel.normalise;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.sax.SAXTransformerFactory;
import javax.xml.transform.sax.TransformerHandler;
import javax.xml.transform.stream.StreamResult;

import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLFilter;
import org.xml.sax.XMLReader;
import org.xml.sax.ext.LexicalHandler;
import org.xml.sax.helpers.XMLFilterImpl;

import au.gov.naa.digipres.xena.javatools.Reflect;
import au.gov.naa.digipres.xena.kernel.XenaException;
import au.gov.naa.digipres.xena.kernel.XenaInputSource;
import au.gov.naa.digipres.xena.kernel.XenaWarningException;
import au.gov.naa.digipres.xena.kernel.filenamer.AbstractFileNamer;
import au.gov.naa.digipres.xena.kernel.metadatawrapper.AbstractMetaDataUnwrapper;
import au.gov.naa.digipres.xena.kernel.metadatawrapper.AbstractMetaDataWrapper;
import au.gov.naa.digipres.xena.kernel.plugin.PluginManager;
import au.gov.naa.digipres.xena.kernel.type.BinaryFileType;
import au.gov.naa.digipres.xena.kernel.type.FileType;
import au.gov.naa.digipres.xena.kernel.type.Type;
import au.gov.naa.digipres.xena.kernel.type.XenaBinaryFileType;
import au.gov.naa.digipres.xena.kernel.type.XenaFileType;
import au.gov.naa.digipres.xena.util.FileUtils;
import au.gov.naa.digipres.xena.util.XmlDeNormaliser;

/**
 * This class is responsible for managing all things pertaining to normalisers.
 * <p>
 * It basically does the following broad tasks:
 * <ul><li>Loads Normalisers</li>
 * <li>Loads De-Normalisers</li>
 * <li>Maintains the relationship of types to normalisers / denormalisers</li>
 * <li>Handles the actual normalisation process</li>
 * <li>Handles exporting (Denormalisation)</li>
 * </ul>
 * <p>
 * The binary normaliser and denormaliser are built into this class. Other
 * normalisers are able to be loaded as part of plugins using the loadManager
 * interface methods (<code>load(JarPreferences jp)</code> and <code>complete()</code>).
 * This is usually called by the plugin manager. Otherwise, this class allows 
 * normalisers and denormalisers to be retrieved based on their name, class or
 * class name, tags, input type or output type. At this stage, there are a number
 * of Maps which are maintained to facilitate this, however, ultimately this may have to
 * be changed. These changes will be transparent to calling applications. 
 * This class also allows normalisers to be 'disabled', although this feature
 * is deprecated and types should be disabled rather than normalisers.
 * </p><p>
 * Finally, the normaliser manager is responsible for the actual normalising of
 * XenaInputSource objects, including wrapping, as well as the Denormalising
 * of Xena files to the appropriate format they are required to be exported to.
 * </p>
 * @see au.gov.naa.digipres.xena.kernel.normalise.AbstractNormaliser
 * @see au.gov.naa.digipres.xena.kernel.normalise.AbstractDeNormaliser
 * @see au.gov.naa.digipres.xena.kernel.plugin.PluginManager
 * @created April 8, 2002
 */
public class NormaliserManager {

	public final static String PREF_AUTO_LOG = "autoLog";
	public final static String SOURCE_DIR_STRING = "sourceDirectory";
	public final static String DESTINATION_DIR_STRING = "destinationDirectory";
	public final static String ERROR_DIR_STRING = "errorDirectory";
	public final static String CONFIG_DIR_STRING = "configDirectory";

	private final Map<Object, List<Class<?>>> fromMap = new HashMap<Object, List<Class<?>>>();

	private final Map<String, Class<?>> nameMap = new HashMap<String, Class<?>>();

	private final Set<Class<?>> all = new HashSet<Class<?>>();

	private final Map<String, List<Class<?>>> denormaliserTagMap = new HashMap<String, List<Class<?>>>();

	private final Map<Class<?>, Set<Type>> outputTypes = new HashMap<Class<?>, Set<Type>>();

	private final Map<Class<?>, Set<Type>> inputTypes = new HashMap<Class<?>, Set<Type>>();

	private final Map<String, AbstractNormaliser> normaliserMap = new HashMap<String, AbstractNormaliser>();

	private final Map<Type, AbstractNormaliser> textNormaliserMap = new HashMap<Type, AbstractNormaliser>();

	private final Logger logger = Logger.getLogger(this.getClass().getName());

	private PluginManager pluginManager;

	/**
	 * Constructor for the NormaliserManager class.
	 * @param pluginManager
	 */
	public NormaliserManager(PluginManager pluginManager) {
		this.pluginManager = pluginManager;

		// add our builtin normalisers with the built in input types.
		Set<Type> binaryTypes = new HashSet<Type>();
		binaryTypes.add(new BinaryFileType());

		Set<Type> xenaBinaryTypes = new HashSet<Type>();
		xenaBinaryTypes.add(new XenaBinaryFileType());

		AbstractNormaliser binaryNormaliser = new BinaryToXenaBinaryNormaliser();
		binaryNormaliser.setNormaliserManager(this);
		normaliserMap.put(binaryNormaliser.toString(), binaryNormaliser);

		inputTypes.put(BinaryToXenaBinaryNormaliser.class, binaryTypes);
		inputTypes.put(XenaBinaryToBinaryDeNormaliser.class, xenaBinaryTypes);

		outputTypes.put(BinaryToXenaBinaryNormaliser.class, xenaBinaryTypes);
		outputTypes.put(XenaBinaryToBinaryDeNormaliser.class, binaryTypes);

		try {
			add(BinaryToXenaBinaryNormaliser.class, inputTypes.get(BinaryToXenaBinaryNormaliser.class));
		} catch (XenaException xe) {
			System.err.println("Could not load binary normaliser.");
			xe.printStackTrace();
		}
		try {
			add(XenaBinaryToBinaryDeNormaliser.class, inputTypes.get(XenaBinaryToBinaryDeNormaliser.class));
		} catch (XenaException xe) {
			System.err.println("Could not load binary de-normaliser.");
			xe.printStackTrace();
		}
	}

	/**
	 * Add the normaliser maps, representing a normaliser's input and output types, to the Normaliser Manager
	 * @param inputMap - a map of Objects (either AbstractNormaliser or AbstractDeNormaliser) to a set of Types handled by the
	 * AbstractNormaliser or AbstractDeNormaliser
	 * @param outputMap - a map of Objects (either AbstractNormaliser or AbstractDeNormaliser) to a set of Types output by the
	 * AbstractNormaliser or AbstractDeNormaliser
	 * @throws XenaException
	 */
	public void addNormaliserMaps(Map<Object, Set<Type>> inputMap, Map<Object, Set<Type>> outputMap) throws XenaException {

		// normaliser can either be an AbstractNormaliser or a AbstractDeNormaliser
		for (Object normaliser : outputMap.keySet()) {

			outputTypes.put(normaliser.getClass(), outputMap.get(normaliser));
			if (normaliser instanceof AbstractNormaliser) {
				normaliserMap.put(normaliser.toString(), (AbstractNormaliser) normaliser);
			}
		}

		// normaliser can either be an AbstractNormaliser or a AbstractDeNormaliser
		for (Object normaliser : inputMap.keySet()) {
			Set<Type> normaliserTypes = inputMap.get(normaliser);
			Class<?> normaliserClass = normaliser.getClass();

			inputTypes.put(normaliser.getClass(), normaliserTypes);
			if (normaliser instanceof AbstractNormaliser) {
				normaliserMap.put(normaliser.toString(), (AbstractNormaliser) normaliser);
			}

			Set<Type> outputTypesForNormaliser = outputTypes.get(normaliserClass);
			if (outputTypesForNormaliser == null) {
				throw new XenaException("Error: outputMap for: " + normaliserClass.getName() + "is null!");
			}
			if (outputTypesForNormaliser.size() != 1) {
				throw new XenaException("Error: outputMap for: " + normaliserClass.getName() + " has: " + outputTypes.size()
				                        + " but must have exactly one entry per normaliser");
			}
			add(normaliser.getClass(), normaliserTypes);
		}
	}

	/**
	 * Add the textNormalisers map, representing a single plugin's text normalisers, to the Normaliser Manager
	 * @param textNormalisers
	 */
	public void addTextNormalisers(Map<Type, AbstractNormaliser> textNormalisers) {
		textNormaliserMap.putAll(textNormalisers);
	}

	/**
	 * Get a list of all available normalisers, denormalisers and other objects
	 * 
	 * @return List
	 */
	public List<Object> getAll() {
		// TODO: what types should these be?
		List<Object> rtn = new ArrayList<Object>();
		for (Class<?> cls : all) {
			try {
				rtn.add(cls.newInstance());
			} catch (IllegalAccessException ex) {
				ex.printStackTrace();
			} catch (InstantiationException ex) {
				ex.printStackTrace();
			}
		}
		return rtn;
	}

	/**
	 * Get a list of all available normalisers (i.e. XMLReader objects)
	 * 
	 * @return List
	 */
	public List<AbstractNormaliser> getAllNormalisers() {
		List<AbstractNormaliser> rtn = new ArrayList<AbstractNormaliser>();
		Iterator it = all.iterator();
		while (it.hasNext()) {
			Class<?> cls = (Class<?>) it.next();
			if (Reflect.conformsTo(cls, AbstractNormaliser.class)) {
				try {
					rtn.add((AbstractNormaliser) cls.newInstance());
				} catch (IllegalAccessException ex) {
					ex.printStackTrace();
				} catch (InstantiationException ex) {
					ex.printStackTrace();
				}
			}
		}
		return rtn;
	}

	@Override
	public String toString() {
		Iterator it = fromMap.entrySet().iterator();
		String rtn = "Normalisers: \n";
		while (it.hasNext()) {
			Map.Entry e = (Map.Entry) it.next();
			rtn += e.getKey().toString() + " -> " + e.getValue() + "\n";
		}
		return rtn;
	}

	/**
	 * Return all the normalisers classes that have the input type given
	 * 
	 * @param from
	 *            Type
	 * @return List
	 */
	public List<Class<?>> lookupList(Type from) {
		return fromMap.get(from);
	}

	/**
	 * Return return an instance of a normaliser given the input type
	 * 
	 * @param type
	 *            Type
	 * @return List
	 */
	public AbstractNormaliser lookup(Type type) throws XenaException {
		AbstractNormaliser rtn = null;
		try {
			Class<?> cls = lookupClass(type);
			if (cls != null) {
				rtn = (AbstractNormaliser) cls.newInstance();
				rtn.setNormaliserManager(this);
			}
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (InstantiationException e) {
			e.printStackTrace();
		}
		return rtn;
	}

	public AbstractNormaliser lookup(String name) {
		AbstractNormaliser rtn = null;
		rtn = normaliserMap.get(name);
		if (rtn != null) {
			rtn.setNormaliserManager(this);
		}
		return rtn;
	}

	/**
	 * Return an instance of a normaliser given its class object
	 * 
	 * @param cls
	 *            Class
	 * @return Object
	 */
	public Object lookupByClass(Class<?> cls) {
		Object rtn = null;
		try {
			if (cls != null) {
				rtn = cls.newInstance();
				if (rtn instanceof AbstractNormaliser) {
					((AbstractNormaliser) rtn).setNormaliserManager(this);
				}
			}
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (InstantiationException e) {
			e.printStackTrace();
		}
		return rtn;
	}

	/**
	 * Return an instance of a normaliser given its class name.
	 * 
	 * @param clsName
	 *            String
	 * @return Object
	 */
	public Object lookupByClassName(String clsName) {
		Class<?> cls = nameMap.get(clsName);
		Object rtn = lookupByClass(cls);
		return rtn;
	}

	/**
	 * Add a normaliser class to the Xena installation
	 * 
	 * @param normaliserClass
	 *            normaliser class (XMLReader).
	 * @param inputTypesParm
	 *            collection of input types allowable for this normaliser
	 * @param outputTypes
	 *            collection of output types possible for this normaliser
	 * @throws XenaException
	 */
	protected void add(Class<?> normaliserClass, Collection<Type> inputTypesParm) throws XenaException {
		try {
			Object normaliserObject = normaliserClass.newInstance();
			if (!(normaliserObject instanceof AbstractNormaliser) && !(normaliserObject instanceof AbstractDeNormaliser)) {
				throw new XenaException("Error: this object does not appear to be a normaliser - " + normaliserObject.getClass().getName());
			}

			if (normaliserObject instanceof AbstractNormaliser) {
				((AbstractNormaliser) normaliserObject).setNormaliserManager(this);
			} else if (normaliserObject instanceof AbstractDeNormaliser) {
				((AbstractDeNormaliser) normaliserObject).setNormaliserManager(this);
			}

			for (Type type : inputTypesParm) {
				add(normaliserClass, type, normaliserObject);
			}
		} catch (IllegalAccessException x) {
			throw new XenaException(x);
		} catch (InstantiationException x) {
			throw new XenaException(x);
		}
	}

	/**
	 * Given a normaliser class, return the output type
	 * 
	 * @param cls
	 *            normaliser Class
	 * @return FileType
	 */
	public FileType getOutputType(Class<?> cls) {
		return (FileType) ((Collection) outputTypes.get(cls)).iterator().next();
	}

	/**
	 * Given a normaliser class, return the possible input types
	 * 
	 * @param cls
	 *            normaliser Class
	 * @return FileType
	 */
	@Deprecated
	public Set getInputTypes(Class<?> cls) {
		return inputTypes.get(cls);
	}

	/**
	 * Add the normaliser to the main configuration collections
	 * 
	 * @param cls
	 *            XMLReader Class
	 * @param normaliser
	 *            XMLReader object
	 */
	protected void addName(Class<?> cls, Object normaliser) {
		all.add(cls);
		nameMap.put(cls.getName(), cls);
	}

	/**
	 * Add the given normaliser to the configuration
	 * 
	 * @param cls
	 *            class object of normaliser
	 * @param type
	 *            Object
	 * @param normaliser
	 *            Object
	 */
	protected void add(Class<?> cls, Type type, Object normaliser) {
		addName(cls, normaliser);
		List<Class<?>> normaliserClassList = fromMap.get(type);
		if (normaliserClassList == null) {
			normaliserClassList = new ArrayList<Class<?>>();
			fromMap.put(type, normaliserClassList);
		}
		normaliserClassList.add(cls);

		if (normaliser instanceof AbstractDeNormaliser && type instanceof XenaFileType) {
			XenaFileType xft = (XenaFileType) type;
			normaliserClassList = denormaliserTagMap.get(xft.getTag());
			if (normaliserClassList == null) {
				normaliserClassList = new ArrayList<Class<?>>();
				denormaliserTagMap.put(xft.getTag(), normaliserClassList);
			}
			normaliserClassList.add(cls);
		}
	}

	/**
	 * Find a DeNormaliser keyed on the XML tag that it can handle
	 */
	public AbstractDeNormaliser lookupDeNormaliser(String tag) {
		try {
			List l = denormaliserTagMap.get(tag);
			if (l == null) {
				return null;
			}
			Class<?> cls = (Class<?>) l.get(0);
			AbstractDeNormaliser denormaliser = (AbstractDeNormaliser) cls.newInstance();
			denormaliser.setNormaliserManager(this);
			return denormaliser;
		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * Find a DeNormaliser keyed on the Xena type that it can handle
	 */
	public AbstractDeNormaliser lookupDeNormaliser(Type type) {
		List list = lookupList(type);
		Iterator it = list.iterator();
		while (it.hasNext()) {
			Class<?> cls = (Class<?>) it.next();
			if (Reflect.conformsTo(cls, TransformerHandler.class)) {
				try {
					AbstractDeNormaliser denormaliser = (AbstractDeNormaliser) cls.newInstance();
					denormaliser.setNormaliserManager(this);
					return denormaliser;
				} catch (InstantiationException e) {
					e.printStackTrace();
				} catch (IllegalAccessException e) {
					e.printStackTrace();
				}
			}
		}
		return null;
	}

	/**
	 * Return the text normaliser for the given type
	 * @param type
	 * @return
	 */
	public AbstractNormaliser lookupTextNormaliser(Type type) {
		return textNormaliserMap.get(type);
	}

	/**
	 * Lookup a normaliser based on the type needed.
	 * 
	 * @param type
	 *            Type
	 * @return Class
	 * @throws XenaException
	 */
	protected Class<?> lookupClass(Type type) throws XenaException {
		Class<?> rtn = null;
		List list = lookupList(type);
		if (list != null) {
			Iterator it = list.iterator();
			while (it.hasNext()) {
				Class<?> cls = (Class<?>) it.next();
				if (Reflect.conformsTo(cls, XMLReader.class)) {
					rtn = cls;
					break;
				}
			}
		}
		if (rtn == null) {
			throw new XenaException("No Normaliser available");
		}
		return rtn;
	}

	/**
	 * An exception class to throw when we find a tag we were looking for in a
	 * SAX parse. Throwing an exception allows us to abandon the parse as soon
	 * as we found what we were looking for which is more efficient.
	 */
	static private class FoundException extends SAXException {
		String tag;

		String qtag;

		public FoundException(String tag, String qtag) {
			super("Found");
			this.tag = tag;
			this.qtag = qtag;
		}
	}

	/**
	 * Given a URL, unwrap the package wrapper and discover what the outermost
	 * XML tag is for this document
	 * 
	 * @param systemid
	 *            URL of Xena document
	 * @return String XML tag of outermost XML
	 */
	public String unwrapGetTag(XenaInputSource xis, XMLFilter unwrapper) throws XenaException {

		// This is pretty poor coding practice (throwing an exception as part of the normal program flow), \
		// but it does mean that we don't have to parse through the entire (possibly large) XML document... 
		// Not sure which is more important. Will leave it for now.
		try {
			XMLReader reader = SAXParserFactory.newInstance().newSAXParser().getXMLReader();
			unwrapper.setContentHandler(new XMLFilterImpl() {
				@Override
				public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {

					// Bail out early as soon as we've found what we want
					// for super efficiency.
					throw new FoundException(localName, qName);
				}
			});
			reader.setContentHandler((ContentHandler) unwrapper);
			reader.parse(xis);

		} catch (FoundException e) {
			if (e.qtag == null || "".equals(e.qtag)) {
				return e.tag;
			} else {
				return e.qtag;
			}
		} catch (SAXException x) {
			throw new XenaException(x);
		} catch (ParserConfigurationException x) {
			throw new XenaException(x);
		} catch (IOException x) {
			throw new XenaException(x);
		} finally {
			try {
				xis.close();
			} catch (IOException iox) {
				throw new XenaException(iox);
			}
		}
		throw new XenaException("unwrapGetTag: Unknown Tag");

	}

	/**
	 * Given a URL, unwrap the package wrapper and discover what the outermost
	 * XML tag is for this document
	 * 
	 * @param systemid
	 *            URL of Xena document
	 * @return String XML tag of outermost XML
	 */
	public String getFirstContentTag(String systemid) throws XenaException {
		try {
			XMLReader reader = SAXParserFactory.newInstance().newSAXParser().getXMLReader();
			XMLFilter unwrapper = pluginManager.getMetaDataWrapperManager().getUnwrapNormaliser();
			unwrapper.setParent(reader);
			unwrapper.setContentHandler(new XMLFilterImpl() {

				@Override
				public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {

					// Bail out early as soon as we've found what we want
					// for super efficiency.
					throw new FoundException(localName, qName);
				}
			});
			InputSource is = new InputSource(systemid);
			reader.setContentHandler((ContentHandler) unwrapper);
			reader.parse(is);
		} catch (FoundException e) {
			if (e.qtag == null || "".equals(e.qtag)) {
				return e.tag;
			} else {
				return e.qtag;
			}
		} catch (SAXException x) {
			throw new XenaException(x);
		} catch (ParserConfigurationException x) {
			throw new XenaException(x);
		} catch (XenaException x) {
			throw new XenaException(x);
		} catch (IOException x) {
			throw new XenaException(x);
		} catch (Exception x) {
			throw new XenaException(x);
		}
		throw new XenaException("unwrapGetTag: Unknown Tag");
	}

	/**
	 * Get the outermost XML tag from a Xena document TODO: Should this be in
	 * here? i mean, this isnt really anything to with normalising is it?
	 * 
	 * @param systemid
	 *            URL of document
	 * @return String tag
	 * @throws XenaException
	 */
	public String getTag(String systemid) throws XenaException {
		try {
			XMLReader reader = SAXParserFactory.newInstance().newSAXParser().getXMLReader();
			XMLFilter filter = new XMLFilterImpl();
			filter.setParent(reader);
			filter.setContentHandler(new XMLFilterImpl() {
				@Override
				public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {

					// Bail out early as soon as we've found what we want
					// for super efficiency.
					throw new FoundException(localName, qName);
				}
			});
			InputSource is = new InputSource(systemid);
			reader.setContentHandler((ContentHandler) filter);
			reader.parse(is);
		} catch (FoundException e) {
			if (e.qtag == null || "".equals(e.qtag)) {
				return e.tag;
			} else {
				return e.qtag;
			}
		} catch (SAXException x) {
			throw new XenaException(x);
		} catch (ParserConfigurationException x) {
			throw new XenaException(x);
		} catch (IOException x) {
			throw new XenaException(x);
		} catch (Exception x) {
			throw new XenaException(x);
		}
		throw new XenaException("getTag: Unknown Error");
	}

	/**
	 * Parse a fragment of an XML document, stripping off the package wrapper
	 * 
	 * @param systemid
	 *            URL of document
	 * @param ch
	 *            ContentHandler
	 */
	public void unwrapFragment(String systemid, ContentHandler ch) throws ParserConfigurationException, SAXException, IOException, XenaException {
		XMLFilterImpl filter = new XMLFilterImpl() {
			@Override
			public void startDocument() {
			}

			@Override
			public void endDocument() {
			}
		};
		filter.setContentHandler(ch);
		unwrap(systemid, filter);
	}

	/**
	 * Parse an XML document, stripping off the package wrapper
	 * 
	 * @param systemid
	 *            URL of document
	 * @param ch
	 *            ContentHandler
	 */
	public void unwrap(String systemid, ContentHandler ch) throws ParserConfigurationException, SAXException, IOException, XenaException {
		XMLReader reader = SAXParserFactory.newInstance().newSAXParser().getXMLReader();
		XMLFilter unwrapper = pluginManager.getMetaDataWrapperManager().getUnwrapNormaliser();
		unwrapper.setParent(reader);
		unwrapper.setContentHandler(ch);
		InputSource is = new InputSource(systemid);
		reader.setContentHandler((ContentHandler) unwrapper);
		reader.parse(is);
	}

	/**
	 * Return a XMLFilter with suitable chaining of events so that the
	 * output file will be wrapped with appropriate meta-data.
	 * This is an embedded normaliser, thus we need to ensure that
	 * start and end document events are not passed on to the primary
	 * content handler.
	 * 
	 * @param normaliser
	 *            normaliser to be used
	 * @param xis
	 *            source of data
	 * @param mesgLevel
	 *            level of embedding within the output Xena file
	 * @return XMLFilter which will handle the XML stream
	 */
	public AbstractMetaDataWrapper wrapEmbeddedNormaliser(AbstractNormaliser normaliser, XenaInputSource xis, ContentHandler primaryHandler,
	                                                      LexicalHandler lexicalHandler) throws SAXException, XenaException {
		AbstractMetaDataWrapper embeddedWrapper = pluginManager.getMetaDataWrapperManager().getActiveWrapperPlugin().getEmbeddedWrapper();
		embeddedWrapper = wrapTheNormaliser(normaliser, xis, embeddedWrapper);

		// Filter to ensure start and end document events are not passed on to the primaryHandler
		XMLFilterImpl wrapperFilter = new XMLFilterImpl() {
			@Override
			public void startDocument() {
				// Do nothing
			}

			@Override
			public void endDocument() {
				// Do nothing
			}
		};

		embeddedWrapper.setContentHandler(wrapperFilter);
		wrapperFilter.setContentHandler(primaryHandler);
		wrapperFilter.setParent(embeddedWrapper);

		// Lexical events can be passed straight through to the lexical handler
		embeddedWrapper.setLexicalHandler(lexicalHandler);

		return embeddedWrapper;
	}

	/**
	 * Return a XMLFilter with suitable chaining of events so that the
	 * output file will be wrapped with appropriate meta-data
	 * 
	 * @param normaliser -
	 *            normaliser to be used
	 * @param xis -
	 *            source of data
	 * @param wrapper -
	 *            xml wrapper
	 * @return XMLFilter which will handle the XML stream
	 */
	public AbstractMetaDataWrapper wrapTheNormaliser(AbstractNormaliser normaliser, XenaInputSource xis, AbstractMetaDataWrapper wrapper)
	        throws SAXException, XenaException {
		if (wrapper != null) {
			wrapper.setParent(normaliser);
			wrapper.setProperty("http://xena/input", xis);
			wrapper.setProperty("http://xena/normaliser", normaliser);
			wrapper.setProperty("http://xena/metaManager", getPluginManager().getMetaDataManager());
			wrapper.setProperty("http://xena/digest", "");

			// Set the property for the normaliser, once passed it should (if implemented) contain the checksum
			// of the file.
			normaliser.setProperty("http://xena/exported_digest", "");
			normaliser.setProperty("http://xena/exported_digest_comment", "");
			normaliser.setContentHandler(wrapper);
			normaliser.setLexicalHandler(wrapper);
		}
		return wrapper;
	}

	/**
	 * Normalise an actual document given a source of data.
	 * 
	 * <p>
	 * This method takes as a parameter a normaliser, an XIS and a wrapper and then
	 * calls the normaliser's parse method on the XIS after the wrapper has been added
	 * to the normaliser.
	 * 
	 * 
	 * </p>
	 * 
	 * @param normaliser
	 *            normaliser to use
	 * @param xis
	 *            source of data
	 * @throws XenaException
	 */
	public void parse(AbstractNormaliser normaliser, InputSource xis, AbstractMetaDataWrapper wrapper, NormaliserResults results)
	        throws XenaException {
		try {

			// Actually create the normalised document
			wrapper.startDocument();
			normaliser.parse(xis, results, false);
			wrapper.endDocument();

			// Don't bother the user with reporting success on every embedded
			// object!
			logger.finest(xis.getSystemId() + " successfully processed by " + normaliser.toString());
		} catch (IOException x) {
			throw new XenaException(x);
		} catch (SAXException x) {
			throw new XenaException(x);
		} finally {
			try {
				if (xis instanceof XenaInputSource) {
					((XenaInputSource) xis).close();
				}
			} catch (IOException x) {
				x.printStackTrace();
			}
		}
	}

	/** 
	 * Normalise an actual document given a source of data. 
	 * New Normaliser to handle the Migrate Only conversion feature.
	 * 
	 * <p>
	 * This method takes as a parameter a normaliser, an XIS and a wrapper and then
	 * calls the normaliser's parse method on the XIS after the wrapper has been added
	 * to the normaliser.
	 * 
	 * 
	 * </p>
	 * 
	 * @param normaliser
	 *            normaliser to use
	 * @param xis
	 *            source of data
	 * @param migrateOnly
	 *            true if this is a convert to Open Format only
	 * @throws XenaException
	 */
	public void parse(AbstractNormaliser normaliser, InputSource xis, AbstractMetaDataWrapper wrapper, NormaliserResults results, boolean migrateOnly)
	        throws XenaException {
		try {

			// Actually create the normalised document
			// Wrap and parse the file
			wrapper.startDocument();
			normaliser.parse(xis, results, migrateOnly);
			wrapper.endDocument();

			// Don't bother the user with reporting success on every embedded
			// object!
			logger.finest(xis.getSystemId() + " successfully processed by " + normaliser.toString());
		} catch (IOException x) {
			throw new XenaException(x);
		} catch (SAXException x) {
			throw new XenaException(x);
		} finally {
			try {
				if (xis instanceof XenaInputSource) {
					((XenaInputSource) xis).close();
				}
			} catch (IOException x) {
				x.printStackTrace();
			}
		}
	}

	//    
	// /**
	// * This class is only here for debug purposes. the idea is that you can
	// * insert it into the stream of content handlers and it will give an error
	// * if you try and insert bad data.
	// */
	// private class MyFilter extends XMLFilterImpl {
	// /**
	// * I'm not sure why SAX doesn't enforce this, but it doesn't seem to.
	// * That means that without this, SAX could create bad XML.
	// */
	// public void characters(char ch[], int start, int length)
	// throws SAXException {
	// int end = start + length;
	// for (int i = start; i < end; i++) {
	// if (!isXMLCharacter(ch[i])) {
	// throw new SAXException("0x" + Integer.toHexString(ch[i])
	// + " is not a legal XML character");
	// }
	// }
	// super.characters(ch, start, length);
	// }
	//
	// /**
	// * Took this from org.jdom.Verifier
	// */
	// private boolean isXMLCharacter(char c) {
	// if (c == '\n') {
	// return true;
	// }
	// if (c == '\r') {
	// return true;
	// }
	// if (c == '\t') {
	// return true;
	// }
	// if (c < 0x20) {
	// return false;
	// }
	// if (c <= 0xD7FF) {
	// return true;
	// }
	// if (c < 0xE000) {
	// return false;
	// }
	// if (c <= 0xFFFD) {
	// return true;
	// }
	// if (c < 0x10000) {
	// return false;
	// }
	// if (c <= 0x10FFFF) {
	// return true;
	// }
	// return false;
	// }
	// }

	/**
	 * normalise This code is part of the Xena API that should be used for all
	 * applications that require Xena functionality (including, arguably, the
	 * Xena GUI!) It should be called thusly: NormaliserManager
	 * normaliserManager = NormaliserManager.singelton();
	 * normaliserManager.normalise(normaliser, xis, log);
	 * 
	 * It is currently not called from the GUI, and changes wuill not effect the
	 * GUI in any way.
	 * 
	 * 
	 * @param normaliser -
	 *            normaliser to use
	 * @param xis -
	 *            the input source to use
	 * @param destinationDir -
	 *            destination dir for output files
	 * @param fileNamer -
	 *            fileNamer to use fo generate the output files
	 * @param wrapper -
	 *            wrapper to use to create the tags around the normalised
	 *            content.
	 * 
	 * @return NormaliseDataStore - an object containing all the information
	 *         generated during the normalise process.
	 * @throws XenaException
	 * @throws IOException
	 * @throws SAXException
	 */
	public NormaliserResults normalise(final XenaInputSource xis, final AbstractNormaliser normaliser, File destinationDir,
	                                   AbstractFileNamer fileNamer, final AbstractMetaDataWrapper wrapper) throws XenaException, IOException {
		// check our arguments....
		if (xis == null) {
			throw new IllegalArgumentException("XenaInputSource must not be null.");
		}
		if (normaliser == null) {
			throw new IllegalArgumentException("Normaliser must not be null.");
		}
		if (destinationDir == null) {
			throw new IllegalArgumentException("Destination directory must not be null.");
		}
		if (fileNamer == null) {
			throw new IllegalArgumentException("File Namer must not be null.");
		}
		if (wrapper == null) {
			throw new IllegalArgumentException("Wrapper must not be null.");
		}

		// set up our thread correctly...
		ClassLoader deserLoader = pluginManager.getDeserClassLoader();
		Thread.currentThread().setContextClassLoader(deserLoader);

		// check to make sure our normaliser has a reference to a normaliser
		// manager (preferably this one!)
		if (normaliser.getNormaliserManager() == null) {
			normaliser.setNormaliserManager(this);
		}

		// create our results object
		NormaliserResults results = new NormaliserResults(xis, normaliser, destinationDir, fileNamer, wrapper);

		// create our output file...
		// TODO: should look at doing something with the file extension...
		File outputFile = fileNamer.makeNewXenaFile(xis, normaliser, destinationDir);

		xis.setOutputFileName(outputFile.getName());
		results.setOutputFileName(outputFile.getName());

		// create our transform handler
		TransformerHandler transformerHandler = null;
		SAXTransformerFactory transformFactory = (SAXTransformerFactory) TransformerFactory.newInstance();
		try {
			transformerHandler = transformFactory.newTransformerHandler();
		} catch (TransformerConfigurationException e) {
			throw new XenaException("Unable to create transformerHandler due to transformer configuration exception.");
		}

		// TODO manage resources better.

		OutputStream outputStream = new FileOutputStream(outputFile);
		try {
			OutputStreamWriter osw = new OutputStreamWriter(outputStream, "UTF-8");
			StreamResult streamResult = new StreamResult(osw);
			transformerHandler.setResult(streamResult);

			// If this is a text normaliser, we don't want to include the XML header
			if (normaliser instanceof AbstractTextNormaliser) {
				transformerHandler.getTransformer().setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
			}

		} catch (UnsupportedEncodingException e) {
			outputStream.close();
			throw new XenaException("Unsupported encoder for output stream writer.");
		}

		// configure our normaliser
		normaliser.setProperty("http://xena/url", xis.getSystemId());
		normaliser.setProperty("http://xena/file", outputFile);
		normaliser.setProperty("http://xena/normaliser", normaliser);
		normaliser.setProperty("http://xena/input", xis);

		// normaliser.setContentHandler(transformerHandler);
		try {
			wrapper.setContentHandler(transformerHandler);
			wrapper.setLexicalHandler(transformerHandler);
			wrapTheNormaliser(normaliser, xis, wrapper);

			// do the normalisation!
			// normaliser.getContentHandler().startDocument();
			parse(normaliser, xis, wrapper, results);
			// normaliser.getContentHandler().endDocument();
			results.setNormalised(true);

			String id = wrapper.getSourceId(new XenaInputSource(outputFile));

			// Handle empty ID by using filename
			if (id == null || id.equals("")) {
				id = outputFile.getName().substring(0, xis.getOutputFileName().lastIndexOf('.'));
			}

			results.setId(id);

		} catch (XenaException x) {
			// JRW - delete xena file if exception occurs
			outputStream.flush();
			outputStream.close();
			outputFile.delete();
			// rethrow exception
			throw x;
		} catch (SAXException s) {
			// JRW - delete xena file if exception occurs
			outputStream.flush();
			outputStream.close();
			outputFile.delete();
			throw new XenaException(s);
		} catch (IOException iex) {
			// JRW - delete xena file if exception occurs
			outputStream.flush();
			outputStream.close();
			outputFile.delete();
			// rethrow exception
			throw iex;
		} finally {
			// let go the output files and any streams that are using it.
			outputStream.flush();
			outputStream.close();
			outputFile = null;
			normaliser.setProperty("http://xena/file", null);
			normaliser.setContentHandler(null);
			normaliser.setLexicalHandler(null);
			transformerHandler = null;
			System.gc();
		}
		return results;
	}

	/** 
	 * normalise This code is part of the Xena API that should be used for all
	 * applications that require Xena functionality (including, arguably, the
	 * Xena GUI!) It should be called thusly: NormaliserManager
	 * normaliserManager = NormaliserManager.singelton();
	 * normaliserManager.normalise(normaliser, xis, log);
	 * 
	 * @param xis -
	 *            the input source to use
	 * @param normaliser -
	 *            normaliser to use
	 * @param destinationDir -
	 *            destination dir for output files
	 * @param fileNamer -
	 *            fileNamer to use to generate the output files
	 * @param wrapper -
	 *            wrapper to use to create the tags around the normalised
	 *            content.
	 * @param migrateOnly -
	 *            true if this is only a migrate to Open Format normalise
	 * 
	 * @return NormaliseDataStore - an object containing all the information
	 *         generated during the normalise process.
	 * @throws XenaException
	 * @throws IOException
	 * @throws SAXException
	 */
	public NormaliserResults normalise(final XenaInputSource xis, final AbstractNormaliser normaliser, File destinationDir,
	                                   AbstractFileNamer fileNamer, final AbstractMetaDataWrapper wrapper, final boolean migrateOnly)
	        throws XenaException, IOException {
		// check our arguments....
		if (xis == null) {
			throw new IllegalArgumentException("XenaInputSource must not be null.");
		}
		if (normaliser == null) {
			throw new IllegalArgumentException("Normaliser must not be null.");
		}
		if (destinationDir == null) {
			throw new IllegalArgumentException("Destination directory must not be null.");
		}
		if (fileNamer == null) {
			throw new IllegalArgumentException("File Namer must not be null.");
		}
		if (wrapper == null) {
			throw new IllegalArgumentException("Wrapper must not be null.");
		}

		// set up our thread correctly...
		ClassLoader deserLoader = pluginManager.getDeserClassLoader();
		Thread.currentThread().setContextClassLoader(deserLoader);

		// check to make sure our normaliser has a reference to a normaliser
		// manager (preferably this one!)
		if (normaliser.getNormaliserManager() == null) {
			normaliser.setNormaliserManager(this);
		}

		// create our output file...
		File outputFile;
		boolean isArchiver = false;
		boolean isEmail = normaliser.getName().equalsIgnoreCase("Email"); // Emails are migrated differently
		AbstractMetaDataWrapper newWrapper = wrapper;

		if (isEmail) {
			// Swap the wrapper back to the default instead of the emptyWrapper
			// Emails are fully normalised then have an export run against them. This ensures attachments are handled.
			newWrapper = pluginManager.getMetaDataWrapperManager().getActiveWrapperPlugin().getWrapper();
		}

		// create our results object
		NormaliserResults results = new NormaliserResults(xis, normaliser, destinationDir, fileNamer, newWrapper);

		normaliser.setProperty("http://xena/input", xis);

		// TODO: should look at doing something with the file extension...
		if (migrateOnly && !isEmail) {

			// Check to see if this file gets converted or passed straight through
			if (normaliser.isConvertible()) {
				// File type does get converted, continue with migration routine
				// Check if this is an archive format, if so they handle creating the output file themselves
				String normType = normaliser.getName();
				isArchiver = isArchiveFile(normType);
				// Create the Open Format file
				outputFile = fileNamer.makeNewOpenFile(xis, normaliser, destinationDir);
			} else {
				// Copy the original file to the destination location unchanged
				File origFile = xis.getFile();
				FileUtils.fileCopy(origFile, destinationDir + File.separator + origFile.getName(), false);

				// File type does not get converted, notify user
				results.addWarning("File does not get converted, copied without modification.");

				// Add Some details about the copy
				results.setInputSystemId(xis.getSystemId());
				results.setInputType(xis.getType());
				results.setOutputFileName(origFile.getName());
				// Set the normaliser name to tell the user what was done.
				results.setNormaliserName("Copy Unchanged");

				return results;
			}
		} else {
			// Create the Xena output file
			outputFile = fileNamer.makeNewXenaFile(xis, normaliser, destinationDir);
		}

		xis.setOutputFileName(outputFile.getName());
		results.setOutputFileName(outputFile.getName());

		// create our transform handler
		TransformerHandler transformerHandler = null;
		SAXTransformerFactory transformFactory = (SAXTransformerFactory) TransformerFactory.newInstance();
		try {
			transformerHandler = transformFactory.newTransformerHandler();
		} catch (TransformerConfigurationException e) {
			throw new XenaException("Unable to create transformerHandler due to transformer configuration exception.");
		}

		// TODO manage resources better.

		OutputStream outputStream = new FileOutputStream(outputFile);
		try {
			OutputStreamWriter osw = new OutputStreamWriter(outputStream, "UTF-8");
			StreamResult streamResult = new StreamResult(osw);
			transformerHandler.setResult(streamResult);

			// If this is a text normaliser or MigrateOnly, we don't want to include the XML header
			if ((normaliser instanceof AbstractTextNormaliser) || (migrateOnly && !isEmail)) {
				transformerHandler.getTransformer().setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
			}

		} catch (UnsupportedEncodingException e) {
			outputStream.close();
			throw new XenaException("Unsupported encoder for output stream writer.");
		}

		// configure our normaliser
		normaliser.setProperty("http://xena/url", xis.getSystemId());
		normaliser.setProperty("http://xena/file", outputFile);
		normaliser.setProperty("http://xena/normaliser", normaliser);

		// normaliser.setContentHandler(transformerHandler);
		try {
			newWrapper.setContentHandler(transformerHandler);
			newWrapper.setLexicalHandler(transformerHandler);
			wrapTheNormaliser(normaliser, xis, newWrapper);

			// do the normalisation!
			parse(normaliser, xis, newWrapper, results, migrateOnly);
			results.setNormalised(true);
			results.setMigrateOnly(migrateOnly);

			String id = wrapper.getSourceId(new XenaInputSource(outputFile));

			// Handle empty ID by using filename
			if (id == null || id.equals("")) {
				id = outputFile.getName().substring(0, xis.getOutputFileName().lastIndexOf('.'));
			}

			results.setId(id);

			if (isEmail && migrateOnly) {
				// Run the export for the emails
				File dirDestination = new File(results.getDestinationDirString());
				File normXenaFile = new File(dirDestination, results.getOutputFileName());
				XenaInputSource xisNorm = new XenaInputSource(normXenaFile);
				try {
					export(xisNorm, dirDestination);
				} catch (ParserConfigurationException e) {
					throw new XenaException("Error in Parser: " + e.getMessage());
				} finally {
					// Remove the Xena file as this is a migrateOnly
					outputFile.delete();
					if (results.getChildAIPResults().size() > 0) {
						// Delete the Children Xena files as well
						for (NormaliserResults childResults : results.getChildAIPResults()) {
							File childFile = new File(childResults.getDestinationDirString() + File.separator + childResults.getOutputFileName());
							childFile.delete();
						}
					}
				}
			}

		} catch (XenaException x) {
			// JRW - delete xena file if exception occurs
			outputStream.flush();
			outputStream.close();
			outputFile.delete();
			// rethrow exception
			throw x;
		} catch (SAXException s) {
			// JRW - delete xena file if exception occurs
			outputStream.flush();
			outputStream.close();
			outputFile.delete();
			throw new XenaException(s);
		} catch (IOException iex) {
			// JRW - delete xena file if exception occurs
			outputStream.flush();
			outputStream.close();
			outputFile.delete();
			// rethrow exception
			throw iex;
		} finally {
			// let go the output files and any streams that are using it.
			outputStream.flush();
			outputStream.close();
			//Check if it was an archiver, if so, delete the outputFile, as this is a temp Xena file
			if (isArchiver && migrateOnly) {
				outputFile.delete();
			}
			outputFile = null;
			normaliser.setProperty("http://xena/file", null);
			normaliser.setContentHandler(null);
			normaliser.setLexicalHandler(null);
			transformerHandler = null;
			System.gc();
		}
		return results;
	}

	/**
	 * This is the export function for xena. It is called from the Xena object
	 * as part of the Xena API.
	 * 
	 * Takes a xenaFile (which is just a regular file!), finds out what
	 * normalised it, and tries to denormalise it. It calls the 
	 * <code>export(XenaInputSource xis, File outDir, boolean overwriteExistingFiles)</code>
	 * method with overwrite set to false.
	 * 
	 * @see NormaliserManager.export(XenaInputSource xis, File outDir, boolean OverwriteExistingFiles)
	 * @param xenaFile
	 * @param outDir
	 * @throws IOException
	 * @throws SAXException
	 * @throws XenaException
	 * @throws ParserConfigurationException
	 */

	public ExportResult export(XenaInputSource xis, File outDir) throws XenaException, IOException, SAXException, ParserConfigurationException {
		return export(xis, outDir, false);
	}

	/**
	 * 
	 * Export a Xena file into either its original format or the format it has
	 * been converted to, depending on the normaliser / denormaliser. Some plugins
	 * do not allow stuff to be denormalised for some reason.
	 * 
	 * <p>
	 * This method takes a Xena file, looks up the abstract denormaliser, and then passes it to
	 * another export method.
	 * 
	 * 
	 * </p>
	 * 
	 * @param xis
	 * @param outDir
	 * @param overwriteExistingFiles
	 * @return
	 * @throws IOException
	 * @throws SAXException
	 * @throws XenaException
	 * @throws ParserConfigurationException
	 */
	public ExportResult export(XenaInputSource xis, File outDir, boolean overwriteExistingFiles) throws IOException, SAXException, XenaException,
	        ParserConfigurationException {

		// get the unwrapper for this package...
		XMLFilter unwrapper;
		try {
			unwrapper = pluginManager.getMetaDataWrapperManager().getUnwrapper(xis);
		} catch (XenaException xe) {
			unwrapper = pluginManager.getMetaDataWrapperManager().getEmptyWrapper().getUnwrapper();
		}

		String tag = unwrapGetTag(xis, unwrapper);

		AbstractDeNormaliser deNormaliser = lookupDeNormaliser(tag);
		if (deNormaliser == null) {
			// Just use basic XML denormaliser
			deNormaliser = new XmlDeNormaliser();
		}

		return export(xis, outDir, overwriteExistingFiles, deNormaliser);

	}

	/**
	 * 
	 * Export a Xena file using the given denormaliser. 
	 * 
	 * <p>
	 * This method will determine the output file name based on the
	 * file name of the original source file, by calling getExportFilename.
	 * 
	 * <p>
	 * An exception may be thrown if this denormaliser is
	 * not the correct type for this file.
	 * 
	 * @param xis
	 * @param outDir
	 * @param overwriteExistingFiles
	 * @param deNormaliser
	 * @return
	 * @throws IOException
	 * @throws SAXException
	 * @throws XenaException
	 * @throws ParserConfigurationException
	 */
	public ExportResult export(XenaInputSource xis, File outDir, boolean overwriteExistingFiles, AbstractDeNormaliser deNormaliser)
	        throws IOException, SAXException, XenaException, ParserConfigurationException {
		String outFileName = getExportFilename(xis, deNormaliser);
		return export(xis, outDir, outFileName, overwriteExistingFiles, deNormaliser);

	}

	/**
	 * Return the name of the exported file for the given XIS and denormaliser.
	 * 
	 * @param xis
	 * @param deNormaliser
	 * @return
	 * @throws XenaException
	 */
	public String getExportFilename(XenaInputSource xis, AbstractDeNormaliser deNormaliser) throws XenaException {
		String sourceSysId = pluginManager.getMetaDataWrapperManager().getSourceName(xis);

		URI uri = null;
		try {
			uri = new java.net.URI(sourceSysId);
		} catch (URISyntaxException x) {
			throw new XenaException(x);
		}
		String outFileName = "";
		try {
			outFileName = new File(uri).toString();
		} catch (IllegalArgumentException iae) {
			// there seems to have been a problem of some description. In this
			// case, we will
			// just get the system id, and take the last part of it for now...
			if (sourceSysId.lastIndexOf('/') != -1) {
				outFileName = sourceSysId.substring(sourceSysId.lastIndexOf('/'));
			}
			if (sourceSysId.lastIndexOf('\\') != -1) {
				outFileName = sourceSysId.substring(sourceSysId.lastIndexOf('\\'));
			}
		}

		if (outFileName == null || outFileName.length() == 0) {
			throw new XenaException("Could not get output filename for some reason.");
		} else if (outFileName.startsWith(File.separator)) {
			// Going from a URI to a file puts a slash at the start - get rid of it!
			outFileName = outFileName.substring(1);
		}

		outFileName = adjustOutputFileExtension(xis, deNormaliser, outFileName);
		return outFileName;
	}

	/**
	 * Check to see if the given output filename is appropriate for the given XIS. 
	 * The extension may have to change if normalisation has changed the file type, eg a GIF has been converted to a JPEG.
	 * 
	 * @param xis
	 * @param deNormaliser
	 * @param outFileName
	 * @return
	 * @throws XenaException
	 */
	public String adjustOutputFileExtension(XenaInputSource xis, AbstractDeNormaliser deNormaliser, String outFileName) throws XenaException {
		String outputFileExtension = deNormaliser.getOutputFileExtension(xis);
		return adjustOutputFileExtension(outFileName, outputFileExtension);
	}

	/**
	 * Check to see if the given output filename is appropriate for the given output file name and extension. 
	 * The extension may have to change if normalisation has changed the file type, eg a GIF has been converted to a JPEG.
	 * 
	 * @param outputFileName
	 * @param outputFileExtension file extension (not including the '.')
	 * @return
	 */
	public String adjustOutputFileExtension(String outputFileName, String outputFileExtension) {
		String adjustedOutputFilename = outputFileName;

		/*
		 * This code adds the extension that the type gives us _if_ the name given to us by the meta data wrapper does
		 * not have the same extension. This could happen in a number of situations, most notably, for the plaintext,
		 * the default extension is txt, however many file extensions are valid text files. at the end of the day, this
		 * will at least reduce the instances of simple.txt -> simple.txt.txt and the like, and still give a reasonable
		 * indication of what is actually in the file.
		 * 
		 */
		if (outputFileExtension != null && !outputFileExtension.equals("")) {
			// This is so crappy. since endsWith is case sensitive, lets make an ugly hack...
			if (!adjustedOutputFilename.toLowerCase().endsWith("." + outputFileExtension.toLowerCase())) {
				// if ( !outFileName.endsWith("." + outputFileExtension) ) {
				adjustedOutputFilename = adjustedOutputFilename + "." + outputFileExtension;
			}
		}
		return adjustedOutputFilename;

	}

	/**
	 * This method allows Xena to export a file, using the given filename.
	 * 
	 * <p>
	 * This method takes a Xena file, looks up the abstract denormaliser, and then passes it to
	 * another export method.
	 * </p>
	 * 
	 * @param xis
	 * @param outDir
	 * @param outFileName
	 * @param overwriteExistingFiles
	 * @return
	 * @throws IOException
	 * @throws SAXException
	 * @throws XenaException
	 * @throws ParserConfigurationException
	 */
	public ExportResult export(XenaInputSource xis, File outDir, String outFileName, boolean overwriteExistingFiles) throws IOException,
	        SAXException, XenaException, ParserConfigurationException {

		// first up - lets find out what is at the top of this xml file.
		// is there a package wrapper we know about or are we straight into a normaliser?
		// or it something we simply dont know about?

		XMLFilter unwrapper = null;
		String tag;
		try {
			unwrapper = pluginManager.getMetaDataWrapperManager().getUnwrapper(xis);
			tag = unwrapGetTag(xis, unwrapper);
		} catch (XenaException xe) {
			// see if we can just get the tag regardless...
			tag = pluginManager.getMetaDataWrapperManager().getTag(xis);
		}

		AbstractDeNormaliser deNormaliser = lookupDeNormaliser(tag);
		if (deNormaliser == null) {
			// Just use basic XML denormaliser
			deNormaliser = new XmlDeNormaliser();
		}

		return export(xis, outDir, outFileName, overwriteExistingFiles, deNormaliser);

	}

	/**
	 * This method allows Xena to export a file, using the given filename and denormaliser.
	 * If the file already exists in the destination directory and overwriting is set to false
	 * the filename will have a sequential number appended just prior to the extension.
	 * 
	 * <p>
	 * An exception may be thrown if this denormaliser is
	 * not the correct type for this file.
	 * 
	 * @param xis
	 * @param outDir
	 * @param outFileName
	 * @param overwriteExistingFiles
	 * @param deNormaliser
	 * @param unwrapper
	 * @return
	 * @throws IOException
	 * @throws SAXException
	 * @throws XenaException
	 * @throws ParserConfigurationException
	 */
	public ExportResult export(XenaInputSource xis, File outDir, String outFileName, boolean overwriteExistingFiles, AbstractDeNormaliser deNormaliser)
	        throws IOException, SAXException, XenaException, ParserConfigurationException {

		// Retrieve the meta data unwrapper. We can't just pass in a previously retrieved unwrapper,
		// because the unwrapGetTag method may have left it in an unstable state.
		// This method will return a fresh instance.
		AbstractMetaDataUnwrapper unwrapper = pluginManager.getMetaDataWrapperManager().getUnwrapper(xis);

		ExportResult result = new ExportResult();
		result.setInputSysId(xis.getSystemId());

		// Add the export result to the list of xena listeners. 
		getPluginManager().getXena().registerXenaListener(result);

		File newFile = new File(outDir, outFileName);

		if (newFile.exists() && !overwriteExistingFiles) {
			// Add an incrementing numerical ID and check again
			int i = 1;
			int lastDot = outFileName.lastIndexOf(".");
			if (lastDot == -1) {
				// No dot found, no extension, use entire filename
				lastDot = outFileName.length();
			}
			String fileName = outFileName.substring(0, lastDot);
			String fileExt = outFileName.substring(lastDot); // May not always be an extension, just the bit after the last . in the filename
			DecimalFormat idFormatter = new DecimalFormat("0000");
			while (newFile.exists()) {
				// Change outFIleName, as we use it later.
				outFileName = fileName + "." + idFormatter.format(i) + fileExt;
				newFile = new File(outDir, outFileName);
				i++;
			}

		}

		if (!newFile.getParentFile().exists()) {
			if (!newFile.getParentFile().mkdirs()) {
				throw new XenaException("Unable to create children folders for file: " + newFile.getAbsolutePath());
			}
		}

		result.setOutputDirectoryName(outDir.getAbsolutePath());

		// Some denormalisers do not actually write to the root export file, but
		// produce their own set of export files (such as the website denormaliser).
		// If this is the case, we do not want to create the root export file
		// as it will just end up being an empty file.
		OutputStream outputStream = null;
		if (deNormaliser.writesToRootExportFile()) {
			outputStream = new FileOutputStream(newFile);
			OutputStreamWriter outputStreamWriter = new OutputStreamWriter(outputStream);
			StreamResult streamResult = new StreamResult(outputStream);
			streamResult.setWriter(outputStreamWriter);
			deNormaliser.setStreamResult(streamResult);
			result.setOutputFileName(outFileName);
		} else {
			// There will not be a root export file
			result.setOutputFileName(null);
		}

		try {
			deNormaliser.setOutputDirectory(outDir);
			deNormaliser.setOutputFilename(outFileName);

			// We need to pass the directory of the source XIS to the denormaliser, so it can handle any child xena
			// files
			// which are assumed to be in the same directory. This assumes that the XIS has been created for a file, so
			// a different (and much more difficult) solution will need to be found if this assumption turns out to be
			// incorrect. We'll check to make sure we have the file here just in case.
			if (xis.getFile() == null) {
				throw new XenaException("XenaInputSource " + xis.getSystemId() + " is not a file."
				                        + " Only XenaInputSources created from files can be exported");
			}
			deNormaliser.setSourceDirectory(xis.getFile().getParentFile());

			XMLReader reader = SAXParserFactory.newInstance().newSAXParser().getXMLReader();
			if (unwrapper != null) {
				unwrapper.setParent(reader);
				unwrapper.setContentHandler(deNormaliser);
				unwrapper.setLexicalHandler(deNormaliser);
				reader.setContentHandler(unwrapper);
				reader.setProperty("http://xml.org/sax/properties/lexical-handler", unwrapper);
			} else {
				reader.setContentHandler(deNormaliser);
				reader.setProperty("http://xml.org/sax/properties/lexical-handler", deNormaliser);
			}
			reader.setFeature("http://xml.org/sax/features/namespaces", true);
			reader.parse(xis);
			result.setExportSuccessful(true);

			// Add child ExportResults
			result.addChildResults(deNormaliser.getChildExportResultList());
		} finally {
			// Remove the result from the list of Xena Listeners
			getPluginManager().getXena().removeXenaListener(result);

			try {
				if (outputStream != null) {
					outputStream.close();
				}
				xis.close();
			} catch (IOException x) {
				throw new XenaException(x);
			}
		}
		return result;

	}

	/**
	 * @return Returns the pluginManager.
	 */
	public PluginManager getPluginManager() {
		return pluginManager;
	}

	/**
	 * @param pluginManager
	 *            The new value to set pluginManager to.
	 */
	public void setPluginManager(PluginManager pluginManager) {
		this.pluginManager = pluginManager;
	}

	/**
	 * @return Returns the normaliserMap.
	 */
	public Map<String, AbstractNormaliser> getNormaliserMap() {
		return normaliserMap;
	}

	/**
	 * This method returns true if the input string is of a Archive file type
	 * 
	 * @param - fileType - String of the type of file being normalised
	 * @return - boolean - True if the input string matches a known archive type as defined by Xena 
	 */
	public boolean isArchiveFile(String fileType) {
		boolean result = false;
		if (fileType.equalsIgnoreCase("gzip") || fileType.equalsIgnoreCase("zip") || fileType.equalsIgnoreCase("tar")
		    || fileType.equalsIgnoreCase("Image Magick Normaliser") || fileType.equalsIgnoreCase("Image Tiff Normaliser")
		    || fileType.equalsIgnoreCase("Website")) {
			result = true;
		}

		return result;
	}

}
