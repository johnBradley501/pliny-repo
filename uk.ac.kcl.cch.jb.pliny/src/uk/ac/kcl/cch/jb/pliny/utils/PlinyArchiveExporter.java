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

package uk.ac.kcl.cch.jb.pliny.utils;

import java.io.ByteArrayInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.text.SimpleDateFormat;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Properties;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.eclipse.core.runtime.IProgressMonitor;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import uk.ac.kcl.cch.jb.pliny.PlinyPlugin;
import uk.ac.kcl.cch.jb.pliny.dnd.IResourceExtensionProcessor;
import uk.ac.kcl.cch.jb.pliny.dnd.IResourceExtensionProcessor.CacheElement;
import uk.ac.kcl.cch.jb.pliny.model.Favourite;
import uk.ac.kcl.cch.jb.pliny.model.FavouriteQuery;
import uk.ac.kcl.cch.jb.pliny.model.IHasAttributeProperties;
import uk.ac.kcl.cch.jb.pliny.model.LOType;
import uk.ac.kcl.cch.jb.pliny.model.LOTypeQuery;
import uk.ac.kcl.cch.jb.pliny.model.Link;
import uk.ac.kcl.cch.jb.pliny.model.LinkQuery;
import uk.ac.kcl.cch.jb.pliny.model.LinkableObject;
import uk.ac.kcl.cch.jb.pliny.model.LinkableObjectQuery;
import uk.ac.kcl.cch.jb.pliny.model.NoteLucened;
import uk.ac.kcl.cch.jb.pliny.model.NoteQuery;
import uk.ac.kcl.cch.jb.pliny.model.ObjectType;
import uk.ac.kcl.cch.jb.pliny.model.ObjectTypeQuery;
import uk.ac.kcl.cch.jb.pliny.model.Plugin;
import uk.ac.kcl.cch.jb.pliny.model.PluginQuery;
import uk.ac.kcl.cch.jb.pliny.model.Resource;
import uk.ac.kcl.cch.jb.pliny.model.ResourceQuery;

/**
 * the Pliny Archive exporter.  The Pliny Archive is a ZIP file.
 * The ZIP file contains an XML file containing the data about
 * Pliny model objects that come from the backing store.  Pliny
 * plugins who wish to contribute data to the archive file can do
 * so through their 
 * {@link uk.ac.kcl.cch.jb.pliny.dnd.IResourceExtensionProcessor IResourceExtensionProcessor}.
 * For each
 * {@link uk.ac.kcl.cch.jb.pliny.model.Resource Resource} for which
 * a <code>IResourceExtensionProcessor</code> can be identified, the
 * processor is asked (via a call to its method <code>#getCacheElements</code>)
 * if it has anything to contribute.  If so, the archiver will store returned
 * items in the archive as well.
 * 
 * @see PlinyArchiveImporter
 * 
 * @author John Bradley
 */
public class PlinyArchiveExporter implements IPlinyExporter{

	private String fileName = null;
	private Document document = null;
	ZipOutputStream zipFile = null;
	private IProgressMonitor monitor;
	private IPlinyExporterDataProvider provider = null;

	//private static final int bufLength = 16*1024;
	//private byte[] buffer = new byte[bufLength];
	
	public static final String DB_ENTRY_NAME="data.xml";

	//public PlinyArchiveExporter(IPlinyExporterDataProvider provider) {
	//	fileName = null;
	//	this.provider = provider;
	//}
	
	//public void setFileName(String fileName){
	//	this.fileName = fileName;
	//}

	public void prepareRun(IPlinyExporterDataProvider provider, String fileName) {
		this.fileName = fileName;
        this.provider = provider;		
	}

	public void finishRun() {
		provider = null;
		
	}
	
	//public PlinyArchiveExporter(IPlinyExporterDataProvider provider, String fileName){
	//	this.fileName = fileName;
	//	this.provider = provider;
	//}

