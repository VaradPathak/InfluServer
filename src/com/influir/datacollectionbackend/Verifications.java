/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.influir.datacollectionbackend;

import com.influir.libraries.json.JSONArray;
import com.influir.libraries.json.JSONException;
import com.influir.libraries.json.JSONObject;
import com.influir.libraries.utils.Constants;
import com.influir.libraries.utils.FileManager;
import java.io.FileNotFoundException;
import java.io.IOException;

/**
 *
 * @author Pranit
 */
public class Verifications
{
    public static void main(String args[]) throws FileNotFoundException, IOException, JSONException
    {
        String json = FileManager.readFile("TOP250Movies.data");
        JSONArray jSONArray = new JSONArray(json);
        for (int i = 0; i < jSONArray.length(); i++)
        {
            JSONObject jSONObject = jSONArray.getJSONObject(i);
            JSONArray dirs = jSONObject.getJSONArray(Constants.DIRECTORS);
            if (dirs.length() == 0)
            {
                System.out.print("Error : ");
                System.out.println(jSONObject.getString(Constants.IMDBTITLE));
            }
        }
    }
}
