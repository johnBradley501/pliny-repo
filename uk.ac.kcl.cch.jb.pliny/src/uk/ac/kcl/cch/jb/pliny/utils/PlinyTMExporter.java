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

import java.io.IOException;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Vector;

import org.eclipse.core.runtime.IProgressMonitor;

import uk.ac.kcl.cch.jb.pliny.model.LOType;
import uk.ac.kcl.cch.jb.pliny.model.LOTypeQuery;
import uk.ac.kcl.cch.jb.pliny.model.Link;
import uk.ac.kcl.cch.jb.pliny.model.LinkQuery;
import uk.ac.kcl.cch.jb.pliny.model.LinkableObject;
import uk.ac.kcl.cch.jb.pliny.model.LinkableObjectQuery;
import uk.ac.kcl.cch.jb.pliny.model.Note;
import uk.ac.kcl.cch.jb.pliny.model.Resource;
import uk.ac.kcl.cch.jb.pliny.model.ResourceQuery;
import uk.ac.kcl.cch.jb.xmlWriter.XmlElement;
import uk.ac.kcl.cch.jb.xmlWriter.XmlFile;

/**
 * the exporter that takes Pliny model data and transforms it into
 * a Topic Map representation.
 * 
 * @author John Bradley
 *
 */
public class PlinyTMExporter implements IPlinyExporter{
	private String fileName = null;
	private IProgressMonitor monitor;
	private Hashtable targetRefs = new Hashtable();
	private Vector addedTargetRefs = new Vector();
	private int addedCount = 0;
	private IPlinyExporterDataProvider provider = null;


	public void prepareRun(IPlinyExporterDataProvider provider, String fileName) {
		this.provider = provider;
		this.fileName = fileName;
	}

	public void finishRun() {
		provider = null;
	}

	//public PlinyTMExporter(IPlinyExporterDataProvider provider2, String fileName2) {
	//	super();
	//	fileName = null;
	//	this.provider = provider2;
	//}
	
	//public void setFileName(String fileName){
	//	this.fileName = fileName;
	//}
	
	//public PlinyTMExporter(String fileName){
	//	this.fileName = fileName;
	//}
	
	//public void dispose(){
	//	// nothing here at present
	//}

	public void run(IProgressMonitor monitor) throws PlinyExportException {
		if(provider == null)
			throw new RuntimeException("PlinyTMExporter run called without provider specified.");
		this.monitor = monitor;
		if(fileName == null){
			monitor.done();
			return;
		}
		monitor.beginTask("Exporting data to a Topic Map", 4);
		try {
			XmlFile xmlFile = new XmlFile(fileName);
			XmlElement topicMap = new XmlElement("topicMap");
			xmlFile.setDocumentElement(topicMap);
			topicMap.addAttribute("xmlns","http://www.topicmaps.org/xtm/1.0/");
			topicMap.addAttribute("xmlns:xlink","http://www.w3.org/1999/xlink");
			
	        addBaseTypes(topicMap);
			handleLinkTypes(topicMap);
	        handleResources(topicMap);
	        handleLinkableObjects(topicMap);
	        handleLinks(topicMap);
	        
	        buildAddedTargetRefs(topicMap);
			
			xmlFile.close();
		} catch (IOException e) {
			e.printStackTrace();
			throw new PlinyExportException("IOError: "+e.getLocalizedMessage());
		}
		monitor.done();
	}

	private void addBaseTypes(XmlElement topicMap) throws IOException {
		topicMap.addComment("base types");
		XmlElement base = new XmlElement("topic");
		topicMap.addContent(base);
		base.addAttribute("id","base.source");
		addBaseName(base, "source");
		base = new XmlElement("topic");
		topicMap.addContent(base);
		base.addAttribute("id", "base.target");
		addBaseName(base, "target");
	}

	private void handleLinkTypes(XmlElement topicMap) throws IOException {
		topicMap.addComment("link types");
		//Iterator it = new LOTypeQuery().executeQuery().iterator();
		Iterator it = provider.getLOTypes();
		while(it.hasNext()){
			LOType item = (LOType)it.next();
			XmlElement node = new XmlElement("topic");
			topicMap.addContent(node);
			node.addAttribute("id","linkType."+item.getALID());
			
			String name = item.getName();
			if(name == null || name.length() == 0)name = "(contains)";
			addBaseName(node, name);
		}
		monitor.worked(1);
	}
	
	private void addBaseName(XmlElement node, String name) throws IOException{
		XmlElement baseName = new XmlElement("baseName");
		node.addContent(baseName);
		XmlElement nameString = new XmlElement("baseNameString");
		baseName.addContent(nameString);
		nameString.addContent(name);
	}

