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
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.draw2d.ColorConstants;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.ImageFigure;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.IWorkbenchPage;

import uk.ac.kcl.cch.jb.pliny.PlinyPlugin;
import uk.ac.kcl.cch.jb.pliny.dnd.AbstractResourceExtensionProcessor;
import uk.ac.kcl.cch.jb.pliny.dnd.IGetsArchiveEntries;
import uk.ac.kcl.cch.jb.pliny.model.NoteLucened;
import uk.ac.kcl.cch.jb.pliny.model.ObjectType;
import uk.ac.kcl.cch.jb.pliny.model.Resource;
import uk.ac.kcl.cch.jb.pliny.utils.PlinyImportException;
import uk.ac.kcl.cch.jb.pliny.utils.StringToNoteHandler;


/**
 * the ResourceExtensionProcessor for Browser/Web Page Resources.
 * 
 * @see uk.ac.kcl.cch.jb.pliny.dnd.AbstractResourceExtensionProcessor
 * 
 * @author John Bradley
 *
 */
public class BrowserResourceExtensionProcessor extends
		AbstractResourceExtensionProcessor {

	private static ImageRegistry imageRegistry = PlinyPlugin.getDefault().getImageRegistry();

	
	public BrowserResourceExtensionProcessor() {
		super(BrowserEditor.BROWSER_ID);
	}
	
	public BrowserResourceExtensionProcessor(IWorkbenchPage thePage){
		super(thePage, BrowserEditor.BROWSER_ID);
	}

	public boolean canHandleObject(Object data) {
		// This is used when a Resource object is dropped on the Resource Explorer's
		// ObjectType folder -- clearly it cannot handle an existing resource.
		return false;
	}

	public IFigure getContentFigure(Resource resource) {
		if(resource == null || resource.getALID() <= 0)return null;
		VirtualBrowserResource myResource = (VirtualBrowserResource)resource;
		
		String thumbId = "thumb-"+resource.getSavedID();
		Image myThumbnail = imageRegistry.get(thumbId);
		if(myThumbnail == null){
			//ImageDescriptor myDescr = PlinyPlugin.getDefault().getThumbnailFromImageCache(resource);
			ImageDescriptor myDescr = myResource.getMyThumbnailDescriptor();
			if(myDescr == null)return null;
			imageRegistry.put(thumbId, myDescr);
		}
		ImageFigure rslt = new ImageFigure(imageRegistry.get(thumbId));
		rslt.setBackgroundColor(ColorConstants.lightBlue);
		return rslt;
	}

	public ObjectType getMyObjectType() {
		return ObjectType.getItem(2);
	}

	protected void handleFile(String name) {
		IPath myPath = new Path(name);
		String ext = myPath.getFileExtension().toLowerCase();
		if(ext.equals("url")) handleUrlFile(myPath);
		else processAsURL(makeURLfromFileName(myPath));
	}

	protected void handleResource(Resource resource) {
		// do nothing here -- resources are already in Pliny, and need not be handled
	}

	protected boolean handleUrl(URL theURL, String title) {
		Resource theResource = processAsURL(theURL);
		if(theResource != null)newResources.add(theResource);
		return processAsURL(theURL) != null;
	}

	public Resource makeMyResource() {
		//return new Resource(true);
		return new VirtualBrowserResource(null);
	}
	
	// real work is done by the following static methods:
	
	/* public static Resource handleStringAsNote(String input){
		input = input.trim();
		int newLine = input.indexOf('\n');
		String title = "New Note";
		// code added Feb 2010 to make short texts into a Pliny note
		// with the dropped text as title, and no content.  j.b.
		if(input.length() <= 60 && !input.contains("\n")){
			title = input;
			input = "";
		}
		else if((newLine >5) && (newLine < 60)){
			title = input.substring(0, newLine).trim();
			//input = input.substring(newLine+1,input.length()-1).trim();
			input = input.substring(newLine+1).trim();
		}
		NoteLucened theNote = null;
		theNote = new NoteLucened(true);
		theNote.setName(title);
		theNote.setContent(input);
		theNote.reIntroduceMe();
		theNote.setObjectType(NoteLucened.getNoteObjectType());
		return theNote;
	} */
	
	/**
	 * returns a Resource based on the specified String containing
	 * a URL.  If the URL reports
	 * mimetype of text/html, the returned Resource will be one for a web page
	 * (with ObjectType ID of 2), if the mimetype is text/plain, the returned
	 * Resource will be a LucenedNote.
	 * <p>A malformed URL, or a URL that does not point to a suitable web
	 * page results in this method returning <code>null</code>
	 * 
	 * @param input The URL as a text string to be processed
	 * @return the created Resource
	 */

	public static Resource processAsURL(String input) {
		URL testURL = null;
		try {
			testURL = new URL(input);
		} catch (MalformedURLException e) {
			return null;
		}
		if(testURL == null)return null;
		return processAsURL(testURL);
	}

	/**
 * returns a Resource based on the specified URL.  If the URL reports
 * mimetype of text/html, the returned Resource will be one for a web page
 * (with ObjectType ID of 2), if the mimetype is text/plain, the returned
 * Resource will be a LucenedNote.
 * <p>A URL that does not point to a suitable web
 * page results in this method returning <code>null</code>
 * 
 * @param testURL The URL to be processed
 * @return the created Resource
 */
	
	public static Resource processAsURL(URL testURL){
		try {
			URLConnection connection = testURL.openConnection();
			String mimeType = connection.getContentType();
			if(mimeType == null)mimeType = connection.guessContentTypeFromName(testURL.getFile());
			if(mimeType == null)return null;
			if(mimeType.startsWith("text/plain")){
				return StringToNoteHandler.getInstance().handleStringAsNote((String)testURL.getContent());
			}
			if(mimeType.startsWith("text/html"))return processAsHTML(testURL);
		} catch (IOException e) {
			return null;
		}
		return null;
	}

	private static Resource processAsHTML(URL testURL) throws IOException {
		String title = testURL.toExternalForm();
		String id = "url:"+testURL.toExternalForm();
		ObjectType htmlObjectType = ObjectType.getItem(2);
		Resource myHtmlResource = Resource.find(htmlObjectType, id);
		if(myHtmlResource != null){
			return myHtmlResource;
		}
		Object contents = testURL.getContent();
		if(!(contents instanceof InputStream))return null;
		String html = AbstractResourceExtensionProcessor.getFileContentsAsString((InputStream)contents);
		Pattern pattern = Pattern.compile("<title>([^<]*)", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE );
		Matcher matcher = pattern.matcher(html);
		if(matcher.find()){
			title = matcher.group(1);
		}
		Resource theResource = new Resource(true);
		theResource.setIdentifiers(id);
		theResource.setName(title);
		theResource.reIntroduceMe();
		theResource.setObjectType(htmlObjectType);
		return theResource;
	}

	public CacheElement[] getCacheElements(Resource resource){
		if(!(resource instanceof VirtualBrowserResource))return null;
		VirtualBrowserResource myResource = (VirtualBrowserResource)resource;
		//String cachedFileName = "w"+resource.getSavedID()+".jpg";
		//File file = new File(PlinyPlugin.getDefault().getImageCachePath(),cachedFileName);
		File file = PlinyPlugin.getDefault().getWebCache().getCacheFile(myResource);
		if(!file.exists())return null;
		FileInputStream in = null;
		try {
			in = new FileInputStream(file);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			return null;
		}
		
		CacheElement[] rslt = new CacheElement[1];
		rslt[0] = new CacheElement();
		rslt[0].fileName = "w"+resource.getALID()+".jpg";
		rslt[0].inputStream = in;
		return rslt;
	}
	
	public void processArchiveEntries(IGetsArchiveEntries archive, Resource resource)
	throws PlinyImportException{
		if(!(resource instanceof VirtualBrowserResource))return;
		VirtualBrowserResource myResource = (VirtualBrowserResource)resource;
		int oldKey = archive.getOldKey();
		String cachedFileName = "w"+oldKey+".jpg";
		InputStream in = archive.getArchiveEntry(cachedFileName);
		//if(in != null){
		//	String dir = PlinyPlugin.getDefault().getImageCachePath()+"/";
		//	String outFileName = "w"+resource.getALID()+".jpg";
		//	this.writeFile(in, dir+outFileName);
		//}
		if(in == null)return;
		PlinyPlugin.getDefault().getWebCache().createThumbnail(myResource, in);
	}

}
