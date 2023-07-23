package sh.talonfox.pyrofrost.temperature;

public record EnvironmentData(boolean isUnderground, boolean isSheltered, double radiation) {
    public double getRadiation() {
        return radiation;
    }
}
