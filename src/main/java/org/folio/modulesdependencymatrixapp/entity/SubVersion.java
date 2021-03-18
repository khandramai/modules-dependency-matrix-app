package org.folio.modulesdependencymatrixapp.entity;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class SubVersion {

    private int major;
    private int minor;

    public SubVersion(String version) {
        var splitVersion = version.split("\\.");

        if (splitVersion.length < 2) {
            major = 0;
            minor = 0;
            return;
        }

        major = parseInt(splitVersion[0]);
        minor = parseInt(splitVersion[1]);
    }

    public boolean isMajorChanged(SubVersion subVersion) {
        return major != subVersion.getMajor();
    }

    public boolean isMinorChanged(SubVersion subVersion) {
        return minor != subVersion.getMinor();
    }

    public boolean isVersionChanged(SubVersion subVersion) {
        return isMajorChanged(subVersion) || isMinorChanged(subVersion);
    }

    private static int parseInt(String integer) {
        try {
            return Integer.parseInt(integer);
        } catch (NumberFormatException e) {
            return 0;
        }
    }
}
