package za.org.grassroot.android.model.dto;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by luke on 2017/08/10.
 * for now, UX determines only two submenus -- in future may want to change, but don't want to overload now
 */

public class BtnGrouping implements Parcelable {

    public static final String BUTTON_GROUP_DETAILS = "BUTTON_GROUP_DETAILS";

    private final BtnParameters[] mainButtons;
    private final BtnParameters[] firstSubMenu;
    private final BtnParameters[] secondSubMenu;

    private BtnGrouping(Builder builder) {
        mainButtons = builder.mainButtons;
        firstSubMenu = builder.firstSubMenu;
        secondSubMenu = builder.secondSubMenu;
    }

    protected BtnGrouping(Parcel in) {
        mainButtons = in.createTypedArray(BtnParameters.CREATOR);
        firstSubMenu = in.createTypedArray(BtnParameters.CREATOR);
        secondSubMenu = in.createTypedArray(BtnParameters.CREATOR);
    }

    public static Builder builder() {
        return new Builder();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeTypedArray(mainButtons, flags);
        dest.writeTypedArray(firstSubMenu, flags);
        dest.writeTypedArray(secondSubMenu, flags);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<BtnGrouping> CREATOR = new Creator<BtnGrouping>() {
        @Override
        public BtnGrouping createFromParcel(Parcel in) {
            return new BtnGrouping(in);
        }

        @Override
        public BtnGrouping[] newArray(int size) {
            return new BtnGrouping[size];
        }
    };

    public BtnParameters[] getMainButtons() {
        return mainButtons;
    }

    public BtnParameters[] getFirstSubMenu() {
        return firstSubMenu;
    }

    public BtnParameters[] getSecondSubMenu() {
        return secondSubMenu;
    }


    public static final class Builder {
        private BtnParameters[] mainButtons;
        private BtnParameters[] firstSubMenu;
        private BtnParameters[] secondSubMenu;

        private Builder() {
        }

        public Builder mainButtons(BtnParameters[] val) {
            mainButtons = val;
            return this;
        }

        public Builder firstSubMenu(BtnParameters[] val) {
            firstSubMenu = val;
            return this;
        }

        public Builder secondSubMenu(BtnParameters[] val) {
            secondSubMenu = val;
            return this;
        }

        public BtnGrouping build() {
            return new BtnGrouping(this);
        }
    }
}
