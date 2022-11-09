package de.musti.dydns;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import org.json.JSONObject;

/**
 * Diese Klasse dient dem Update.
 * 
 * @author Musti
 * @version 1.0
 */
public class RunUpdate
{
	LogBuilder lb;
	GetResponseCode grc;

	/**
	 * Der Konstruktor
	 * 
	 * @param lb  - Die Kontrolle ueber den Logbuilder wird hier uebergeben.
	 * @param grc - Die Kontrolle ueber den ResponseCode Handler wird hier
	 *            uebergeben.
	 */
	public RunUpdate(LogBuilder lb, GetResponseCode grc)
	{
		this.lb = lb;
		this.grc = grc;

	}

	/**
	 * Diese Methode bekommt den UpdateURL String und fuehrt diese ueber den Parser
	 * aus.
	 * 
	 * @param updateLink - Die Update URL
	 * @return - Gibt ein Array zurueck mit Antworten vom Server.
	 */
	public String[] udapteIP(String updateLink)
	{
		try
		{
			return parseUpdate(updateLink);
		}
		catch (Exception e)
		{
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * Diese Methode nimmt den StringURL und sendet ein Request an den DynDNS
	 * Server, danach wird das JSON Ergebnis zerlegt und ausgegeben.
	 * 
	 * @param updateLink - Die Update URL
	 * @return - Gibt ein Array zurueck mit Antworten vom Server.
	 * @throws Exception - Die Methode udapteIP() gibt dann ein NULL zurueck.
	 */
	private String[] parseUpdate(String updateLink) throws Exception
	{
		String[] returnArray = new String[2];

		System.out.println("\nSende request an ipv64.net ...\n");

		URL url = new URL(updateLink);
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

			JSONObject jResponse = new JSONObject(sb.toString());

			if (jResponse.toString().contains("status"))
				System.out.println(" Status: " + jResponse.getString("status"));
			if (jResponse.toString().contains("info"))
				System.out.println(" Info: " + jResponse.getString("info"));

			if (jResponse.toString().contains("ip"))
			{
				StringBuffer innerResponse = new StringBuffer();
				innerResponse.append(jResponse.get("ip").toString());
				JSONObject jInnerResponse = new JSONObject(innerResponse.toString());

				if (jResponse.toString().contains("ipv4"))
				{
					System.out.println(" Neue IPv4: " + jInnerResponse.getString("ipv4"));
				}
				if (jResponse.toString().contains("ipv6"))
				{
					System.out.println(" Neue IPv6: " + jInnerResponse.getString("ipv6"));
				}
			}
			returnArray[1] = "updateOK";
		}

		return returnArray;
	}

}
