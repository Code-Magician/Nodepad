package application;

import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Stack;

import javax.sql.RowSet;
import javax.swing.text.StyledEditorKit.FontSizeAction;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument; 
import org.apache.pdfbox.pdmodel.PDPage; 
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.PDPageContentStream.AppendMode;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts.FontName;

import javafx.beans.InvalidationListener;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.Clipboard;
import javafx.scene.input.DataFormat;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.media.VideoTrack;
import javafx.scene.text.Font;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

/**
 * @author <h1> Priyansh Kumar Singh </h1>
 * @version 1.0
 */
public class Controller  {
	Double MIN_FONT_SIZE = 10.0;					// Minimum Font size that textArea can have.
	Double DEFAULT_FONT_SIZE = 12.0;				// Default Font size of text Area.
	Double MAX_FONT_SIZE = 50.0;					// Maximum Font size that textArea can have.
	
	
	@FXML private TextArea textArea;				// Area where we write text.
	@FXML private Label locationLabel;				// Label which tells us the rows and columns of the caret position.
	@FXML private GridPane replaceAllPane;			// Grid pane that shows up when we click on Replace All Menu Item in Edit.
	@FXML private TextField replaceTextField;		// Element in {replaceAllPane} that takes input string that will be replaced by {replaceWithField}.
	@FXML private TextField replaceWithField;		// Element in {replaceWithField} that takes input string that will be placed in place of {replaceTextField}.
	@FXML private HBox gotoPaneBox;					// HBox that shows up when we click on the Goto MenuItem in Edit menu.
	@FXML private TextField gotoLineField;			// Element in {gotoPaneBox} that takes string as a Input which is row number to place caret.
	
	
	private Stage stage;							// Stores Primary Stage.
	private Scene scene;							// Stores Current Scene.
	
	private File file;								// Stores current file that is opened.
	
	private boolean hasUpdateSaved = true;			// Flag to check if the changes made to the text are saved or not.
	
	private int row = 0, col = 0;					// Stores the row and column number of the caret position.
	
	Stack<String> undoStack;						// stores previous saved texts for Undo Operation.
	String intialContentString = "";				// Stores Initial string it's empty if no file is opened. and contains the content of file if any file is opened.
	String prevString = "";							// Stores the previous Saved String.
	

	
	
	
	/**
	 * @param scene : Currently Opened Scene.
	 * Handles all the code that needs to be initialized before the app starts.
	 */
	public void Initialize(Scene scene) {
//		Setting scene.
		this.scene = scene;
		
//		setting primary stage.
		stage = (Stage)scene.getWindow();
		
//		Making an instance of the stack.
		undoStack = new Stack<String>();
		
//		Setting valid title to the App when it openes first.
		SetValidTitle();
		
//		Setting Change Listeners to the the TextArea.
		SetTextAreaListeners();
	}
	
	
	
	/**
	 * Adds Caret Position and text change listener.
	 */
	public void SetTextAreaListeners()
	{
//		Caret Position Listener.
		textArea.caretPositionProperty().addListener(new ChangeListener<Number>() {

			@Override
			public void changed(ObservableValue<? extends Number> arg0, Number arg1, Number arg2) {
//				Set caret position on every change in Text Area.
				SetCaretPosition();
			}
			
		});
		
//		Text Change Listener.
		textArea.textProperty().addListener(new ChangeListener<String>() {

			@Override
			public void changed(ObservableValue<? extends String> arg0, String arg1, String arg2) {
//				If the Changes previously were saved then make unsaved true.
				if(hasUpdateSaved)
				{
					hasUpdateSaved = false;
				}
				
//				If text area is empty and there is not file opened then make the save true.
				if(file == null && textArea.getText().isEmpty())
				{
					hasUpdateSaved = true;
				}
				
//				Set valid title.
				SetValidTitle();
			}
		});
	}
	
	
	
	/**
	 * Changes locationLabel based on caret position.
	 */
	public void SetCaretPosition() {
//		Getting the caret position.
		int caret = textArea.getCaretPosition();
		
//		Getting the current textArea string and calculating it's length.
		String textString = textArea.getText();
		int currTextLength = textString.length();
		
//		Initial row is 1 and column is 0.
		row = 1; col = 0;
		for(int i=0; i<Math.min(caret, currTextLength); i++)
		{
//			If newline character is found in string then increase row number and set column number to 0.
			if(textString.charAt(i) == '\n')
			{
				row++;
				col = 0;
			}
//			Else increase the column number.
			else {
				col++;
			}
		}
		
//		Setting the location label.
		locationLabel.setText("Ln : " + row + ", Col : " + col);
	}
	
	
	
