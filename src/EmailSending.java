import org.apache.commons.mail.HtmlEmail;

public class EmailSending {

	public String sendEmail(String recipientEmailAddress, String subject,
			String emailTemplate) {
		String senderEmailAddress = "gvit00702@gmail.com";
		String senderPassword = "groventureit007";
		String result = null;

		if (senderEmailAddress != null && senderPassword != null) {

			try {

				// Create the email message
				HtmlEmail email = new HtmlEmail();

				email.isStartTLSEnabled();
				email.setHostName("smtp.gmail.com");
				email.setSmtpPort(587);
				email.setStartTLSRequired(true);
				email.setAuthentication(senderEmailAddress, senderPassword);
				email.addTo(recipientEmailAddress);
				email.setFrom(senderEmailAddress, "Wakakaka");
				email.setSubject(subject);
				email.setBounceAddress(senderEmailAddress);

				// set the html message email.setHtmlMsg(emailTemplate);

				// set the alternative message
				email.setTextMsg("Your email client does not support HTML messages");

				// send the email
				email.send();

			} catch (Exception e) {
				System.out.println("Error: ");
				e.printStackTrace();
			}
		} else {
			System.out.println("email address, password, username is null");
			result = null;
		}
		return result;
	}
}
