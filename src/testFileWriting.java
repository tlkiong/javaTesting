import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Scanner;


public class testFileWriting {

	public static void main(String[] args) {
		Scanner scanner=new Scanner(System.in);
		System.out.println("Enter your file Path: ");
		Path file = Paths.get(scanner.nextLine());
		scanner.close();
		BasicFileAttributes attribute;
		try {
			attribute = Files.readAttributes(file, BasicFileAttributes.class);
			
			System.out.println("creationTime: " + attribute.creationTime());
			System.out.println("lastAccessTime: " + attribute.lastAccessTime());
			System.out.println("lastModifiedTime: " + attribute.lastModifiedTime());

			System.out.println("isDirectory: " + attribute.isDirectory());
			System.out.println("isOther: " + attribute.isOther());
			System.out.println("isRegularFile: " + attribute.isRegularFile());
			System.out.println("isSymbolicLink: " + attribute.isSymbolicLink());
			System.out.println("size: " + attribute.size());
			
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
