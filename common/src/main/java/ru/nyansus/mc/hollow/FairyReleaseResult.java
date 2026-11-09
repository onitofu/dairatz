package ru.nyansus.mc.hollow;

import ru.nyansus.mc.entity.DairatzEntity;

public record FairyReleaseResult(FairyReleaseStatus status, DairatzEntity fairy) {
    public FairyReleaseResult {
        if ((status == FairyReleaseStatus.RELEASED) != (fairy != null)) {
            throw new IllegalArgumentException("Inconsistent fairy release result");
        }
    }

    public boolean succeeded() {
        return status == FairyReleaseStatus.RELEASED;
    }

    public static FairyReleaseResult released(DairatzEntity fairy) {
        return new FairyReleaseResult(FairyReleaseStatus.RELEASED, fairy);
    }

    public static FairyReleaseResult refused(FairyReleaseStatus status) {
        return new FairyReleaseResult(status, null);
    }
}
