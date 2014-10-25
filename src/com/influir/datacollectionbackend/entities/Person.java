/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.influir.datacollectionbackend.entities;

import com.influir.libraries.json.JSONException;
import com.influir.libraries.json.JSONObject;
import com.influir.libraries.utils.Constants;

/**
 *
 * @author Pranit
 */
public class Person
{
    public String name;
    public int id;

    public Person(String name, int id)
    {
        this.name = name;
        this.id = id;
    }

    public Person(String name)
    {
        this.name = name;
    }

    public JSONObject toJSON() throws JSONException
    {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put(Constants.RTPERSONID, id);
        jsonObject.put(Constants.RTPERSONNAME, name);
        return jsonObject;
    }
}