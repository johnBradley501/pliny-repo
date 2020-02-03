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

package uk.ac.kcl.cch.jb.pliny.pdfAnnot;

import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;

import javax.imageio.ImageIO;

import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.jpedal.PdfDecoder;
import org.jpedal.images.SamplingFactory;
import org.osgi.framework.BundleContext;

import uk.ac.kcl.cch.jb.pliny.PlinyPlugin;
import uk.ac.kcl.cch.jb.pliny.data.IFileCacheManager;
import uk.ac.kcl.cch.jb.pliny.model.ObjectType;

/**
 * The main plugin class for Pliny's PDF Editor to be used in the desktop.
 * The plugin as a whole is a classic Eclipse plugin, and contains many basic
 * Pliny elements including most of the model and UI code.
 * <p>
 * Much of the code found in the the Editor itself, and some in this
 * particular class come from models provided with JPedal -- the PDF
 * display engine used here.
 * <p>This plugin class itself, as well as supporting the usual base
 * Eclipse functionality provides several Pliny-specific functions:
 * <ul>
 * <li>It sets up and manages the image cache -- a place to store both the
 * PDF file itself, and a thumbnail image (of the first page).
 * <li>It returns the standard 
 * {@link uk.ac.kcl.cch.jb.pliny.model.ObjectType ObjectType} for
 * PDF Resources.
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
public class PdfAnnotPlugin extends AbstractUIPlugin {

	public static String EDITOR_ID = "uk.ac.kcl.cch.jb.pliny.pdfAnnot.PDFEditor";
	public static String PLUGIN_ID = "uk.ac.kcl.cch.jb.pliny.pdfAnnot";

	
	//The shared instance.
	private static PdfAnnotPlugin plugin;
	private static IFileCacheManager manager = null;
	
	public static boolean isBroken=false;
	
	/**
	 * The constructor.
	 */
	public PdfAnnotPlugin() {
		plugin = this;
		try {
			// a requirement of JPedal, that for Linux the java version is 1.5 or newer  .. jb
			String name = System.getProperty("os.name");
			
			String version=System.getProperty("java.version");
			
			if (name.equals("Linux")&&((version.startsWith("1.4")||(version.startsWith("1.3"))))){
				isBroken=true;
				MessageDialog.openError(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(), 
						"Plugin needs 1.5 under Linux","Linux needs Java 1.5 or greater to support Pliny's PDFEditor plugin");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}

	/**
	 * This method is called upon plug-in activation
	 */
	public void start(BundleContext context) throws Exception {
		if(!isBroken)
		super.start(context);
		//SamplingFactory.setDownsampleMode(SamplingFactory.medium);
		// http://support.idrsolutions.com/default.asp?W19
		/*
		 * -Dorg.jpedal.memory =true
		 * Downsamples images to save memory. 
		 */
		//System.setProperties();
		System.setProperty("org.jpedal.memory", "false");
		System.setProperty("org.jpedal.jai", "true");
	}

	/**
	 * This method is called when the plug-in is stopped
	 */
	public void stop(BundleContext context) throws Exception {
		if(!isBroken)
		super.stop(context);
		plugin = null;
	}

	/**
	 * Returns the shared instance.
	 */
	public static PdfAnnotPlugin getDefault() {
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
		return AbstractUIPlugin.imageDescriptorFromPlugin("uk.ac.kcl.cch.jb.pliny.pdfAnnot", path);
	}
	
	private static ObjectType myPdfObjectType = null;
	
	
	/**
	 * returns the ObjectType for PDF resources that the editor in this
	 * plugin can handle.
	 * 
	 * @return the ObjectType for PDF resources.
	 */
	public static ObjectType getMyObjectType(){
		if(myPdfObjectType != null)return myPdfObjectType;
		myPdfObjectType = ObjectType.findFromIds(PLUGIN_ID, EDITOR_ID);
		if(myPdfObjectType == null){
			myPdfObjectType = new ObjectType(true);
			myPdfObjectType.setEditorId(PdfAnnotPlugin.EDITOR_ID);
			myPdfObjectType.setName("PDF/Acrobat");
			//ImageDescriptor icon = ImageResPlugin.getImageDescriptor("icons/imageIcon.gif");
			myPdfObjectType.setIconId("editor:"+PdfAnnotPlugin.EDITOR_ID);
			myPdfObjectType.reIntroduceMe();
			myPdfObjectType.setPlugin(uk.ac.kcl.cch.jb.pliny.model.Plugin.
					findFromId(PdfAnnotPlugin.PLUGIN_ID));
		}
		return myPdfObjectType;
	}
	
	public IFileCacheManager getCache(){
		if(manager == null)
			manager = PlinyPlugin.getPlinyDataServerInstance().createCacheManager(this, "pdfCache", "c");
		return manager;
	}

}
