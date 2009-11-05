import java.applet.Applet;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.Map;

public class LibraryApplet extends Applet
{
	String homepath = "";
	
	public void setHomepath(String path)
	{
		path = path.substring(0,path.length()-"techlib.html".length());
		homepath = path;
	}
	
	public String getHomepath()
	{
		return homepath;
	}
	
	public String getLibraryData()
	{
		try { 
			File libdata = new File(homepath+"libdata.json");
			BufferedReader f = new BufferedReader(new FileReader(libdata));
			String contents = f.readLine();
			f.close();
			return contents;
		} catch (Exception e) {
			String error = e.getMessage();
			return error;
		}
		
	}
	
	// write-to-file
	public boolean SaveLibrary(Map bookmappings)
	{
		return false;
	}
	
	public String hello() {return "hello"; }
	
}