	/**
	 * runs the exporting process.
	 * 
	 * @param monitor 
	 */
	public void run(IProgressMonitor monitor) throws PlinyExportException {
		if(provider == null)
			throw new RuntimeException("PlinyArchiveExporter run called without provider specified.");
		this.monitor = monitor;
		if(fileName == null){
			monitor.done();
			return;
		}
		monitor.beginTask("Exporting data", 10);
		try {
			zipFile = new ZipOutputStream(new FileOutputStream(fileName));
		} catch (FileNotFoundException e2) {
			throw new PlinyExportException("Creating Archive File: "+e2.getMessage());
		}
		
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        try {
        	DocumentBuilder builder = factory.newDocumentBuilder();
        	document = builder.newDocument();
        } catch (ParserConfigurationException pce) {
        	throw new PlinyExportException("Parser Configuration: "+pce.getMessage());
        }
        Element root = 
            (Element) document.createElement("Pliny");
        root.setAttribute("version", "1.0");
        document.appendChild(root);
        monitor.worked(1);
        
        handlePlugin(root);
        handleObjectType(root);
        handleLinkType(root);
        handleResource(root);
        handleNote(root);
        handleLinkableObject(root);
        handleLink(root);
        handleFavourite(root);
        
        DOMSource source = new DOMSource(document);
        
        try {
			zipFile.putNextEntry(new ZipEntry(DB_ENTRY_NAME));
		} catch (IOException e2) {
			throw new PlinyExportException("problem starting data.xml");
		}
        
        TransformerFactory tff = TransformerFactory.newInstance();
        Transformer tf = null;
        try {
			tf = tff.newTransformer();
		} catch (TransformerConfigurationException e) {
        	throw new PlinyExportException("Transformer Configuration: "+e.getMessage());
		}
		tf.setOutputProperty(OutputKeys.INDENT,"yes");

		StreamResult result = null;
		try {
			//result = new StreamResult(new FileWriter(fileName));
			result = new StreamResult(new OutputStreamWriter(zipFile, "UTF8"));
		} catch (IOException e1) {
        	throw new PlinyExportException("IO: "+e1.getMessage());
		}
		try {
			tf.transform(source, result);
		} catch (TransformerException e) {
        	throw new PlinyExportException("Transformer: "+e.getMessage());
		}
		try {
			zipFile.closeEntry();
			zipFile.close();
		} catch (IOException e) {
			e.printStackTrace();
        	throw new PlinyExportException("closing Zip file");
		}
		monitor.done();
		PlinyPlugin.clearStreamBuffer();
	}

	private void handlePlugin(Element root) {
		Element group = document.createElement("plugins");
		root.appendChild(group);
		
		//Iterator it = new PluginQuery().executeQuery().iterator();
		Iterator it = provider.getPlugins();
		while(it.hasNext()){
			Plugin item = (Plugin)it.next();
			Element node = document.createElement("plugin");
			group.appendChild(node);
			node.setAttribute("id", "plugin."+item.getALID());
			node.setAttribute("idString", item.getIdString());
		}
		monitor.worked(1);
	}

	private void handleObjectType(Element root) {
		Element group = document.createElement("types");
		root.appendChild(group);
		
		//Iterator it = new ObjectTypeQuery().executeQuery().iterator();
		Iterator it = provider.getObjectTypes();
		while(it.hasNext()){
			ObjectType item = (ObjectType)it.next();
			Element node = document.createElement("type");
			group.appendChild(node);
			node.setAttribute("id", "type."+item.getALID());
			node.setAttribute("name", item.getName());
			node.setAttribute("plugin", "plugin."+item.getPlugin().getALID());
			node.setAttribute("editorId", item.getEditorId());
			if(item.getIconId() != null)
			   node.setAttribute("iconId", item.getIconId());
		}
		monitor.worked(1);
	}

	private void handleLinkType(Element root) {
		Element group = document.createElement("linkTypes");
		root.appendChild(group);
		//Iterator it = new LOTypeQuery().executeQuery().iterator();
		Iterator it = provider.getLOTypes();
		while(it.hasNext()){
			LOType item = (LOType)it.next();
			Element node = document.createElement("linkType");
			group.appendChild(node);
			node.setAttribute("id", "linkType."+item.getALID());
			node.setAttribute("name", item.getName());
			node.setAttribute("titleForeColour", LOType.rgbToString(item.getTitleForeColourRGB()));
			node.setAttribute("titleBackColour", LOType.rgbToString(item.getTitleBackColourRGB()));
			node.setAttribute("bodyForeColour", LOType.rgbToString(item.getBodyForeColourRGB()));
			node.setAttribute("bodyBackColour", LOType.rgbToString(item.getBodyBackColourRGB()));
			if(item.getTargetRole() != null)
				node.setAttribute("targetRole", "resource."+item.getTargetRole().getALID());
			if(item.getSourceRole() != null)
				node.setAttribute("sourceRole", "resource."+item.getSourceRole().getALID());
		}
		monitor.worked(1);
	}
	
