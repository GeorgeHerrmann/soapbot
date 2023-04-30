package com.georgster.profile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

import org.bson.Document;
import org.bson.codecs.configuration.CodecProvider;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.codecs.pojo.PojoCodecProvider;
import org.bson.conversions.Bson;

import com.georgster.events.SoapEvent;
import com.georgster.profile.adapter.DatabaseObjectClassAdapter;
import com.google.gson.Gson;
import com.google.gson.JsonParseException;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Projections;

import static com.mongodb.MongoClientSettings.getDefaultCodecRegistry;
import static org.bson.codecs.configuration.CodecRegistries.fromProviders;
import static org.bson.codecs.configuration.CodecRegistries.fromRegistries;
import static com.mongodb.client.model.Filters.eq;

/**
 * <p>A service capable of storing and retrieving objects from SOAP Bot's MongoDB database.
 * It can be used to store and retrieve any object through any methods, however interfaces
 * or abstract classes must use a {@link DatabaseObjectClassAdapter} to supply this service
 * with the correct class type.</p>
 * 
 * <p>Methods which require an "identifierName" and "identifierValue" parameter are used to
 * find the object to update or delete. The identifier name is the name of the field in the
 * object to search for and the identifier value is the value of that field.</p>
 * 
 * @param <T> The type of object to store or retrieve.
 */
public class DatabaseService<T> {

    private static final CodecProvider pojoCodecProvider = PojoCodecProvider.builder().automatic(true).build();
    private static final CodecRegistry pojoCodecRegistry = fromRegistries(getDefaultCodecRegistry(), fromProviders(pojoCodecProvider));
    private static MongoClient mongoClient; // The Singleton MongoClient
    private final Class<T> classType; // The class type of the object to store or retrieve
    private final ProfileType type; // The type of the server's profile to store or retrieve from
    private String id; // The ID of the server to store or retrieve from

    /**
     * <p>Creates a new DatabaseService instance for a specific Discord server.
     * Objects stored or retrieved will be of the given class type.
     * If the class type is an interface or abstract class, a {@link DatabaseObjectClassAdapter}
     * must be used for most method calls to supply the extending class type.</p>
     * 
     * <p>This service will only store or retrieve objects from the collection of the given {@code ProfileType}.</p>
     * 
     * @param guildId The ID of the Discord server to store or retrieve from.
     * @param type The type of the server's profile to store or retrieve from.
     * @param classType The class type of the object to store or retrieve.
     */
    public DatabaseService(String guildId, ProfileType type, Class<T> classType) {
        this.classType = classType;
        this.type = type;
        createClient();

        this.id = guildId;
    }

