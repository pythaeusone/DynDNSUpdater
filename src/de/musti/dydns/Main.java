package de.musti.dydns;

/**
 * Die Mainklasse :)
 * 
 * @author Musti
 * @version 0.4
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
		DynDNS dyn = new DynDNS(args[0], args[1], args[2]);

		Thread thread = new Thread(dyn);
		thread.start();
	}

}
