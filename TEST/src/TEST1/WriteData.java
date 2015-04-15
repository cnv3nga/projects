package TEST1;
/** 
 * This program test write data to a file
 * @author tangmi
 *
 */

public class WriteData {
	public static void main(String[] args) throws Exception {
		java.io.File file = new java.io.File("scores.txt");
		if ( file.exists()) {
			System.out.println("File already exists! \n  The absolute path is " + 
		file.getAbsolutePath() + "\n Last Modified on " +new  java.util.Date(file.lastModified()));
			
			System.exit(0);
		}
		
		// Create a file
		java.io.PrintWriter output = new java.io.PrintWriter(file);
		
		// Write formatted output to the file
		output.print("John T Smith ");
		output.println(90);
		output.print("Eric K Jones ");
		output.println(85);
		
		// Close the file
		output.close();
	}
}
