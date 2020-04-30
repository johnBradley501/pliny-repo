/*******************************************************************************
 * Copyright (c) 2012 John Bradley
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     John Bradley - initial API and implementation
 *******************************************************************************/
package uk.ac.kcl.cch.jb.pliny.data;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.InputStream;
import java.net.URL;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.graphics.ImageData;

import uk.ac.kcl.cch.jb.pliny.model.ICachingResource;

/**
 * used to support file caching of data for Pliny.  
 * 
 * @author John Bradley
 *
 */
public interface IFileCacheManager {

	/* no longer present. JB
	 * creates object in the file cache. This is done by copying the file from outside
	 * the cache into it
	 * 
	 * @param url points to the object (from outside the cache) to be cached
	 * @param cacheNo numeric ID of cache item
	 * @param type type of the cache (e.g., a thumbnail image of of type "t").  Practice
	 * so far has had types as single letter strings.
	 * @param ext file extension to be used in the cache.
	 * @return true if the caching succeeded.
	 */
	//public boolean createCacheItem(URL url, int cacheNo, String type,
	//		String ext);
	
	/**
	 * takes the data provided in the given InputStream and stores it in a file in the cache.
	 * 
	 * @param in provides the source of the data to cache.
	 * @param resource Pliny Resource that is associated with this data
	 * @return true if the caching succeeded.,
	 */
	public boolean createCacheItem(InputStream in, ICachingResource resource);

	/**
	 * takes the image data provided stores the corrresponding image  in a file in the cache.
	 * 
	 * @param data provides the ImageData for the image to be cached.
	 * @param resource Pliny Resource that is associated with this data
	 * @return true if the caching succeeded.,
	 */
	public boolean createCacheItem(ImageData data, ICachingResource resource);

	
	/**
	 * creates a File object that points to the file in the cache that is associated with
	 * the given Cache specification.
	 * <p> Check that the returned File object exists.  Normally, of course, it will!
	 * 
	 * @param cacheNo numeric ID of the cache item
	 * @param ext file extension of the file that is wanted in the cache.
	 * @return a File object that points to the cache file.
	 */
	public File getCacheFile(int cacheNo, String ext);

	/**
	 * gets a File object that points to a file in the cache that is of the type "type", and
	 * is associated with the CachingResource object.
	 * 
	 * @param resource the Resource that identifies the cache file that is wanted.
	 * @return
	 */
	public File getCacheFile(ICachingResource resource);

	/**
	 * used when creating a thumbnail image for a cached object.  The calling program provides
	 * the Resource item that will "own" the thumbnail, and the data about the thumbnail image
	 * in the form of a BufferedImage (java.awt.image.BufferedImage)
	 * <p>Note that the BufferedImage must already be the image as the right size to be a thumbnail.
	 * 
	 * @param resource Resource that is to own the created thumbnail image
	 * @param thumbImage the thumbnail image data as a BufferedImage object
	 * @return pointer to the newly created thumbnail image as an ImageDescriptor
	 * (org.eclipse.jface.resource.ImageDescriptor)
	 */
	public ImageDescriptor createThumbnail(ICachingResource resource,
			BufferedImage thumbImage);

	/**
	 * used when creating a thumbnail image for a cached object.  The calling program provides
	 * the Resource item that will "own" the thumbnail, and the data about the thumbnail image
	 * in the form of a ImageData (org.eclipse.swt.graphics.ImageData)
	 * <p>Note that the ImageData must already be the image as the right size to be a thumbnail.
	 * 
	 * @param resource Resource that is to own the created thumbnail image
	 * @param thumbImage the thumbnail image data as an ImageData object
	 * @return pointer to the newly created thumbnail image as an ImageDescriptor
	 * (org.eclipse.jface.resource.ImageDescriptor)
	 */
	public ImageDescriptor createThumbnail(ICachingResource resource,
			ImageData thumbData);
	

	/**
	 * used when creating a thumbnail image for a cached object.  The calling program provides
	 * the Resource item that will "own" the thumbnail, and the data about the thumbnail image
	 * as data in an InputStream.
	 * <p>Note that the InputStream must present the image data as the right size to be a thumbnail.
	 * 
	 * @param resource Resource that is to own the created thumbnail image
	 * @param in an InputStream that provides the file data for the thumbnail.
	 * @return pointer to the newly created thumbnail image as an ImageDescriptor
	 * (org.eclipse.jface.resource.ImageDescriptor)
	 */
	public ImageDescriptor createThumbnail(ICachingResource resource, InputStream in);

	/**
	 * creates a File object that points to the thumbnail image in the file cache that corresponds
	 * to the given Resource object.
	 * <p>The caller should check that the returned File object points to a file that actually
	 * exists.
	 * 
	 * @param resource Resource that owns the required thumbnail image
	 * @return File object that points to the thumbnail image in the cache.
	 */
	public File getThumbnailFile(ICachingResource resource);

	public void setThumbFileNamePrefix(String p);
}