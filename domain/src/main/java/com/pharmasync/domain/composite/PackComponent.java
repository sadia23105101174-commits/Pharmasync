package com.pharmasync.domain.composite;

import java.util.List;

/**
 * Composite pattern interface representing a packaging level (e.g. Box, Strip, Tablet).
 */
public interface PackComponent {
    /**
     * Name of the packaging unit, e.g. "box", "strip", "tablet".
     */
    String getName();

    /**
     * How many child units make up one unit of this component.
     * Leaf component returns 1.
     */
    int getMultiplier();

    /**
     * Total number of base (leaf) units represented by one unit of this component.
     */
    int getUnitsInBase();

    /**
     * Returns true if this is the fundamental base unit (e.g. tablet).
     */
    boolean isLeaf();

    /**
     * Child component in the hierarchy, or null for leaf.
     */
    PackComponent getChild();

    /**
     * Returns the ordered path of components from this component down to the leaf.
     */
    List<PackComponent> getHierarchyPath();
}
