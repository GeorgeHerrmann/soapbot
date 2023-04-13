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
import com.georgster.profile.adapter.DatabaseObjectDeserializer;
import com.georgster.util.SoapUtility;
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

public class DatabaseService<T> {

    private static final CodecProvider pojoCodecProvider = PojoCodecProvider.builder().automatic(true).build();
    private static final CodecRegistry pojoCodecRegistry = fromRegistries(getDefaultCodecRegistry(), fromProviders(pojoCodecProvider));
    private static MongoClient mongoClient;
    private final Class<T> classType;
    private final ProfileType type;
    private String id;

    public DatabaseService(String guildId, ProfileType type, Class<T> classType) {
        this.classType = classType;
        this.type = type;
        createClient();

        this.id = guildId;
    }

    private static void createClient() {
        if (mongoClient == null) {
            try {
                mongoClient = MongoClients.create(Files.readString(Path.of(System.getProperty("user.dir"),"src", "main", "java", "com", "georgster", "profile", "dbconnection.txt")));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void withDatabase(Consumer<MongoDatabase> consumer) {
        consumer.accept(mongoClient.getDatabase(id).withCodecRegistry(pojoCodecRegistry));
    }

    public void addObject(T object) {
        withDatabase(database -> {
            MongoCollection<T> collection = database.getCollection(type.toString().toLowerCase(), classType);
            collection.insertOne(object);
        });
    }

    public void updateObject(String identifierName, String identifierValue, T object) {
        withDatabase(database -> {
            MongoCollection<T> collection = database.getCollection(type.toString().toLowerCase(), classType);
            Bson filter = eq(identifierName, identifierValue);
            collection.replaceOne(filter, object);
        });
    }

    public void removeObject(String identifierName, String identifierValue) {
        withDatabase(database -> {
            MongoCollection<T> collection = database.getCollection(type.toString().toLowerCase(), classType);
            Bson query = eq(identifierName, identifierValue);
            collection.deleteOne(query);
        });
    }

    public boolean objectExists(String identifierName, String identifierValue) {
        return getObject(identifierName, identifierValue) != null;
    }

    public boolean objectExists(String identifierName, String identifierValue, DatabaseObjectDeserializer<T> deserializer) {
        return getObject(identifierName, identifierValue, deserializer) != null;
    }

    public T getObject(String identifierName, String identifierValue) {
        DBObject<T> object = new DBObject<>();
        withDatabase(database -> {
            MongoCollection<Document> collection = database.getCollection(type.toString().toLowerCase(), Document.class);
            Bson query = eq(identifierName, identifierValue);
            Bson projection = Projections.fields(Projections.excludeId());
            
            Gson gson = new Gson();

            object.setObject(gson.fromJson(collection.find(query).projection(projection).first().toJson(), classType));
        });
        
        return object.getObject();
    }

    public T getObject(String identifierName, String identifierValue, DatabaseObjectDeserializer<T> deserializer) {
        DBObject<T> object = new DBObject<>();
        withDatabase(database -> {
            MongoCollection<Document> collection = database.getCollection(type.toString().toLowerCase(), Document.class);
            Bson query = eq(identifierName, identifierValue);
            Bson projection = Projections.fields(Projections.excludeId());
            
            Gson gson = new Gson();

            try {
                String json = collection.find(query).projection(projection).first().toJson();
                System.out.println("Json: " + json);
                object.setObject(gson.fromJson(json, deserializer.getClass(json)));
            } catch (Exception e) {
                e.printStackTrace();
                object.setObject(null);
            }
        });
        
        return object.getObject();
    }

    public List<T> getAllObjects() {
        if (classType == SoapEvent.class) throw new JsonParseException("Cannot use this method for SoapEvent");
        DBObject<List<T>> objects = new DBObject<>();
        withDatabase(database -> {
            MongoCollection<Document> collection = database.getCollection(type.toString().toLowerCase(), Document.class);
            List<T> list = new ArrayList<>();
            Gson gson = new Gson();
            collection.find().forEach((Consumer<Document>) document -> {
                list.add(gson.fromJson(document.toJson(), classType));
                //System.out.println(document.toJson());
            });
            objects.setObject(list);
        });
        if (objects.getObject() == null) return Collections.emptyList();
        return objects.getObject();
    }

    public List<T> getAllObjects(DatabaseObjectDeserializer<T> deserializer) {
        DBObject<List<T>> objects = new DBObject<>();
        withDatabase(database -> {
            MongoCollection<Document> collection = database.getCollection(type.toString().toLowerCase(), Document.class);
            List<T> list = new ArrayList<>();
            collection.find().forEach((Consumer<Document>) document -> {
                
                Gson gson = new Gson();
                String json = document.toJson();
                try {
                    list.add(gson.fromJson(json, deserializer.getClass(json)));
                    System.out.println(json);
                } catch (Exception e) {
                    e.printStackTrace();
                    System.out.println("No output");
                }
            });
            objects.setObject(list);
        });
        if (objects.getObject() == null) return Collections.emptyList();
        return objects.getObject();
    }

    public void removeObjectIfExists(String identifierName, String identifierValue) {
        if (objectExists(identifierName, identifierValue)) {
            removeObject(identifierName, identifierValue);
        }
    }

    public void removeObjectIfExists(String identifierName, String identifierValue, DatabaseObjectDeserializer<T> deserializer) {
        if (objectExists(identifierName, identifierValue, deserializer)) {
            removeObject(identifierName, identifierValue);
        }
    }

    public void addObjectIfNotExists(T object, String identifierName, String identifierValue) {
        if (!objectExists(identifierName, identifierValue)) {
            addObject(object);
        }
    }

    public void addObjectIfNotExists(T object, String identifierName, String identifierValue, DatabaseObjectDeserializer<T> deserializer) {
        if (!objectExists(identifierName, identifierValue, deserializer)) {
            addObject(object);
        }
    }

    public void updateObjectIfExists(T object, String identifierName, String identifierValue) {
        if (objectExists(identifierName, identifierValue)) {
            updateObject(identifierName, identifierValue, object);
        }
    }

    public void updateObjectIfExists(T object, String identifierName, String identifierValue, DatabaseObjectDeserializer<T> deserializer) {
        if (objectExists(identifierName, identifierValue, deserializer)) {
            updateObject(identifierName, identifierValue, object);
        }
    }

    public static void close() {
        mongoClient.close();
    }
}
