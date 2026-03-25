
package com.mycompany.patientregistrationweb.model;

import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
/**
 * Represents a patient in the registration system.
 * Contains personal data including name, surname, age and PESEL identifier.
 * Fields are fully encapsulated and accessivle only via getters.
 * 
 * @author Katarzyna Kamińska
 * @version 1.0
 */

@Getter @Setter
@ToString 
public class Patient {
    
    /**
     * Immutable patient data.
     */
    //private final PatientData data;
  
    private final String name;
    
    private final String surname;
    
    private final int age;
    
    private final String pesel;
    
    private final String gender;
    
    
    /**
     * Constructs a patient with validated data.
     * 
     * @param data the patient data record
     * @throws InvalidPatientDataException if data is invalid
     */
    public Patient(PatientData data) throws InvalidPatientDataException {
        try {
            this.name = data.name();
            this.surname=data.surname();
            this.age=data.age();
            this.pesel=data.pesel();
            this.gender=data.getGenderCode();
        } catch (IllegalArgumentException e) {
            throw new InvalidPatientDataException(e.getMessage());
        }
    }
    
    /**
     * Convenience constructor for creating patient from individual fields.
     * 
     * @param name the first name
     * @param surname the surname
     * @param age the age
     * @param pesel the PESEL
     * @throws InvalidPatientDataException if data is invalid
     */
    public Patient(String name, String surname, int age, String pesel,String gender) throws InvalidPatientDataException {
        this(new PatientData(name, surname, age, pesel,gender));
    }
   
   
}
