package com.example.fantazoo.model;

import java.io.Serializable;

public class Animal implements Serializable {
    private long id;
    private String name;
    private Cage cage;

    public Animal() {
    }

    public Animal(String name) {
        this.name = name;
    }

    public Animal(long id, String name, Cage cage) {
        this.id = id;
        this.name = name;
        this.cage = cage;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Cage getCage() {
        return cage;
    }

    public void setCage(Cage cage) {
        this.cage = cage;
    }

    @Override
    public String toString() {
        return name;
    }
}
