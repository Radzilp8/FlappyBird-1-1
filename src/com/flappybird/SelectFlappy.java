package com.flappybird;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;

public class SelectFlappy extends JPanel {
    // Fields for assets and state management
    private Image backgroundImg;
    private Image[] birdImages;
    private String[] birdNames;
    private int selectedBirdIndex = 0;
    private int displayedBirdIndex = 0;

    // UI component bounds
    private Rectangle goButtonBounds;
    private Rectangle leftButtonBounds;
    private Rectangle rightButtonBounds;
    @SuppressWarnings("unused")
    private Rectangle displayedBirdBounds;

    private JFrame frame;
    private boolean hoverGoButton = false;
    private boolean hoverGoButtonPlayed = false;

    // Constructor
    public SelectFlappy(JFrame frame) {
        this.frame = frame;
        setPreferredSize(new Dimension(FlappyBird.BOARD_WIDTH, FlappyBird.BOARD_HEIGHT));

        // Initialize assets and UI components
        initializeAssets();
        initializeBounds();

        // Add back button
        JLabel backButton = createBackButton();
        setLayout(null); // Enable absolute positioning
        add(backButton);
        backButton.setBounds(15, 20, 40, 40);

        // Add mouse listeners
        addMouseListeners();
    }

    private void initializeAssets() {
        backgroundImg = new ImageIcon(getClass().getResource("/resources/flappybirdbg.png")).getImage();
        birdImages = new Image[] {
            new ImageIcon(getClass().getResource("/resources/flappyImg/flappybird.png")).getImage(),
            new ImageIcon(getClass().getResource("/resources/flappyImg/flappybird1.png")).getImage(),
            new ImageIcon(getClass().getResource("/resources/flappyImg/flappybird2.png")).getImage(),
            new ImageIcon(getClass().getResource("/resources/flappyImg/flappybird3.png")).getImage(),
            new ImageIcon(getClass().getResource("/resources/flappyImg/flappybird4.png")).getImage()
        };
        birdNames = new String[] {"Flappy Bird", "Flappy Man", "Flappy Bot", "Flappy Red", "Flappy Pinky"};
    }

    private void initializeBounds() {
        goButtonBounds = new Rectangle(130, 500, 100, 45);
        leftButtonBounds = new Rectangle(65, 185, 50, 50);
        rightButtonBounds = new Rectangle(FlappyBird.BOARD_WIDTH - 125, 185, 50, 50);
    }

    private JLabel createBackButton() {
        JLabel label = new JLabel("<", SwingConstants.CENTER);
        label.setFont(new Font("Inter", Font.PLAIN, 30));
        label.setForeground(Color.WHITE);

        label.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                label.setForeground(Color.RED);
                playSound("/resources/sfx/menuhoverclick.wav");
            }

            @Override
            public void mouseExited(MouseEvent e) {
                label.setForeground(Color.WHITE);
            }

