
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queryparser.*;
import org.apache.lucene.queryparser.surround.parser.QueryParser;
import org.apache.lucene.search.*;
import org.apache.lucene.search.BooleanClause.Occur;
import org.apache.lucene.search.BooleanQuery.Builder;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.QueryBuilder;
 
 
public class Searcher {
    private IndexSearcher searcher = null;
    private QueryParser parser = null;
	private Query hs;

    public Searcher(String[] args) throws IOException {
    	
    	final Map<String, List<String>> params = new HashMap<>();

    	List<String> options = null;
    	for (int i = 0; i < args.length; i++) {
    	    final String a = args[i];

    	    if (a.charAt(0) == '-') {
    	        if (a.length() < 2) {
    	            System.err.println("Error at argument " + a);
    	            return;
    	        }

    	        options = new ArrayList<>();
    	        params.put(a.substring(1), options);
    	    }
    	    else if (options != null) {
    	        options.add(a);
    	    }
    	    else {
    	        System.err.println("Illegal parameter usage");
    	        return;
    	    }
    	}
    	
        searcher = new IndexSearcher(DirectoryReader.open(FSDirectory.open(Paths.get(params.get("index").get(0)))));
        TopScoreDocCollector collector = TopScoreDocCollector.create(10);
        QueryBuilder builder=new QueryBuilder(new StandardAnalyzer());
        
		Builder main1= new BooleanQuery.Builder();
        for(int i=0;i<params.get("q").size();i++)
        {
        hs=builder.createPhraseQuery("body",params.get("q").get(i));

        main1.add(hs, Occur.SHOULD);
        }
        BooleanQuery finalq = main1.build();
         
        searcher.search(finalq, collector);
        ScoreDoc[] hits = collector.topDocs().scoreDocs;
        
        // System.out.println(" Found " + hits.length + " hits."); 
        for(int i=0;i<hits.length;++i) {
            int docId = hits[i].doc;
            Document d = searcher.doc(docId);
            if(d.get("title")!=null){
            System.out.println((i + 1) + ". " + d.get("title") );}
            else if (d.get("header")!=null){
            	System.out.println((i + 1) + ". " + d.get("header")  );}
            else{
            	System.out.println((i + 1) + ". " + "null" );}
           System.out.println("Location:"+ d.get("location"));
        }
        
    }
    public static void main (String[] args) throws IOException
    {
    new Searcher(args);
    	
    }

    
}