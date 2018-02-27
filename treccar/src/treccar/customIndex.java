package treccar;

import java.io.IOException;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.*;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.jetbrains.annotations.NotNull;
import java.io.*;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.util.Iterator;
import edu.unh.cs.treccar_v2.Data;
import edu.unh.cs.treccar_v2.read_data.DeserializeData;

/*
 * @author Ajesh Vijayaragavan
 * based on example from TREMA-UNH
 */

public class customIndex 
{
    public static void main(String[] args) throws IOException {
        System.setProperty("file.encoding", "UTF-8");       
        if (args.length != 2){
        	System.out.println("Format: Paragraphscborfile Pathtoluceneindex");
        	System.exit(-1);
        }
           
        final String paragraphsFile = args[0];
        	String indexPath = args[1];
            final FileInputStream fileInputStream2 = new FileInputStream(new File(paragraphsFile));
            
            System.out.println("Creating index....");
            
            final IndexWriter indexWriter = setupIndexWriter(indexPath, "Index");
            final Iterator<Data.Paragraph> paragraphIterator = DeserializeData.iterParagraphs(fileInputStream2);

            for (int i=1; paragraphIterator.hasNext(); i++){
                final Document doc = paragraphToLuceneDoc(paragraphIterator.next());
                indexWriter.addDocument(doc);
              }
            
            indexWriter.commit();
            indexWriter.close();
    }

    @NotNull
    private static Document paragraphToLuceneDoc(Data.Paragraph p) {
        final Document doc = new Document();
        final String content = p.getTextOnly(); // <-- Todo Adapt this to your needs!
        doc.add(new TextField("text", content, Field.Store.YES));
        doc.add(new StringField("paragraphid", p.getParaId(), Field.Store.YES));  // don't tokenize this!
        return doc;
    }


    @NotNull
    private static IndexWriter setupIndexWriter(String indexPath, String typeIndex) throws IOException {
        Path path = FileSystems.getDefault().getPath(indexPath, typeIndex);
        Directory indexDir = FSDirectory.open(path);
        IndexWriterConfig config = new IndexWriterConfig(new StandardAnalyzer());
        return new IndexWriter(indexDir, config);
    }
}