package com.endpoints.util;

import com.mongodb.DB;
import com.mongodb.MongoClient;
import com.mongodb.MongoCredential;

/**
 * Created by franklin on 12/3/15.
 */
public class MongoConnector {

    private MongoClient mongo;
    private DB db;
    private MongoCredential credential;

    public MongoConnector() {
        this.mongo = new MongoClient("localhost", 27017);
        this.db = mongo.getDB("myvideos");
        this.credential = MongoCredential.createCredential("admin", "myvideos", "password".toCharArray());

    }

    public MongoClient getMongo() {
        return mongo;
    }

    public void setMongo(MongoClient mongo) {
        this.mongo = mongo;
    }

    public DB getDb() {
        return db;
    }

    public void setDb(DB db) {
        this.db = db;
    }

    public MongoCredential getCredential() {
        return credential;
    }

    public void setCredential(MongoCredential credential) {
        this.credential = credential;
    }
}
