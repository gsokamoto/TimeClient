import java.net.*;
import java.io.*;

public class TimeClient
{
    private Socket timeSocket		 = null;
    private DataInputStream input = null;

    public TimeClient() throws IOException
    {
        timeSocket = new Socket("time.nist.gov", 13);
    }

    /*
     * Purpose: Gets server time from server through time client object
     * Return: (String) time.nist.gov's server time
     */
    public String getTime() throws IOException
    {
        input = new DataInputStream(timeSocket.getInputStream());
        int character;
        StringBuilder data = new StringBuilder();

        while ((character = input.read()) != -1) {
            data.append((char) character);
        }

        return data.toString();
    }
}
