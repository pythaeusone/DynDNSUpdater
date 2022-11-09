package de.musti.dydns;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;

/**
 * Diese Klasse beinhaltet alle noetigen Methoden.
 * 
 * @author Musti
 * @version 1.0
 */
public class DynDNS implements Runnable
{
	String domain;
	String updateHash;
	String interval;
	String ipOption;
	String lastIP4;
	String ip4;
	String lastIP6;
	String ip6;
	String logText;
	SimpleDateFormat logZeit;
	Calendar cal;

	boolean ipv4Changed;
	boolean ipv6Changed;

	RunUpdate ru;
	GetIP gip;
	LogBuilder lb;
	private volatile boolean stopeTimer = false;

	/**
	 * Der Konstruktor der Klasse.
	 * 
	 * @param domain     - Die Domain worum es beim Update geht.
	 * @param updateHash - Der DynDNS Updatehash.
	 * @param interval   - Ueberprueft jede x min wie die aktuelle IP ist.
	 * @param ipOption   - Beim ausfuehren des Programms gibt man ipv4/6 an.
	 * @param gip        - Die Kontrolle von GetIP wird hier uebergeben.
	 * @param lb         - Die Kontrolle vom LogBuilder wird hier uebergeben.
	 * @param ru         - Die Kontrolle vom RunUpdate wird hier uebergeben.
	 */
	public DynDNS(String domain, String updateHash, String interval, String ipOption, GetIP gip, LogBuilder lb,
			RunUpdate ru)
	{
		this.domain = domain;
		this.updateHash = updateHash;
		this.interval = interval;
		this.ipOption = ipOption.toLowerCase();

		this.lb = lb;
		this.ru = ru;
		this.gip = gip;
	}

	/**
	 * Hier beginnt der Thread.
	 */
	@Override
	public void run()
	{
		logZeit = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");
		cal = new GregorianCalendar();

		System.out.println("DynDNS Updater v1.0 fuer ipv64.net gestartet.\n");

		int timer = tryParse(interval);

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
		if (ipOption.equals("ipv4") | ipOption.equals("ipv64"))
			if (gip.getIPvFour() != null)
			{
				ip4 = gip.getIPvFour()[1];
				if (ip4 != null)
				{
					if (!checkIP(ip4))
					{
						lastIP4 = ip4;
						ipv4Changed = true;
						System.out.println("\nIPv4 hat sich veraendert, Updater wird vorbereitet.");
					}
				}
				else
				{
					System.out.println("\nIPv4 konnte nicht ermittelt werden!");
				}
			}
			else
			{
				lb.logBuilder("\nGET-IPv4 Error:" + gip.getIPvFour()[0] + " | " + logZeit.format(cal.getTime()) + " | "
						+ gip.getIPvFour()[1]);
				signalStopeTimer();
			}

		if (ipOption.equals("ipv6") | ipOption.equals("ipv64"))
			if (gip.getIPvSix() != null)
			{
				ip6 = gip.getIPvSix()[1];
				if (ip6 != null)
				{
					if (!checkIP(ip6))
					{
						lastIP6 = ip6;
						ipv6Changed = true;
						System.out.println("\nIPv6 hat sich veraendert, Updater wird vorbereitet.");
					}
				}
				else
				{
					System.out.println("\nIPv6 konnte nicht ermittelt werden!");
				}
			}
			else
			{
				lb.logBuilder("\nGET-IPv6 Error:" + gip.getIPvSix()[0] + " | " + logZeit.format(cal.getTime()) + " | "
						+ gip.getIPvSix()[1]);
				signalStopeTimer();
			}

		if (ipOption.equals("ipv4") && ipv4Changed)
			runUpdaterUrl(prepareRunUpdaterUrl());
		if (ipOption.equals("ipv6") && ipv6Changed)
			runUpdaterUrl(prepareRunUpdaterUrl());
		if (ipOption.equals("ipv64") && ipv4Changed && ipv6Changed)
			runUpdaterUrl(prepareRunUpdaterUrl());
	}

	/**
	 * Diese Methode vergleicht die neu eingelesene IP mit der alten gespeicherten.
	 * 
	 * @param ip - Die neu eingelesene IP.
	 * @return - True wenn die alte und neue IP gleich sind, bei neuer IP False.
	 */
	boolean checkIP(String ip)
	{
		if (lastIP4 != null)
			if (lastIP4.equals(ip))
			{
				System.out.println("\nIPv4 hat sich nicht geaendert.");
				ipv4Changed = false;
				return true;
			}

		if (lastIP6 != null)
			if (lastIP6.equals(ip))
			{
				System.out.println("\nIPv6 hat sich nicht geaendert.");
				ipv6Changed = false;
				return true;
			}

		return false;
	}

	/**
	 * Diese Methode baut den Update String zusammen und gibt ihn zurueck.
	 * 
	 * @return - Update STring fuer den DynDNS Server.
	 */
	String prepareRunUpdaterUrl()
	{

		if (ipOption.equals("ipv4"))
			return "https://ipv64.net/update.php?key=" + updateHash + "&domain=" + domain + "&ip=" + lastIP4;
		if (ipOption.equals("ipv6"))
			return "https://ipv64.net/update.php?key=" + updateHash + "&domain=" + domain + "&ip=" + lastIP6;
		if (ipOption.equals("ipv64"))
			return "https://ipv64.net/update.php?key=" + updateHash + "&domain=" + domain + "&ip=" + lastIP4 + "&ip6="
					+ lastIP6;

		return null;
	}

	/**
	 * Diese Methode ruft den Updater von ipv64.net auf und zerlegt die Seite für
	 * ein Output. Danach wird das Ergebnis in die Logdatei geschrieben.
	 */
	void runUpdaterUrl(String ipArt)
	{
		String[] responseArray = ru.udapteIP(ipArt);

		if (responseArray != null)
		{
			if (responseArray[1] != null)
			{
				if (ipOption.equals("ipv4") | ipOption.equals("ipv64"))
					lb.logBuilder("UPDATER | " + logZeit.format(cal.getTime()) + " | LastIPv4: " + lastIP4
							+ " | NewIPv4: " + ip4);
				if (ipOption.equals("ipv6") | ipOption.equals("ipv64"))
					lb.logBuilder("UPDATER | " + logZeit.format(cal.getTime()) + " | LastIPv6: " + lastIP6
							+ " | NewIPv6: " + ip6);
			}
			else
			{
				lb.logBuilder("\nUPDATER | " + logZeit.format(cal.getTime()) + " | " + responseArray[0]);
				signalStopeTimer();
			}

		}

	}

	/**
	 * Killswitch falls etwas schief laeuft. Logdatei wird ein Eintrag gemacht.
	 */
	void signalStopeTimer()
	{
		lb.logBuilder("\nEXIT | " + logZeit.format(cal.getTime()) + " | Programm wurde wegen einem fehler beendet.\n");
		System.out.println(
				"\nEXIT | " + logZeit.format(cal.getTime()) + " | Programm wurde wegen einem fehler beendet.\n");
		stopeTimer = true;
		System.exit(0);
	}

	/**
	 * Da die Intervalvariable eine Zahl sein muss, wird diese hier von einem String
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
}
