/*******************************************************************************
 * Copyright (c) 2006 John Bradley
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     John Bradley - initial API and implementation
 *******************************************************************************/

package uk.ac.kcl.cch.jb.pliny.rcp.appl;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.GroupMarker;
import org.eclipse.jface.action.ICoolBarManager;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.action.ToolBarContributionItem;
import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.swt.SWT;
import org.eclipse.ui.actions.ActionFactory;
import org.eclipse.ui.actions.ActionFactory.IWorkbenchAction;
import org.eclipse.ui.application.ActionBarAdvisor;
import org.eclipse.ui.application.IActionBarConfigurer;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.IWorkbenchWindow;

public class ApplicationActionBarAdvisor extends ActionBarAdvisor {

    private IWorkbenchAction introAction;
    private IWorkbenchAction helpOpenAction;
    private IWorkbenchAction exitAction;
    private IWorkbenchAction aboutAction;
    
    private IWorkbenchAction undoAction;
    private IWorkbenchAction redoAction;
    private IWorkbenchAction cutAction;
    private IWorkbenchAction copyAction;
    private IWorkbenchAction pasteAction;
    private IWorkbenchAction deleteAction;
    private IWorkbenchAction selectAllAction;
    private IWorkbenchAction preferencesAction;
    
    private IWorkbenchAction forwardAction;
    private IWorkbenchAction backwardAction;
    
	public ApplicationActionBarAdvisor(IActionBarConfigurer configurer) {
		super(configurer);
	}

	protected void makeActions(IWorkbenchWindow window) {
		introAction = ActionFactory.INTRO.create(window);
		register(introAction);
		helpOpenAction = ActionFactory.HELP_CONTENTS.create(window);
		register(helpOpenAction);
        exitAction = ActionFactory.QUIT.create(window);
        register(exitAction);
        aboutAction = ActionFactory.ABOUT.create(window);
        register(aboutAction);
        preferencesAction = ActionFactory.PREFERENCES.create(window);
        
        undoAction = ActionFactory.UNDO.create(window);
        register(undoAction);
        redoAction = ActionFactory.REDO.create(window);
        register(redoAction);
        cutAction = ActionFactory.CUT.create(window);
        register(cutAction);
        copyAction = ActionFactory.COPY.create(window);
        register(copyAction);
        pasteAction = ActionFactory.PASTE.create(window);
        register(pasteAction);
        deleteAction = ActionFactory.DELETE.create(window);
        register(deleteAction);
        selectAllAction = ActionFactory.SELECT_ALL.create(window);
        register(selectAllAction);
        
        forwardAction = ActionFactory.FORWARD_HISTORY.create(window);
        register(forwardAction);
        backwardAction = ActionFactory.BACKWARD_HISTORY.create(window);
        register(backwardAction);
	}

	protected void fillMenuBar(IMenuManager menuBar) {
		
        MenuManager fileMenu = new MenuManager("&File", IWorkbenchActionConstants.M_FILE);
        MenuManager editMenu = new MenuManager("&Edit", IWorkbenchActionConstants.M_EDIT);
		MenuManager helpMenu = new MenuManager("&Help", IWorkbenchActionConstants.M_HELP);

		menuBar.add(fileMenu);
		menuBar.add(editMenu);
        // Add a group marker indicating where action set menus will appear.
        menuBar.add(new GroupMarker(IWorkbenchActionConstants.MB_ADDITIONS));
        menuBar.add(helpMenu);

        // File
        fileMenu.add(new Separator(IWorkbenchActionConstants.FILE_START));
        fileMenu.add(new Separator(IWorkbenchActionConstants.NEW_EXT));
        fileMenu.add(new Separator(IWorkbenchActionConstants.FILE_END));
        
        fileMenu.add(exitAction);
        
        // Edit
        editMenu.add(undoAction);
        editMenu.add(redoAction);
        editMenu.add(new Separator(IWorkbenchActionConstants.CUT));
        editMenu.add(cutAction);
        editMenu.add(copyAction);
        editMenu.add(pasteAction);
        editMenu.add(new Separator(IWorkbenchActionConstants.DELETE));
        editMenu.add(deleteAction);
        editMenu.add(selectAllAction);

		// Help
		helpMenu.add(introAction);
		helpMenu.add(helpOpenAction);
		helpMenu.add(aboutAction);
		helpMenu.add(preferencesAction);
	}
	
    protected void fillCoolBar(ICoolBarManager coolBar) {
        IToolBarManager toolbar = new ToolBarManager(SWT.FLAT | SWT.RIGHT);
        coolBar.add(new ToolBarContributionItem(toolbar, "main"));
        
        toolbar.add(backwardAction);
        toolbar.add(forwardAction);
    }
}
