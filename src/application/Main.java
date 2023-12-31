package application;
	
import javafx.application.Application;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;


public class Main extends Application {
	@Override
	public void start(Stage primaryStage) {
		try {
//			Fxml Loader Object.
			FXMLLoader loader = new FXMLLoader(getClass().getResource("Main.fxml"));
			
//			Loading FXML document
			Parent root = loader.load();
			
//			Making scene from loaded fxml and setting scene on primary stage.
			Scene scene = new Scene(root);
			primaryStage.setScene(scene);
			
			primaryStage.setMinHeight(200); 	primaryStage.setMinWidth(300);
			
//			Getting Controller used in the FXML Document and Initializing.
			Controller controller = loader.getController();
			controller.Initialize(scene);
			
//			Setting Icon of the Image.
			primaryStage.getIcons().add(new Image("/Logo.png"));
			
//			Setting Cross Button Dialog if file is not saved.
			primaryStage.setOnCloseRequest(new EventHandler<WindowEvent>() {
				
				@Override
				public void handle(WindowEvent e) {
					e.consume();
					controller.File_Exit();
				}
			});
			
//			Showing Stage.
			primaryStage.show();
		} catch(Exception e) {
			e.printStackTrace();
		}
	}

	
	public static void main(String[] args) {
		launch(args);
	}
}
