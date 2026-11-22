package com.pharmasync.domain.composite;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PackHierarchyTest {

    @Test
    @DisplayName("Composite pack hierarchy builds Box -> Strip -> Tablet correctly")
    void testBoxStripTabletHierarchy() {
        // 1 Box = 10 Strips; 1 Strip = 10 Tablets; Total 1 Box = 100 Tablets
        PackHierarchy hierarchy = PackHierarchy.boxStripTablet(10, 10);

        assertThat(hierarchy.getRoot().getName()).isEqualTo("box");
        assertThat(hierarchy.getRoot().getUnitsInBase()).isEqualTo(100);
        assertThat(hierarchy.getRoot().getMultiplier()).isEqualTo(10);
        assertThat(hierarchy.getRoot().isLeaf()).isFalse();

        PackComponent strip = hierarchy.getRoot().getChild();
        assertThat(strip.getName()).isEqualTo("strip");
        assertThat(strip.getUnitsInBase()).isEqualTo(10);
        assertThat(strip.getMultiplier()).isEqualTo(10);
        assertThat(strip.isLeaf()).isFalse();

        PackComponent tablet = strip.getChild();
        assertThat(tablet.getName()).isEqualTo("tablet");
        assertThat(tablet.getUnitsInBase()).isEqualTo(1);
        assertThat(tablet.getMultiplier()).isEqualTo(1);
        assertThat(tablet.isLeaf()).isTrue();
        assertThat(tablet.getChild()).isNull();
    }

    @Test
    @DisplayName("toBaseUnits converts various packaging units to base units correctly")
    void testToBaseUnits() {
        PackHierarchy hierarchy = PackHierarchy.boxStripTablet(10, 10);

        assertThat(hierarchy.toBaseUnits("box", 2)).isEqualTo(200);
        assertThat(hierarchy.toBaseUnits("strip", 3)).isEqualTo(30);
        assertThat(hierarchy.toBaseUnits("tablet", 5)).isEqualTo(5);
        assertThat(hierarchy.toBaseUnits("box", 0)).isEqualTo(0);
    }

    @Test
    @DisplayName("toBaseUnits throws IllegalArgumentException on unknown unit or negative quantity")
    void testToBaseUnitsValidation() {
        PackHierarchy hierarchy = PackHierarchy.boxStripTablet(10, 10);

        assertThatThrownBy(() -> hierarchy.toBaseUnits("vial", 1))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Unknown pack unit");

        assertThatThrownBy(() -> hierarchy.toBaseUnits("strip", -2))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("negative");
    }

    @Test
    @DisplayName("fromBaseUnits breaks down total base units greedily from highest to leaf unit")
    void testFromBaseUnitsBreakdown() {
        PackHierarchy hierarchy = PackHierarchy.boxStripTablet(10, 10);

        // 235 tablets = 2 boxes (200), 3 strips (30), 5 tablets (5)
        Map<String, Integer> breakdown = hierarchy.fromBaseUnits(235);
        assertThat(breakdown)
                .containsEntry("box", 2)
                .containsEntry("strip", 3)
                .containsEntry("tablet", 5);

        // Exact box: 300 tablets = 3 boxes, 0 strips, 0 tablets
        Map<String, Integer> exactBoxes = hierarchy.fromBaseUnits(300);
        assertThat(exactBoxes)
                .containsEntry("box", 3)
                .containsEntry("strip", 0)
                .containsEntry("tablet", 0);
    }
}
