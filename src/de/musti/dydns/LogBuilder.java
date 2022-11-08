package de.musti.dydns;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;

public class LogBuilder
{
	Calendar cal;
	SimpleDateFormat sDF;

	public LogBuilder()
	{
		cal = new GregorianCalendar();
		sDF = new SimpleDateFormat("dd.MM.yyyy");
	}

	/**
	 * Diese Methode erstellt, falls noetig den Log Ordner und Schreibt eine LogInfo
	 * mit Uhrzeit.
	 * 
	 * @param txt - Die Methode getOnlineIP() und runUpdaterUrl() schreiben in die
	 *            Log woher der Fehler kommt und welches Datum/Uhrzeit.
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
		// System.out.println("\n" + txt);
	}

}
