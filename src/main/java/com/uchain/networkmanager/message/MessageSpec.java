package com.uchain.networkmanager.message;

public interface MessageSpec<T> {

    byte messageCode = 0;
    String messageName = "";


    public byte[] serialize(T object);


    public T deserialize(byte[] stream);
}


class SyncInfoMessageSpec<SyncInfo> implements MessageSpec<SyncInfo> {
    byte messageCode = 55;
    String messageName = "Inv";

    @Override
    public byte[] serialize(SyncInfo object) {
        return new byte[0];
    }

    @Override
    public SyncInfo deserialize(byte[] stream) {
        return null;
    }
}

class InvSpec implements MessageSpec {
    byte messageCode = 65;

    String messageName = "Sync";

    int maxInvObjects;

    public InvSpec(int maxInvObjects) {
        this.maxInvObjects = maxInvObjects;
    }

    @Override
    public byte[] serialize(Object object) {
        return new byte[0];
    }

    @Override
    public Object deserialize(byte[] stream) {
        return null;
    }
}

class RequestModifierSpec implements MessageSpec {
    byte messageCode = 22;

    String messageName = "RequestModifier";

    int maxInvObjects;
    InvSpec invSpec;

    public RequestModifierSpec(int maxInvObjects) {
        this.maxInvObjects = maxInvObjects;
        this.invSpec = new InvSpec(maxInvObjects);
    }

    @Override
    public byte[] serialize(Object object) {
        return new byte[0];
    }

    @Override
    public Object deserialize(byte[] stream) {
        return null;
    }

    class ModifiersSpec implements MessageSpec {
        byte messageCode = 33;

        String messageName = "Modifier";

        int maxMessageSize;

        public ModifiersSpec(int maxMessageSize) {
            this.maxMessageSize = maxMessageSize;
        }

        @Override
        public byte[] serialize(Object object) {
            return new byte[0];
        }

        @Override
        public Object deserialize(byte[] stream) {
            return null;
        }
    }

    class GetPeersSpec<Integer> implements MessageSpec<Integer> {

        byte messageCode = 1;

        String messageName = "GetPeers message";

        @Override
        public byte[] serialize(Integer object) {
            return new byte[0];
        }

        @Override
        public Integer deserialize(byte[] stream) {
            return null;
        }
    }

    class PeersSpec<List> implements MessageSpec<List> {
        private int AddressLength = 4;
        private int PortLength = 4;
        private int DataLength = 4;

        byte messageCode = 2;

        String messageName = "Peers message";


        @Override
        public byte[] serialize(List object) {
            return new byte[0];
        }

        @Override
        public List deserialize(byte[] stream) {
            return null;
        }
    }
}