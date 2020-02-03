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

package uk.ac.kcl.cch.jb.pliny.browser;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DropTarget;
import org.eclipse.swt.dnd.DropTargetEvent;
import org.eclipse.swt.dnd.DropTargetListener;
import org.eclipse.swt.dnd.FileTransfer;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.widgets.Combo;

/**
 * A SWT DropTargetListener that allows the user to drop a URL
 * in the form of either the icon on an external web browser's address
 * field or a URL shortcut onto the internal browser's address
 * field.  This will cause the internal browser to redirect itself
 * to the given web page.
 * <p>
 * The code tries to locate the URL within the data provided by
 * the drop, and if successful calls
 * {@link BrowserViewer#setURL} to direct the internal browser
 * to that page.
 * 
 * @author Bradley
 *
 */
public class URLDropTargetListener implements DropTargetListener {

	private Combo locationItem;
	private BrowserViewer viewer;
	
	public URLDropTargetListener(Combo locationItem, BrowserViewer viewer) {
		this.locationItem = locationItem;
		this.viewer = viewer;
		int operations = DND.DROP_MOVE | DND.DROP_COPY | DND.DROP_DEFAULT; 
		DropTarget target = new DropTarget(locationItem, operations);
		
		final TextTransfer textTransfer = TextTransfer.getInstance(); 
		final FileTransfer fileTransfer = FileTransfer.getInstance(); 
		Transfer[] types = new Transfer[] {fileTransfer, textTransfer}; 
		target.setTransfer(types); 
		target.addDropListener(this);
	}

	public void dragEnter(DropTargetEvent event) {
		event.detail = DND.DROP_COPY;
	}

	public void dragLeave(DropTargetEvent event) {
		// nothing to do here
	}

	public void dragOperationChanged(DropTargetEvent event) {
		// nothing to do here
	}

	public void dragOver(DropTargetEvent event) {
		// nothing to do here
	}

	public void drop(DropTargetEvent event) {
		if (TextTransfer.getInstance().isSupportedType(event.currentDataType)) {
			String text = (String)event.data;
			processText(text);
		}
		if (FileTransfer.getInstance().isSupportedType(event.currentDataType)){
			String[] files = (String[])event.data;
			if(files.length == 1)processFile(files[0]);
		}
	}

	private void processFile(String name) {
		IPath myPath = new Path(name);
		String ext = myPath.getFileExtension().toLowerCase();
		if(ext.equals("url")) handleUrlFile(myPath);
	}
	
	protected void handleUrlFile(IPath myPath){
		File myFile = myPath.toFile();
		FileInputStream myStream = null;
		try {
			myStream = new FileInputStream(myFile);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return;
		}
		String contents = getFileContentsAsString(myStream);
		contents = contents.replaceAll("\r","").trim();
		
		
		/* file format from IE
 
		[DEFAULT]
		 BASEURL=http://137.73.122.75:8084/diamm/loginform.jsp
		 [InternetShortcut]
		 URL=http://137.73.122.75:8084/diamm/loginform.jsp
		 Modified=C0BC896C2BAEC50110
		 */
		String[] inputLines = contents.split("\n");
		for(int i = 0; i < inputLines.length; i++){
			if((inputLines[i].matches("\\[InternetShortcut\\]")) && ((i+1)<inputLines.length)){
				String url = inputLines[i+1];
				handleUrl(url.replaceAll("URL=",""));
			}
		}
	}
	
	protected String getFileContentsAsString(InputStream stream){
		if(stream == null)return "";
		StringBuffer buf = new StringBuffer();
		int c;
		try {
			while((c = stream.read()) != -1){
				buf.append((char)c);
			}
			stream.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			try {
				stream.close();
			} catch (IOException e1) {
			}
			return new String(buf);
		}
		return new String(buf);
	}

	private void processText(String input) {
		input = input.replaceAll("\r","").trim();
		if(input.startsWith("http:") || input.startsWith("ftp:")  || input.startsWith("file:")){
			String[]inputParts = (input+"\n \n").split("\n");
			handleUrl(inputParts[0]);
		}
	}

	private void handleUrl(String string) {
		try {
			URL theURL = new URL(string);
			locationItem.setText(string);
			viewer.setURL(string);
		} catch (MalformedURLException e) {
			return;
		}
		
	}

	public void dropAccept(DropTargetEvent event) {
		// nothing to do here.

	}

}
