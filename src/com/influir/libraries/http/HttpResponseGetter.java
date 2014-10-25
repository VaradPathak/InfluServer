/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.influir.libraries.http;

import com.influir.libraries.utils.InfluException;
import com.influir.libraries.utils.InfluLogger;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

/**
 *
 * @author Pranit
 */
public class HttpResponseGetter
{
    public static String getHttpResponse(String urlString) throws InfluException
    {
        while (true)
        {
            try
            {
                URL url = new URL(urlString);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setDoOutput(true);
                connection.setRequestMethod("GET");
                connection.setAllowUserInteraction(false);
                connection.connect();

                BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = br.readLine()) != null)
                {
                    sb.append(line).append('\n');
                }
                return sb.toString();
            }
            catch (MalformedURLException e)
            {
                InfluLogger.print("MLU in main: " + urlString);
                throw new InfluException();
            }
            catch (IOException e)
            {
                InfluLogger.print("IOException while fetching URL: " + urlString);
            }
        }
    }
}
