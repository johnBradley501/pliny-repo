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
import org.eclipse.draw2d.ColorConstants;
import org.eclipse.draw2d.ConnectionLayer;
import org.eclipse.draw2d.Figure;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.Label;
import org.eclipse.draw2d.LineBorder;
import org.eclipse.draw2d.MouseEvent;
import org.eclipse.draw2d.MouseMotionListener;
import org.eclipse.draw2d.Panel;
import org.eclipse.draw2d.StackLayout;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;

import uk.ac.kcl.cch.jb.pliny.PlinyPlugin;
import uk.ac.kcl.cch.jb.pliny.model.LOType;
import uk.ac.kcl.cch.jb.pliny.model.LinkableObject;
// import uk.ac.kcl.cch.jb.pliny.model.ReferencerManager;
import uk.ac.kcl.cch.jb.pliny.model.Resource;
import uk.ac.kcl.cch.jb.pliny.parts.IButtonHolderPart;
// import uk.ac.kcl.cch.jb.pliny.parts.IHasReferencerManager;
import uk.ac.kcl.cch.jb.pliny.parts.LinkableObjectPart;

/**
 * Creates the draw2d Figure that represents the MVC View for 
 * {@link uk.ac.kcl.cch.jb.pliny.model.LinkableObject}s
 * in a GEF generated reference/annotation area -- what the Pliny help pages
 * calls the <i>Reference Object</i>.
 * <p>
 * The visual display has two areas -- a top title-bar like area which is
 * implemented in the {@link TopPanel} area,.and a larger content area below it.
 * Generally, the content area will be filled one of:
 * <ul>
 * <li>a {@link MapContentFigure}
 * (if the reference object is showing the surrogate's reference area),
 * <li>a {@link TextContentFigure} if it is showing the content text of a Note, or
 * <li>a figure (often a thumbnail image) provided by the surrogate that
 * represents the surrogate itself pictorially.
 * </ul>
 * <p>
 * This figure places a connectionlayer that GEF can use for placing connection
 * objects between instances of this figure.  This may be an artifact of an
 * earlier design of this Figure and perhaps is not needed anymore.
 * 
 * @author John Bradley
 *
 */

public class LinkableObjectFigure extends Figure {
	
	public static int NO_CONTENTS = 1;
	public static int TEXT_CONTENTS = 2;
	public static int AREA_CONTENTS = 3;
	public static int TEXT_CONTENTS_ONLY = 4;
	
	public static int MINIMIZED_HEIGHT = 18;
	
	private static Resource referrerResource = null;
	private static Resource referrerOwnerResource = null;
	
	private IFigure baseContents;
	private IFigure mapContents;
	//private IFigure standardContents = null;
	protected TopPanel topPanel = null;
	private Color myForegroundColor = ColorConstants.blue;
	private Color myBackgroundColor = ColorConstants.white;
	private ConnectionLayer myConnectionLayer;
	private LinkableObjectPart loPart;
	private int contentType = AREA_CONTENTS;
	private boolean canSwitchOnMap;
	protected String myName = "";
	protected boolean isOpen = true;
	// private IHasReferencerManager rManagerPart;
	private Image topLeftIcon = null;
	private Clickable otherButtons[] = new Clickable[2];

	private Clickable switchMapButton = null;
	private Label switchMapLabel = null;
	private Clickable referencerButton = null;

	private Label referencerLabel;
    private boolean referencerOn = false;
    private int surrPageNo = 0;
    
    private class ReferencerButtonMouseMotionListener implements MouseMotionListener{
       private Resource myResource = null;

	public ReferencerButtonMouseMotionListener(Resource myResource) {
		this.myResource = myResource;
	}

	@Override
	public void mouseDragged(MouseEvent me) {
		// ignore this
		
	}

	@Override
	public void mouseEntered(MouseEvent me) {
		referrerResource = myResource;
		referrerOwnerResource = loPart.getLinkableObject().getDisplayedIn();
		// System.out.println("Entering referrer icon: "+myResource);
	}

