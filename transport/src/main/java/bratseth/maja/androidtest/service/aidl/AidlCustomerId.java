package bratseth.maja.androidtest.service.aidl;

import java.io.Serializable;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * @author Maja S Bratseth
 */
public class AidlCustomerId implements Parcelable {

    private String id;

    public static final Parcelable.Creator<AidlCustomerId> CREATOR = new
        Parcelable.Creator<AidlCustomerId>() {
            public AidlCustomerId createFromParcel(Parcel in) {
                return new AidlCustomerId(in);
            }

            public AidlCustomerId[] newArray(int size) {
                return new AidlCustomerId[size];
            }
        };

    public void readFromParcel(Parcel in) {
        id = in.readString();
    }


    public AidlCustomerId(String id) {
        this.id = id;
    }

    public AidlCustomerId(Parcel in) {
        readFromParcel(in);
    }

    public String getId() {
        return id;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        AidlCustomerId that = (AidlCustomerId) o;
        return id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "@" + System.identityHashCode(this) + "{" + id + "}";
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(id);
    }
}
