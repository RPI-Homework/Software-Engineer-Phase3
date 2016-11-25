import java.io.*;
import java.util.*;
import java.util.Map.Entry;

// this class is used at run time tor gather information about methods
// and call sites, and to compute the coverage statistics.

public class RuntimeTracker {

    // directory where the output files should be written
    private static String out_dir;
    
    private static String Call_Site_ID;
    
    private static int StartEdges;
    private static int StartMethods;
    private static TreeMap<String, Integer> nedges;
    private static TreeMap<Integer, String> nmethods;

    // before anything else in RuntimeTracker is called, method
    // 'start' is invoked by the wrapper. The parameter is a directory
    // in which RuntimeTracker will find the necessary CHA-generated
    // files ("rmethods" and "edges"). In the same directory,
    // RuntimeTracker will write info about coverage statistics.
    public static void start(String io_dir) { 
	
	System.out.println("\n--- Instrumentation started in " + 
	io_dir + " ---\n");

	out_dir = io_dir;
	
	nedges = new TreeMap<String, Integer>();
	nmethods = new TreeMap<Integer, String>();
	
	Call_Site_ID = null;
	
	try
	{
		//Get all rmethods
		DataInputStream stream = new DataInputStream(new FileInputStream(io_dir + "/rmethods"));
		BufferedReader file = new BufferedReader(new InputStreamReader(stream));
		
		String line;
		while ((line = file.readLine()) != null)
		{
			  int divide = line.indexOf(":");
			  int Method_ID = Integer.parseInt(line.substring(0, divide));
			  String Method = line.substring(divide+1);
			  
			  nmethods.put(Method_ID, Method);
		}
		
		stream.close();
		
		//Get all edges
		stream = new DataInputStream(new FileInputStream(io_dir + "/edges"));		
		file = new BufferedReader(new InputStreamReader(stream));
		
		while ((line = file.readLine()) != null)
		{
			  int divide = line.indexOf(",");
			  //String Call_Site_ID = line.substring(0, divide);
			  int Method_ID = Integer.parseInt(line.substring(divide+1));
			  
			  nedges.put(line, Method_ID);
		}
		
		stream.close();
	} 
	catch (Exception e)
	{
		e.printStackTrace();
	}
	
	StartEdges = nedges.size();
	StartMethods = nmethods.size();
	
	// YOUR CODE HERE: read rmethods and edges, initialize
	// necessary data structures, etc.
    }
    
    // this method will be invoked by the wrapper at the end of the
    // execution; it should write the output files to disk
    public static void end() {

	System.out.println("\n--- Instrumentation ended ---\n");
	
	// output file for not-covered methods
	BufferedWriter nc_methods;
	
	// output file for not-covered edges
	BufferedWriter nc_edges; 

	try
	{
	    nc_methods = 
		new BufferedWriter(new FileWriter(out_dir + "/nmethods"));
	    nc_edges =
		new BufferedWriter(new FileWriter(out_dir + "/nedges"));
	    
	    for(Entry<Integer, String> iter : nmethods.entrySet())
	    {
	    	 nc_methods.write(iter.getKey() + ":" + iter.getValue());
	    	 nc_methods.newLine();
	    }
	    
	    nc_methods.write("Not covered: " + nmethods.size() + " out of " + StartMethods  + " [" + ((nmethods.size() * 100) / StartMethods) + "%]");
	    
	    for(Entry<String, Integer> iter : nedges.entrySet())
	    {
	    	 nc_edges.write(iter.getKey());
	    	 nc_edges.newLine();
	    }
	    
	    nc_edges.write("Not covered: " + nedges.size() + " out of " + StartEdges + " [" + ((nedges.size() * 100) / StartEdges) + "%]");

	    // close the files
	    nc_methods.close();
	    nc_edges.close();

	}
	catch (Exception e)
	{
		e.printStackTrace();
	}
	
    }

    // --------------------------------------------------------------
    // if this method is called, it means that the corresponding call
    // site is executed. calls to beforeCall are inserted by
    // MyTransformer.
    public static void beforeCall(String call_site_id)
    { 
		System.out.println("Call site: " + call_site_id);
		Call_Site_ID = call_site_id;
    }

    // ----------------------------------------------------- 
    // this means that the excution just entered some method. calls to
    // methodEntry are inserted by MyTransformer.
    public static void methodEntry(int method_id)
    { 
		System.out.println("Method: " + method_id);
		nmethods.remove(method_id);
		if(Call_Site_ID != null)
		{
			nedges.remove(Call_Site_ID + "," + method_id);
			Call_Site_ID = null;
		}
    }

    // ------------------------------------------- 
    // helper method: given x and y, returns their ratio. use this
    // when computing the percentage of not-covered methods and
    // not-covered edges
    public static String percent(long x, long y) {
	double z = (100.0*x) / ((double)y);
	String res = new String (String.valueOf(Math.round(z)));
	return res + "%";
    }
}
