package com.uchain.vm.repository;

import com.uchain.core.consensus.ForkBase;
import com.uchain.core.datastore.BlockBase;
import com.uchain.core.datastore.DataBase;

public class RepositoryRoot extends RepositoryImpl{

    public RepositoryRoot(ForkBase forkBase, DataBase dataBase, BlockBase blockBase){
        super(forkBase, dataBase, blockBase);
    }
}
