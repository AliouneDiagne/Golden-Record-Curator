package it.aliounediagne.epicode.goldenrecord.manifest;

public class ManifestRecord {

    private final String id;
    private final String parentId;
    private final String rawType;
    private final String title;
    private final String durationSeconds;
    private final String capacitySeconds;
    private final String languageCode;
    private final String languageFamily;
    private final String era;
    private final String region;
    private final String detail;
    private final String extra;

    public ManifestRecord(String id, String parentId, String rawType, String title,
            String durationSeconds, String capacitySeconds, String languageCode,
            String languageFamily, String era, String region, String detail, String extra) {
        this.id = id;
        this.parentId = parentId;
        this.rawType = rawType;
        this.title = title;
        this.durationSeconds = durationSeconds;
        this.capacitySeconds = capacitySeconds;
        this.languageCode = languageCode;
        this.languageFamily = languageFamily;
        this.era = era;
        this.region = region;
        this.detail = detail;
        this.extra = extra;
    }

    public String getId() {
        return id;
    }

    public String getParentId() {
        return parentId;
    }

    public String getRawType() {
        return rawType;
    }

    public String getTitle() {
        return title;
    }

    public String getDetail() {
        return detail;
    }

    public String getExtra() {
        return extra;
    }

    public String getLanguageCode() {
        return languageCode;
    }

    public String getLanguageFamily() {
        return languageFamily;
    }

    public String getEra() {
        return era;
    }

    public String getRegion() {
        return region;
    }


    public int getDurationSeconds() {
        return parseIntOrDefault(durationSeconds, 0);
    }


    public Integer getCapacitySeconds() {
        if (capacitySeconds == null || capacitySeconds.trim().isEmpty()) {
            return null;
        }
        return parseIntOrDefault(capacitySeconds, 0);
    }


    private static int parseIntOrDefault(String raw, int fallback) {
        if (raw == null || raw.trim().isEmpty()) {
            return fallback;
        }
        try {
            return Integer.parseInt(raw.trim());
        } catch (NumberFormatException ex) {
            return fallback;
        }
    }
}