	/**
	 * Sets the valid title based on if the file is saved or unsaved and if any file is opened or not.
	 */
	public void SetValidTitle()
	{
//		If all the updates has been saved then set the title with no {*}.
//		file title will be the name of the file if any file is opened else it will be untitled.
		if(hasUpdateSaved)
		{
			stage.setTitle(file!=null?file.getName():"Untitled");
		}
//		If updates are not saved then set the file title with the {*} symbol in preceding it.
//		file title will be the name if any file is opened else it will be Untitled.
		else {
			if(file == null)	stage.setTitle("*Untitled");
			else 				stage.setTitle("*"+file.getName());
		}
	}
	
	
	
	/**
	 * If the file is not saved then this alert box appears.
	 */
	public Alert SavingAlert()
	{
//		If the file is not saved then make a confirmation alert box.
//		It has 3 buttons {save, don't save, cancel}
		Alert alert = new Alert(AlertType.CONFIRMATION);
		alert.setTitle("Notepad");
		alert.setHeaderText("Do you want to save Changes to "+ (file!=null?file.getName():"Untitled") + "?");
			
		ButtonType saveButtonType = new ButtonType("Save", ButtonData.YES);
		ButtonType dSaveButtonType = new ButtonType("Don't Save", ButtonData.NO);
			
		alert.getButtonTypes().remove(0);
		alert.getButtonTypes().addAll(dSaveButtonType, saveButtonType);
			
		return alert;
	}
	
	
	/**
	 * Filters the files with extension provided in File chooser Dialog.
	 * @param fileChooser	Extension filter will be set in this file choooser.
	 * @param extension		Extension that will be filtered.
	 */
	public void SetExtentionFilter(FileChooser fileChooser, String extension)
	{
		FileChooser.ExtensionFilter extensionFilter = new FileChooser.ExtensionFilter("FILE EXTENTION : (*." +extension + ")", "*." +extension);
		fileChooser.getExtensionFilters().add(extensionFilter);
	}
	
	
	
	/**
	 * Function that Handles New MenuItem in File Menu.
	 * Creates a new file.
	 */
	public void File_New() {
//		Make current file = null
		file = null;

//		Clears the text area.
		textArea.clear();
	}
	
	
	
	/**
	 * Opens a file dialog box to open a file.
	 * <br>
	 * Sets the Choosen file to the {file} variable.
	 * <br>
	 * Sets the text of the file to textArea so we can edit.
	 */
	public void File_Open()
	{
//		Opens a file chooser.
		FileChooser fileChooser = new FileChooser();
		fileChooser.setTitle("Open File");
		SetExtentionFilter(fileChooser, "txt");
		
//		If file is not saved then open saving alert box. else continue to open the open dialog.
		if(!hasUpdateSaved)
		{
//			Alert Box.
			Alert alert = SavingAlert();
			
//			Getting the ButtonType Pressed.
			ButtonType type = alert.showAndWait().get();
			
//			if the ButtonType is null of cross is pressed or Cancel button is pressed then return do noting.
			if(type == null || type == ButtonType.CANCEL)		return;
			
//			If the button type has data yes which is for Save. then save file.
			if(type.getButtonData() == ButtonData.YES)
			{
				File_Save();
				
//				If the user clicked on Open->Save(Alert)->Cancel then just return to previous opened file and do noting.
				if(!hasUpdateSaved)		return;
			}
			
//			If the Button type has Data No then it is for Don't Save. 
			if(type.getButtonData() == ButtonData.NO)
			{
//				Opens a OpenDialog and for choosing file.
				File openedFile = fileChooser.showOpenDialog(stage);
				
//				If any file is not opened then return.
				if(openedFile == null)		return;
				
//				If we opened a new file then it must be already saved.
				hasUpdateSaved = true;
				
//				Set opened file to current file.
				file = openedFile;
			}
		}
		else {
			File openedFile = fileChooser.showOpenDialog(stage);
			
			if(openedFile == null)		return;
			file = openedFile;
		}
		
//		Set the valid title.
		SetValidTitle();
		
		
//		Opens FileReader and reads the text file. 
//		Sets Content of the file in textArea.
		try {
			FileReader fr = new FileReader(file.getAbsoluteFile());
			BufferedReader br = new BufferedReader(fr);
			
			String lineString, contentString = "";
			while((lineString = br.readLine()) != null)
			{
				if(contentString.isEmpty())		contentString = lineString;
				else							contentString += ('\n' + lineString);
			}
			textArea.setText(contentString);
		}catch (IOException e) {
			System.out.println(e.getMessage());
		}
		catch (Exception e) {
			System.out.println(e.getMessage());
		}
		
		intialContentString = textArea.getText();
		prevString = intialContentString;
	}
	
	
	
