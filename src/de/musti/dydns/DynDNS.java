package de.musti.dydns;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import com.profesorfalken.jpowershell.PowerShell;

/**
 * Diese Klasse beinhaltet alle noetigen Methoden.
 * 
 * @author Musti
 * @version 0.1
 */
public class DynDNS implements Runnable
{
	String domain;
	String updateHash;
	String interval;
	String lastIP;
	private volatile boolean stopeTimer = false;

	/**
	 * Der Konstruktor der Klasse.
	 * 
	 * @param domain     - Die Domain worum es beim Update geht.
	 * @param updateHash - Der DynDNS Updatehash.
	 * @param interval   - Wie oft soll eine arnder ueberprueft werden.
	 */
	public DynDNS(String domain, String updateHash, String interval)
	{
		this.domain = domain;
		this.updateHash = updateHash;
		this.interval = interval;
	}

	/**
	 * Hier beginnt der Thread.
	 */
	@Override
	public void run()
	{
		int timer = tryParse(interval);

		System.out.println("DynDNS Updater fuer ipv64.net gestartet.\n");

		try
		{
			while (!stopeTimer)
			{
				updater();
				Thread.sleep(timer);
			}
		}
		catch (InterruptedException e)
		{
			e.printStackTrace();
		}
	}

	/**
	 * Diese Methode wird immer vom Thread aufgerufen und ueberprueft ob sich die IP
	 * geaendert hat.
	 */
	void updater()
	{
		String ip = getOnlineIP();
		if (!checkIP(ip))
		{
			lastIP = ip;
			System.out.println("IP hat sich veraendert, Updater wird gestartet.");
			runUpdaterUrl();
		}
	}


	/**
	 * Diese Methode vergleicht die neu eingelesene IP mit der alten gespeicherten.
	 * 
	 * @param ip - Die neu eingelesene IP.
	 * @return - True wenn die alte und neue IP gleich sind, bei neuer IP False.
	 */
	boolean checkIP(String ip)
	{
		if (lastIP != null)
			if (lastIP.equals(ip))
			{
				System.out.println("IP hat sich nicht geaendert.");
				return true;
			}

		return false;
	}

	/**
	 * Diese Methode ruft den Updater von ipv64.net auf und zerlegt die Seite für
	 * ein Output.
	 */
	void runUpdaterUrl()
	{

		try
		{
			System.out.println("Sende request an ipv64.net ...");
			Document d = Jsoup
					.connect("https://ipv64.net/update.php?key=" + updateHash + "&domain=" + domain + "&ip=" + lastIP)
					.ignoreContentType(true).get();
			Elements tbody = d.select("body");
			String[] bodyArray = (tbody.text().replaceAll("\\{", "").replaceAll("\\}", "")).split(",");

			for (String bA : bodyArray)
			{
				System.out.println(bA.replaceAll("\"", " "));
			}

		}
		catch (IOException e)
		{
			System.out.println("Request fehlgeschlagen!");
			e.printStackTrace();
		}
	}


	/**
	 * Diese Methode ueberprueft was fuer ein OS laeuft. Danach wird ein Request an
	 * ifconfig.me gesendet und die IPv4 ermittelt.
	 * 
	 * @return - Return ist die Empfangene IPv4 Adresse.
	 */
	String getOnlineIP()
	{
		if (System.getProperty("os.name").toLowerCase().contains("windows"))
		{
			String command = "(Invoke-WebRequest ifconfig.me/ip -UseBasicParsing).Content.Trim()";
			String powerShellOut = PowerShell.executeSingleCommand(command).getCommandOutput();
			if (powerShellOut.contains("InvalidOperation"))
			{
				System.out.println("ifconfig.me evtl. nicht erreichbar.");
				powerShellOut = null;
			}
			return powerShellOut;
		}
		else if (System.getProperty("os.name").toLowerCase().contains("linux"))
		{
			String urlString = "https://ifconfig.me/ip";
			URL url;
			try
			{
				url = new URL(urlString);
				BufferedReader br = new BufferedReader(new InputStreamReader(url.openStream()));
				return br.readLine();
			}
			catch (IOException e)
			{
				System.out.println("ifconfig.me evtl. nicht erreichbar.");
				e.printStackTrace();
			}
		}

		return "Betriebssystem nicht erkannt!";
	}

	/**
	 * Diese Methode ist fuer die Zukunft, damit es spaeter ein Off Schalter gibt.
	 */
	public void signalStopeTimer()
	{
		stopeTimer = true;
	}

	/**
	 * Da die Intervalvariable eine Zahl sein muss, wird diese hier vin einem String
	 * zu einem int geparst.
	 * 
	 * @param text - Interval als String.
	 * @return - Interval als int.
	 */
	public static Integer tryParse(String text)
	{
		try
		{
			int min = Integer.parseInt(text) * 60000;
			System.out.println("IP-Check wird nun alle " + (min / 60000) + " min. durchgefuehrt.");
			return min;
		}
		catch (NumberFormatException e)
		{
			System.out.println(text + " ist keine Zahl, Timer wurde auf 5 Minuten gestellt!");
			return 300000;
		}
	}
}