	@Override
	public void mouseExited(MouseEvent me) {
		referrerResource = null;
		referrerOwnerResource = null;
		// System.out.println("Exiting refferer icon: "+myResource);
	}

	@Override
	public void mouseHover(MouseEvent me) {
		// ignore this
		
	}

	@Override
	public void mouseMoved(MouseEvent me) {
		// TODO Auto-generated method stub
		
	}
    }
	
	public LinkableObjectFigure(LinkableObjectPart myPart,
			IFigure baseContents,
			int contentType){
        super();
		this.contentType = contentType;
		this.loPart = myPart;

		LinkableObject myLinkableObject = myPart.getLinkableObject();
        Resource mySurrogate = myLinkableObject.getSurrogateFor();
        myName = mySurrogate.getName();
		this.myBackgroundColor = myLinkableObject.getLoType().getTitleBackColour();
		this.myForegroundColor = myLinkableObject.getLoType().getTitleForeColour();
		surrPageNo = myLinkableObject.getSurrPageNo();
		if(mySurrogate.getObjectType() != null){
			this.topLeftIcon = mySurrogate.getObjectType().getIconImage();
		}
		this.isOpen = myLinkableObject.getIsOpen();
		this.canSwitchOnMap = mySurrogate.canDisplayMap();
        
        // this.rManagerPart = null;

        // if(myPart.getParent() instanceof IHasReferencerManager){
        	// this.rManagerPart = (IHasReferencerManager)myPart.getParent();
        	buildReferencerButton();
        // }
		if(this.canSwitchOnMap)buildSwitchMapButton();
		
		buildMyLayout();
		
		mapContents = makePanelContentArea();
		if(baseContents != null)this.baseContents = baseContents;
		else this.baseContents = mapContents;

		otherButtons[0] = switchMapButton;
		otherButtons[1] = referencerButton;
		//buildContentArea();
		buildTheFigure();
	}
	
	private void buildMyLayout(){
		BorderLayout layout = new BorderLayout();
        setLayoutManager(layout);
		setBorder(new LineBorder(ColorConstants.black,1));
		
	}
	
	public ConnectionLayer getMyConnectionLayer(){
		return myConnectionLayer;
	}
	
	private String buildToolTipText(){
		String toolTipText = myName;
		if(surrPageNo > 0)
			toolTipText += " (pg "+surrPageNo+")";
		LinkableObject lo = loPart.getLinkableObject();
		if(lo.getLoType() != null && !lo.getLoType().getName().equals(""))
			toolTipText += " ["+lo.getLoType().getName()+"]";
		return toolTipText;
	}
	
	protected void buildTheFigure(){
		removeAll();
		if(topPanel == null)
		   topPanel = new TopPanel(
				myName, surrPageNo,
				true, // canOpen
				isOpen,
				myBackgroundColor,
				myForegroundColor,
				loPart, topLeftIcon,
				otherButtons);
		topPanel.setToolTipText(buildToolTipText());
		LinkableObject lo = loPart.getLinkableObject();
		if(lo.getShowingMap())
			setBackgroundColor(ColorConstants.white);
		else
			setBackgroundColor(lo.getLoType().getBodyBackColour());
		//add(topPanel);
		add(topPanel, BorderLayout.TOP);
		//if(isOpen && (contentType != NO_CONTENTS))add(contents);
		IFigure myContents = baseContents;
		if(contentType == AREA_CONTENTS) myContents = mapContents;
		if(isOpen && (contentType != NO_CONTENTS))add(myContents, BorderLayout.CENTER);
	    setOpaque(true);
	}
	
	private Panel makePanelContentArea(){
		Panel rslt = new Panel();
		rslt.setLayoutManager(new StackLayout());
		rslt.setOpaque(true);
		return rslt;
	}
	
	public LinkableObjectFigure(){
		super();
		//buildContentArea();
		buildTheFigure();
	}

	public IFigure getContentsFigure(){
		if(contentType == NO_CONTENTS)return null;
		if(contentType == AREA_CONTENTS)return mapContents;
		return baseContents;
	}
	
	public Label getTitleLabel(){
		return topPanel.getTitleLabel();
	}
	
