package mainTest;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import javafx.application.Application;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.binding.ListBinding;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Pagination;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Callback;

public class PaginatedTableViewCheckBoxAndButtonColumn extends Application {
	public static Stage popupStage;
	HashSet<Sample> selectedData = new HashSet<>();

	private CheckBox selectAllCheckBox;
	private Button exportButton;
	private Button selectAllDataButton;

	private final static int dataSize = 200000;
	private final static int rowsPerPage = 15;

	private final List<Sample> data = createData();
	private final TableView<Sample> table = createTable();

	private int fromIndex;
	private int toIndex;

	private static Sample sample;

	private List<Sample> createData() {
		List<Sample> data = new ArrayList<>(dataSize);

		for (int i = 0; i < dataSize; i++) {
			data.add(new Sample(false, i, "foo " + i, "bar " + i));
		}

		return data;
	}

	@SuppressWarnings("unchecked")
	private TableView<Sample> createTable() {

		// "Selected" column
		TableColumn<Sample, Boolean> selectedCol = new TableColumn<Sample, Boolean>();
		selectedCol.setMinWidth(50);
		selectedCol.setGraphic(getSelectAllCheckBox());
		selectedCol
				.setCellValueFactory(new PropertyValueFactory<Sample, Boolean>(
						"selected"));

		selectedCol
				.setCellFactory(new Callback<TableColumn<Sample, Boolean>, TableCell<Sample, Boolean>>() {
					public TableCell<Sample, Boolean> call(
							TableColumn<Sample, Boolean> p) {
						final TableCell<Sample, Boolean> cell = new TableCell<Sample, Boolean>() {
							@Override
							public void updateItem(final Boolean item,
									boolean empty) {
								if (item == null)
									return;
								super.updateItem(item, empty);
								if (!isEmpty()) {
									final Sample employee = getTableView()
											.getItems().get(getIndex());
									
									if(!item){
										selectedData.remove(employee);
									} else {
										selectedData.add(employee);
									}
									CheckBox checkBox = new CheckBox();
									checkBox.selectedProperty()
											.bindBidirectional(
													employee.selectedProperty());
									checkBox.setOnAction(checkBoxClicked);
									// in line checkbox
									setGraphic(checkBox);
								}
							}
						};
						cell.setAlignment(Pos.CENTER);
						return cell;
					}
				});

		// "Selected" column
		TableColumn<Sample, Boolean> editBtnCol = new TableColumn<>("Edit");
		editBtnCol.setMinWidth(50);
		editBtnCol
				.setCellValueFactory(new PropertyValueFactory<Sample, Boolean>(
						"selected"));

		editBtnCol
				.setCellFactory(new Callback<TableColumn<Sample, Boolean>, TableCell<Sample, Boolean>>() {
					public TableCell<Sample, Boolean> call(
							TableColumn<Sample, Boolean> p) {
						final TableCell<Sample, Boolean> cell = new TableCell<Sample, Boolean>() {
							@Override
							public void updateItem(final Boolean item,
									boolean empty) {
								if (item == null)
									return;
								super.updateItem(item, empty);
								if (!isEmpty()) {
									final Sample employee = getTableView()
											.getItems().get(getIndex());
									Image editBtnImg = new Image(
											getClass()
													.getResourceAsStream(
															"../resources/img/Buttons/Edit.png"));
									Button editBtn = new Button();
									editBtn.setGraphic(new ImageView(editBtnImg));
									editBtn.setStyle("-fx-background-color: #BFBFBF;");
									editBtn.setOnAction(new EventHandler<ActionEvent>() {
										@Override
										public void handle(ActionEvent event) {
											// employee.setSelected(!employee.getSelected());

											// Select the current row
											// information to edit
											editPopUp(employee);
										}
									});
									// editBtn.setOnAction(editBtnClicked);
									// in line edit button
									setGraphic(editBtn);
								}
							}
						};
						cell.setAlignment(Pos.CENTER);
						return cell;
					}
				});

		TableColumn<Sample, Integer> column1 = new TableColumn<>("Id");
		column1.setCellValueFactory(param -> param.getValue().id);
		column1.setPrefWidth(150);

		TableColumn<Sample, String> column2 = new TableColumn<>("Foo");
		column2.setCellValueFactory(param -> param.getValue().foo);
		column2.setPrefWidth(250);

		TableColumn<Sample, String> column3 = new TableColumn<>("Bar");
		column3.setCellValueFactory(param -> param.getValue().bar);
		column3.setPrefWidth(250);

		final TableView<Sample> tableView = new TableView<Sample>();
		tableView.setItems(FXCollections.observableArrayList(data.subList(
				fromIndex, toIndex)));
		tableView.getColumns().addAll(selectedCol, editBtnCol, column1,
				column2, column3);

		ListBinding<Boolean> lb = new ListBinding<Boolean>() {
			{
				bind(tableView.getItems());
			}

			@Override
			protected ObservableList<Boolean> computeValue() {
				ObservableList<Boolean> list = FXCollections
						.observableArrayList();
				for (Sample p : tableView.getItems()) {
					list.add(p.getSelected());
				}
				return list;
			}
		};

		tableView.getItems().addListener(new InvalidationListener() {

			@Override
			public void invalidated(Observable arg0) {
				System.out.println("invalidated");
			}
		});
		tableView.getItems().addListener(new ListChangeListener<Sample>() {

			@Override
			public void onChanged(
					javafx.collections.ListChangeListener.Change<? extends Sample> arg0) {
				System.out.println("changed");
			}
		});
		return tableView;
	}

