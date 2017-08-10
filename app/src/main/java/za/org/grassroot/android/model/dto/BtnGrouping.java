package za.org.grassroot.android.model.dto;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * Created by luke on 2017/08/10.
 * for now, UX determines only two submenus -- in future may want to change, but don't want to overload now
 */

public class BtnGrouping implements Parcelable {

    BtnParameters[] mainButtons;
    BtnParameters[] firstSubMenu;
    BtnParameters[] secondSubMenu;

    public BtnGrouping(BtnParameters[] mainButtons, BtnParameters[] firstSubMenu, BtnParameters[] secondSubMenu) {
        this.mainButtons = mainButtons;
        this.firstSubMenu = firstSubMenu;
        this.secondSubMenu = secondSubMenu;
    }

    protected BtnGrouping(Parcel in) {
        mainButtons = in.createTypedArray(BtnParameters.CREATOR);
        firstSubMenu = in.createTypedArray(BtnParameters.CREATOR);
        secondSubMenu = in.createTypedArray(BtnParameters.CREATOR);
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


}
