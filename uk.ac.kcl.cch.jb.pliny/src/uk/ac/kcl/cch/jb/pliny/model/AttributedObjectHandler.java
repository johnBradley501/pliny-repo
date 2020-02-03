package uk.ac.kcl.cch.jb.pliny.model;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Properties;

import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.draw2d.geometry.Dimension;
/**
 * provides a tool to manage rdb2java items with attributes field as a
 * <code>java.util.Properties</code> item.  In this way, users of this item can
 * store a range of properties in the single attributes field without
 * needing to modify the DB definition to add more DB fields.
 * <p>
 * It is important to recognise, of course, that storing data using
 * this tool means that the DB engine itself is not going to be very good
 * at selected items from the DB records that are based on values of these
 * stored values.
 * 
 * @author John Bradley
 *
 */

public class AttributedObjectHandler {
	
	protected IHasAttribute myItem = null;
	private Properties myProperties = null;
	private String handlerType = null;
	
	public AttributedObjectHandler(IHasAttribute item, String type){
		this.myItem = item;
		this.handlerType = type;
	}

	public String getIdentifier(){
		if(myItem == null)return null;
		return handlerType+":"+myItem.getALID();
	}
	
	private void loadProperties(){
		myProperties = new Properties();
		if(myItem == null)return;
		String attributes = myItem.getAttributes();
		ByteArrayInputStream input = new ByteArrayInputStream(attributes.getBytes());
		try {
			myProperties.load(input);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private void updateAttributes(){
		if(myItem == null)return;
		ByteArrayOutputStream output = new ByteArrayOutputStream();
		if(myProperties == null)loadProperties();
		myProperties.save(output,"attr");
		myItem.setAttributes(output.toString());
	}
	
	/**
	 * looks up value in the properties associated with this 
	 * {@link Resource}'s
	 * attribute field, and returns it as a <code>String</code>.
	 * <code>null</code> means that the attributes was not found.
	 * 
	 * @param propName name of property.
	 * @return String with value
	 */
	
	public String getString(String propName){
		if(myProperties == null)loadProperties();
		return myProperties.getProperty(propName);
	}
	
	/**
	 * looks up value in the properties associated with this Resource's
	 * attribute field, and returns it as an <code>Integer</code>.
	 * <code>null</code> means that the attributes was not found.
	 * 
	 * @param propName name of property.
	 * @return Integer with value
	 */
	
	public int getInt(String propName){
		String rslt = getString(propName);
		if(rslt == null)return 0;
		return Integer.parseInt(rslt);
	}
	
	/**
	 * looks up value in the properties associated with this Resource's
	 * attribute field, and returns it as an <code>float</code>.
	 * <code>null</code> means that the attributes was not found.
	 * 
	 * @param propName name of property.
	 * @return Float with value
	 */
	
	public float getFloat(String propName){
		String rslt = getString(propName);
		if(rslt == null)return 0f;
		return Float.parseFloat(rslt);
	}
	
	/**
	 * looks up value in the properties associated with this Resource's
	 * attribute field, and returns it as an draw2d <code>Rectangle</code>.
	 * <code>null</code> means that the attributes was not found.
	 * 
	 * @param propName name of property.
	 * @return Rectangle with value
	 */
	
	
	public Rectangle getRectangle(String propName){
		String posString = getString(propName);
		   if((posString == null) || (posString.equals("")))
			   return new Rectangle(0,0,0,0);
	   String[] parts = posString.split(":");
	   if(!parts[0].equals("rect"))return null;
	   String[] numbs = parts[1].split(",");
	   if(numbs.length != 4)return null;
	   return new Rectangle(
			   Integer.parseInt(numbs[0]) /* x */,
			   Integer.parseInt(numbs[1]) /* y */,
			   Integer.parseInt(numbs[2]) /* width */,
			   Integer.parseInt(numbs[3]) /* height */);
	}
	
	
	/**
	 * looks up value in the properties associated with this Resource's
	 * attribute field, and returns it as an draw2d <code>Dimension</code>.
	 * <code>null</code> means that the attributes were badly formed or not found.
	 * 
	 * @param propName name of property.
	 * @return Dimension with value
	 */

	public Dimension getDimension(String propName){
		String dimString = getString(propName);
		if(dimString == null || dimString.trim().length() == 0)
			return null; // new Dimension(0,0);
		String[] parts = dimString.trim().split(":");
		if(!parts[0].equals("dim"))return null;
		String[] numbs = parts[1].split(",");
		if(numbs.length != 2)return null;
		return new Dimension(
				Integer.parseInt(numbs[0]) /* width */, 
				Integer.parseInt(numbs[1]) /* height */
		);
	}
	
	/**
	 * takes the given <code>value</code> as a String and stores it as the value
	 * associated with the given attribute name <code>propName</code>.
	 * The DB field attribute is updated to reflect the new value for this item.
	 * 
	 * @param propName String that names the attribute
	 * @param value String value that it is to be assigned. A value of
	 * <code>null</code> removes the property.
	 */
	
	public void updateString(String propName, String value){
		if(myProperties == null)loadProperties();
		if(value == null)myProperties.remove(propName); //added Oct 2010  JB
		else myProperties.setProperty(propName, value);
		updateAttributes();
	}
	
	/**
	 * takes the given <code>value</code> as an <code>int</code> and stores it as the value
	 * associated with the given attribute name <code>propName</code>.
	 * The DB field attribute is updated to reflect the new value for this item.
	 * 
	 * @param propName String that names the attribute
	 * @param value <code>int</code> value that it is to be assigned.
	 */
	
	public void updateInt(String propName, int value){
		String newVal = Integer.toString(value);
		updateString(propName, newVal);
	}
	
	/**
	 * takes the given <code>value</code> as a <code>float</code> and stores it as the value
	 * associated with the given attribute name <code>propName</code>.
	 * The DB field attribute is updated to reflect the new value for this item.
	 * 
	 * @param propName String that names the attribute
	 * @param value <code>float</code> value that it is to be assigned.
	 */
	
	public void updateFloat(String propName, float value){
		String newVal = Float.toString(value);
		updateString(propName, newVal);
	}
	
	/**
	 * takes the given <code>value</code> as a draw2d <code>Rectangle</code> and stores it as the value
	 * associated with the given attribute name <code>propName</code>.
	 * The DB field attribute is updated to reflect the new value for this item.
	 * 
	 * @param propName String that names the attribute
	 * @param r draw2d <code>Rectangle</code> value that it is to be assigned. A value of
	 * <code>null</code> removes the property.
	 */
	
	public void updateRectangle(String propName, Rectangle r){
		   String posString = "rect:";
		   if(r == null)posString = null;
		   else posString += r.x+","+r.y+","+r.width+","+r.height;
		   updateString(propName, posString);
   }
	
	/**
	 * takes the given <code>value</code> as a draw2d <code>Dimension</code> and stores it as the value
	 * associated with the given attribute name <code>propName</code>.
	 * The DB field attribute is updated to reflect the new value for this item.
	 * 
	 * @param propName String that names the attribute
	 * @param d draw2d <code>Dimension</code> value that it is to be assigned. A value of
	 * <code>null</code> removes the property.
	 */

	public void updateDimension(String propName, Dimension d){
		String dimString = "dim:";
		if(d == null)dimString = null;
		else dimString += d.width+","+d.height;
		updateString(propName, dimString);
	}


}
