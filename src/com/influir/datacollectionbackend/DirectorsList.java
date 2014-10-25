/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.influir.datacollectionbackend;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import com.influir.libraries.json.JSONArray;
import com.influir.libraries.json.JSONException;
import com.influir.libraries.json.JSONObject;
import com.influir.libraries.utils.FileManager;

/**
 *
 * @author Pranit
 */
public class DirectorsList
{
    public static void main(String args[]) throws FileNotFoundException, IOException, JSONException
    {
        int counter = 0;
        Set<String> directors = new HashSet<>();
        for (int i = 1; i <= 15; i++)
        {
            System.out.println("Now fetching : d" + i + ".txt");
            String file = FileManager.readFile("d" + i + ".txt");
            JSONArray current = new JSONArray(file);
            counter += current.length() * 2;
            for (int j = 0; j < current.length(); j++)
            {
                JSONObject currentDirector = current.getJSONObject(j);
                directors.add(currentDirector.getString("Director"));
                directors.add(currentDirector.getString("InfluencedBy"));
            }
        }
        StringBuilder sb = new StringBuilder();
        for (String director : directors)
        {
            sb.append(director).append(";");
        }
        FileManager.writeToFile("AllDirectorsList.data", sb.toString());
        System.out.println("Total: " + counter);
        System.out.println("Distinct: " + directors.size());
    }
}
