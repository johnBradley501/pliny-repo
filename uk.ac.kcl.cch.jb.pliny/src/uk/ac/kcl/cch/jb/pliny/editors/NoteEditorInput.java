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

package uk.ac.kcl.cch.jb.pliny.editors;

import org.eclipse.core.resources.IStorage;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.IPersistableElement;
import org.eclipse.ui.IStorageEditorInput;

import uk.ac.kcl.cch.jb.pliny.PlinyPlugin;
import uk.ac.kcl.cch.jb.pliny.model.Note;
import uk.ac.kcl.cch.jb.pliny.model.NoteLucened;

/**
 * The <code>EditorInput</code> object for the {@link NoteEditor}.  The
 * object holds the 
 * {@link uk.ac.kcl.cch.jb.pliny.model.NoteLucened NoteLucenced} 
 * resource that it refers to.
 * <p>
 * By making the object <code>IPersistableElement</code> and <code>IAdaptable</code> it is
 * made possible for open editors to persist between Eclipse/Pliny
 * sessions.
 * 
 * @see uk.ac.kcl.cch.jb.pliny.model.Resource#idString2EditorInput
 * 
 * @author John Bradley
 *
 */

public class NoteEditorInput implements IStorageEditorInput,
		IPersistableElement, IAdaptable {

	public static String NOTE_EDIT_INPUT_FACTORY_ID=
		"uk.ac.kcl.cch.jb.pliny.noteEditorInputFactory";
	public static String RESOURCE_KEY_TAG = "resourceKey";

	private NoteLucened myNote;
	
	public NoteLucened getMyNote(){return myNote;}
	
	public NoteEditorInput(Note myNote) {
		this.myNote = (NoteLucened)myNote;
	}

	public IStorage getStorage() throws CoreException {
		//return null;
		return EmptyIStorage.getInstance(); // seems to be needed during generation of contextual menu in 3.5
	}

	public boolean exists() {
		return myNote != null;
	}

	public ImageDescriptor getImageDescriptor() {
		//return PlinyPlugin.imageDescriptorFromPlugin(
		//   PlinyPlugin.getDefault().getDescriptor().getUniqueIdentifier(),
		//   "icons/noteIcon.gif");
		return PlinyPlugin.getImageDescriptor("icons/noteIcon.gif");
	}

	public String getName() {
		if(myNote == null)return null;
		return myNote.getName();
	}

	public IPersistableElement getPersistable() {
		return this;
	}

	public String getToolTipText() {
		return getName();
	}

	public Object getAdapter(Class adapter) {
        if(adapter.equals(NoteLucened.class))return myNote;
		return null;
	}

	public String getFactoryId() {
		return NOTE_EDIT_INPUT_FACTORY_ID;
	}

	public void saveState(IMemento memento) {
		if((memento == null) || (myNote == null))return;
		memento.putInteger(RESOURCE_KEY_TAG, myNote.getALID());
	}
	
	public boolean equals(Object obj){
		if(!(obj instanceof NoteEditorInput))return false;
		NoteEditorInput candidate = (NoteEditorInput)obj;
		return candidate.myNote.getALID() == myNote.getALID();
	}

}
