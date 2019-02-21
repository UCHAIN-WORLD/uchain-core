package com.uchain.core.datastore;

public interface Serializer<T, S> {
    S serialize(T object);
    T deserialize(S stream);
}
