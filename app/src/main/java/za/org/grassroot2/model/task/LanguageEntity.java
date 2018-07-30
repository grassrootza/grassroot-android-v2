package za.org.grassroot2.model.task;

public class LanguageEntity {

    private String entityType;

    private String value;

    private int start;

    private int end;

    @Override
    public String toString() {
        return "LanguageEntity{" +
                "entityType='" + entityType + '\'' +
                ", value='" + value + '\'' +
                ", start=" + start +
                ", end=" + end +
                '}';
    }

    public String getEntityType() {
        return entityType;
    }

    public void setEntityType(String entityType) {
        this.entityType = entityType;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public int getStart() {
        return start;
    }

    public void setStart(int start) {
        this.start = start;
    }

    public int getEnd() {
        return end;
    }

    public void setEnd(int end) {
        this.end = end;
    }
}
