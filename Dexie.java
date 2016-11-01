//package xDrip2g;

import java.net.*;
import java.io.*;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.InsertOneModel;
import org.bson.Document;
import java.util.Date;
import java.util.logging.Logger;
import javax.crypto.*;
import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import sun.misc.*;
import javax.xml.bind.DatatypeConverter;
// might need
//import java.util
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
//	findjar.com/class/org/json/simple/JSONArray.html 
//	http://www.findjar.com/jar/com/googlecode/json-simple/json-simple/1.1/json-simple-1.1.jar.html
import java.net.HttpURLConnection;
import java.net.URL;


import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;

import javax.net.ssl.HttpsURLConnection;
//import org.joda.time.*;
import java.text.*;

// might need this
import java.util.*;

// Credit to javapf for the original PortMonitor and One Connection example scripts and also  JavaProgrammingForums.com
 
public class Dexie {
 
    public static void main(String[] args) throws Exception {

//	Get parameters from dexie.ini file
		String ASCIIencryptionKey ="";		//	The exact 16 ASCII characters defined in dexdrip.c
		String MongoDatabaseName ="";		//	Mongolab Database Name
		String MongoConnectionUser ="";		//	MongoLab Database User ID (not the Mongolab Account Name)
		String MongoConnectionPass ="";		//	NongoLab Database Password
		String MongoConnectionID ="";		//	Mongolab Datbase Identifier
		String MongoConnectionPort ="";		//	Mongolab port (usally numbers from above)
		String MongoSensorCollect ="";		//	Collection name used as xDrip's ""Wifi Wixel" source
		String StrPortToListenOn ="";		//	Input port number for socket to listen on - 17611 recommended
		Integer MyPortToListenOn = 17611;	//	Get integer value of port number


		try{
			Dexie ini = new Dexie();
			Properties p = new Properties();
			p.load(new FileInputStream("Dexie.ini"));
			ASCIIencryptionKey =	p.getProperty("ASCIIencryptionKey");	//	The exact 16 ASCII characters defined in dexdrip.c
			MongoDatabaseName =	p.getProperty("MongoDatabaseName");		//	Mongolab Database Name
			MongoConnectionUser =	p.getProperty("MongoConnectionUser");	//	MongoLab Database User ID (not the Mongolab Account Name)
			MongoConnectionPass =	p.getProperty("MongoConnectionPass");	//	NongoLab Database Password
			MongoConnectionID =	p.getProperty("MongoConnectionID");		//	Mongolab Datbase Identifier
			MongoConnectionPort =	p.getProperty("MongoConnectionPort");	//	Mongolab port (usally numbers from above)
			MongoSensorCollect =	p.getProperty("MongoSensorCollect");	//	Collection name used as xDrip's ""Wifi Wixel" source
			StrPortToListenOn =	p.getProperty("MyPortToListenOn");		//	Input port number for socket to listen on - 17611 recommended
			MyPortToListenOn = Integer.parseInt(StrPortToListenOn);		//	Get integer value of port number
			}	catch (Exception ex) {
				System.out.println("Invalid parameters in Dexie.ini or file cannot be read");
			ex.printStackTrace(); }




//		Derive a connection string in the format "mongodb://UserName:PassWord@ds012345.mongolab.com:12345/?authSource=myDatabaseNamedb"
		String MongoConnectionString =	"mongodb://" + MongoConnectionUser + ":" + MongoConnectionPass + "@" +
												MongoConnectionID + ".mongolab.com:" + MongoConnectionPort + "/?authSource=" + MongoDatabaseName;
 
        System.out.println();
 	    System.out.println("***************************************************************************");
        System.out.println();

//		Open socket on the port below to monitor for communications from remote device
        final int myPort = MyPortToListenOn;
		int missedCount = 0;
        ServerSocket ssock = new ServerSocket(myPort);
		System.out.println("port " + myPort + " opened");

		while(true) {								// MAIN LOOP
			System.out.println();
			//New code to prevent locking if no input
			ssock.setSoTimeout(3600000);  // Time out after 1 hour
			//Remove if does not work

			Socket sock = null;
		   try {
				sock = ssock.accept();
		   } catch (SocketTimeoutException e) {
				System.out.println("Socket accept timed out!");
			   // accept timed out, continue listening unless stop requested
		   }

		   InetAddress socketAddress = sock.getInetAddress();
//			String hostAddr = addr.getHostAddress();
//			System.out.println("Addr: " + hostAddr);
			String stringTime = new Date().toString();
			System.out.println("Socket connection at " + stringTime + " from " + socketAddress);
//	Above times out ok

			BufferedReader in = null;
			DataOutputStream out = null;
			// More new code for timeout

// The trick here is to set soco timeout as opposed to ssock timeout!
			sock.setSoTimeout(60000);  // Time out after 60 seconds

			try {
				in = new BufferedReader(new InputStreamReader(sock.getInputStream()));
			} catch (SocketTimeoutException e2) {
				System.out.println("Socket Input Stream timed out!");
// 				break;
			}
//			catch (IOException e) {
//  	    	e.printStackTrace();
//      		break;
//      	}

//			Might generate a response here are some stage			
			out = new DataOutputStream(sock.getOutputStream());

			String s = null;
			String receivedString = null;
//			Sleep 5 seconds
			try {
				Thread.sleep(5000);
				} catch(InterruptedException ex) {
					Thread.currentThread().interrupt();
				}
				
				
//			System.out.println("Getting input...");
			ssock.setSoTimeout(30000);  // Time out after 30 seconds
			try {
				receivedString = in.readLine();
			} catch (SocketTimeoutException e3) {
				System.out.println("Readline timed out!");
 				break;
			}

// Gets stuck before here sometimes

			
			System.out.println("AES Encrypted Ciphertext >>> " + receivedString);
			System.out.println();
	
			if (receivedString != null) {
				String[] splitStringArray = receivedString.split(" ");
//				Check if this is encoded and if it is then decode the string before BAU processing
//				I should probably add some validation and error handling here but will assume we get what we expect for the moment...
				String receivedPreamble = splitStringArray[0];
				if ((receivedPreamble.equals("xDrip2g(AES):")) || (receivedPreamble.equals("yDrip(AES):"))) {
					byte[] IV = hexStringToByteArray (splitStringArray[1]);
					byte[] encryptionKey = ASCIIencryptionKey.getBytes();
					byte[] encryptedText = hexStringToByteArray(splitStringArray[2]);

				System.out.println("Remote Timestamp: " 
				+ (int)(IV[6]-48)  + (int)(IV[7]-48)  + "/" + (int)(IV[4]-48)  + (int)(IV[5]-48)  + "/" 
				+ (int)(IV[0]-48)  + (int)(IV[1]-48)  + (int)(IV[2]-48)  + (int)(IV[3]-48) + " "
				+ (int)(IV[8]-48)  + (int)(IV[9]-48)  + ":" + (int)(IV[10]-48) + (int)(IV[11]-48) + ":" + (int)(IV[12]-48) + (int)(IV[13]-48)
				+ " (as derived from Initialisation Vector)"
				);
				
			
					String strIV = new String(IV);
					
					String strKey = new String(encryptionKey);
					String decryptedString = "";
					try {
						SecretKeySpec aesKey = new SecretKeySpec(encryptionKey, "AES");	
						Cipher cipher = Cipher.getInstance("AES/CBC/NoPadding");
						cipher.init(Cipher.DECRYPT_MODE, aesKey, new IvParameterSpec(IV));
						byte[] original = cipher.doFinal(encryptedText);
						decryptedString = new String(original);	
					} catch (Exception ex) {
						ex.printStackTrace();
					}
					if (receivedPreamble.equals("xDrip2g(AES):")) {
						receivedString = ("xDrip2g(Decrypt): " + decryptedString);
					} else {
						receivedString = ("yDrip(Decrypt): " + decryptedString);
					}
					splitStringArray = receivedString.split(" ");
//					System.out.println("Decrypted packet is: " + receivedString);

					System.out.println(receivedString.toString());			// Might need to loop here to print spaced element
				}
				else {
					System.out.println("No AES encryption detected as preamble was " + receivedPreamble);
				}
//				System.out.println();

				
//				Either way, at this point we now have plaintext to split into the constituent fields
//				It might be nice to add some validation and error checking here at some stage...
				String wixfoneId = splitStringArray[1];
				Integer transmissionID = Integer.parseInt(splitStringArray[2]);
				String transmitterID = splitStringArray[3];
				Integer rawValue = Integer.parseInt(splitStringArray[4]);
				Integer filteredValue = Integer.parseInt(splitStringArray[5]);
				Integer batteryLife = Integer.parseInt(splitStringArray[6]);
				Integer rssi = Integer.parseInt(splitStringArray[7]);
				Integer uploaderBattery = Integer.parseInt(splitStringArray[8]);
// Temporary fudge to compensate for wixel code bug
				if (uploaderBattery < 1) {uploaderBattery=100;}
				
				String uploaderGPS;

				double lat=666;
				double lng=666;
				
				if (splitStringArray[9] == null) {
					uploaderGPS = "Not Determined";
				} else {
					uploaderGPS = splitStringArray[9];
					String[] splitStringGPS = uploaderGPS.split(",");
					lng = Double.parseDouble(splitStringGPS[0]);
					lat = Double.parseDouble(splitStringGPS[1]);	// Allow for errors later (if no comma)
				}
					
//				Now we can connect to the MongoLab database and starting writing out stuff
				MongoClientURI connectionString  = new MongoClientURI(MongoConnectionString);
				MongoClient mongoClient = new MongoClient(connectionString);
				MongoDatabase database = mongoClient.getDatabase(MongoDatabaseName);		
			
				Long captureTime = new Date().getTime();

//				Check here if we got a heartbeat or actual sensor readings to decide if we need to populate the sensor data or possibly just add a note
				if (transmissionID != 0) {
					MongoCollection<Document> collection = database.getCollection(MongoSensorCollect);
//					Long captureTime = new Date().getTime();
					Document doc = new Document("TransmissionId", transmissionID)
					   .append("TransmitterId", transmitterID)
					   .append("RawValue", rawValue)
					   .append("FilteredValue", filteredValue)
					   .append("BatteryLife", batteryLife)
					   .append("ReceivedSignalStrength", rssi)
					   .append("CaptureDateTime", captureTime)
					   .append("UploaderId", wixfoneId)
					   .append("UploaderBatteryLife", uploaderBattery)
					   .append("ReadableTime", stringTime);
					collection.insertOne(doc);
					missedCount = 0;
				} else { 
					missedCount = missedCount + 1;
					if (missedCount > 3) {
						MongoCollection<Document> collection2 = database.getCollection("treatments");
						Document doc2 = new Document ("enteredBy", "xDrip2g")
							.append("eventType", "Announcement")		// Should probably be note
							.append("notes", "Remote device  still alive but missed a number of readings")
							.append("created_at", stringTime)
							.append("glucose", 2.0)
							.append("units", "mmol")
							.append("isAnnouncement", false);
						collection2.insertOne(doc2);
						missedCount = 0;
					}	
				}

//				I should really parameterise this as not everyone might want their battery level overwritten
//				Now add entry for uploader battery
				MongoCollection<Document> collection3 = database.getCollection("devicestatus");
//				Set date to be later than that one set by xDrip so it will over-ride whatever it puts out
				captureTime = captureTime + (60000 * 2);
//				stringTime = new Date(captureTime).toString();

				SimpleDateFormat format1 = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSX");
				String stringTimeFormatted = format1.format(captureTime).toString();
//				stringTime = new Date(captureTime).toString();

				
//				Write extra record to devicestatus collection
				Document doc3 = new Document("uploaderBattery", uploaderBattery)
					   .append("uploaderDevice", wixfoneId)
					   .append("created_at", stringTimeFormatted);
				collection3.insertOne(doc3);


//		Interpret GPS co-ordinates
		final String USER_AGENT = "Mozilla/5.0";
		String url = "http://maps.google.com/maps/api/geocode/json?latlng="+lat+","+lng+"&sensor=false";
		
		URL obj = new URL(url);
		HttpURLConnection con = (HttpURLConnection) obj.openConnection();

		// optional default is GET
		con.setRequestMethod("GET");

		//add request header
		con.setRequestProperty("User-Agent", USER_AGENT);

		int responseCode = con.getResponseCode();
//		System.out.println("\nSending 'GET' request to URL : " + url);
		if (responseCode != 200) {
			System.out.println("API Response Code : " + responseCode);
		}
//		System.out.println();

		BufferedReader in2 = new BufferedReader(
		        new InputStreamReader(con.getInputStream()));
		String inputLine;
		String previousLine="Nowhere";
		String thisLine="Somewhere";
		String routeString="Unidentified";
		StringBuffer response = new StringBuffer();

		while ((inputLine = in2.readLine()) != null  && routeString.equals("Unidentified")) {
//			System.out.println("Input Line: " + inputLine);
			previousLine=thisLine;
			thisLine=inputLine;
			splitStringArray = thisLine.split("\"");
			if(splitStringArray.length > 3) {
				if(splitStringArray[3].equals("route")) {
					splitStringArray = previousLine.split("\"");
					routeString=splitStringArray[3];
				}
			}
			
		}
		
		in2.close();

		//print result
//		System.out.println(response.toString());
		System.out.println("(The cell tower connected to was near " + routeString +")");

				
//				Also add entry here for GPS (if it is passed)
				MongoCollection<Document> collection4 = database.getCollection("location");
				if (uploaderGPS != "Not Determined") {
//				System.out.println("Uploading GPS...");
				Document doc4 = new Document("uploaderGPS", uploaderGPS)
					   .append("Cell GPS location", routeString)
					   .append("uploaderDevice", "Wixfone")
					   .append("created_at", stringTime);
					collection4.insertOne(doc4);
				} else {
				System.out.println("GPS not detected...");
				}
				



//				SimpleDateFormat format1 = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSX");
//				String stringTimeNow = format1.format(new Date());
				String stringTimeNow = format1.format(new Date());

// I should parameterise this as well becuase not everyone will want the GPS in the status				
				MongoCollection<Document> collection5 = database.getCollection("treatments");
				if (uploaderGPS != "Not Determined") {
				Document doc5 = new Document("notes", (wixfoneId + " (connected to cell tower near " + routeString +")"))
					   .append("created_at", stringTimeNow)
					   .append( "isAnnouncement", true);
					collection5.insertOne(doc5);
				}

			System.out.println("- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -");

			mongoClient.close();
			}	
		}
	}

	
	public static byte[] hexStringToByteArray(String s) {
		int len = s.length();
		byte[] data = new byte[len / 2];
		for (int i = 0; i < len; i += 2) {
			data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
								 + Character.digit(s.charAt(i+1), 16));
		}
		return data;
	}

}