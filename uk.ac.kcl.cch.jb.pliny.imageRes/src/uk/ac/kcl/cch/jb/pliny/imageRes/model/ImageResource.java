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

package uk.ac.kcl.cch.jb.pliny.imageRes.model;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.net.MalformedURLException;
import java.net.URL;

import org.eclipse.draw2d.ColorConstants;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.ImageLoader;
import org.eclipse.swt.widgets.Display;

import uk.ac.kcl.cch.jb.pliny.controls.IZoomableImageResource;
import uk.ac.kcl.cch.jb.pliny.imageRes.ImageResPlugin;
import uk.ac.kcl.cch.jb.pliny.model.AttributedResourceHandler;
import uk.ac.kcl.cch.jb.pliny.model.ICachingResource;
import uk.ac.kcl.cch.jb.pliny.model.IHasAttributeProperties;
import uk.ac.kcl.cch.jb.pliny.model.Resource;
import uk.ac.kcl.cch.jb.pliny.parts.IHasCachedThumbnail;
import uk.ac.kcl.cch.jb.pliny.utils.ImageDataTools;

/**
 * an extension on the Pliny 
 * {@link uk.ac.kcl.cch.jb.pliny.model.Resource Resource} that
 * contains special data for Images.  All the extra image data is
 * stored in the Resource's <code>attributes</code> field, packed
 * in via the
 * {@link uk.ac.kcl.cch.jb.pliny.model.AttributedResourceHandler AttributedResourceHandler}.
 * 
 * @author John Bradley
 *
 */
public class ImageResource extends Resource 
implements IHasAttributeProperties, IZoomableImageResource, IHasCachedThumbnail{
	
	private AttributedResourceHandler attrHandler = null;
	
	/**
	 * creates a new instance of the ImageResource and stores it
	 * in the backing store DB.
	 *
	 */
	public ImageResource(){
		super();
		this.setObjectType(ImageResPlugin.getImageObjectType());
		attrHandler = new AttributedResourceHandler(this);
	}
	
	/**
	 * creates a new instance of the ImageResource and if the
	 * parameter is <code>true</code> does <i>i</i> initially
	 * store it in the backing store DB.
	 *
	 */
	public ImageResource(boolean empty){
		super(empty);
		attrHandler = new AttributedResourceHandler(this);
	}
	
	public String getIdentifier(){
		return "resource:"+getALID();
	}
	
	/**
	 * returns the current Image position within the editors
	 * annotation area (as a draw2d Rectange).
	 * 
	 */
	public Rectangle getImagePosition(){
		return attrHandler.getRectangle("imgPos");
	}
	
	
	/**
	 * sets the current Image position within the editors
	 * annotation area (as a draw2d Rectange) to the given value.
	 * 
	 */
	public void setImagePosition(Rectangle r){
		attrHandler.updateRectangle("imgPos", r);
	}
	
	/**
	 * gets the URL (as a character string) for the original version of the image referenced
	 * by this Resource.
	 * 
	 */
	public String getUrl(){
		return attrHandler.getString("url");
	}
	
	/**
	 * sets the URL that points to the original version of the image referenced
	 * by this Resource.
	 * 
	 */
	public void setUrl(String url){
		attrHandler.updateString("url", url);
	}
	
	/**
	 * gets the filename extension (and by implication, the image type) for
	 * the image referenced by this Resource.
	 * 
	 */
	public String getExtension(){
		return attrHandler.getString("extension");
	}
	
	/**
	 * sets the filename extension (and by implication, the image type) for
	 * the image referenced by this Resource.
	 * 
	 */
	public void setExtension(String ext){
		attrHandler.updateString("extension", ext);
	}
	
	/**
	 * gets the current zoomsize for the image referenced by this Resource.
	 * 
	 */
	public int getZoomSize(){
		int rslt = attrHandler.getInt("zoom");
		if(rslt == 0){
			Rectangle r = getImagePosition();
			rslt = r.width;
			if(rslt > 500)rslt = 500;
		}
		return rslt;
	}
	
	/**
	 * sets the current zoomsize for the image referenced by this Resource.
	 * 
	 */
	
	public void setZoomSize(int zoom){
		attrHandler.updateInt("zoom", zoom);
	}
	
	/**
	 * gets the image cache ID for this image.
	 * 
	 */
	public int getCacheNumber(){
		int rslt = attrHandler.getInt("cache");
		if(rslt == 0){
			rslt = getALID();
			attrHandler.updateInt("cache", rslt);
		}
		return rslt;
	}
	
	/**
	 * sets the image cache ID for this image.
	 * 
	 */
	public void setCacheNumber(int numb){
		attrHandler.updateInt("cache", numb);
	}
	
	
	/**
	 * gets the URL (as a java URL object) for the cached version of the image referenced
	 * by this Resource.
	 * 
	 */
	private URL getMyUrl(){
		URL theImageUrl;
		if(getUrl() == null || getUrl().length() == 0)return null;
		try {
			theImageUrl = new URL(getUrl());
		} catch (MalformedURLException e) {
			return null;
		}
		return theImageUrl;
	}
	
	/**
	 * gets an image descriptor handle to the image in the Image cache
	 * for this image.
	 * 
	 * @return an ImageDescriptor for this image.
	 */
	public ImageDescriptor getMyImageDescriptor(){
		URL theImageUrl = getMyUrl();
		if(theImageUrl == null)return null;
		String cacheExtension = getExtension();
		File imageFile = ImageResPlugin.getDefault().getCache().getCacheFile(this);
		if(imageFile.exists())
			return ImageDescriptor.createFromFile(null, imageFile.getAbsolutePath());
		return null;
	}
	
	
	/**
	 * gets an image descriptor handle to the thumbnail version of
	 * this image (stored in the cache). If the thumbnail image does not currently exist, 
	 * this method also creates it.
	 * 
	 * @return an ImageDescriptor for this tumbnail image.
	 */
	public ImageDescriptor getMyThumbnailDescriptor(){
		URL theImageUrl = getMyUrl();
		if(theImageUrl == null)return null;
		File thumbFile = ImageResPlugin.getDefault().getCache().getThumbnailFile(this);
		if(thumbFile.exists())
			return ImageDescriptor.createFromFile(null, thumbFile.getAbsolutePath());
		return createThumbnail(thumbFile);
	}
	
	public File getMyThumbnailFile() {
		URL theImageUrl = getMyUrl();
		if(theImageUrl == null)return null;
		File thumbFile = ImageResPlugin.getDefault().getCache().getThumbnailFile(this);
		if(!thumbFile.exists())createThumbnail(thumbFile);
		return thumbFile;
	}
	
	static final int thumbWidth = 120;
	static final int thumbHeight =120;
	static final Dimension thumbSize = new Dimension(thumbWidth, thumbHeight);

	private ImageDescriptor createThumbnail(File thumbFile){
		ImageData thumbData = ImageDataTools.getInstance().createThumbImage(ImageDescriptor.createFromFile(null, getResourceFile().getAbsolutePath()), thumbSize);
		return ImageResPlugin.getDefault().getCache().createThumbnail(this, thumbData);
	}

	public File getResourceFile(){
		URL theImageUrl = getMyUrl();
//		return ImageResPlugin.getDefault().getCacheFile(theImageUrl, getCacheNumber(), getExtension());
		return ImageResPlugin.getDefault().getCache().getCacheFile(this);
	}

}
