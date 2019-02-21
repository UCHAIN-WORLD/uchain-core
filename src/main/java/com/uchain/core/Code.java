package com.uchain.core;

import com.uchain.common.Serializable;
import com.uchain.common.Serializabler;
import lombok.Getter;
import lombok.Setter;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Arrays;

import static com.uchain.common.Serializabler.readByteArray;

@Setter
@Getter
public class Code implements Serializable {

    byte[] code;

    private byte[] id;

    public Code(byte[] code, byte[] id) {
        this.code = code;
        this.id = id;
    }

    @Override
    public void serialize(DataOutputStream os) {
            try {
                Serializabler.writeByteArray(os, code);
                Serializabler.writeByteArray(os, id);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    public static Code deserialize(DataInputStream is) {
        try {
            byte[] code = readByteArray(is);
            byte[] id = readByteArray(is);
            return new Code(code, id);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

}
