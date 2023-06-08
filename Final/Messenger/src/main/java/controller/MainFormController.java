package controller;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXTextField;
import connection.SocketConnection;
import dto.Message;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import service.CatchingMessageTask;

import java.io.File;
import java.io.IOException;
import java.net.Socket;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class MainFormController {
    public JFXTextField txtMessage;
    public VBox vBox;
    public JFXButton btnSendMessage;
    public JFXTextField txtName;
    public JFXButton btnChooseImage;


    public void initialize(){
        txtMessage.setVisible(false);
        btnSendMessage.setDisable(true);
        try {
            Socket socket = SocketConnection.getConnection().getSocket();
            CatchingMessageTask task = new CatchingMessageTask(socket);
            task.valueProperty().addListener(new ChangeListener<Message>() {
                @Override
                public void changed(ObservableValue<? extends Message> observable, Message oldValue, Message newValue) {
                    //System.out.println(newValue);
                    Label l1 = new Label(newValue.getMessage());
                    l1.setWrapText(true);
                    l1.setMaxWidth(100);
                    l1.setMinHeight(20);
                    if(newValue.getImage()==null){
                        HBox hb = new HBox();
                        hb.setAlignment(Pos.CENTER_LEFT);
                        hb.getChildren().add(l1);
                        vBox.getChildren().add(hb);
                    }else{
                        HBox hb = new HBox();
                        hb.setAlignment(Pos.CENTER_LEFT);
                        hb.getChildren().add(l1);
                        ImageView imageView = new ImageView(SwingFXUtils.toFXImage(newValue.getImage(), null));
                        imageView.setFitHeight(100);
                        imageView.setFitWidth(100);
                        hb.getChildren().add(imageView);
                        vBox.getChildren().add(hb);
                    }

                }
            });

            Thread t = new Thread(task);
            t.start();


        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void sendMessageOnKeyAction(ActionEvent actionEvent) throws IOException {
        Label l1 = new Label("ME : "+txtMessage.getText());
        l1.setWrapText(true);
        l1.setMaxWidth(100);
        l1.setMinHeight(20);
        HBox hb = new HBox();
        hb.setAlignment(Pos.CENTER_RIGHT);
        hb.getChildren().add(l1);
        vBox.getChildren().add(hb);
        SocketConnection.getConnection().sendToServer(txtName.getText()+" : "+txtMessage.getText());
    }

    public void sendMessageOnAction(ActionEvent actionEvent) throws IOException {
        sendMessageOnKeyAction(actionEvent);
    }

    public void txtNameOnAction(ActionEvent actionEvent) {
        String text = txtName.getText();
        Pattern compile = Pattern.compile("^[A-z|\\s]{3,}$");
        Matcher matcher = compile.matcher(text);
        if(matcher.matches()){
            txtMessage.setEditable(true);
            btnSendMessage.setDisable(false);
            txtName.setEditable(false);
            txtMessage.setVisible(true);
        }
        try {
            SocketConnection.getConnection().sendToServer(txtName.getText());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void btnChoseImgOnAction(ActionEvent actionEvent) {
        final FileChooser fileChooser = new FileChooser();
        btnChooseImage.setOnAction((final ActionEvent e) -> {
            File file = fileChooser.showOpenDialog((Stage)(btnChooseImage.getScene().getWindow()));
            if (file != null) {
                Image image1 = new Image(file.toURI().toString());
                HBox hBox = new HBox();
                hBox.setAlignment(Pos.CENTER_RIGHT);
                ImageView imageView = new ImageView(image1);
                imageView.setFitHeight(100);
                imageView.setFitWidth(100);
                hBox.getChildren().add(new Label("Me : "));
                hBox.getChildren().add(imageView);
                vBox.getChildren().add(hBox);
                try {
                    SocketConnection.getConnection().sendToServer(file,txtName.getText());
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        });
    }
}
