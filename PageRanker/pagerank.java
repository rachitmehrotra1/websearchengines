
import java.text.*;
import java.util.*;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.net.*;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.io.*;
import org.apache.commons.cli.*;
import org.apache.commons.cli.ParseException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;


//Rachit Mehrotra
//rm4149@nyu.edu


public class pagerank {
  public static String docs;
  public static float fm, epsilon;
  public static int N=0;
  public static List<Float> P_base=new ArrayList<Float>();
  
  public static List<Float> P_score=new ArrayList<Float>();
  public static List<Float> P_newscore=new ArrayList<Float>();
  public static List<String> filez = new ArrayList<String>();
  public static Float[][] Weight;
  public static Float[] W_Sum;
  public static void showFiles(File[] files, String docs2) throws IOException {
	    for (File file : files) {
	        if (file.isDirectory()) {
	            //System.out.println("Directory: " + file.getName());
	            showFiles(file.listFiles(),docs2); // Calls same method again.
	        } else {
	            //System.out.println("File: " + file.getName());
	            try {
	            	String content="";
					if(file.getName().endsWith("html")||file.getName().endsWith("htm"))
					{ 
						N++;
						content = readFile(file.getName(), Charset.defaultCharset(),docs2);
					//System.out.println(countWords(content));
					filez.add(docs2+file.getName());
					P_base.add((float) (Math.log(countWords(content))/Math.log(2)));
					}
					} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
	        }
	    }
	}
  static String readFile(String path, Charset encoding,String docs2) 
		  throws IOException 
		{
		  byte[] encoded = Files.readAllBytes(Paths.get(docs2+path));
		  return new String(encoded, encoding);
		}
  private static int countWords(String html) throws Exception {
	    org.jsoup.nodes.Document dom = Jsoup.parse(html);
	    String text = dom.text();

	    return text.split(" ").length;
	}
	@SuppressWarnings("deprecation")
	public void run(String[] argv) throws IOException
	{try {
		Options options = new Options();

		options.addOption("docs", true, "path");
		options.addOption("f",true ,"f-measure");
		
		
		CommandLineParser parser = new DefaultParser();
		
		
		CommandLine	cmd = parser.parse( options, argv);
		
		if(cmd.hasOption("docs"))
		{
			
			docs=(String) cmd.getOptionObject("docs");
			//System.out.println(docs);
		}
		if(cmd.hasOption("f"))
		{
			String temp;
			temp=(String) cmd.getOptionObject("f");
			fm= Float.parseFloat(temp);
			//System.out.println(fm);
		}
		
	
		
		File[] fi = new File(docs).listFiles();
							try {
								showFiles(fi,docs);
							} catch (IOException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
							epsilon=(float) (0.01/N);
							//System.out.println("N is "+N+" Epsilon-->"+epsilon);
					//System.out.println(filez.toString());
					//System.out.println(P_base.toString());
		float sum=0;
		for(int i=0;i<P_base.size();i++)
		{
			sum+=P_base.get(i);
		}
		//System.out.println("Sum is:"+sum);
		for(int i=0;i<P_base.size();i++)
		{
			float temp=P_base.get(i)/sum;
			P_score.add(temp);
			P_base.set(i, temp);
		}
		
		//System.out.println("After calculating score:");
		//System.out.println(P_base.toString());
		//System.out.println(P_score.toString());
		
		Weight = new Float[N][N];
		W_Sum = new Float[N];
		for(int i=0;i<N;i++)
			for(int j=0;j<N;j++)
				Weight[i][j]=(float) 0;
		
		for(int i=0;i<filez.size();i++)
		{	File input=new File(filez.get(i));
		
			Document doc = Jsoup.parse(input, "UTF-8", "http://example.com/");
			Elements links = doc.select("a[href]");
			if(links.isEmpty())
			{ //System.out.println("Links are empty for"+filez.get(i));
				for(int j=0;j<N;j++)
				{
					Weight[j][i]=(float) P_base.get(j);
				}
			}
			else{
				//ELSE PART
														for (Element link : links) {
														    String href = link.attr("href");
														    //System.out.println("Cropped Link(Title): "+ link.text());
														    //System.out.println("title: " + filez.get(i));
														    //System.out.println("href: " + href);
														    //System.out.println("Score of doc-->"+scoring(doc,filez.get(i),href));
														    Weight[filez.indexOf(docs+href)][i]=(float) scoring(doc,filez.get(i),href);
														}  
														////System.out.println(filez.get(i)+"--->"+linkhref);
														//System.out.println();
														W_Sum[i]=(float) 0;
														for(int j=0;j<filez.size();j++)
														{
															W_Sum[i]+=Weight[j][i];
														}
														//System.out.println("Sum for-->"+filez.get(i)+"<-->"+W_Sum[i]);
														Vector<String> visited=new Vector<String>();
														for (Element link : links) {
														    String href = link.attr("href");
														   
//														    //System.out.println("title: " + filez.get(i));
//														    //System.out.println("href: " + href);
														    float temp=Weight[filez.indexOf(docs+href)][i];
														    //System.out.println("------------Start----------");
														    //System.out.println(input + " with "+href +" W_Sum is "+ W_Sum[i] + " score is "+temp );
														    //System.out.println("------------End----------");
															  if(!visited.contains(href)) 
														    Weight[filez.indexOf(docs+href)][i]=temp/W_Sum[i];
															  visited.addElement(href);
														}  
														////System.out.println(filez.get(i)+"--->"+linkhref);
														//System.out.println();
			}
		}
		
		//System.out.println("Weight matrix-NORMALIZED");
		for(int i=0;i<N;i++)
		{//System.out.print(filez.get(i)+"\t");}
		}
			//System.out.println();
		for(int i=0;i<N;i++)
			{////System.out.print(filez.get(i)+"\t");
			for(int j=0;j<N;j++)
				{
//				//System.out.print(Weight[i][j]+"\t");

				//System.out.print(String.format("%-20s" , Weight[i][j] ));
				
				}
			//System.out.println();
			}
		
//		//System.out.println("Weight matrix-UNNORMALIZED");
//		for(int i=0;i<N;i++)
//			//System.out.print(filez.get(i)+"\t");
//		//System.out.println();
//		for(int i=0;i<N;i++)
//			{////System.out.print(filez.get(i)+"\t");
//			for(int j=0;j<N;j++)
//				{
////				//System.out.print(Weight[i][j]+"\t");
//
//				//System.out.print(String.format("%-20s" , Weight[i][j]*W_Sum[j] ));
//				
//				}
//			//System.out.println();}
			
		
		Boolean flag=true;
		for(int i=0;i<filez.size();i++)
		{
			P_newscore.add((float) 0);
		}
		do
		{flag=false;
		for(int i=0;i<filez.size();i++)
		{	float new_score = 0;
			float Q_score=0;
			for(int j=0;j<filez.size();j++)
			{
				Q_score+=P_score.get(j)*Weight[i][j];
			}
			//System.out.println("Q score is "+ Q_score);
			for(int j=0;j<filez.size();j++)
			{
				new_score=(1-fm)*P_base.get(i)+fm*Q_score;	
			}
			
			P_newscore.add(i, new_score);
												if(Math.abs(P_newscore.get(i)-P_score.get(i))>epsilon)
									{
													flag=true;
									}
		}
												
												for(int k=0;k<filez.size();k++)
			{ //System.out.println("Setting new score as ->"+P_newscore.get(k)+"   old score being->"+P_score.get(k));
				P_score.set(k, P_newscore.get(k));
			}
		
			
		} while (flag);
		
		for(int i=0;i<N;i++)
		{
			for(int j = 0 ; j<N;j++)
			{
				if(P_score.get(i)>P_score.get(j))
				{
					String tempz=filez.get(i);
					filez.set(i, filez.get(j));
					filez.set(j, tempz);
					Float temp=P_score.get(i);
					P_score.set(i, P_score.get(j));
					P_score.set(j, temp);
					
				}
			}
		}
		//System.out.println("");
		System.out.println("Final new score");
		for(int i=0;i<filez.size();i++)
		{
			System.out.println(filez.get(i).substring(filez.get(i).lastIndexOf("/")+1, filez.get(i).lastIndexOf(".")	)+"  "+P_score.get(i));
		}
		
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	public int scoring(Document doc , String file , String url)
	{	int score=0;
		Elements links = doc.select("a[href]");
		for (Element link : links) {
		    String href = link.attr("href");
		    //System.out.println("Comparing to->"+file.substring(file.lastIndexOf("/"), file.length()-1));
		    if(href.compareTo(url)==0)
		    {
		    	score++;
		    }
		    }
		Elements links_b = doc.select("b > a[href]");
	
		for (Element link : links_b) {
		    String href = link.attr("href");
		    //System.out.println("******************************");
		   //System.out.println("Link inside bold "+href+ " in "+ file);
		    // //System.out.println("Comparing to->"+file.substring(file.lastIndexOf("/"), file.length()-1));
		    if(href.compareTo(url)==0)
		    {
		    	score++;
		    }
		    }
		Elements links_h1 = doc.select("h1 > a[href]");
		
		for (Element link : links_h1) {
		    String href = link.attr("href");
		    //System.out.println("******************************");
		   //System.out.println("Link inside bold "+href+ " in "+ file);
		    // //System.out.println("Comparing to->"+file.substring(file.lastIndexOf("/"), file.length()-1));
		    if(href.compareTo(url)==0)
		    {
		    	score++;
		    }
		    }
		Elements links_h2 = doc.select("h2 > a[href]");
		
		for (Element link : links_h2) {
		    String href = link.attr("href");
		    //System.out.println("******************************");
		   //System.out.println("Link inside bold "+href+ " in "+ file);
		    // //System.out.println("Comparing to->"+file.substring(file.lastIndexOf("/"), file.length()-1));
		    if(href.compareTo(url)==0)
		    {
		    	score++;
		    }
		    }
		Elements links_h3 = doc.select("h3 > a[href]");
		
		for (Element link : links_h3) {
		    String href = link.attr("href");
		    //System.out.println("******************************");
		   //System.out.println("Link inside bold "+href+ " in "+ file);
		    // //System.out.println("Comparing to->"+file.substring(file.lastIndexOf("/"), file.length()-1));
		    if(href.compareTo(url)==0)
		    {
		    	score++;
		    }
		    }
		Elements links_h4 = doc.select("h4 > a[href]");
		
		for (Element link : links_h4) {
		    String href = link.attr("href");
		    //System.out.println("******************************");
		   //System.out.println("Link inside bold "+href+ " in "+ file);
		    // //System.out.println("Comparing to->"+file.substring(file.lastIndexOf("/"), file.length()-1));
		    if(href.compareTo(url)==0)
		    {
		    	score++;
		    }
		    }
		Elements links_em = doc.select("em > a[href]");
		
		for (Element link : links_em) {
		    String href = link.attr("href");
		    //System.out.println("******************************");
		   //System.out.println("Link inside bold "+href+ " in "+ file);
		    // //System.out.println("Comparing to->"+file.substring(file.lastIndexOf("/"), file.length()-1));
		    if(href.compareTo(url)==0)
		    {
		    	score++;
		    }
		    }
		
		return score;
	}
	
	public static void main(String[] argv) throws IOException
	{
//	P_base.add((float) (Math.log(94)/Math.log(2)));
//	P_base.add((float) (Math.log(74)/Math.log(2)));
//	P_base.add((float) (Math.log(123)/Math.log(2)));
//	P_base.add((float) (Math.log(58)/Math.log(2)));
//	P_base.add((float) (Math.log(134)/Math.log(2)));
//	P_base.add((float) (Math.log(192)/Math.log(2)));
//	P_base.add((float) (Math.log(101)/Math.log(2)));
//	P_base.add((float) (Math.log(189)/Math.log(2)));
	
		
		pagerank pr=new pagerank();
	pr.run(argv);
	}
}

