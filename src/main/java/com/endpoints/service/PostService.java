package com.endpoints.service;

import com.endpoints.Iservice.IPostService;
import com.endpoints.domain.Post;
import com.endpoints.domain.User;
import com.mongodb.MongoClient;
import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.Morphia;
import org.mongodb.morphia.query.Query;
import org.mongodb.morphia.query.UpdateOperations;

import java.util.List;

/**
 * Created by franklin on 12/2/15.
 */
public class PostService implements IPostService {

    MongoClient client = new MongoClient("localhost", 27017);
    Datastore datastore = new Morphia().createDatastore(client, "users");

    UpdateOperations<User> ops;


    public User create(Post post, User user) {
        int id = (user.getPosts().size()) + 1;
        post.setId(id);

        Query<User> updateQuery = datastore.createQuery(User.class).field("_id").equal(user.getId());
        ops = datastore.createUpdateOperations(User.class).add("posts", post);
        datastore.update(updateQuery, ops);

        return user;
    }

    public List<Post> getPosts(User user) {
        return user.getPosts();
    }

    public Post getPost(int id, User user) {
       return user.getPosts().get(id - 1);

    }
}
