package umm3601.todo;

import com.mongodb.BasicDBObject;
import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.*;
import org.bson.codecs.*;
import org.bson.codecs.configuration.CodecRegistries;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.json.JsonReader;
import org.bson.types.ObjectId;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;

public class TodoControllerSpec {
    private TodoController todoController;
    private ObjectId ownerId;

    @Before
    public void clearAndPopulateDB() throws IOException {
        MongoClient mongoClient = new MongoClient();
        MongoDatabase database = mongoClient.getDatabase("test");
        MongoCollection<Document> todoDocuments = database.getCollection("todo");
        todoDocuments.drop();
        List<Document> testTodos = new ArrayList<>();
        testTodos.add(Document.parse("{\n" +
            "                    owner: \"Vipul\",\n" +
            "                    status: true,\n" +
            "                    body: \"Vipul says Hi!!\",\n" +
            "                    category: \"homework\"\n" +
            "                }"));
        testTodos.add(Document.parse("{\n" +
            "                    owner: \"Danish\",\n" +
            "                    status: true,\n" +
            "                    body: \"Danish says Hi!!\",\n" +
            "                    category: \"software design\"\n" +
            "                }"));
        testTodos.add(Document.parse("{\n" +
            "                    owner: \"DesiBoyz\",\n" +
            "                    status: false,\n" +
            "                    body: \"Rock n Roll\",\n" +
            "                    email: \"software design\"\n" +
            "                }"));
        ownerId = new ObjectId();
        BasicDBObject owner = new BasicDBObject("_id", ownerId);
        owner = owner.append("owner", "Nic")
            .append("status", true)
            .append("body", "Nic says Hi!!")
            .append("category", "teacher");
        todoDocuments.insertMany(testTodos);
        todoDocuments.insertOne(Document.parse(owner.toJson()));
        todoController = new TodoController(database);
    }

    // http://stackoverflow.com/questions/34436952/json-parse-equivalent-in-mongo-driver-3-x-for-java
    private BsonArray parseJsonArray(String json) {
        final CodecRegistry codecRegistry = CodecRegistries.fromProviders(Arrays.asList(
            new ValueCodecProvider(),
            new BsonValueCodecProvider(),
            new DocumentCodecProvider()));

        JsonReader reader = new JsonReader(json);
        BsonArrayCodec arrayReader = new BsonArrayCodec(codecRegistry);

        return arrayReader.decode(reader, DecoderContext.builder().build());
    }

    private static String getOwner(BsonValue val) {
        BsonDocument doc = val.asDocument();
        return ((BsonString) doc.get("owner")).getValue();
    }

    private static String getCategory(BsonValue val) {
        BsonDocument doc = val.asDocument();
        return ((BsonString) doc.get("category")).getValue();
    }

    @Test
    public void addNewTodo() {
        todoController.addNewTodo("testOwner", "true", "test body", "testCategory");

        Map<String, String[]> emptyMap = new HashMap<>();
        String jsonResult = todoController.getTodos(emptyMap);
        BsonArray docs = parseJsonArray(jsonResult);

        assertEquals("Should be 5 todos", 5, docs.size());
        List<String> owners = docs
            .stream()
            .map(TodoControllerSpec::getOwner)
            .sorted()
            .collect(Collectors.toList());
        List<String> expectedOwners = Arrays.asList("Vipul", "Danish", "DesiBoyz", "Nic", "testOwner");
        assertEquals("Owners should match", expectedOwners, owners);

        List<String> categories = docs
            .stream()
            .map(TodoControllerSpec::getCategory)
            .sorted()
            .collect(Collectors.toList());
        List<String> expectedCategories = Arrays.asList("homework", "software design", "software design", "teacher", "testCategory");
        assertEquals("Categories should match", expectedCategories, categories);
    }

    @Test
    public void getTodosWithCategoryandOwner() {
        Map<String, String[]> argMap = new HashMap<>();
        argMap.put("category", new String[]{"homework"});
        argMap.put("owner", new String[]{"Vipul"});
        String jsonResult = todoController.getTodos(argMap);
        BsonArray docs = parseJsonArray(jsonResult);

        assertEquals("Should be 1 todo", 1, docs.size());
        assertEquals("Owner should match", "Vipul", docs.get(0).asDocument().get("owner").asString().getValue());
        assertEquals("Category should match", "homework", docs.get(0).asDocument().get("category").asString().getValue());
    }

    @Test
    public void getTodosLimited() {
        Map<String, String[]> argMap = new HashMap<>();
        argMap.put("limit", new String[]{"2"});
        String jsonResult = todoController.getTodos(argMap);
        BsonArray docs = parseJsonArray(jsonResult);


        assertEquals("Should be 2 todos", 2, docs.size());
    }

    @Test
    public void getTodosOrderbyOwner() {
        Map<String, String[]> argMap = new HashMap<>();
        argMap.put("orderBy", new String[]{"owner"});
        String jsonResult = todoController.getTodos(argMap);
        BsonArray docs = parseJsonArray(jsonResult);

        assertEquals("Should be 4 todos", 4, docs.size());
        List<String> owners = docs
            .stream()
            .map(TodoControllerSpec::getOwner)
            .collect(Collectors.toList());
        List<String> expectedOwners = Arrays.asList("Vipul", "Danish", "DesiBoyz", "Nic");
        assertEquals("Owners should match", expectedOwners, owners);
    }


