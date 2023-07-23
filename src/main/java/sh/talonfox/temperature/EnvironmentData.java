package sh.talonfox.temperature;

public record EnvironmentData(boolean isUnderground, boolean isSheltered, double radiation) {
    public double getRadiation() {
        return radiation;
    }

    public String toString() {
        return "isUnderground: " + isUnderground + " isSheltered: " + isSheltered
                + " radiation: " + radiation;
    }
}
