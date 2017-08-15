package za.org.grassroot.android.model.dto;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;


/**
 * Created by luke on 2017/08/10.
 */

public class BtnParameters implements Parcelable {

    private final String name;
    private final int actionCode;
    private final int drawableRes;
    private final int labelRes;

    public BtnParameters(@NonNull String name,
                         int actionCode,
                         int drawableRes,
                         int labelRes) {
        this.name = name;
        this.actionCode = actionCode;
        this.drawableRes = drawableRes;
        this.labelRes = labelRes;
    }

    protected BtnParameters(Parcel in) {
        name = in.readString();
        actionCode = in.readInt();
        drawableRes = in.readInt();
        labelRes = in.readInt();
    }

    private BtnParameters(Builder builder) {
        name = builder.name;
        actionCode = builder.actionCode;
        drawableRes = builder.drawableRes;
        labelRes = builder.labelRes;
    }

    public static Builder builder() {
        return new Builder();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(name);
        dest.writeInt(actionCode);
        dest.writeInt(drawableRes);
        dest.writeInt(labelRes);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<BtnParameters> CREATOR = new Creator<BtnParameters>() {
        @Override
        public BtnParameters createFromParcel(Parcel in) {
            return new BtnParameters(in);
        }

        @Override
        public BtnParameters[] newArray(int size) {
            return new BtnParameters[size];
        }
    };

    public String getName() {
        return name;
    }

    public int getDrawableRes() {
        return drawableRes;
    }

    public int getLabelRes() {
        return labelRes;
    }

    public int getActionCode() {
        return actionCode;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        BtnParameters that = (BtnParameters) o;

        return name.equals(that.name);

    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }

    public static final class Builder {
        private String name;
        private int actionCode;
        private int drawableRes;
        private int labelRes;

        private Builder() {
        }

        @NonNull
        public Builder name(@NonNull String val) {
            name = val;
            return this;
        }

        @NonNull
        public Builder actionCode(int val) {
            actionCode = val;
            return this;
        }

        @NonNull
        public Builder drawableRes(int val) {
            drawableRes = val;
            return this;
        }

        @NonNull
        public Builder labelRes(int val) {
            labelRes = val;
            return this;
        }

        @NonNull
        public BtnParameters build() {
            return new BtnParameters(this);
        }
    }
}
