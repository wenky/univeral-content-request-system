import java.io.IOException;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;


public class TODO
{
    void blah() throws IOException
    {
        // change handling of OutputStreams to InputStreams using pipes so we can theoretically exceed memory size
        
        PipedInputStream in = new PipedInputStream();
        PipedOutputStream out = new PipedOutputStream(in);
        new Thread(
          new Runnable(){
            public void run(){
              //class1.putDataOnOutputStream(out);
            }
          }
        ).start();
        //class2.processDataFromInputStream(in);
    }
    
    void blah2() 
    {
        // Permissive SSL
        // HttpClient version of URLConnection        
    }
}
