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

package uk.ac.kcl.cch.jb.pliny.imageRes.dnd;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.Charset;
import java.util.Iterator;
import java.util.Vector;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.draw2d.ColorConstants;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.ImageFigure;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.swt.SWT;
import org.eclipse.swt.SWTException;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.ui.IWorkbenchPage;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import jdk.nashorn.internal.parser.JSONParser;
import uk.ac.kcl.cch.jb.pliny.browser.BrowserViewer;
import uk.ac.kcl.cch.jb.pliny.browser.VirtualBrowserResource;
import uk.ac.kcl.cch.jb.pliny.dnd.AbstractResourceExtensionProcessor;
import uk.ac.kcl.cch.jb.pliny.dnd.IGetsArchiveEntries;
import uk.ac.kcl.cch.jb.pliny.imageRes.ImageResPlugin;
import uk.ac.kcl.cch.jb.pliny.imageRes.controls.HtmlImagesSelectionDialog;
import uk.ac.kcl.cch.jb.pliny.imageRes.model.ImageResource;
import uk.ac.kcl.cch.jb.pliny.model.LOType;
import uk.ac.kcl.cch.jb.pliny.model.LinkableObject;
import uk.ac.kcl.cch.jb.pliny.model.NoteLucened;
import uk.ac.kcl.cch.jb.pliny.model.ObjectType;
import uk.ac.kcl.cch.jb.pliny.model.Resource;
import uk.ac.kcl.cch.jb.pliny.model.VirtualResource;
import uk.ac.kcl.cch.jb.pliny.utils.PlinyImportException;

/**
 * the ResourceExtensionProcessor for Image Resources.
 * 
 * @see uk.ac.kcl.cch.jb.pliny.dnd.AbstractResourceExtensionProcessor
 * 
 * @author John Bradley
 *
 */
public class ImageEditorResourceExtensionProcessor extends AbstractResourceExtensionProcessor {

	private static ImageRegistry imageRegistry = ImageResPlugin.getDefault().getImageRegistry();

	//private ViewPart myView = null;
	//private IWorkbenchPage myPage;
	//private ObjectType myObjectType = null;
	
	//private static ObjectType noteObjectType = null;

	public ImageEditorResourceExtensionProcessor() {
		super(ImageResPlugin.EDITOR_ID);
	}
	
	public ImageEditorResourceExtensionProcessor(IWorkbenchPage thePage){
		super(thePage, ImageResPlugin.EDITOR_ID);
	}
	
	public void processUrl(URL theURL, String title){
	    newResources = new Vector();
		handleUrl(theURL, title);
	}
	
	private HtmlImageData makeDataFromJSON(JSONObject input){
		String altText = null;
		String url = null;
		int width = 0;
		int height = 0;
		try {
			altText = input.getString("alt");
			url = input.getString("src");
		} catch (JSONException e) {
			e.printStackTrace();
			return null;
		}
		URL theURL;
		try {
			theURL = new URL(url);
		} catch (MalformedURLException e) {
			// e.printStackTrace();
			return null;
		}
		HtmlImageData rslt = new HtmlImageData(altText, theURL);
		boolean gotDims = false;
		try {
			width = input.getInt("width");
			height = input.getInt("height");
			gotDims = true;
		} catch (JSONException e) {
		}
		if(gotDims)rslt.setDimension(width, height);
		return rslt;
		
	}
	
	// http://stackoverflow.com/questions/7792385/how-to-talk-to-a-javascript-function-from-swt
	// http://blog.vogella.com/2009/12/21/javascript-swt/
	
