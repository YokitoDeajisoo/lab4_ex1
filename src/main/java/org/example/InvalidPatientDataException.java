package org.example;

public class InvalidPatientDataException extends Exception {
    public InvalidPatientDataException(String message) {
        super(message);
    }
}