	private void processCacheElements(Resource resource, CacheElement[] elements){
		if(elements == null)return;
		String dirName = resource.getObjectType().getEditorId();
		for(int i = 0; i < elements.length; i++){
			//System.out.println(elements[i].fileName);
			try {
				zipFile.putNextEntry(new ZipEntry(dirName+"/"+elements[i].fileName));
				PlinyPlugin.copyInputStream(elements[i].inputStream, zipFile);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	private void handleResource(Element root) {
		Element group = document.createElement("resources");
		root.appendChild(group);
		
        SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd");
        SimpleDateFormat timeFormatter = new SimpleDateFormat("hh.mm.ss");

		
		//Iterator it = new ResourceQuery().executeQuery().iterator();
		Iterator it = provider.getResources();
		while(it.hasNext()){
			Resource item = (Resource)it.next();
			
			IResourceExtensionProcessor myProc = PlinyPlugin.getResourceExtensionProcessor(item);
			if(myProc != null){
				processCacheElements(item, myProc.getCacheElements(item));
			}

			Element node = document.createElement("resource");
			group.appendChild(node);
			node.setAttribute("id", "resource."+item.getALID());
			node.setAttribute("name", item.getName());
			node.setAttribute("type","type."+item.getObjectTypeKey());
			node.setAttribute("creationDate", dateFormatter.format(item.getCreationDate()));
			node.setAttribute("creationTime", timeFormatter.format(item.getCreationTime()));
			String ident = item.getIdentifier();
			if((ident != null) && (!ident.startsWith("note:")) && (!ident.startsWith("resource:")))
			    node.setAttribute("ident",ident);
			String attr = item.getAttributes();
			if((attr != null) && (!attr.equals(""))){
				if(item instanceof IHasAttributeProperties)
				   handleAttributes(node, attr);
				else node.setAttribute("attr",attr);
			}
		}
		monitor.worked(1);
	}

	private void handleAttributes(Element node, String attributes) {
		Properties p = new Properties();
		ByteArrayInputStream input = new ByteArrayInputStream(attributes.getBytes());
		try {
			p.load(input);
		} catch (IOException e) {
			e.printStackTrace();
			return;
		}
		Enumeration e = p.keys();
		while(e.hasMoreElements()){
			String key = (String)e.nextElement();
			String val = (String)p.get(key);
			Element child = document.createElement(key);
			child.setAttribute("val", val);
			node.appendChild(child);
		}
	}

	private void handleNote(Element root) {
		Element group = document.createElement("notes");
		root.appendChild(group);
		
		//Iterator it = new NoteQuery().executeQuery().iterator();
		Iterator it = provider.getNotes();
		while(it.hasNext()){
			NoteLucened item = (NoteLucened)it.next();
			Element node = document.createElement("note");
			group.appendChild(node);
			node.setAttribute("id", "resource."+item.getALID());
			if(item.getContent() != null){
				  node.appendChild(document.createCDATASection(item.getContent().replaceAll("\r","")));
				}
		}
		monitor.worked(1);
	}

	private void handleLinkableObject(Element root) {
		Element group = document.createElement("linkableObjects");
		root.appendChild(group);
		
		//Iterator it = new LinkableObjectQuery().executeQuery().iterator();
		Iterator it = provider.getLinkableObjects();
		while(it.hasNext()){
			LinkableObject item = (LinkableObject)it.next();
			Element node = document.createElement("object");
			group.appendChild(node);
			node.setAttribute("id", "lo."+item.getALID());
			if(item.getLoType() != null && item.getLoTypeKey() != 1)
				node.setAttribute("type","linkType."+item.getLoTypeKey());
			node.setAttribute("position",item.getPosition());
			if(item.getDisplayedInKey() > 0)
			    node.setAttribute("displayedIn","resource."+item.getDisplayedInKey());
			if(item.getSurrogateForKey() > 0)
				node.setAttribute("surrogateFor","resource."+item.getSurrogateForKey());
			if(item.getDisplPageNo() != 0)
				node.setAttribute("displPageNo",""+item.getDisplPageNo());
			if(item.getSurrPageNo() != 0)
				node.setAttribute("surrPageNo",""+item.getSurrPageNo());
			node.setAttribute("isOpen",item.getIsOpen()?"Y":"N");
			node.setAttribute("showingMap", item.getShowingMap()?"Y":"N");
		}
		monitor.worked(1);
	}

	private void handleLink(Element root) {
		Element group = document.createElement("links");
		root.appendChild(group);
		
		//Iterator it = new LinkQuery().executeQuery().iterator();
		Iterator it = provider.getLinks();
		while(it.hasNext()){
			Link item = (Link)it.next();
			Element node = document.createElement("link");
			group.appendChild(node);
			node.setAttribute("id", "link."+item.getALID());
			//if(item.getLoType() != null && item.getLoType().getALID() != 1)
		    if(item.getLoTypeKey() > 1) // 0 = unassigned, 1 means "default type"
				node.setAttribute("type","linkType."+item.getLoTypeKey());
			node.setAttribute("from","lo."+item.getFromLinkKey());
			node.setAttribute("to","lo."+item.getToLinkKey());
			String attr = item.getAttributes();
			if((attr != null) && (!attr.equals("")))handleAttributes(node, attr);
		}
		monitor.worked(1);
	}
	
	private void handleFavourite(Element root){
		Element group = document.createElement("favourites");
		root.appendChild(group);

		//Iterator it = new FavouriteQuery().executeQuery().iterator();
		Iterator it = provider.getFavourites();
		while(it.hasNext()){
			Favourite item = (Favourite)it.next();
			Element node = document.createElement("favourite");
			group.appendChild(node);
			node.setAttribute("id", "favorite."+item.getALID());
			node.setAttribute("item","resource."+item.getFavouriteResourceKey());
		}
		monitor.worked(1);
	}
}
