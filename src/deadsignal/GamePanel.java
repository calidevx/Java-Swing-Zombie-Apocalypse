package deadsignal;

import deadsignal.audio.SoundEngine;
import deadsignal.core.Const;
import deadsignal.core.Input;
import deadsignal.game.Game;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;

/**
 * The Swing component. Inherits from JPanel (INHERITANCE from the Java library) and overrides
 * paintComponent (POLYMORPHISM). A dedicated thread runs the fixed-step game loop and renders
 * into off-screen buffers; Swing just paints the latest finished frame, scaled to the window.
 */
public final class GamePanel extends JPanel {
    private static final long serialVersionUID = 1L;
    private final transient Input input = new Input();
    private final JFrame window;
    private transient Game game;
    private transient volatile BufferedImage frame;
    private double scale = 1, offX, offY;
    private boolean maximized;

    public GamePanel(JFrame window) {
        this.window = window;
        setPreferredSize(new Dimension(Const.WIDTH, Const.HEIGHT));
        setBackground(Color.BLACK);
        setFocusable(true);
        setFocusTraversalKeysEnabled(false);
        setCursor(Toolkit.getDefaultToolkit().createCustomCursor(new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB), new Point(0, 0), "blank"));
        installListeners();
    }

    private void installListeners() {
        addKeyListener(new KeyAdapter() {
            @Override public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_F11) { toggleMaximized(); return; }
                input.keyDown(e.getKeyCode());
            }
            @Override public void keyTyped(KeyEvent e) { input.charTyped(e.getKeyChar()); }
            @Override public void keyReleased(KeyEvent e) { input.keyUp(e.getKeyCode()); }
        });
        MouseAdapter mouse = new MouseAdapter() {
            @Override public void mousePressed(MouseEvent e) { requestFocusInWindow(); move(e); input.mouseButton(button(e), true); }
            @Override public void mouseReleased(MouseEvent e) { move(e); input.mouseButton(button(e), false); }
            @Override public void mouseMoved(MouseEvent e) { move(e); }
            @Override public void mouseDragged(MouseEvent e) { move(e); }
            @Override public void mouseWheelMoved(MouseWheelEvent e) { input.wheelMoved(e.getWheelRotation()); }
        };
        addMouseListener(mouse); addMouseMotionListener(mouse); addMouseWheelListener(mouse);
    }

    private static int button(MouseEvent e) { return SwingUtilities.isRightMouseButton(e) ? 3 : SwingUtilities.isLeftMouseButton(e) ? 1 : 2; }
    private void move(MouseEvent e) { input.mouseMoved((int) ((e.getX() - offX) / scale), (int) ((e.getY() - offY) / scale)); }
    private void toggleMaximized() { maximized = !maximized; window.setExtendedState(maximized ? JFrame.MAXIMIZED_BOTH : JFrame.NORMAL); }

    public void start() {
        game = new Game(input, SoundEngine.create());
        Thread t = new Thread(this::loop, "game-loop");
        t.setDaemon(true);
        t.start();
    }

    private void loop() {
        GraphicsConfiguration gc = getGraphicsConfiguration();
        BufferedImage[] buffers = new BufferedImage[3];
        for (int i = 0; i < 3; i++)
            buffers[i] = gc != null ? gc.createCompatibleImage(Const.WIDTH, Const.HEIGHT) : new BufferedImage(Const.WIDTH, Const.HEIGHT, BufferedImage.TYPE_INT_RGB);
        final double step = 1.0 / 60.0;
        long last = System.nanoTime();
        double acc = 0;
        int idx = 0;
        while (true) {
            long now = System.nanoTime();
            acc += Math.min(0.1, (now - last) / 1e9);
            last = now;
            try {
                int steps = 0;
                while (acc >= step && steps < 4) { game.update((float) step); acc -= step; steps++; }
                if (steps == 4) acc = 0;
                Graphics2D g = buffers[idx].createGraphics();
                game.render(g);
                g.dispose();
                frame = buffers[idx];
                idx = (idx + 1) % 3;
            } catch (Throwable ex) { ex.printStackTrace(); }
            repaint();
            long sleep = (long) (step * 1000) - (System.nanoTime() - now) / 1_000_000L;
            try { Thread.sleep(Math.max(1, sleep)); } catch (InterruptedException ie) { return; }
        }
    }

    @Override
    protected void paintComponent(Graphics g0) {
        super.paintComponent(g0);
        BufferedImage f = frame;
        if (f == null) return;
        scale = Math.min(getWidth() / (double) Const.WIDTH, getHeight() / (double) Const.HEIGHT);
        int dw = (int) (Const.WIDTH * scale), dh = (int) (Const.HEIGHT * scale);
        offX = (getWidth() - dw) / 2.0; offY = (getHeight() - dh) / 2.0;
        Graphics2D g = (Graphics2D) g0;
        if (scale != 1) g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g.drawImage(f, (int) offX, (int) offY, dw, dh, null);
        Toolkit.getDefaultToolkit().sync();
    }
}
