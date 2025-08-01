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

package uk.ac.kcl.cch.jb.pliny.browser;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.ImageLoader;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Display;

import uk.ac.kcl.cch.jb.pliny.PlinyPlugin;
import uk.ac.kcl.cch.jb.pliny.dnd.ClipboardHandler;
import uk.ac.kcl.cch.jb.pliny.model.AttributedResourceHandler;
import uk.ac.kcl.cch.jb.pliny.model.ICachingResource;
import uk.ac.kcl.cch.jb.pliny.model.IHasAttributeProperties;
import uk.ac.kcl.cch.jb.pliny.model.ObjectType;
import uk.ac.kcl.cch.jb.pliny.model.Resource;
import uk.ac.kcl.cch.jb.pliny.model.VirtualResource;
import uk.ac.kcl.cch.jb.pliny.parts.IHasCachedThumbnail;

/***
 * VirtualBrowserResource is used by the Pliny integrated web browser to
 * represent an item that needs to be shown for a reference/annotation area -- 
 * but there is as of yet no data in the reference/annotation area
 * and hence a corresponding Resource to hold it is not yet
 * needed in the backing store DB.  It gets most of this functionality from
 * VirtualResource, and extends it by adding support to capture and track an
 * image thumbnail of the webpage.
 * 
 * @author Bradley
 */

