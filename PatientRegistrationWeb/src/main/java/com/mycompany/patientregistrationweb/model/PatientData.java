package com.mycompany.patientregistrationweb.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Immutable patient input data with validation and normalization rules.
 * <p>
 * Validation is performed in the canonical constructor. If validation fails,
 * an {@link IllegalArgumentException} is thrown (caught and mapped in {@link Patient}).
 * </p>
 *
 * @author Katarzyna Kamińska
 * @version 1.0
 */
public record PatientData(
        String name,
        String surname,
        int age,
        String pesel,
        Gender gender
) {

    /**
     * Patient gender with code used in HTTP forms ("M" / "K") and human-readable description.
     */
    public enum Gender {
        /** Male gender. */
        MALE("M", "Male"),
        /** Female gender. */
        FEMALE("K", "Female");

        private final String code;
        private final String description;

        /**
         * Creates gender enum value.
         *
         * @param code short code used in forms
         * @param description human-readable name
         */
        Gender(String code, String description) {
            this.code = code;
            this.description = description;
        }

        /**
         * Returns the short code used in forms.
         *
         * @return gender code
         */
        public String getCode() {
            return code;
        }

        /**
         * Returns the human-readable description.
         *
         * @return gender description
         */
        public String getDescription() {
            return description;
        }

        /**
         * Converts form code ("M" / "K") to {@link Gender}.
         *
         * @param code gender code
         * @return matching gender
         * @throws IllegalArgumentException if code is invalid
         */
        public static Gender fromCode(String code) {
            if (code == null) {
                throw new IllegalArgumentException("Gender code is required.");
            }
            for (Gender g : Gender.values()) {
                if (g.code.equals(code)) {
                    return g;
                }
            }
            throw new IllegalArgumentException("Invalid gender code: " + code);
        }
    }

    /**
     * Canonical constructor that validates and normalizes patient data.
     *
     * @throws IllegalArgumentException if validation fails
     */
    public PatientData {
        List<String> errors = new ArrayList<>();

        // Normalize raw values early (without breaking record assignment rules)
        String rawName = name == null ? null : name.trim();
        String rawSurname = surname == null ? null : surname.trim();
        String rawPesel = pesel == null ? null : pesel.trim();

        // Name / surname: letters only (including Polish), spaces, hyphen, apostrophe; no digits
        // Examples allowed: "Anna", "Jan Kowalski", "O'Connor", "Nowak-Kowalska"
        String namePattern = "^[A-Za-zĄĆĘŁŃÓŚŹŻąćęłńóśźż\\-\\' ]{2,50}$";

        if (rawName == null || rawName.isBlank()) {
            errors.add("Name cannot be empty.");
        } else if (!rawName.matches(namePattern)) {
            errors.add("Name must contain letters only (no digits).");
        }

        if (rawSurname == null || rawSurname.isBlank()) {
            errors.add("Surname cannot be empty.");
        } else if (!rawSurname.matches(namePattern)) {
            errors.add("Surname must contain letters only (no digits).");
        }

        if (age < 1 || age > 130) {
            errors.add("Age must be between 1 and 130.");
        }

        if (rawPesel == null || rawPesel.isBlank()) {
            errors.add("PESEL cannot be empty.");
        } else if (!rawPesel.matches("^\\d{11}$")) {
            errors.add("PESEL must contain exactly 11 digits.");
        }

        if (gender == null) {
            errors.add("Gender is required.");
        }

        if (!errors.isEmpty()) {
            // Multiple errors in one message; Patient maps it to InvalidPatientDataException
            throw new IllegalArgumentException(String.join("; ", errors));
        }

        // Apply normalization to record components
        name = capitalizeName(rawName);
        surname = capitalizeName(rawSurname);
        pesel = rawPesel;
    }

    /**
     * Convenience constructor for form usage (gender code as String).
     *
     * @param name first name
     * @param surname last name
     * @param age age
     * @param pesel PESEL (11 digits)
     * @param genderCode gender code ("M" or "K")
     * @throws IllegalArgumentException if validation fails
     */
    public PatientData(String name, String surname, int age, String pesel, String genderCode) {
        this(name, surname, age, pesel, Gender.fromCode(genderCode));
    }

    /**
     * Capitalizes the first letter of each word and lowercases the remaining characters.
     *
     * @param value raw text
     * @return normalized value
     */
    private static String capitalizeName(String value) {
        if (value == null || value.isBlank()) {
            return value;
        }

        String[] words = value.split("\\s+");
        StringBuilder result = new StringBuilder();

        for (int i = 0; i < words.length; i++) {
            if (i > 0) {
                result.append(' ');
            }

            String word = words[i];
            if (!word.isEmpty()) {
                result.append(Character.toUpperCase(word.charAt(0)))
                      .append(word.substring(1).toLowerCase());
            }
        }

        return result.toString();
    }

    /**
     * Returns gender code used by the web layer ("M" / "K").
     *
     * @return gender code
     */
    public String getGenderCode() {
        return gender.getCode();
    }
}