import java.applet.Applet;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

public class Persistor extends Applet 
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
			File libdata = new File(new URI(homepath+"libdata.json"));
			BufferedReader f = new BufferedReader(new FileReader(libdata));
			String contents = f.readLine();
			f.close();
			return contents;
		} catch (Exception e) {
			String error = e.getMessage();
			return error;
		}
		
	}
	
	public String setLibraryData(String data)
	{
		try { 
			File libdata = new File(new URI(homepath+"libdata.json"));
			BufferedWriter f = new BufferedWriter(new FileWriter(libdata));
			f.write(data);
			f.close();
			return "success";
		} catch (Exception e) {
			String error = e.getMessage();
			return "failure - "+error;
		}
	}

	public String getLibraryData(String filename)
	{
		try { 
			File libdata = new File(filename);
			BufferedReader f = new BufferedReader(new FileReader(libdata));
			String contents = f.readLine();
			f.close();
			return contents;
		} catch (Exception e) {
			String error = e.getMessage();
			return error;
		}
		
	}
	
	public String setLibraryData(String data, String filename)
	{
		try { 
			File libdata = new File(filename);
			BufferedWriter f = new BufferedWriter(new FileWriter(libdata));
			f.write(data);
			f.close();
			return "success";
		} catch (Exception e) {
			String error = e.getMessage();
			return "failure - "+error;
		}
	}

	public String getKeyList()
	{
		// get list of all file keys, return as JSON list
		try {
			File basedir = new File(new URI(homepath));
			// get list of all dirs:
			File[] baselist = basedir.listFiles();
			List basedirlist = new ArrayList();
			for (int i=0; i < baselist.length; i++)
			{
				if (baselist[i].isDirectory())
				{
					basedirlist.add(baselist[i]);
				}
			}
			StringBuffer keylist = new StringBuffer("{");
			boolean first = true;
			for (int j=0; j < basedirlist.size(); j++)
			{
				File curdir = (File)basedirlist.get(j);
				String curdirname = curdir.getName();
				File[] curdirlist = curdir.listFiles();
				for (int k=0; k<curdirlist.length; k++)
				{
					if (first) first = false; else keylist.append(',');
					keylist.append("\""+curdirname + "/" + curdirlist[k].getName()+"\"");
				}
			}
			keylist.append("}");
			return keylist.toString();
		} catch (Exception e) {
			return "Error - "+e.getMessage();
		}
	}
	
	public void  writeTestFile(String s) throws Exception
	{
		File f1 = new File ("C:/testwrite.txt");
		BufferedWriter f = new BufferedWriter(new FileWriter(f1));
		f.write(s);
		f.close();
	}

	public String readTestFile() throws Exception
	{
		File f1 = new File ("C:/testread.txt");
		BufferedReader f = new BufferedReader(new FileReader(f1));
		String s = f.readLine();
		f.close();
		return s;
	}


}
