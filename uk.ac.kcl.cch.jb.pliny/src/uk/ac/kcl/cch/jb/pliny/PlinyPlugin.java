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

package uk.ac.kcl.cch.jb.pliny;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.sql.Connection;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Set;
import java.util.Vector;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Plugin;
import org.eclipse.draw2d.ColorConstants;
import org.eclipse.jface.resource.ColorRegistry;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.ImageLoader;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

import uk.ac.kcl.cch.jb.pliny.browser.BrowserResourceExtensionProcessor;
import uk.ac.kcl.cch.jb.pliny.data.DataServerDialog;
import uk.ac.kcl.cch.jb.pliny.data.DataServerSetupManager;
import uk.ac.kcl.cch.jb.pliny.data.IDataServerWithCaching;
import uk.ac.kcl.cch.jb.pliny.data.IFileCacheManager;
import uk.ac.kcl.cch.jb.pliny.data.cloud.CloudServices;
import uk.ac.kcl.cch.jb.pliny.data.rdb.DBServices;
//import uk.ac.kcl.cch.jb.pliny.data.rdb.DbVersionManager;
import uk.ac.kcl.cch.jb.pliny.dnd.IResourceExtensionProcessor;
import uk.ac.kcl.cch.jb.pliny.editors.NoteEditorInput;
import uk.ac.kcl.cch.jb.pliny.model.ObjectType;
import uk.ac.kcl.cch.jb.pliny.model.Resource;
import uk.ac.kcl.cch.jb.pliny.views.utils.ResHolderContentProvider;
import uk.ac.kcl.cch.rdb2java.Rdb2javaPlugin;
import uk.ac.kcl.cch.rdb2java.dynData.BaseObject;
import uk.ac.kcl.cch.rdb2java.dynData.IDataServer;

/**
 * The main plugin class for Pliny to be used in the desktop.
 * The plugin as a whole is a classic Eclipse plugin, and contains many basic
 * Pliny elements including most of the model and UI code.
 * <p>This plugin class itself, as well as supporting the usual base
 * Eclipse functionality provides several Pliny-specific functions:
 * <ul>
 * <li>It sets up and manages the connection to the database
 * where Pliny data is stored (via object <code>DBServices</code>),
 * and provides a central place for getting a DB connection.
 * <li>It uses the ImageRegistry provided in a plugin to
 * support image caching.
 * <li>It keeps track of its open editors, and can provide as a service
 * a list of currently open "Pliny" note-editors to those who ask for it.
 * <li>It provides enhanced services for a colour registry.
 * <li>It provides support for the <code>resourceExtensionProcessor</code>
 * plugin extension point -- looking to see what other plugins have a 
 * contribution to this, and maintaining a list of them for other Pliny elements 
 * to use.  A resourceExtensionProcessor object is associated with a Pliny ObjectType.
 * <li>It maintains the Resource Referent pointer and counter that is used
 * to count notes created that link to the Referent.
 * </ul>
 * 
 * @author John Bradley
 */
public class PlinyPlugin extends AbstractUIPlugin {

	//The shared instance.
	private static PlinyPlugin plugin;
	private IDataServerWithCaching dataServer = null;
	private static Hashtable resourceExtensions = null;
	private static IFileCacheManager manager = null; // for web browser thumbnails    .. jb

	
	private static Resource referent = null;
	private static int referenceNumber = 0;
	private static BaseObject currentObject = null;
	
	public static final String PLUGIN_ID = "uk.ac.kcl.cch.jb.pliny";
	
	/**
	 * The constructor.
	 */
	public PlinyPlugin() {
		plugin = this;
	}

	/**
	 * This method is called upon plug-in activation
	 */
	public void start(BundleContext context) throws Exception {
		super.start(context);
		
		//IDataServerWithCaching dbServices = new DBServices(getStateLocationUrl("pliny"), this);
		//IDataServerWithCaching dbServices = new CloudServices(null, "bradley@test.org", "passwd");
		//IDataServerWithCaching dbServices = new CloudServices("http://pliny-cloud.appspot.com", 
		//		"john.bradley501@googlemail.com", "xxxxxxxx");
		//dataServer = dbServices;
		//Rdb2javaPlugin.setDataServer(dataServer);
		//dbServices.start();
		
		DataServerSetupManager manager = new DataServerSetupManager(getStateLocationUrl("pliny"), this);
		dataServer = manager.establishDataServer();
		if(dataServer == null){
			throw new RuntimeException("A data server could not be established for Pliny.");
		}
		
		//(new DbVersionManager(this)).run();
		//setupResourceExtensions();
	}

