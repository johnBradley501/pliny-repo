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
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Vector;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
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
import org.eclipse.ui.part.ViewPart;

import uk.ac.kcl.cch.jb.pliny.PlinyPlugin;
import uk.ac.kcl.cch.jb.pliny.utils.IPlinyExporter;
import uk.ac.kcl.cch.jb.pliny.utils.IPlinyExporterDataProvider;
import uk.ac.kcl.cch.jb.pliny.utils.PlinyArchiveExporter;
import uk.ac.kcl.cch.jb.pliny.utils.PlinyExportException;
import uk.ac.kcl.cch.jb.pliny.utils.PlinyExporterFullDataProvider;
import uk.ac.kcl.cch.jb.pliny.utils.PlinyTMExporter;

/**
 * the export action for the Resource Explorer
 * ({@link uk.ac.kcl.cch.jb.pliny.views.ResourceExplorerView}).
 * <p>
 * The action code is responsible for displaying the JFace
 * FileDialog to allow the user to specify a file into which
 * to export data.  It then checks to see if the file already exists
 * and if it does confirms (via a SWT MessageBox) that the
 * user wants to proceed.
 * <p>
 * Then, depending upon the kind of output file the user selected
 * it choose one of two types of
 * {@link uk.ac.kcl.cch.jb.pliny.utils.IPlinyExporter}
 * processes and runs it while setting up a
 * JFace ProgressMonitorDialog to display to the user.
 * <p>
 * If the process fails, it reports the failure to the user
 * via a SWT MessageBox.
 * 
 * @see uk.ac.kcl.cch.jb.pliny.actions.ResourceExplorerImportAction
 * @see uk.ac.kcl.cch.jb.pliny.utils.PlinyTMExporter
 * @see uk.ac.kcl.cch.jb.pliny.utils.PlinyArchiveExporter
 * 
 * @author John Bradley
 *
 */

public class PlinyExportAction extends Action {
	
	private static final String plinyArchiveExtension = "pla";
	private static final String exportManagerExtensionPointID = "uk.ac.kcl.cch.jb.pliny.exportManager";
	private static IPlinyExporter plinyArchiveExporter = null;
	
	private class ManagerData {
		public IPlinyExporter manager;
		public String name;
		public String extension;
	}
	
	private static Hashtable<String, ManagerData> managers = null;
	private static Vector<String> fileExtensions = new Vector<String>();

	public PlinyExportAction() {
		super("Export data ...");
		//this.myView = myView;
		setToolTipText("Export data to a file");
		setImageDescriptor(
				ImageDescriptor.createFromImage(PlinyPlugin.getDefault().getImage("icons/export.gif")));
	}
	
	private void setupExportManagers(){
		managers = new Hashtable<String, ManagerData>();
		
		IExtensionRegistry registry= Platform.getExtensionRegistry();
		IConfigurationElement[] extensions = registry.getConfigurationElementsFor(exportManagerExtensionPointID);
		for (int i= 0; i < extensions.length; i++) {
			IConfigurationElement element = extensions[i];
			try {
				Object item= element.createExecutableExtension("class");
				String fileExtension = element.getAttribute("extension");
				String name = element.getAttribute("name"); 
				if(item != null && (item instanceof IPlinyExporter)  && fileExtension != null && fileExtension.trim().length() > 0){
					fileExtension = fileExtension.trim();
					ManagerData data = new ManagerData();
					data.manager = (IPlinyExporter)item;
					data.extension = "*."+fileExtension;
					if(name != null)data.name = name.trim();
					else data.name = fileExtension+" file";
					data.name = data.name + " (*."+fileExtension+")"; // "TopicMap Files (*.xtm)"
					managers.put(fileExtension, data);
					if(fileExtension.equals(plinyArchiveExtension)) plinyArchiveExporter = data.manager;
					else fileExtensions.add(fileExtension);
				}
			} catch (CoreException e) {
				e.printStackTrace();
			}
		}
		if(plinyArchiveExporter == null)plinyArchiveExporter = new PlinyArchiveExporter();
		
	}
	
	private String[] generateStrings(int type){ // type = 0: filterExtensions; 1: filterNames
		Vector<String> list = new Vector<String>();
		String[] rslt = new String[fileExtensions.size()+2];
		if(type == 0)list.add("*."+plinyArchiveExtension);
		else list.add(managers.get(plinyArchiveExtension).name);
		Iterator<String> it = fileExtensions.iterator();
		while(it.hasNext()){
			if(type == 0)list.add(managers.get(it.next()).extension);
			else list.add(managers.get(it.next()).name);
		}
		if(type == 0)list.add("*.*");
		else list.add("All files (*.*)");
		list.toArray(rslt);	
		return rslt;
	}

	public void run(){
		if(managers == null)setupExportManagers();
		
		Shell parentShell = Display.getDefault().getActiveShell();
		FileDialog dialog = new FileDialog(parentShell, SWT.SAVE);
		dialog.setFilterNames(generateStrings(1));
		dialog.setFilterExtensions(generateStrings(0));
		dialog.setFileName("*."+plinyArchiveExtension);
		dialog.setText("Export data to file");
		
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
		
		IPath filePath = new Path(fileName);
		IPlinyExporter exporter;
		String givenFileExtension = filePath.getFileExtension().toLowerCase();
		ManagerData data = managers.get(givenFileExtension);
		if(data == null)data = managers.get(plinyArchiveExtension);
		
		exporter = data.manager;
		
		IPlinyExporterDataProvider provider = getTheDataProvider();
		exporter.prepareRun(provider, fileName);

		//if(filePath.getFileExtension().equals("xtm"))
		//	exporter = new PlinyTMExporter(provider, fileName);
		//else exporter = new PlinyArchiveExporter(provider, fileName);
		
		final IPlinyExporter de = exporter;
		
		ProgressMonitorDialog monitor = new ProgressMonitorDialog(parentShell);
		
		try {
			monitor.run(false, false, new IRunnableWithProgress() {
				public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
					try {
						de.run(monitor);
					} catch (PlinyExportException e) {
						e.printStackTrace();
						throw new InvocationTargetException(e);
					} finally {
						de.finishRun();
					}
				}
			});
		} catch (InvocationTargetException e) {
			Throwable t = e.getCause();
			MessageBox messageBox = new MessageBox(parentShell, SWT.ICON_ERROR | SWT.OK);
			messageBox.setText("Export Failed");
			messageBox.setMessage("The export failed, cause: "+t.getMessage());
			messageBox.open();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	protected IPlinyExporterDataProvider getTheDataProvider(){
		return new PlinyExporterFullDataProvider();
	}
}
