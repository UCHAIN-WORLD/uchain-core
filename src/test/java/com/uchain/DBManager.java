package com.uchain;

import com.uchain.storage.LevelDbStorage;
import org.apache.commons.collections.map.HashedMap;

import java.io.File;
import java.util.Map;

public class DBManager {
    private static final Map<String, LevelDbStorage> dbs = new HashedMap();
    private static String testClass;
    private final static Map<String, DBManager> dbManagers = new HashedMap();

    public DBManager(String testClass){
        this.testClass = testClass;
    }

    public static LevelDbStorage openDB(String dir){
        if (!dbs.containsKey(dir)) {
            LevelDbStorage db = LevelDbStorage.open(dir);
            dbs.put(dir, db);
        }
        return dbs.get(dir);
    }

    public static void cleanUp() {
        for(Map.Entry db : dbs.entrySet()){
            ((LevelDbStorage)db.getValue()).close();
        }
        deleteDir(testClass);
    }

    public static void deleteDir(String dir){
        try {
            File scFileDir = new File(dir);
            File TrxFiles[] = scFileDir.listFiles();
            for(File curFile:TrxFiles ){
                curFile.delete();
            }
            scFileDir.delete();
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    public static LevelDbStorage open(String testClass, String dir) {
        if (!dbManagers.containsKey(testClass)) {
            DBManager dbMgr = new DBManager(testClass);
            dbManagers.put(testClass, dbMgr);
        }
        return dbManagers.get(testClass).openDB(testClass+'/'+dir);
    }

    public static void close(String testClass, String dir ) {
        DBManager dbMgr = dbManagers.get(testClass);

        if (dbMgr.dbs.containsKey(dir)) {
            dbMgr.dbs.get(dir).close();
            dbMgr.dbs.remove(dir);
        }
    }

    public static void clearUp(String testClass) {
        DBManager dbMgr = dbManagers.get(testClass);
        dbMgr.cleanUp();
        dbManagers.remove(testClass);
    }
}
