package com.example.daniel.todule_android.parcelable;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.v4.util.LongSparseArray;

/**
 * Created by danieL on 11/14/2017.
 */

public class LongSparseArrayBooleanParcelable extends LongSparseArray<Boolean> implements Parcelable {

    public LongSparseArrayBooleanParcelable(){
        // Normal actions performed by class, since this is still a normal object!
    }

    public LongSparseArrayBooleanParcelable(LongSparseArray<Boolean> longSparseBooleanArray){
        for(int i=0; i<longSparseBooleanArray.size(); i++){
            this.put(longSparseBooleanArray.keyAt(i), longSparseBooleanArray.valueAt(i));
        }
    }

    public static final Parcelable.Creator<LongSparseArrayBooleanParcelable> CREATOR
            = new Parcelable.Creator<LongSparseArrayBooleanParcelable>(){
        @Override
        public LongSparseArrayBooleanParcelable createFromParcel(Parcel parcel) {
            LongSparseArrayBooleanParcelable read = new LongSparseArrayBooleanParcelable();
            int size = parcel.readInt();
            long[] keys = new long[size];
            boolean[] values = new boolean[size];

            parcel.readLongArray(keys);
            parcel.readBooleanArray(values);

            for (int i=0; i<size; i++){
                read.put(keys[i], values[i]);
            }

            return read;
        }

        @Override
        public LongSparseArrayBooleanParcelable[] newArray(int i) {
            return new LongSparseArrayBooleanParcelable[i];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        long[] keys = new long[size()];
        boolean[] values = new boolean[size()];

        for (int k = 0; k < size(); k++){
            keys[k] = keyAt(k);
            values[k] = valueAt(k);
        }

        parcel.writeInt(size());
        parcel.writeLongArray(keys);
        parcel.writeBooleanArray(values);
    }
}
