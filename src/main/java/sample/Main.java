package sample;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {

    private Parent page;

    @Override
    public void start(Stage primaryStage) throws Exception{
        FXMLLoader loader = new FXMLLoader(Main.class.getClassLoader().getResource("fxml/sample.fxml"));
        page = loader.load();
        Controller controller = loader.getController();
        //primaryStage.setResizable(false);
        Scene scene = new Scene(page, 1040, 900);
        primaryStage.setTitle("eeg viewer");
        primaryStage.setScene(scene);
        //primaryStage.show();
        //primaryStage.setResizable(true);
    }


    public static void main(String[] args) {
        launch(args);
    }
}
