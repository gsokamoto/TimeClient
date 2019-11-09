import java.net.*;
import java.io.*;
import java.text.*;
import java.time.*;
import java.util.*;



public class Server
{
    //initialize socket and input stream
    private Socket		 socket = null;
    private ServerSocket server  = null;
    private DataInputStream in	 = null;
    private DataOutputStream out = null;

    // constructor creates server socket
    public Server(int portNumber) throws IOException
    {
        server = new ServerSocket(portNumber);
    }

    /*
     *
     */
    public void run() throws IOException, ParseException
    {
        System.out.println("Listening for connection on port 5000 ...");

        while(true)
        {
            try(Socket socket = server.accept())
            {
                // Accept browser request and parse
                String httpResponse = null;
                in = new DataInputStream(socket.getInputStream());
                BufferedReader inFromBrowserClient = new BufferedReader(new InputStreamReader(in));
                String request = inFromBrowserClient.readLine();
                System.out.println(request);

                // Check if request can be parsed
                if(request != null && request.contains(" ")) {
                    String[] requestParsed = request.split(" ");
                    request = requestParsed[1];

                }

                // Create /time response for client
                if(request != null && request.equals("/time"))
                {
                    httpResponse = createResponse("all");
                }

                // Create /time?zone= response for client
                else if(request != null && request.startsWith("/time?zone="))
                {
                    String timeZone = request.substring(11);
                    httpResponse = createResponse(timeZone);
                }

                // Send response to browser client
                out = new DataOutputStream(socket.getOutputStream());
                out.write(httpResponse.getBytes("UTF-8"));
            }
        }

    }

    /*
     * Purpose: Creates Response
     * Input: String timeZone
     * Return: (String) MM/DD/YYYY, null if can't be parsed
     */
    private String createResponse(String timeZone) throws IOException, ParseException
    {
        String timeZoneLC = timeZone.toLowerCase();
        TimeClient tc = new TimeClient();
        // Get time from server and parse
        String serverTime = tc.getTime();
        String[] serverTimeParsed = serverTime.split(" ");
        if(serverTimeParsed.length > 2 && !serverTimeParsed[1].equals("denied")) {
            // Generate Header
            String date = reformatDate(serverTimeParsed[1]);
            String time = reformatTime(serverTimeParsed[2]);
            String GMT = String.format("%s, %s", date, time);
            String EST = GMTtoEST(GMT);
            String PST = GMTtoPST(GMT);

            if(timeZoneLC.equals("all"))
                return String.format("HTTP/1.1 200 OK\r\n" +
                                     "Content-Type: text/html; charset=UTF-8\r\n\r\n" +
                                     "GMT Date/Time: %s<br/>EST Date/Time: %s<br/>PST Date/Time: %s",
                                     GMT, EST, PST);
            else if(timeZoneLC.equals("pst")) {
                return String.format("HTTP/1.1 200 OK\r\n" +
                                     "Content-Type: text/html; charset=UTF-8\r\n\r\n" +
                                     "PST Date/Time: %s",
                                     PST);
            }
            else if(timeZoneLC.equals("est")) {
                return String.format("HTTP/1.1 200 OK\r\n" +
                                     "Content-Type: text/html; charset=UTF-8\r\n\r\n" +
                                     "EST Date/Time: %s",
                                     EST);
            }
            return String.format("HTTP/1.1 200 OK\r\n" +
                    "Content-Type: text/html; charset=UTF-8\r\n\r\n" +
                    "Invalid Request");
        }
        // Access Denied -- Too Many Requests
        return  "HTTP/1.1 429 Too Many Requests\r\n" +
                "Content-Type: text/html; charset=UTF-8\r\n\r\n" +
                "Access Denied -- too many requests";
    }

    /*
     * Purpose: Converts date from time.nist.gov to US date format
     * Input: String date (format = yy-MM-dd)
     * Return: (String) MM/dd/yy, null if can't be parsed
     */
    private String reformatDate(String date)
    {

        if(date.contains("-")) {
            String[] dateParsed = date.split("-");
            String month = dateParsed[1];
            String year = dateParsed[0];
            String day = dateParsed[2];

            return String.format("%s/%s/%s", month, day, year);
        }
        System.out.println("date: ");
        return null;
    }

    /*
     * Purpose: Converts time from time.nist.gov to US date format
     * Input: String date (format: hh:mm a)
     * Return: (String) hh:mm a, null if can't be parsed
     */
    private String reformatTime(String time)
    {

        if(time.contains(":")) {
            String[] timeParsed = time.split(":");
            String minute = timeParsed[1];
            String meridies;
            int hour = Integer.parseInt(timeParsed[0]);
            if (hour > 12) {
                meridies = "PM";
                hour -= 12;
            }
            else
                meridies = "AM";
            if (hour == 0)
                hour = 12;

            return String.format("%d:%s %s", hour, minute, meridies);
        }
        System.out.println("time: ");
        return null;
    }

    /*
     * Purpose: Converts GMT to EST
     * Input: String GMT (GMT date format: MM/dd/yy, hh:mm a)
     * Return: (String) EST date format: MM/dd/yy, hh:mm a
     */
    private String GMTtoEST(String GMT) throws ParseException
    {
        SimpleDateFormat formatter = new SimpleDateFormat("MM/dd/yy, hh:mm a");
        Date GMTdate = formatter.parse(GMT);
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(GMTdate);
        calendar.add(Calendar.HOUR_OF_DAY, -4);
        return formatter.format(calendar.getTime());
    }

    /*
     * Purpose: Converts GMT to PST
     * Input: String GMT (GMT date format: MM/dd/yy, hh:mm a)
     * Return: (String) PST date format: MM/dd/yy, hh:mm a
     */
    private String GMTtoPST(String GMT) throws ParseException
    {
        SimpleDateFormat formatter = new SimpleDateFormat("MM/dd/yy, hh:mm a");
        Date GMTdate = formatter.parse(GMT);
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(GMTdate);
        calendar.add(Calendar.HOUR_OF_DAY, -7);
        return formatter.format(calendar.getTime());
    }

    public static void main(String args[]) throws IOException, ParseException
    {
       Server server = new Server(5000);
       server.run();
    }
}
