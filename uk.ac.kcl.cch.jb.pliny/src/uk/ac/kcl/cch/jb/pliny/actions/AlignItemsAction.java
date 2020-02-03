package uk.ac.kcl.cch.jb.pliny.actions;

import java.util.Vector;

import org.eclipse.gef.commands.CommandStack;
import org.eclipse.jface.action.Action;

import uk.ac.kcl.cch.jb.pliny.PlinyPlugin;
import uk.ac.kcl.cch.jb.pliny.commands.AlignItemsCommand;

public class AlignItemsAction extends Action {
	private Vector items;
	private CommandStack commandStack;
	private boolean vertical;

	public AlignItemsAction(Vector items, CommandStack commandStack, boolean vertical){
		super();
		if(vertical){
			this.setText("Align vertically");
			this.setToolTipText("Align selected objects vertically");
			setImageDescriptor(PlinyPlugin.getImageDescriptor("icons/align-vert.gif"));
		} else {
			this.setText("Align horizontally");
			this.setToolTipText("Align selected objects horizontally");
			setImageDescriptor(PlinyPlugin.getImageDescriptor("icons/align-horiz.gif"));
		}
		this.items = items;
		this.commandStack = commandStack;
		this.vertical = vertical;
	}
	
	public void run(){
		if(items != null && items.size() > 1){
			AlignItemsCommand command = new AlignItemsCommand(vertical, false);
			command.setList(items);
			commandStack.execute(command);
		}
	}
}
