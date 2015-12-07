package com.endpoints.Iservice;

import com.endpoints.domain.Post;
import com.endpoints.domain.User;

import java.util.List;

/**
 * Created by franklin on 12/3/15.
 */
public interface IPostService {
    User create(Post post, User user);
    List<Post> getPosts(User user);
    Post getPost(int id, User user);

}
