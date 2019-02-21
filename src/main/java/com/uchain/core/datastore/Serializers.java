package com.uchain.core.datastore;


public class Serializers {

   
    public static class Identity<T> implements Serializer<T, T> {
        @Override
        public T serialize(T object) {
            return object;
        }
        @Override
        public T deserialize(T stream) {
            return stream;
        }
    }
}
