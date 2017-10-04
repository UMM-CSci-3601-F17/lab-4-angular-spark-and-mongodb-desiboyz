package umm3601.todo;

import com.google.gson.Gson;
import com.mongodb.BasicDBObject;
import com.mongodb.MongoException;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Aggregates;
import com.mongodb.util.JSON;
import org.bson.Document;
import org.bson.types.ObjectId;
import spark.Request;
import spark.Response;

import java.util.Iterator;
import java.util.Map;


import java.util.*;

import com.mongodb.client.AggregateIterable;

import static com.mongodb.client.model.Filters.eq;

/**
 * Controller that manages requests for info about todos.
 */
public class TodoController {

    private final Gson gson;
    private MongoDatabase database;
    private final MongoCollection<Document> todoCollection;

    /**
     * Construct a controller for todos.
     *
     * @param database the database containing todo data
     */
    public TodoController(MongoDatabase database) {
        gson = new Gson();
        this.database = database;
        todoCollection = database.getCollection("todos");
    }


    /**
     * Get a JSON response with a list of all the todos in the database.
     *
     * @param req the HTTP request
     * @param res the HTTP response
     * @return one todo in JSON formatted string and if it fails it will return text with a different HTTP status code
     */
    public String getTodo(Request req, Response res) {
        res.type("application/json");
        String id = req.params("id");
        String todo;
        try {
            todo = getTodo(id);
        } catch (IllegalArgumentException e) {
            // This is thrown if the ID doesn't have the appropriate
            // form for a Mongo Object ID.
            // https://docs.mongodb.com/manual/reference/method/ObjectId/
            res.status(400);
            res.body("The requested todo id " + id + " wasn't a legal Mongo Object ID.\n" +
                "See 'https://docs.mongodb.com/manual/reference/method/ObjectId/' for more info.");
            return "";
        }
        if (todo != null) {
            return todo;
        } else {
            res.status(404);
            res.body("The requested todo with id " + id + " was not found");
            return "";
        }
    }


    /**
     * Get the single todo specified by the `id` parameter in the request.
     *
     * @param id the Mongo ID of the desired todo
     * @return the desired todo as a JSON object if the todo with that ID is found,
     * and `null` if no todo with that ID is found
     */
    public String getTodo(String id) {
        FindIterable<Document> jsonTodos = todoCollection.find(eq("_id", new ObjectId(id)));

        Iterator<Document> iterator = jsonTodos.iterator();
        if (iterator.hasNext()) {
            Document todo = iterator.next();
            return todo.toJson();
        } else {
            // We didn't find the desired todo
            return null;
        }
    }


    /**
     * @param req
     * @param res
     * @return an array of todos in JSON formatted String
     */
    public String getTodos(Request req, Response res) {
        res.type("application/json");
        return getTodos(req.queryMap().toMap());
    }

    /**
     * @param queryParams
     * @return an array of Todos in a JSON formatted string
     */
    public String getTodos(Map<String, String[]> queryParams) {

        Document filterDoc = new Document();

        if (queryParams.containsKey("owner")) {
            String targetOwner = (queryParams.get("owner")[0]);
            filterDoc = filterDoc.append("owner", targetOwner);
        }

        if (queryParams.containsKey("status")) {
            String targetStatus = (queryParams.get("status")[0]);
            filterDoc = filterDoc.append("status", targetStatus);
        }

        if (queryParams.containsKey("body")) {
            String targetBody = (queryParams.get("body")[0]);
            filterDoc = filterDoc.append("body", targetBody);
        }

        if (queryParams.containsKey("category")) {
            String targetCategory = (queryParams.get("category")[0]);
            filterDoc = filterDoc.append("category", targetCategory);
        }

        //FindIterable comes from mongo, Document comes from Gson
        FindIterable<Document> matchingTodos = todoCollection.find(filterDoc);

        return JSON.serialize(matchingTodos);
    }

    /**
     * @param req
     * @param res
     * @return
     */
    public boolean addNewTodo(Request req, Response res) {

        res.type("application/json");
        Object o = JSON.parse(req.body());
        try {
            if (o.getClass().equals(BasicDBObject.class)) {
                try {
                    BasicDBObject dbO = (BasicDBObject) o;

                    String owner = dbO.getString("owner");
                    String status = dbO.getString("status");
                    String category = dbO.getString("category");
                    String body = dbO.getString("body");

                    System.err.println("Adding new todo [owner=" + owner + "status=" + status + " body=" + body + " category=" + category + ']');
                    return addNewTodo(owner, status, body, category);
                } catch (NullPointerException e) {
                    System.err.println("A value was malformed or omitted, new todo request failed.");
                    return false;
                }

            } else {
                System.err.println("Expected BasicDBObject, received " + o.getClass());
                return false;
            }
        } catch (RuntimeException ree) {
            ree.printStackTrace();
            return false;
        }
    }

    /**
     * @parama None
     * @return JSON formatted summary of Todos
     */

    public String todoSummary() {
        Document doc = new Document();
        Document extra = new Document();
        float summary;
        extra.append("status", true);
        summary = todoCollection.count(extra);
        float p = summary / todoCollection.count();
        doc.append("percentageTodosComplete", p);
        AggregateIterable<Document> tempDoc = todoCollection.aggregate(Arrays.asList(Aggregates.group("$category")));
        List<String> newRes = new ArrayList<>();
        for (Document d : tempDoc) {
            newRes.add(d.getString("_id"));
        }
        Document categoryDoc = new Document();
        for (String c : newRes) {
            categoryDoc.append(c, add("category", c) / todoCollection.count(eq("category", c)));
        }
        doc.append("categoriesPercentComplete", categoryDoc);
        AggregateIterable<Document> ownerDoc = todoCollection.aggregate(Arrays.asList(Aggregates.group("$owner")));
        List<String> resTempDoc = new ArrayList<>();
        for (Document d : ownerDoc) {
            resTempDoc.add(d.getString("_id"));
        }
        Document tempOwner = new Document();
        for (String o : resTempDoc) {
            tempOwner.append(o, add("owner", o) / todoCollection.count(eq("owner", o)));
        }
        doc.append("ownersPercentComplete", tempOwner);
        return JSON.serialize(doc);
    }

    // Function that take in field and value and returns count (used by todoSummary)

    private float add(String fields, String val) {
        Document c = new Document();
        c.append(fields, val);
        c.append("status", true);
        return todoCollection.count(c);
    }


    /**
     * @param owner,status,body,category
     * @return true
     */
    public boolean addNewTodo(String owner, String status, String body, String category) {

        Document newTodo = new Document();
        newTodo.append("owner", owner);
        newTodo.append("status", status);
        newTodo.append("body", body);
        newTodo.append("category", category);

        try {
            todoCollection.insertOne(newTodo);
        } catch (MongoException me) {
            me.printStackTrace();
            return false;
        }

        return true;
    }
}
