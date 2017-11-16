
package hackathon;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Date;
import java.util.TreeSet;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field.Store;
import org.apache.lucene.document.LongPoint;
import org.apache.lucene.document.StoredField;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.IndexWriterConfig.OpenMode;
import org.apache.lucene.index.IndexableField;
import org.apache.lucene.index.Term;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

import java.io.FileReader;

public class LuceneIndex {
    
	//private ArrayList<String> listCities = new ArrayList<String>();
    /**
     * Index all text files under a directory.
     * @throws IOException 
     */
    public static void main(String[] args) throws IOException {
        String usage = "java org.apache.lucene.demo.IndexFiles"
                + " [-index INDEX_PATH] [-docs DOCS_PATH] [-update]\n\n"
                + "This indexes the documents in DOCS_PATH, creating a Lucene index"
                + "in INDEX_PATH that can be searched with SearchFiles";
        
        
        String indexPathMNCP = args[0];
        String indexPathMED = args[1];
        String docsPath = args[2];
        String pathFile = args[3];
        
        listCities(docsPath, pathFile);
         
        if (docsPath == null) {
            System.err.println("Usage: " + usage);
            System.exit(1);
        }

        final Path docDir = Paths.get(docsPath);
        if (!Files.isReadable(docDir)) {
            System.out.println("Document directory '" + docDir.toAbsolutePath() + "' does not exist or is not readable, please check the path");
            System.exit(1);
        }

        Date start = new Date();
        try {
            System.out.println("Indexing to directory '" + indexPathMNCP + "'...");

            Directory dirMNCP = FSDirectory.open(Paths.get(indexPathMNCP));
            Analyzer analyzerMNCP = new StandardAnalyzer();
            IndexWriterConfig iwcMNCP = new IndexWriterConfig(analyzerMNCP);
            iwcMNCP.setOpenMode(OpenMode.CREATE_OR_APPEND);
            
            
            System.out.println("Indexing to directory '" + indexPathMED + "'...");

            Directory dirMED = FSDirectory.open(Paths.get(indexPathMED));
            Analyzer analyzerMED = new StandardAnalyzer();
            IndexWriterConfig iwcMED = new IndexWriterConfig(analyzerMED);
            iwcMED.setOpenMode(OpenMode.CREATE_OR_APPEND);

            IndexWriter writerMNCP = new IndexWriter(dirMNCP, iwcMNCP);
            IndexWriter writerMED= new IndexWriter(dirMED, iwcMED);
            indexDocsByMNCP(writerMNCP, docDir);
            indexDocsByMED(writerMED, docDir);
            
            writerMNCP.close();
            writerMED.close();

            Date end = new Date();
            System.out.println(end.getTime() - start.getTime() + " total milliseconds");

        } catch (IOException e) {
            System.out.println(" caught a " + e.getClass() +
                    "\n with message: " + e.getMessage());
        }
    }
    
    static void listCities(String docsPath, String pathFile) throws IOException{
    	BufferedReader fileCSV = new BufferedReader(new FileReader(docsPath));
		String line;
		String[] sline;
		ArrayList<String> cities = new ArrayList<String>();
		BufferedWriter writer = new BufferedWriter(new FileWriter(new File(pathFile)));
		// normalement si le fichier n'existe pas, il est crée à la racine du projet
		int cpt =0;
		fileCSV.readLine();
		while((line = fileCSV.readLine())!= null){
			sline = line.split(";");
			if(!cities.contains(sline[4])){
				cities.add(sline[4]);
				writer.write(sline[4] + "\n");
				cpt+=1;
				
			}				
		}
		System.out.println("compteur ="+ cpt);
		writer.close();
		fileCSV.close();
    }

