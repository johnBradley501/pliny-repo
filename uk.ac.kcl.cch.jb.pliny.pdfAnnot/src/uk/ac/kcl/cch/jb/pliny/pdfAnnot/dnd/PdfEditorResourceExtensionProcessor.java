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

package uk.ac.kcl.cch.jb.pliny.pdfAnnot.dnd;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.draw2d.ColorConstants;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.ImageFigure;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.swt.graphics.Image;

import uk.ac.kcl.cch.jb.pliny.dnd.AbstractResourceExtensionProcessor;
import uk.ac.kcl.cch.jb.pliny.dnd.IGetsArchiveEntries;
import uk.ac.kcl.cch.jb.pliny.model.ObjectType;
import uk.ac.kcl.cch.jb.pliny.model.Resource;
import uk.ac.kcl.cch.jb.pliny.pdfAnnot.PdfAnnotPlugin;
import uk.ac.kcl.cch.jb.pliny.pdfAnnot.model.PdfResource;
import uk.ac.kcl.cch.jb.pliny.utils.PlinyImportException;

/**
 * the ResourceExtensionProcessor for PDF Resources.
 * 
 * @see uk.ac.kcl.cch.jb.pliny.dnd.AbstractResourceExtensionProcessor
 * 
 * @author John Bradley
 *
 */
public class PdfEditorResourceExtensionProcessor 
extends AbstractResourceExtensionProcessor{

	private static ImageRegistry imageRegistry = PdfAnnotPlugin.getDefault().getImageRegistry();

	//private IWorkbenchPage myPage;

	public PdfEditorResourceExtensionProcessor() {
		super(PdfAnnotPlugin.EDITOR_ID);
		// TODO Auto-generated constructor stub
	}

	public Resource makeMyResource() {
		return new PdfResource(true);
	}

	public ObjectType getMyObjectType() {
		return PdfAnnotPlugin.getMyObjectType();
	}

	public boolean canHandleObject(Object data) {
		if(!(data instanceof Resource))return false;
		Resource theResource = (Resource)data;
		String theIdentifier = theResource.getIdentifier();
		if(!theIdentifier.startsWith("url:"))return false;
		String theUrl = theIdentifier.substring(4,theIdentifier.length());
		IPath myPath = new Path(theUrl);
		String ext = myPath.getFileExtension().toLowerCase();
		return ext.equals("pdf");
	}

	public IFigure getContentFigure(Resource resource) {
		if(!(resource instanceof PdfResource))return null;
		PdfResource myResource = (PdfResource)resource;
		String thumbId = "thumbPdf-"+myResource.getCacheNumber();
		Image myThumbnail = imageRegistry.get(thumbId);
		if(myThumbnail == null){
			ImageDescriptor myDescr = myResource.getMyThumbnailDescriptor();
			if(myDescr == null)return null;
			imageRegistry.put(thumbId, myDescr);
		}
		ImageFigure rslt = new ImageFigure(imageRegistry.get(thumbId));
		rslt.setBackgroundColor(ColorConstants.lightBlue);
		return rslt;
	}

	protected void handleResource(Resource myResource) {
		String theIdentifier = myResource.getIdentifier();
		if(theIdentifier.length() < 4 || !theIdentifier.startsWith("url:"))return;
		String theUrl = theIdentifier.substring(4,theIdentifier.length());
		handleUrl(theUrl, theUrl);
	}

	protected void handleFile(String name) {
		IPath myPath = new Path(name);
		String ext = myPath.getFileExtension().toLowerCase();
		if(ext.equals("url")) handleUrlFile(myPath);
		if(!ext.equals("pdf")) return;
		handlePdfFile(myPath);
	}

	private void handlePdfFile(IPath myPath) {
		URL theURL = makeURLfromFileName(myPath);
		if(theURL == null)return;
		handlePdfUrl(theURL);
	}

	protected boolean handleUrl(URL theURL, String title) {
		try {
			URLConnection connection = theURL.openConnection();
			String mimeType = connection.getContentType();
			if(mimeType.startsWith("text/plain"))
				makeNote(getFileContentsAsString(connection.getInputStream()), title);
			if(mimeType.startsWith("application/pdf"))
				handlePdfUrl(theURL);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
		return true;
	}

	private void handlePdfUrl(URL theURL) {
		IPath thePath = new Path(theURL.getFile());
		String title = thePath.lastSegment();
		InputStream in = null;
		try {
			in = theURL.openStream();
		} catch (IOException e) {
			in = null;
			e.printStackTrace();
		}
		if(in == null)return;
		
		PdfResource myResource = new PdfResource(true);
		myResource.setName(title);
		myResource.setUrl(theURL.toString());
		myResource.reIntroduceMe();
		if(!PdfAnnotPlugin.getDefault().getCache().createCacheItem(in, myResource)){
			myResource.deleteMe();
			return;
		}
		myResource.setObjectType(getMyObjectType());
		newResources.add(myResource);
	}
	
	public CacheElement[] getCacheElements(Resource r){
		if(!(r instanceof PdfResource))return null;
		PdfResource pdfResource = (PdfResource)r;
		String cachedFileName = "c"+pdfResource.getCacheNumber()+".pdf";
		File file = PdfAnnotPlugin.getDefault().getCache().getCacheFile(pdfResource);
		if(!file.exists())return null;
		FileInputStream in = null;
		try {
			in = new FileInputStream(file);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			return null;
		}
		
		CacheElement element1 = new CacheElement();
		element1.fileName = cachedFileName;
		element1.inputStream = in;

		
		CacheElement element2 = null;
		
		String thumbFileName = "t"+pdfResource.getCacheNumber()+".jpg";
		File thumbFile = PdfAnnotPlugin.getDefault().getCache().getThumbnailFile(pdfResource);
		if(thumbFile.exists()){
			element2 = new CacheElement();
			element2.fileName = thumbFileName;
			try {
				element2.inputStream = new FileInputStream(thumbFile);
			} catch (FileNotFoundException e) {
				element2 = null;
				e.printStackTrace();
			}
		}
		
		if(element2 == null)return new CacheElement[]{element1};
		return new CacheElement[]{element1, element2};
	}
	
	public void processArchiveEntries(IGetsArchiveEntries archive, Resource r)
	throws PlinyImportException{
		if(!(r instanceof PdfResource))return;
		PdfResource pdfResource = (PdfResource)r;
		//String dir = PdfAnnotPlugin.getDefault().getPdfCachePath()+"/";

		int oldCacheNumber = pdfResource.getCacheNumber();
		pdfResource.setCacheNumber(pdfResource.getALID());
		String cachedFileName = "c"+oldCacheNumber+".pdf";
		InputStream in = archive.getArchiveEntry(cachedFileName);
		if(in == null)return;
		PdfAnnotPlugin.getDefault().getCache().createCacheItem(in, pdfResource);

		cachedFileName = "t"+oldCacheNumber+".jpg";
		in = archive.getArchiveEntry(cachedFileName);
		if(in == null) return;
		PdfAnnotPlugin.getDefault().getCache().createThumbnail(pdfResource, in);
   }

}
