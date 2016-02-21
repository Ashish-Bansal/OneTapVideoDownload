package com.phantom.onetapvideodownload.utils;

import android.support.v4.util.SparseArrayCompat;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

public class SerializableSparseArray<T> extends SparseArrayCompat<T> implements Serializable {

    private static final long serialVersionUID = 748242644L;

    public SerializableSparseArray(){
        super();
    }

    public SerializableSparseArray(int capacity){
        super(capacity);
    }

    private void writeObject(ObjectOutputStream oos) throws IOException {
        Object[] data = new  Object[size()];

        for(int i = 0; i < data.length; i++) {
            Object[] pair = { keyAt(i), valueAt(i) };
            data[i] = pair;
        }
        oos.writeObject(data);
    }

    private void readObject(ObjectInputStream ois) throws IOException, ClassNotFoundException {
        Object[] data = (Object[]) ois.readObject();
        for(Object object : data) {
            Object[] pair = (Object[]) object;
            this.append((Integer)pair[0], (T) pair[1]);
        }
    }
}