	/**
	 * If no file is opened then call Save as method.
	 * <br>
	 * else save the content of textArea in the opened file.
	 */
	public void File_Save()
	{	
//		If file is null then call save as
		if(file == null)
		{
			File_SaveAs();
			return;
		}
		
//		Add the previous string into the stack.
		undoStack.add(prevString);
//		Now current text in textArea is the new SavePoint.
//		So set content of textArea to prevString.
		prevString = textArea.getText();
		
		
		try (PrintWriter pw = new PrintWriter(file)) {
			pw.print(textArea.getText());
		}catch (Exception e) {
			System.out.println(e.getMessage());
		}
		
//		File is saved.
		hasUpdateSaved = true;
		
//		Set the valid title.
		SetValidTitle();
	}
	
	
	
	
	/**
	 * Makes a new empty file.
	 * <br>
	 * Saves content of the textArea in empty file.
	 */
	public void File_SaveAs()
	{
//		Make an instance of file chooser.
		FileChooser fileChooser = new FileChooser();
		fileChooser.setTitle("Save File");
		SetExtentionFilter(fileChooser, "txt");
		
//		Open save file dialog.
		File savedfile = fileChooser.showSaveDialog(stage);
		
//		If cancel is clicked on save dialog then return do noting.
		if(savedfile == null)	return;
//		Else set saved file to the current file.
		else{
			file = savedfile;
				
//			Add the previous string into the stack.
			undoStack.add(prevString);
//			Now current text in textArea is the new SavePoint.
//			So set content of textArea to prevString.
			prevString = textArea.getText();
		}
		
		try (PrintWriter pw = new PrintWriter(file)) {
			pw.print(textArea.getText());
		}catch (Exception e) {
			System.out.println(e.getMessage());
		}
		
//		File is saved.
		hasUpdateSaved = true;
		
//		Set the valid title.
		SetValidTitle();
	}
	
	
	
