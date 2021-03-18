package org.folio.modulesdependencymatrixapp.entity;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.ArrayList;
import java.util.List;

@Setter
@Getter
@ToString
public class Version {

    private List<SubVersion> versions;

    public Version(String version) {
        versions = new ArrayList<>();
        var rawVersions = version.split(" ");

        for (String rawVersion : rawVersions) {
            versions.add(new SubVersion(rawVersion));
        }
    }

    public boolean isMajorChanged(Version someVersion) {
        if (!isMultiple() && !someVersion.isMultiple()) {
            return versions.get(0).isMajorChanged(someVersion.getVersions().get(0));
        } else if (!isMultiple() && someVersion.isMultiple()) {
            return isMajorChangedForOneMultiple(this, someVersion);
        } else if (isMultiple() && !someVersion.isMultiple()) {
            return isMajorChangedForOneMultiple(someVersion, this);
        } else {
            return isMajorChangedForTwoMultiple(this, someVersion);
        }
    }

    public boolean isMinorChanged(Version someVersion) {
        if (!isMultiple() && !someVersion.isMultiple() && !isMajorChanged(someVersion)) {
            return versions.get(0).isMinorChanged(someVersion.getVersions().get(0));
        } else if (!isMultiple() && someVersion.isMultiple()) {
            return isMinorChangedForOneMultiple(this, someVersion);
        } else if (isMultiple() && !someVersion.isMultiple()) {
            return isMinorChangedForOneMultiple(someVersion, this);
        } else if (isMultiple() && someVersion.isMultiple()) {
            return isMinorChangedForTwoMultiple(this, someVersion);
        } else {
            return false;
        }
    }

    private boolean isMultiple() {
        return versions.size() > 1;
    }

    private static boolean isMajorChangedForOneMultiple(Version singleVersion, Version manyVersions) {
        var singleSubVersion = singleVersion.getVersions().get(0);
        for (SubVersion someSubVersion : manyVersions.getVersions()) {
            if (!singleSubVersion.isMajorChanged(someSubVersion)) {
                return false;
            }
        }
        return true;
    }

    private static boolean isMinorChangedForOneMultiple(Version singleVersion, Version manyVersions) {
        var singleSubVersion = singleVersion.getVersions().get(0);

        if (isMajorChangedForOneMultiple(singleVersion, manyVersions)) {
            return false;
        }

        for (SubVersion someSubVersion : manyVersions.getVersions()) {
            if (!singleSubVersion.isMajorChanged(someSubVersion)) {
                if (!singleSubVersion.isMinorChanged(someSubVersion)) {
                    return false;
                }
            }
        }
        return true;
    }

    private static boolean isMajorChangedForTwoMultiple(Version version1, Version version2) {
        var subVersions1 = version1.getVersions();
        var subVersions2 = version2.getVersions();

        for (SubVersion subVersion1 : subVersions1) {
            for (SubVersion subVersion2 : subVersions2) {
                if (!subVersion1.isMajorChanged(subVersion2)) {
                    return false;
                }
            }
        }
        return true;
    }

    private static boolean isMinorChangedForTwoMultiple(Version version1, Version version2) {
        var subVersions1 = version1.getVersions();
        var subVersions2 = version2.getVersions();

        if (isMajorChangedForTwoMultiple(version1, version2)) {
            return false;
        }

        for (SubVersion subVersion1 : subVersions1) {
            for (SubVersion subVersion2 : subVersions2) {
                if (!subVersion1.isMajorChanged(subVersion2)) {
                    if (!subVersion1.isMinorChanged(subVersion2)) {
                        return false;
                    }
                }
            }
        }
        return true;
    }
}
