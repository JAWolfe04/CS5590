package icp11.filegen;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class App {

	public static void main(String[] args) throws InterruptedException, IOException {
		int fileNum = 1;
		Random rand = new Random();
		List<String> lines = readIntoList("C:\\Users\\Jonathan\\Desktop\\UMKC\\CS 5590\\ICP\\CS5590-ICP-11\\Input\\lorem.txt");
		while(fileNum <= 30) {
			System.out.print("creating file log" + fileNum + ".txt\n");
			FileWriter writer = new FileWriter("C:\\Users\\Jonathan\\Desktop\\UMKC\\CS 5590\\ICP\\CS5590-ICP-11\\Input\\Logs\\log" 
					+ fileNum + ".txt");
			int linenumber = rand.nextInt(lines.size() - 10);
			for(String line : lines.subList(linenumber, lines.size())) {
				writer.write(line + " ");
			}
			writer.close();
			++fileNum;
			Thread.sleep(5000);
		}
	}

	private static List<String> readIntoList(String fileName) {
		List<String> lines = Collections.emptyList();
		try {
			lines = Files.readAllLines(Paths.get(fileName), StandardCharsets.UTF_8);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return lines;
	}
}
