package com.uchain.core.consensus;

import com.uchain.common.Serializable;
import com.uchain.common.Serializabler;
import com.uchain.crypto.UInt256;
import com.uchain.util.Utils;
import lombok.Getter;
import lombok.Setter;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

@Setter
@Getter
public class SwitchState implements Serializable {

    private UInt256 oldHead;
    private  UInt256 newHead;
    private UInt256 forkPoint;
    private int height;

    public SwitchState(UInt256 oldHead,UInt256 newHead,UInt256 forkPoint,int height){
        this.oldHead = oldHead;
        this.newHead = newHead;
        this.forkPoint = forkPoint;
        this.height = height;
    }

    @Override
    public void serialize(DataOutputStream os) {
        Serializabler.write(os,oldHead);
        Serializabler.write(os,newHead);
        Serializabler.write(os,forkPoint);
        try{
        Utils.writeVarint(height,os);
        }catch (IOException e){
            e.printStackTrace();
        }
    }

    public static SwitchState deserialize (DataInputStream is) throws IOException{
        return new SwitchState(Serializabler.readObj(is,"256"),
                Serializabler.readObj(is,"256"),
                Serializabler.readObj(is,"256"),
                Utils.readVarInt(is).intValue());
    }

    public static SwitchState fromBytes(byte[] bytes) throws IOException{
        ByteArrayInputStream bs = new ByteArrayInputStream(bytes);
        DataInputStream is = new DataInputStream(bs);
        return deserialize(is);
    }
}
