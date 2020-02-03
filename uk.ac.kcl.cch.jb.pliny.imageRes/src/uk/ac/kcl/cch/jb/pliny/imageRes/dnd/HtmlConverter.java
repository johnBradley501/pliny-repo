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

package uk.ac.kcl.cch.jb.pliny.imageRes.dnd;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Vector;

import org.eclipse.swt.graphics.ImageData;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.tidy.Tidy;

/**
 * a utility class that processes HTML from a web page for Image Resources by
 * extracting the text (which will appear in a note initially set up
 * on the image page) and coding it into a WIKI-like markup, and
 * locating images suitable as Image Resources and identifying them.
 * <p>
 * This uses methods provided by <code>org.w3c.tidy</code>, for which thanks
 * is hereby given.
 * 
 * @author John Bradley
 */
public class HtmlConverter {
	private Document domDocument = null;
	private URL theURL = null;
	private Element root = null;
	
	/**
	 * this constructor takes an InputStream that points to the HTML
	 * page specified by the given URL, and uses <code>org.w3c.tidy</code>
	 * to create a DOM of the text, which can subsequently be harvested
	 * for either text or list of images.
	 * 
	 * @param in an InputStream for the HTML page.
	 * @param theURL the URL to the HTML page.
	 */
	public HtmlConverter(InputStream in, URL theURL){
		this.theURL = theURL;
		Tidy tidy = new Tidy();
        tidy.setXmlOut(true);
        tidy.setQuiet(true);
        tidy.setDropEmptyParas(true);
        tidy.setDropFontTags(true);
        tidy.setFixComments(true);
        tidy.setShowWarnings(false);
        tidy.setQuoteAmpersand(true);
        tidy.setLogicalEmphasis(true);
        domDocument = tidy.parseDOM( in, null );
		root = domDocument.getDocumentElement();
	}
	
	/**
	 * fetches information about images that were found on the given HTML
	 * page.
	 * 
	 * @return an array of ImageData elements.
	 */
	public HtmlImageData[] getImageData(){
		if(root == null)return null;
		Vector rslts = new Vector();
		NodeList list = root.getElementsByTagName("img");
		for(int i = 0; i < list.getLength(); i++){
			Element imgTag = (Element)list.item(i);
			String srcAttr = imgTag.getAttribute("src");
			srcAttr = srcAttr.replaceAll("\\&amp;","&");
			String altAttr = null;
			if(imgTag.hasAttribute("alt"))altAttr = imgTag.getAttribute("alt");
			if((srcAttr != null)  && (!srcAttr.trim().equals(""))){
				URL imgURL;
				try {
					imgURL = new URL(theURL, srcAttr);
				} catch (MalformedURLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					imgURL = null;
				}
				if(imgURL != null)rslts.add(new HtmlImageData(altAttr, imgURL));
			}
		}
		return (HtmlImageData[])rslts.toArray(new HtmlImageData[0]);
	}
	
	/**
	 * gets the text of the HTML title element.  If not provided, returns an empty
	 * string.
	 */
	
	public String getTitle(){
		if(root == null)return null;
		NodeList list = root.getElementsByTagName("title");
		if(list.getLength()<=0)return null;
		Element titleTag = (Element)list.item(0);
        Node child = titleTag.getFirstChild();
        if(child == null)return "";
        return child.getNodeValue();
	}
	
	private StringBuffer buf = null;
	private boolean preserveLayout = false;
	private boolean trimLeadingBlanks = false;
	private int numbLines = 99;
	
	private void handleGenericTag(Element node){
		NodeList list = node.getChildNodes();
		for(int i = 0; i< list.getLength(); i++){
			processNode(list.item(i));
		}
	}
	
	private void conditionalBlankLines(int no){
		int needed = no - numbLines;
		if(needed <= 0)return;
		numbLines = no;
		for(int i = 0; i < numbLines; i++)buf.append('\n');
	}
	
	private void blankLines(int no){
		numbLines = no;
		for(int i = 0; i < no; i++)buf.append('\n');
	}
	
