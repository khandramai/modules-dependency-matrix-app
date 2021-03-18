package org.folio.modulesdependencymatrixapp;

import org.folio.modulesdependencymatrixapp.entity.SubVersion;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class SubVersionTest {

    @Test
    void shouldParseVersion(){
        var rawVersion = "1.0";
        SubVersion version = new SubVersion(rawVersion);

        assertEquals(version.getMajor(),1);
        assertEquals(version.getMinor(),0);
    }

    @Test
    void majorIsChangedShouldBeTrue(){
        var rawVersion1 = "1.0";
        var rawVersion2 = "2.0";
        SubVersion version1 = new SubVersion(rawVersion1);
        SubVersion version2 = new SubVersion(rawVersion2);

        assertTrue(version1.isMajorChanged(version2));
    }

    @Test
    void minorIsChangedShouldBeTrue(){
        var rawVersion1 = "1.0";
        var rawVersion2 = "1.1";
        SubVersion version1 = new SubVersion(rawVersion1);
        SubVersion version2 = new SubVersion(rawVersion2);

        assertTrue(version1.isMinorChanged(version2));
    }

    @Test
    void invalidVersionShouldNotFail(){
        var rawVersion = "invalid";
        SubVersion version = new SubVersion(rawVersion);

        assertEquals(version.getMajor(),0);
        assertEquals(version.getMinor(),0);
    }

}
