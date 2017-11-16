package hackathon;

import java.io.*;
import java.lang.reflect.Array;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TopScoreDocCollector;
import org.apache.lucene.queryparser.classic.MultiFieldQueryParser;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.json.*;

public class SearchIndex {
	
	public static void generateGraphicJSON(String path)throws IOException{
        FileReader f = new FileReader(path+"//villes_luxembourg.txt");
        BufferedReader bf = new BufferedReader(f);
        JSONObject jsonObject = new JSONObject();
        String city;
        while((city=bf.readLine())!=null){
            jsonObject.accumulate("medicalCity", generateGraphicJSON(path+"//MED",city));
        }
        File jf = new File("graphicJSON.json");
        if(jf.exists())
            jf.delete();

        FileWriter jsonOutput = new FileWriter(jf);

        jsonOutput.write(jsonObject.toString(4));

        jsonOutput.close();
    }


	public static JSONObject generateGraphicJSON(String path, String cityparam) throws IOException{
        ArrayList<ArrayList<String>> res = SearchIndex(path, cityparam, "VILLE_MEDECIN");
        HashMap<String, Integer> map = new HashMap<>();
        int nbConsultations = 0,
            nbConsultationsIgn =0;
        for(ArrayList<String> line : res){
            nbConsultations++;
            //System.out.println("date = "+ line.get(3));
            //line.get(3) = line.get(3).substring(0,7);
            Integer count = map.get(line.get(3));
            //System.out.println("count =" + count);
            if (count == null) {
                map.put(line.get(3), 1);
            }
            else {
                map.put(line.get(3), count + 1);
            }
        }
        JSONObject mncp_medJSON = new JSONObject();
        mncp_medJSON.put("medicalCityName",cityparam);
        for(Map.Entry<String, Integer> entry : map.entrySet()){
        	JSONObject jsonObject = new JSONObject();
        	//System.out.println("date = " + entry.getKey().substring(0, 7));
            jsonObject.put("date", entry.getKey().substring(6));
            jsonObject.put("nb", entry.getValue());
            mncp_medJSON.accumulate("patient", jsonObject);
        }
            

        return mncp_medJSON;
    }


    public static void generateMapJSON(String path)throws IOException{
        FileReader f = new FileReader(path+"//villes.txt");
        BufferedReader bf = new BufferedReader(f);
        JSONObject jsonObject = new JSONObject();
        String city;
        while((city=bf.readLine())!=null){
            jsonObject.accumulate("patientCity", generateMapJSON(path+"//MNCP",city));
        }
        File jf = new File("mapJSON.json");
        if(jf.exists())
            jf.delete();

        FileWriter jsonOutput = new FileWriter(jf);

        jsonOutput.write(jsonObject.toString(4));

        jsonOutput.close();
    }

	public static JSONObject generateMapJSON(String path, String cityparam) throws IOException{
        ArrayList<ArrayList<String>> res = SearchIndex(path, cityparam, "MNCP_NAME");
        HashMap<ArrayList<String>, Integer> map = new HashMap<>();
        for(ArrayList<String> line : res){
        	ArrayList<String> temp = new ArrayList<>();
            temp.add(line.get(1));
            temp.add(line.get(6));
            Integer count = map.get(temp);
            if (count == null) {
                map.put(temp, 1);
            }
            else {
                map.put(temp, count + 1);
            }
        }
        JSONObject mapJSON = new JSONObject();
        mapJSON.put("patientCityName",cityparam);
        for(Map.Entry<ArrayList<String>, Integer> entry : map.entrySet()){
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("age", entry.getKey().get(1));
            jsonObject.put("sex", entry.getKey().get(0));
            jsonObject.put("nb", entry.getValue());
            mapJSON.accumulate("medicalCity", jsonObject);
        }
        return mapJSON;
    }



    public static void generateDonutJSON(String path)throws IOException{
        FileReader f = new FileReader(path+"//villes.txt");
        BufferedReader bf = new BufferedReader(f);
        JSONObject jsonObject = new JSONObject();
        String city;
        while((city=bf.readLine())!=null){
            jsonObject.accumulate("patientCity", generateDonutJSON(path+"//MNCP",city));
        }
        File jf = new File("donutJSON.json");
        if(jf.exists())
            jf.delete();

        FileWriter jsonOutput = new FileWriter(jf);

        jsonOutput.write(jsonObject.toString(4));

        jsonOutput.close();
    }

