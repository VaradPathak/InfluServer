/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.influir.libraries.utils;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

/**
 * 
 * @author Pranit
 */
public class FileManager {
	public static String readFile(String filename)
			throws FileNotFoundException, IOException {
		File file = new File(filename);
		BufferedReader br = new BufferedReader(new FileReader(file));
		StringBuilder sb = new StringBuilder();
		String line;
		while ((line = br.readLine()) != null) {
			sb.append(line);
		}
		br.close();
		return sb.toString();
	}

	public static void writeToFile(String filename, String contents)
			throws IOException {
		File file = new File(filename);
		try (BufferedWriter bw = new BufferedWriter(new FileWriter(file))) {
			bw.write(contents);
		}
	}
}
