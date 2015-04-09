package sendgrid;

import java.io.IOException;
import java.util.ArrayList;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;

import com.sendgrid.SendGrid;
import com.sendgrid.SendGrid.Email;
import com.sendgrid.SendGridException;

public class SendgridSendEmail {
	SendGrid sendgrid;

	public SendgridSendEmail(String username, String password) {
		sendgrid = new SendGrid(username, password);
	}

	public void sendEmailViaSendgrid() {
		Email email = new Email();
		email.addTo("adsdkasjdkajsbdkajsbdkajsbd@gmail.com");
		email.addToName("Example Guy");
		email.setFrom("dlaoksdnlnsdlkasndlkasndlks@gmail.com");
		email.setSubject("Hello World");
		email.setText("My first email through SendGrid");

		try {
			SendGrid.Response response = sendgrid.send(email);
			System.out.println("response: " + response.getMessage());
			System.out.println("Email sent! Hopefully");
		} catch (SendGridException e) {
			e.printStackTrace();
		}
	}
}
