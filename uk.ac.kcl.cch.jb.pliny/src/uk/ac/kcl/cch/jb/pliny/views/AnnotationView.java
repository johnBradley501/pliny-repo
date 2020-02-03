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

package uk.ac.kcl.cch.jb.pliny.views;

import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.part.EditorPart;
import org.eclipse.ui.part.IPage;
import org.eclipse.ui.part.MessagePage;
import org.eclipse.ui.part.PageBook;
import org.eclipse.ui.part.PageBookView;

import uk.ac.kcl.cch.jb.pliny.IHandlesAnnotations;
import uk.ac.kcl.cch.jb.pliny.IResourceDrivenPart;
import uk.ac.kcl.cch.jb.pliny.model.ObjectType;
import uk.ac.kcl.cch.jb.pliny.model.Plugin;
import uk.ac.kcl.cch.jb.pliny.model.Resource;
import uk.ac.kcl.cch.jb.pliny.model.VirtualResource;

/**
 * This is an PageBookView that manages Pliny's Annnotation View.
 * With PageBookViews, Eclipse provides a linking mechanism between the
 * main editor pane and this view so that is notified each
 * time the user changes to a new Editor on a new Object, and can change
 * its display to synchronize with the editor pane's focus. 
 * Specifially w.r.t. this AnnotationView, for those
 * objects that are Pliny 
 * {@link uk.ac.kcl.cch.jb.pliny.model.Resource Resource}s it can generate its display.
 * As all PageBookViews are supposed to do, it creates one instance of 
 * <code>AnnotationViewPage</code> for
 * each Resource it is given during its operation, and (by means of
 * inherited code) will switch between them as the user switches betwee
 * different Pliny Resources.
 * <p>
 * Much of the code to generate the display and lay out the data
 * is recycled from the example project <code>com.realpersist.gef.schemaeditor</code>.
 * We thank the authors of this project for their example.  The code here
 * does not, however, actually inherit any code from this project, and it need
 * not be included in the build path for this item to work.
 * </p>
 * 
 * @see uk.ac.kcl.cch.jb.pliny.views.AnnotationViewPage
 * @see org.eclipse.ui.part.PageBookView
 * 
 * @author John Bradley
 */

public class AnnotationView extends PageBookView implements IResourceDrivenPart{

	private static final String defaultText =
		"AnnotationView: cannot show annotations for the current page";
	
	public static final String ANNOTATIONVIEW_ID="uk.ac.kcl.cch.jb.pliny.annotationView";
	
	private IWorkbenchPart currentPart = null;
	
	public AnnotationView() {
		super();
	}
	
	public void dispose(){
		super.dispose();
	}
	
	protected IPage createDefaultPage(PageBook book) {
        MessagePage page = new MessagePage();
        initPage(page);
        page.createControl(book);
        page.setMessage(defaultText);
        return page;
	}

