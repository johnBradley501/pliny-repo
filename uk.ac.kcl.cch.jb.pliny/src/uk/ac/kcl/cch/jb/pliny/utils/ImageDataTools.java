package uk.ac.kcl.cch.jb.pliny.utils;

import org.eclipse.draw2d.ColorConstants;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.widgets.Display;

public class ImageDataTools {
    private static ImageDataTools instance = null;
    
    public static ImageDataTools getInstance(){
    	if (instance == null)instance = new ImageDataTools();
    	return instance;
    }
    
	//static final int thumbWidth = 120;
	//static final int thumbHeight =120;
	
	public ImageData createThumbImage(ImageDescriptor fullImage, Dimension thumbSize){
		ImageData data = fullImage.getImageData();
		return createThumbImage(data, thumbSize);
	}
	
	public ImageData createThumbImage(ImageData data, Dimension thumbSize){
		int width = data.width;
		int height = data.height;
		if(width > thumbSize.width){
			if(height < width){
				width = thumbSize.width;
				height = (width*data.height)/data.width;
			} else {
				height = thumbSize.height;
				width = (height * data.width)/data.height;
			}
			
		} else if(height > thumbSize.width){
			height = thumbSize.height;
			width = (height * data.width)/data.height;
		}
		ImageData thumbData;
		if(data.depth == 1){
			Image newImage = new Image(Display.getCurrent(),data.width, data.height);
			GC gc = new GC(newImage);
			gc.setBackground(ColorConstants.black);
			newImage = new Image(Display.getCurrent(),newImage.getImageData(),data);
			data = newImage.getImageData();
		}
		if(width != data.width)
		   thumbData = data.scaledTo(width, height);
		else thumbData = data;
		
		return thumbData;

	}

}
