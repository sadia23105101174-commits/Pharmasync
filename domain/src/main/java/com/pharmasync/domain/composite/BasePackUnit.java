package com.pharmasync.domain.composite;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Leaf component in the PackHierarchy Composite pattern.
 * Represents the fundamental base unit (e.g. tablet, capsule, ml).
 */
public class BasePackUnit implements PackComponent {
    private final String name;

    public BasePackUnit(String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Unit name must not be blank");
        }
        this.name = name.trim().toLowerCase();
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public int getMultiplier() {
        return 1;
    }

    @Override
    public int getUnitsInBase() {
        return 1;
    }

    @Override
    public boolean isLeaf() {
        return true;
    }

    @Override
    public PackComponent getChild() {
        return null;
    }

    @Override
    public List<PackComponent> getHierarchyPath() {
        return Collections.singletonList(this);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BasePackUnit that = (BasePackUnit) o;
        return Objects.equals(name, that.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }

    @Override
    public String toString() {
        return "BasePackUnit{" + name + " (1 base unit)}";
    }
}
