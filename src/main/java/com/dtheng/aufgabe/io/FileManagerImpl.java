package com.dtheng.aufgabe.io;

import rx.Observable;

import java.io.File;
import java.io.IOException;
import java.util.Scanner;

/**
 * @author Daniel Thengvall <fender5289@gmail.com>
 */
public class FileManagerImpl implements FileManager {

	@Override
	public Observable<String> read(String filename) {
		StringBuilder result = new StringBuilder("");
		ClassLoader classLoader = getClass().getClassLoader();
		File file = new File(classLoader.getResource(filename).getFile());
		try (Scanner scanner = new Scanner(file)) {
			while (scanner.hasNextLine()) {
				String line = scanner.nextLine();
				result.append(line).append("\n");
			}
			scanner.close();
		} catch (IOException e) {
			return Observable.error(e);
		}
		return Observable.just(result.toString());
	}
}
