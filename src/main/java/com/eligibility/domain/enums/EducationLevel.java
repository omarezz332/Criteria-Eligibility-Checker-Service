package com.eligibility.domain.enums;

public enum EducationLevel {

    NONE    (0),
    DIPLOMA (30),
    BSC     (50),
    MSC     (80),
    PHD     (100);

    private final int weight;

    EducationLevel(int weight) {
        this.weight = weight;
    }

    public int getWeight() {
        return weight;
    }
}
