/*******************************************************************************
 * Copyright (c) 2007, 2012 John Bradley
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     John Bradley - initial API and implementation
 *******************************************************************************/

package uk.ac.kcl.cch.jb.pliny.pdfAnnot.model;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.Vector;

import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.jpedal.PdfDecoder;

import uk.ac.kcl.cch.jb.pliny.model.AttributedResourceHandler;
import uk.ac.kcl.cch.jb.pliny.model.ICachingResource;
import uk.ac.kcl.cch.jb.pliny.model.IHasAttributeProperties;
import uk.ac.kcl.cch.jb.pliny.model.LinkableObject;
import uk.ac.kcl.cch.jb.pliny.model.Resource;
import uk.ac.kcl.cch.jb.pliny.pdfAnnot.PdfAnnotPlugin;
import uk.ac.kcl.cch.jb.pliny.pdfAnnot.SwtImageFromPdf;
import uk.ac.kcl.cch.rdb2java.dynData.FKReferenceList;

/**
 * an extension on the Pliny 
 * {@link uk.ac.kcl.cch.jb.pliny.model.Resource Resource} that
 * contains special data for PDF files.  Most of the extra image data is
 * stored in the Resource's <code>attributes</code> field, packed
 * in via the
 * {@link uk.ac.kcl.cch.jb.pliny.model.AttributedResourceHandler AttributedResourceHandler}.
 * <p>
 * The Page number is not stored in the Resource, and is therefore
 * not persistent from here.  This is because, of course, any particular
 * page number is not really a part of the resource.  It is provided
 * here as a convenient way for the various classes that need to know
 * the current page number being displayed can get at it.
 * 
 * @author John Bradley
 *
 */
