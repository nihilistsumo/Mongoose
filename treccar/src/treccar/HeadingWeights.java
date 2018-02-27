package treccar;

import edu.unh.cs.treccar_v2.Data;
import edu.unh.cs.treccar_v2.Data.Section;
import edu.unh.cs.treccar_v2.read_data.DeserializeData;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.*;
import org.apache.lucene.search.*;
import org.apache.lucene.search.similarities.BM25Similarity;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.jetbrains.annotations.NotNull;
import org.omg.Messaging.SyncScopeHelper;

import java.io.FileInputStream;
import java.util.Collections;
import java.util.ArrayList;
import java.nio.file.Path;
import java.io.FileWriter;
import java.io.StringReader;
import java.nio.file.FileSystems;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import java.io.File;

/*
 * @author Ajesh Vijayaragavan
 */

public class HeadingWeights {

    static class MyQueryBuilder {

        private final StandardAnalyzer analyzer;
        private List<String> tokens;

        public MyQueryBuilder(StandardAnalyzer standardAnalyzer){
            analyzer = standardAnalyzer;
            tokens = new ArrayList<String>(128);
        }

        public BooleanQuery toQuery(String queryStr) throws IOException {

            TokenStream tokenStream = analyzer.tokenStream("text", new StringReader(queryStr));
            tokenStream.reset();
            tokens.clear();
            while (tokenStream.incrementToken()) {
                final String token = tokenStream.getAttribute(CharTermAttribute.class).toString();
                tokens.add(token);
            }
            tokenStream.end();
            tokenStream.close();
            BooleanQuery.Builder booleanQuery = new BooleanQuery.Builder();
            for (String token : tokens) {
                booleanQuery.add(new TermQuery(new Term("text", token)), BooleanClause.Occur.SHOULD);
            }
            return booleanQuery.build();
        }
    }

    public static void main(String[] args) throws IOException {
        System.setProperty("file.encoding", "UTF-8");

        String indexPath = args[1];
        if (args.length != 5) {
        	System.out.println("Format: Outlinescborfile IndexPath Pagenameoutput lowestheadingoutput interiorheadingoutput");
        	System.exit(-1);
        }
        
            IndexSearcher searcher = setupIndexSearcher(indexPath, "paragraph.lucene");
            searcher.setSimilarity(new BM25Similarity());
            final MyQueryBuilder queryBuilder = new MyQueryBuilder(new StandardAnalyzer());
            final String pagesFile = args[0];
            
            //BM25-pagename
            PrintWriter out1 = new PrintWriter(new FileWriter(args[2]));
            final FileInputStream fileInputStream1 = new FileInputStream(new File(pagesFile));
            for (Data.Page page : DeserializeData.iterableAnnotations(fileInputStream1)) {
                final String queryId = page.getPageId();

                String queryStr1 = buildSectionQueryStrpn(page, Collections.<Data.Section>emptyList());

                TopDocs tops = searcher.search(queryBuilder.toQuery(queryStr1), 100);
                ScoreDoc[] scoreDoc = tops.scoreDocs;
                String paragraphid2 = null;
                for (int i = 0; i < scoreDoc.length; i++) {
                    ScoreDoc score = scoreDoc[i];
                    final Document doc = searcher.doc(score.doc);
                    final String paragraphid = doc.getField("paragraphid").stringValue();
                    final float searchScore = score.score;
                    final int searchRank = i+1;
                    if (paragraphid != paragraphid2)
                    	out1.println(queryId+" Q0 "+paragraphid+" "+searchRank + " "+searchScore+" Lucene-BM25");
                    paragraphid2 = paragraphid;   
                }
            }
        out1.close();
        
	      	//Lowest heading
	        PrintWriter out2 = new PrintWriter(new FileWriter(args[3]));
	        String queryId2 = null;
	        String queryStr2 = null;
	        final FileInputStream fileInputStream2 = new FileInputStream(new File(pagesFile));
	        for (Data.Page page : DeserializeData.iterableAnnotations(fileInputStream2)) {
	            for (List<Data.Section> sectionPath : page.flatSectionPaths()) {
	                queryId2 = Data.sectionPathId(page.getPageId(), sectionPath);   
	                queryStr2 = buildSectionQueryStrlh(page, sectionPath);
	            }
	                TopDocs tops = searcher.search(queryBuilder.toQuery(queryStr2), 100);
	                ScoreDoc[] scoreDoc = tops.scoreDocs;
	                String paragraphid2 = null;
	                for (int i = 0; i < scoreDoc.length; i++) {
	                    ScoreDoc score = scoreDoc[i];
	                    final Document doc = searcher.doc(score.doc);
	                    final String paragraphid = doc.getField("paragraphid").stringValue();
	                    final float searchScore = score.score;
	                    final int searchRank = i+1;
	                    if (paragraphid != paragraphid2)
	                    	out2.println(queryId2+" Q0 "+paragraphid+" "+searchRank + " "+searchScore+" Lucene-BM25");
	                    paragraphid2 = paragraphid;
	                }
	            //}
	        }
	        out2.close();
            
            //interior headings concatenated
            PrintWriter out3 = new PrintWriter(new FileWriter(args[4]));
            final FileInputStream fileInputStream3 = new FileInputStream(new File(pagesFile));
            for (Data.Page page : DeserializeData.iterableAnnotations(fileInputStream3)) {
                for (List<Data.Section> sectionPath : page.flatSectionPaths()) {
                    final String queryId3 = Data.sectionPathId(page.getPageId(), sectionPath);
                    String queryStr3 = buildSectionQueryStrihc(page, sectionPath);
                    TopDocs tops = searcher.search(queryBuilder.toQuery(queryStr3), 100);
                    ScoreDoc[] scoreDoc = tops.scoreDocs;
                    String paragraphid2 = null;
                    for (int i = 0; i < scoreDoc.length; i++) {
                        ScoreDoc score = scoreDoc[i];
                        final Document doc = searcher.doc(score.doc);
                        final String paragraphid = doc.getField("paragraphid").stringValue();
                        final float searchScore = score.score;
                        final int searchRank = i+1;
                        if (paragraphid != paragraphid2)
                        	out3.println(queryId3+" Q0 "+paragraphid+" "+searchRank + " "+searchScore+" Lucene-BM25");
                        paragraphid2 = paragraphid;
                    }
                }               
            }
            out3.close();
       }     
    
    @NotNull
    private static IndexSearcher setupIndexSearcher(String indexPath, String typeIndex) throws IOException {
        Path path = FileSystems.getDefault().getPath(indexPath, typeIndex);
        Directory indexDir = FSDirectory.open(path);
        IndexReader reader = DirectoryReader.open(indexDir);
        return new IndexSearcher(reader);
    }
    
    @NotNull
    private static String buildSectionQueryStrpn(Data.Page page, List<Data.Section> sectionPath) {
        StringBuilder queryStr1 = new StringBuilder();
        queryStr1.append(page.getPageName());
        return queryStr1.toString();
    }

    @NotNull
    private static String buildSectionQueryStrlh(Data.Page page, List<Data.Section> sectionPath) {
    	Section queryStr2;
        queryStr2 = sectionPath.get(sectionPath.size() - 1);
        return queryStr2.toString();
    }
      
    @NotNull
    private static String buildSectionQueryStrihc(Data.Page page, List<Data.Section> sectionPath) {
        StringBuilder queryStr3 = new StringBuilder();
        for (Data.Section section: sectionPath) {
            queryStr3.append(" ").append(section.getHeading());          
        }
        return queryStr3.toString();
    }
}