import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Scalar;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.io.File;

import static org.opencv.imgproc.Imgproc.rectangle;

public class Main {
    public static void main(String[] args) throws Exception {
        Rect r = find_with_mask("template.png");
        click(r.Center());
    }


    public static void move(Point p) throws AWTException {
        Robot robot = new Robot();
        robot.mouseMove(p.x, p.y);
    }


    public static void click(Point p) throws AWTException {
        Robot robot = new Robot();
        move(p);
        robot.mousePress(InputEvent.BUTTON1_DOWN_MASK);
        robot.mouseRelease(InputEvent.BUTTON1_DOWN_MASK);
    }


    public static void enter_text(String keys) throws AWTException {
        Robot robot = new Robot();
        for (char c : keys.toCharArray()) {
            int keyCode = KeyEvent.getExtendedKeyCodeForChar(c);
            if (KeyEvent.CHAR_UNDEFINED == keyCode) {
                throw new RuntimeException(
                        "Key code not found for character '" + c + "'");
            }
            robot.keyPress(keyCode);
            robot.delay(100);
            robot.keyRelease(keyCode);
            robot.delay(100);
        }
    }


    public static Rect find(String templateFile, int match_method) throws Exception {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
        if (match_method == -1){
            match_method = Imgproc.TM_CCORR_NORMED;
        }
        saveScreen(0, 0, 1000, 1000);
        Mat img = Imgcodecs.imread("screen.png");
        Mat templ = Imgcodecs.imread(templateFile);

        int result_cols = img.cols() - templ.cols() + 1;
        int result_rows = img.rows() - templ.rows() + 1;
        Mat result = new Mat(result_rows, result_cols, CvType.CV_32FC1);


        Imgproc.matchTemplate(img, templ, result, match_method);
        Core.MinMaxLocResult mmr = Core.minMaxLoc(result);

        org.opencv.core.Point matchLoc;
        double best_result = 0.0;
        if (match_method == Imgproc.TM_SQDIFF
                || match_method == Imgproc.TM_SQDIFF_NORMED) {
            matchLoc = mmr.minLoc;
            best_result = 1 + mmr.minVal;
        } else {
            matchLoc = mmr.maxLoc;
            best_result = mmr.maxVal;
        }
        if (best_result < 0.9991){
            throw new Exception(templateFile + " Не найдено! Лучшее совпадение:" + (mmr.maxVal * 100));
        }
        return new Rect((int) matchLoc.x, (int) matchLoc.y, templ.cols(),  templ.rows());

    }


    public static Rect find_with_mask(String templateFile) throws Exception {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
        saveScreen(0, 0, 1000, 1000);
        Mat img = Imgcodecs.imread("screen.png");
        Mat templ = Imgcodecs.imread(templateFile);
        Mat mask = new Mat(templ.size(), CvType.CV_8UC3, new Scalar(255, 255, 255));
        rectangle(mask, new org.opencv.core.Point(8, 9), new org.opencv.core.Point(31, 31), new Scalar(0), -1);

        int result_cols = img.cols() - templ.cols() + 1;
        int result_rows = img.rows() - templ.rows() + 1;
        Mat result = new Mat(result_rows, result_cols, CvType.CV_32FC1);

        Imgproc.matchTemplate(img, templ, result, Imgproc.TM_CCORR_NORMED, mask);
        Core.MinMaxLocResult mmr = Core.minMaxLoc(result);

        org.opencv.core.Point matchLoc = mmr.maxLoc;
        if (mmr.maxVal < 0.9991){
            throw new Exception(templateFile + " Не найдено! Лучшее совпадение:" + (mmr.maxVal * 100));
        }
        return new Rect((int) matchLoc.x, (int) matchLoc.y, templ.cols(),  templ.rows());

    }


    public static void saveScreen(int x, int y, int w, int h) throws Exception {
        Rectangle screenRect = new Rectangle(x, y, w, h);
        BufferedImage capture = new Robot().createScreenCapture(screenRect);
        File imageFile = new File("screen.png");
        ImageIO.write(capture, "png", imageFile );
    }
}