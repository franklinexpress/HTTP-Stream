package com.endpoints.service;

import com.endpoints.Iservice.IUserService;
import com.endpoints.domain.User;
import com.mongodb.MongoClient;
import org.mindrot.jbcrypt.BCrypt;
import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.Morphia;

/**
 * Created by franklin on 12/1/15.
 */
public class UserService implements IUserService {

    MongoClient client = new MongoClient("localhost", 27017);
    Datastore datastore = new Morphia().createDatastore(client, "users");

    public User getUser(String username) {

        User user = datastore.find(User.class, "username", username).get();
        if(user != null) {
            return user;
        } else {
            return null;
        }
    }

    public String createUser(User user) {
        datastore.save(user);
        return "User created";
    }

    public boolean authenticate(String username, String password) {
        User user = getUser(username);
        if(user != null && BCrypt.checkpw(password, user.getPassword())) {
            return true;

        }
        return false;
    }

}
