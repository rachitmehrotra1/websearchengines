import java.io.File;
import java.io.FileReader;
import java.io.IOException;
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
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopScoreDocCollector;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.store.RAMDirectory;
import org.apache.lucene.util.Version;
import org.apache.lucene.*;
import org.w3c.tidy.Tidy;

import org.apache.lucene.document.Field;
import org.apache.lucene.document.TextField;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;
import org.w3c.tidy.Tidy;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

public class JTidyHTMLHandler {

    public org.apache.lucene.document.Document
    getDocument(InputStream is,File f) {

        Tidy tidy = new Tidy();
        tidy.setQuiet(true);
        tidy.setShowWarnings(false);
        org.w3c.dom.Document root = tidy.parseDOM(is, null);
        Element rawDoc = root.getDocumentElement();

        org.apache.lucene.document.Document doc =
                new org.apache.lucene.document.Document();

        String title = getTitle(rawDoc);
        String body = getBody(rawDoc);
        String header = getHeaderelement(rawDoc);
        
        if ((title != null) && (!title.equals(""))) {
            doc.add(new TextField("title", title, Field.Store.YES));
        }
        if ((body != null) && (!body.equals(""))) {
            doc.add(new TextField("body", body, Field.Store.YES));
        }
        if ((header != null) && (!header.equals(""))) {
        doc.add(new TextField("header",header,Field.Store.YES));}
        doc.add(new TextField("location",f.toString(),Field.Store.YES));

        return doc;
    }

    /**
     * Gets the title text of the HTML document.
     *
     * @rawDoc the DOM Element to extract title Node from
     * @return the title text
     */
    protected String getTitle(Element rawDoc) {
        if (rawDoc == null) {
            return null;
        }

        String title = "";

        NodeList children = rawDoc.getElementsByTagName("title");
        if (children.getLength() > 0) {
            Element titleElement = ((Element) children.item(0));
            Text text = (Text) titleElement.getFirstChild();
            if (text != null) {
                title = text.getData();
            }
        }
        return title;
    }

    /**
     * Gets the body text of the HTML document.
     *
     * @rawDoc the DOM Element to extract body Node from
     * @return the body text
     */
    protected String getHeaderelement(Element rawDoc)
    {   if (rawDoc==null){
		return null;
    	}
    String header="";
    NodeList children = rawDoc.getElementsByTagName("h1");
    if(children.getLength() > 0){
    	 header = getText(children.item(0));
    }
    else
    {
    	children = rawDoc.getElementsByTagName("h2");
    	if(children.getLength() > 0){
       	 header = getText(children.item(0));
    			}
    	else
    	{
    		children = rawDoc.getElementsByTagName("h3");
    		if(children.getLength() > 0){
    	       	 header = getText(children.item(0));
    	    			}
    	}
    }
    
    return header;
    }
    protected String getBody(Element rawDoc) {
        if (rawDoc == null) {
            return null;
        }
        
        String body = "";
        NodeList children = rawDoc.getElementsByTagName("body");
        if (children.getLength() > 0) {
            body = getText(children.item(0));
        }
        return body;
    }

    /**
     * Extracts text from the DOM node.
     *
     * @param node a DOM node
     * @return the text value of the node
     */
    protected String getText(Node node) {
        NodeList children = node.getChildNodes();
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < children.getLength(); i++) {
            Node child = children.item(i);
            switch (child.getNodeType()) {
                case Node.ELEMENT_NODE:
                    sb.append(getText(child));
                    sb.append(" ");
                    break;
                case Node.TEXT_NODE:
                    sb.append(((Text) child).getData());
                    break;
            }
        }
        return sb.toString();
    }

    public static void main(String args[]) throws Exception {
        JTidyHTMLHandler handler = new JTidyHTMLHandler();
        org.apache.lucene.document.Document doc = handler.getDocument(
                new FileInputStream(new File(args[0])),new File(args[0]));
        System.out.println(doc);
    }
}