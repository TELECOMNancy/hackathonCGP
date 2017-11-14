package hackathon;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Date;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TopScoreDocCollector;
import org.apache.lucene.util.QueryBuilder;
import org.apache.lucene.queryparser.classic.QueryParserBase;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

public class SearchIndex {


    public static ArrayList<String> SearchIndex(String path, String search) throws IOException {
        System.out.println("Path = " + path);
        StandardAnalyzer analyzer = new StandardAnalyzer();
        Directory index = FSDirectory.open(Paths.get(path));
        System.out.println("length = " + index.listAll().toString().length());
        ArrayList<String> res = new ArrayList<String>();
        String id = "";
        String sex = "";
        String date_birth = null;
        String ville_doctor = "";
        String pays_doctor = "";
        //q est la recherche
        Query q = null;
        try {
            q = new QueryParser("MNCP_NAME", analyzer).parse(search);

        } catch (org.apache.lucene.queryparser.classic.ParseException e) {
            e.printStackTrace();
        }
        int hitsPerPage = 100;
        IndexReader reader = DirectoryReader.open(index);
        IndexSearcher searcher = new IndexSearcher(reader);
        TopScoreDocCollector collector = TopScoreDocCollector.create(hitsPerPage);
        searcher.search(q, collector);
        ScoreDoc[] hits = collector.topDocs().scoreDocs;
        // =============== Display  =============================
        System.out.println("Found " + hits.length + " hits.");
        for (int i = 0; i < hits.length; ++i) {
            int docId = hits[i].doc;
            Document d = searcher.doc(docId);
            System.out.println("document = " + d.getFields());
            id = d.get("PRSN_INTERNALID");
            sex = d.get("PRSN_SEX");
            date_birth = d.get("PRSN_BIRTHDATE");
            ville_doctor = d.get("VILLE_MEDECIN");
            pays_doctor = d.get("PAYS_MEDECIN");
            System.out.println("id = " + id);
            System.out.println("sex = " + sex);
            System.out.println("date_birth = " + date_birth);
            System.out.println("ville_doctor = " + ville_doctor);
            System.out.println("pays_doctor = " + pays_doctor);
            res.add(id);
            res.add(sex);
            res.add(date_birth);
            res.add(ville_doctor);
            res.add(pays_doctor);
        }

        reader.close();
        System.out.println("res = " + res.get(0));
        System.out.println("res = " + res.get(1));
        System.out.println("res = " + res.get(5));
        return res;
    }

    public static void main(String[] args) throws Exception {
        SearchIndex(args[0], args[1]);
    }

}
