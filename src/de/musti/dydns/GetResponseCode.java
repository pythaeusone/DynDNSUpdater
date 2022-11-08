package de.musti.dydns;

public class GetResponseCode
{
	public String responseCodeHanlder(int code)
	{
		if (code == 200)
		{
			return "Antwort vom Server erhalten.";
		}
		else if (code == 400)
		{
			return "400: Server kann Anfrage nicht bearbeiten!";
		}
		else if (code == 401)
		{
			return "401: Unerlaubte Anfrage an den Server!";
		}
		else if (code == 403)
		{
			return "403: Anfrage verboten!";
		}
		else if (code == 429)
		{
			return "429: Zuviele Anfragen!";
		}
		else if (code == 502)
		{
			return "502: Bad Gateway!";
		}
		else if (code == 504)
		{
			return "504: Gateway Timeout!";
		}

		return "Fehler mit unbekannten Code!";
	}

}
