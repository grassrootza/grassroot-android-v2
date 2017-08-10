package za.org.grassroot.android.model.dto;

import android.os.Parcel;
import android.os.Parcelable;

import io.reactivex.annotations.NonNull;

/**
 * Created by luke on 2017/08/10.
 */

public class BtnParameters implements Parcelable {

    private final String name;
    private final int drawableRes;
    private final int labelRes;

    public BtnParameters(@NonNull String name,
                         int drawableRes, int labelRes) {
        this.name = name;
        this.drawableRes = drawableRes;
        this.labelRes = labelRes;
    }

    protected BtnParameters(Parcel in) {
        name = in.readString();
        drawableRes = in.readInt();
        labelRes = in.readInt();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(name);
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
}
