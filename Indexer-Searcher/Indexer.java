import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Date;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.store.RAMDirectory;
public class Indexer {
	
	public static void main (String args[]) throws Exception
	{ String usage = "Indexer "
	                    + " [-index INDEX_PATH] [-docs DOCS_PATH] \n\n"
	                    + "This indexes the documents in DOCS_PATH, creating a Lucene index"
	                    + "in INDEX_PATH that can be searched with SearchFiles";
	       String indexPath = "index";
	       String docsPath = null;
	       
	       for(int i=0;i<args.length;i++) {
	        if ("-index".equals(args[i])) {
	          indexPath = args[i+1];
	          i++;
	         } else if ("-docs".equals(args[i])) {
	           docsPath = args[i+1];
	          i++;
	         } 
	       }
	   
	       if (docsPath == null) {
	        System.err.println("Usage: " + usage);
	         System.exit(1);
	       }	
	Directory dataDir= FSDirectory.open(Paths.get(docsPath));
	
	 long start = new Date().getTime();
	  int numIndexed = index(FSDirectory.open(Paths.get(indexPath)), dataDir,docsPath);
	  long end = new Date().getTime();
	  System.out.println("Indexing " + numIndexed + " files took "
	    + (end - start) + " milliseconds");

	  
	
		
		
	}
	public static int index (Directory indexDir,Directory datadir, String docsPath) throws IOException
	{
		IndexWriter write = new IndexWriter(indexDir, new IndexWriterConfig(new StandardAnalyzer()));
		indexDirectory(write,datadir,docsPath+"/");
		int count=write.maxDoc();
		write.close();	
		return count;
		
	}
	private static void indexDirectory(IndexWriter writer, Directory datadir,String addr)
			 throws IOException {
		  String[] files = datadir.listAll();
		  for(int i=0;i< files.length;i++ )
		  {
			  System.out.println(files[i]);
			  
			  
		  }
		  for (int i = 0; i < files.length; i++) {
		    File f = new File(addr+"/"+files[i]);
		   if (f.isDirectory())
		   {		System.out.println(f.toString());
			   indexDirectory(writer,FSDirectory.open(Paths.get(f.toString())),f.toString());
		   }
		   else
		   {
		 if (f.getName().endsWith(".html")||f.getName().endsWith(".htm")) {
		    System.out.println(f.getAbsolutePath());
		      indexFile(writer, f);
		   }
		   }
		  				} 
		  }
	
	
	private static void indexFile(IndexWriter writer, File f)
			  throws IOException {
			//  if ( !f.canRead()) {
			//	  System.out.println("Cant read");
			//    return;
		//	}
			  System.out.println("Indexing " + f.getCanonicalPath());
			  
			  JTidyHTMLHandler handler = new JTidyHTMLHandler();
			  org.apache.lucene.document.Document doc = handler.getDocument(
		                new FileInputStream(f),f);
			 
			writer.addDocument(doc); 
			}
}


