package main;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import edu.unh.cs.treccar_v2.Data;
import edu.unh.cs.treccar_v2.read_data.DeserializeData;


public class BuildIndex 
{
	private  static String INDEX_DIR ;
	private  static String CBOR_FILE ;
	
	public BuildIndex(String INDEX_DIR,String CBOR_FILE)
	{
		BuildIndex.INDEX_DIR = INDEX_DIR;
		BuildIndex.CBOR_FILE = CBOR_FILE ;
	}
	private static IndexWriter createWriter()throws IOException
	{
		Directory indexdir = FSDirectory.open((new File(INDEX_DIR)).toPath());
		IndexWriterConfig conf = new IndexWriterConfig(new StandardAnalyzer());
		conf.setOpenMode(IndexWriterConfig.OpenMode.CREATE);
		IndexWriter iw = new IndexWriter(indexdir, conf);
		return iw;
	}
	private static Document createDocument(Data.Paragraph para)
	{
		Document paradoc = new Document();
		paradoc.add(new StringField("paraid", para.getParaId(), Field.Store.YES));
		paradoc.add(new TextField("parabody", para.getTextOnly(), Field.Store.YES));
		System.out.println(para.getParaId());
		return paradoc;
	}
	public static void createIndex()throws IOException
	{
		IndexWriter writer = createWriter();
		for (Data.Paragraph p : DeserializeData.iterableParagraphs(new FileInputStream(new File(CBOR_FILE))))
		{
			Document d = createDocument(p);
			writer.addDocument(d);
		}
		writer.close();
	}
	
}