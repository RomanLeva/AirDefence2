package airdefence;

import java.net.URL;
import java.util.ResourceBundle;
import java.util.StringTokenizer;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

public class HeadQuartersController implements Initializable {
    @FXML
    private Canvas myCanvas;
    private GraphicsContext gc;
    
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        gc = myCanvas.getGraphicsContext2D();
        gc.setFont(Font.font(20));
        gc.setStroke(Color.BLUE);
    }    
    void draw(String data){
        Image img;
        String imageName, code = "", dir = "", time = "";
        int speed = 0, xt = 0, yt = 0, xr = 0, yr = 0, imH = 0, imW = 0;
        StringTokenizer tokenizer1 = new StringTokenizer(data, "\n");
        while (tokenizer1.hasMoreTokens()) { 
            StringTokenizer tokenizer2 = new StringTokenizer(tokenizer1.nextToken(), "/");
            if (tokenizer2.countTokens() == 3) {
                img = new Image("img/"+tokenizer2.nextToken()+".jpg");
                xr = Integer.parseInt(tokenizer2.nextToken());
                yr = Integer.parseInt(tokenizer2.nextToken());
                gc.drawImage(img, xr - img.getWidth()/2, yr - img.getHeight()/2);
                gc.setStroke(Color.LIGHTBLUE);
                gc.strokeOval(xr-200, yr-200, 400, 400);
            } else {
                imageName = tokenizer2.nextToken();
                code = tokenizer2.nextToken().substring(0, 3);
                dir = tokenizer2.nextToken();
                tokenizer2.nextToken();
                xt = Integer.parseInt(tokenizer2.nextToken());
                yt = Integer.parseInt(tokenizer2.nextToken());
                tokenizer2.nextToken();
                img = new Image("img/"+imageName+"_"+dir+".jpg");
                imH = (int)img.getHeight();
                imW = (int)img.getWidth();
                gc.drawImage(img, xt - imW / 2, yt - imH / 2, 30, 30);
            }
            switch(code){
                case "ACQ": 
                    gc.setStroke(Color.RED);
                    gc.strokeOval(xt + 2 - imW / 2, yt + 2 - imH / 2, imW-4, imH-4);
                    break;
                case "DST":
                    img = new Image("img/DESTROYED.jpg");
                    gc.drawImage(img, xt - imW / 2, yt - imH / 2, 30, 30);
                    break;
                case "MST":
                    gc.setStroke(Color.GREEN);
                    gc.strokeOval(xt + 2 - imW / 2, yt + 2 - imH / 2, imW-4, imH-4);
                    break;
                case "AAM":
                    break;
                default:break;
            }
        
        }
    }
    void delete(String data) {
        int xt, yt;
        StringTokenizer tokenizer1 = new StringTokenizer(data, "\n");
        tokenizer1.nextToken();
        while (tokenizer1.hasMoreTokens()) {            
            StringTokenizer tokenizer2 = new StringTokenizer(tokenizer1.nextToken(), "/");
            tokenizer2.nextToken();
            tokenizer2.nextToken();
            tokenizer2.nextToken();
            tokenizer2.nextToken();
            xt = Integer.parseInt(tokenizer2.nextToken());
            yt = Integer.parseInt(tokenizer2.nextToken());
            gc.clearRect(xt - 15, yt - 15, 30, 30);
        } 
    }
}

