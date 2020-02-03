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

package uk.ac.kcl.cch.jb.pliny.imageRes;

import org.eclipse.core.runtime.IPath;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

import uk.ac.kcl.cch.jb.pliny.PlinyPlugin;
import uk.ac.kcl.cch.jb.pliny.data.IFileCacheManager;
import uk.ac.kcl.cch.jb.pliny.model.ObjectType;

/**
 * The main plugin class for Pliny's Image Editor to be used in the desktop.
 * The plugin as a whole is a classic Eclipse plugin, and contains many basic
 * Pliny elements including most of the model and UI code.
 * <p>This plugin class itself, as well as supporting the usual base
 * Eclipse plugin functionality provides several Pliny-specific functions:
 * <ul>
 * <li>It sets up and manages the image cache.
 * <li>It returns the standard 
 * {@link uk.ac.kcl.cch.jb.pliny.model.ObjectType ObjectType} for
 * Image Resources.
 * </ul>
 * <p>Note that the <i>meaning</i> of the image cache has substantially changed between this
 * version and previous versions.  Starting with this version, there are specific objects
 * (@link uk.ac.kcl.cch.jb.pliny.data.IFileCacheManager IFileCacheManager)
 * that handle file-oriented caches.  This is to allow different cache mechanisms to be used
 * in different situations, and is one of the changes that have been made to support data storage
 * for Pliny to be handled in the cloud as well as locally.
 * 
 * @author John Bradley
 */
public class ImageResPlugin extends AbstractUIPlugin {

	public static String EDITOR_ID = "uk.ac.kcl.cch.jb.pliny.imageRes.editor";
	public static String PLUGIN_ID = "uk.ac.kcl.cch.jb.pliny.imageRes";

	//The shared instance.
	private static ImageResPlugin plugin;
	private static IFileCacheManager manager = null;
	
	/**
	 * The constructor.
	 */
	public ImageResPlugin() {
		plugin = this;
	}

	/**
	 * This method is called upon plug-in activation
	 */
	public void start(BundleContext context) throws Exception {
		super.start(context);
	}

	/**
	 * This method is called when the plug-in is stopped
	 */
	public void stop(BundleContext context) throws Exception {
		super.stop(context);
		plugin = null;
	}

	/**
	 * Returns the shared instance.
	 */
	public static ImageResPlugin getDefault() {
		return plugin;
	}

	/**
	 * Returns an image descriptor for the image file at the given
	 * plug-in relative path.
	 *
	 * @param path the path
	 * @return the image descriptor
	 */
	public static ImageDescriptor getImageDescriptor(String path) {
		return AbstractUIPlugin.imageDescriptorFromPlugin("uk.ac.kcl.cch.jb.pliny.imageRes", path);
	}
	
	
	//public String getStateLocationUrl(String objectName){
	//	   IPath myStatePath = this.getStateLocation();
	//	   String myObjectsURL = myStatePath.makeAbsolute().toString()+"/"+objectName;
	//	   return myObjectsURL;

	//}
	
	public IFileCacheManager getCache(){
		if(manager == null)
			manager = PlinyPlugin.getPlinyDataServerInstance().createCacheManager(this, "imageCache", "i");
		return manager;
	}
	
	private static ObjectType myImageObjectType = null;
	
	/**
	 * returns the ObjectType for image resources that the editor in this
	 * plugin can handle.
	 * 
	 * @return the ObjectType for image resources.
	 */
	public static ObjectType getImageObjectType(){
		if(myImageObjectType != null)return myImageObjectType;
		myImageObjectType = ObjectType.findFromIds(PLUGIN_ID, EDITOR_ID);
		if(myImageObjectType == null){
			myImageObjectType = new ObjectType(true);
			myImageObjectType.setEditorId(ImageResPlugin.EDITOR_ID);
			myImageObjectType.setName("Image");
			//ImageDescriptor icon = ImageResPlugin.getImageDescriptor("icons/imageIcon.gif");
			myImageObjectType.setIconId("editor:"+ImageResPlugin.EDITOR_ID);
			myImageObjectType.reIntroduceMe();
			myImageObjectType.setPlugin(uk.ac.kcl.cch.jb.pliny.model.Plugin.
					findFromId(ImageResPlugin.PLUGIN_ID));
		}
		return myImageObjectType;
	}

}
