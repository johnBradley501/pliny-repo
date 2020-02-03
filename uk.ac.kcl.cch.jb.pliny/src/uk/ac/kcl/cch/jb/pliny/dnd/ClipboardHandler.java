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

package uk.ac.kcl.cch.jb.pliny.dnd;

import java.util.Collection;
import java.util.Iterator;
import java.util.Vector;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.gef.dnd.SimpleObjectTransfer;
import org.eclipse.jface.text.TextSelection;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.ui.IEditorDescriptor;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.part.FileEditorInput;
import org.eclipse.ui.part.ResourceTransfer;

import uk.ac.kcl.cch.jb.pliny.browser.BrowserResourceExtensionProcessor;
import uk.ac.kcl.cch.jb.pliny.model.LinkableObject;
import uk.ac.kcl.cch.jb.pliny.model.NoteLucened;
import uk.ac.kcl.cch.jb.pliny.model.ObjectType;
import uk.ac.kcl.cch.jb.pliny.model.Resource;
import uk.ac.kcl.cch.jb.pliny.utils.StringToNoteHandler;

/**
 * This class is a singleton class that handles Pliny-oriented cut, copy and
 * paste operations related to the Clipboard via Text transfer and 
 * GEF's internal-to-Eclipse copy and paste <code>SimpleObjectTransfer</code>.
 * 
 * @author John Bradley
 */
public class ClipboardHandler  {
	
	private static ClipboardHandler instance = null;

	public static final SimpleObjectTransfer TRANSFER = new SimpleObjectTransfer() {
		private final String TYPE_NAME = "org.eclipse.gef.clipboard.transfer"; //$NON-NLS-1$
		private final int TYPE_ID = registerType(TYPE_NAME);
		protected int[] getTypeIds() {
			return new int[] {TYPE_ID};
		}
		protected String[] getTypeNames() {
			return new String[] {TYPE_NAME};
		}
	};

	
	private ClipboardHandler() {
		super();
	}
	
	/**
	 * invoke this method to get access to the singleton instance of this class.
	 * 
	 * @return ClipboardHandler the handler.
	 */
	public static ClipboardHandler getDefault(){
		if(instance == null)instance = new ClipboardHandler();
		return instance;
	}
	
	/**
	 * invoke this method on copy or cut operations to send Pliny-oriented
	 * materials to the clipboard.  This code knows how to handle Pliny
	 * {@link uk.ac.kcl.cch.jb.pliny.model.NoteLucened NoteLucened}, 
	 * {@link uk.ac.kcl.cch.jb.pliny.model.Resource Resource} (more generally),
	 * {@link uk.ac.kcl.cch.jb.pliny.model.LinkableObject LinkableObject},
	 * and selections of text, and Collections of these objects.
	 * 
	 * @param contents Object to be sent to the clipboard
	 */
	public void setContents(Object contents){
		mySeparator = null;
		Clipboard cb = new Clipboard(null);
		cb.setContents(
			new Object[] {
				contents,
				asText(contents)}, 
			new Transfer[] {
				TRANSFER,
				TextTransfer.getInstance()});
		cb.dispose();
	}
	
	private static String mySeparator = null;
	
	public static String generateSeparator(){
		if(mySeparator == null){
			String sep = System.getProperty("line.separator");
			mySeparator = sep+sep;
			//return "";
		}
		return mySeparator;
	}
	
	private boolean sepNeeded = false;
	
	private String asText(Object contents){
		sepNeeded = false;
		return asTextWorker(contents);
	}

	private String asTextWorker(Object contents) {
		if(contents == null)return "";
		StringBuffer buf = new StringBuffer();
		//buf.append(generateSeparator());
		
		if(contents instanceof Collection){
			Iterator it = ((Collection)contents).iterator();
			while(it.hasNext()){
				buf.append(asTextWorker(it.next()));
			}
		//} else if(contents instanceof NoteLucened){
		//	NoteLucened note = (NoteLucened)contents;
		//	buf.append(generateSeparator());
		//	buf.append(note.getName());
		//	buf.append(generateSeparator());
		//	buf.append(note.getContent());
		} else if(contents instanceof Resource){
			Resource resource = (Resource)contents;
			if(sepNeeded)buf.append(generateSeparator());
			sepNeeded = true;
			//buf.append(resource.getName());
			buf.append(resource.getCutCopyText());
		} else if(contents instanceof LinkableObject){
			Resource surrogate = ((LinkableObject)contents).getSurrogateFor();
			if(surrogate == null) surrogate = ((LinkableObject)contents).getHeldSurrogate();
			buf.append(asTextWorker(surrogate));
		} else if(contents instanceof TextSelection){
			TextSelection selection = (TextSelection)contents;
			if(selection.getText() != null)
				buf.append(selection.getText());
		} else if(contents instanceof String)handlPlainString((String)contents, buf);
		return new String(buf);
	}
	
