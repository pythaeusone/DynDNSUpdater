package de.musti.dydns;

/**
 * Die Mainklasse :)
 * 
 * @author Musti
 * @version 1.0
 */
public class Main
{
	/**
	 * Startpunkt von Java.
	 * 
	 * @param args - Erwartet Domain, UpdaterHash, Intervalzeit in minuten.
	 */
	public static void main(String[] args)
	{
		LogBuilder lb = new LogBuilder();
		GetResponseCode grc = new GetResponseCode();
		RunUpdate ru = new RunUpdate(lb, grc);
		GetIP gip = new GetIP(lb, grc);

		DynDNS dyn = new DynDNS(args[0], args[1], args[2], args[3], gip, lb, ru);

		Thread thread = new Thread(dyn);
		thread.start();
	}

}
