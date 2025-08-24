package model;

public enum FileType {
    OLYMPUS_C180;

    public String toLowerCase() {
        return name().toLowerCase();
    }
}
