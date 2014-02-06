/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package br.ufmt.genericgui;

import java.io.IOException;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

/**
 *
 * @author raphael
 */
public class Main extends Application {

    public static Stage screen;
    public static Locale locale = new Locale("br");

    @Override
    public void start(Stage stage) throws Exception {
        screen = stage;
        stage.initStyle(StageStyle.DECORATED);
        ResourceBundle rb = ResourceBundle.getBundle("br.ufmt.bundles.lang", locale);

        Parent root = FXMLLoader.load(Main.class.getResource("/br/ufmt/genericgui/genericgui.fxml"), rb);
        Scene scene = new Scene(root);
        screen.setTitle(rb.getString("title"));
        screen.setScene(scene);
        screen.show();
    }
    
    public static void reload(){
        try {
            ResourceBundle rb = ResourceBundle.getBundle("br.ufmt.bundles.lang", locale);
            Parent root = FXMLLoader.load(Main.class.getResource("/br/ufmt/genericgui/genericgui.fxml"), rb);
            screen.setTitle(rb.getString("title"));
            screen.getScene().setRoot(root);
            screen.sizeToScene();
        } catch (IOException ex) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * The main() method is ignored in correctly deployed JavaFX application.
     * main() serves only as fallback in case the application can not be
     * launched through deployment artifacts, e.g., in IDEs with limited FX
     * support. NetBeans ignores main().
     *
     * @param args the command line arguments
     */
    public static void main(String[] args) {
//        try {
//            String fileLog = System.getProperty("user.dir") + "/log.log";
//            java.io.PrintStream log = new java.io.PrintStream(new java.io.FileOutputStream(new java.io.File(fileLog), true));
//            System.setErr(log);
//            
//            String fileConsole = System.getProperty("user.dir") + "/console.log";
//            java.io.PrintStream logConsole = new java.io.PrintStream(new java.io.FileOutputStream(new java.io.File(fileConsole), true));
//            System.setOut(logConsole);
//            
//        } catch (java.io.FileNotFoundException ex) {
//            java.util.logging.Logger.getLogger(Main.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
//        }
        launch(args);
    }
}