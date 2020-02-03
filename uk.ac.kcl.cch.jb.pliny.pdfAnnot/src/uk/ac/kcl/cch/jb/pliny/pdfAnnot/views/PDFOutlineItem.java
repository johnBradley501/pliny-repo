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

package uk.ac.kcl.cch.jb.pliny.pdfAnnot.views;

import java.util.Vector;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * the model item for the PDFOutline view's display -- each item corresponds
 * to an item in the PDF's table of contents.
 * <p>
 * Some code in here is derived from a JPedal example.
 * 
 * @author John Bradley
 */

public class PDFOutlineItem {
	
	private String title = null;
	private int page = 0;
	private String rawDest = null;
	private String ref = null;
	private Vector children = new Vector();
	private PDFOutlineItem parent = null;

	public PDFOutlineItem() {
		super();
		// TODO Auto-generated constructor stub
	}
	
	private void setParent(PDFOutlineItem parent){
		this.parent = parent;
	}
	
	public PDFOutlineItem getParent(){
		return parent;
	}
	
	public String getTitle(){
		return title;
	}
	
	public int getPage(){
		return page;
	}
	
	public Vector getChildren(){
		return children;
	}
	
	private int decodePage(String page){
		int rslt = 0;
		if(page.equals(""))return 0;
		try {
			rslt = Integer.parseInt(page);
		}
		catch(NumberFormatException nfe) {
			System.out.println("bad page number: " + page); 
			return 0;
		}
		return rslt;
	}
	
	public PDFOutlineItem(Element currentElement){
		//System.out.println("  PDFOutlineItem: currentElement: "+currentElement.toString());
		title = currentElement.getAttribute("title");
		page=decodePage(currentElement.getAttribute("page")); 
		rawDest=currentElement.getAttribute("dest"); 
		ref=currentElement.getAttribute("objectRef");
		if(currentElement.hasChildNodes())
			collectChildren(currentElement);
	}

	public void collectChildren(Element currentElement) {
		//children = new Vector();
		if(currentElement == null)return;
		NodeList list = currentElement.getChildNodes();
		int childCount = list.getLength();
        for(int i=0;i<childCount;i++){
			Node child=list.item(i);
			PDFOutlineItem childItem = 
				new PDFOutlineItem((Element) child);
			childItem.setParent(this);
			children.add(childItem);
        }
	}
	
	public boolean hasChildren(){
		if(children == null)return false;
		return children.size() > 0;
	}

	public void setAsRoot(Document outline) {
		Node rootNode = outline.getFirstChild();
        collectChildren((Element)rootNode);
	}

}
