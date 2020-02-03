package uk.ac.kcl.cch.jb.pliny.actions;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;

import uk.ac.kcl.cch.jb.pliny.PlinyPlugin;

public class CreateMinimiseStatus extends Action implements IWorkbenchWindowActionDelegate {
	
	private static CreateMinimiseStatus action = null;
	
    private CreateMinimiseStatus(){
    	super("", AS_CHECK_BOX);
		this.setImageDescriptor(PlinyPlugin.getImageDescriptor("icons/openNote.gif"));
    }
    
    public static CreateMinimiseStatus instance(){
    	if(action == null)action = new CreateMinimiseStatus();
    	return action;
    }
    
    public boolean makeMin(){
    	return this.isChecked();
    }
    
    public void run(){
    	if(isChecked()){
    		setImageDescriptor(PlinyPlugin.getImageDescriptor("icons/closeNote.gif"));
    		this.setToolTipText("Click to change to fullsize creation");
    	} else {
    		setImageDescriptor(PlinyPlugin.getImageDescriptor("icons/openNote.gif"));
    		this.setToolTipText("Click to change to minimised creation");
    	}
    	
    }

	@Override
	public void run(IAction action) {
		run();
	}

	@Override
	public void selectionChanged(IAction action, ISelection selection) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void dispose() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void init(IWorkbenchWindow window) {
		// TODO Auto-generated method stub
		
	}
}
