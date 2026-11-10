package ru.nyansus.mc.hollow;

import ru.nyansus.mc.entity.HousedFairyEntity;

public record FairyReleaseResult(FairyReleaseStatus status, HousedFairyEntity fairy) {
    public FairyReleaseResult {
        if ((status == FairyReleaseStatus.RELEASED) != (fairy != null)) {
            throw new IllegalArgumentException("Inconsistent fairy release result");
        }
    }

    public boolean succeeded() {
        return status == FairyReleaseStatus.RELEASED;
    }

    public static FairyReleaseResult released(HousedFairyEntity fairy) {
        return new FairyReleaseResult(FairyReleaseStatus.RELEASED, fairy);
    }

    public static FairyReleaseResult refused(FairyReleaseStatus status) {
        return new FairyReleaseResult(status, null);
    }
}
