package com.endpoints; /**
 * Created by franklin on 12/1/15.
 */

import com.endpoints.Iservice.IPostService;
import com.endpoints.Iservice.IUserService;
import com.endpoints.domain.Post;
import com.endpoints.domain.User;
import com.endpoints.guice.ServiceModule;
import com.endpoints.service.PostService;
import com.endpoints.service.UserService;
import com.endpoints.util.*;
import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.MongoClient;
import com.mongodb.gridfs.GridFS;
import com.mongodb.gridfs.GridFSDBFile;
import com.mongodb.gridfs.GridFSInputFile;
import org.bson.types.ObjectId;
import org.mindrot.jbcrypt.BCrypt;
import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.Morphia;
import spark.utils.IOUtils;


import javax.servlet.MultipartConfigElement;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.Part;
import javax.xml.bind.DatatypeConverter;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import static com.endpoints.util.JsonUtil.fromJson;
import static com.endpoints.util.JsonUtil.json;
import static spark.Spark.*;

public class Api {

    private static MongoConnector mongoDb = new MongoConnector();

    @Inject
    static IUserService userService;

    @Inject
    static IPostService postService;


    public static void main(String[] args) {

        Injector injector = Guice.createInjector(new ServiceModule());

        DB db = mongoDb.getDb();
        DBCollection collection = db.getCollection("myvideos.files");

        before("/api/*", (req, res) -> {
           String authorization = req.headers("Authorization");

            if(authorization != null && authorization.startsWith("Basic")) {
                String credentials = authorization.substring("Basic".length()).trim();
                byte[] decoded = DatatypeConverter.parseBase64Binary(credentials);
                String decodedString = new String(decoded);
                System.out.println(decodedString);
                String[] actualCredentials = decodedString.split(":");
                String username = actualCredentials[0];
                String password = actualCredentials[1];

                if(!userService.authenticate(username, password)) {
                    halt(401, "Sorry, not authorized :)");
                }

            } else {
                halt(401, "Not authorized!");
            }
        });


        post("/api/:username/videos/new", (req, res) -> {

            MultipartConfigElement multipartConfigElement = new MultipartConfigElement("/tmp");
            req.raw().setAttribute("org.eclipse.jetty.multipartConfig", multipartConfigElement);

            String username = req.params(":username");

            InputStream inputStream = req.raw().getPart("file").getInputStream();
            Part uploadFile = req.raw().getPart("file");

            GridFS gridFS = new GridFS(db, "myvideos");

            GridFSInputFile gfsFile = gridFS.createFile(inputStream);
            gfsFile.put("username", username);
            gfsFile.put("contentType", req.raw().getContentType());
            gfsFile.put("filename", uploadFile.getSubmittedFileName());
            collection.insert(gfsFile);

            gfsFile.save();

            return 201;


        });

        get("/api/:username/videos", (req, res) -> {
           res.type("application/json");
            String username = req.params(":username");
            BasicDBObject query = new BasicDBObject("username", username);

            GridFS gridFS = new GridFS(db, "myvideos");
            List<GridFSDBFile> files = new ArrayList<GridFSDBFile>();

            files = gridFS.find(query);

            return files;

        });

        get("/:username/videos/:videoId", (req, res) -> {
            BasicDBObject query = new BasicDBObject();
            String id = req.params(":videoId");
            ObjectId videoId = new ObjectId(id);
            query.put("_id", videoId);

            GridFS gridFS = new GridFS(db, "myvideos");
            GridFSDBFile gridFSDBFile = gridFS.findOne(query);
            System.out.println(gridFSDBFile.getFilename());
           res.type("video/mp4");

            InputStream inputStream = gridFSDBFile.getInputStream();
            ServletOutputStream out = res.raw().getOutputStream();
            String range = req.headers("Range");
            if(range == null) {
                try {
                    IOUtils.copy(inputStream, out);
                } finally {
                    inputStream.close();

                }
            }

            String[] ranges = range.split("=")[1].split("-");
            int from = Integer.parseInt(ranges[0]);
            int to = (int) gridFSDBFile.getChunkSize() + from;

            if (to > gridFSDBFile.getLength() ) {
                to = (int) (gridFSDBFile.getLength() -1);
            }

            if(ranges.length == 2) {
                to = Integer.parseInt(ranges[1]);
            }

            int len = to - from + 1;
            res.status(206);
            res.header("Accept-Ranges", "bytes");
            String responseRange = String.format("bytes %d-%d/%d", from, to, (int) gridFSDBFile.getLength());
            res.header("Content-Range", responseRange);
            res.raw().setContentLength(len);
            inputStream.skip(from);
            byte[] buffer = new byte[1024];

            try {
                while (len != 0) {
                    int read = inputStream.read(buffer, 0,  buffer.length > len ? len : buffer.length);
                    out.write(buffer, 0, read);
                    len -= read;
                }
            } finally {
                inputStream.close();
            }



            return 206;

        });





        get("/api/:username", (req, res) -> {
            res.type("application/json");
            String username = req.params(":username");
            User user =  userService.getUser(username);

            if(user != null) {
                return user;
            } else {
                return "User was not found";
            }
        }, json());


        post("/new-user", (req, res) -> {
            res.type("application/json");
            User user = fromJson(req.body(), User.class);
            String hashedPassword = BCrypt.hashpw(user.getPassword(), BCrypt.gensalt());
            user.setPassword(hashedPassword);
            return userService.createUser(user);

        }, json());


        post("/api/:username/newpost", (req, res) -> {
            String username = req.params(":username");
            User user = userService.getUser(username);
            Post post = fromJson(req.body(), Post.class);
            postService.create(post, user);

            res.type("application/json");
            return post;

        }, json());

        get("/api/:username/posts", (req, res) -> {
            res.type("application/json");

            String username = req.params(":username");
            User user = userService.getUser(username);
            return postService.getPosts(user);
        }, json());


        get("api/:username/posts/:postId", (req,res) -> {
            String username = req.params(":username");
            int id = Integer.parseInt(req.params(":postId"));

            res.type("application/json");
            return postService.getPost(id, userService.getUser(username));

        }, json());



    }
}
