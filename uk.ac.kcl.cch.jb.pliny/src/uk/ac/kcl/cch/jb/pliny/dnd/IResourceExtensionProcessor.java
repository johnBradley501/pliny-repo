/*******************************************************************************
 * Copyright (c) 2007 John Bradley
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     John Bradley - initial API and implementation
 *******************************************************************************/

package uk.ac.kcl.cch.jb.pliny.dnd;

import java.io.InputStream;

import org.eclipse.draw2d.IFigure;
import org.eclipse.swt.dnd.DropTargetEvent;
import org.eclipse.ui.part.ViewPart;

import uk.ac.kcl.cch.jb.pliny.model.ObjectType;
import uk.ac.kcl.cch.jb.pliny.model.Resource;
import uk.ac.kcl.cch.jb.pliny.utils.PlinyImportException;

/**
 * Plugins who wish to interact with Pliny should implement this interface.
 * See the extension point defined by the Pliny Plugin 'resourceExtensionProcessor'.
 * <p>
 * The interface makes it possible for Pliny to do several things with the 
 * the implementor:
 * <ul>
 * <li>Drag and Drop: a plugin can choose to require that materials that
 * are to be shared with Pliny must be imported by dropping them onto
 * Pliny's Resource Explorer.  This is done with the Image
 * and PDF plugins, where in both cases the resource is cached, and in the case
 * of Images, the dropped object might be a webbed page that must be harvested for
 * suitable images.  This interface makes it possible for Pliny to call code implemented
 * here when a drop is done. Methods involved in supporting Drag and Drop are:
 * <ul>
 * <li><code>canHandleObject</code>
 * <li><code>processDrop</code>
 * </ul>
 * <li>Defining a Resource: when a resource is being fetched from the backing DB it
 * can always be created as a Resource.  In some cases, however, a plugin may want
 * its resources to be setup as a class derived from Resource.  Code here allows this
 * to happen.
 * <ul>
 * <li><code>makeMyResource</code>
 * <li><code>getSource</code>
 * </ul>
 * <li>Displaying resource content: When a resource is displayed in a Pliny-supported
 * viewer or editor by virtue of appearing in a LinkableObject it should be able to
 * get the Resource Object's content from the owning plugin.  This is supported here.
 * <ul>
 * <li><code>getContentFigure</code>
 * </ul>
 * </ul>
 * <p>
 * Which IResourceExtensionProcessor to use is determined by the resource's
 * {@link uk.ac.kcl.cch.jb.pliny.model.ObjectType ObjectType}.  Thus, the implementor must
 * identify which resource type he is for by providing it through method
 * <code>getMyObjectType</code>
 * 
 * @see uk.ac.kcl.cch.jb.pliny.dnd.AbstractResourceExtensionProcessor AbstractResourceExtensionProcessor
 * which provides some useful common code to support drop processing.
 * 
 * @author John Bradley
 *
 */

public interface IResourceExtensionProcessor {
	
	/**
	 * Pliny's Resource Explorer will make its ViewPart available to the implementor
	 * of this interface by calling this method at the time that this
	 * processor is setup by the Explorer.  Store it in your implementation
	 * if you need it for future use.
	 * 
	 * @param myViewTree
	 */
	public void setViewPart(ViewPart myViewPart);
	
	/**
	 * provides the ObjectType that this Processor is for.
	 * Pliny will look at the ObjectType associated with the Resource it is
	 * working with to decide which IResourceExtensionProcessor to invoke.
	 * 
	 * @return ObjectType this processor will support
	 */
	public ObjectType getMyObjectType();
	
	// code to support dropping of objects in the Resource Explorer

	/**
	 * processes an object that has been dropped onto an ObjectType folder within
	 * the Resource Explorer.  Details about the dropped object are provided by
	 * the {@link org.eclipse.swt.dnd.DropTargetEvent}.
	 * 
	 * @param event
	 */
	public boolean processDrop(DropTargetEvent event);
	
	/**
	 * tests an object that has been dropped onto an ObjectType folder within
	 * the Resource Explorer to see if this object can process it.  The object
	 * passed as a parameter is the <code>data</code> field in
	 * the {@link org.eclipse.swt.dnd.DropTargetEvent}.
	 * 
	 * @param data
	 */
	public boolean canHandleObject(Object data);

	// code to support the creation of appropriate Resource class
	
	/**
	 * generate an empty instance of the appropriate Resource class that
	 * this plugin expects to use for its Resources.  The constructor should
	 * create an instance, but <i>not</i> write it to the DB. (Often this is
	 * the rdb2java object constructor with the parameter boolean realEmpty set to
	 * <code>true</code>, e.g. <code>new Resource(true)</code>).  This is because
	 * this method is invoked on pre-existing DB resource data, and the caller 
	 * within Pliny will
	 * fill data that has already been read from the DB into this created instance.
	 * 
	 * @see uk.ac.kcl.cch.jb.pliny.utils.PlinyArchiveImporter PlinyXMLImporter
	 */
	public Resource makeMyResource();
	
	/**
	 * return an appropriate instance of IResourceExtensionProcessorSource.
	 * @return the appropriate IResourceExtensionProcessorSource
	 */
    public IResourceExtensionProcessorSource getSource();	
	
    // code to support content generation for display of Resource in a Reference Object
    
    /**
     * return an IFigure object that can be used to provide a 'content' display
     * by Pliny.  For resources that represent an image, this is likely to be
     * an IFigure containing a thumbnail represention of that image.
     * 
     * @param resource the Resource the IFigure should be about. 
     */
	public IFigure getContentFigure(Resource resource);
	
	// code to support cache handing for Pliny archiving
	
	public class CacheElement {
		public String fileName;
		public InputStream inputStream;
	}
	
	/**
	 * provides access to cache data you want stored in the pliny archive file.
	 * 
	 * @return an array of CacheElements that tell the archiver what data is to
	 * be put in the archive file, and give a handle to a Stream that can provide
	 * it.
	 */
	public CacheElement[] getCacheElements(Resource r);
	
	/**
	 * provides a way for the Pliny archive importer to give your access to
	 * data from its archive that you will want to store in a local cache.
	 * <p>
	 * Once the archive reader has invoked your caching code through this
	 * method, your code can request data for caching by calling
	 * {@link uk.ac.kcl.cch.jb.pliny.dnd.IGetsArchiveEntries#getArchiveEntry}
	 * 
	 * @param archive IGetsArchiveEntries archive process that can provide you
	 * with access to input streams from the archive containing the data you want
	 * to put in the cache.
	 * 
	 * @param r Resource the cache data should belong to.
	 * @throws PlinyImportException
	 */
	public void processArchiveEntries(IGetsArchiveEntries archive, Resource r)
	throws PlinyImportException;
}