	/**
	 * Print the content of file as a pdf.
	 * @throws IOException 
	 */
	public void File_Print() throws IOException 
	{
//		Open File Chooser
		FileChooser fileChooser = new FileChooser();
		fileChooser.setTitle("Save As PDF File");
		
//		Set Extension to PDF.
		SetExtentionFilter(fileChooser, "pdf");
		
//		Open Save file dialog.
		File savedFile = fileChooser.showSaveDialog(stage);
		
//		If no file is saved and dialog closed then return.
		if(savedFile == null)	return;
		
//		Getting file path and content.
		String filePath =	savedFile.getAbsolutePath();
//		Have to split using newline character because PDFBOX encoding does not support special characters like '\n'.
        String[] content = textArea.getText().split("\n");
         
//      Make an PDF document.
        PDDocument doc = new PDDocument();
        try {
//        	Make a PDF page.
            PDPage page = new PDPage();
            
//          Add Page to PDF Document.
            doc.addPage(page);
             
//          Font of the text.
            PDFont font = new PDType1Font(FontName.TIMES_ROMAN);
 
//          Open PDF content stream.
            PDPageContentStream contents = new PDPageContentStream(doc, page, AppendMode.APPEND, true, true);
            
//          Begin the Text Content writing for PDF.
            contents.beginText();
            
//          Set Font and size.
            int fontSize = (int)textArea.getFont().getSize();
            contents.setFont(font, fontSize);
            
//          Set offset (x, y) or starting position of text in a page.
//          (0, 0) is at bottom left.
            int initialX = (Integer)MAX_FONT_SIZE.intValue(), initialY = (int) (page.getBBox().getHeight()-MAX_FONT_SIZE);
            contents.newLineAtOffset(initialX, initialY);
            
//          Set Vertical distance between lines.
            float leadingDist = (float)(4.0*fontSize/5.0);
            contents.setLeading(leadingDist);
            
//          Max Number of lines that can be adjusted in one page.
//          Page length is multiplied by factor of 2.2 and then divided by the (fontsize + leadingDist).
            int maxLinesInOnePage = (int)(((2.2*(initialY))/(float)(fontSize + leadingDist)));
            
//          Add Content to the page.
            for(int i=0; i<content.length; i++)
            {
//            	Adding text
            	contents.showText(content[i]);
//            	Making new Line.
            	contents.newLine();
            	
//            	if current number of lines is a multiple of maximum number of lines then add new page.
            	if((i+1)%maxLinesInOnePage == 0)	
            	{
//					End the text and close the previous page stream.
            		contents.endText();
            		contents.close();
            		
//            		Making a new page and adding it to the document.
            		PDPage newPage = new PDPage();
            		doc.addPage(newPage);
            		
//            		Setting newPage to the page variable.
            		page = newPage;
            		
//            		Opening new Content stream for this new page in append mode and beginning text.
            		contents = new PDPageContentStream(doc, page, AppendMode.APPEND, true, true);
            		contents.beginText();
            		
//            		Setting font.
                    contents.setFont(font, fontSize);
                    
//                  Setting line offsets and leading distance.
                    contents.newLineAtOffset(initialX, initialY);
                    contents.setLeading((float)(4.0*fontSize/5.0));
            	}
            }
            
//          End the Text content writing in PDF.
            contents.endText();
            
//          Close the Content stream.
            contents.close();

//          Save the PDF Document in the specified path.
            doc.save(filePath);
        }
        catch (IOException e) {
        	System.out.println(e.getMessage());
		}
        catch (Exception e) {
			System.out.println(e.getMessage());
		}
        finally {
//        	Close the Document.
            doc.close();
        }
	}
	
	
	
	/**
	 * Handles Exit Button.
	 * <br>
	 * If file is unsaved then opens a warning dialog box.
	 */
	public void File_Exit()
	{
		if(!hasUpdateSaved)
		{
//			Alert Box.
			Alert alert = SavingAlert();
			
//			Getting the ButtonType Pressed.
			ButtonType type = alert.showAndWait().get();
			
//			if the ButtonType is null of cross is pressed or Cancel button is pressed then return do noting.
			if(type == null || type == ButtonType.CANCEL)		return;
			
//			If the button type has data yes which is for Save. then save file.
			if(type.getButtonData() == ButtonData.YES) {
				File_Save();
				
				if(!hasUpdateSaved)	return;
			}
		}
		
		stage.close();
	}
	
	
	
	/**
	 * Cuts the Selected Text in the textArea and adds it to top of clipboard.
	 */
	public void Edit_Cut()
	{
		textArea.cut();
	}
	
	
	
	/**
	 * Copies the Selected Text in the textArea and adds it to top of clipboard.
	 */
	public void Edit_Copy()
	{
		textArea.copy();
	}
	
	
	
	/**
	 * Pastes the text at top of the clipboard to the caret position.
	 */
	public void Edit_Paste()
	{
//		Get the system clipboard.
		Clipboard clipboard = Clipboard.getSystemClipboard();
		
//		If clipboard does not have content of text format then return.
		if(!clipboard.hasContent(DataFormat.PLAIN_TEXT))
		{
			return;
		}
		
//		get the top text content from clipboard.
		String string = clipboard.getString();
		
//		Insert the text at the caret position.
		textArea.insertText(textArea.getCaretPosition(), string);
	}
	
	
	
	/**
	 * Delete the selected text in textArea.
	 */
	public void Edit_Delete()
	{
		textArea.deleteText(textArea.getSelection());
	}
	
	
	
	/**
	 * Sets the Replace all panel to true.
	 */
	public void Edit_ReplaceAll()
	{
		replaceAllPane.setVisible(true);
		textArea.setDisable(true);
	}
	
	
	
	/**
	 * Gets the replacing text and replace with text from replaceAllPane.
	 * then replaces the texts.
	 */
	public void ReplaceAll()
	{
		Reset();
		
//		If the replacing text is empty then return.
		if(replaceTextField.getText().isEmpty())	return;
		
//		Replaces the text
		textArea.setText(textArea.getText().replace(replaceTextField.getText(), replaceWithField.getText()));
	}
	
	
	
