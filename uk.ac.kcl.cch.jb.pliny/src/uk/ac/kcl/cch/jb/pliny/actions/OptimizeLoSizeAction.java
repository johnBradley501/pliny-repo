package uk.ac.kcl.cch.jb.pliny.actions;

import java.util.Vector;

import org.eclipse.gef.commands.CommandStack;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.resource.ImageDescriptor;

import uk.ac.kcl.cch.jb.pliny.PlinyPlugin;
import uk.ac.kcl.cch.jb.pliny.commands.AlignItemsCommand;
import uk.ac.kcl.cch.jb.pliny.commands.OptimiseSizeCommand;

public class OptimizeLoSizeAction extends Action {
	
	private Vector selectedEditParts;
	private CommandStack commandStack;

	public OptimizeLoSizeAction(Vector selectedEditParts, CommandStack commandStack) {
		this.selectedEditParts = selectedEditParts;
		this.commandStack = commandStack;
		this.setText("Optimise size");
		this.setToolTipText("Optimize Size of the Note Reference");
		this.setImageDescriptor(
				ImageDescriptor.createFromImage(PlinyPlugin.getDefault().getImage("icons/optimiseSize.gif")));
	}
	
	public void run(){
		if(selectedEditParts != null){
			OptimiseSizeCommand command = new OptimiseSizeCommand(selectedEditParts);
			commandStack.execute(command);
		}
	}

}
