package com.example.fantazoo.model;

import java.util.List;

public class Cage {
    private long id;
    private String name;
    private List<Animal> animals;
    private List<Zookeeper> zookeepers;

    public Cage() {
    }

    public Cage(String name) {
        this.name = name;
    }

    public Cage(long id, String name, List<Animal> animals, List<Zookeeper> zookeepers) {
        this.id = id;
        this.name = name;
        this.animals = animals;
        this.zookeepers = zookeepers;
    }

    public String getAnimalString() {
        StringBuilder sb = new StringBuilder();

        sb.append("Animal(s):");

        for (Animal animal : this.getAnimals()) {
            sb.append('\n').append(animal.getName());
        }

        return sb.toString();
    }

    public String getKeeperString() {
        StringBuilder sb = new StringBuilder();

        sb.append("Keeper(s):");

        for (Zookeeper zookeeper : this.getZookeepers()) {
            sb.append('\n').append(zookeeper.getName());
        }

        return sb.toString();
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

    public List<Animal> getAnimals() {
        return animals;
    }

    public void setAnimals(List<Animal> animals) {
        this.animals = animals;
    }

    public List<Zookeeper> getZookeepers() {
        return zookeepers;
    }

    public void setZookeepers(List<Zookeeper> zookeepers) {
        this.zookeepers = zookeepers;
    }

    @Override
    public String toString() {
        return name;
    }
}
