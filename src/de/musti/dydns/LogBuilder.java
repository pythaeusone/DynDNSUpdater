package de.musti.dydns;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;

/**
 * Diese Klasse kuemmert sich um das Schreiben der Logdatei.
 * 
 * @author Musti
 * @version 1.0
 */
public class LogBuilder
{
	Calendar cal;
	SimpleDateFormat sDF;

	/**
	 * Der Konstruktor vom LogBuilder.
	 */
	public LogBuilder()
	{
		cal = new GregorianCalendar();
		sDF = new SimpleDateFormat("dd.MM.yyyy");
	}

	/**
	 * Dieser logBuilder erstellt falls noetig den Ordner log und schreibt dann
	 * Logfiles mit dem aktuellen Datum.
	 * 
	 * @param txt - Erhaelt die Information, welche geschrieben werden soll.
	 */
	public void logBuilder(String txt)
	{
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
	}

}
