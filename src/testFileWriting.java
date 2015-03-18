import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Scanner;

public class testFileWriting {

	public static void main(String[] args) {
		//Unable to create file on Linux due to permission
		createFile();
		Scanner scanner = new Scanner(System.in);
		System.out.println("Enter your file Path: ");
		Path file = Paths.get(scanner.nextLine());
		scanner.close();
		BasicFileAttributes attribute;
		try {
			attribute = Files.readAttributes(file, BasicFileAttributes.class);

			System.out.println("creationTime: " + attribute.creationTime());
			System.out.println("lastAccessTime: " + attribute.lastAccessTime());
			System.out.println("lastModifiedTime: "
					+ attribute.lastModifiedTime());

			System.out.println("isDirectory: " + attribute.isDirectory());
			System.out.println("isOther: " + attribute.isOther());
			System.out.println("isRegularFile: " + attribute.isRegularFile());
			System.out.println("isSymbolicLink: " + attribute.isSymbolicLink());
			System.out.println("size: " + attribute.size());

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void createFile() {
		try {
			File fileDir = new File("test");
	 
			Writer out = new BufferedWriter(new OutputStreamWriter(
				new FileOutputStream(fileDir), "UTF8"));
	 
			out.append("Website UTF-8").append("\r\n");
			out.append("?? UTF-8").append("\r\n");
			out.append("??????? UTF-8").append("\r\n");
	 
			out.flush();
			out.close();
	 
		    } 
		   catch (UnsupportedEncodingException e) 
		   {
			System.out.println(e.getMessage());
		   } 
		   catch (IOException e) 
		   {
			System.out.println(e.getMessage());
		    }
		   catch (Exception e)
		   {
			System.out.println(e.getMessage());
		   } 
	}
}
