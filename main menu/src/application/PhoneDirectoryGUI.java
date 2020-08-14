package application;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.control.TableView;
import javafx.scene.control.TableColumn;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.geometry.Insets;
import javafx.beans.property.StringProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.ObservableList;
import java.io.File;
import java.io.PrintWriter;
import java.io.IOException;
import java.util.Scanner;
import java.util.List;





public class PhoneDirectoryGUI extends Application {

    public static void main(String[] args) {
        launch();    
    }
    
    
    private static String DATA_FILE_NAME = ".phone_book";


    public static class PhoneEntry {
        StringProperty name;
        StringProperty number;
        PhoneEntry(String name, String number) {
            this.name = new SimpleStringProperty(name);
            this.number = new SimpleStringProperty(number);
        }
        public StringProperty nameProperty() {
            return name;
        }
        public StringProperty numberProperty() {
            return number;
        }
    }
    
    
    
    public void start(Stage stage) {
        
        /* Create a dataFile of different files to represent the
         * data file that is stored in the user's home directory. */

        File userHomeDirectory = new File( System.getProperty("user.home") );
        File dataFile = new File( userHomeDirectory, DATA_FILE_NAME );

        /* Create a TableView to hold the data for the phonebook, and
         * load the existing phonebook data from the file into the table. 
         */

        
        TableView<PhoneEntry> phoneBook = new TableView<>();
        phoneBook.setEditable(true);
        phoneBook.setPrefSize(420,350);
        ObservableList<PhoneEntry> phoneEntries = phoneBook.getItems();
        
        loadPhoneBook(dataFile, phoneEntries);
        if (phoneEntries.size() == 0)
            phoneEntries.add( new PhoneEntry("name","number"));
        
        /* the table columns for the "Name" and "Phone Number" columns. */
        
        
        TableColumn<PhoneEntry, String> nameColumn = new TableColumn<>("Name");
        nameColumn.setCellValueFactory( new PropertyValueFactory<PhoneEntry, String>("name") );
        nameColumn.setCellFactory( TextFieldTableCell.forTableColumn() );
        nameColumn.setPrefWidth(200);  // (Default size is too small)
        phoneBook.getColumns().add(nameColumn);
        nameColumn.setEditable(true);
        
        TableColumn<PhoneEntry, String> numberColumn = new TableColumn<>("Phone Number");
        numberColumn.setCellValueFactory( new PropertyValueFactory<PhoneEntry, String>("number") );
        numberColumn.setCellFactory( TextFieldTableCell.forTableColumn() );
        numberColumn.setPrefWidth(200);  // (Default size is too small)
        phoneBook.getColumns().add(numberColumn);

        /* making "Add" and "Delete" buttons at the bottom window below the table. */
        
        
        TextField nameInput = new TextField();
        nameInput.setPrefColumnCount(10);
        nameInput.setPromptText("(name)");
        TextField numberInput = new TextField();
        numberInput.setPromptText("(number)");
        numberInput.setPrefColumnCount(10);

        Button deleteButton = new Button("Delete Highlighted Entry");
        deleteButton.setOnAction( e -> {
            int selected = phoneBook.getSelectionModel().getSelectedIndex();
            if (selected >= 0)
                phoneEntries.remove( phoneBook.getSelectionModel().getSelectedIndex());
        });
        deleteButton.setMaxWidth(Double.POSITIVE_INFINITY);
        deleteButton.disableProperty().bind(
                phoneBook.getSelectionModel().selectedIndexProperty().isEqualTo(-1));
        Button addButton = new Button("Add:");
        addButton.setOnAction( e -> {  // add a new row to the table
            String name = nameInput.getText().trim();
            String number = numberInput.getText().trim();
            if (name.length() == 0 ) {
                    // Don't allow empty name in the table
                error("You must enter a name and number before adding an entry.");
                nameInput.requestFocus();
                return;
            }
            if (number.length() == 0 ) {
                    // Don't allow an empty phone number in the table
                error("You must enter a name and number before adding an entry.");
                numberInput.requestFocus();
                return;
            }
            nameInput.setText("");   // empty the input boxes, since the data has been
            numberInput.setText(""); //        copied into the table
            PhoneEntry newEntry = new PhoneEntry(name,number);
            phoneEntries.add( newEntry );
            phoneBook.scrollTo(phoneEntries.size() - 1); // make sure new row is visibe
            phoneBook.getSelectionModel().select(phoneEntries.size() - 1); // highlight new entry
        });
        HBox add = new HBox(8,addButton,nameInput,numberInput);
        add.setPadding( new Insets(5) );
        VBox buttons = new VBox(add,deleteButton);
        buttons.setStyle("-fx-border-color:black; -fx-border-width: 2px");
        
        BorderPane tableHolder = new BorderPane(phoneBook);
        tableHolder.setBottom(buttons);
        
        stage.setOnHidden( e -> savePhoneBook(dataFile, phoneEntries) );
        
        stage.setScene( new Scene(tableHolder) );
        stage.setTitle("Phone Book Editor");
        stage.show();
        
    } 
    
    
        
    private void loadPhoneBook(File dataFile, List<PhoneEntry> entries) {
        if ( ! dataFile.exists() ) {
            message("No phone book data file found.  A new one\n"
                        + "will be created when the program ends,\n"
                        + "if you add any entries to the table.\n"
                        + "File name:\n    " + dataFile.getAbsolutePath());
        }
        else {
            try( Scanner scanner = new Scanner(dataFile) ) {
                while (scanner.hasNextLine()) {
                    String phoneEntry = scanner.nextLine();
                    int separatorPosition = phoneEntry.indexOf('%');
                    if (separatorPosition == -1)
                        throw new IOException("File is not a phonebook data file.");
                    String name = phoneEntry.substring(0, separatorPosition);
                    String number = phoneEntry.substring(separatorPosition+1);
                    entries.add( new PhoneEntry(name,number) );
                }
            }
            catch (IOException e) {
                error("Error in phone book data file.\n"
                            + "This program cannot continue.\n"
                            + "Data File name: \n   " + dataFile.getAbsolutePath());
                System.exit(1);
            }
        }
    }
    
    
    /*
     * Write the phone book entries from the list to the data files.
     * But if the list is empty, don't write anything.
     */
    private void savePhoneBook(File dataFile, List<PhoneEntry> entries) {
        if (entries.size() == 0)
            return;
        System.out.println("Saving phone directory to file " + 
                dataFile.getAbsolutePath() + " ...");
        PrintWriter out;
        try {
            out = new PrintWriter( dataFile );
        }
        catch (IOException e) {
            error("ERROR: Can't open data file."
                    + "Phone book data can't be saved.");
            return;
        }
        for ( PhoneEntry entry : entries ) {
            out.println( entry.nameProperty().get() + "%" + entry.numberProperty().get() );
        }
        out.flush();
        out.close();
        if (out.checkError())
            error("ERROR: Error occurred while writing the data file."
                    + "Phone book data might not have been saved correctly.");
    }
    
    
     /* showing an informational message to the user. */
     
    private void message(String text) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION, text);
        alert.setHeaderText(null);
        alert.showAndWait();
    }
    
    /* showing an error message to the user. */
    private void error(String text) {
        Alert alert = new Alert(Alert.AlertType.ERROR, text);
        alert.setHeaderText(null);
        alert.showAndWait();
    }
    
    
} 