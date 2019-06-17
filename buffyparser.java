import org.jsoup.nodes.Element;
import org.jsoup.nodes.Document;
import org.jsoup.Jsoup;

import com.mongodb.MongoClient;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.BasicDBObject;

import java.io.FileNotFoundException;
import java.io.IOException;

public class buffyparser {

    public static void main(String[] args) {
        MongoClient mongoClient = new MongoClient("localhost", 27017);
        DB database = mongoClient.getDB("buffy_db");
        DBCollection collection= database.createCollection("episodes", null);
        for (int i=1;i<8;i++){
            String buffyurl = String.format("https://en.wikipedia.org/wiki/Buffy_the_Vampire_Slayer_(season_%s)",Integer.toString(i));
            try {
                Document doc = Jsoup.connect(buffyurl).get();
                Element node = doc.select(".wikiepisodetable").first();
                node = node.child(0).child(0).nextElementSibling().child(0);
                while (true) {
                    BasicDBObject episode = new BasicDBObject();
                    episode.put("season",i);
                    System.out.println(node.text());
                    episode.put("seriesno",Integer.parseInt(node.text()));
                    node = node.nextElementSibling();
                    episode.put("seasonno",Integer.parseInt(node.text()));
                    node = node.nextElementSibling();
                    String title=node.text().replace("\"","");
                    episode.put("title",title);
                    node = node.nextElementSibling();
                    episode.put("director",node.text());
                    node = node.nextElementSibling();
                    episode.put("writer",node.text());
                    node = node.nextElementSibling();
                    String date =node.text();
                    episode.put("premiere",date.substring(date.length()-11,date.length()-1));
                    node = node.nextElementSibling();
                    episode.put("productioncode",node.text());
                    node = node.nextElementSibling();
                    if (node.text().equals("N/A")){episode.put("viewwership",node.text());}
                    else {
                        String x=node.text().split("\\[")[0].trim();
                        Double views=Double.parseDouble(x);
                        episode.put("viewwership",views);
                    }
                    node = node.parent().nextElementSibling();
                    episode.put("summary",node.text());
                    // System.out.println(episode);
                    collection.insert(episode);
                    try{node = node.nextElementSibling().child(0);}
                    catch(NullPointerException e){break;};
                }
            }
            catch (IOException e){}
            finally {}
        }
    }
}


