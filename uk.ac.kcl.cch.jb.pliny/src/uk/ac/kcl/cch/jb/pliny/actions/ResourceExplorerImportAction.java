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

import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;

import uk.ac.kcl.cch.jb.pliny.PlinyPlugin;
import uk.ac.kcl.cch.jb.pliny.utils.IPlinyImporter;
import uk.ac.kcl.cch.jb.pliny.utils.PlinyArchiveImporter;
import uk.ac.kcl.cch.jb.pliny.utils.PlinyImportException;
import uk.ac.kcl.cch.jb.pliny.utils.PlinyTxtImporter;
import uk.ac.kcl.cch.jb.pliny.views.ResourceExplorerView;
import uk.ac.kcl.cch.jb.pliny.views.utils.ResourceExplorerContentProvider;

/**
 * the import action for the Resource Explorer
 * ({@link uk.ac.kcl.cch.jb.pliny.views.ResourceExplorerView}).
 * <p>
 * The action code is responsible for displaying the JFace
 * FileDialog to allow the user to specify a file from which
 * it should import data.
 * <p>
 * Then, depending upon the kind of output file the user selected
 * it choose one of two types of
 * {@link uk.ac.kcl.cch.jb.pliny.utils.IPlinyImporter}
 * processes and runs it while setting up a
 * JFace ProgressMonitorDialog to display to the user.
 * <p>
 * If the process fails, it reports the failure to the user
 * via a SWT MessageBox.
 * 
 * @see uk.ac.kcl.cch.jb.pliny.actions.PlinyExportAction
 * @see uk.ac.kcl.cch.jb.pliny.utils.PlinyTxtImporter
 * @see uk.ac.kcl.cch.jb.pliny.utils.PlinyArchiveImporter
 * 
 * @author John Bradley
 *
 */

public class ResourceExplorerImportAction extends Action {
	
	private static final String extension = "pla";
	private ResourceExplorerView resourceExplorerView;

	public ResourceExplorerImportAction(ResourceExplorerView resourceExplorerView) {
		super("Import from file");
		setToolTipText("Import data from a file");
		setImageDescriptor(
				ImageDescriptor.createFromImage(PlinyPlugin.getDefault().getImage("icons/import.gif")));
		this.resourceExplorerView = resourceExplorerView;
	}
	
	public void run(){
		final Shell parentShell = Display.getDefault().getActiveShell();
		FileDialog dialog = new FileDialog(parentShell, SWT.OPEN);
		dialog.setFilterNames(new String[]{
				"Notes for Pliny in Text Files (*.txt)",
				"Pliny Export Files (*."+extension+")",
				"All files (*.*)"
		});
		dialog.setFilterExtensions(new String[]{"*.txt", "*."+extension, "*.*"});
		dialog.setFileName("pliny.txt");
		dialog.setText("File containing input data");
		
		String fileName = dialog.open();
		if(fileName == null)return;
		
		IPath filePath = new Path(fileName);
		IPlinyImporter importer;
		if(filePath.getFileExtension().equals("txt"))
		   importer = new PlinyTxtImporter(fileName);
		else importer = new PlinyArchiveImporter(fileName);
		
		final IPlinyImporter di = importer;
		
		ProgressMonitorDialog monitor = new ProgressMonitorDialog(parentShell);
		
		ResourceExplorerContentProvider.setAutoRefreshEnabled(false);
		try {
			monitor.run(false, false, new IRunnableWithProgress() {
				public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
					try {
						if(di.getOptions(parentShell))
						   di.run(monitor);
					} catch (PlinyImportException e) {
						e.printStackTrace();
						throw new InvocationTargetException(e);
					}
				}
			});
		} catch (InvocationTargetException e) {
			Throwable t = e.getCause();
			MessageBox messageBox = new MessageBox(parentShell, SWT.ICON_ERROR | SWT.OK);
			messageBox.setText("Import Failed");
			messageBox.setMessage("The import failed, cause: "+t.getMessage());
			messageBox.open();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			ResourceExplorerContentProvider.setAutoRefreshEnabled(true);
			resourceExplorerView.refreshTreeViewers();
		}
	}
}
