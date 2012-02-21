package bratseth.maja.androidtest.service.aidl;

import java.io.Serializable;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * @author Maja S Bratseth
 */
public class AidlCustomer implements Parcelable {
    
    private AidlCustomerId id;
    private String name;

    public static final Parcelable.Creator<AidlCustomer> CREATOR = new
        Parcelable.Creator<AidlCustomer>() {
            public AidlCustomer createFromParcel(Parcel in) {
                return new AidlCustomer(in);
            }

            public AidlCustomer[] newArray(int size) {
                return new AidlCustomer[size];
            }
        };
    
    public AidlCustomer(AidlCustomerId id, String name) {
        this.id = id;
        this.name = name;
    }
    
    public AidlCustomer(Parcel in) {
        readFromParcel(in);
    }

    public AidlCustomerId getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int flag) {
        parcel.writeParcelable(id, 0);
        parcel.writeString(name);
    }

    public void readFromParcel(Parcel in) {
        id = in.readParcelable(getClass().getClassLoader());
        name = in.readString();
    }
    
}