	private void handleResources(XmlElement topicMap) throws IOException {
		topicMap.addComment("resources");
		//Iterator it = new ResourceQuery().executeQuery().iterator();
		Iterator it = provider.getResources();
		while(it.hasNext()){
			Resource item = (Resource)it.next();
			XmlElement node = new XmlElement("topic");
			topicMap.addContent(node);
			node.addAttribute("id","resource."+item.getALID());
			String urlString = item.getUrl();
			if(urlString != null && urlString.length() > 0){
				XmlElement subIdent = new XmlElement("subjectIdentity");
				node.addContent(subIdent);
				XmlElement ref = new XmlElement("subjectIndicatorRef");
				subIdent.addContent(ref);
				ref.addAttribute("xlink:href",urlString);
			}
			String name = item.getName();
			if(name != null && name.length() > 0){
			   addBaseName(node, name);
			}
			if(item instanceof Note){
				Note note = (Note)item;
				String contents = note.getContent();
				if(contents != null && contents.length() > 0){
					XmlElement occurrence = new XmlElement("occurrence");
					node.addContent(occurrence);
					XmlElement data = new XmlElement("resourceData");
					occurrence.addContent(data);
					data.addContent(contents);
				}
			}
		}
		monitor.worked(1);
	}

	private void handleLinkableObjects(XmlElement topicMap) throws IOException{
		topicMap.addComment("linkable objects");
		//Iterator it = new LinkableObjectQuery().executeQuery().iterator();
		Iterator it = provider.getLinkableObjects();
		while(it.hasNext()){
			LinkableObject item = (LinkableObject)it.next();
			if(item.getSurrogateFor() != null){
				String id = "lo."+item.getALID();
				buildAssociation(topicMap, id, 
						item.getLoType(), 
						item.getDisplayedIn(), 
						item.getSurrogateFor());
			}
		}
		monitor.worked(1);
	}
	
	private void buildAssociation(XmlElement topicMap, 
			String id, 
			LOType myType, 
			Resource source, 
			Resource target) throws IOException{
		if(source == null || target == null)return;
		XmlElement node = new XmlElement("association");
		topicMap.addContent(node);
		node.addAttribute("id",id);
		if(myType != null/* && myType.getALID() != 1*/){
			XmlElement instanceOf = new XmlElement("instanceOf");
			node.addContent(instanceOf);
			XmlElement topicRef = new XmlElement("topicRef");
			instanceOf.addContent(topicRef);
			topicRef.addAttribute("xlink:href","#linkType."+myType.getALID());
		}
		addMember(node, getTargetRef(myType,true), source);
		addMember(node, getTargetRef(myType,false), target);
	}
	
	private String getTargetName(LOType type, Resource resource, String typeName){
		if(resource != null)return "resource."+resource.getALID();
		if(type.getALID() == 1)return "base."+typeName;
		String rslt = "role."+(++addedCount);
		addedTargetRefs.add(rslt+"\t"+typeName+"\t"+type.getName());
		return rslt;
	}
	
	private String getTargetRef(LOType type, boolean source){
		String key = type.getALID()+"/"+source;
		if(targetRefs.containsKey(key))return (String)targetRefs.get(key);
		String rslt;
		if(source)rslt = getTargetName(type, type.getSourceRole(), "source");
		else rslt = getTargetName(type, type.getTargetRole(), "target");
		targetRefs.put(key, rslt);
		return rslt;
	}
	
	private void addMember(XmlElement node, String type, Resource item) throws IOException{
		XmlElement member = new XmlElement("member");
		node.addContent(member);
		XmlElement roleSpec = new XmlElement("roleSpec");
		member.addContent(roleSpec);
		XmlElement topicRef = new XmlElement("topicRef");
		roleSpec.addContent(topicRef);
		topicRef.addAttribute("xlink:href","#"+type);
		topicRef = new XmlElement("topicRef");
		member.addContent(topicRef);
		topicRef.addAttribute("xlink:href","#resource."+item.getALID());
	}

	private void handleLinks(XmlElement topicMap) throws IOException {
		topicMap.addComment("links");
		//Iterator it = new LinkQuery().executeQuery().iterator();
		Iterator it = provider.getLinks();
		while(it.hasNext()){
			Link item = (Link)it.next();
			String id = "link."+item.getALID();
			buildAssociation(topicMap, id, 
					item.getLoType(), 
					item.getFromLink().getSurrogateFor(), 
					item.getToLink().getSurrogateFor());
		}
		monitor.worked(1);
	}

	private void buildAddedTargetRefs(XmlElement topicMap) throws IOException {
		if(addedTargetRefs.size() == 0)return;
		topicMap.addComment("added Roles");
		Iterator it = addedTargetRefs.iterator();
		while(it.hasNext()){
			String[] parms = ((String)it.next()).split("\t");
			String id = parms[0];
			String sOrT = parms[1];
			String typeName = parms[2];
			XmlElement node = new XmlElement("topic");
			topicMap.addContent(node);
			node.addAttribute("id",id);
			addBaseName(node, typeName+" "+sOrT);
		}
	}

}