public class PdfResource extends Resource 
implements IHasAttributeProperties, ICachingResource{
	
	public static final String CURRENTPAGE_EVENT="PdfResource.currentPage";
	public static final String SCALECHANGE_EVENT="PdfResource.scale";
	
	private AttributedResourceHandler attrHandler = null;

	public PdfResource() {
		super();
		this.setObjectType(PdfAnnotPlugin.getMyObjectType());
		attrHandler = new AttributedResourceHandler(this);
	}

	public PdfResource(boolean empty) {
		super(empty);
		attrHandler = new AttributedResourceHandler(this);
	}
	
	private int currentPage = 1;
	
	public int getCurrentPage(){
		return currentPage;
	}
	
	public void setCurrentPage(int currentPage){
		if(this.currentPage == currentPage)return;
		int oldCurrentPage = this.currentPage;
		this.currentPage = currentPage;
		//myPagedDisplayedItems = null;
		this.firePropertyChange(CURRENTPAGE_EVENT, 
				new Integer(oldCurrentPage),new Integer(currentPage));
	}

	//private FKReferenceList myPagedDisplayedItems = null;
	
	/**
	 * returns the set of 
	 * {@link uk.ac.kcl.cch.jb.pliny.model.LinkableObject LinkableObject}s that should be displayed
	 * on the current page as reference objects.
	 * 
	 * 
	 */
	
	public FKReferenceList getMyPagedDisplayedItems(){
		// needs to be dynamic because when new item added to page it must
		// show up both in this list and in the all-page list maintained 
		// in by this resource.  .. jb
		/*
		if(resourceKey == 0)return FKReferenceList.EMPTY_FKREFERENCELIST;
		if(myPagedDisplayedItems == null)myPagedDisplayedItems = new FKReferenceList(
		     new LinkableObjectQuery(), 
		     "displayedInKey="+resourceKey+ " and displPageNo="+getCurrentPage());
		return myPagedDisplayedItems;
		*/
		Vector curList = getMyDisplayedItems().getItems();
		Iterator it = curList.iterator();
		FKReferenceList rslt = new FKReferenceList();
		while(it.hasNext()){
			LinkableObject obj = (LinkableObject)it.next();
			if(obj.getDisplPageNo()==currentPage)rslt.add(obj);
		}
		return rslt;
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
	 * gets the current scale setting for the PDF page images.
	 * @return float the current scale setting
	 */
	public float getScale(){
		float rslt = attrHandler.getFloat("scale");
		if(rslt == 0f)return 1.0f;
		return rslt;
	}
	
	
	/**
	 * sets the current scale setting for the PDF page images.
	 * @param scale float the current scale setting
	 */
	public void setScale(float scale){
		float oldScale = getScale();
		if(oldScale == scale)return;
		attrHandler.updateFloat("scale", scale);
		this.firePropertyChange(SCALECHANGE_EVENT, 
				new Float(oldScale),new Float(scale));
	}
	
	/**
	 * returns the full path filename for the cached PDF file that
	 * is associated with this resource.
	 * 
	 * @return String the full path filename to the cached file
	 */
	public String getMyCachedPdfFilename(){
		try {
			return PdfAnnotPlugin.getDefault().getCache().getCacheFile(this).getCanonicalPath().toString();
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	public String getExtension(){return "pdf";}
	
	
	/**
	 * returns an JFace ImageDescriptor for the thumbnail image that is
	 * is associated with the PDF file associated with this resource. If the thumbname image does
	 * not current exist, this method also creates it.
	 * 
	 * @return the ImageDescriptor for the associated thumbnail
	 */
	public ImageDescriptor getMyThumbnailDescriptor(){
		//return PdfAnnotPlugin.getDefault().getThumbnailFromImageCache(getCacheNumber());
		File thumbFile = PdfAnnotPlugin.getDefault().getCache().getThumbnailFile(this);
		if(thumbFile.exists())
			return ImageDescriptor.createFromFile(null, thumbFile.getAbsolutePath());
		return createThumbnail();
	}
	
	/**
	 * creates a thumbnail size image of the first page of the PDF file. 
	 * 
	 * @param file PDF file for which the thumbnail is needed.
	 * @param cacheNo the cache ID for the file.
	 * @return
	 */
	private ImageDescriptor createThumbnail() {
		PdfDecoder decodePDF = new PdfDecoder();

		try {
			decodePDF.openPdfFile(getMyCachedPdfFilename());
			
			boolean fileCanBeOpened= false;
			if (decodePDF.isEncrypted()) {
				Shell parentShell = Display.getDefault().getActiveShell();

				InputDialog input = new InputDialog(parentShell,"Password","Enter a password","",null);
				input.open();
				
				String password = input.getValue();
				
	            /** try and reopen with new password */
	            if (password != null) {
	            	decodePDF.setEncryptionPassword(password);
	            	// decodePDF.verifyAccess(); // from version 2.7.1

	                if (decodePDF.isFileViewable())
	                    fileCanBeOpened = true;
	                else
	                    fileCanBeOpened = false;
	            }
	            
	            if(!fileCanBeOpened)
	            	MessageDialog.openInformation(parentShell,"Password","No valid password");

	        }else
	            fileCanBeOpened=true; 
			
			if(fileCanBeOpened){
				decodePDF.setPageParameters(1f,1);
				decodePDF.decodePage(1);
			}
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
		//BufferedImage thumbImage =decodePDF.getPageAsThumbnail(1,120);
		BufferedImage thumbImage =decodePDF.getPageAsThumbnail(120,null);
		decodePDF.closePdfFile();
		return PdfAnnotPlugin.getDefault().getCache().createThumbnail(this, thumbImage);
	}

	
	private SwtImageFromPdf imageMaker = null;
	
	public void setSwtImageFromPdf(SwtImageFromPdf maker){
		imageMaker = maker;
	}
	
	/**
	 * returns a SWT Image of the current page of this PDF file.
	 * 
	 * @return Image of this current page.
	 */
	public Image getPageImage(){
		return imageMaker.getSwtImageForPage(getCurrentPage()/*, getScale()*/);
	}
	
	/**
	 * returns the <code>PdfDecoder</code> which currently is working with
	 * this PDF file.
	 * 
	 * @return Image of this current page.
	 */
	public PdfDecoder getMyPdfDecoder(){
		return imageMaker.getMyPdfDecoder();
	}

	public File getResourceFile(){
		return PdfAnnotPlugin.getDefault().getCache().getCacheFile(this);
	}
}
