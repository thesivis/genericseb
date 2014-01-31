/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package br.ufmt.genericgui;

import java.io.IOException;
import java.net.URL;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.Menu;
import javafx.scene.layout.AnchorPane;

/**
 * FXML Controller class
 *
 * @author raphael
 */
public class GenericGUIController implements Initializable {

    private String screen;
    private ResourceBundle bundle;
    @FXML
    private AnchorPane panel;
    @FXML
    private Menu menuFile;
    private GenericController genericController;

    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // TODO
        bundle = rb;

//        try {
//            Parent root = FXMLLoader.load(Main.class.getResource("/br/ufmt/genericgui/menubar.fxml"), bundle);
//            panelMenu.getChildren().clear();
//            panelMenu.getChildren().add(root);
//        } catch (IOException ex) {
//            Logger.getLogger(GenericGUIController.class.getName()).log(Level.SEVERE, null, ex);
//        }

    }

    @FXML
    private void landSatSebalAction(ActionEvent event) {
        screen = "/br/ufmt/genericsebalgui/sebal-landsat-gui.fxml";
        changeContent(screen);
    }
    
    @FXML
    private void csvAction(ActionEvent event) {
        screen = "/br/ufmt/genericcsv/genericcsv.fxml";
        changeContent(screen);
    }
    
    @FXML
    private void sebAction(ActionEvent event) {
        screen = "/br/ufmt/genericsebgui/genericseb.fxml";
        changeContent(screen);
    }

    @FXML
    private void brAction(ActionEvent event) {
        changeLanguage("br");
    }

    @FXML
    private void enAction(ActionEvent event) {
        changeLanguage("en");
    }
    
    @FXML
    private void openAction(ActionEvent event) {
        genericController.openNew();
    }
    
    @FXML
    private void saveAction(ActionEvent event) {
        genericController.saveNew();
    }

    private void changeContent(String xml) {
        try {
            if (xml != null && !xml.isEmpty()) {
                URL url = Main.class.getResource(xml);
                FXMLLoader fxmlLoader = new FXMLLoader();
                fxmlLoader.setResources(bundle);
                fxmlLoader.setLocation(url);
                Parent root = (Parent) fxmlLoader.load(url.openStream());
                panel.getChildren().clear();
                panel.getChildren().add(root);
                genericController = fxmlLoader.getController();
                menuFile.setVisible(true);
            }
        } catch (IOException ex) {
            Logger.getLogger(GenericGUIController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void changeLanguage(String language) {
        Main.locale = new Locale(language);
        ResourceBundle rb = ResourceBundle.getBundle("br.ufmt.bundles.lang", Main.locale);
        bundle = rb;
        Main.reload();
        changeContent(screen);
    }
}
