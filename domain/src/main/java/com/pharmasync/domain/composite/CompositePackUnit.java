package com.pharmasync.domain.composite;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Composite node in the PackHierarchy pattern.
 * Represents a packaging level that contains multiple child units (e.g. Strip of 10 Tablets, Box of 10 Strips).
 */
public class CompositePackUnit implements PackComponent {
    private final String name;
    private final int multiplier;
    private final PackComponent child;
    private final int unitsInBase;

    public CompositePackUnit(String name, int multiplier, PackComponent child) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Unit name must not be blank");
        }
        if (multiplier <= 0) {
            throw new IllegalArgumentException("Multiplier must be strictly positive");
        }
        Objects.requireNonNull(child, "Child component must not be null in CompositePackUnit");
        this.name = name.trim().toLowerCase();
        this.multiplier = multiplier;
        this.child = child;
        this.unitsInBase = multiplier * child.getUnitsInBase();
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public int getMultiplier() {
        return multiplier;
    }

    @Override
    public int getUnitsInBase() {
        return unitsInBase;
    }

    @Override
    public boolean isLeaf() {
        return false;
    }

    @Override
    public PackComponent getChild() {
        return child;
    }

    @Override
    public List<PackComponent> getHierarchyPath() {
        List<PackComponent> path = new ArrayList<>();
        path.add(this);
        path.addAll(child.getHierarchyPath());
        return List.copyOf(path);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CompositePackUnit that = (CompositePackUnit) o;
        return multiplier == that.multiplier &&
                unitsInBase == that.unitsInBase &&
                Objects.equals(name, that.name) &&
                Objects.equals(child, that.child);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, multiplier, child, unitsInBase);
    }

    @Override
    public String toString() {
        return "CompositePackUnit{" + name + " (" + multiplier + " x " + child.getName() + " = " + unitsInBase + " base units)}";
    }
}