	/**
	 * Sets gotoPaneBox visible and calls Move caret function
	 * <br>
	 * which moves the caret to the specified line.
	 */
	public void Edit_GotoLine()
	{
		gotoPaneBox.setVisible(true);
		textArea.setDisable(true);
	}
	
	
	
	/**
	 * Moves the Caret to the specified line.
	 */
	public void MoveCaret()
	{
//		Stores the row number to set caret in.
		int rowIn = textArea.getCaretPosition();
		
//		Parsing the row number input from the gotoLineField.
		try {
			rowIn = Integer.parseInt(gotoLineField.getText());
		}catch (NumberFormatException e) {
			gotoLineField.setText("Invalid Input.");
			return;
		}catch (Exception e) {
			gotoLineField.setText(e.getMessage());
			return;
		}
		
		Reset();
		
//		Getting the textArea string and it's current length.
		String textString = textArea.getText();
		int currTextLength = textString.length();
		
//		Counting Maximum rows in textArea.
		int maxRows = 1;
		for(int i=0;i<currTextLength; i++)
		{
			if(textString.charAt(i) == '\n')	maxRows++;
		}
		
//		Stores the final position of the caret.
		int caretPosFinal = textArea.getCaretPosition();
		
//		If the row number input is not between [1, maxRows] range then do nothing.
		if(rowIn > maxRows || rowIn <= 0)	return;
		else
		{
//			Count the position of the input row number and set the position to the {caretPosFinal}.
			int row = 1;
			for(int i=0;i<currTextLength; i++)
			{
				if(row == rowIn)
				{
					caretPosFinal = i;
					break;
				}
				
				if(textString.charAt(i) == '\n')		row++;
			}
		}
		
//		Set the Caret Position.
		textArea.positionCaret(caretPosFinal);
	}
	
	
	public void Reset()
	{
//		System.out.println("Clicked");
		replaceAllPane.setVisible(false);
		gotoPaneBox.setVisible(false);
		textArea.setDisable(false);
	}
	
	
	
	/**
	 * Selects all the text in the textArea.
	 */
	public void Edit_SelectAll()
	{
		textArea.selectAll();
	}
	
	
	
	
	/**
	 * Appends the current time and date in the textArea.
	 */
	public void Edit_TimeDate()
	{
//		Getting local time date.
		LocalDateTime dt = LocalDateTime.now();
		
//		Appending current time date at end of textArea text in the format mentioned below.
		textArea.appendText(DateTimeFormatter.ofPattern("dd/MMM/yyyy  HH:mm:ss").format(dt));
	}
	
	
	
	/**
	 * Replaces the current text in textArea with 
	 * <br>
	 * previously saved text.
	 */
	public void Edit_Undo()
	{
//		If stack is empty or there were no save points then replace text with the initial text.
		if(undoStack.isEmpty())
		{
			textArea.setText(intialContentString);
		}
//		Else pop the last saved text and set it.
		else {
			String textString = undoStack.pop();
			textArea.setText(textString);
		}
	}
	
	
	
	/**
	 * Increases the Font size by 1 unit.
	 */
	public void View_ZoomIn()
	{
//		Getting the current font size.
		double fontSize = textArea.getFont().getSize();
		
//		Increase the font size by 1 and bound it in between the Range Specified.
		fontSize = Math.min(fontSize+1, MAX_FONT_SIZE);
		
//		Set the font size.
		Font font = new Font(textArea.getFont().getName(), fontSize);
		textArea.fontProperty().set(font);
	}
	
	
	
	/**
	 * Decreases the Font size by 1 unit.
	 */
	public void View_ZoomOut()
	{
//		Getting the current font size.
		double fontSize = textArea.getFont().getSize();
		
//		Decrease the font size by 1 and bound it in between the Range Specified.
		fontSize = Math.max(fontSize-1, MIN_FONT_SIZE);
		
//		Set the font size.
		Font font = new Font(textArea.getFont().getName(), fontSize);
		textArea.fontProperty().set(font);
	}
	
	
	
	/**
	 * Reset the Font size to default.
	 * <br>
	 * which is 12.0
	 */
	public void View_ZoomReset()
	{
		Font font = new Font(textArea.getFont().getName(), DEFAULT_FONT_SIZE);
		textArea.fontProperty().set(font);
	}
}
