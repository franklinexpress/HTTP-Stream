package com.endpoints.Iservice;

import com.endpoints.domain.User;

/**
 * Created by franklin on 12/3/15.
 */
public interface IUserService {

     User getUser(String username);
     String createUser(User user);
     boolean authenticate(String username, String password);

}
