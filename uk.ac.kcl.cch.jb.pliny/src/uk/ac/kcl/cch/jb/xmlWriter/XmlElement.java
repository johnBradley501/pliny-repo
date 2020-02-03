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

import java.io.IOException;

/**
 * allows for the creation of an XML element in the writing of an XML
 * file by {@link XmlFile}.
 * 
 * @author John Bradley
 *
 */

public class XmlElement {
	
	private String name;
	//private Vector attributes;
	private XmlFile xmlFile;
	private XmlElement currContent = null;
	
	private int state = 0;
	private static final int notStarted = 0;
	private static final int nameWritten = 1;
	//private static final int attsWritten = 2;
	private static final int headerDone = 3;

	public XmlElement(String name) {
		super();
		this.name = name;
	}
	
	void setFile(XmlFile xmlFile){
		this.xmlFile = xmlFile;
	}
	
	public void addAttribute(String name, String value) throws IOException{
		if(state == notStarted )writeGIName();
		xmlFile.getWriter().write(" "+name+"=\""+makeXMLAttribute(value)+"\"");
	}
	
	public String getGiName(){
		return name;
	}
	
	private void writeGIName() throws IOException {
		xmlFile.getWriter().write("<"+name);
		state = nameWritten;
		
	}

	public void addContent(XmlElement content) throws IOException{
		if(state == headerDone){
			if(currContent != null)currContent.close();
		}
		currContent = content;
		currContent.setFile(xmlFile);
		if(state == notStarted)writeGIName();
		if(state != headerDone)xmlFile.getWriter().write(">\n");
		state = headerDone;
	}
	
	public void addContent(String string) throws IOException{
		if(state == headerDone){
			if(currContent != null)currContent.close();
		}
		currContent = null;
		if(state == notStarted)writeGIName();
		if(state != headerDone)xmlFile.getWriter().write(">");
		state = headerDone;
		xmlFile.getWriter().write(makeXMLString(string));
	}
	
	public void addComment(String string) throws IOException{
		if(state == headerDone){
			if(currContent != null)currContent.close();
		}
		currContent = null;
		if(state == notStarted)writeGIName();
		if(state != headerDone)xmlFile.getWriter().write(">\n");
		state = headerDone;
		xmlFile.getWriter().write("\n<!-- "+string+" -->\n\n");
	}
	
	public void close() throws IOException{
		if(state != headerDone){
			if(state == notStarted)writeGIName();
			xmlFile.getWriter().write("/>\n");
			return;
		}
		if(currContent != null)currContent.close();
		xmlFile.getWriter().write("</"+name+">\n");
	}
	
	static protected String update(String pat, char match, String repl){
      StringBuffer sb = new StringBuffer();
      int prev = 0, next = 0;
      next = pat.indexOf(match, prev);
      while(next != -1){
         String chunk = pat.substring(prev, next);
         sb.append(chunk + repl);
         prev = next+1;
         next = pat.indexOf(match, prev);
      }
      sb.append(pat.substring(prev));
      return new String(sb);
	}

    private String makeXMLString(String val){
	      val = update(val, '&',"&amp;");
	      val = update(val, '<',"&lt;");
	      val = update(val, '>',"&gt;");
	      return val;
	   }

    public String makeXMLAttribute(String val){
      if(val == null)return "";
      val = update(val, '&',"&amp;");
      val = update(val, '"', "&dquote;");
      return val;
   }


}