    static void indexDocsByMNCP(final IndexWriter writer, Path path) throws IOException {
        if (Files.isDirectory(path)) {
            Files.walkFileTree(path, new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    try {
                        indexDocByMNCP(writer, file, attrs.lastModifiedTime().toMillis());
                    } catch (IOException ignore) {
                        // don't index files that can't be read.
                    }
                    return FileVisitResult.CONTINUE;
                }
            });
        } else {
            indexDocByMNCP(writer, path, Files.getLastModifiedTime(path).toMillis());
        }
    }

    /**
     * Indexes a single document
     */
    static void indexDocByMNCP(IndexWriter writer, Path file, long lastModified) throws IOException {
        System.out.println("file = " + file);
        try (InputStream stream = Files.newInputStream(file)) {
        	TreeSet<String> tSet = new TreeSet<String>();
            System.out.println("stream = " + stream);
            InputStreamReader ipsr = new InputStreamReader(stream, StandardCharsets.UTF_8);
            System.out.println("ipsr = " + ipsr.getEncoding());
            BufferedReader br = new BufferedReader(ipsr);
            // make a new, empty document
            Document doc = new Document();
            String line;
            IndexableField pathField = new StringField("path", file.toString(), Store.YES);
            doc.add(pathField);
            doc.add(new LongPoint("modified", lastModified));

            System.out.println("buff = " + br.readLine());
            while ((line = br.readLine()) != null) {
            	String[] tmp = line.split(";");
            	if (tmp[1].length() == 10){
            		//System.out.print("line = " + line + "\n");
                    
                    //System.out.println("tmp = " + tmp[4]);
                    String MNCP_NAME = tmp[4];
                    tSet.add(MNCP_NAME);
                    String PRSN_INTERNALID = tmp[0];
                    String PRSN_SEX = tmp[2];
                    String VILLE_MEDECIN = tmp[6];
                    String PAYS_MEDECIN = tmp[7];
                    String PRSN_BIRTHDATE = tmp[1];
                    String DATE_DOSSIER = tmp[5];
                    String PRSN_AGE = String.valueOf(Integer.parseInt(tmp[1].substring(0, 4))-Integer.parseInt(tmp[5].substring(0, 4)));
                    
                    //System.out.println("tmp = " + tmp[5]);
                    doc.add(new TextField("MNCP_NAME", MNCP_NAME, Store.YES));
                    doc.add(new StoredField("PRSN_INTERNALID", PRSN_INTERNALID));
                    doc.add(new TextField("PRSN_SEX", PRSN_SEX, Store.YES));
                    doc.add(new StoredField("VILLE_MEDECIN", VILLE_MEDECIN));
                    doc.add(new StoredField("PAYS_MEDECIN", PAYS_MEDECIN));
                    doc.add(new StoredField("PRSN_BIRTHDATE", PRSN_BIRTHDATE));
                    doc.add(new TextField("PRSN_AGE",PRSN_AGE, Store.YES));
                    doc.add(new StoredField("DATE_DOSSIER", DATE_DOSSIER));
                    writer.addDocument(doc);

                    doc = new Document();
            	}

            }
                
            writerUpdateUpdate(writer, file, doc);
        }
    }

    private static void writerUpdateUpdate(IndexWriter writer, Watchable file, Iterable<IndexableField> doc) throws IOException {
        writerUpdate(writer, file, doc);
    }

    private static void writerUpdate(IndexWriter writer, Watchable file, Iterable<IndexableField> doc) throws IOException {
        if (writer.getConfig().getOpenMode() == OpenMode.CREATE) {
            // New index, so we just add the document (no old document can be there):
            System.out.println("adding " + file);
            writer.addDocument(doc);
        } else {
            // Existing index (an old copy of this document may have been indexed) so
            // we use updateDocument instead to replace the old one matching the exact
            // path, if present:
            System.out.println("updating " + file);
            writer.updateDocument(new Term("path", file.toString()), doc);
        }
    }


    static void indexDocsByMED(final IndexWriter writer, Path path) throws IOException {
        if (Files.isDirectory(path)) {
            Files.walkFileTree(path, new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    try {
                        indexDocByMED(writer, file, attrs.lastModifiedTime().toMillis());
                    } catch (IOException ignore) {
                        // don't index files that can't be read.
                    }
                    return FileVisitResult.CONTINUE;
                }
            });
        } else {
            indexDocByMED(writer, path, Files.getLastModifiedTime(path).toMillis());
        }
    }

    /**
     * Indexes a single document
     */
    static void indexDocByMED(IndexWriter writer, Path file, long lastModified) throws IOException {
        System.out.println("file = " + file);
        try (InputStream stream = Files.newInputStream(file)) {

            System.out.println("stream = " + stream);
            InputStreamReader ipsr = new InputStreamReader(stream, StandardCharsets.UTF_8);
            System.out.println("ipsr = " + ipsr.getEncoding());
            BufferedReader br = new BufferedReader(ipsr);
            // make a new, empty document
            Document doc = new Document();
            String line;
            IndexableField pathField = new StringField("path", file.toString(), Store.YES);
            doc.add(pathField);
            doc.add(new LongPoint("modified", lastModified));

            System.out.println("buff = " + br.readLine());
            while ((line = br.readLine()) != null) {
            	String[] tmp = line.split(";");
            	if (tmp[1].length() == 10){
            		//System.out.print("line = " + line + "\n");
                    
                    //System.out.println("tmp = " + tmp[4]);
                    String MNCP_NAME = tmp[4];
                    String PRSN_INTERNALID = tmp[0];
                    String PRSN_SEX = tmp[2];
                    String VILLE_MEDECIN = tmp[6];
                    String PAYS_MEDECIN = tmp[7];
                    String PRSN_BIRTHDATE = tmp[1];
                    String DATE_DOSSIER = tmp[5];
                    String PRSN_AGE = String.valueOf(Integer.parseInt(tmp[1].substring(0, 4))-Integer.parseInt(tmp[5].substring(0, 4)));
                    
                    doc.add(new TextField("VILLE_MEDECIN", VILLE_MEDECIN, Store.YES));
                    doc.add(new StoredField("MNCP_NAME", MNCP_NAME));
                    doc.add(new StoredField("PRSN_INTERNALID", PRSN_INTERNALID));
                    doc.add(new TextField("PRSN_SEX", PRSN_SEX,Store.YES));
                    doc.add(new StoredField("PAYS_MEDECIN", PAYS_MEDECIN));
                    doc.add(new StoredField("PRSN_BIRTHDATE", PRSN_BIRTHDATE));
                    doc.add(new StoredField("DATE_DOSSIER", DATE_DOSSIER));
                    doc.add(new TextField("PRSN_AGE",PRSN_AGE, Store.YES));
                    writer.addDocument(doc);

                    doc = new Document();
            	}
                
            }

            writerUpdate(writer, file, doc);
        }
    }
}