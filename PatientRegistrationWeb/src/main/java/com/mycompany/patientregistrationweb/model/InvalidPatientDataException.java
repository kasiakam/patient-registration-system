
package com.mycompany.patientregistrationweb.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Exception thrown when patient data is invalid.
 * Can contain multiple validation errors.
 * 
 * @author Katarzyna Kamińska
 * @version 1
 */
public class InvalidPatientDataException extends Exception {
    
    /**
     * List of validation error messages.
     */
    private final List<String>errors;
    
    /**
     * Constructs exception with single error.
     * 
     * @param message the detail message explaining the reason for the exception
     */
    public InvalidPatientDataException(String message){
        super(message);
        this.errors=new ArrayList<>();
        this.errors.add(message);
    }
    
    /**
     * Constructs exception with multiple validation errors.
     * 
     * @param errors list of validation error messages
     */
    public InvalidPatientDataException(List<String> errors){
        super(String.join(", ", errors));
        this.errors=new ArrayList<>(errors);
    }

    /**
     * Returns all validation errors.
     * 
     * @return list of error messages
     */
    public List<String> getErrors() {
        return new ArrayList<> (errors);
    }
    
    /**
     * Returns formatted error message with all errors.
     * 
     * @return formatted error message
     */
    @Override
    public String getMessage(){
        if(errors.size()==1){
            return errors.get(0);
        } else{
            return "Multiple validation errors: " + String.join("; ", errors);
        }
    }
}
