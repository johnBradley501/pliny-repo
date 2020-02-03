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

package uk.ac.kcl.cch.jb.pliny.figures;

import org.eclipse.draw2d.ActionEvent;
import org.eclipse.draw2d.ActionListener;
import org.eclipse.draw2d.BorderLayout;
import org.eclipse.draw2d.Clickable;
import org.eclipse.draw2d.Figure;
import org.eclipse.draw2d.Label;
import org.eclipse.draw2d.MouseEvent;
import org.eclipse.draw2d.MouseListener;
import org.eclipse.draw2d.MouseMotionListener;
import org.eclipse.draw2d.Panel;
import org.eclipse.draw2d.ToolbarLayout;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;

import uk.ac.kcl.cch.jb.pliny.PlinyPlugin;
import uk.ac.kcl.cch.jb.pliny.parts.IButtonHolderPart;
import uk.ac.kcl.cch.rdb2java.dynData.BaseObject;
/**
 * provides a draw2d Figure that displays the title area of a annotation/reference
 * area's Reference Object.
 * 
 * @author John Bradley
 *
 */

public class TopPanel extends Figure {

	protected IButtonHolderPart myPart;
	private Label titleLabel;
	private Label openerLabel;
	protected boolean isOpen, canMinimize;

	private static BaseObject currentObject = null;
	
	protected String myName;
	protected int maxNameLength = 0;
	protected int surrPageNo;
	protected Color background, foreground;
	protected Clickable[] otherButtons = new Clickable[0];
	
	protected Image objectTypeIcon = null;
	private Label theIdentifierIconLabel = null;
	private Label openEditorLabel = null;

	/**
	 * A listener to see when the mouse is placed over the top-left icon.
	 * This is used to support drag and drop of icons connected to a
	 * LinkableObject representation on the screen.
	 * <p>In its original implementation it was also supposed
	 * to allow the code to make this icon clickable (and
	 * act as an "open this item" button -- but it doesn't seem to do this!
	 * 
	 * @author John Bradley
	 *
	 */
	
	public static class OpenButtonMouseMotionListener implements MouseMotionListener{

		private BaseObject myObject;
		
		public OpenButtonMouseMotionListener(BaseObject myObject){
			this.myObject = myObject;
		}
		
		public void mouseDragged(MouseEvent me) {
		}

		public void mouseEntered(MouseEvent me) {
			currentObject = myObject;
		}

		public void mouseExited(MouseEvent me) {
			currentObject = null;
			
		}

		public void mouseHover(MouseEvent me) {
		}

		public void mouseMoved(MouseEvent me) {
		}
		
	}
	
	public TopPanel(){
		myPart = null;
	}
	
	/**
	 * creates an instance of this object, and sets up the various pieces
	 * of information needed to display it.
	 * 
	 * @param myName the text to display as the "name field" in the title area.
	 * @param surrPageNo the page number to display in the title area.  If
	 * no page number is wanted, set this to zero.
	 * @param canMinimize controls whether the minimize/expand button should
	 * appear in the area.
	 * @param isOpen controls whether the minimize or expand icon initially appears.
	 * @param background provides the background colour to be used.
	 * @param foreground provides the foreground colour to be used.
	 * @param thePart the GEF Part that holds this TopPanel object.
	 * @param topLeftIcon the SWT Image object that is to be used as the
	 * top left icon.
	 * @param otherButtons an array of other buttons that should be displayed towards
	 * the right end of the title area.
	 */
	public TopPanel(String myName,
			int surrPageNo,
			boolean canMinimize,
			boolean isOpen, 
			Color background, Color foreground, 
			IButtonHolderPart thePart,
			Image topLeftIcon,
			Clickable[] otherButtons){
		this.myPart = thePart;
		this.surrPageNo = surrPageNo;
		this.isOpen = isOpen;
		this.myName = myName;
		this.background = background;
		this.foreground = foreground;
		this.objectTypeIcon = topLeftIcon;
		if(objectTypeIcon == null)
			objectTypeIcon = PlinyPlugin.getDefault().getImage("icons/openToEditor.gif");
		this.otherButtons = otherButtons;
		this.canMinimize = canMinimize;
		doBuild();
	}
	
	
	protected Label buildIdentifierIcon(){
		openEditorLabel = new Label(objectTypeIcon);
		if(myPart != null)
           openEditorLabel.addMouseMotionListener(new OpenButtonMouseMotionListener(myPart.getHeldObject()));
        return openEditorLabel;
	}
	
	public void addMyPart(IButtonHolderPart myPart){
		this.myPart = myPart;
		if(myPart != null)
	          openEditorLabel.addMouseMotionListener(new OpenButtonMouseMotionListener(myPart.getHeldObject()));
	}
	
