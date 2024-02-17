import java.util.ArrayList;
import java.util.Scanner;
import java.io.*;
import javax.swing.SwingUtilities;

public class Start {

    public static void main(String[] args) {

        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                new HomePage().setVisible(true);
            }
        });
    }
}