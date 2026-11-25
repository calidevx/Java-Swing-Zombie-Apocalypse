package deadsignal;

import javax.swing.*;

/**
 * DEAD SIGNAL - entry point.
 *   java -cp out deadsignal.Main            play the game
 *   java -cp out deadsignal.Main --test     headless bot play-through (writes screenshots to ./shots)
 */
public final class Main {
    private Main() { }

    public static void main(String[] args) throws Exception {
        if (args.length > 0 && args[0].equals("--test")) { TestHarness.run(args.length > 1 ? args[1] : "shots"); return; }
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("DEAD SIGNAL");
            GamePanel panel = new GamePanel(frame);
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.add(panel);
            frame.pack();
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
            panel.requestFocusInWindow();
            panel.start();
        });
    }
}