	/**
	 * This method is called when the plug-in is stopped
	 */
	public void stop(BundleContext context) throws Exception {
		super.stop(context);
		referentWatcher.dispose();
		if(dataServer != null)dataServer.dispose();
		//DBServices.closeConnection();
		plugin = null;
	}

	/**
	 * Returns the shared instance.
	 */
	public static PlinyPlugin getDefault() {
		return plugin;
	}
	
	/**
	 * Returns the shared instance of DBServices -- the access mechanism
	 * for Pliny's backing database.
	 */
	//public static DBServices getDBServicesInstance() {
	//	if(plugin == null)return null;
	//	return plugin.dbServices;
	//}
	
	public static IDataServerWithCaching getPlinyDataServerInstance() {
		if(plugin == null)return null;
		return plugin.dataServer;
	}
	
	public IFileCacheManager createFileCacheManager(Plugin parent, String cacheName, String type){
		return dataServer.createCacheManager(parent, cacheName, type);
	}

	/**
	 * Returns an image descriptor for the image file at the given
	 * plug-in relative path.
	 *
	 * @param path the path
	 * @return the image descriptor
	 */
	public static ImageDescriptor getImageDescriptor(String path) {
		return AbstractUIPlugin.imageDescriptorFromPlugin("uk.ac.kcl.cch.jb.pliny", path);
	}
	
	/**
	 * Returns an image from the image registry based on a given string
	 * as a key.
	 * 
	 * @param id the key by which the image is identified in the registry
	 * @return the image
	 */
	
	public Image getImage(String id){
		Image rslt = getImageRegistry().get("local:"+id);
		if(rslt != null)return rslt;
		//ImageDescription myID = NoteManUiPlugin.getDefault().getDescriptor().getInstallURL().
		IPath mypath = new Path(id);
		try {
			rslt = new Image(null, openStream(mypath));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			rslt = PlatformUI.getWorkbench().getSharedImages().getImage(ISharedImages.IMG_OBJ_ELEMENT);
		}
		getImageRegistry().put("local:"+id, rslt);
		return rslt;
	}
	
	/**
	 * returns a path to the plugin's state area that is extended by
	 * the given name.  This is a convenience method.
	 * 
	 * @param objectName name to be added to the state area path
	 * @return the extended path
	 */
	
	public String getStateLocationUrl(String objectName){
		   //Bundle myBundle = this.getBundle();
		   //URL thisURL = myBundle.getLocation();
		   IPath myStatePath = this.getStateLocation();
		   String myObjectsURL = myStatePath.makeAbsolute().toString()+"/"+objectName;
		   return myObjectsURL;
	}
	
	/*
	 * returns a DB connection to the Pliny backing database.
	 * 
	 * @return a DB connection
	 
	public Connection getConnection(){
		if(conn != null)return conn;
		DBServices.setDBName(getStateLocationUrl("pliny"));
		conn = DBServices.getConnection();
		return conn;
	} */
	
	/*
	 * returns the given connection (got with <code>getConnection()</code>
	 * back.  This allows for connection pooling, although this is not currently
	 * used in Pliny -- there is only one connection made available.
	 * 
	 * @param c the connection to return
	 
	public void returnConnection(Connection c){
		DBServices.returnConnection(c);
	} */
	
	/*
	 * Following code is commented out because it is not needed here
	 * any longer -- now in plugin uk.ac.kcl.cch.jb.pliny.imageRes
	 ** 
	 * returns a File that represents the directory where the Pliny image
	 * cache is located.
	 * 
	 * @return a File as a directory
	
	public File getImageCachePath(){
		   File cachePath = new File(this.getStateLocation().toString(),"imageCache");
		   if(!cachePath.exists()){
			   cachePath.mkdirs();
		   }
		   return cachePath;
	}
	
	private static int bufLength = 1024*1024;
	
	private File getCacheFileName(URL url, int imageNo){
		IPath path = new Path(url.getFile());
		return new File(getImageCachePath(),"i"+imageNo+"."+path.getFileExtension());
	}
	 */
	
	/*
	 * fetches and stores the image associated with the URL in the image cache managed
	 * by this plugin.  The key to the image will be the given image number.
	 * 
	 * @param url a URL to the image to be cached.
	 * @param imageNo the key to be used to store the image
	 * @return returns false if the process failed -- IO error or file not found, etc.

	public boolean cacheImage(URL url, int imageNo){
		File theFile = getCacheFileName(url, imageNo);
		BufferedOutputStream writer = null;
		BufferedInputStream input = null;
		byte[] buffer = new byte[bufLength];
		int amountRead = 0;
		try {
			writer = new BufferedOutputStream(new FileOutputStream(theFile));
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
		
		try {
			int len;
			input = new BufferedInputStream(url.openStream());
			while((len = input.read(buffer,0,bufLength)) != -1){
				    amountRead += len;
					writer.write(buffer,0,len);
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		} finally {
			try {
				//input.close();
				writer.close();
				if((amountRead == 0) && theFile.exists()){
					theFile.delete();
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return false;
			}
		}
		return amountRead != 0;
	}
	 */
	
