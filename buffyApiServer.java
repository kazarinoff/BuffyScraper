import java.net.ServerSocket;
import java.net.Socket;
import java.net.URI;

import java.util.HashMap;
import java.io.InputStreamReader;
import java.io.BufferedReader;
import java.io.FileReader;

import com.mongodb.MongoClient;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import com.mongodb.DBCursor;

import java.io.File;
import java.io.FileInputStream;


public class buffyApiServer { 
    public static void main(String[] args) throws Exception { 
        final ServerSocket server = new ServerSocket(8080);

        MongoClient mongoClient = new MongoClient("localhost", 27017);
        DB database = mongoClient.getDB("buffy_db");
        DBCollection collection = database.getCollection("episodes");

         final File WEB_ROOT = new File(".");
	     final String DEFAULT_FILE = "index.html";

        System.out.println("Listening for connection on port 8080 ...."); 
        while (true){
            try (Socket socket = server.accept()) {
                InputStreamReader isr = new InputStreamReader(socket.getInputStream()); 
                BufferedReader reader = new BufferedReader(isr);
                String line = reader.readLine(); 
                String[] response = line.split(" ");
                String requesttype=response[0];
                String requesturl= response[1].replace("/", "");
                if (!requesttype.equals("GET")){
                    String httpResponse = "HTTP/1.1 200 OK\r\n\r\n" + "<h1>NOT A GET REQUEST</h1>"; 
                    socket.getOutputStream().write(httpResponse.getBytes("UTF-8")); 
                    }
                else{
                    if (requesturl.matches("\\d+")){
                        DBObject query = new BasicDBObject("seriesno",Integer.parseInt(requesturl));
                        DBCursor cursor = collection.find(query);
                        DBObject episode = cursor.one();
                        if (episode==null){
                            String httpResponse = "HTTP/1.1 400 OK\r\n\r\n" + "<h1>episode not found</h1>"; 
                            socket.getOutputStream().write(httpResponse.getBytes("UTF-8")); 
                        }
                        else {
                            String httpResponse = "HTTP/1.1 400 OK\r\n\r\n" + episode; 
                            socket.getOutputStream().write(httpResponse.getBytes("UTF-8")); 
                        }
                    }
                    else{
                        BufferedReader filereader = new BufferedReader(new FileReader("./index.html"));
                        String builder = new String();
                        String currentLine = filereader.readLine();
                        while (currentLine != null) {
                            builder += currentLine;
                            // builder += "<h1>ARGGGG</h1>";
                            currentLine = filereader.readLine();
                        }
                        filereader.close();                        
                        String httpResponse = "HTTP/1.1 200 OK\r\n\r\n" + builder; 
                        socket.getOutputStream().write(httpResponse.getBytes("UTF-8"));
                    }
                }
            }
        }
    }    
}