            @Override
            public void mouseClicked(MouseEvent e) {
                playSound("/resources/sfx/back.wav");
                backToPreviousClass();
            }
        });

        return label;
    }

    private void addMouseListeners() {
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (goButtonBounds.contains(e.getPoint())) {
                    playSound("/resources/sfx/menuclick.wav");
                    startGameWithSelectedBird();
                } else if (leftButtonBounds.contains(e.getPoint())) {
                    playSound("/resources/sfx/chooseflappy.wav");
                    previousBird();
                } else if (rightButtonBounds.contains(e.getPoint())) {
                    playSound("/resources/sfx/chooseflappy.wav");
                    nextBird();
                }
            }
        });

        addMouseMotionListener(new MouseAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                hoverGoButton = goButtonBounds.contains(e.getPoint());
                if (hoverGoButton && !hoverGoButtonPlayed) {
                    playSound("/resources/sfx/menuhoverclick.wav");
                    hoverGoButtonPlayed = true;
                } else if (!hoverGoButton) {
                    hoverGoButtonPlayed = false;
                }
                repaint();
            }
        });
    }

    private void backToPreviousClass() {
        SwingUtilities.invokeLater(() -> {
            frame.getContentPane().removeAll();
            JPanel previousPanel = new MenuPanel(frame);
            frame.add(previousPanel);
            frame.revalidate();
            previousPanel.requestFocus();
        });
    }

    private void startGameWithSelectedBird() {
        SwingUtilities.invokeLater(() -> {
            frame.getContentPane().removeAll();
            FlappyBird flappyBird = new FlappyBird(birdImages[selectedBirdIndex]);
            frame.add(flappyBird);
            frame.revalidate();
            flappyBird.requestFocus();
        });
    }

    private void previousBird() {
        displayedBirdIndex = (displayedBirdIndex > 0) ? displayedBirdIndex - 1 : birdImages.length - 1;
        selectedBirdIndex = displayedBirdIndex;
        repaint();
    }

    private void nextBird() {
        displayedBirdIndex = (displayedBirdIndex < birdImages.length - 1) ? displayedBirdIndex + 1 : 0;
        selectedBirdIndex = displayedBirdIndex;
        repaint();
    }

    private void playSound(String soundPath) {
        try (AudioInputStream audioIn = AudioSystem.getAudioInputStream(getClass().getResource(soundPath))) {
            Clip clip = AudioSystem.getClip();
            clip.open(audioIn);
            clip.start();
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error playing sound: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        drawBackground(g);
        drawNavigationButtons(g);
        drawBirdImage(g);
        drawBirdName(g);
        drawGoButton(g);
    }

    private void drawBackground(Graphics g) {
        g.drawImage(backgroundImg, 0, 0, getWidth(), getHeight(), null);
    }

    private void drawNavigationButtons(Graphics g) {
        Font buttonFont = new Font("Inter", Font.BOLD, 25);
        g.setFont(buttonFont);
        g.setColor(Color.WHITE);
        FontMetrics fm = g.getFontMetrics(buttonFont);

        String leftArrow = "<";
        int leftArrowX = leftButtonBounds.x + (leftButtonBounds.width - fm.stringWidth(leftArrow)) / 2;
        int leftArrowY = leftButtonBounds.y + (leftButtonBounds.height + fm.getAscent()) / 2;
        g.drawString(leftArrow, leftArrowX, leftArrowY);

        String rightArrow = ">";
        int rightArrowX = rightButtonBounds.x + (rightButtonBounds.width - fm.stringWidth(rightArrow)) / 2;
        int rightArrowY = rightButtonBounds.y + (rightButtonBounds.height + fm.getAscent()) / 2;
        g.drawString(rightArrow, rightArrowX, rightArrowY);
    }

    private void drawBirdImage(Graphics g) {
        int birdX = 135;
        int birdY = 185;
        int birdWidth = 80;
        int birdHeight = 60;
        displayedBirdBounds = new Rectangle(birdX, birdY, birdWidth, birdHeight);
        g.drawImage(birdImages[displayedBirdIndex], birdX, birdY, birdWidth, birdHeight, null);
    }

    private void drawBirdName(Graphics g) {
        g.setFont(new Font("Inter", Font.BOLD, 30));
        g.setColor(Color.WHITE);
        String message = birdNames[selectedBirdIndex];
        FontMetrics fm = g.getFontMetrics();
        int textX = (getWidth() - fm.stringWidth(message)) / 2;
        g.drawString(message, textX, 100);
    }

    private void drawGoButton(Graphics g) {
        g.setColor(hoverGoButton ? new Color(0xe1d694) : new Color(0x4CAF50));
        g.fillRect(goButtonBounds.x, goButtonBounds.y, goButtonBounds.width, goButtonBounds.height);

        g.setColor(Color.WHITE);
        g.setFont(new Font("Inter", Font.BOLD, 25));
        String goText = "GO!";
        int goTextX = goButtonBounds.x + (goButtonBounds.width - g.getFontMetrics().stringWidth(goText)) / 2;
        int goTextY = goButtonBounds.y + (goButtonBounds.height + g.getFontMetrics().getAscent()) / 2 - 5;
        g.drawString(goText, goTextX, goTextY);
    }
}
