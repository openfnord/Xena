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
 * Created on 28/10/2005 andrek24
 * 
 */
package au.gov.naa.digipres.xena.kernel.metadatawrapper;

import au.gov.naa.digipres.xena.kernel.XenaException;

public class MetaDataWrapperPlugin {

	private String name;
	private Class wrapperClass;
	private Class unwrapperClass;
	private String topTag;
	private MetaDataWrapperManager metaDataWrapperManager;

	public MetaDataWrapperPlugin() {
	}

	public MetaDataWrapperPlugin(String name, AbstractMetaDataWrapper wrapper, AbstractMetaDataUnwrapper unwrapper, String topTag,
	                             MetaDataWrapperManager metaDataWrapperManager) {
		this.name = name;
		wrapperClass = wrapper.getClass();
		unwrapperClass = unwrapper.getClass();
		this.topTag = topTag;
		this.metaDataWrapperManager = metaDataWrapperManager;
	}

	public MetaDataWrapperPlugin(String name, Class wrapperClass, Class unwrapperClass, String topTag, MetaDataWrapperManager metaDataWrapperManager) {
		this.name = name;
		this.wrapperClass = wrapperClass;
		this.unwrapperClass = unwrapperClass;
		this.topTag = topTag;
		this.metaDataWrapperManager = metaDataWrapperManager;
	}

	@Override
	public String toString() {
		if (name != null) {
			return name;
		}
		return "New filter";

	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null) {
			return false;
		}
		if (obj.getClass() != this.getClass()) {
			return false;
		}

		MetaDataWrapperPlugin other = (MetaDataWrapperPlugin) obj;
		if (name != other.getName()) {
			return false;
		}
		if (wrapperClass != other.getWrapperClass()) {
			return false;
		}
		if (unwrapperClass.getClass() != other.getUnwrapperClass()) {
			return false;
		}
		return true;
	}

	@Override
	public int hashCode() {
		return (name + wrapperClass.getName() + unwrapperClass.getName()).hashCode();
	}

	/**
	 * @return Returns the name.
	 */
	public String getName() {
		return name;
	}

	/**
	 * @param name The name to set.
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * @returns the unwrapper class
	 */
	public Class getUnwrapperClass() {
		return unwrapperClass;
	}

	/**
	 * @return Returns an instance of the unwrapper.
	 */
	public AbstractMetaDataUnwrapper getUnwrapper() throws XenaException {
		try {
			Object object = unwrapperClass.newInstance();
			if (object instanceof AbstractMetaDataUnwrapper) {
				return (AbstractMetaDataUnwrapper) object;
			}
			throw new XenaException("Could not create unwrapper!");
		} catch (InstantiationException ie) {
			throw new XenaException(ie);
		} catch (IllegalAccessException iae) {
			throw new XenaException(iae);
		}
	}

	/**
	 * set the unwrapper class
	 * @param unwrapperClass
	 */
	public void setUnwrapper(Class unwrapperClass) {
		this.unwrapperClass = unwrapperClass;
	}

	/**
	 * Set the unwrapper using an instance of a class.
	 * @param unwrapper The new value to set unwrapper to.
	 */
	public void setUnwrapper(AbstractMetaDataUnwrapper unwrapper) {
		unwrapperClass = unwrapper.getClass();
	}

	/**
	 * @return Returns the wrapper.
	 */
	public Class getWrapperClass() {
		return wrapperClass;
	}

	/**
	 * @return Returns an instance of the wrapper class.
	 */
	public AbstractMetaDataWrapper getWrapper() throws XenaException {
		try {
			Object object = wrapperClass.newInstance();
			if (object instanceof AbstractMetaDataWrapper) {
				AbstractMetaDataWrapper wrapper = (AbstractMetaDataWrapper) object;
				wrapper.setMetaDataWrapperManager(metaDataWrapperManager);
				return wrapper;
			}
			throw new XenaException("Could not create unwrapper!");
		} catch (InstantiationException ie) {
			throw new XenaException(ie);
		} catch (IllegalAccessException iae) {
			throw new XenaException(iae);
		}
	}

	public AbstractMetaDataWrapper getEmbeddedWrapper() throws XenaException {
		AbstractMetaDataWrapper wrapper = getWrapper();
		wrapper.setEmbedded(true);
		return wrapper;
	}

	public void setWrapper(Class wrapperClass) {
		this.wrapperClass = wrapperClass;
	}

	/**
	 * @param wrapper The new value to set wrapper to.
	 */
	public void setWrapper(AbstractMetaDataWrapper wrapper) {
		wrapperClass = wrapper.getClass();
	}

	/**
	 * @return Returns the topTag.
	 */
	public String getTopTag() {
		return topTag;
	}

	/**
	 * @param topTag The new value to set topTag to.
	 */
	public void setTopTag(String topTag) {
		this.topTag = topTag;
	}

	/**
	 * @return Returns the metaDataWrapperManager.
	 */
	public MetaDataWrapperManager getMetaDataWrapperManager() {
		return metaDataWrapperManager;
	}

	/**
	 * @param metaDataWrapperManager The new value to set metaDataWrapperManager to.
	 */
	public void setMetaDataWrapperManager(MetaDataWrapperManager metaDataWrapperManager) {
		this.metaDataWrapperManager = metaDataWrapperManager;
	}

}
