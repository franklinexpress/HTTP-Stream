package com.endpoints.guice;

import com.endpoints.Api;
import com.endpoints.Iservice.IPostService;
import com.endpoints.Iservice.IUserService;
import com.endpoints.domain.Post;
import com.endpoints.service.PostService;
import com.endpoints.service.UserService;
import com.google.inject.AbstractModule;

/**
 * Created by franklin on 12/3/15.
 */
public class ServiceModule extends AbstractModule {

    @Override
    protected void configure() {
        bind(IUserService.class).to(UserService.class);
        bind(IPostService.class).to(PostService.class);
        requestStaticInjection(Api.class);
    }
}
