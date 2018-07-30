package za.org.grassroot2.model.task;

public class LanguageIntent {

    private String name;

    private double confidence;

    @Override
    public String toString() {
        return "LanguageIntent{" +
                "name='" + name + '\'' +
                ", confidence=" + confidence +
                '}';
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getConfidence() {
        return confidence;
    }

    public void setConfidence(double confidence) {
        this.confidence = confidence;
    }
}
