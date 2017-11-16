package hackathon;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.Fields;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.MultiFields;
import org.apache.lucene.index.Terms;
import org.apache.lucene.index.TermsEnum;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TopScoreDocCollector;
import org.apache.lucene.util.BytesRef;
import org.apache.lucene.util.QueryBuilder;
import org.apache.lucene.queryparser.classic.MultiFieldQueryParser;
import org.apache.lucene.queryparser.classic.QueryParserBase;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

import java.applet.Applet;

public class SearchIndex extends Applet {

	
	public static int[] nbPatientsparAge(String path, String cityparam) throws IOException{
		int[] tabAges = new int[10];
		for(int i=0; i<tabAges.length-1; i++){
			tabAges[i] = getNbPatients(path,cityparam,"1",String.valueOf(i*10),String.valueOf(i*10+9))
					+getNbPatients(path,cityparam,"2",String.valueOf(i*10),String.valueOf(i*10+9));
		}
		tabAges[9]=getNbPatients(path,cityparam,"1",String.valueOf(90),String.valueOf(110))
				+getNbPatients(path,cityparam,"2",String.valueOf(90),String.valueOf(110));
		return tabAges;
	}
	
	public static int getNbPatients(String path, String cityparam, String sexparam, String agemin, String agemax) throws IOException{
		return SearchIndexbis( path,  cityparam,  sexparam,  agemin,  agemax ).size();
	}

    public static ArrayList<ArrayList<String>> SearchIndexbis(String path, String cityparam, String sexparam, String agemin, String agemax ) throws IOException {
    	ArrayList<ArrayList<String>> res = new ArrayList<ArrayList<String>>();
    	for(int i=Integer.parseInt(agemin); i<Integer.parseInt(agemax); i++)
    		res.addAll(SearchIndex(path,cityparam,sexparam,String.valueOf(i)));
    	//System.out.println(res);
    	return res;
    }
    
