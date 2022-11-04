package de.musti.dydns;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

/**
 * Diese Klasse beinhaltet alle noetigen Methoden.
 * 
 * @author Musti
 * @version 0.3
 */
public class DynDNS implements Runnable
{
	String domain;
	String updateHash;
	String interval;
	String lastIP;
	String ip;
	String logText;
	SimpleDateFormat logZeit;
	Calendar cal;
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
		logZeit = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");
		cal = new GregorianCalendar();

		int timer = tryParse(interval);

		System.out.println("DynDNS Updater v0.3 fuer ipv64.net gestartet.\n");

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
		ip = getOnlineIP();
		if (ip != null)
		{
			if (!checkIP(ip))
			{
				lastIP = ip;
				System.out.println("IP hat sich veraendert, Updater wird gestartet.");
				runUpdaterUrl();
			}
		}
		else
		{
			System.out.println("IPv4 konnte nicht ermittelt werden!");
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
			System.out.println("Sende request an ipv64.net ...\n");
			Document d = Jsoup
					.connect("https://ipv64.net/update.php?key=" + updateHash + "&domain=" + domain + "&ip=" + lastIP)
					.ignoreContentType(true).get();
			Elements tbody = d.select("body");
			String[] bodyArray = (tbody.text().replaceAll("\\{", "").replaceAll("\\}", "")).split(",");

			for (String bA : bodyArray)
			{
				System.out.println(bA.replaceAll("\"", " "));
			}

			logBuilder("UPDATER | " + logZeit.format(cal.getTime()) + " | LastIP: " + lastIP + " | NewIP: " + ip);
		}
		catch (IOException e)
		{
			System.out.println("Request fehlgeschlagen!");
			logBuilder(
					"\nUPDATER | " + logZeit.format(cal.getTime()) + " | Request fehlgeschlagen! Thread wird gestopt.");
			e.printStackTrace();
			signalStopeTimer();
		}
	}


	/**
	 * Es wird ein Request an ifconfig.me gesendet und die IPv4 ermittelt.
	 * 
	 * @return - Return ist die Empfangene IPv4 Adresse.
	 */
	String getOnlineIP()
	{
		Document d = null;
		try
		{
			d = Jsoup.connect("https://ipv4.ipv64.net").timeout(6000).get();
			Elements tbody = d.select("div.card-body dl.row dd.col-sm-7");

			return tbody.val();
		}
		catch (IOException e)
		{
			System.out.println("https://ipv64.net evtl. nicht erreichbar!");
			logBuilder("\nGET-IP | " + logZeit.format(cal.getTime())
					+ " | https://ipv64.net evtl. nicht erreichbar! Thread wird gestopt.");
			e.printStackTrace();
			signalStopeTimer();
		}
		return null;
	}

	/**
	 * Diese Methode ist fuer die Zukunft, damit es spaeter ein Off Schalter gibt.
	 */
	void signalStopeTimer()
	{
		logBuilder("\nEXIT | " + logZeit.format(cal.getTime()) + " | Programm wurde wegen einem fehler beendet.\n");
		stopeTimer = true;
		System.exit(0);
	}

	/**
	 * Da die Intervalvariable eine Zahl sein muss, wird diese hier vin einem String
	 * zu einem int geparst.
	 * 
	 * @param text - Interval als String.
	 * @return - Interval als int.
	 */
	Integer tryParse(String text)
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

	/**
	 * Diese Methode erstellt, falls noetig den Log Ordner und Schreibt eine LogInfo
	 * mit Uhrzeit.
	 * 
	 * @param txt - Die Methode getOnlineIP() und runUpdaterUrl() schreiben in die
	 *            Log woher der Fehler kommt und welches Datum/Uhrzeit.
	 */
	void logBuilder(String txt)
	{
		SimpleDateFormat sDF = new SimpleDateFormat("dd.MM.yyyy");

		String date = sDF.format(cal.getTime());

		File dir = new File("log");
		if (!dir.exists())
		{
			dir.mkdirs();
		}

		File f = new File(dir + File.separator + "DynDNSUpdate_-_" + date + ".log");
		try
		{
			BufferedWriter bw = new BufferedWriter(new FileWriter(f, true));
			bw.append(txt + "\n");
			bw.close();
		}
		catch (IOException e)
		{
			System.out.println(e.getMessage());
		}
		System.out.println("\n" + txt);
	}

}
