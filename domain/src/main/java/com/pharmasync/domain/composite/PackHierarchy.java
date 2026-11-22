package com.pharmasync.domain.composite;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * Manages the packaging hierarchy for a medicine (e.g. Box -> Strip -> Tablet).
 * Provides bi-directional unit conversion between package levels and base units.
 */
public class PackHierarchy {
    private final PackComponent root;
    private final PackComponent leaf;
    private final List<PackComponent> components;

    public PackHierarchy(PackComponent root) {
        this.root = Objects.requireNonNull(root, "Root pack component must not be null");
        this.components = root.getHierarchyPath();
        this.leaf = components.get(components.size() - 1);
    }

    /**
     * Standard factory method for Box -> Strip -> Tablet packaging.
     *
     * @param stripsPerBox    number of strips in a box (e.g. 10)
     * @param tabletsPerStrip number of tablets in a strip (e.g. 10)
     * @return configured PackHierarchy
     */
    public static PackHierarchy boxStripTablet(int stripsPerBox, int tabletsPerStrip) {
        BasePackUnit tablet = new BasePackUnit("tablet");
        CompositePackUnit strip = new CompositePackUnit("strip", tabletsPerStrip, tablet);
        CompositePackUnit box = new CompositePackUnit("box", stripsPerBox, strip);
        return new PackHierarchy(box);
    }

    /**
     * Factory for custom 2-level hierarchy (e.g. Bottle of 100 Capsules).
     */
    public static PackHierarchy of(String rootName, int multiplier, String leafName) {
        BasePackUnit leafUnit = new BasePackUnit(leafName);
        CompositePackUnit rootUnit = new CompositePackUnit(rootName, multiplier, leafUnit);
        return new PackHierarchy(rootUnit);
    }

    public PackComponent getRoot() {
        return root;
    }

    public PackComponent getLeaf() {
        return leaf;
    }

    public List<PackComponent> getComponents() {
        return components;
    }

    public Optional<PackComponent> findComponent(String unitName) {
        if (unitName == null) return Optional.empty();
        String target = unitName.trim().toLowerCase();
        return components.stream()
                .filter(c -> c.getName().equalsIgnoreCase(target))
                .findFirst();
    }

    /**
     * Converts a given quantity of a specific packaging unit into base units.
     * E.g. 2 boxes of 10 strips of 10 tablets -> 200 tablets.
     *
     * @param unitName unit name (e.g. "box", "strip", "tablet")
     * @param quantity quantity of this unit
     * @return total quantity in base units
     */
    public int toBaseUnits(String unitName, int quantity) {
        if (quantity < 0) {
            throw new IllegalArgumentException("Quantity cannot be negative: " + quantity);
        }
        PackComponent component = findComponent(unitName)
                .orElseThrow(() -> new IllegalArgumentException("Unknown pack unit: '" + unitName + "'. Available: " + getAvailableUnitNames()));
        return quantity * component.getUnitsInBase();
    }

    /**
     * Breaks down a total quantity of base units into greedy highest-level units.
     * E.g. 235 tablets -> {box: 2, strip: 3, tablet: 5}.
     *
     * @param totalBaseUnits total base units to break down
     * @return LinkedHashMap preserving hierarchy order (highest unit to leaf)
     */
    public Map<String, Integer> fromBaseUnits(int totalBaseUnits) {
        if (totalBaseUnits < 0) {
            throw new IllegalArgumentException("Base units cannot be negative: " + totalBaseUnits);
        }
        Map<String, Integer> breakdown = new LinkedHashMap<>();
        int remainder = totalBaseUnits;

        for (PackComponent component : components) {
            int unitSize = component.getUnitsInBase();
            int count = remainder / unitSize;
            remainder = remainder % unitSize;
            breakdown.put(component.getName(), count);
        }

        return breakdown;
    }

    public List<String> getAvailableUnitNames() {
        return components.stream().map(PackComponent::getName).toList();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PackHierarchy that = (PackHierarchy) o;
        return Objects.equals(root, that.root);
    }

    @Override
    public int hashCode() {
        return Objects.hash(root);
    }

    @Override
    public String toString() {
        return "PackHierarchy{" + root + "}";
    }
}