	private void handlPlainString(String contents, StringBuffer buf) {
		String[] lines = contents.split("\\n");
		String newline = System.getProperty("line.separator");
		for (int i = 0 ; i < lines.length; i++){
			buf.append(lines[i]);
			buf.append(newline);
		}
	}

	/**
	 * invoke this method on paste operations to generate Pliny-oriented
	 * materials from the contents of clipboard.  This code knows how to handle Pliny
	 * {@link uk.ac.kcl.cch.jb.pliny.model.NoteLucened NoteLucened}, 
	 * {@link uk.ac.kcl.cch.jb.pliny.model.Resource Resource} (more generally),
	 * {@link uk.ac.kcl.cch.jb.pliny.model.LinkableObject LinkableObject},
	 * and selections of text, Files, and Collections of these objects.
	 * <p>
	 * Files are handled by looking up the kind of Eclipse editor that can
	 * handle the particular file type, and then producing a Pliny resource
	 * that refers to the file and its editor.
	 * 
	 * @return Collection containing materials transformed into
	 * Pliny objects.
	 */
	public Collection getContents(){
		Collection rslt = null;
		Clipboard cb = new Clipboard(null);
		rslt = trySimpleTransfer(cb);
		if(rslt == null)rslt = tryResourceTransfer(cb);
		if(rslt == null)rslt = tryTextTransfer(cb);
		if(rslt == null)rslt = new Vector(); // returns empty result.
		cb.dispose();
		return rslt;
	}
	
	private Collection tryTextTransfer(Clipboard cb) {
		TextTransfer transfer = TextTransfer.getInstance();
		Object object = cb.getContents(transfer);
		Vector rslt = new Vector();
		handleObjectForText(rslt, object);
		return rslt;
	}
	
	/**
	 * processes various kind of text-related objects that can be generated
	 * by cut/copy/paste or drag-and-drop operations.  If the provided
	 * text turns out to be a value URL, the result will be a Resource
	 * that points to that URL.  Otherwise, the result is a
	 * {@link uk.ac.kcl.cch.jb.pliny.model.NoteLucened NoteLucened} containing
	 * the provided text.
	 * 
	 * @param object Object provided by cut/copy/paste or drag-drop
	 * @return Vector of Notes created from materials provided.
	 */
	public Vector processTextObject(Object object){
		Vector rslt = new Vector();
		handleObjectForText(rslt, object);
		return rslt;
	}

	private void handleObjectForText(Vector rslt, Object object) {
		if(object == null)return;
		if(object instanceof String){
			String theString = new String(((String)object).trim());
			theString.replaceAll("\\r", "");
			if(theString.startsWith("http:") || theString.startsWith("https:"))
			   rslt.add(BrowserResourceExtensionProcessor.processAsURL(theString));
			else rslt.add(StringToNoteHandler.getInstance().handleStringAsNote(theString));
		}
		else if(object instanceof String[]){
			String theStrings[] = (String[])object;
			for(int i = 0; i < theStrings.length; i++)
				handleObjectForText(rslt, theStrings[i]);
		}
		else if(object instanceof Collection){
			Iterator it = ((Collection)object).iterator();
			while(it.hasNext())
				handleObjectForText(rslt, it.next());
		}
		else if(object instanceof TextSelection){
			TextSelection selection = (TextSelection)object;
			handleObjectForText(rslt,selection.getText());
		}
		
	}

	private Collection trySimpleTransfer(Clipboard cp){
		Object contents = cp.getContents(TRANSFER);
		if(contents == null)return null;
		if(contents instanceof Collection){
			Collection rslt = (Collection)contents;
			if(rslt.size() == 0)return null;
			return rslt;
		}
		Vector rslt = new Vector();
		rslt.add(contents);
		return rslt;
	}
	
	private void addToCollection(Vector rslt, IFile theFile){
		IEditorDescriptor editor = IDE.getDefaultEditor(theFile);
		if(editor == null)return;
		String editorId = editor.getId();
		String editorLabel = editor.getLabel();
		ObjectType ot = ObjectType.findFromEditorId(editorId);
		if(ot == null){
			ot = new ObjectType();
			ot.setEditorId(editorId);
			ot.setName(editorLabel);
		}
		FileEditorInput fei = new FileEditorInput(theFile);
		Resource resource = Resource.find(ot, fei);
		if(resource == null){
			resource = new Resource();
			resource.setIdentifiers(fei);
			resource.setObjectType(ot);
			resource.setName(theFile.getName());
		}

		rslt.add(resource);
	}
	
	private Collection tryResourceTransfer(Clipboard cp){
		ResourceTransfer transfer = ResourceTransfer.getInstance();
		IResource[] resources = (IResource[])cp.getContents(transfer);
		if((resources == null) || (resources.length == 0))return null;
		Vector rslt = new Vector();
		for(int i = 0; i < resources.length; i++){
			if(resources[i].getType() == IResource.FILE)
				addToCollection(rslt, (IFile)resources[i]);
		}
		if(rslt.size() == 0)return null;
		return rslt;
	}

}