	public void setName(String name){
		if(name.equals(myName))return;
		topPanel.setName(name);
		myName = name; // JB moved to get tooltip text to reflect name name too.
		topPanel.setToolTipText(buildToolTipText());
		//myName = name;
	}
	
	public String getName(){
		return myName;
	}

	/* private class ReferencerButtonActionListener implements ActionListener {
		
		private LinkableObjectPart myPart;
		
		public ReferencerButtonActionListener(LinkableObjectPart myPart){
			this.myPart = myPart;
		}
		
		public void actionPerformed(ActionEvent e){
			referencerOn = !referencerOn;
			ReferencerManager myManager = rManagerPart.getReferencerManager();
			if(referencerOn)myManager.setReferencerOwnerPart(myPart);
			else myManager.clearReferencerOwnerPart();
		}

	} */
	
	private void buildReferencerButton() {
		referencerLabel = new Label();
		String iconID = referencerOn?"icons/referencersOff.gif":"icons/referencersOn.gif";
		referencerLabel.setIcon(PlinyPlugin.getDefault().getImage(iconID));
		referencerButton = new Clickable(referencerLabel);
		
		//referencerButton.addActionListener(new ReferencerButtonActionListener(loPart));
		referencerButton.addMouseMotionListener(new ReferencerButtonMouseMotionListener(loPart.getLinkableObject().getSurrogateFor()));
	}
	
	public void setReferencerIcon(boolean referencerOn){
		//if(this.referencerOn == referencerOn)return;
		String iconID = referencerOn?"icons/referencersOff.gif":"icons/referencersOn.gif";
		referencerLabel.setIcon(PlinyPlugin.getDefault().getImage(iconID));
		referencerLabel.repaint();
	}
	
	public void setIsOpen(boolean isOpen){
		if(this.isOpen == isOpen)return;
		this.isOpen = isOpen;
		topPanel.setIsOpen(this.isOpen);
		buildTheFigure();
		this.repaint();
	}

	private void buildSwitchMapButton() {
		final IButtonHolderPart myFinalPart = loPart;
    	String iconID = myFinalPart.getMapStatus()?"icons/showContents.gif":"icons/showMap.gif";
    	if(contentType==TEXT_CONTENTS_ONLY)iconID="icons/noShowMap.gif";
    	switchMapLabel = new Label(PlinyPlugin.getDefault().getImage(iconID));
		switchMapButton = new Clickable(switchMapLabel);
		if(contentType != TEXT_CONTENTS_ONLY)
		  switchMapButton.addActionListener(new ActionListener(){
        	public void actionPerformed(ActionEvent e){
        		myFinalPart.switchMapStatus();
        	}
          });
	}
	
	public void setMapIcon(){
		if(switchMapButton == null)return;
    	String iconID = loPart.getMapStatus()?"icons/showContents.gif":"icons/showMap.gif";
    	if(contentType==TEXT_CONTENTS_ONLY)iconID="icons/noShowMap.gif";
     	switchMapLabel.setIcon(PlinyPlugin.getDefault().getImage(iconID));
    	switchMapButton.repaint();
	}

	public void setContentType(int contentType){
		if(this.contentType == contentType)return;
		this.contentType = contentType;
		//buildContentArea();
		buildTheFigure();
	}
	public void setColoursFromType(LOType type){
		if(type == null)return;
		this.myBackgroundColor = type.getTitleBackColour();
		this.myForegroundColor = type.getTitleForeColour();
		LinkableObject lo = loPart.getLinkableObject();
		if(lo.getShowingMap())
			setBackgroundColor(ColorConstants.white);
		else
			setBackgroundColor(type.getBodyBackColour());
		
		topPanel.setColours(myBackgroundColor, myForegroundColor);
		topPanel.setToolTipText(buildToolTipText());
	}
	
	public static Resource getReferrerResource(){
		return referrerResource;
	}
	
	public static Resource getReferrerOwnerResource(){
		return referrerOwnerResource;
	}
	
	public static void clearReferrerResource(){
		referrerResource = null;
		referrerOwnerResource = null;
	}
}
