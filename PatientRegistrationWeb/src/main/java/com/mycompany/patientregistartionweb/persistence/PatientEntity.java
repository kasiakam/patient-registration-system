
package com.mycompany.patientregistartionweb.persistence;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;
/**
 *
 * @author Katarzyna Kamińska
 */
@Entity


@Table(name = "PATIENT", uniqueConstraints = @UniqueConstraint(columnNames = "PESEL"))
public class PatientEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID")
    private Long id;

    @Column(name = "NAME", nullable = false, length = 50)
    private String name;

    @Column(name = "SURNAME", nullable = false, length = 50)
    private String surname;

    @Column(name = "AGE", nullable = false)
    private int age;

    @Column(name = "PESEL", nullable = false, length = 11)
    private String pesel;

    @Column(name = "GENDER", nullable = false, length = 1)
    private String gender; // "M" / "K"

    @OneToMany(mappedBy = "patient", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OperationLogEntity> operations = new ArrayList<>();

    public PatientEntity() {}

    public PatientEntity(String name, String surname, int age, String pesel, String gender) {
        this.name = name;
        this.surname = surname;
        this.age = age;
        this.pesel = pesel;
        this.gender = gender;
    }

    public Long getId() { return id; }
    public String getName() { return name; }
    public String getSurname() { return surname; }
    public int getAge() { return age; }
    public String getPesel() { return pesel; }
    public String getGender() { return gender; }
    
    public void setName(String name) { this.name = name; }
    public void setSurname(String surname) { this.surname = surname; }
    public void setAge(int age) { this.age = age; }
    public void setPesel(String pesel) { this.pesel = pesel; }
    public void setGender(String gender) { this.gender = gender; }
}