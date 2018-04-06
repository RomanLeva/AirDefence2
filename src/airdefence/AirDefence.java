package airdefence;

import java.util.logging.FileHandler;
import java.util.logging.SimpleFormatter;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 *
 * @author user
 */
public class AirDefence extends Application {
    static FileHandler fileHandler;
    @Override
    public void start(Stage stage) throws Exception {
        fileHandler = new FileHandler("app_log.txt", 50000, 2);
        fileHandler.setFormatter(new SimpleFormatter());
        FXMLLoader loader = new FXMLLoader();
        loader.setLocation(getClass().getClassLoader().getResource("HeadQuartersView.fxml"));
        Parent root = loader.load();
        HeadQuartersController controller = loader.<HeadQuartersController>getController();
        Scene scene = new Scene(root, Color.WHITE);
        stage.setScene(scene);
        stage.setOnCloseRequest(new EventHandler<WindowEvent>() {
               @Override
               public void handle(WindowEvent event) {
               Platform.exit();
               System.exit(0);
              }
            });
        stage.setResizable(false);
        stage.show();
        ApplicationContext apc = new ClassPathXmlApplicationContext("Beans.xml");
        HeadQuarters hq = (HeadQuarters) apc.getBean("headquarters");
        hq.setScreen(controller);
        RadarUnit ru1 = (RadarUnit) apc.getBean("radarunit1");
        ru1.beginRadarSimulation();
        RadarUnit ru2 = (RadarUnit) apc.getBean("radarunit2");
        ru2.beginRadarSimulation();
        hq.beginSimulation();
        
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        launch(args);
    }
    
}
