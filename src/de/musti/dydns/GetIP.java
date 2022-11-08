package de.musti.dydns;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;

import org.json.JSONObject;

public class GetIP
{
	private final static String iPvFour = "https://ipv4.ipv64.net/update.php?howismyip";
	private final static String iPvSix = "https://ipv6.ipv64.net/update.php?howismyip";

	SimpleDateFormat logZeit;
	Calendar cal;

	LogBuilder lb;
	GetResponseCode grc;

	public GetIP(LogBuilder lb, GetResponseCode grc)
	{
		this.lb = lb;
		this.grc = grc;

		logZeit = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");
		cal = new GregorianCalendar();
	}

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