    /**
     * Creates and connects a new MongoClient if one does not already exist.
     */
    private static void createClient() {
        if (mongoClient == null) {
            try {
                mongoClient = MongoClients.create(Files.readString(Path.of(System.getProperty("user.dir"),"src", "main", "java", "com", "georgster", "profile", "dbconnection.txt")));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Executes the given consumer with the database of the current server.
     * 
     * @param consumer The consumer to execute.
     */
    private void withDatabase(Consumer<MongoDatabase> consumer) {
        consumer.accept(mongoClient.getDatabase(id).withCodecRegistry(pojoCodecRegistry));
    }

    /**
     * Adds the given object to the database unconditionally.
     * 
     * @param object The object to add.
     */
    public void addObject(T object) {
        withDatabase(database -> {
            MongoCollection<T> collection = database.getCollection(type.toString().toLowerCase(), classType);
            collection.insertOne(object);
        });
    }

    /**
     * Attempts to update the object found by the given identifier name and value.
     * 
     * @param identifierName The name of the field to search for.
     * @param identifierValue The value of the field to search for.
     * @param object The object to update with.
     */
    public void updateObject(String identifierName, String identifierValue, T object) {
        withDatabase(database -> {
            MongoCollection<T> collection = database.getCollection(type.toString().toLowerCase(), classType);
            Bson filter = eq(identifierName, identifierValue);
            collection.replaceOne(filter, object);
        });
    }

    /**
     * Attempts to remove the object found by the given identifier name and value.
     * 
     * @param identifierName The name of the field to search for.
     * @param identifierValue The value of the field to search for.
     */
    public void removeObject(String identifierName, String identifierValue) {
        withDatabase(database -> {
            MongoCollection<T> collection = database.getCollection(type.toString().toLowerCase(), classType);
            Bson query = eq(identifierName, identifierValue);
            collection.deleteOne(query);
        });
    }

    /**
     * Returns whether or not an object exists in the database with the given identifier name and value.
     * 
     * @param identifierName The name of the field to search for.
     * @param identifierValue The value of the field to search for.
     * @return Whether or not an object exists in the database with the given identifier name and value.
     */
    public boolean objectExists(String identifierName, String identifierValue) {
        return getObject(identifierName, identifierValue) != null;
    }

    /**
     * Returns whether or not an object exists in the database with the given identifier name and value of
     * the class type specified by the given deserializer.
     * 
     * @param identifierName The name of the field to search for.
     * @param identifierValue The value of the field to search for.
     * @param deserializer The deserializer to use to get the class type of the object to search for.
     * @return Whether or not an object exists in the database with the given identifier name and value of
     */
    public boolean objectExists(String identifierName, String identifierValue, DatabaseObjectClassAdapter<T> deserializer) {
        return getObject(identifierName, identifierValue, deserializer) != null;
    }

    /**
     * Returns the object found by the given identifier name and value.
     * 
     * @param identifierName The name of the field to search for.
     * @param identifierValue The value of the field to search for.
     * @return The object found by the given identifier name and value.
     */
    public T getObject(String identifierName, String identifierValue) {
        DBObject<T> object = new DBObject<>();
        withDatabase(database -> {
            MongoCollection<Document> collection = database.getCollection(type.toString().toLowerCase(), Document.class);
            Bson query = eq(identifierName, identifierValue);
            Bson projection = Projections.fields(Projections.excludeId());
            
            Gson gson = new Gson();
            try {
                object.setObject(gson.fromJson(collection.find(query).projection(projection).first().toJson(), classType));
            } catch (Exception e) {
                object.setObject(null);
            }
        });
        
        return object.getObject();
    }

    /**
     * Returns the object found by the given identifier name and value of the class type specified by the given deserializer.
     * 
     * @param identifierName The name of the field to search for.
     * @param identifierValue The value of the field to search for.
     * @param deserializer The deserializer to use to get the class type of the object to search for.
     * @return The object found by the given identifier name and value of the class type specified by the given deserializer.
     */
    public T getObject(String identifierName, String identifierValue, DatabaseObjectClassAdapter<T> deserializer) {
        DBObject<T> object = new DBObject<>();
        withDatabase(database -> {
            MongoCollection<Document> collection = database.getCollection(type.toString().toLowerCase(), Document.class);
            Bson query = eq(identifierName, identifierValue);
            Bson projection = Projections.fields(Projections.excludeId());
            
            Gson gson = new Gson();

            try {
                String json = collection.find(query).projection(projection).first().toJson();
                object.setObject(gson.fromJson(json, deserializer.getClass(json)));
            } catch (Exception e) {
                object.setObject(null);
            }
        });
        
        return object.getObject();
    }

    /**
     * Returns all objects in the database for this service's {@code ProfileType}.
     * 
     * @return All objects in the database for this service's {@code ProfileType}.
     */
    public List<T> getAllObjects() {
        if (classType == SoapEvent.class) throw new JsonParseException("Cannot use this method for SoapEvent");
        DBObject<List<T>> objects = new DBObject<>();
        withDatabase(database -> {
            MongoCollection<Document> collection = database.getCollection(type.toString().toLowerCase(), Document.class);
            List<T> list = new ArrayList<>();
            Gson gson = new Gson();
            collection.find().forEach((Consumer<Document>) document -> list.add(gson.fromJson(document.toJson(), classType)));
            objects.setObject(list);
        });
        if (objects.getObject() == null) return Collections.emptyList();
        return objects.getObject();
    }

    /**
     * Returns all objects in the database for this service's {@code ProfileType} of the class type specified by the given deserializer.
     * 
     * @param deserializer The deserializer to use to get the class type of the objects to search for.
     * @return All objects in the database for this service's {@code ProfileType} of the class type specified by the given deserializer.
     */
    public List<T> getAllObjects(DatabaseObjectClassAdapter<T> deserializer) {
        DBObject<List<T>> objects = new DBObject<>();
        withDatabase(database -> {
            MongoCollection<Document> collection = database.getCollection(type.toString().toLowerCase(), Document.class);
            List<T> list = new ArrayList<>();
            collection.find().forEach((Consumer<Document>) document -> {
                
                Gson gson = new Gson();
                String json = document.toJson();
                if (!json.isEmpty()) {
                    list.add(gson.fromJson(json, deserializer.getClass(json)));
                }
            });
            objects.setObject(list);
        });
        if (objects.getObject() == null) return Collections.emptyList();
        return objects.getObject();
    }

    /**
     * Removes the object found by the given identifier name and value if it exists in the database.
     * 
     * @param identifierName The name of the field to search for.
     * @param identifierValue The value of the field to search for.
     */
    public void removeObjectIfExists(String identifierName, String identifierValue) {
        if (objectExists(identifierName, identifierValue)) {
            removeObject(identifierName, identifierValue);
        }
    }

    /**
     * Removes the object found by the given identifier name and value of the class type specified by the given deserializer if it exists in the database.
     * 
     * @param identifierName The name of the field to search for.
     * @param identifierValue The value of the field to search for.
     * @param deserializer The deserializer to use to get the class type of the object to search for.
     */
    public void removeObjectIfExists(String identifierName, String identifierValue, DatabaseObjectClassAdapter<T> deserializer) {
        if (objectExists(identifierName, identifierValue, deserializer)) {
            removeObject(identifierName, identifierValue);
        }
    }

    /**
     * Adds the given object to the database if it does not already exist.
     * 
     * @param object The object to add to the database.
     * @param identifierName The name of the field to search for.
     * @param identifierValue The value of the field to search for.
     */
    public void addObjectIfNotExists(T object, String identifierName, String identifierValue) {
        if (!objectExists(identifierName, identifierValue)) {
            addObject(object);
        }
    }

    /**
     * Adds the given object to the database if it does not already exist.
     * 
     * @param object The object to add to the database.
     * @param identifierName The name of the field to search for.
     * @param identifierValue The value of the field to search for.
     * @param deserializer The deserializer to use to get the class type of the object to search for.
     */
    public void addObjectIfNotExists(T object, String identifierName, String identifierValue, DatabaseObjectClassAdapter<T> deserializer) {
        if (!objectExists(identifierName, identifierValue, deserializer)) {
            addObject(object);
        }
    }

    /**
     * Updates the object found by the given identifier name and value if it exists in the database.
     * 
     * @param object The object to update the database with.
     * @param identifierName The name of the field to search for.
     * @param identifierValue The value of the field to search for.
     */
    public void updateObjectIfExists(T object, String identifierName, String identifierValue) {
        if (objectExists(identifierName, identifierValue)) {
            updateObject(identifierName, identifierValue, object);
        }
    }

    /**
     * Updates the object found by the given identifier name and value of the class type specified by the given deserializer if it exists in the database.
     * 
     * @param object The object to update the database with.
     * @param identifierName The name of the field to search for.
     * @param identifierValue The value of the field to search for.
     * @param deserializer The deserializer to use to get the class type of the object to search for.
     */
    public void updateObjectIfExists(T object, String identifierName, String identifierValue, DatabaseObjectClassAdapter<T> deserializer) {
        if (objectExists(identifierName, identifierValue, deserializer)) {
            updateObject(identifierName, identifierValue, object);
        }
    }

    /**
     * Closes the connection to the database.
     */
    public static void close() {
        mongoClient.close();
    }
}
