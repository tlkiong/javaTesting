package mainTest;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import sendgrid.SendgridSendEmail;

public class SendGridTest {

	public static void main(String[] args) {
		String username = "groventure";
		String password = "groventureit007";
		SendgridSendEmail sendgridSendEmail = new SendgridSendEmail(username,password);
		sendgridSendEmail.sendEmailViaSendgrid();

		String endPoint = String.format("https://api.sendgrid.com/api/profile.get.json?api_user=%s&api_key=%s",username,password);
		
		System.out.println("endpoint: "+endPoint);
		
		try {

			URL url = new URL(endPoint);
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			conn.setRequestMethod("GET");
			conn.setRequestProperty("Accept", "application/json");

			if (conn.getResponseCode() != 200) {
				throw new RuntimeException("Failed : HTTP error code : "
						+ conn.getResponseCode());
			}

			BufferedReader br = new BufferedReader(new InputStreamReader(
					(conn.getInputStream())));

			String output;
			System.out.println("Output from Server .... \n");
			while ((output = br.readLine()) != null) {
				System.out.println(output);
			}

			conn.disconnect();

		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}
}
