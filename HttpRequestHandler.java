package project1_sambathpich;

import java.io.*;
import java.net.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.StringTokenizer;

final class HttpRequestHandler implements Runnable
{
    Socket mySocket;
    boolean keeAlive = true;

    // Constructor
    public HttpRequestHandler(Socket socket) throws Exception {
        this.mySocket = socket;
    }

    // Implement run() method
    public void run() {
        while(keeAlive)
        {
            try {
                startProcess();
            }
            catch (Exception e) {
                keeAlive = false;
            }
        }
    }

    private void startProcess() throws Exception {
        /* ::::: VARIABLES ::::: */
        String firstRequestLine, remainingRequestLines, userAgent = null, getIfModifiedSince = null;
        String rootLocation = "/Users/brossam/WebServerProject/";
        String fileName, strRN = "\r\n";
        String statusLine, contentType, contentLength, receiveDate, myCookie = null;
        String htmlOutput = null;
        Date date = new Date();

        /* ::::: INPUT AND OUTPUT ::::: */
        InputStream myInputStream = this.mySocket.getInputStream();
        DataOutputStream myOutputStream = new DataOutputStream(this.mySocket.getOutputStream());

        //Read myInputStream
        BufferedReader myBufferedReader = new BufferedReader(new InputStreamReader(myInputStream));

        /* ::::: DISPLAY REQUEST MESSAGE ::::: */
        //First line of HTTP request
        firstRequestLine = myBufferedReader.readLine();
        System.out.println(firstRequestLine);

        //Remaining Lines of HTTP request

        while ((remainingRequestLines = myBufferedReader.readLine()).length() != 0) {
            System.out.println(remainingRequestLines);

            //Retrieve User-Agent and ifModifiedSince from Request
            if(remainingRequestLines.toLowerCase().contains("user-agent".toLowerCase())) {
                userAgent = remainingRequestLines.substring(12,remainingRequestLines.length());
            }

            if(remainingRequestLines.toLowerCase().contains("if-modified-since".toLowerCase())) {
                getIfModifiedSince = remainingRequestLines.substring(19,remainingRequestLines.length());
            }

        } System.out.println("\n");

        /* ::::: Extract FILE_NAME from first line ::::: */
        StringTokenizer tokens = new StringTokenizer(firstRequestLine);
        tokens.nextToken();  // skip over the method, which should be "GET"
        fileName = tokens.nextToken();

        /* ::::: Check if file exists ::::: */
        FileInputStream myFileInputString = null;
        boolean fileExists = true;
        try {
            myFileInputString = new FileInputStream(rootLocation + fileName);
        } catch (FileNotFoundException e) {
            fileExists = false;
        }

        /* ::::: RESPONSE HEADER ::::: */
        if (fileExists) {
            statusLine = "HTTP/1.1 200 OK";
            contentLength = Long.toString(myFileInputString.getChannel().size());
            contentType = getContentType(fileName);
            receiveDate = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss zzz").format(date);
        } else {
            statusLine = "HTTP/1.1 404 NOT FOUND";
            contentLength = "0";
            contentType = getContentType(fileName);
            receiveDate = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss zzz").format(date);
            htmlOutput = "<HTML><HEAD><TITLE>NOT FOUND</TITLE></HEAD><BODY><center><h1>FILE NOT FOUND</h1></center></BODY></HTML>";
        }

        /* ::::: Check Last-Modified Date ::::: */
        File f = new File(rootLocation+ fileName);
        //Date lastModifiedDate = new Date("Mon, 01 Jan 2010 20:20:20 GMT");
        Date lastModifiedDate = new Date(f.lastModified());
        if(getIfModifiedSince != null) {
            Date ifModifiedSince = new Date(getIfModifiedSince);
            if (ifModifiedSince.compareTo(lastModifiedDate) >= 0)
            {
                statusLine = statusLine.replace("200 OK", "304 Not Modified");
            }

            myCookie = "Set-Cookie: visitor=return";
        }
        else
        {
            myCookie = "Set-Cookie: visitor=new";
        }

        /* ::::: Send Response Message ::::: */
        myOutputStream.writeBytes(statusLine + strRN);
        myOutputStream.writeBytes("Content-Length: " + contentLength + " bytes" + strRN);
        myOutputStream.writeBytes("Content-Type: " + contentType + strRN);
        myOutputStream.writeBytes("Date: " + receiveDate + strRN);
        myOutputStream.writeBytes("User-Agent: " + userAgent + strRN);
        myOutputStream.writeBytes("Last-Modified: " + lastModifiedDate + strRN);
        myOutputStream.writeBytes("Connection: Keep-Alive" + strRN);
        myOutputStream.writeBytes(myCookie);

        //Send a blank line to indicate the end of the header lines.
        myOutputStream.writeBytes(strRN);

        //Send the entity body.
        if (fileExists) {
            sendBytes(myFileInputString, myOutputStream);
            myFileInputString.close();
        } else {
            myOutputStream.writeBytes(htmlOutput);
        }

        /* ::::: Write Log Entry ::::: */
        writeLogFile( this.mySocket.getInetAddress().getHostName()
                + " - - "
                + "[" + new SimpleDateFormat("dd/MMM/yyyy:hh:mm:ss +-hhmm").format(date) + "] "
                + "\"" + firstRequestLine + "\" "
                + "200 "
                + contentLength
                + " \"" + userAgent + "\"");

        myOutputStream.flush();
        mySocket.setKeepAlive(true);

    }

    private static void sendBytes(FileInputStream fis, OutputStream os) throws Exception {
        //Construct a 5MB buffer to hold bytes on their way to the socket.
        byte[] buffer = new byte[1024*5];
        int bytes = 0;

        //Copy requested file into the socket's output stream.
        while((bytes = fis.read(buffer)) != -1 ) {
            os.write(buffer, 0, bytes);
        }
    }

    private static String getContentType(String fileName) {
        if(fileName.endsWith(".htm") || fileName.endsWith(".html"))
            return "text/html";
        else if(fileName.endsWith(".txt"))
            return "text/plain";
        else if(fileName.endsWith(".js"))
            return "text/javascript";
        else if(fileName.endsWith(".css"))
            return "text/css";
        else if(fileName.endsWith(".png"))
            return "image/png";
        else if(fileName.endsWith(".pdf"))
            return "application/pdf";
        else if(fileName.endsWith(".xml"))
            return "application/xhtml+xml";
        else if(fileName.endsWith(".gif"))
            return "image/gif";
        else if(fileName.endsWith(".jpeg"))
            return "image/jpeg";
        else
            return "application/octet-stream";
    }

    private static void writeLogFile(String logEntry) {
        try {
            File file = new File("/Users/sambathpich/WebServerProject/log_entry.txt");

            if (file.createNewFile()) {
                //System.out.println("File is created!");
                FileWriter writer = new FileWriter("/Users/sambathpich/WebServerProject/log_entry.txt", true);
                writer.write(logEntry + "\r\n");
                writer.close();
            } else {
                //System.out.println("File already exists.");
                FileWriter writer = new FileWriter("/Users/sambathpich/WebServerProject/log_entry.txt", true);
                BufferedWriter bufferedWriter = new BufferedWriter(writer);
                writer.write("\r\n" + logEntry);
                bufferedWriter.newLine();
                bufferedWriter.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}