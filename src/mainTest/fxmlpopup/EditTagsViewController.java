package mainTest.fxmlpopup;

import mainTest.PaginatedTableViewCheckBoxAndButtonColumn;
import mainTest.PaginatedTableViewCheckBoxAndButtonColumn.Sample;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;

public class EditTagsViewController {
	@FXML
	private TextField idTxtField;
	@FXML
	private TextField selectedTxtField;
	@FXML
	private TextField fooTxtField;
	@FXML
	private TextField barTxtField;
	
	private Sample sample;
	
	@FXML
	private void initialize() {
		this.sample = PaginatedTableViewCheckBoxAndButtonColumn.getSample();
		
		idTxtField.setText(this.sample.getId().getValue().toString());
		selectedTxtField.setText(Boolean.toString(this.sample.getSelected()));
		fooTxtField.setText(this.sample.getFoo().getValue());
		barTxtField.setText(this.sample.getBar().getValue());
	}
	
	@FXML
	public void handleDone(){
		Platform.runLater(new Runnable() {
			@Override
			public void run() {
				PaginatedTableViewCheckBoxAndButtonColumn.popupStage.close();
			}
		});
	}
}