	private Node createPage(int pageIndex) {
		setFromIndex(pageIndex * rowsPerPage);
		setToIndex(Math.min(getFromIndex() + rowsPerPage, data.size()));
		checkSelected();
		
		return new BorderPane(createTable());
	}

	private void editPopUp(Sample sample) {
		PaginatedTableViewCheckBoxAndButtonColumn.sample = sample;
		popupStage = new Stage();
		try {
			Parent root = FXMLLoader.load(getClass().getResource(
					"fxmlpopup/editTagsView.fxml"));
			popupStage.setScene(new Scene(root));
			popupStage.setTitle("Add Address Book");
			popupStage.initModality(Modality.APPLICATION_MODAL);
			popupStage.setResizable(false);
			popupStage.centerOnScreen();
			// Get data from db to display on the table again
			popupStage.showAndWait();

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void start(final Stage stage) throws Exception {
		VBox vb = new VBox(20);
		Pagination pagination = new Pagination((data.size() / rowsPerPage + 1),
				0);
		pagination.setStyle("-fx-border-color: black;");
		pagination.setPageFactory(this::createPage);
		pagination.setMaxPageIndicatorCount(9);
		pagination.setMinHeight(460);
		pagination.setMaxHeight(460);

		vb.getChildren().addAll(pagination, getExportButton(),getSelectAllDataButton());
		vb.setMinHeight(400);
		Scene scene = new Scene(new BorderPane(vb), 1024, 700);

		stage.setScene(scene);
		stage.setTitle("Paginated Table View Check Box Column");
		stage.show();
	}

	public static void main(String[] args) throws Exception {
		launch(args);
	}

	public static class Sample {

		private SimpleBooleanProperty selected;
		private final ObservableValue<Integer> id;
		private final SimpleStringProperty foo;
		private final SimpleStringProperty bar;

		private Sample(boolean selected, int id, String foo, String bar) {
			this.selected = new SimpleBooleanProperty(selected);
			this.id = new SimpleObjectProperty<>(id);
			this.foo = new SimpleStringProperty(foo);
			this.bar = new SimpleStringProperty(bar);
		}

		public BooleanProperty selectedProperty() {
			return selected;
		}

		public boolean getSelected() {
			return selected.get();
		}

		public void setSelected(boolean selected) {
			this.selected.set(selected);
		}

		public ObservableValue<Integer> getId() {
			return id;
		}

		public SimpleStringProperty getFoo() {
			return foo;
		}

		public SimpleStringProperty getBar() {
			return bar;
		}

	}

	private final EventHandler<ActionEvent> checkBoxClicked = new EventHandler<ActionEvent>() {
		@Override
		public void handle(ActionEvent event) {
			checkSelected();
		}
	};

	/**
	 * Lazy getter for the selectAllCheckBox.
	 * 
	 * @return selectAllCheckBox
	 */
	public CheckBox getSelectAllCheckBox() {
		if (selectAllCheckBox == null) {
			final CheckBox selectAllCheckBox = new CheckBox();

			// Adding EventHandler to the CheckBox to select/deselect all
			// employees in table.
			selectAllCheckBox.setOnAction(new EventHandler<ActionEvent>() {
				@Override
				public void handle(ActionEvent event) {
					// Setting the value in all the employees.
					for (Sample item : createTable().getItems()) {
						item.setSelected(selectAllCheckBox.isSelected());
					}
					getExportButton().setDisable(
							!selectAllCheckBox.isSelected());
				}
			});

			this.selectAllCheckBox = selectAllCheckBox;
		}
		return selectAllCheckBox;
	}

	public Button getExportButton() {
		if (this.exportButton == null) {
			final Button exportButton = new Button("Export");
			exportButton.setOnAction(new EventHandler<ActionEvent>() {
				@Override
				public void handle(ActionEvent arg0) {
					System.out.println("Sample Selected : [");
//					for (Sample employee : createTable().getItems()) {
//						if (employee.getSelected()) {
//							System.out.println(employee.getId().getValue()
//									+ " : " + employee.getFoo().getValue()
//									+ " : " + employee.getBar().getValue());
//						}
//					}
					
					for (Sample employee : selectedData) {
						if (employee.getSelected()) {
							System.out.println(employee.getId().getValue()
									+ " : " + employee.getFoo().getValue()
									+ " : " + employee.getBar().getValue());
						}
					}
					System.out.println(" ]\n");
					System.out.println(" selectedData size: "+selectedData.size());
				}
			});
			exportButton.setDisable(true);
			this.exportButton = exportButton;
		}
		return this.exportButton;
	}
	
	public Button getSelectAllDataButton() {
		if (this.selectAllDataButton == null) {
			final Button selectAllDataButton = new Button("Select All");
			selectAllDataButton.setOnAction(new EventHandler<ActionEvent>() {
				@Override
				public void handle(ActionEvent arg0) {
					if(getSelectAllCheckBox().isSelected()){
						for (Sample employee : data) {
							employee.setSelected(false);
							selectedData.remove(employee);
						}
						checkSelected();
					} else if (!getSelectAllCheckBox().isSelected()) {
						for (Sample employee : data) {
							employee.setSelected(true);
							selectedData.add(employee);
						}
						checkSelected();
					}
					
				}
			});
			this.selectAllDataButton = selectAllDataButton;
		}
		return this.selectAllDataButton;
	}

	/**
	 * Enables the "Export" button.
	 */
	public void enableExportButton() {
		getExportButton().setDisable(false);
	}

	/**
	 * Disables the "Export" button.
	 */
	public void disableExportButton() {
		getExportButton().setDisable(true);
	}

	public int getFromIndex() {
		return fromIndex;
	}

	public void setFromIndex(int fromIndex) {
		this.fromIndex = fromIndex;
	}

	public int getToIndex() {
		return toIndex;
	}

	public void setToIndex(int toIndex) {
		this.toIndex = toIndex;
	}

	public static Sample getSample() {
		return sample;
	}

	private void checkSelected() {
		// Checking for an unselected employee in the table view.
		boolean unSelectedFlag = false;
		for (Sample item : createTable().getItems()) {
			if (!item.getSelected()) {
				unSelectedFlag = true;
				break;
			}
			
		}
		
		/*
		 * If at least one employee is not selected, then deselecting the check
		 * box in the table column header, else if all employees are selected,
		 * then selecting the check box in the header.
		 */
		if (unSelectedFlag) {
			getSelectAllCheckBox().setSelected(false);
		} else {
			getSelectAllCheckBox().setSelected(true);
		}

		// Checking for a selected employee in the table view.
		boolean selectedFlag = false;
		for (Sample item : createTable().getItems()) {
			if (item.getSelected()) {
				selectedFlag = true;
				break;
			}
		}
		
		if(!selectedData.isEmpty()){
			selectedFlag = true;
		}

		/*
		 * If at least one employee is selected, then enabling the "Export"
		 * button, else if none of the employees are selected, then disabling
		 * the "Export" button.
		 */
		if (selectedFlag) {
			enableExportButton();
		} else {
			disableExportButton();
		}
		System.out.println("\t\t selectedData: "+selectedData.size());
	}
}
