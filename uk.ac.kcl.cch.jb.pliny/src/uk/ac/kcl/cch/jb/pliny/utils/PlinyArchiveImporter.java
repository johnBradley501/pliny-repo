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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Date;
import java.sql.Time;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Properties;
import java.util.Vector;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Platform;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IEditorRegistry;
import org.eclipse.ui.PlatformUI;
import org.osgi.framework.Bundle;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import uk.ac.kcl.cch.jb.pliny.PlinyPlugin;
import uk.ac.kcl.cch.jb.pliny.dnd.IGetsArchiveEntries;
import uk.ac.kcl.cch.jb.pliny.dnd.IResourceExtensionProcessor;
import uk.ac.kcl.cch.jb.pliny.model.Favourite;
import uk.ac.kcl.cch.jb.pliny.model.LOType;
import uk.ac.kcl.cch.jb.pliny.model.LOTypeQuery;
import uk.ac.kcl.cch.jb.pliny.model.Link;
import uk.ac.kcl.cch.jb.pliny.model.LinkableObject;
import uk.ac.kcl.cch.jb.pliny.model.NoteLucened;
import uk.ac.kcl.cch.jb.pliny.model.ObjectType;
import uk.ac.kcl.cch.jb.pliny.model.ObjectTypeQuery;
import uk.ac.kcl.cch.jb.pliny.model.Plugin;
import uk.ac.kcl.cch.jb.pliny.model.PluginQuery;
import uk.ac.kcl.cch.jb.pliny.model.Resource;
import uk.ac.kcl.cch.jb.pliny.model.ResourceQuery;

/**
 * the Pliny Archive importer.  The Pliny Archive is a ZIP file.
 * The ZIP file contains an XML file containing the data about
 * Pliny model objects that come from the backing store, and items
 * that have been stored by
 * {@link uk.ac.kcl.cch.jb.pliny.dnd.IResourceExtensionProcessor IResourceExtensionProcessor}s.
 * For each 
 * {@link uk.ac.kcl.cch.jb.pliny.model.Resource Resource} that has a
 * IResoruceExtensionProcessor associated with it, the importer will give
 * processor a chance to fetch the data the exporter stored in the archive
 * for it, by a call to the processor's <code>#processArchiveEntries</code> method.
 * 
 * @see PlinyArchiveExporter
 * 
 * @author John Bradley
 */

public class PlinyArchiveImporter implements IPlinyImporter, IGetsArchiveEntries{

	private String fileName = null;
	private Document document = null;
	private ZipFile zipFile; 
	private IProgressMonitor monitor;
	private Vector roleData = new Vector();
	
	Hashtable cache = new Hashtable();
	Hashtable zipEntries = new Hashtable();
	
	public PlinyArchiveImporter() {
		fileName = null;
	}
	
	public void setFileName(String fileName){
		this.fileName = fileName;
	}

	public PlinyArchiveImporter(String fileName) {
		this.fileName = fileName;
	}
	
	public void dispose(){
		// nothing here at present
	}	
	
	private String getId(Element node) throws PlinyImportException{
		if(!node.hasAttribute("id"))
			throw new PlinyImportException(node.getNodeName()+" has no id attribute");
		return node.getAttribute("id");
	}
	
	private String getAttrValueStr(Element node, String name) throws PlinyImportException{
		if(!node.hasAttribute(name))
			throw new PlinyImportException(node.getNodeName()+" has no "+name+" attribute");
		String attrval = node.getAttribute(name);
		if(attrval == null)return "";
		return attrval;
	}
	
	private int getAttrValueInt(Element node, String name)throws PlinyImportException{
		if(!node.hasAttribute(name))
			throw new PlinyImportException(node.getNodeName()+" has no "+name+" attribute");
		String attrval = node.getAttribute(name);
		if(attrval == null)return 0;
		return Integer.parseInt(attrval);
	}
	
	private String currentDir = null;
	
