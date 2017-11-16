package hackathon;

import java.io.*;
import java.lang.reflect.Array;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import javafx.beans.binding.IntegerBinding;
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
        FileReader f = new FileReader(path+"//villes.txt");
        BufferedReader bf = new BufferedReader(f);
        JSONObject jsonObject = new JSONObject();
        String city;
        while((city=bf.readLine())!=null){
            jsonObject.accumulate("patientCity", generateGraphicJSON(path+"//MED",city));
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
            Integer count = map.get(line.get(3));
            System.out.println("count =" + count);
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
            jsonObject.put("date", entry.getKey());
            jsonObject.put("nb", entry.getValue());
            mncp_medJSON.accumulate("patient", jsonObject);
        }
            

        return mncp_medJSON;
    }


    public static void generateMapJSON(String path)throws IOException{
        FileReader f = new FileReader(path+"//ville_luxembourg.txt");
        BufferedReader bf = new BufferedReader(f);
        JSONObject jsonObject = new JSONObject();
        String city;
//        int cpt=0,
//            indexNb=0;
//        boolean hasCreatedJSON = false;
        ArrayList<String> cityName = new ArrayList<>();

        while((city=bf.readLine())!=null){
            cityName.add(city);
        }
        System.out.println(cityName.size());
        for(String s : cityName){
            //hasCreatedJSON = false;
            //cpt++;
            jsonObject.accumulate("patientCity", generateMapJSON(path+"//MNCP",s));
//            if(cpt%(cityName.size()/3)==0){
//                indexNb++;
//                File jf = new File("mapJSON"+indexNb+".json");
//                if(jf.exists())
//                    jf.delete();
//
//                FileWriter jsonOutput = new FileWriter(jf);
//
//                jsonOutput.write(jsonObject.toString(4));
//
//                jsonOutput.close();
//                hasCreatedJSON = true;
//                jsonObject = new JSONObject();
//            }
        }
        File jf = new File("mapJSON.json");
        if(jf.exists())
            jf.delete();

        FileWriter jsonOutput = new FileWriter(jf);

        jsonOutput.write(jsonObject.toString(4));

        jsonOutput.close();
//        if(!hasCreatedJSON){
//            indexNb++;
//            File jf = new File("mapJSON"+indexNb+".json");
//            if(jf.exists())
//                jf.delete();
//            FileWriter jsonOutput = new FileWriter(jf);
//            jsonOutput.write(jsonObject.toString(4));
//            jsonOutput.close();
//        }

    }

	public static JSONObject generateMapJSON(String path, String cityparam) throws IOException{
        ArrayList<ArrayList<String>> res = SearchIndex(path, cityparam, "MNCP_NAME");
        HashMap<ArrayList<String>, Integer> map = new HashMap<>();
        for(ArrayList<String> line : res){
        	ArrayList<String> temp = new ArrayList<>();
            temp.add(line.get(1));
            temp.add(line.get(6));
//            int age = Integer.parseInt(line.get(6));
//            if(age <=5)
//                temp.add("5");
//            else if(age > 5 && age <=10)
//                temp.add("10");
//            else if(age > 10 && age <=15)
//                temp.add("15");
//            else if(age > 15 && age <=20)
//                temp.add("20");
//            else if(age > 20 && age <=25)
//                temp.add("25");
//            else if(age > 25 && age <=30)
//                temp.add("30");
//            else if(age > 30 && age <=35)
//                temp.add("35");
//            else if(age > 35 && age <=40)
//                temp.add("40");
//            else if(age > 40 && age <=45)
//                temp.add("45");
//            else if(age > 45 && age <=50)
//                temp.add("50");
//            else if(age > 50 && age <=55)
//                temp.add("55");
//            else if(age > 55 && age <=60)
//                temp.add("60");
//            else if(age > 60 && age <=65)
//                temp.add("65");
//            else if(age > 65 && age <=70)
//                temp.add("70");
//            else if(age > 70 && age <=75)
//                temp.add("75");
//            else if(age > 75 && age <=80)
//                temp.add("80");
//            else if(age > 80)
//                temp.add("80+");

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
            if(entry.getKey().get(0).equals("1"))
                jsonObject.put("sex", "M");
            if(entry.getKey().get(0).equals("2"))
                jsonObject.put("sex", "F");
            jsonObject.put("nb", entry.getValue());
            mapJSON.accumulate("patientInfo", jsonObject);
        }
        return mapJSON;
    }



    public static void generateDonutJSON(String path)throws IOException{
        FileReader f = new FileReader(path+"//ville_luxembourg.txt");
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
            res.add(dossier);
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
    	generateMapJSON(args[0]);
//        JSONObject jsonObject = generateMapJSON(args[0]+"//MNCP", "LUXEMBOURG");
//        File jf = new File("mapJSON.json");
//        if(jf.exists())
//            jf.delete();
//
//        FileWriter jsonOutput = new FileWriter(jf);
//
//        jsonOutput.write(jsonObject.toString(4));
//
//        jsonOutput.close();
    	//generateGraphicJSON(args[0]);
    }

}
