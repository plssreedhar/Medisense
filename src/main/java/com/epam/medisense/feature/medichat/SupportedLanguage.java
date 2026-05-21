package com.epam.medisense.feature.medichat;

public enum SupportedLanguage {
    ENGLISH("English"),
    HINDI("Hindi"),
    TAMIL("Tamil"),
    TELUGU("Telugu"),
    KANNADA("Kannada"),
    MALAYALAM("Malayalam"),
    MARATHI("Marathi");

    private final String displayName;

    SupportedLanguage(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public static SupportedLanguage fromString(String value) {
        if (value == null) return ENGLISH;
        for (SupportedLanguage lang : values()) {
            if (lang.displayName.equalsIgnoreCase(value) || lang.name().equalsIgnoreCase(value)) {
                return lang;
            }
        }
        return ENGLISH;
    }
}
