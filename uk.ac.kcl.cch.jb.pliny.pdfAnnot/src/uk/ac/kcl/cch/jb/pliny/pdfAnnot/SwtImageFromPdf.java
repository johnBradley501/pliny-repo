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

package uk.ac.kcl.cch.jb.pliny.pdfAnnot;

import java.awt.image.BufferedImage;
import java.io.File;

import javax.imageio.ImageIO;

import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.PaletteData;
import org.eclipse.swt.widgets.Display;
import org.jpedal.PdfDecoder;
import org.jpedal.exception.PdfException;

public class SwtImageFromPdf {
	private static final PaletteData PALETTE_DATA =
		    new PaletteData(0xFF0000, 0xFF00, 0xFF);

	private PdfDecoder pdfDecoder;
	private ImageData swtImageData = null;
	private int width = -1, height = -1, currPage = -1;
	private int[] awtPixels = null;
	private Image swtImage = null;


	public SwtImageFromPdf(PdfDecoder pdfDecoder) {
		this.pdfDecoder = pdfDecoder;
	}
	
	public void dispose(){
		if(swtImage != null)
		   swtImage.dispose();
		swtImage = null;
	}
	
	public PdfDecoder getMyPdfDecoder(){
		return pdfDecoder;
	}
	
	public Image getSwtImageForPage(int pgnumb/*, float scaling*/){
		if(currPage == pgnumb && (width == pdfDecoder.getWidth()))return swtImage;
		currPage = pgnumb;
		BufferedImage awtImage = null;
		try {
			//pdfDecoder.setPageParameters(scaling, pgnumb); 
			//pdfDecoder.decodePage(pgnumb);
			//pdfDecoder.invalidate();
			//pdfDecoder.repaint();
			awtImage = pdfDecoder.getPageAsImage(pgnumb);
			//File outimage = new File("c:/decodePDF2.jpg");
			//ImageIO.write(awtImage, "jpg", outimage);
		} catch (PdfException e) {
			e.printStackTrace();
			return null;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	    if (swtImage != null) swtImage.dispose();
	    swtImage = null;
	    
		if(awtImage.getWidth() != width || awtImage.getHeight() != height){
			width = awtImage.getWidth();
			height = awtImage.getHeight();
			swtImageData = new ImageData(width, height, 24, PALETTE_DATA);
			awtPixels = new int[width*height];
		}
	    //int step = swtImageData.depth / 8;
	    byte[] data = swtImageData.data;
	    awtImage.getRGB(0, 0, width, height, awtPixels, 0, width);
	    for (int i = 0; i < height; i++) {
	      int idx = (i) * swtImageData.bytesPerLine;
	      for (int j = 0; j < width; j++) {
	        int rgb = awtPixels[j + i * width];
	        for (int k = swtImageData.depth - 8; k >= 0; k -= 8) {
	          data[idx++] = (byte) ((rgb >> k) & 0xFF);
	        }
	      }
	    }
	    swtImage = new Image(Display.getDefault(), swtImageData);
	    return swtImage;
	}

}
