
package hackathon;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Date;

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

public class LuceneIndex {
    private LuceneIndex() {
    }

    /**
     * Index all text files under a directory.
     */
    public static void main(String[] args) {
        String usage = "java org.apache.lucene.demo.IndexFiles"
                + " [-index INDEX_PATH] [-docs DOCS_PATH] [-update]\n\n"
                + "This indexes the documents in DOCS_PATH, creating a Lucene index"
                + "in INDEX_PATH that can be searched with SearchFiles";
        String indexPath = "index";
        String docsPath = null;
        boolean create = true;
        for (int i = 0; i < args.length; i++) {
            if ("-index".equals(args[i])) {
                indexPath = args[i + 1];
                i++;
            } else if ("-docs".equals(args[i])) {
                docsPath = args[i + 1];
                i++;
            } else if ("-update".equals(args[i])) {
                create = false;
            }
        }

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
            System.out.println("Indexing to directory '" + indexPath + "'...");

            Directory dir = FSDirectory.open(Paths.get(indexPath));
            Analyzer analyzer = new StandardAnalyzer();
            IndexWriterConfig iwc = new IndexWriterConfig(analyzer);

            if (create) {
                // Create a new index in the directory, removing any
                // previously indexed documents:
                iwc.setOpenMode(OpenMode.CREATE);
            } else {
                // Add new documents to an existing index:
                iwc.setOpenMode(OpenMode.CREATE_OR_APPEND);
            }

            IndexWriter writer = new IndexWriter(dir, iwc);
            indexDocsByMNCP(writer, docDir);

            writer.close();

            Date end = new Date();
            System.out.println(end.getTime() - start.getTime() + " total milliseconds");

        } catch (IOException e) {
            System.out.println(" caught a " + e.getClass() +
                    "\n with message: " + e.getMessage());
        }
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

                System.out.print("line = " + line + "\n");
                String[] tmp = line.split(";");
                System.out.println("tmp = " + tmp[4]);
                String MNCP_NAME = tmp[4];
                String PRSN_INTERNALID = tmp[0];
                String PRSN_SEX = tmp[2];
                String VILLE_MEDECIN = tmp[6];
                String PAYS_MEDECIN = tmp[7];
                String PRSN_BIRTHDATE = tmp[1];
                doc.add(new TextField("MNCP_NAME", MNCP_NAME, Store.YES));
                doc.add(new StoredField("PRSN_INTERNALID", PRSN_INTERNALID));
                doc.add(new StoredField("PRSN_SEX", PRSN_SEX));
                doc.add(new StoredField("VILLE_MEDECIN", VILLE_MEDECIN));
                doc.add(new StoredField("PAYS_MEDECIN", PAYS_MEDECIN));
                doc.add(new StoredField("PRSN_BIRTHDATE", PRSN_BIRTHDATE));
                writer.addDocument(doc);

                doc = new Document();

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

                System.out.print("line = " + line + "\n");
                String[] tmp = line.split(";");
                System.out.println("tmp = " + tmp[4]);
                String MNCP_NAME = tmp[4];
                String PRSN_INTERNALID = tmp[0];
                String PRSN_SEX = tmp[2];
                String VILLE_MEDECIN = tmp[6];
                String PAYS_MEDECIN = tmp[7];
                String PRSN_BIRTHDATE = tmp[1];
                doc.add(new StoredField("MNCP_NAME", MNCP_NAME));
                doc.add(new StoredField("PRSN_INTERNALID", PRSN_INTERNALID));
                doc.add(new StoredField("PRSN_SEX", PRSN_SEX));
                doc.add(new TextField("VILLE_MEDECIN", VILLE_MEDECIN, Store.YES));
                doc.add(new StoredField("PAYS_MEDECIN", PAYS_MEDECIN));
                doc.add(new StoredField("PRSN_BIRTHDATE", PRSN_BIRTHDATE));
                writer.addDocument(doc);

                doc = new Document();

            }

            writerUpdate(writer, file, doc);
        }
    }
}