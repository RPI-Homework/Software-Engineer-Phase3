import soot.*;
import soot.jimple.*;
import java.io.*;
import java.util.*;

public class ChaWriter {

    // directory containing the output files
    private String output_dir;

    // file storing info about call sites inside non-library methods
    private BufferedWriter call_file;

    // file representing the call edges
    private BufferedWriter call_edges;

    // auxiliary variable used to filter out call sites inside library
    // methods
    private boolean inside_nonlib_method = false;

    // the id of the last complex call site. needed to print the call
    // edges
    private String last_site;

    // A Hierarchy object providing necessary info for the printing
    private Hierarchy hierarchy;
    

    // --------------------------------
    public ChaWriter(String dir_name, Hierarchy h) { 

	output_dir = dir_name; 
	hierarchy = h;

	// open files to store info about all calls in non-library methods
	try {
	    call_file =
		new BufferedWriter(new FileWriter(output_dir + "/calls"));
	    call_edges =
		new BufferedWriter(new FileWriter(output_dir + "/edges"));
	} catch (Exception e) {
	    System.out.println("OOPS! " + e);
	}
    }
    
    
    public void writeMethodInfo(Hashtable reachable) {
	
	// open files to store the info
	try {
	    BufferedWriter file =
		new BufferedWriter(new FileWriter(output_dir + "/rmethods_all"));
	    BufferedWriter file_nl =
		new BufferedWriter(new FileWriter(output_dir + "/rmethods"));
	
	    file.write("Total num reachable methods: " + reachable.size() + "\n");
	    for (Iterator it = reachable.keySet().iterator(); it.hasNext();) {
		SootMethod m = (SootMethod) it.next();
		Integer id = (Integer) reachable.get(m);
		file.write(id + ": " + m + "\n");
		if (hierarchy.notLibrary(m)) 
		    file_nl.write(id + ": " + m + "\n");
	    }
	    file.close();
	    file_nl.close();
	} catch (Exception e) {
	    System.out.println("OOPS! " + e);
	}
    }
    public void writeHierarchyInfo() {

	// open a file to store the info
	try {
	    BufferedWriter file =
		new BufferedWriter(new FileWriter(output_dir + "/hier_all"));
	    BufferedWriter file_nl =
		new BufferedWriter(new FileWriter(output_dir + "/hier"));
	
	    Set C = hierarchy.allClasses();
	    file.write("Total num classes: " + C.size() + "\n");
	    for (Iterator it = C.iterator(); it.hasNext();) {
		SootClass c = (SootClass) it.next();
		file.write(c + "," + 
			   hierarchy.possibleReceiverClasses(c).size() + "\n");
		if (hierarchy.notLibrary(c)) 
		    file_nl.write(c + "," + 
				  hierarchy.possibleReceiverClasses(c).size() + 
				  "\n");
	    }
	    file.close();
	    file_nl.close();
	} catch (Exception e) {
	    System.out.println("OOPS! " + e);
	}
    }

    public void startNewMethod(SootMethod m, int method_id) {
	inside_nonlib_method = hierarchy.notLibrary(m);
	if (inside_nonlib_method) 
	    try {
		call_file.write("\n===== Method " + method_id + ": "
				+ m + "\n");
	    } catch (Exception e) {
		System.out.println("OOPS! " + e);
	    }
    }

    public void writeSimpleCall(InvokeExpr call, String call_site_id,
				int target_method_id) {
	if (inside_nonlib_method) 
	    try {
		call_file.write(call_site_id + ": [S] " + call + "\n");

		// also need to write call edges. we only care about
		// targets that are non-library methods.  all library
		// methods are assigned id = 0.
		if (target_method_id != 0)
		    call_edges.write(call_site_id + "," + 
				     target_method_id + "\n");
	    } catch (Exception e) {
		System.out.println("OOPS! " + e);
	    }
    }

    public void writeComplexCall(InvokeExpr call,
				 String call_site_id,
				 int num_rcv_classes, 
				 int num_target_methods) {
	if (inside_nonlib_method) 
	    try {
		call_file.write(call_site_id + ": [C] " + call + "," + 
				num_rcv_classes + "," + 
				num_target_methods + "\n");
		last_site = call_site_id;
	    } catch (Exception e) {
		System.out.println("OOPS! " + e);
	    }
    }

    public void writeTarget(SootMethod m, int target_method_id) {
	if (inside_nonlib_method) 
	    try {
		call_file.write("     " + m + "\n");
		// also write the call edges, but only if the target
		// is a non-library method
		if (target_method_id != 0)
		    call_edges.write(last_site + "," + 
				     target_method_id + "\n");
	    } catch (Exception e) {
		System.out.println("OOPS! " + e);
	    }
    }

    public void done() {
	try {
	    call_file.close();
	    call_edges.close();

	} catch (Exception e) {
	    System.out.println("OOPS! " + e);
	}
    }

}
