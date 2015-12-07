package com.endpoints.util;

import com.google.gson.Gson;
import spark.ResponseTransformer;

/**
 * Created by franklin on 12/1/15.
 */
public class JsonUtil {

    public static String toJson(Object object) {
        return new Gson().toJson(object);
    }

    public static <T> T fromJson(String jsonString, Class<T> objectClass) {
        return new Gson().fromJson(jsonString, objectClass);
    }

    public static ResponseTransformer json() {
      return JsonUtil::toJson;

    }
}
