package uk.ac.kcl.cch.jb.pliny.imageRes.dnd;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.widgets.Button;

import uk.ac.kcl.cch.rdb2java.dynData.PropertyChangeObject;

public class HtmlImageData{
	private String altText;
	private URL theURL;
	private ImageData theImage;
	private boolean selected = false;
	private Button myButton = null;
	private Dimension imageDimension = null;
	
	public HtmlImageData(String altText, URL theURL){
		this.altText = altText;
		this.theURL = theURL;
		this.theImage = null;
	}
	
	public void setDimension(int width, int height){
		imageDimension = new Dimension(width, height);
	}
	
	public Dimension getDimension(){
		if(imageDimension == null){
			ImageData image = getTheImageData();
			imageDimension = new Dimension(image.width, image.height);
		}
		return imageDimension;
	}
	
	public String getAltText(){
		return altText;
	}
	
	public URL getTheURL(){
		return theURL;
	}
	
	public void setSelected(boolean val){ 
		if(val == selected)return;
		selected = val;
		if(myButton != null)myButton.setSelection(selected);
	}
			
	public boolean isSelected(){return selected; }
	
	public ImageData getTheImageData(){
		if(theImage != null)return theImage;
		URLConnection connection = null;
		InputStream in = null;
		try {
			connection = theURL.openConnection();
			in = connection.getInputStream();
			theImage = new ImageData(in);
			in.close();
		} catch (IOException e1) {
			e1.printStackTrace();
			return null;
		}
		return theImage;
	}

	public void setMyButton(Button b) {
		myButton = b;
		
	}

}
