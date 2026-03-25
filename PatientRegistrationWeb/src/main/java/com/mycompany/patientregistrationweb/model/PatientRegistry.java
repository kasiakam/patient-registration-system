
package com.mycompany.patientregistrationweb.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

/**
 * Repository storing registered and managing registered patients.
 * Provides methods for adding patients and retrieving patient data.
 * Ensures data intergrity by preventing duplicate PESEL numbers.
 * 
 * @author Katarzyna Kamińska
 * @version 1.0
 * 
 */
public class PatientRegistry {
    
    /**
     * Constructs an empty patient registry.
     */
    
    public PatientRegistry(){
        
    }
    /**
     * Internal modifiable list of patients stored in the registry.
     */
    private final List<Patient> patients=new CopyOnWriteArrayList<>();
    
    /**
     * Functional interface for filtering patients with lambda expressions.
     */
    @FunctionalInterface
    public interface PatientFilter {
        /**
         * Tests if the patient should be included in the result.
         * 
         * @param patient the patient to test
         * @return true if patient passes the filter
         */
        boolean test(Patient patient);
    }
    /**
     * Adds a patient to the registry.
     * 
     * 
     * @param patient the patient to add to the registry
     * @throws InvalidPatientDataException if a patient with the same PESEL already exists
     */
    public void addPatient(Patient patient)throws InvalidPatientDataException {
        //check for duplicate PESEL
        boolean peselExists = patients.stream()
            .anyMatch(p -> p.getPesel().equals(patient.getPesel()));
            
        if (peselExists) {
            throw new InvalidPatientDataException("Pacjent z takim numerem PESEL już istnieje!");
        }
        
        patients.add(patient);
    }
        /*
      boolean peselExists=patients.stream()
              .anyMatch(p->p.getPesel().equals(patient.getPesel()));
        patients.add(patient);
    }
    */
        
    /**
     * Returns an immutable copy of all registered patients.
     * 
     * @return list containing copies of registered patients
     */
    public List<Patient> getAllPatients(){
        return new ArrayList<>(patients);
    }
    
    /**
     * Indicates whether the registery currently conatins any patients.
     * @return true if there are no patients, false if there is at least one patient
     */
    public boolean isEmpty(){
        return patients.isEmpty();
    }
    
    public boolean removePatientByPesel(String pesel){
    for(Patient patient:patients){
        if(patient.getPesel().equals(pesel)){
            patients.remove(patient);
            return true;
        }
    }
    return false;
    
    }
    public boolean removePatient(Patient patient){
        return patients.remove(patient);
    }
    
    public void updatePatient(String oldPesel, Patient updatedPatient) throws InvalidPatientDataException{
        if(!oldPesel.equals(updatedPatient.getPesel())){
            boolean peselExists=patients.stream()
                    .anyMatch(p->p.getPesel().equals(updatedPatient.getPesel()));
            if(peselExists){
                throw new InvalidPatientDataException("Pacjent z tym PESEL'em już istnieje.");
            }
        }
    
 boolean found= false;
 for (int i=0; i<patients.size(); i++){
     Patient existingPatient = patients.get(i);
     if(existingPatient.getPesel().equals(oldPesel)){
         patients.set(i, updatedPatient);
         found=true;
         break;
     }
 }
 if(!found){
     throw new InvalidPatientDataException("Nie znaleziono pacjenta z PESELEM: "+ oldPesel);
 }
    }
    public List<Patient> filterPatients(PatientFilter filter) {
        return patients.stream()
            .filter(filter::test)
            .collect(Collectors.toList());
    }
    
    /**
     * Gets adult patients (age >= 18).
     * 
     * @return list of adult patients
     */
    public List<Patient> getAdultPatients() {
        return filterPatients(p -> p.getAge() >= 18);
    }
    
    /**
     * Counts patients by gender.
     * 
     * @param gender the gender code ("M" or "K")
     * @return count of patients with specified gender
     */
    public long countByGender(String gender) {
        return patients.stream()
            .filter(p -> p.getGender().equals(gender))
            .count();
    }
    
    /**
     * Gets all patient names in "Name Surname" format.
     * 
     * @return list of full patient names
     */
    public List<String> getAllPatientNames() {
        return patients.stream()
            .map(p -> p.getName() + " " + p.getSurname())
            .collect(Collectors.toList());
    }
}
