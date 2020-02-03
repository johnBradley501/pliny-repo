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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Iterator;
import java.util.Vector;

import org.eclipse.core.runtime.IPath;
import org.eclipse.draw2d.IFigure;
import org.eclipse.swt.dnd.DropTargetEvent;
import org.eclipse.swt.dnd.FileTransfer;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.part.ViewPart;

import uk.ac.kcl.cch.jb.pliny.PlinyPlugin;
import uk.ac.kcl.cch.jb.pliny.editors.ResourceEditorInput;
import uk.ac.kcl.cch.jb.pliny.model.NoteLucened;
import uk.ac.kcl.cch.jb.pliny.model.ObjectType;
import uk.ac.kcl.cch.jb.pliny.model.Resource;
import uk.ac.kcl.cch.jb.pliny.utils.PlinyImportException;

/**
 * an abstract class which implements {@link IResourceExtensionProcessor} to 
 * provide a base set of functionality which other can then be extended to support
 * interactions between Pliny plugins and other plugins.  The main features here
 * are code to support drag-and-drop of objects outside of Eclipse into
 * Pliny.
 * 
 * @author John Bradley
 *
 */
public abstract class AbstractResourceExtensionProcessor implements
		IResourceExtensionProcessor {

	protected IWorkbenchPage myPage = null;
	protected Vector newResources = null;
	protected String editorId = null;

	public AbstractResourceExtensionProcessor(String editorId) {
		newResources = new Vector();
		this.editorId = editorId;
	}
	
	public AbstractResourceExtensionProcessor(IWorkbenchPage myPage, String editorId){
		this.myPage = myPage;
		newResources = new Vector();
		this.editorId = editorId;
	}
	
	/**
	 * see {@link uk.ac.kcl.cch.jb.pliny.dnd.IResourceExtensionProcessor#setViewPart}.
	 * The default implementation takes the given ViewPart, fetches its
	 * <code>IWorkbenchPage</code>, and stores that for object use (to allow editors to be
	 * opened on newly created Resources.
	 * <p>
	 * This method may be extended, but this versioni should also be called via
	 * <code>super.setViewPart(...)</code> in
	 * the code extension.
	 */

	public void setViewPart(ViewPart myViewPart) {
		myPage = myViewPart.getSite().getPage();
	}
	
	/**
	 * returns an 
	 * {@link IResourceExtensionProcessorSource}.  In this default
	 * implementation, this is the same as the associated 
	 * {@link uk.ac.kcl.cch.jb.pliny.model.ObjectType ObjectType}.
	 */

	public IResourceExtensionProcessorSource getSource(){
		return (IResourceExtensionProcessorSource)getMyObjectType();
	}

	/**
	 * see {@link uk.ac.kcl.cch.jb.pliny.dnd.IResourceExtensionProcessor#makeMyResource}
	 * for details.
	 * 
	 * @return Resource a new Resource of suitable type not yet set up in the
	 * backing store.
	 */
	public abstract Resource makeMyResource();


	/**
	 * see {@link uk.ac.kcl.cch.jb.pliny.dnd.IResourceExtensionProcessor#getMyObjectType}
	 * for details.
	 * 
	 * @return ObjectType the type of resource created by this processor.
	 */
	public abstract ObjectType getMyObjectType();

	/**
	 * see {@link uk.ac.kcl.cch.jb.pliny.dnd.IResourceExtensionProcessor#canHandleObject}
	 * for details.
	 * 
	 * @return boolean returns <code>true</code> if this processor can handle this object.
	 */
    public abstract boolean canHandleObject(Object data);

	/**
	 * see {@link uk.ac.kcl.cch.jb.pliny.dnd.IResourceExtensionProcessor#getContentFigure}
	 * for details.
	 * 
	 * @return IFigure the figure to display.
	 */
	public abstract IFigure getContentFigure(Resource resource);


	/**
	 * this method checks the kind of object being dropped in the Pliny environment
	 * and depending upon the kind, invokes various other methods to handle
	 * different kinds of data.
	 * See {@link uk.ac.kcl.cch.jb.pliny.dnd.IResourceExtensionProcessor#processDrop}
	 * for details.
	 * <p>
	 * drop data handled include:
	 * <ul>
	 * <li>Resources dragged from other parts of Pliny -- process it by
	 * the code <code>handleResource()</code>.
	 * <li>Strings of text dragged from outside Pliny -- process it by
	 * the code <code>handleString()</code>.  The default implementation found
	 * here will take the string and see if it can be interpreted as a URL.  If it can,
	 * it will invoke abstract function <code>handleUrl()</code>.
	 * <li>Files dragged from outside Pliny -- process them (one at a time)
	 * by the code in <code>handleFile()</code>
	 * </ul>
	 * 
	 * @return IFigure the figure to display.
	 */
	public boolean processDrop(DropTargetEvent event) {
		newResources = new Vector();
		
		boolean handled = false;
		
		if(ClipboardHandler.TRANSFER.isSupportedType(event.currentDataType)){
			if(canHandleObject(event.data)){
				handleResource((Resource)event.data);
				handled = true;
			}
		}

		if(handled); // do nothing 
		else if(TextTransfer.getInstance().isSupportedType(event.currentDataType)){
			handleString((String)event.data);
		}

		else if(FileTransfer.getInstance().isSupportedType(event.currentDataType)){
			handleFiles((String [])event.data);
		}
		else return false;
		openResources();
		return true;
	}

	/**
	 * implementment this method to process a Resource that has been dragged
	 * into this process from another Pliny application.
	 * 
	 * @param resource Resource to be introduced.
	 */
	protected abstract void handleResource(Resource resource);
	
	private void handleFiles(String[] input){
		for(int i = 0; i < input.length; i++)handleFile(input[i]);
	}

	/**
	 * implement this method to handle a file that has been dropped on
	 * your processor.
	 * 
	 * @param string String name of the File
	 */
	
	protected abstract void handleFile(String string);

	public void openResources(){
		if((myPage != null)&&(newResources.size() > 0)){
			Iterator it = newResources.iterator();
			while(it.hasNext()){
				Resource newResource = (Resource)it.next();
				try {
					myPage.openEditor(new ResourceEditorInput(newResource),editorId);
				} catch (PartInitException e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	protected void handleUrlFile(IPath myPath){
		File myFile = myPath.toFile();
		FileInputStream myStream = null;
		try {
			myStream = new FileInputStream(myFile);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return;
		}
		String contents = getFileContentsAsString(myStream);
		contents = contents.replaceAll("\r","").trim();
		
		
		/* file format from IE
 
		[DEFAULT]
		 BASEURL=http://137.73.122.75:8084/diamm/loginform.jsp
		 [InternetShortcut]
		 URL=http://137.73.122.75:8084/diamm/loginform.jsp
		 Modified=C0BC896C2BAEC50110
		 */
		String[] inputLines = contents.split("\n");
		for(int i = 0; i < inputLines.length; i++){
			if((inputLines[i].matches("\\[InternetShortcut\\]")) && ((i+1)<inputLines.length)){
				String url = inputLines[i+1];
				handleUrl(url.replaceAll("URL=",""),"");
			}
		}
	}
	
	protected void handleString(String input){
		input = input.replaceAll("\r","").trim();
		if(input.startsWith("http:") || input.startsWith("ftp:")  || input.startsWith("file:")){
			String[]inputParts = (input+"\n \n").split("\n");
			if(handleUrl(inputParts[0], inputParts[1]))return;
		}
		makeNote(input, "New Note");
	}
	
	protected boolean handleUrl(String input, String title){
		URL theURL = null;
		try {
			theURL = new URL(input);
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
		return handleUrl(theURL, title);
	}

	/**
	 * implement this abstract method to provide code to process a dropped
	 * object that was a URL.
	 * 
	 * @param theURL URL the URL the object represents
	 * @param title String a suggested title for the Resource you should create.
	 * 
	 * @return boolean return <code>true</code> if the URL was successfully
	 * handled.
	 */
	protected abstract boolean handleUrl(URL theURL, String title);
	
	protected void makeNote(String input, String title){
		int newLine = input.indexOf('\n');
		if((newLine >5) && (newLine < 60)){
			title = input.substring(0, newLine);
			input = input.substring(newLine+1,input.length()-1).trim();
		}
		NoteLucened newNote = new NoteLucened();
		newNote.setObjectType(NoteLucened.getNoteObjectType());
		newNote.setName(title);
		newNote.setContent(input);
	}
	
	public static String getFileContentsAsString(InputStream stream){
		if(stream == null)return "";
		StringBuffer buf = new StringBuffer();
		int c;
		try {
			while((c = stream.read()) != -1){
				buf.append((char)c);
			}
			stream.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			try {
				stream.close();
			} catch (IOException e1) {
			}
			return new String(buf);
		}
		return new String(buf);
	}
	
	protected URL makeURLfromFileName(IPath myPath){
		URL theURL = null;
		try {
			theURL = new URL("file:///"+myPath.toString());
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
		return theURL;
	}
	/**
	 * override this method to provide support for adding cache data from your
	 * plugin into a pliny archive file.
	 * See {@link uk.ac.kcl.cch.jb.pliny.dnd.IResourceExtensionProcessor#getCacheElements}.
	 * 
	 * @return an array of CacheElements that the the archiver what data is to
	 * be put in the archive file, and give a handle to a Stream that can provide
	 * it.
	 */
	
	public CacheElement[] getCacheElements(Resource r){
		return null;
	}
	
	/**
	 * override this method to provide support for creating cache data from
	 * Pliny archive files.
	 * See {@link  uk.ac.kcl.cch.jb.pliny.dnd.IResourceExtensionProcessor#processArchiveEntries}.
	 * 
	 * @param archive IGetsArchiveEntries archive process that can provide you
	 * with access to input streams from the archive containing the data you want
	 * to put in the cache.
	 * @param r Resource the cache data should belong to.
	 * @throws PlinyImportException
	 */
	
	public void processArchiveEntries(IGetsArchiveEntries archive, Resource r)
	throws PlinyImportException{
		return;
	}

	
	protected void writeFile(InputStream in, String outputFileName) throws PlinyImportException{
		FileOutputStream out = null;
		try {
			out = new FileOutputStream(outputFileName);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			throw new PlinyImportException("Creating File: "+outputFileName);
		}
		try {
			PlinyPlugin.copyInputStream(in, out);
		} catch (IOException e) {
			e.printStackTrace();
			throw new PlinyImportException("Copying Data: "+outputFileName);
		}
	}

}
