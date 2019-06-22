package com.app.thestream.blacklist.model;

import android.graphics.drawable.Drawable;
import android.os.Parcel;
import android.os.Parcelable;

public class BlockedAppModel implements Parcelable {
    private String pkgName;

    public BlockedAppModel(String pkgName, String lable, Drawable appIcon) {
        this.pkgName = pkgName;
        this.lable = lable;
        this.appIcon = appIcon;
    }

    private String lable;

    public String getPkgName() {
        return pkgName;
    }

    public void setPkgName(String pkgName) {
        this.pkgName = pkgName;
    }

    public String getLable() {
        return lable;
    }

    public void setLable(String lable) {
        this.lable = lable;
    }

    public Drawable getAppIcon() {
        return appIcon;
    }

    public void setAppIcon(Drawable appIcon) {
        this.appIcon = appIcon;
    }

    private Drawable appIcon;

    @Override
    public int describeContents() {
        return 0;
    }

    public static Creator<BlockedAppModel> getCREATOR() {
        return CREATOR;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(pkgName);
    }

    protected BlockedAppModel(Parcel in) {
        pkgName = in.readString();
    }

    public static final Creator<BlockedAppModel> CREATOR = new Creator<BlockedAppModel>() {
        @Override
        public BlockedAppModel createFromParcel(Parcel in) {
            return new BlockedAppModel(in);
        }

        @Override
        public BlockedAppModel[] newArray(int size) {
            return new BlockedAppModel[size];
        }
    };
}