	protected PageRec doCreatePage(IWorkbenchPart part) {
		if(!isImportant(part))return null;
		if(part instanceof IHandlesAnnotations) return null;
		EditorPart editorPart = (EditorPart)part;
		//IEditorInput theInput = editorPart.getEditorInput();
		
		// following code no longer needed (when NoteEditor was added) ..jb
		
		//if(theInput instanceof NoteEditorInput){
		//	NoteLucened theNote = ((NoteEditorInput)theInput).getMyNote();
		//	PageRec newPageRec = new PageRec(part, new AnnotationViewPage(theNote, getPageBook(), this));
		//	return newPageRec;
		//}
		IEditorSite editorSite = (IEditorSite)editorPart.getSite();
		String pluginId = editorSite.getPluginId();
		String editorId = editorSite.getId();
		ObjectType objectType = ObjectType.findFromIds(pluginId, editorId);
		/*
		 * Object types are defined (perhaps unnecessarily!) as an editor
		 * from a particular plugin.  When new Resources are created by DnD
		 * from the Hierarchy window (via IResources) there is on plugin
		 * available (even though the preferred editor "is" available.
		 * 
		 * The following code tries to fill in the Plugin Id if it was missing.
		 * If it was missing, the above findFromIds method will find nothing.
		 * The following code looks for an ObjectType from the editorId alone, 
		 * and if it finds it gets the current Plugin object and updates the
		 * ObjectType record with it, so next time it will work properly.
		 */
		if(objectType == null){
			objectType = ObjectType.findFromEditorId(editorId);
			if(objectType != null){
				Plugin myPlugin = Plugin.findFromId(pluginId);
				objectType.setPlugin(myPlugin);
			}
		}
		Resource resource = null;
		if(objectType == null){
			Plugin myPlugin = Plugin.findFromId(pluginId);
			objectType = new ObjectType();
			objectType.setEditorId(editorId);
			objectType.setName(editorSite.getRegisteredName());
			//objectType.reIntroduceMe();
			objectType.setPlugin(myPlugin);
		} else
		   resource = Resource.find(objectType, editorPart.getEditorInput());
		if(resource == null){
			resource = new VirtualResource();
			resource.setIdentifiers(editorPart.getEditorInput());
			resource.setObjectType(objectType);
			resource.setName(editorPart.getPartName());
		}
		//IEditorInput myEditorInput = Resource.idString2EditorInput(resource.getIdentifier());
		PageRec newPageRec = new PageRec(part, new AnnotationViewPage(resource, getPageBook(), this));
		return newPageRec;
	}
	
	public void partActivated(IWorkbenchPart part) {
		super.partActivated(part);
		currentPart = part;
		if(getCurrentPage() instanceof AnnotationViewPage){
			AnnotationViewPage myPage = (AnnotationViewPage)getCurrentPage();
			myPage.updateCommandStackActions();
			myPage.updateUndoActions();
		}
	}
	  
	public void partDeactivated(IWorkbenchPart part){
		if(currentPart == part)currentPart = null;
	}

	protected void doDestroyPage(IWorkbenchPart part, PageRec pageRecord) {
		pageRecord.dispose();
		if(currentPart == part)currentPart = null;
	}

	/*
	public void partBroughtToTop(IWorkbenchPart part){
		IPage newTopPage = getCurrentPage();
		if(!(newTopPage instanceof AnnotationViewerPage))return;
		//AnnotationViewerPage newTopAVPage = (AnnotationViewerPage)newTopPage;
		//changeActionCommandStack(newTopAVPage.getCommandStack());
		this.deleteAction.update();
		this.cutAction.update();
		this.copyAction.update();
		this.pasteAction.update();
	}
	*/

	protected IWorkbenchPart getBootstrapPart() {
        IWorkbenchPage page = getSite().getPage();
        if (page != null)
            return page.getActiveEditor();

        return null;
	}

	protected boolean isImportant(IWorkbenchPart part) {
		if(!(part instanceof EditorPart)) return false;
		EditorPart editorPart = (EditorPart)part;
        if(editorPart.getEditorInput().getPersistable() == null)return false;
		return true;
	}
	
	public Object getAdapter(Class adapter){
		Object rslt = null;
		if(!(getCurrentPage() instanceof AnnotationViewPage))return super.getAdapter(adapter);
		AnnotationViewPage myCurrentPage = (AnnotationViewPage)getCurrentPage();
		if(myCurrentPage != null)rslt = myCurrentPage.getAdapter(adapter);
		if(rslt != null)return rslt;
		return super.getAdapter(adapter);
	}

	public Resource getMyResource() {
		IPage thePage = this.getCurrentPage();
		if(thePage == null)return null;
		if(!(thePage instanceof AnnotationViewPage))return null;
		return ((AnnotationViewPage)thePage).getMyResource();
		
	}
	
	public void refreshPart(IWorkbenchPart part){
		if(currentPart != part)return;
		partClosed(part);
		partActivated(part);
	}
}