	public boolean processBrowserPage(BrowserViewer browserViewer){
		if(browserViewer == null)return false;
		Browser theBrowser = browserViewer.getBrowser();
		if(theBrowser == null)return false;
		String script = "imgs = document.images;\nimgdata = new Array();\nfor (i = 0; i < imgs.length; i++){\n"+
              "   current = imgs[i];\n   curdata = new Object();\n   curdata['src'] = current.src;\n   curdata['height'] = current.naturalHeight;\n"+
			  "   curdata['width'] = current.naturalWidth;\n   curdata['alt'] = current.alt;\n   imgdata[imgdata.length] = curdata;\n}"+
		      "return JSON.stringify(imgdata);";
		String rslt = (String) theBrowser.evaluate(script, true);
		if(rslt == null)return false;

		JSONArray data = null;
		Vector images = new Vector();
		
		URL theURL;
		try {
			theURL = new URL(theBrowser.getUrl());
		} catch (MalformedURLException e1) {
			e1.printStackTrace();
			return true; // should this be "false" (so that calling code tries alternate approach?   ..jb
		}
		String htmlText = theBrowser.getText();
		InputStream in;
		in = new ByteArrayInputStream(htmlText.getBytes(Charset.forName("UTF-8")));
		HtmlConverter converter = new HtmlConverter(in, theURL);
		String htmlPageTitle = converter.getTitle();
		String contents = converter.getTextualContents().trim();

		try {
			data = new JSONArray(new JSONTokener(rslt));
			for(int i = 0; i < data.length(); i++){
				HtmlImageData id = makeDataFromJSON(data.getJSONObject(i));
				if(id != null){
				    Dimension dd = id.getDimension();
				    if((dd.height >= minImportSize.height) && (dd.width >= minImportSize.width))
					    images.add(id);
				}

			}
		} catch (JSONException e) {
			e.printStackTrace();
			return false;
		}
		
		processListOfImages(images, theURL, htmlPageTitle, contents);
		return true;
	}
	
	public Vector getTheNewImages(){
		return newResources;
	}
	
