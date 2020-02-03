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

package uk.ac.kcl.cch.jb.xmlWriter;

import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;

/**
 * provides a simple-minded generic mechanism to create an XML file when the
 * data is available in document order -- sort of the opposite to SAX.  Not
 * all possible components of an XML file can be created using this mechanism,
 * but we have enough for our needs within Pliny!!
 * 
 * @author John Bradley
 *
 */

public class XmlFile {
	
	private String fileName = null;
	private OutputStreamWriter out;
	private XmlElement docElement;
	private boolean includeXmlDeclaration = true;
	private boolean includeDocType = false;
	private String publicIdentifier = null;
	private String systemIdentifier = null;
	
	public XmlFile(String fileName) {
		super();
		this.fileName = fileName;
	}
	
	public void setIncludeXmlDeclaration(boolean value){
		includeXmlDeclaration = value;
	}

	public void setIncludeDocType(boolean value){
		includeDocType = value;
	}
	
	public void setPublicIdentifier(String value){
		publicIdentifier = value;
		includeDocType = true;
	}
	
	public void setSystemIdentifier(String value){
		systemIdentifier = value;
	}
	
	OutputStreamWriter getWriter(){
		return out;
	}
	
	protected void open() throws IOException{
		OutputStream fout = new FileOutputStream(fileName);
		OutputStream bout = new BufferedOutputStream(fout);
		out = new OutputStreamWriter(bout, "UTF-8");
		
		if(includeXmlDeclaration)
		   out.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");  
        if(docElement != null && includeDocType)doIncludeOfDocType();
	}
	
	private void doIncludeOfDocType() throws IOException {
		out.write("<!DOCTYPE "+docElement.getGiName());
		if(publicIdentifier != null)
			out.write(" PUBLIC \""+publicIdentifier+"\"");
		if(systemIdentifier != null)
			out.write(" \""+systemIdentifier+"\"");
		out.write(">\n");
	}

	public void setDocumentElement(XmlElement element) throws IOException{
		element.setFile(this);
		docElement = element;
		open();
	}
	
	public void close() throws IOException{
		docElement.close();
		out.flush();
		out.close();
	}

}