    @Test
    public void addMultipleNewTodo() {
        todoController.addNewTodo("testOwner", "true", "test body", "testCategory");

        Map<String, String[]> emptyMap = new HashMap<>();
        BsonArray docs = parseJsonArray(todoController.getTodos(emptyMap));

        assertEquals("Should be 5 todos", 5, docs.size());

        todoController.addNewTodo("testOwner2", "true", "test body2", "testCategory2");
        docs = parseJsonArray(todoController.getTodos(emptyMap));

        assertEquals("Should be 6 todos", 6, docs.size());

        todoController.addNewTodo("testOwner3", "false", "test body3", "testCategory3");
        docs = parseJsonArray(todoController.getTodos(emptyMap));

        assertEquals("Should be 7 todos", 7, docs.size());
    }

    @Test
    public void getAllTodos() {
        Map<String, String[]> emptyMap = new HashMap<>();
        String jsonResult = todoController.getTodos(emptyMap);
        BsonArray docs = parseJsonArray(jsonResult);

        assertEquals("should be 4 users", 4, docs.size());
        List<String> o = docs
            .stream()
            .map(TodoControllerSpec::getOwner)
            .sorted()
            .collect(Collectors.toList());
        List<String> ownerList = Arrays.asList("Vipul", "Danish", "DesiBoyz", "Nic");
        assertEquals("owner names should match", ownerList, o);

        List<String> c = docs
            .stream()
            .map(TodoControllerSpec::getCategory)
            .sorted()
            .collect(Collectors.toList());
        List<String> expectedCategories = Arrays.asList("homework", "software design", "software design", "teacher");
        assertEquals("Todos should match", expectedCategories, c);
    }

    
    @Test
    public void getTodosWithOwner() {
        Map<String, String[]> argMap = new HashMap<>();
        argMap.put("owner", new String[]{"Vipul"});
        String jsonResult = todoController.getTodos(argMap);
        BsonArray docs = parseJsonArray(jsonResult);

        assertEquals("Should be 2 todos", 2, docs.size());
        List<String> owners = docs
            .stream()
            .map(TodoControllerSpec::getOwner)
            .sorted()
            .collect(Collectors.toList());
        List<String> expectedOwners = Arrays.asList("Vipul", "Vipul");
        assertEquals("Owners should match", expectedOwners, owners);
    }

    @Test
    public void getTodosWithStatus() {
        Map<String, String[]> argMap = new HashMap<>();
        argMap.put("status", new String[]{"complete"});
        String jsonResult = todoController.getTodos(argMap);
        BsonArray docs = parseJsonArray(jsonResult);

        assertEquals("Should be 1 todo", 1, docs.size());
        assertEquals("Status should match", true, docs.get(0).asDocument().get("status").asBoolean().getValue());
    }

    @Test
    public void getTodosWithBody() {
        Map<String, String[]> argMap = new HashMap<>();
        argMap.put("contains", new String[]{"Rock"});
        String jsonResult = todoController.getTodos(argMap);
        BsonArray docs = parseJsonArray(jsonResult);

        assertEquals("Should be 1 todo", 1, docs.size());
        assertEquals("Body should match", "Rock n Roll",
            docs.get(0).asDocument().get("body").asString().getValue());
        assertEquals("Owner should match", "DesiBoyz", docs.get(0).asDocument().get("owner").asString().getValue());
    }

    @Test
    public void getTodosWithCategory() {
        Map<String, String[]> argMap = new HashMap<>();
        argMap.put("category", new String[]{"homework"});
        String jsonResult = todoController.getTodos(argMap);
        BsonArray docs = parseJsonArray(jsonResult);

        assertEquals("Should be 2 todos", 2, docs.size());
        List<String> categories = docs
            .stream()
            .map(TodoControllerSpec::getCategory)
            .collect(Collectors.toList());
        List<String> expectedCategories = Arrays.asList("homework", "homework");
        assertEquals("Categories should match", expectedCategories, categories);
    }


    @Test
    public void getTodosOrderByCategory() {
        Map<String, String[]> argMap = new HashMap<>();
        argMap.put("orderBy", new String[]{"category"});
        String jsonResult = todoController.getTodos(argMap);
        BsonArray docs = parseJsonArray(jsonResult);

        assertEquals("Should be 4 todos", 4, docs.size());
        List<String> categories = docs
            .stream()
            .map(TodoControllerSpec::getCategory)
            .collect(Collectors.toList());
        List<String> expectedCategories = Arrays.asList("homework", "software design", "software design", "teacher");
        assertEquals("Categories should match", expectedCategories, categories);
    }

    @Test
    public void getTodobyId() {
        String jsonResult = todoController.getTodo(ownerId.toHexString());
        Document exampleTodoDoc = Document.parse(jsonResult);
        assertEquals("Owner should match", "Barry", exampleTodoDoc.get("owner"));
        assertEquals("Category should match", "homework", exampleTodoDoc.get("category"));
    }

}