	protected boolean handleUrl(URL theURL, String title){
		try {
			URLConnection connection = theURL.openConnection();
			String mimeType = connection.getContentType();
			if(mimeType.startsWith("text/plain"))
				makeNote(getFileContentsAsString(connection.getInputStream()), title);
			if(mimeType.startsWith("text/html"))
				handleHTMLStream(connection.getInputStream(), theURL);
			else if(mimeType.startsWith("image")){
				HtmlImageData data = new HtmlImageData("", theURL);
			    makeImage(data, title);
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
		return true;
	}
	
	private String getExtension(ImageData data){
		String ext = null;
		if(data.type == SWT.IMAGE_GIF)ext = "gif";
		else if(data.type == SWT.IMAGE_JPEG)ext = "jpg";
		else if(data.type == SWT.IMAGE_PNG)ext = "png";
		else if(data.type == SWT.IMAGE_BMP)ext = "bmp";
		// else ext = path.getFileExtension();
		return ext;
	}
	
	private ImageResource makeImage(HtmlImageData imageData, String title){
		ImageData theImage = imageData.getTheImageData();
		String theExt = getExtension(theImage);
		if(theExt == null)return null;
		
		if((title == null) || (title.trim().equals(""))){
			title = imageData.getTheURL().toString();
		}
		ImageResource myImage = new ImageResource(true);
		myImage.setName(title);
		myImage.setUrl(imageData.getTheURL().toString());
		myImage.setExtension(theExt);
		myImage.reIntroduceMe();
		if(!ImageResPlugin.getDefault().getCache().createCacheItem(theImage, myImage)){
			myImage.deleteMe();
			return null;
		}
		myImage.setObjectType(getMyObjectType());
		newResources.add(myImage);
		return myImage;
	}
	
	protected void handleString(String input){
		input = input.replaceAll("\r","").trim();
		if(input.startsWith("http:") || input.startsWith("ftp:")  || input.startsWith("file:")){
			String[]inputParts = (input+"\n \n").split("\n");
			if(handleUrl(inputParts[0], inputParts[1]))return;
		}
		makeNote(input, "New Note");
	}
	
	private void handleHTMLFile(IPath myPath){
		URL theURL = makeURLfromFileName(myPath);
		if(theURL == null)return;
		FileInputStream input = null;
		try {
			input = new FileInputStream(myPath.toFile());
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return;
		}
		handleHTMLStream(input, theURL);
	}
	
	private static final Dimension minImportSize = new Dimension(150,150);
	
	private void processImageInHTML(HtmlImageData imageData, String title, NoteLucened note, Resource myHtmlResource){
		String imageName = imageData.getAltText();
		if(imageName == null)imageName = title;
		ImageResource newImage = makeImage(imageData, imageName);
		if(newImage == null)return;
		
		ImageData dta = imageData.getTheImageData();
		int imgWidth = dta.width; int imgHeight = dta.height;
		Rectangle r = new Rectangle(100,100,imgWidth, imgHeight);
		newImage.setImagePosition(r);
		
		LinkableObject lo = new LinkableObject();
		lo.setLoType(LOType.getCurrentType());
		lo.setSurrogateFor(note);
		lo.setShowingMap(false);
		// public Rectangle(int x, int y, int width, int height) {
		lo.setDisplayRectangle(new Rectangle(1,1,200,100));
		lo.setDisplayedIn(newImage);
		
		lo = new LinkableObject();
		lo.setSurrogateFor(myHtmlResource);
		lo.setDisplayRectangle(new Rectangle(1,200,200,100));
		lo.setLoType(LOType.getBibRefType());
		lo.setIsOpen(false);
		lo.setDisplayedIn(newImage);
	}
	
	private void handleHTMLStream(InputStream in,URL theURL){
		HtmlConverter converter = new HtmlConverter(in, theURL);
		String htmlPageTitle = converter.getTitle();
		String contents = converter.getTextualContents().trim();
		Vector images = new Vector();
		HtmlImageData[] imageData = converter.getImageData();
		for(int i=0; i<imageData.length;i++){
			Dimension dd = imageData[i].getDimension();
			if((dd.height >= minImportSize.height) && (dd.width >= minImportSize.width))
				images.add(imageData[i]);
	    }
		processListOfImages(images, theURL, htmlPageTitle, contents);
	}
		
	private void processListOfImages(Vector images, URL theURL, String htmlPageTitle, String contents){
		if(images.size() == 0)
		   MessageDialog.openInformation(null, "No images", "No suitable images were found on this page");
		
		// there are some images that need processing
		if(images.size() > 1)
			images = handleUserSelectionOfImages(images);
		
		if(images == null)return;
		if(images.size() == 0)
		   MessageDialog.openInformation(null, "No images", "No images were selected by you from this page");
		
		String id = "url:"+theURL.toExternalForm();
		ObjectType htmlObjectType = ObjectType.getItem(2);
		Resource myHtmlResource = Resource.find(htmlObjectType, id);
		boolean isVirtual = false;
		if(myHtmlResource == null){
		   myHtmlResource = new VirtualBrowserResource(null);
		   myHtmlResource.setIdentifiers(id);
		   myHtmlResource.setObjectType(ObjectType.getItem(2));
		   myHtmlResource.setName(htmlPageTitle);
		   isVirtual = true;
		}
		
		if(isVirtual)((VirtualResource)myHtmlResource).makeMeReal();
		
		NoteLucened newNote = new NoteLucened();
		newNote.setName(htmlPageTitle);
		contents = contents+"\n\nFrom URL: "+theURL.toString();
		newNote.setContent(contents);
		newNote.setObjectType(NoteLucened.getNoteObjectType());
		
		// HtmlConverter.HtmlImageData[] imageData = converter.getImageData();
		Iterator it = images.iterator();
		int i = 0;
		while(it.hasNext()){
			i++;
			String imageTitle = htmlPageTitle;
			if(i > 1)imageTitle = imageTitle+" ("+i+")";
			HtmlImageData theImage = (HtmlImageData)it.next();
			processImageInHTML(theImage, imageTitle, newNote, myHtmlResource);
		}
	}
	
	private Vector handleUserSelectionOfImages(Vector images) {
		HtmlImagesSelectionDialog dialog = new HtmlImagesSelectionDialog(null, images);
		if(dialog.open() != InputDialog.OK) return null;
		Vector selectedImages = new Vector();
		Iterator it = images.iterator();
		while(it.hasNext()){
			HtmlImageData item = (HtmlImageData)it.next();
			if(item.isSelected())selectedImages.add(item);
		}
		return selectedImages;
	}

	private void handleImageFile(IPath myPath){
		URL theURL = makeURLfromFileName(myPath);
		if(theURL == null)return;
		HtmlImageData data = new HtmlImageData("", theURL);
		makeImage(data, theURL.toString());

	}
	
	private void handleUnknownFileType(IPath myPath){
		MessageDialog.openError(null, "Unknown type", "This file is not recognised as an image file");
	}
	
	protected void handleFile(String name){
		IPath myPath = new Path(name);
		String ext = myPath.getFileExtension().toLowerCase();
		if(ext.equals("url")) handleUrlFile(myPath);
		else if(ext.equals("html")  || ext.equals("htm"))handleHTMLFile(myPath);
		else if(ext.equals("bmp") || ext.equals("jpg") || ext.equals("jpeg") ||
				ext.equals("gif") || ext.equals("png"))
			handleImageFile(myPath);
		else handleUnknownFileType(myPath);
	}
	
	protected void handleResource(Resource myResource){
		String theIdentifier = myResource.getIdentifier();
		if(theIdentifier.length() < 4 || !theIdentifier.startsWith("url:"))return;
		String theUrl = theIdentifier.substring(4,theIdentifier.length());
		handleUrl(theUrl, theUrl);
	}

	public Resource makeMyResource() {
		return new ImageResource(true);
	}

	public ObjectType getMyObjectType() {
		return ImageResPlugin.getImageObjectType();
	}

	public boolean canHandleObject(Object data) {
		if(!(data instanceof Resource))return false;
		Resource theResource = (Resource)data;
		return theResource.getIdentifier().startsWith("url:");
	}
	
	public IFigure getContentFigure(Resource resource){
		if(!(resource instanceof ImageResource))return null;
		ImageResource myResource = (ImageResource)resource;
		String thumbId = "thumb-"+myResource.getCacheNumber();
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
	
	public CacheElement[] getCacheElements(Resource resource){
		if(!(resource instanceof ImageResource))return null;
		ImageResource imgResource = (ImageResource)resource;
		String theExtension = imgResource.getExtension();
		if(theExtension == null){
			imgResource.getMyImageDescriptor();
			theExtension = imgResource.getExtension();
		}
		if(theExtension == null)return null;
		String cachedFileName = "i"+imgResource.getCacheNumber()+"."+theExtension;
		//File file = ImageResPlugin.getDefault().getCacheFile(null, imgResource.getCacheNumber(), theExtension);
		File file = ImageResPlugin.getDefault().getCache().getCacheFile(imgResource);
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
		
		String thumbFileName = "t"+imgResource.getCacheNumber()+".jpg";
		//File thumbFile = new File(ImageResPlugin.getDefault().getImageCachePath(),thumbFileName);
		File thumbFile = ImageResPlugin.getDefault().getCache().getThumbnailFile(imgResource);
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
	
	public void processArchiveEntries(IGetsArchiveEntries archive, Resource resource)
	throws PlinyImportException{
		if(!(resource instanceof ImageResource))return;
		ImageResource imgResource = (ImageResource)resource;
		String theExtension = imgResource.getExtension();
		if(theExtension == null){
			imgResource.getMyImageDescriptor();
			theExtension = imgResource.getExtension();
		}
		if(theExtension == null)return;
		
		int oldCacheNumber = imgResource.getCacheNumber();
		imgResource.setCacheNumber(imgResource.getALID());
		String cachedFileName = "i"+oldCacheNumber+"."+theExtension;
		InputStream in = archive.getArchiveEntry(cachedFileName);
		//String dir = ImageResPlugin.getDefault().getImageCachePath()+"/";
		if(in != null){
			ImageResPlugin.getDefault().getCache().createCacheItem(in, imgResource);
		}
		
		cachedFileName = "t"+oldCacheNumber+".jpg";
		in = archive.getArchiveEntry(cachedFileName);
		if(in != null){
			ImageResPlugin.getDefault().getCache().createThumbnail(imgResource, in);
		}
	}

}