    public static JSONObject generateDonutJSON(String path, String cityparam) throws IOException{
        ArrayList<ArrayList<String>> res = SearchIndex(path, cityparam, "MNCP_NAME");
        HashMap<String, Integer> map = new HashMap<>();
        int nbConsultations = 0,
            nbConsultationsIgn =0;
        float pctEntry = 0;
        for(ArrayList<String> line : res){
            nbConsultations++;
            Integer count = map.get(line.get(4));
            if (count == null) {
                map.put(line.get(4), 1);
            }
            else {
                map.put(line.get(4), count + 1);
            }
        }
        JSONObject mncp_medJSON = new JSONObject();
        mncp_medJSON.put("patientCityName",cityparam);
        for(Map.Entry<String, Integer> entry : map.entrySet()){
            pctEntry=(float)entry.getValue()/(float)nbConsultations;
            if(pctEntry>0.01f) {
                //System.out.println(pctEntry);
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("medicalCityName", entry.getKey());
                jsonObject.put("nb", entry.getValue());
                mncp_medJSON.accumulate("medicalCity", jsonObject);
            }
            else nbConsultationsIgn+=entry.getValue();
        }
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("medicalCityName", "Autres");
        jsonObject.put("nb", nbConsultationsIgn);
        mncp_medJSON.accumulate("medicalCity", jsonObject);


        return mncp_medJSON;
    }



	
    
    public static ArrayList<ArrayList<String>> SearchIndex(String path, String cityparam, String field ) throws IOException {
        //System.out.println("Path = " + path);
        StandardAnalyzer analyzer = new StandardAnalyzer();
        Directory index = FSDirectory.open(Paths.get(path));
        //System.out.println("length = " + index.listAll().toString().length());
        ArrayList<ArrayList<String>> res2 = new ArrayList<ArrayList<String>>();

        String id = "";
        String sex = "";
        String date_birth = null;
        String city_doctor = "";
        String pays_doctor = "";
        String dossier = null;
        String age = "";
        //q est la recherche
        Query q = null;
        try {
            q = new QueryParser(field, analyzer).parse(QueryParser.escape(cityparam));

        } catch (org.apache.lucene.queryparser.classic.ParseException e) {
            e.printStackTrace();
        }
        int hitsPerPage = 1000000;
        IndexReader reader = DirectoryReader.open(index);

        IndexSearcher searcher = new IndexSearcher(reader);
        TopScoreDocCollector collector = TopScoreDocCollector.create(hitsPerPage);
        searcher.search(q, collector);
        ScoreDoc[] hits = collector.topDocs().scoreDocs;
        // =============== Display  =============================
        //System.out.println("Found " + hits.length + " hits.");
        for (int i = 0; i < hits.length; ++i) {
            ArrayList<String> res = new ArrayList<String>();
            int docId = hits[i].doc;
            Document d = searcher.doc(docId);
            id = d.get("PRSN_INTERNALID");
            sex = d.get("PRSN_SEX");
            date_birth = d.get("PRSN_BIRTHDATE");
            city_doctor = d.get("VILLE_MEDECIN");
            pays_doctor = d.get("PAYS_MEDECIN");
            dossier = d.get("DATE_DOSSIER");
            age = d.get("PRSN_AGE");

            res.add(id);
            res.add(sex);
            res.add(date_birth);
            res.add(dossier.substring(0,7));
            res.add(city_doctor);
            res.add(pays_doctor);
            res.add(age);

            res2.add(i, res);
        }

        reader.close();

        return res2;
    }

   
    public static void main(String[] args) throws Exception {
    	
        //generateDonutJSON(args[0]);
        //generateDonutJSON(args[0], args[1]);
    	//System.out.println(getNbPatients(args[0],args[1], "1", "20", "25"));
    	//generateMapJSON(args[0]);
    	generateGraphicJSON(args[0]);
    }

}