	private void handlePtag(Element node){
		conditionalBlankLines(2);
		trimLeadingBlanks = true;
		blankLines(0);
		handleGenericTag(node);
		blankLines(1);
	}
	
	private void handleLItag(Element node){
		conditionalBlankLines(2);
		buf.append("* ");
		trimLeadingBlanks = true;
		blankLines(0);
		handleGenericTag(node);
		blankLines(1);
	}
	
	private void handleHeaderTag(Element node){
		conditionalBlankLines(2);
		buf.append("*");
		blankLines(0);
		handleGenericTag(node);
		trimLeadingBlanks = true;
		buf.append("*");
		blankLines(1);
	}
	
	private void handleBTag(Element node){
		buf.append("*");
		handleGenericTag(node);
		buf.append("*");
	}
	
	private void handleITag(Element node){
		buf.append("_");
		handleGenericTag(node);
		buf.append("_");
	}
	
	private void handlePRETag(Element node){
		preserveLayout = true;
		conditionalBlankLines(1);
		blankLines(0);
		handleGenericTag(node);
		preserveLayout = false;
	}
	
	private void handleTABLEtag(Element node){
		conditionalBlankLines(2);
		//buf.append("_________________");
		//blankLines(1);
		handleGenericTag(node);
		//conditionalBlankLines(1);
		//buf.append("_________________");
		blankLines(2);
	}
	
	boolean beginningRow = true;
	
	String lastText = "";
	
	private void handleTDtag(Element node){
		//if(!beginningRow)buf.append('\t');
		beginningRow = false;
		trimLeadingBlanks = true;
		if((lastText.length() > 0) && (lastText.charAt(lastText.length()-1) != ' '))buf.append(" ");
		//buf.append("| ");
		handleGenericTag(node);
	}
	
	private void handleTRtag(Element node){
		conditionalBlankLines(1);
		beginningRow = true;
		handleGenericTag(node);
		//buf.append("\t|");
		blankLines(1);
	}
	
	private void handleElement(Element node){
		String elementName = node.getNodeName();
		if(elementName.equals("title")) ; // ignore it
		else if(elementName.equals("head")) ; // ignore it
		else if(elementName.equals("script")) ; // ignore it
		else if(elementName.equals("p"))handlePtag(node);
		else if(elementName.equals("li"))handleLItag(node);
		else if(elementName.matches("h[1-9]"))handleHeaderTag(node);
		else if(elementName.equals("strong"))handleBTag(node);
		else if(elementName.equals("em"))handleITag(node);
		else if(elementName.equals("pre"))handlePRETag(node);
		else if(elementName.equals("br"))blankLines(1);
		else if(elementName.equals("hr")){
			conditionalBlankLines(1);
			buf.append("_____________");
			blankLines(1);
		}
		else if(elementName.equals("table"))handleTABLEtag(node);
		else if(elementName.equals("td") ||
				elementName.equals("th"))handleTDtag(node);
		else if(elementName.equals("tr"))handleTRtag(node);
		else handleGenericTag(node);
	}
	
	private void processNode(Node node){
		if(node.getNodeType() == Node.TEXT_NODE){
			String contents = node.getNodeValue();
			if(!preserveLayout){
			   contents = contents.replaceAll("\r","");
			   contents = contents.replaceAll("\n"," ");
			   contents = contents.replaceAll("  "," ");
			   contents = contents.replaceAll(">>\\]", " ");
			   if(trimLeadingBlanks){
				   trimLeadingBlanks = false;
				   contents = contents.replaceAll("^ *","");
			   }
			}
			buf.append(contents);
			if(contents.length() > 0)lastText = contents;
		}
		else if(node.getNodeType() == Node.ELEMENT_NODE){
			handleElement((Element)node);
		}
	}
	
	/**
	 * takes the DOM representation of the HTML page and converts the
	 * text found therein in to a WIKI-markup-like text string.
	 */
	
	public String getTextualContents(){
		buf = new StringBuffer();
		processNode(root);
		return new String(buf);
	}

}
