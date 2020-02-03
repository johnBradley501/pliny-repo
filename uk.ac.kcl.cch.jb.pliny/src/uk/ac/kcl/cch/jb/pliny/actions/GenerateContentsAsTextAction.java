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

package uk.ac.kcl.cch.jb.pliny.actions;

import java.io.File;
import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;

import uk.ac.kcl.cch.jb.pliny.IResourceDrivenPart;
import uk.ac.kcl.cch.jb.pliny.PlinyPlugin;
import uk.ac.kcl.cch.jb.pliny.model.Resource;

/**
 * This action supports the export of the reference area associated with
 * a resource associated with a display part (view or editor).  The action
 * must track the currently active display part (and hence the currently
 * active resource).  There is an example of how this is done in 
 * {@link org.eclipse.gef.EditDomain.NoteEditorActionBarContributor NoteEditorActionBarContributor}.
 * 
 * @author John Bradley
 *
 */
public class GenerateContentsAsTextAction extends Action {

	//private IResourceDrivenPart currentPart = null;
	protected Resource headResource = null;
	
	public GenerateContentsAsTextAction(){
		super();
		setText("Generate text file ...");
		setToolTipText("Generate text file from contents");
		setImageDescriptor(
				ImageDescriptor.createFromImage(PlinyPlugin.getDefault().getImage("icons/saveFile.gif")));
	}
	
	public GenerateContentsAsTextAction(Resource theResource){
		this();
		headResource = theResource;
	}
	
	/**
	 * If this operates from an ActionBarContributor, this method must be invoked (probably for an editor by an
	 * <code>ActionBarContributor</code> each time the editor instance
	 * is changed.
	 * 
	 * @param currentPart
	 */
	
	public void setResource(Resource currentResource){
		headResource = currentResource;
	}
	
	//public void setResourceDrivenPart(IResourceDrivenPart currentPart){
	//	this.currentPart = currentPart;
	//}
	
	protected ITextFileGenerator getMyTextGenerator(Resource headResource, String fileName){
		return new TextFileGenerator(headResource, fileName);
	}
	
	public void run(){
		if(headResource == null)return;
		
		//Resource headResource = currentPart.getMyResource();
		
		final Shell parentShell = Display.getDefault().getActiveShell();
		FileDialog dialog = new FileDialog(parentShell, SWT.SAVE);
		dialog.setFilterNames(new String[]{
				"HTML files (*.html)",
				"All files (*.*)"
		});
		dialog.setFilterExtensions(new String[]{"*.html", "*.*"});
		dialog.setFileName(headResource.getName()+".html");
		dialog.setText("Write contents to file");
		
		String fileName = dialog.open();
		if(fileName == null)return;
		
		File myFile = new File(fileName);
		if(myFile.exists()){
			boolean replace = MessageDialog.openQuestion(
					parentShell,
					"Confirm replacing a file",
					"File '"+fileName+"' already exist. "+
					"Do you wish replace it with a new version?");
			if(!replace)return;
			myFile.delete();
		}
		
		final ITextFileGenerator generator = getMyTextGenerator(headResource, fileName);
		if(generator.getOptions(parentShell)){
			ProgressMonitorDialog monitor = new ProgressMonitorDialog(parentShell);
			try {
				monitor.run(false, false, new IRunnableWithProgress() {
					public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
						try {
								generator.run(monitor);
						} catch (Exception e) {
							e.printStackTrace();
							throw new InvocationTargetException(e);
						}
					}
				});
			} catch (InvocationTargetException e) {
				Throwable t = e.getCause();
				MessageBox messageBox = new MessageBox(parentShell, SWT.ICON_ERROR | SWT.OK);
				messageBox.setText("Text File generation Failed");
				messageBox.setMessage("The generation of the textFile failed, cause: "+t.getMessage());
				messageBox.open();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
}
