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
		String guid = java.util.UUID.randomUUID().toString();
		String encodedData = encodeData(guid);
		createFile(encodedData);
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

	/**
	 * Creating a file and appending the thing with 
	 * @param encodedData
	 */
	public static void createFile(String encodedData) {
		try {
			File fileDir = new File("test");
	 
			Writer out = new BufferedWriter(new OutputStreamWriter(
				new FileOutputStream(fileDir), "UTF-8"));
	 
			out.append(encodedData);
	 
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
	
	/**
	 * Encode Data using LZString
	 * @param data
	 * @return the encodedData
	 */
	public static String encodeData(String data){
		//TODO
		String toBeEncoded = data;
		//String encodedValue = LZString.compressToBase64(toBeEncoded);
		// '$' is group symbol in regex's replacement parameter
//		String newEncodedValue = encodedValue.replaceAll("=", "\\$");
//		String correctEncodedValue = newEncodedValue.replaceAll("/","-");
		String correctEncodedValue = LZString.compressToUTF16(toBeEncoded);
		return correctEncodedValue;
	}
	
	/**
	 * Decode encoded data using LZString
	 * @param encodedValue is the encoded data
	 */
	public static String decodeData(String encodedValue){
//		String newEncodedValue = encodedValue.replaceAll("\\$","=");
//		String correctEncodedValue = newEncodedValue.replaceAll("-","/");
//		String decodedValue = LZString.decompressFromBase64(correctEncodedValue);
		String decodedValue = LZString.decompressFromUTF16(encodedValue);
		return decodedValue;
	}
}