	//protected void doEditorOpen(){
	//	myPart.openFullEditor();
	//}
	
	//private void setToolTipText(){
	//	String toolTipText = myName;
	//	if(surrPageNo > 0)
	//		toolTipText += " (pg "+surrPageNo+")";
	//	titleLabel.setToolTip(new Label(toolTipText));
	//}
	
	/**
	 * provides the text to appear as the tooltip for the textual label
	 * part of this figure. Overrides the inherited function so that
	 * the tooltip is only attached to the title area, and not the bar
	 * overall.
	 * 
	 * @param text the new tooltip text.
	 */
	public void setToolTipText(String text){
		titleLabel.setToolTip(new Label(text));
	}
	
	protected void doBuild(){
		setLayoutManager(new BorderLayout());
		setBackgroundColor(background);
		setForegroundColor(foreground);
		setOpaque(true);
		
		Panel buttonArea = new Panel();
		buttonArea.setLayoutManager(new ToolbarLayout(true));

		//final IButtonHolderPart myFinalPart = myPart;
		//if(myFinalPart == null){
		//	System.out.println("myFinalPart == null");
		//}
		
		titleLabel = new Label();
		if(maxNameLength > 0 && myName.length() > maxNameLength)
			titleLabel.setText(myName.substring(0,maxNameLength+1)+"...");
		else titleLabel.setText(myName);
		titleLabel.setOpaque(true);
		//String toolTipText = myName;
		//titleLabel.setToolTip(new Label(myName));
		//setToolTipText();
		
		/*
		Image openIcon = PlinyPlugin.getDefault().getImage("icons/openToEditor.gif");

		Clickable openInEditorButton = new Clickable(
				new Label(openIcon));
        openInEditorButton.addActionListener(new ActionListener(){
        	public void actionPerformed(ActionEvent e){
        		doEditorOpen();
        	}
        });
        */
        
        Clickable openToggleButton = null;
		
        if(canMinimize){
			openerLabel = new Label();
			String iconID = isOpen?"icons/closeNote.gif":"icons/openNote.gif";
			openerLabel.setIcon(PlinyPlugin.getDefault().getImage(iconID));
			openToggleButton = new Clickable(openerLabel);
			
			openToggleButton.addActionListener(new ActionListener(){
				public void actionPerformed(ActionEvent e){
					myPart.switchOpenStatus();
				}
			});
	     }
        
        //buttonArea.add(openInEditorButton);

		for(int i = 0; i < otherButtons.length; i++){
			if(otherButtons[i] != null) buttonArea.add(otherButtons[i]);
		}
		
		if(canMinimize)buttonArea.add(openToggleButton);
		theIdentifierIconLabel = buildIdentifierIcon();
		add(theIdentifierIconLabel, BorderLayout.LEFT);
		add(titleLabel, BorderLayout.CENTER);
		add(buttonArea, BorderLayout.RIGHT);
	}
	
	public Label getTheIdentifierIconLabel() {
		return theIdentifierIconLabel;
	}
	
	/**
	 * changes the text that appears in the title area to the new specified
	 * text.
	 * 
	 * @param name the new text to appear as the title.
	 */
	
	public void setName(String name){
		if(myName.equals(name))return;
		myName = name;
		if(maxNameLength > 0 && myName.length() > maxNameLength)
			titleLabel.setText(myName.substring(0,maxNameLength+1)+"...");
		else titleLabel.setText(myName);
		//titleLabel.setToolTip(new Label(name));
		//setToolTipText();

		this.repaint();
	}
	/**
	 * returns the draw2d Label component of this figure that contains the title.
	 */
	
	public Label getTitleLabel(){
		return titleLabel;
	}
	
	/**
	 * handles the update of the title bar when the <code>isOpen</code> status
	 * is changed by the user.
	 * 
	 * @param isOpen the new isOpen status.
	 */
	public void setIsOpen(boolean isOpen){
		if(this.isOpen == isOpen)return;
		this.isOpen = isOpen;
		String iconID = isOpen?"icons/closeNote.gif":"icons/openNote.gif";
		openerLabel.setIcon(PlinyPlugin.getDefault().getImage(iconID));
		this.repaint();
	}
	
	public static BaseObject getCurrentObject(){
		return currentObject;
	}
	
	public static void clearCurrentObject(){
		currentObject = null;
	}
	
	//public static void setCurrentObject(BaseObject thisObject){
	//	currentObject = thisObject;
	//}
	
	/**
	 * updates the display of this object to use new colours.
	 * 
	 * @param background the new background colour to use
	 * @param foreground the new foreground colour to use
	 */
	
	public void setColours(Color background, Color foreground){
		this.background = background;
		this.foreground = foreground;
		setBackgroundColor(background);
		setForegroundColor(foreground);
		this.repaint();
	}

}