public class VirtualBrowserResource extends VirtualResource 
implements IHasAttributeProperties, IHasCachedThumbnail{
	
	public static final int browserObjectType = 2;

	private BrowserViewer browserViewer;
	
	private AttributedResourceHandler attrHandler = new AttributedResourceHandler(this);
	
	public VirtualBrowserResource(BrowserViewer browserViewer){
		super();
		this.browserViewer = browserViewer;
		setObjectType(ObjectType.getItem(browserObjectType));
	}
	
	public void setBrowserViewer(BrowserViewer b){
		browserViewer = b;
	}
	
	public void makeMeReal(){
		if(this.getALID() != 0)return;
		super.makeMeReal();
		this.getSavedID(); // store saved ID for possible future delete undos
		if(browserViewer != null)
		   //PlinyPlugin.getDefault().createThumbnail(browserViewer.getBrowser(), this);
		  this.createThumbnail(browserViewer.getBrowser());
	}
	
	public void reIntroduceMe(){
		super.reIntroduceMe();
		int oldId = getSavedID();
		if(oldId == this.getALID())return;
		String thumbName = "w"+oldId+".jpg";
		//File file = new File(PlinyPlugin.getDefault().getImageCachePath(),thumbName);
		File file = PlinyPlugin.getDefault().getWebCache().getCacheFile(oldId, "jpg");
		if(file.exists()){
			//File newFile = new File(PlinyPlugin.getDefault().getImageCachePath(),"w"+getALID()+".jpg");
			File newFile = PlinyPlugin.getDefault().getWebCache().getCacheFile(this);
			file.renameTo(newFile);
		}
	}
	
	public void deleteMe(){
		getSavedID();
		super.deleteMe();
	}
	
	public String getCutCopyText(){
		StringBuffer buf = new StringBuffer();
		buf.append(getFullName());
		String theUrl = this.getIdentifier().substring(4);
		if(theUrl != null && theUrl.length() > 0){
			   buf.append(ClipboardHandler.generateSeparator());
			   buf.append(theUrl);
		}
		return buf.toString();
	}
	
	public int getSashValue(){
		return attrHandler.getInt("sash");
	}
	
	public void setSashValue(int val){
		attrHandler.updateInt("sash",val);
	}

	/**
	 * gets the thumbnail cache ID for this web page.
	 * 
	 */
	@Override
	public int getCacheNumber() {
		int rslt = attrHandler.getInt("cache");
		if(rslt == 0){
			rslt = getALID();
			attrHandler.updateInt("cache", rslt);
		}
		return rslt;
	}

	@Override
	public void setCacheNumber(int numb) {
		attrHandler.updateInt("cache", numb);
	}

	@Override
	public String getExtension() {
		return "jpg";
	}

	static final int thumbWidth = 150;
	static final int thumbHeight =150;

	public ImageDescriptor createThumbnail(Browser browser){
		if(this.isNotPersisting())return null;
		if(getMyThumbnailFile().exists())return getMyThumbnailDescriptor();
		return makeThumbnail(browser);
	}
		
	public ImageDescriptor makeThumbnail(Browser browser) {
		Image image = null;
		// GC does not work properly for EDGE Browser type
		// System.out.println("BrowserType: "+browser.getBrowserType());
		if(browser.getBrowserType().equals("edge"))
			image = getImageFromScreenCapture(browser);
		else image = getImageFromCopyArea(browser);
		if(image == null)return null;
		// Rectangle imageArea = browser.getClientArea(); // is this right? j.b.
        // GC gc = new GC(browser);
        // final Image image = new Image(browser.getDisplay(), imageArea.width, imageArea.height);
        // gc.copyArea(image, 0, 0);
        // gc.dispose();
        
        ImageData data = image.getImageData();
//      save image 
        //ImageLoader imageLoader = new ImageLoader(); 
        //imageLoader.data = new ImageData[] { image.getImageData() }; 
        //imageLoader.save("c:/test.jpg", SWT.IMAGE_JPEG); // fails 

		Rectangle imageArea = image.getBounds();
        int width = imageArea.width;
		int height = imageArea.height;
		if(width > thumbWidth){
			if(height < width){
				width = thumbWidth;
				height = (width*imageArea.height)/imageArea.width;
			} else {
				height = thumbHeight;
				width = (height * imageArea.width)/imageArea.height;
			}
			
		} else if(height > thumbWidth){
			height = thumbHeight;
			width = (height * imageArea.width)/imageArea.height;
		}
		ImageData thumbData;
		if(width != imageArea.width){
			final Image scaledImage = new Image(Display.getDefault(),width,height);
		    GC gc2 = new GC(scaledImage);
	        //gc2.setAntialias(SWT.ON);
	        gc2.setInterpolation(SWT.HIGH);
		    gc2.drawImage(image,0,0,imageArea.width,imageArea.height,0,0,width,height);
		    gc2.dispose();

		   thumbData = scaledImage.getImageData();
		   scaledImage.dispose();
		} else thumbData = data;
        image.dispose();
		// ImageLoader loader = new ImageLoader();
		// loader.data = new ImageData[]{thumbData};
		
		return PlinyPlugin.getDefault().getWebCache().createThumbnail(this, thumbData);
		
		//String thumbName = "w"+this.getSavedID()+".jpg";
		//File file = new File(getImageCachePath(),thumbName);
		//String fullThumbName = file.getAbsolutePath();
		//loader.save(fullThumbName, SWT.IMAGE_JPEG);
		//loader.save(fname, SWT.IMAGE_PNG);
		/*
		 * One would think that having created and saved the image, and having the
		 * imageData, that the createFromImageData could be returned.  Indeed, this
		 * >usually< works, but it doesn't seem to when the image goes through the
		 * depth==1 code above.  Getting the image by getting the ImageDescriptor
		 * from the file (via a URL) seems to work in both cases!   ... jb
		 */
		//URL theUrl = null;
		//try {
		//	theUrl = new URL("file:///"+fullThumbName);
		//} catch (MalformedURLException e) {
		//	return ImageDescriptor.createFromImageData(thumbData);
		//}
		//return ImageDescriptor.createFromURL(theUrl);
		
	}
	
	private Image getImageFromScreenCapture(Browser browser) {
		Rectangle imageArea = browser.getClientArea();
		Point bPlace = browser.toDisplay(0, 0);
		imageArea = new Rectangle(imageArea.x+bPlace.x, imageArea.y+bPlace.y, imageArea.width, imageArea.height);

		final Display display = Display.getCurrent();
		Rectangle displayBounds = display.getBounds();

		if(!(displayBounds.contains(imageArea.x, imageArea.y) && (displayBounds.contains(imageArea.x+imageArea.width, imageArea.y+imageArea.height))))
			// Browser display is in part off the screen
			return null;

		final Image disImage = new Image(display, displayBounds.width, displayBounds.height);
		GC disGC = new GC(display);
		disGC.copyArea(disImage, 0, 0); // disImage now has screen shot of full computer screen in it
		
		final Image rslt = new Image(display, imageArea.width, imageArea.height);
	    GC rsltGC = new GC(rslt);
	    rsltGC.drawImage(disImage, imageArea.x, imageArea.y, imageArea.width, imageArea.height, 0, 0, imageArea.width, imageArea.height);
	    disGC.dispose();
	    rsltGC.dispose();
	    disImage.dispose();
	    return rslt;
	}

	private Image getImageFromCopyArea(Browser browser) {
		Rectangle imageArea = browser.getClientArea(); // is this right? j.b.
        GC gc = new GC(browser);
        final Image image = new Image(browser.getDisplay(), imageArea.width, imageArea.height);
        gc.copyArea(image, 0, 0);
        gc.dispose();
        return image;
	}

	public File getMyThumbnailFile() {
		return PlinyPlugin.getDefault().getWebCache().getThumbnailFile(this);
	}
	
	public ImageDescriptor getMyThumbnailDescriptor(){
		//return PdfAnnotPlugin.getDefault().getThumbnailFromImageCache(getCacheNumber());
		File thumbFile = PlinyPlugin.getDefault().getWebCache().getThumbnailFile(this);
		if(thumbFile.exists())
			return ImageDescriptor.createFromFile(null, thumbFile.getAbsolutePath());
		return null;
	}

}
