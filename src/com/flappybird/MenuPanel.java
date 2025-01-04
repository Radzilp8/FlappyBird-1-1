package com.flappybird;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;

public class MenuPanel extends JPanel {
    private JFrame frame;
    private Image backgroundImg;
    private String startText = "START GAME";
    private String leaderboardText = "LEADERBOARD";
    private boolean startHover = false;
    private boolean leaderboardHover = false;
    private boolean hoverPlayed = false;

    private final int BUTTON_WIDTH = 250;
    private final int BUTTON_HEIGHT = 45;
    private final int START_BUTTON_Y = 440;
    private final int LEADERBOARD_BUTTON_Y = 500;

    public MenuPanel(JFrame frame) {
        this.frame = frame;
        setPreferredSize(new Dimension(FlappyBird.BOARD_WIDTH, FlappyBird.BOARD_HEIGHT));

        // Load background image
        backgroundImg = new ImageIcon(getClass().getResource("/resources/flappybirdbg.png")).getImage();

        // Add mouse listeners for button interaction
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int startX = (getWidth() - BUTTON_WIDTH) / 2;
                int leaderboardX = (getWidth() - BUTTON_WIDTH) / 2;

                if (isHoveringButton(e.getPoint(), startX, START_BUTTON_Y, BUTTON_WIDTH, BUTTON_HEIGHT)) {
                    playMenuClickSound();
                    startGame();
                } else if (isHoveringButton(e.getPoint(), leaderboardX, LEADERBOARD_BUTTON_Y, BUTTON_WIDTH, BUTTON_HEIGHT)) {
                    playMenuClickSound();
                    openLeaderboard();
                }
            }
        });

        // Add mouse motion listener for hover effects
        addMouseMotionListener(new MouseAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                int startX = (getWidth() - BUTTON_WIDTH) / 2;
                int leaderboardX = (getWidth() - BUTTON_WIDTH) / 2;

                // Check hover state for buttons
                startHover = isHoveringButton(e.getPoint(), startX, START_BUTTON_Y, BUTTON_WIDTH, BUTTON_HEIGHT);
                leaderboardHover = isHoveringButton(e.getPoint(), leaderboardX, LEADERBOARD_BUTTON_Y, BUTTON_WIDTH, BUTTON_HEIGHT);

                // Play hover sound when hovering over a button
                if ((startHover || leaderboardHover) && !hoverPlayed) {
                    playMenuHoverSound();
                    hoverPlayed = true;
                } else if (!startHover && !leaderboardHover) {
                    hoverPlayed = false;
                }

                repaint();
            }
        });
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        // Draw background image
        g.drawImage(backgroundImg, 0, 0, getWidth(), getHeight(), null);

        // Draw game title image
        Image gameImage = new ImageIcon(getClass().getResource("/resources/flappyimg/flappybird.png")).getImage();
        int gameImageWidth = 98;
        int gameImageHeight = 80;
        int gameImageX = (getWidth() - gameImageWidth) / 2;
        int gameImageY = 80;
        g.drawImage(gameImage, gameImageX, gameImageY, gameImageWidth, gameImageHeight, null);

        // Set font for buttons
        g.setFont(new Font("Inter", Font.BOLD, 21));

        // Dynamically calculate button positions and draw them
        int startX = (getWidth() - BUTTON_WIDTH) / 2;
        int leaderboardX = (getWidth() - BUTTON_WIDTH) / 2;

        drawButton(g, startX, START_BUTTON_Y, BUTTON_WIDTH, BUTTON_HEIGHT, startText, startHover); // START GAME button
        drawButton(g, leaderboardX, LEADERBOARD_BUTTON_Y, BUTTON_WIDTH, BUTTON_HEIGHT, leaderboardText, leaderboardHover); // LEADERBOARD button
    }

    private void drawButton(Graphics g, int x, int y, int width, int height, String text, boolean isHovered) {
        // Set button color based on hover state
        g.setColor(isHovered ? new Color(0xe1d694) : new Color(0x4CAF50));
        g.fillRect(x, y, width, height);

        // Draw button text centered on the button
        g.setColor(Color.WHITE);
        FontMetrics fm = g.getFontMetrics();
        int textWidth = fm.stringWidth(text);
        int textX = x + (width - textWidth) / 2;
        int textY = y + (height + fm.getAscent()) / 2 - 5;
        g.drawString(text, textX, textY);
    }

    private boolean isHoveringButton(Point point, int x, int y, int width, int height) {
        // Check if the mouse is within the button bounds
        return point.x >= x && point.x <= x + width && point.y >= y && point.y <= y + height;
    }

    private void startGame() {
        frame.getContentPane().removeAll();
        SelectFlappy selectFlappy = new SelectFlappy(frame);
        frame.add(selectFlappy);
        frame.revalidate();
        frame.repaint();
        selectFlappy.requestFocus();
    }

    private void openLeaderboard() {
        frame.getContentPane().removeAll();  // Clear the current panel
        MenuLeaderboard menuLeaderboard = new MenuLeaderboard(frame);  // Initialize MenuLeaderboard
        frame.add(menuLeaderboard);  // Add the leaderboard panel
        frame.revalidate();  // Revalidate the frame to ensure it updates
        frame.repaint();  // Repaint the frame to show the new content
        menuLeaderboard.requestFocus();  // Request focus for the leaderboard panel
    }

    private void playMenuClickSound() {
        playSound("/resources/sfx/menuclick.wav");
    }

    private void playMenuHoverSound() {
        playSound("/resources/sfx/menuhoverclick.wav");
    }

    private void playSound(String soundPath) {
        try {
            AudioInputStream audioIn = AudioSystem.getAudioInputStream(getClass().getResource(soundPath));
            Clip clip = AudioSystem.getClip();
            clip.open(audioIn);
            clip.start();
        } catch (Exception ex) {
            ex.printStackTrace();
            System.out.println("Error playing sound: " + ex.getMessage());
        }
    }
}
