// A minimal Web Crawler written in Java
// Usage: From command line 
//     java WebCrawler <URL> [N]
//  where URL is the url to start the crawl, and N (optional)
//  is the maximum number of pages to download.
//Rachit Mehrotra
//rm4149@nyu.edu


import java.text.*;
import java.util.*;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.net.*;

import java.io.*;
import org.apache.commons.cli.*;
import org.apache.commons.cli.ParseException;


public class WebCrawler {
    public static final int    SEARCH_LIMIT = 50;  // Absolute max pages 
    public static final boolean DEBUG = false;
    public static final String DISALLOW = "Disallow:";
    public static final int MAXSIZE = 20000; // Max size of file 

    // URLs to be searched
    Vector<URL> newURLs;
    // Known URLs
    Hashtable<URL,List<Integer>> knownURLs;
    // max number of pages to download
    int maxPages; 
    int insert_counter=0;
    String[] query=null;;
    Boolean trace=false;

// initializes data structures.  argv is the command line arguments.

public void initialize(String argv, CommandLine cmd, String temp) {
    URL url;
    knownURLs = new Hashtable<URL,List<Integer>>();
    newURLs = new Vector<URL>();
    try { url = new URL(argv); }
      catch (MalformedURLException e) {
        System.out.println("Invalid starting URL " + argv);
        return;
      }
    
    knownURLs.put(url,Arrays.asList(new Integer(0),new Integer(99),new Integer(++insert_counter)));
    newURLs.addElement(url);
    System.out.println("Crawling for relevant to \""+temp+"\" starting from	" + url.toString());
    maxPages = SEARCH_LIMIT;
    if (cmd.hasOption("m")) {
       int iPages = Integer.parseInt(cmd.getOptionValue("m"));
       if (iPages < maxPages) maxPages = iPages; }
    System.out.println("Maximum number of pages:" + maxPages);

/*Behind a firewall set your proxy and port here!
*/
    Properties props= new Properties(System.getProperties());
    props.put("http.proxySet", "true");
    props.put("http.proxyHost", "webcache-cup");
    props.put("http.proxyPort", "8080");

    Properties newprops = new Properties(props);
    System.setProperties(newprops);
/**/
}

// Check that the robot exclusion protocol does not disallow
// downloading url.

public boolean robotSafe(URL url) {
    String strHost = url.getHost();

	// form URL of the robots.txt file
    String strRobot = "http://" + strHost + "/robots.txt";
    URL urlRobot;
    try { urlRobot = new URL(strRobot);
	} catch (MalformedURLException e) {
	    // something weird is happening, so don't trust it
	    return false;
	}

    if (DEBUG) System.out.println("Checking robot protocol " + 
                                   urlRobot.toString());
    String strCommands;
    try {
       InputStream urlRobotStream = urlRobot.openStream();

	    // read in entire file
       byte b[] = new byte[1000];
       int numRead = urlRobotStream.read(b);
       strCommands = new String(b, 0, numRead);
       while (numRead != -1) {
          numRead = urlRobotStream.read(b);
          if (numRead != -1) {
             String newCommands = new String(b, 0, numRead);
	         strCommands += newCommands;
		}
	    }
       urlRobotStream.close();
	} catch (IOException e) {
	    // if there is no robots.txt file, it is OK to search
	    return true;
	}
        if (DEBUG) System.out.println(strCommands);

	// assume that this robots.txt refers to us and 
	// search for "Disallow:" commands.
	String strURL = url.getFile();
	int index = 0;
	while ((index = strCommands.indexOf(DISALLOW, index)) != -1) {
	    index += DISALLOW.length();
	    String strPath = strCommands.substring(index);
	    StringTokenizer st = new StringTokenizer(strPath);

	    if (!st.hasMoreTokens())
		break;
	    
	    String strBadPath = st.nextToken();

	    // if the URL starts with a disallowed path, it is not safe
	    if (strURL.indexOf(strBadPath) == 0)
		return false;
	}

	return true;
    }

// adds new URL to the queue. Accept only new URL's that end in
// htm or html. oldURL is the context, newURLString is the link
// (either an absolute or a relative URL).

public void addnewurl(URL oldURL, String newUrlString, int scorez)

{ URL url; 
  if (DEBUG) System.out.println("URL String " + newUrlString);
  try { url = new URL(oldURL,newUrlString);
        if (!knownURLs.containsKey(url)) 
        {
           String filename =  url.getFile();
           int iSuffix = filename.lastIndexOf("htm");
           if ((iSuffix == filename.length() - 3) ||   (iSuffix == filename.length() - 4)) {
        	   //PUT SCORING HERE
              
              newURLs.addElement(url);
              knownURLs.put(url,Arrays.asList(new Integer(scorez),new Integer(99),new Integer(++insert_counter)));
              if(trace)
              if(knownURLs.get(url).get(1)!=1)
              System.out.println("Adding to queue:" + url.toString()+". Score = "+scorez);
              
              }		
              
           } 
        else
        {	if(knownURLs.get(url).get(1)!=1){
        	if(trace)
        	System.out.println("Adding "+scorez+" to the score of " + url.toString()+".");
        	
        	knownURLs.put(url, Arrays.asList(knownURLs.get(url).get(0)+scorez ,knownURLs.get(url).get(1),knownURLs.get(url).get(2)));
        }
        }
        
        }
  catch (MalformedURLException e) { return; }
}




public int score(String M,String P,int iEnd, URL url)
{  //System.out.println(M);
	if(query==null) {
		return 0;
	}
	else
	{
		int anchor_s=P.indexOf(">", iEnd);
		int anchor_e=P.indexOf("</a>", anchor_s);
		int K_match=0;
		//System.out.println("Start is:"+anchor_s + "  End is:"+anchor_e);
		String anchor=P.substring(anchor_s+1, anchor_e);
		//System.out.println("URL: "+M.toString()+"  anchor is:"+anchor);
		
			for(int i=0;i<query.length;i++)
			{
				if(anchor.toLowerCase().contains(query[i]))
				{
					K_match++;
				}
				
			}
			if(K_match>0)
			{		//System.out.println("Score is "+K_match*50 + " for anchor : "+ anchor + "  and UrL:"+M);
				return K_match*50;
			}
			
			for(int i=0;i<query.length;i++)
			{
				if(M.toLowerCase().contains(query[i]))
				{
					//System.out.println("Score is 40 for anchor : "+ anchor + "  and UrL:"+M);
					return 40;
				}
			}
			String fhalf=P.substring(0,anchor_s+1).replaceAll("\\<.*?>","");;
			String shalf=P.substring(anchor_e, P.length()-1).replaceAll("\\<.*?>","");;
			
		
			String temp="";
			//System.out.println("----------------------------------FHALF-----------------------------------------------");
			Pattern p = Pattern.compile("[\\w']+");
			Matcher m = p.matcher(fhalf);

			while ( m.find() ) {
				temp+=	fhalf.substring(m.start(), m.end())+" ";
			}
			
			String[] first=temp.split(" ");
			
//			for(int i=0;i<first.length;i++)
//				{
//					System.out.println(first[i]);
//				}
//			System.out.println("---------------------------------ANCHOR------------------------------------------------");
//			System.out.println(anchor);
//			System.out.println("--------------------------------SHALF-------------------------------------------------");			
			temp=" ";
			 m = p.matcher(shalf);

			while ( m.find() ) {
				temp+=	shalf.substring(m.start(), m.end())+" ";
			}
			
			String[] second=temp.split(" ");
//			for(int i=0;i<second.length;i++)
//			{
//				System.out.println(second[i]);
//			}
//			
//			System.out.println("--------------------------------END-------------------------------------------------");
			// START SHALF from 1 instead of 0!!!
			//CODING U SCORING
			
			int u=0;
			for (int j=0;j<query.length;j++)
			{ int found=0;
				for(int i=1;i<=5;i++)
				{
//					System.out.println(i+":Query checking-->"+query[j]);
//					System.out.println("First value is-->"+first[first.length-i]);
//					System.out.println("value of u:"+u);
					if(first.length>i-1)
					if(first[first.length-i].compareTo(query[j])==0)
					{	if(found==0){
						u++;found=1;break;
					}
					}
					
					if(second.length>i){
//						System.out.println("  Query checking-->"+query[j]);
//						System.out.println("Second value is-->"+second[i]);
//						System.out.println("value of u:"+u);
					if(second[i].compareTo(query[j])==0)
					{if(found==0){
						u++;found=1;break;
					}
					}
				
					}
				}
					
			}
			//CODING V Scoring
			String noHTML = P.replaceAll("\\<.*?>","");
			
			String[] v_string=noHTML.split(" ");
//			System.out.println("---------------------------------------------------------------------------------");
//			for(int i=0;i<v_string.length;i++)
//			{
//				System.out.println(v_string[i]);
//			}
//			System.out.println("---------------------------------------------------------------------------------");
			int v=0;
			for (int i=0;i<query.length;i++)
			{	for(int j=0;j<v_string.length;j++)
				if(v_string[j].replaceAll("\\s","").replaceAll("\\p{P}","").compareTo(query[i])==0)
				{	//System.out.println("True for"+query[i]);
					v++;
					break;
				}
			}
			//System.out.println("Value of u and v-->"+u+"<--->"+v);
			//System.out.println("Score is "+((4*Math.abs(u))+Math.abs(v-u))+ " for anchor : "+ anchor + "  and UrL:"+M);
			return ((4*Math.abs(u))+Math.abs(v-u));
			
			
			
			
			
//			System.out.println("---------------------------------------------------------------------------------");
//			System.out.println(P);
//			System.out.println("---------------------------------------------------------------------------------");
//			System.out.println(noHTML);
//			System.out.println("---------------------------------------------------------------------------------");
//			System.out.println("---------------------------------------------------------------------------------");
//			String[] p_words=noHTML.split(" ");
//			for(int i=0;i<p_words.length;i++)
//			{
//				System.out.println(p_words[i]);
//			}
//			System.out.println("---------------------------------------------------------------------------------");
			
	}
	
	
	
}









// Download contents of URL

public String getpage(URL url,CommandLine cmd)

{ try { 
    // try opening the URL
    URLConnection urlConnection = url.openConnection();
    if(trace){
    System.out.println();
    System.out.println("Downloading: " + url.toString() + ". Score ="+ knownURLs.get(url).get(0));
    }
    //Setting visited FLAG TO 1
    knownURLs.put(url,Arrays.asList(knownURLs.get(url).get(0),1,knownURLs.get(url).get(2)));
    urlConnection.setAllowUserInteraction(false);

    InputStream urlStream = url.openStream();
   
    String urlz=url.toString();
    String[] name;
  FileOutputStream fos=null; 
if(!urlz.substring( urlz.lastIndexOf('/')+1, urlz.length() ).isEmpty())
    fos = new FileOutputStream(new File(cmd.getOptionValue("docs")+urlz.substring( urlz.lastIndexOf('/')+1, urlz.length() )));
else
{    name=urlz.split("/");
	//System.out.println(name[name.length-1]);
	fos = new FileOutputStream(new File(cmd.getOptionValue("docs")+name[name.length-1]+".html"));
}
		// search the input stream for links
		// first, read in the entire URL
    byte b[] = new byte[1000];
    int numRead = urlStream.read(b);
    String content = new String(b, 0, numRead);
    
    fos.write(b, 0, numRead);
    while ((numRead != -1) && (content.length() < MAXSIZE)) {
       numRead = urlStream.read(b);
       if (numRead != -1) {
         String newContent = new String(b, 0, numRead);
         content += newContent;
        // if(!urlz.substring( urlz.lastIndexOf('/')+1, urlz.length() ).isEmpty())
         fos.write(b, 0, numRead);
		    }
		}
   // if(!urlz.substring( urlz.lastIndexOf('/')+1, urlz.length() ).isEmpty()){
    fos.flush();
   fos.close();
   if(trace)
   System.out.println("Recieved:"+url.toString()+".");
    return content;
  
 } catch (IOException e) {
	   
       System.out.println("ERROR: couldn't open URL "+e);
       e.printStackTrace();
       return "";
    }  }

// Go through page finding links to URLs.  A link is signalled
// by <a href=" ...   It ends with a close angle bracket, preceded
// by a close quote, possibly preceded by a hatch mark (marking a
// fragment, an internal page marker)

public void processpage(URL url, String page)	

{ String lcPage = page.toLowerCase(); // Page in lower case
  int index = 0; // position in page
  int iEndAngle, ihref, iURL, iCloseQuote, iHatchMark, iEnd;
  while ((index = lcPage.indexOf("<a",index)) != -1) {
    iEndAngle = lcPage.indexOf(">",index);
    ihref = lcPage.indexOf("href",index);
    if (ihref != -1) {
      iURL = lcPage.indexOf("\"", ihref) + 1; 
      if ((iURL != -1) && (iEndAngle != -1) && (iURL < iEndAngle))
        { iCloseQuote = lcPage.indexOf("\"",iURL);
          iHatchMark = lcPage.indexOf("#", iURL);
          if ((iCloseQuote != -1) && (iCloseQuote < iEndAngle)) {
            iEnd = iCloseQuote;
            if ((iHatchMark != -1) && (iHatchMark < iCloseQuote))
             iEnd = iHatchMark;
            String newUrlString = page.substring(iURL,iEnd);
            //System.out.println("url is:"+newUrlString);
          //CALL SCORE FROM HERE(pass iEnd)
           int scorez= score(newUrlString,lcPage,iEnd,url);
            addnewurl(url, newUrlString,scorez); 
             
         } } }
    index = iEndAngle;
   }
}

// Top-level procedure. Keep popping a url off newURLs, download
// it, and accumulate new URLs

public void run(String[] argv)

{ try {
	Options options = new Options();
	options.addOption("u", true, "URL");
	options.addOption("q",true, "query");
//	Option que=new Option("q","queryy");
//	que.setArgs(Option.UNLIMITED_VALUES);
//	options.addOption(que);
	
	//Option asd=Option.builder("q").hasArgs().build();
	
	//options.addOption(asd);
	// Option optionz = Option.builder("q").hasArgs().build();
	//options.addOption(optionz);
	options.addOption("docs", true, "path");
	options.addOption("m",true ,"max pages");
	options.addOption("t", false, "trace flag");
	
	CommandLineParser parser = new DefaultParser();
	
	CommandLine	cmd = parser.parse( options, argv);
	if(cmd.hasOption("t"))
	{
		trace=true;
	}
//	if(cmd.hasOption("u")) {
//	   System.out.println("The value of u is->"+cmd.getOptionValue("u"));
//	}
	String temp="";
	if(cmd.hasOption("q")) {
		int startc = 0;
		int count=0;
		
		for(int i=0;i<argv.length;i++)
		{
			if(argv[i].contains("-q"))
				startc=i;
		}
		for(int i=startc+1;i<argv.length;i++)
		{
			if(argv[i].charAt(0)=='-')
			{
				break;
			}
			else
			{
				count++;
			}
		}
		String[] test =new String[count];
		int j=0;
		for(int i=startc+1;i<(startc+1+count);i++)
		{
			test[j]=argv[i];
			j++;
		}
		
		//query=cmd.getOptionValue("q");
		
	
		
		   for(int i=0;i<test.length;i++)
		   {
			   if(test[i].charAt(0)!='-')
			   {
				   temp+=test[i].toLowerCase()+" ";
				   
			   }
			   else{
				   break;
			   }
		   }
		   //System.out.println("The query is-->"+temp+"<----");
		   query=temp.split(" ");
//		   for(int i=0;i<query.length;i++)
//		   {
//			  System.out.println(query[i]);
//		   }
	
	}
//	if(cmd.hasOption("docs")) {
//		   System.out.println("The value of docs is->"+cmd.getOptionValue("docs"));
//		}
//	if(cmd.hasOption("m")) {
//		   System.out.println("The value of m is->"+cmd.getOptionValue("m"));
//		}
//	
//	if(cmd.hasOption("t")) {
//		   System.out.print("The value of t is->"+cmd.hasOption("t"));
//		}
	
	
	initialize(cmd.getOptionValue("u"),cmd,temp);
  for (int i = 0; i < maxPages; i++) {
   
	
    
    Entry<URL,List<Integer>> maxEntry = null;
   // System.out.println("---------------------------------max strt------------------------------------------------");
    for(Entry<URL, List<Integer>> entry : knownURLs.entrySet()) {
    
        
        if ((maxEntry == null || entry.getValue().get(0) >= maxEntry.getValue().get(0))&&(entry.getValue().get(1)==99)) {
           if(maxEntry!=null){
         //  System.out.println("detail of entry:"+entry+"  with score:"+entry.getValue().get(0));
		  // System.out.println("detail of max entry:"+maxEntry+"  with score:"+maxEntry.getValue().get(0));
        	if(entry.getValue().get(0).intValue()==maxEntry.getValue().get(0).intValue()){
        	//	System.out.println("found same score");
        	   if(entry.getValue().get(2).intValue()<=maxEntry.getValue().get(2).intValue())
        	   {
        		  
        		   maxEntry = entry; 
        		 //  System.out.println("New max is :----->"+maxEntry);
        	   }
           }
           else
           {
        	   maxEntry = entry;   
           }
           }
           else
           {
        	   maxEntry = entry;   
           }
        	
        }
    }
//    System.out.println("---------------------------------max end-----------------------------------------FINAL MAX-------");
//    System.out.println(maxEntry);
//    System.out.println("-----------------------KNOWN URLS----------------------------------------------------------");
//	System.out.println(knownURLs);
//    System.out.println("-------------------------CHOSEN ONE--------------------------------------------------------");
//	

    
    
    URL url = maxEntry.getKey();
    newURLs.remove(url);
//    System.out.println(url);
//    System.out.println("---------------------------------END------------------------------------------------");
    if (DEBUG) System.out.println("Searching " + url.toString());
    if (robotSafe(url)) {
      String page = getpage(url,cmd);
      if (DEBUG) System.out.println(page);
      if (page.length() != 0) processpage(url,page);
      if (newURLs.isEmpty()) break;
     }
  }
  System.out.println("Search complete.");
 // System.out.print("Contents of the known hashtable are:"+ knownURLs);
} catch (ParseException e) {
	// TODO Auto-generated catch block
	//e.printStackTrace();
	System.out.println("Error at parsing");
}
} 

public static void main(String[] argv)
{ WebCrawler wc = new WebCrawler();
  wc.run(argv);
  
  
  
}




}