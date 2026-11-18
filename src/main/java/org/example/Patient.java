package org.example;

public class Patient {
    private int id;
    private String fullName;
    private String birthDate;
    private double weight;
    private boolean hasDeclaration;
    private String diagnosis;

    public Patient(int id, String fullName, String birthDate, double weight, boolean hasDeclaration, String diagnosis) {
        this.id = id;
        this.fullName = fullName;
        this.birthDate = birthDate;
        this.weight = weight;
        this.hasDeclaration = hasDeclaration;
        this.diagnosis = diagnosis;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getBirthDate() { return birthDate; }
    public void setBirthDate(String birthDate) { this.birthDate = birthDate; }

    public double getWeight() { return weight; }
    public void setWeight(double weight) { this.weight = weight; }

    public boolean isHasDeclaration() { return hasDeclaration; }
    public void setHasDeclaration(boolean hasDeclaration) { this.hasDeclaration = hasDeclaration; }

    public String getDiagnosis() { return diagnosis; }
    public void setDiagnosis(String diagnosis) { this.diagnosis = diagnosis; }
}