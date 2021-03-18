package org.folio.modulesdependencymatrixapp;

import org.folio.modulesdependencymatrixapp.entity.Version;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class VersionTest {

    @Test
    void shouldParseAllVersions() {
        var rawVersions = "1.0 2.0 3.0";
        var version = new Version(rawVersions);
        var expectedMajorVersions = new int[]{1, 2, 3};
        var expectedMinorVersions = new int[]{0, 0, 0};

        for (int i = 0; i < version.getVersions().size(); i++) {
            assertEquals(version.getVersions().get(i).getMajor(), expectedMajorVersions[i]);
            assertEquals(version.getVersions().get(i).getMinor(), expectedMinorVersions[i]);
        }
        assertEquals(version.getVersions().size(), 3);
    }

    @Test
    void isMajorChangedShouldBeTrueWithTwoSingles() {
        var rawVersion1 = "1.0";
        var rawVersion2 = "2.0";

        var version1 = new Version(rawVersion1);
        var version2 = new Version(rawVersion2);

        assertTrue(version1.isMajorChanged(version2));
        assertTrue(version2.isMajorChanged(version1));
    }

    @Test
    void isMajorChangedShouldBeFalseWithTwoSingles() {
        var rawVersion1 = "1.0";
        var rawVersion2 = "1.0";

        var version1 = new Version(rawVersion1);
        var version2 = new Version(rawVersion2);

        assertFalse(version1.isMajorChanged(version2));
        assertFalse(version2.isMajorChanged(version1));
    }

    @Test
    void isMajorChangedShouldBeTrueWithOneSingleAndOneMultiple() {
        var rawVersion1 = "1.0 2.0";
        var rawVersion2 = "3.0";

        var version1 = new Version(rawVersion1);
        var version2 = new Version(rawVersion2);

        assertTrue(version1.isMajorChanged(version2));
        assertTrue(version2.isMajorChanged(version1));
    }

    @Test
    void isMajorChangedShouldBeFalseWithOneSingleAndOneMultiple() {
        var rawVersion1 = "1.0 2.0";
        var rawVersion2 = "2.0";

        var version1 = new Version(rawVersion1);
        var version2 = new Version(rawVersion2);

        assertFalse(version1.isMajorChanged(version2));
        assertFalse(version2.isMajorChanged(version1));
    }

    @Test
    void isMajorChangedShouldBeTrueWithTwoMultiples() {
        var rawVersion1 = "1.0 2.0";
        var rawVersion2 = "3.0 4.0";

        var version1 = new Version(rawVersion1);
        var version2 = new Version(rawVersion2);

        assertTrue(version1.isMajorChanged(version2));
        assertTrue(version2.isMajorChanged(version1));
    }

    @Test
    void isMajorChangedShouldBeFalseWithTwoMultiples() {
        var rawVersion1 = "1.0 2.0";
        var rawVersion2 = "2.0 3.0";

        var version1 = new Version(rawVersion1);
        var version2 = new Version(rawVersion2);

        assertFalse(version1.isMajorChanged(version2));
        assertFalse(version2.isMajorChanged(version1));
    }

    @Test
    void isMinorChangedShouldBeTrueWithTwoSingles() {
        var rawVersion1 = "1.0";
        var rawVersion2 = "1.1";

        var version1 = new Version(rawVersion1);
        var version2 = new Version(rawVersion2);

        assertTrue(version1.isMinorChanged(version2));
        assertTrue(version2.isMinorChanged(version1));
    }

    @Test
    void isMinorChangedShouldBeFalseWithTwoSingles() {
        var rawVersion1 = "1.0";
        var rawVersion2 = "1.0";

        var version1 = new Version(rawVersion1);
        var version2 = new Version(rawVersion2);

        assertFalse(version1.isMinorChanged(version2));
        assertFalse(version2.isMinorChanged(version1));
    }

    @Test
    void isMinorChangedShouldBeTrueWithOneSingleAndOneMultiple() {
        var rawVersion1 = "1.0 2.0";
        var rawVersion2 = "2.1";

        var version1 = new Version(rawVersion1);
        var version2 = new Version(rawVersion2);

        assertTrue(version1.isMinorChanged(version2));
        assertTrue(version2.isMinorChanged(version1));
    }

    @Test
    void isMinorChangedShouldBeFalseWithOneSingleAndOneMultiple() {
        var rawVersion1 = "1.0 2.0";
        var rawVersion2 = "2.0";

        var version1 = new Version(rawVersion1);
        var version2 = new Version(rawVersion2);

        assertFalse(version1.isMinorChanged(version2));
        assertFalse(version2.isMinorChanged(version1));
    }

    @Test
    void isMinorChangedShouldBeTrueWithTwoMultiples() {
        var rawVersion1 = "1.0 2.0";
        var rawVersion2 = "2.1 4.0";

        var version1 = new Version(rawVersion1);
        var version2 = new Version(rawVersion2);

        assertTrue(version1.isMinorChanged(version2));
        assertTrue(version2.isMinorChanged(version1));
    }

    @Test
    void isMinorChangedShouldBeFalseWithTwoMultiples() {
        var rawVersion1 = "1.0 2.0";
        var rawVersion2 = "2.0 3.0";

        var version1 = new Version(rawVersion1);
        var version2 = new Version(rawVersion2);

        assertFalse(version1.isMinorChanged(version2));
        assertFalse(version2.isMinorChanged(version1));
    }

    @Test
    void isMinorChangedShouldBeFalseWithTwoMultiples2() {
        var rawVersion1 = "1.0 2.0";
        var rawVersion2 = "3.1 4.0";

        var version1 = new Version(rawVersion1);
        var version2 = new Version(rawVersion2);

        assertFalse(version1.isMinorChanged(version2));
        assertFalse(version2.isMinorChanged(version1));
    }

    @Test
    void isMinorChangedShouldBeFalseWithOneSingleAndOneMultiple2() {
        var rawVersion1 = "1.0 2.0";
        var rawVersion2 = "3.1";

        var version1 = new Version(rawVersion1);
        var version2 = new Version(rawVersion2);

        assertFalse(version1.isMinorChanged(version2));
        assertFalse(version2.isMinorChanged(version1));
    }

    @Test
    void isMinorChangedShouldBeFalseWithTwoSingles2() {
        var rawVersion1 = "1.0";
        var rawVersion2 = "2.1";

        var version1 = new Version(rawVersion1);
        var version2 = new Version(rawVersion2);

        assertFalse(version1.isMinorChanged(version2));
        assertFalse(version2.isMinorChanged(version1));
    }


}
