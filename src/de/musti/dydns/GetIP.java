package de.musti.dydns;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;

import org.json.JSONObject;

/**
 * Diese Klasse kuemmert sich um die Globale IP.
 * 
 * @author Musti
 * @version 1.0
 */
public class GetIP
{
	private final static String iPvFour = "https://ipv4.ipv64.net/update.php?howismyip";
	private final static String iPvSix = "https://ipv6.ipv64.net/update.php?howismyip";

	SimpleDateFormat logZeit;
	Calendar cal;

	LogBuilder lb;
	GetResponseCode grc;

	/**
	 * Der Konstruktor von GetIP.
	 * 
	 * @param lb  - Die Kontrolle ueber den Logbuilder wird hier uebergeben.
	 * @param grc - Die Kontrolle ueber den ResponseCode Handler wird hier
	 */
	public GetIP(LogBuilder lb, GetResponseCode grc)
	{
		this.lb = lb;
		this.grc = grc;

		logZeit = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");
		cal = new GregorianCalendar();
	}

	/**
	 * Diese Methode ruft die parseIP fuer IPv4 auf.
	 * 
	 * @return - Gibt ein Array zurueck mit Antworten vom Server.
	 */
	public String[] getIPvFour()
	{
		try
		{
			return parseIP(iPvFour);
		}
		catch (Exception e)
		{
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * Diese Methode ruft die parseIP fuer IPv6 auf.
	 * 
	 * @return - Gibt ein Array zurueck mit Antworten vom Server.
	 */
	public String[] getIPvSix()
	{
		try
		{
			return parseIP(iPvSix);
		}
		catch (Exception e)
		{
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * Diese Methode bekommt die IP Check URL und zerlegt die JSON.
	 * 
	 * @param getIpLink - Die URL welche IPv4/6 zurueck gibt.
	 * @return - Gibt ein String Array mit Response Infos zurueck.
	 * @throws Exception - Uebergibt die Exception getIP Methoden.
	 */
	private String[] parseIP(String getIpLink) throws Exception
	{
		String[] returnArray = new String[2];

		URL url = new URL(getIpLink);
		HttpURLConnection conn = (HttpURLConnection) url.openConnection();
		conn.setRequestMethod("GET");
		conn.setRequestProperty("User-Agent", "Mozilla/5.0");
		int responeCode = conn.getResponseCode();
		returnArray[0] = grc.responseCodeHanlder(responeCode);
		if (responeCode != 200)
		{
			returnArray[1] = null;
		}
		else
		{
			BufferedReader bRiS = new BufferedReader(new InputStreamReader(conn.getInputStream()));
			String inputStream;
			StringBuffer sb = new StringBuffer();

			while ((inputStream = bRiS.readLine()) != null)
			{
				sb.append(inputStream);
			}

			bRiS.close();

			JSONObject jRespone = new JSONObject(sb.toString());

			returnArray[1] = jRespone.getString("ip");
		}

		return returnArray;
	}

}
