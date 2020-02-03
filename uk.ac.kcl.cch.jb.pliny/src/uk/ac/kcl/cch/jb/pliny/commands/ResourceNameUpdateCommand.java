package uk.ac.kcl.cch.jb.pliny.commands;

import org.eclipse.gef.commands.Command;

import uk.ac.kcl.cch.jb.pliny.model.Resource;

public class ResourceNameUpdateCommand extends Command {

	private String oldName, newName;
    private Resource resource;
    
    public ResourceNameUpdateCommand(Resource resource, String newName){
    	super("change Resource name");
    	this.newName = newName;
    	this.resource = resource;
    	this.oldName = resource.getName();
    }

	public void execute(){
		resource.setName(newName);
	}
	
	public void updo(){
		resource.setName(oldName);
	}

}
