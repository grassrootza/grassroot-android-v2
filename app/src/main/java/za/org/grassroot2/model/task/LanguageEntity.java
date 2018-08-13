package za.org.grassroot2.model.task;

import android.os.Parcel;
import android.os.Parcelable;

public class LanguageEntity implements Parcelable {

    private String entity;

    private String value;

    private int start;

    private int end;

    protected LanguageEntity(Parcel in) {
        entity = in.readString();
        value = in.readString();
        start = in.readInt();
        end = in.readInt();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(entity);
        dest.writeString(value);
        dest.writeInt(start);
        dest.writeInt(end);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<LanguageEntity> CREATOR = new Creator<LanguageEntity>() {
        @Override
        public LanguageEntity createFromParcel(Parcel in) {
            return new LanguageEntity(in);
        }

        @Override
        public LanguageEntity[] newArray(int size) {
            return new LanguageEntity[size];
        }
    };

    @Override
    public String toString() {
        return "LanguageEntity{" +
                "entityType='" + entity + '\'' +
                ", value='" + value + '\'' +
                ", start=" + start +
                ", end=" + end +
                '}';
    }

    public String getEntityType() {
        return entity;
    }

    public void setEntityType(String entityType) {
        this.entity = entityType;
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
