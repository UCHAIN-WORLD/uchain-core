package com.uchain.storage;

import org.iq80.leveldb.DB;
import org.iq80.leveldb.Options;

import java.io.File;

import static org.iq80.leveldb.impl.Iq80DBFactory.factory;

public class LevelDbConnFacory {
    
    
    private static LevelDbStorage levelDbStorage = null;
    
	private LevelDbConnFacory() {
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