	public InputStream getArchiveEntry(String name){
		if(name == null)return null;
		String fullName = currentDir == null ? name : currentDir+"/"+name;
		ZipEntry entry = (ZipEntry)zipEntries.get(fullName);
		if(entry == null)return null;
		try {
			return zipFile.getInputStream(entry);
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}


	public void run(IProgressMonitor monitor) throws PlinyImportException{
		this.monitor = monitor;
		if(fileName == null){
			monitor.done();
			return;
		}
		monitor.beginTask("Importing data", 11);
		
		try {
			zipFile = new ZipFile(fileName);
			Enumeration en = zipFile.entries();
			while(en.hasMoreElements()){
				ZipEntry entry = (ZipEntry)en.nextElement();
				if(!entry.isDirectory())zipEntries.put(entry.getName(),entry);
			}
		} catch (IOException e1) {
			e1.printStackTrace();
			throw new PlinyImportException("Open failed: "+e1.getMessage());
		}
		
		InputStream dbData = getArchiveEntry(PlinyArchiveExporter.DB_ENTRY_NAME);
		if(dbData == null)
			throw new PlinyImportException("Could not find "+PlinyArchiveExporter.DB_ENTRY_NAME+
					"in the archive.");
		
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		try {
			DocumentBuilder builder = factory.newDocumentBuilder();
			//document = builder.parse( new File(fileName) );
			document = builder.parse( dbData );
		} catch (SAXParseException spe) {
			throw new PlinyImportException("Parsing (line: "+spe.getLineNumber()+", col: "+spe.getColumnNumber()+"): "+spe.getMessage());
		} catch (IOException e){
			throw new PlinyImportException("IO error: "+e.getMessage());
		} catch (ParserConfigurationException e) {
			throw new PlinyImportException("Parser Configuration: "+e.getMessage());
		} catch (SAXException e) {
			throw new PlinyImportException("SAX exception: "+e.getMessage());
		}
		
		Element root = document.getDocumentElement();
		if(!(root.getNodeName().equals("Pliny"))){
			throw new PlinyImportException("Wrong document node: "+root.getNodeName());
		}
		if((!root.hasAttribute("version")) || (!root.getAttribute("version").equals("1.0"))){
			throw new PlinyImportException("Wrong or missing version for file");
		}
        monitor.worked(1);
		
		handlePlugins(root);
		handleObjectType(root);
		handleLinkType(root);
		handleNote(root); // must be done here, because data used by handleResource   jb
		handleResource(root);
		handleLinkableObject(root);
		handleLink(root);
		handleFavourite(root);
		
		fixupRoleData();
		PlinyPlugin.clearStreamBuffer();

		try {
			zipFile.close();
		} catch (IOException e) {
			e.printStackTrace();
			throw new PlinyImportException("Archive close failed: "+e.getMessage());
		}

	}

	private NodeList getList(Element root, String id, String id2) throws PlinyImportException{
		NodeList list = root.getElementsByTagName(id);
		if(list.getLength() != 1)throw new PlinyImportException("not exactly one '"+id+"' tag: "+list.getLength());
		Element dataRoot = (Element)list.item(0);
		return dataRoot.getElementsByTagName(id2);
	}

	private void handlePlugins(Element root) throws PlinyImportException {
		Hashtable ids = new Hashtable();
		Iterator it = new PluginQuery().executeQuery().iterator();
		while(it.hasNext()){
			Plugin item = (Plugin)it.next();
			ids.put(item.getIdString(), item);
		}
		
		NodeList list = getList(root, "plugins", "plugin");
		for(int i = 0; i < list.getLength(); i++){
			Element element = (Element)list.item(i);
			String id = getId(element);
			String idString = getAttrValueStr(element, "idString");
			if(ids.get(idString) != null)cache.put(id, ids.get(idString));
			else {
			    idString = idString.trim();
				Bundle myBundle = Platform.getBundle(idString);
				if(myBundle == null){
					displayMessage("Your Eclipse installation has no plugin with id '"+idString+
							"'.  Objects linked to it will be ignored.");
				} else {
					Plugin item = new Plugin();
					item.setIdString(idString);
					cache.put(id, item);
				}
			}
		}
        monitor.worked(1);
	}

	private void handleObjectType(Element root) throws PlinyImportException {
		Hashtable ids = new Hashtable();
		Iterator it = new ObjectTypeQuery().executeQuery().iterator();
		while(it.hasNext()){
			ObjectType item = (ObjectType)it.next();
			ids.put(item.getEditorId(), item);
		}

		NodeList list = getList(root, "types", "type");
		IEditorRegistry registry = PlatformUI.getWorkbench().getEditorRegistry();
		for(int i = 0; i < list.getLength(); i++){
			Element element = (Element)list.item(i);
			String id = getId(element);
			String idString = getAttrValueStr(element, "editorId");
			if(ids.get(idString) != null)cache.put(id, ids.get(idString));
			else {
				String pluginId = getAttrValueStr(element, "plugin");
				Plugin myPlugin = (Plugin)cache.get(pluginId);
				if(myPlugin != null){
			       idString = idString.trim();
			       if(registry.findEditor(idString) == null){
						displayMessage("Your Eclipse installation has no editor with id '"+idString+
						"'.  Objects linked to it will be ignored.");
			       } else {
			          ObjectType item = new ObjectType();
			          item.setName(getAttrValueStr(element, "name"));
			          item.setEditorId(idString);
			          if(element.hasAttribute("iconId"))
			             item.setIconId(getAttrValueStr(element, "iconId"));
			          item.setPlugin(myPlugin);
			          cache.put(id, item);
			       }
				}
			}
		}
		monitor.worked(1);

	}

	private void handleLinkType(Element root) throws PlinyImportException {
		Hashtable ids = new Hashtable();
		Iterator it = new LOTypeQuery().executeQuery().iterator();
		while(it.hasNext()){
			LOType item = (LOType)it.next();
			String key = item.getName().toLowerCase().replaceAll("[^ a-z0-9]","");
			ids.put(key, item);
		}

		NodeList list = getList(root, "linkTypes", "linkType");
		for(int i = 0; i < list.getLength(); i++){
			Element element = (Element)list.item(i);
			String id = getId(element);
			String keyStr = id.replaceAll("[^0-9]","");
			int key = Integer.parseInt(keyStr);
			if(key <= LOType.MAX_UNDELETABLE_TYPES){
				cache.put(id, LOType.getItem(key));
			} else {
				String name = getAttrValueStr(element, "name");
				String keyName = name.toLowerCase().replaceAll("[^ a-z0-9]","");
				if(ids.get(keyName) != null)cache.put(id, ids.get(keyName));
				else {
				     LOType item = new LOType(true);
				     item.setName(name);
				     item.setTitleForeColourRGB(LOType.stringToRGB(getAttrValueStr(element, "titleForeColour")));
				     item.setTitleBackColourRGB(LOType.stringToRGB(getAttrValueStr(element, "titleBackColour")));
				     item.setBodyForeColourRGB(LOType.stringToRGB(getAttrValueStr(element, "bodyForeColour")));
				     //if(element.hasAttribute("bodyBackColour"))
				     item.setBodyBackColourRGB(LOType.stringToRGB(getAttrValueStr(element, "bodyBackColour")));
				     item.reIntroduceMe();
				     if(element.hasAttribute("targetRole"))
				    	 roleData.add(id+"\tT\t"+getAttrValueStr(element, "targetRole"));
				    	 //item.setTargetRole((Resource)cache.get(getAttrValueStr(element, "targetRole")));
				     if(element.hasAttribute("sourceRole"))
				    	 roleData.add(id+"\tS\t"+getAttrValueStr(element, "sourceRole"));
				    	 //item.setSourceRole((Resource)cache.get(getAttrValueStr(element, "targetRole")));
				     cache.put(id, item);
				}
			}
		}
		monitor.worked(1);
	}

	private void handleNote(Element root) throws PlinyImportException {
		NodeList list = getList(root, "notes", "note");
		for(int i = 0; i < list.getLength(); i++){
			Element element = (Element)list.item(i);
			String id = getId(element);
			NoteLucened note = new NoteLucened(true); // not yet written to DB.
			Node firstChild = element.getFirstChild();
			//if(firstChild != null){
			//   if(firstChild.getNodeType() != Node.CDATA_SECTION_NODE)
			//	  throw new PlinyImportException("wrong kind of contents in note element, not CDATA");
			//   note.setContent(firstChild.getNodeValue().trim());
			//} else note.setContent("");
			note.setContent(collectNoteContent(firstChild));
			cache.put(id, note);
		}
        monitor.worked(1);
	}
	
	// exporter seems to sometimes split CDATA into separate CDATA's separated by
	// entities -- hence need for this code.   j.b.
	
	private String collectNoteContent(Node child) throws PlinyImportException {
		if(child == null)return "";
		StringBuffer buf = new StringBuffer();
		while(child != null){
			if(child.getNodeType() == Node.CDATA_SECTION_NODE)
				buf.append(child.getNodeValue());
			else if(child.getNodeType() == Node.TEXT_NODE)
				buf.append(child.getNodeValue());
			else throw new PlinyImportException("wrong kind of contents in note element, not CDATA or TEXT");
			child = child.getNextSibling();
		}
		return new String(buf).trim();
	}
	
	private Resource makeNewResource(ObjectType myType){
		Resource rslt = null;
		IResourceExtensionProcessor proc = PlinyPlugin.getResourceExtensionProcessor(myType.getALID());
		if(proc != null){
			rslt = proc.makeMyResource();
		}
		if(rslt == null)rslt = new Resource(true);
		return rslt;
	}

	private int oldKey = 0;
	
	public int getOldKey(){
		return oldKey;
	}

	private void handleResource(Element root) throws PlinyImportException {
		Hashtable ids = new Hashtable();
		Iterator it = new ResourceQuery().executeQuery().iterator();
		while(it.hasNext()){
			Resource item = (Resource)it.next();
			String identifier = item.getIdentifier();
			if((identifier.startsWith("note:"))||(identifier.startsWith("resource:")))identifier = null;
			if((identifier != null) && (!identifier.trim().equals("")))
			   ids.put(identifier, item);
		}

        SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd");
        SimpleDateFormat timeFormatter = new SimpleDateFormat("hh.mm.ss");
		
		NodeList list = getList(root, "resources", "resource");
		for(int i = 0; i < list.getLength(); i++){
			Element element = (Element)list.item(i);
			String id = getId(element);
			String oldKeyStr = id.substring(9); // "resource." 
			oldKey = Integer.parseInt(oldKeyStr);
			String typeId = element.getAttribute("type");
			if(cache.get(typeId) != null){
			   ObjectType myObjectType = (ObjectType)cache.get(typeId);
			   Resource myResource = null;
			   String ident = null;
			   if(element.hasAttribute("ident")){
				  ident = getAttrValueStr(element, "ident");
				  if((ident.startsWith("note:"))||(ident.startsWith("resource:")))ident = null;
				  else myResource = (Resource)ids.get(ident);
			   }
			   if(myResource != null)cache.put(id, myResource);
			   else {
				  myResource = (Resource)cache.get(id);
				  if(myResource == null){
					  myResource = makeNewResource(myObjectType);
					  cache.put(id, myResource);
				  }
				  myResource.setName(getAttrValueStr(element,"name"));
				  if(ident != null)
				    myResource.setIdentifiers(ident);
				  if(element.hasAttribute("attr"))
					  myResource.setAttributes(getAttrValueStr(element,"attr"));
				  else {
				     String attr = processAttributes(element);
				     if(attr != null)myResource.setAttributes(attr);
				  }
					try {
					   if(element.hasAttribute("creationDate"))
						  myResource.setCreationDate(new Date(dateFormatter.parse(element.getAttribute("creationDate")).getTime()));
					   if(element.hasAttribute("creationTime"))
						  myResource.setCreationTime(new Time(timeFormatter.parse(element.getAttribute("creationTime")).getTime()));;
					} catch (ParseException e) {
					   throw new PlinyImportException("Bad creation date/time given: "+e.getLocalizedMessage());
					}

				  myResource.reIntroduceMe();
				  myResource.setObjectType(myObjectType);
				  
				  IResourceExtensionProcessor myProc = PlinyPlugin.getResourceExtensionProcessor(myResource);
				  if(myProc != null){
					  currentDir = myObjectType.getEditorId();
					  myProc.processArchiveEntries(this, myResource);
					  currentDir = null;
				  }

			   }
			}
		}
        monitor.worked(1);
	}
	
	private String processAttributes(Element element){
		Properties p = new Properties();
		NodeList list = element.getChildNodes();
		if(list.getLength() == 0)return null;
		for(int i = 0; i < list.getLength(); i++){
			Node node = list.item(i);
			if(node.getNodeType() == Node.ELEMENT_NODE){
				Element e = (Element)node;
				if(e.hasAttribute("val")){
				   String key = e.getNodeName();
				   String val = e.getAttribute("val");
				   p.put(key, val);
				}
			}
		}
		ByteArrayOutputStream output = new ByteArrayOutputStream();
		p.save(output, "attr");
		return output.toString();
	}

	private void handleLinkableObject(Element root) throws PlinyImportException {
		NodeList list = getList(root, "linkableObjects", "object");
		for(int i = 0; i < list.getLength(); i++){
			Element element = (Element)list.item(i);
			String id = getId(element);
			Resource displayedIn = null;
			if(element.hasAttribute("displayedIn"))
			   displayedIn = (Resource)cache.get(getAttrValueStr(element,"displayedIn"));
			if(displayedIn != null){
				Resource surrogateFor = null;
				boolean buildLo = true;
				if(element.hasAttribute("surrogateFor")){
					String surrID = getAttrValueStr(element, "surrogateFor");
					surrogateFor = (Resource)cache.get(surrID);
					if(surrogateFor == null){
						buildLo = false;
						displayMessage("Surrogate for "+id+" ("+surrID+") is missing." );
					}
				}
				if(buildLo){
					LinkableObject object = new LinkableObject(true);
					object.setPosition(getAttrValueStr(element,"position"));
					object.setIsOpen(getAttrValueStr(element,"isOpen").equals("Y"));
					if(surrogateFor != null){
						object.setShowingMap(getAttrValueStr(element,"showingMap").equals("Y"));
					} else
						object.setShowingMap(false);
					
					if(element.hasAttribute("displPageNo"))
						object.setDisplPageNo(getAttrValueInt(element, "displPageNo"));
					if(element.hasAttribute("surrPageNo"))
						object.setSurrPageNo(getAttrValueInt(element, "surrPageNo"));
	
					object.reIntroduceMe();
					
					if(element.hasAttribute("type")){
						LOType myType = (LOType)cache.get(getAttrValueStr(element, "type"));
						if(myType == null)myType = LOType.getDefaultType();
						object.setLoType(myType);
					} else object.setLoType(LOType.getDefaultType());	
	
					object.setSurrogateFor(surrogateFor);
					object.setDisplayedIn(displayedIn);
					cache.put(id, object);
				}
			}
		}
        monitor.worked(1);
	}

	private void handleLink(Element root) throws PlinyImportException {
		NodeList list = getList(root, "links", "link");
		for(int i = 0; i < list.getLength(); i++){
			Element element = (Element)list.item(i);
			//String id = getId(element);
			LinkableObject from = (LinkableObject)cache.get(getAttrValueStr(element, "from"));
			LinkableObject to = (LinkableObject)cache.get(getAttrValueStr(element, "to"));
			if((from != null) && (to != null)){
				Link link = new Link(true);
				String attr = processAttributes(element);
				if(attr != null)link.setAttributes(attr);
				link.reIntroduceMe();
				link.setFromLink(from);
				link.setToLink(to);
				
				if(element.hasAttribute("type")){
					LOType myType = (LOType)cache.get(getAttrValueStr(element, "type"));
					if(myType == null)myType = LOType.getDefaultType();
					link.setLoType(myType);
				} else link.setLoType(LOType.getDefaultType());	
			}
		}
        monitor.worked(1);
	}
	
	private void handleFavourite(Element root) throws PlinyImportException {
		NodeList list = root.getElementsByTagName("favourites");
		if(list.getLength() == 0) return;
		list = getList(root, "favourites", "favourite");
		for(int i = 0; i < list.getLength(); i++){
			Element element = (Element)list.item(i);
			Resource favRes = (Resource)cache.get(getAttrValueStr(element,"item"));
			if(favRes != null){
				Favourite fav = new Favourite();
				fav.setResource(favRes);
			}
		}
		monitor.worked(1);
	}

	private void fixupRoleData() {
		Iterator it = roleData.iterator();
		while(it.hasNext()){
			String[]parms = ((String)it.next()).split("\t");
			LOType type = (LOType)cache.get(parms[0]);
			Resource resource = (Resource)cache.get(parms[2]);
			if(parms[1].equals("T"))type.setTargetRole(resource);
			else type.setSourceRole(resource);
			monitor.worked(1);
		}
		
	}

	private void displayMessage(String msg) {
		Shell parentShell = Display.getDefault().getActiveShell();
		MessageBox messageBox = new MessageBox(parentShell, SWT.ICON_ERROR | SWT.OK);
		messageBox.setText("Import Message");
		messageBox.setMessage(msg);
		messageBox.open();
	}

	public boolean getOptions(Shell parentShell) {
		return true;
	}
}
