package com.nbteam.hislite.common.castor;


import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Properties;

import org.exolab.castor.mapping.ConfigurableFieldHandler;
import org.exolab.castor.mapping.GeneralizedFieldHandler;
import org.exolab.castor.mapping.ValidityException;

/**
 * The FieldHandler for the Date class
 *
 */
@SuppressWarnings("rawtypes")
public class MyDateHandler extends GeneralizedFieldHandler implements ConfigurableFieldHandler {

    private SimpleDateFormat FORMAT;

    /**
     * Creates a new MyDateHandler instance
     */
    public MyDateHandler() {
        super();
    }

    public void setConfiguration(Properties config) throws ValidityException {  
        String pattern = config.getProperty("date-format");  
        if (pattern == null) {  
            throw new ValidityException("Required parameter \"date-format\" is missing");  
        }  
        try {  
            FORMAT = new SimpleDateFormat(pattern);  
        } catch (IllegalArgumentException e) {  
            throw new ValidityException("Pattern \"" + pattern + "\" is not a valid date format.");  
        }  
    }  
  
    
    /**
     * This method is used to convert the value when the
     * getValue method is called. The getValue method will
     * obtain the actual field value from given 'parent' object.
     * This convert method is then invoked with the field's
     * value. The value returned from this method will be
     * the actual value returned by getValue method.
     *
     * @param value the object value to convert after
     *  performing a get operation
     * @return the converted value.
     */
    public Object convertUponGet(Object value) {
        if (value == null) return null;
        SimpleDateFormat formatter = FORMAT;
        Date date = (Date)value;
        return formatter.format(date);
    }


    /**
     * This method is used to convert the value when the
     * setValue method is called. The setValue method will
     * call this method to obtain the converted value.
     * The converted value will then be used as the value to
     * set for the field.
     *
     * @param value the object value to convert before
     *  performing a set operation
     * @return the converted value.
     */
    public Object convertUponSet(Object value) {
        SimpleDateFormat formatter = FORMAT;
        Date date = null;
        try {
            date = formatter.parse((String)value);
        }
        catch(ParseException px) {
            throw new IllegalArgumentException(px.getMessage());
        }
        return date;
    }

    /**
     * Returns the class type for the field that this
     * GeneralizedFieldHandler converts to and from. This
     * should be the type that is used in the
     * object model.
     *
     * @return the class type of of the field
     */
    public Class<?> getFieldType() {
        return Date.class;
    }

    /**
     * Creates a new instance of the object described by
     * this field.
     *
     * @param parent The object for which the field is created
     * @return A new instance of the field's value
     * @throws IllegalStateException This field is a simple
     *  type and cannot be instantiated
     */
    public Object newInstance(Object parent) throws IllegalStateException
    {
        //-- Since it's marked as a string...just return null,
        //-- it's not needed.
        return null;
    }

}