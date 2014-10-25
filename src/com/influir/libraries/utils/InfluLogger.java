/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.influir.libraries.utils;

/**
 *
 * @author Pranit
 */
public class InfluLogger
{
    private static final boolean LOGGER = true;

    public static void print(String string)
    {
        if (LOGGER)
        {
            System.out.println("Logger: " + string);
        }
    }
}