	/*
	 * returns an ImageDescriptor that points to an image in the cache.
	 * The URL is used to provide a suitable file extension (for image type info).
	 * 
	 * @param url URL to the image
	 * @param imageNo key number in cache for the image.
	 * @return ImageDescriptor for the image

	public ImageDescriptor getFromImageCache(URL url, int imageNo){
		File file = getCacheFileName(url, imageNo);
		if(!file.exists())return null;
		return ImageDescriptor.createFromFile(null, file.getAbsolutePath());
	}
	 */
	
	/* the following code is used in situations like that
	 * which arose in DeleteLinkableObject -- when one needs to
	 * establish if there is an open editor on an object before
	 * deciding whether to delete it or not.  This one returns the
	 * set of notes that have currently open editors
	 */
	
	/**
	 * returns the set of Pliny open note editors. Notes with open editors
	 * are identified as a set of Integers containing DB keys to the note.
	 * 
	 * @return a Set of Integers -- keys to note objects in the DB.
	 */
	public Set getOpenNotesIds(){
		HashSet rslt = new HashSet();
		IEditorReference[] editors = getWorkbench().getActiveWorkbenchWindow().getActivePage().getEditorReferences();
        for(int i = 0; i < editors.length; i++){
        	try {
				IEditorInput eInput = editors[i].getEditorInput();
				if(eInput instanceof NoteEditorInput){
					NoteEditorInput nei = (NoteEditorInput)eInput;
					rslt.add(new Integer(nei.getMyNote().getALID()));
				}
			} catch (PartInitException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
        }
		return rslt;
	}
	
	static private ColorRegistry myColourRegistry = null;
	
	/**
	 * uses the parameter as a key to the colour registry.  If the entry
	 * is there, returns it as a Color object.  If it is not there, it
	 * stores the given RDB object in the registry so that it will be there
	 * next time.
	 * <p>
	 * The returned Color object is managed by the registry and need not be
	 * disposed by the caller.
	 * 
	 * @param name name as key to the colour
	 * @param rgb colour to be used if not in registry
	 * 
	 * @return Color object that represents the colour.
	 */
	static public Color getColour(String name, RGB rgb){
		if(myColourRegistry == null)myColourRegistry = new ColorRegistry(Display.getCurrent());
		Color rslt = myColourRegistry.get(name);
		if(rslt != null)return rslt;
		myColourRegistry.put(name, rgb);
		return myColourRegistry.get(name);
	}
	
	/**
	 * updates the colour associated with the given name to be the new colour.
	 * 
	 * @param name name of registry entry to be updated.
	 * @param rgb RGB of new colour to be stored
	 */
	static public void updateColour(String name, RGB rgb){
		if(myColourRegistry == null)myColourRegistry = new ColorRegistry(Display.getCurrent());
		myColourRegistry.put(name, rgb);
	}
	
	static public Color getBackgroundGray(){
		String colourName = "backgroundGray";
		RGB backgroundGray = new RGB(240,240,240);
		return getColour(colourName, backgroundGray);
	}
	
	private static BrowserResourceExtensionProcessor browserProcessor = null;
	
	/**
	 * manages a single instance of the BrowserResourceExtensionProcessor.
	 * 
	 * @return the single instance of the BrowserResoruceExtensionProcessor.
	 */
	public static BrowserResourceExtensionProcessor getTheBrowserResourceExtensionProcessor(){
		if(browserProcessor == null)
			browserProcessor = new BrowserResourceExtensionProcessor();
		return browserProcessor;
	}

	private static final String targetProcessorId =
		"uk.ac.kcl.cch.jb.pliny.resourceExtensionProcessor";
	
	private static void setupResourceExtensions(){
		//System.out.println("setupResourceExtensions starts");
		//org.eclipse.osgi.framework.debug.FrameworkDebugOptions.getDefault().
		//    setOption("debug.loader", "true");
		resourceExtensions = new Hashtable();
		IExtensionRegistry registry= Platform.getExtensionRegistry();
		IExtensionPoint extensionPoint= registry.getExtensionPoint(targetProcessorId);
		IExtension[] extensions= extensionPoint.getExtensions();
		for (int i= 0; i < extensions.length; i++) {
			IConfigurationElement[] elements= extensions[i].getConfigurationElements();
			for (int j= 0; j < elements.length; j++) {
				///System.out.println(elements[j].getAttribute("class"));
				try {
					Object item= elements[j].createExecutableExtension("class");
					if (item instanceof IResourceExtensionProcessor){
						IResourceExtensionProcessor proc = (IResourceExtensionProcessor)item;
						ObjectType myOt = proc.getMyObjectType();
						resourceExtensions.put(new Integer(myOt.getALID()), proc);
					}
				} catch (CoreException e) {
					e.printStackTrace();
					System.out.println();
				}
			}
		}
		resourceExtensions.put(
				new Integer(2), 
				getTheBrowserResourceExtensionProcessor());
		//System.out.println("setupResourceExtensions ends");
	}
	
	/**
	 * returns a ResourceExtensionProcessor associated with the given DB key for
	 * an {@link uk.ac.kcl.cch.jb.pliny.model.ObjectType}.
	 * The value might be null if there is no associated processor.
	 * 
	 * @param otKey DB key of the ObjectType
	 * @return IResourceExtensionProcessor the matching processor
	 */
	
	public static IResourceExtensionProcessor getResourceExtensionProcessor(int otKey){
		if(resourceExtensions == null)setupResourceExtensions();
		return (IResourceExtensionProcessor)resourceExtensions.get(new Integer(otKey));
	}
	
	/**
	 * returns a ResourceExtensionProcessor associated with the given 
	 * {@link uk.ac.kcl.cch.jb.pliny.model.ObjectType}.
	 * The value might be null if there is no associated processor.
	 * 
	 * @param type
	 * @return IResourceExtensionProcessor the matching processor
	 */
	public static IResourceExtensionProcessor getResourceExtensionProcessor(ObjectType type){
		if(type == null)return null;
		return getResourceExtensionProcessor(type.getALID());
	}
	
	/**
	 * returns a ResourceExtensionProcessor associated with the given Resource
	 * through that Resource's ObjectType.
	 * The value might be null if there is no associated processor.
	 * 
	 * @param resource the Resource in question
	 * @return IResourceExtensionProcessor the matching processor
	 */
	public static IResourceExtensionProcessor getResourceExtensionProcessor(Resource resource){
		if(resource == null || resource.getObjectType() == null)return null;
		return getResourceExtensionProcessor(resource.getObjectType().getALID());
	}

	private static class ReferentWatcher implements PropertyChangeListener {
		
		private Resource resource = null;
		
		public void setupReferent(Resource resource){
			if(this.resource == null && resource != null)
				Rdb2javaPlugin.getDataServer().addPropertyChangeListener(this);
			else if(this.resource != null && resource == null)
				Rdb2javaPlugin.getDataServer().removePropertyChangeListener(this);
			this.resource = resource;
		}
		
		public void dispose(){
			if(resource != null)
				Rdb2javaPlugin.getDataServer().removePropertyChangeListener(this);
		}

		public void propertyChange(PropertyChangeEvent arg0) {
			String pString = arg0.getPropertyName();
			if(pString.equals("Delete-Resource") && arg0.getOldValue()==resource){
				referent = null;
			} else if(pString.equals("Create-Resource") && arg0.getNewValue()==resource){
				referent = resource;
			}
		}
		
	}
	
	private static ReferentWatcher referentWatcher = new ReferentWatcher();
	
	/**
	 * stores the given Resource to be the new Referent.  Pass in null
	 * to clear the current referent.
	 * 
	 * @param resource the new object to be the referent
	 */
	public static void setReferent(Resource resource){
		referentWatcher.setupReferent(resource);
		referent = resource;
		if(resource == null){
			referenceNumber = 0;
			return;
		}
		int numberDisplayedNotes = resource.getMyDisplayedItems().getCount();
		referenceNumber = numberDisplayedNotes;
	}
	
	/**
	 * gets the current referent.  This will return null if there is
	 * no current referent.
	 * @return Resource
	 */
	public static Resource getReferent(){
		return referent;
	}
	
	/**
	 * increments and returns the current reference number associated
	 * with the referent.
	 * 
	 * @return int representing the current reference number
	 */
	public static int incReferenceNumber(){
		return ++referenceNumber;
	}
	
	/**
	 * returns the current reference number associated
	 * with the referent.
	 * 
	 * @return int representing the current reference number
	 */
	public static int getReferenceNumber(){
		return referenceNumber;
	}
	
	public static void setCurrentObject(BaseObject obj){
		currentObject = obj;
	}
	
	public static BaseObject getCurrentObject(){
		return currentObject;
	}

	private static final int bufLength2 = 64*1024;
	private static byte[] buffer = null;

	/**
	 * a utility function to copy material in input stream to the output stream.
	 * Streams are closed after copy.
	 * 
	 * @param in Input Stream
	 * @param out Output Stream
	 * @throws IOException
	 */
	public static final void copyInputStream(InputStream in, OutputStream out) 
	   throws IOException { 
	   if(buffer == null) buffer = new byte[bufLength2]; 
	     int len; 
	  
	     while((len = in.read(buffer)) >= 0) 
	       out.write(buffer, 0, len); 
	  
	     if(in instanceof ZipInputStream)
	    	 ((ZipInputStream)in).closeEntry();
	     else in.close();
	     if(out instanceof ZipOutputStream)
	    	 ((ZipOutputStream)out).closeEntry();
	     else out.close(); 
	   }
	/**
	 * removes the reference to the character buffer created by
	 * <code>copyInputStream</code>, so that it can be garbage collected.
	 *
	 */
	
	public static final void clearStreamBuffer(){
		buffer = null;
	}

	static final int thumbWidth = 150;
	static final int thumbHeight =150;

	public IFileCacheManager getWebCache(){
		if(manager == null){
			manager = PlinyPlugin.getPlinyDataServerInstance().createCacheManager(this, "webIconCache", "w");
			manager.setThumbFileNamePrefix("w"); // to preserve past anomoly with web page thumbnail file names   jb
		}
		return manager;
	}
	
	
	/**
	 * returns the full path to the directory where web page icon images are to be
	 * cached.
	 * 
	 * @return File containing the Cache path
	 */
	
/*	public File getImageCachePath(){
		   File cachePath = new File(this.getStateLocation().toString(),"webIconCache");
		   if(!cachePath.exists()){
			   cachePath.mkdirs();
		   }
		   return cachePath;
	}
*/
	
/*	public ImageDescriptor createThumbnail(Browser browser, Resource resource){
		if(resource.isNotPersisting())return null;
        
        //Point tableSize = browser.getSize();
		Rectangle imageArea = browser.getClientArea(); // is this right? j.b.
        GC gc = new GC(browser);
        final Image image = new Image(browser.getDisplay(), imageArea.width, imageArea.height);
        gc.copyArea(image, 0, 0);
        gc.dispose();
        
        ImageData data = image.getImageData();
//      save image 
        //ImageLoader imageLoader = new ImageLoader(); 
        //imageLoader.data = new ImageData[] { image.getImageData() }; 
        //imageLoader.save("c:/test.jpg", SWT.IMAGE_JPEG); // fails 

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
		ImageLoader loader = new ImageLoader();
		loader.data = new ImageData[]{thumbData};
		
		String thumbName = "w"+resource.getSavedID()+".jpg";
		File file = new File(getImageCachePath(),thumbName);
		String fullThumbName = file.getAbsolutePath();
		loader.save(fullThumbName, SWT.IMAGE_JPEG);
		//loader.save(fname, SWT.IMAGE_PNG);
		
		 * One would think that having created and saved the image, and having the
		 * imageData, that the createFromImageData could be returned.  Indeed, this
		 * >usually< works, but it doesn't seem to when the image goes through the
		 * depth==1 code above.  Getting the image by getting the ImageDescriptor
		 * from the file (via a URL) seems to work in both cases!   ... jb
		 
		URL theUrl = null;
		try {
			theUrl = new URL("file:///"+fullThumbName);
		} catch (MalformedURLException e) {
			return ImageDescriptor.createFromImageData(thumbData);
		}
		return ImageDescriptor.createFromURL(theUrl);
		
	}
*/
/*	public ImageDescriptor getThumbnailFromImageCache(Resource res){
		String thumbName = "w"+res.getALID()+".jpg";
		//String thumbName = "t"+imageNo+".png"; // problem with creation of 1-bit depth images not helped by this.
		File file = new File(getImageCachePath(),thumbName);
		if(file.exists()){
			return ImageDescriptor.createFromFile(null, file.getAbsolutePath());
		}
        return null;
	}
*/
    // The Vector resHolderItems is used by the ResHolderView.  It is held here so
	// that any materials it might be holding survive the ResHolderView view being
	// shutdown and then reopened.
	
	Vector resHolderItems = null;
	public Vector getResHolderItems(){
		if(resHolderItems == null)resHolderItems = new Vector();
		return resHolderItems;
	}
}