    public static ArrayList<ArrayList<String>> SearchIndex(String path, String cityparam, String sexparam, String ageparam ) throws IOException {
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
        String ville="";
        //q est la recherche
        Query q = null;
        try {
        	String[] listField = {"MNCP_NAME", "PRSN_AGE", "PRSN_SEX"};
        	String[] listPath = {cityparam,ageparam,sexparam};
        	BooleanClause.Occur[] flags = {BooleanClause.Occur.MUST,BooleanClause.Occur.MUST, BooleanClause.Occur.MUST};
            q = new MultiFieldQueryParser(listField, analyzer).parse(listPath,listField,flags, analyzer);
        
           

        } catch (org.apache.lucene.queryparser.classic.ParseException e) {
            e.printStackTrace();
        }
        int hitsPerPage = 1000000;
        IndexReader reader = DirectoryReader.open(index);
		
        IndexSearcher searcher = new IndexSearcher(reader);
        //System.out.println("searcher =" + searcher.count(q));
        TopScoreDocCollector collector = TopScoreDocCollector.create(hitsPerPage);
        //System.out.println("collector = " + collector.toString());
        searcher.search(q, collector);
        ScoreDoc[] hits = collector.topDocs().scoreDocs;
        // =============== Display  =============================
        //System.out.println("Found " + hits.length + " hits.");
        for (int i = 0; i < hits.length; ++i) {
        	ArrayList<String> res = new ArrayList<String>();
            int docId = hits[i].doc;
            Document d = searcher.doc(docId);
            //System.out.println("document = " + d.getFields());
            id = d.get("PRSN_INTERNALID");
            sex = d.get("PRSN_SEX");
            date_birth = d.get("PRSN_BIRTHDATE");
            city_doctor = d.get("VILLE_MEDECIN");
            pays_doctor = d.get("PAYS_MEDECIN");
            dossier = d.get("DATE_DOSSIER");
            
            /*
            System.out.println("id = " + id);
            System.out.println("sex = " + sex);
            System.out.println("date_birth = " + date_birth);
            System.out.println("ville_doctor = " + ville_doctor);
            System.out.println("pays_doctor = " + pays_doctor);
            System.out.println("dossier = "+ dossier);
            */
            res.add(id);
            res.add(sex);
            res.add(date_birth);
            res.add(dossier);
            res.add(city_doctor);
            
            
            //System.out.println("res = " + res);
            res2.add(i, res);
        }

        reader.close();
        
       
        
        return res2;
    }
    /*
    public static int[] NbAgeVille (String path, String ville) throws IOException, ParseException{
    	ArrayList<ArrayList<String>> liste = new ArrayList<ArrayList<String>>();
    	liste = SearchIndex(path, ville);
    	int[] age = new int[9];
    	int size = liste.size();
    	int cpt=0;
    	for (int i=0;i<size; i++){
    		if ( liste.get(i).get(2).length() == 10){
    			//System.out.println("birth = "+ liste.get(i).get(2) + ", = " + i + " ligne = " + liste.get(i));
        		int birth = Integer.parseInt(liste.get(i).get(2).substring(0, 4));
        		//System.out.println("birth ="+ birth);
        		int dossier = Integer.parseInt(liste.get(i).get(3).substring(0, 4));
        		//System.out.println("dossier ="+ dossier);
        		int difference = dossier-birth;
        		
        		if (difference<=10){
        			age[0]+=1;
        			//break;
        		}
        		if (10<difference && difference<=20){
        			age[1]+=1;
        			
        		}
        		if (20<difference && difference<=30){
        			age[2]+=1;
        			
        		}
        		if (30<difference && difference<=40){
        			age[3]+=1;
        			
        		}
        		if (40<difference && difference<=50){
        			age[4]+=1;
        			
        		}
        		if (50<difference && difference<=60){
        			age[5]+=1;
        			
        		}
        		if (60<difference && difference<=70){
        			age[6]+=1;
        			
        		}
        		if (70<difference && difference<=80){
        			age[7]+=1;
        			
        		}
        		if (80<difference){
        			age[8]+=1;
        			
        		}
    		}
    		else {
    			cpt +=1;
    		}
    		
    	}
    	System.out.println("age ="+ age[0]);
    	System.out.println("age ="+ age[1]);
    	System.out.println("age ="+ age[2]);
    	System.out.println("age ="+ age[3]);
    	System.out.println("age ="+ age[4]);
    	System.out.println("age ="+ age[5]);
    	System.out.println("age ="+ age[6]);
    	System.out.println("age ="+ age[8]);
    	System.out.println("age ="+ age[7]);
    	int somme = age[0]+age[1]+age[2]+age[3]+age[4]+age[5]+age[6]+age[7]+age[8];
    	System.out.println("somme ="+ somme);
    	System.out.println("cpt = "+ cpt);
    	
    	return age;
    }*/
    /*
    public static int NbConsultationVille (String path, String ville, String sex, String ageMin, String ageMax) throws IOException{
    	int NbConsultationVille = 0;
    	ArrayList<ArrayList<String>> liste = new ArrayList<ArrayList<String>>();
    	liste = SearchIndex(path, ville);
    	int size = liste.size(); 
    	for (int i=0; i<size; i++){
    		if (liste.get(i).get(1)==sex || sex.equals("")){
    			int birth = Integer.parseInt(liste.get(i).get(2).substring(0, 4));
        		//System.out.println("birth ="+ birth);
        		int dossier = Integer.parseInt(liste.get(i).get(3).substring(0, 4));
        		//System.out.println("dossier ="+ dossier);
        		int difference = dossier-birth;
    			if ( difference<Integer.parseInt(ageMax) && difference>Integer.parseInt(ageMin)){
    				NbConsultationVille +=1;
    			}
    		}
    	}
    	
    	return NbConsultationVille;
    }*/
    
    public static HashMap NbConsultation (String path, String sex, int ageMin, int ageMax){
    	HashMap NbConsultation = new HashMap();
    	//for 
    	
    	return NbConsultation;
    }
    
    /*public static ArrayList<String> NbAge (String path){
    	ArrayList<String> villes = new ArrayList<String>();
    	
    	return villes;
    }*/

    public static void main(String[] args) throws Exception {
    	//SearchIndex(args[0], args[1], "1","20");
    	int[] tab = nbPatientsparAge(args[0], args[1]);
    	int tt=0;
    	for(int i : tab){
    		tt+=i;
    		System.out.println(i);
    	}
    	System.out.println(tt);
    	//System.out.println(getNbPatients(args[0],args[1], "1", "20", "25"));
    }

}
