package com.uchain.storage;

import static org.iq80.leveldb.impl.Iq80DBFactory.factory;

import java.io.File;

import org.iq80.leveldb.DB;
import org.iq80.leveldb.Options;

public class ConnFacory {
    
    
    private static LevelDbStorage levelDbStorage = null;
    
	private ConnFacory() {
	}
 
	public static LevelDbStorage getInstance(String dbFile) {
					try {
			        	Options options = new Options();
			        	options.createIfMissing(true);
			        	DB db = factory.open(new File(dbFile), options);
			        	levelDbStorage = new LevelDbStorage(db);
			        } catch (Exception e) {
			            e.printStackTrace();
			        }
		            return levelDbStorage;
	}

}