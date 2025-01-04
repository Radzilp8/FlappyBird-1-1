package com.flappybird;

import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import javax.swing.*;
import javax.sound.sampled.*;

public class FlappyBird extends JPanel implements ActionListener, KeyListener {
    // Game board dimensions
    public static final int BOARD_WIDTH = 360;
    public static final int BOARD_HEIGHT = 640;

    // Game images (background, bird, pipes)
    private Image backgroundImg, birdImg, topPipeImg, bottomPipeImg;

    // Bird properties
    private static final int BIRD_WIDTH = 34;
    private static final int BIRD_HEIGHT = 24;
    private int birdX = BOARD_WIDTH / 8, birdY = BOARD_HEIGHT / 2;

    // Pipe properties
    private static final int PIPE_WIDTH = 64;
    private static final int PIPE_HEIGHT = 512;
    private int pipeX = BOARD_WIDTH, pipeY = 0;

    // Game logic variables
    private Bird bird;
    private int velocityX = -4, velocityY = 0, gravity = 1;
    private ArrayList<Pipe> pipes;
    private Timer gameLoop, pipeGenerator, flickerMessageTimer;
    private boolean gameOver = false, isJumping = false, gameStarted = false, showMessage = true;
    private double score = 0;
    private Clip backgroundMusic;
    private JButton addToLeaderboardButton;

    // Constructor to initialize the game
    public FlappyBird(Image selectedBirdImg) {
        setPreferredSize(new Dimension(BOARD_WIDTH, BOARD_HEIGHT));
        setFocusable(true);
        addKeyListener(this);
        setLayout(null); // Set layout to null to manually control component positioning

        // Load images for the game
        loadImages(selectedBirdImg);

        // Initialize the bird object
        bird = new Bird(birdImg);
        pipes = new ArrayList<>();
        
        // Initialize timers for game loop and pipe generation
        initializeTimers();
    }

    private void loadImages(Image selectedBirdImg) {
        // Loading images for the game (background, bird, pipes)
        backgroundImg = new ImageIcon(getClass().getResource("/resources/flappybirdbg.png")).getImage();
        birdImg = selectedBirdImg;
        topPipeImg = new ImageIcon(getClass().getResource("/resources/pipes/toppipe.png")).getImage();
        bottomPipeImg = new ImageIcon(getClass().getResource("/resources/pipes/bottompipe.png")).getImage();
    }

    private void adjustPipeDistance() {
        if (score >= 10) {
            // Reduce the delay for pipe generation dynamically as the score increases
            int newDelay = 1500 - (int) (score * 10); // Decrease delay by 10ms per point
            pipeGenerator.setDelay(Math.max(newDelay, 800)); // Minimum delay of 800ms
        } else {
            pipeGenerator.setDelay(1500); // Default pipe generation interval
        }
    }   

    private void initializeTimers() {
        // Timer to generate pipes at intervals
        pipeGenerator = new Timer(1500, e -> {
            if (gameStarted && !gameOver) {
                spawnPipes();
                adjustPipeDistance();
            }
        });
        pipeGenerator.start();

        // Main game loop timer, runs at 60 FPS
        gameLoop = new Timer(1000 / 60, this);
        gameLoop.start();

        // Timer to toggle the start message visibility
        flickerMessageTimer = new Timer(500, e -> {
            if (!gameStarted) {
                showMessage = !showMessage;
                repaint();
            }
        });
        flickerMessageTimer.start();
    }

    private void spawnPipes() {
        // Base opening space between pipes
        int baseOpeningSpace = BOARD_HEIGHT / 4; // Default opening space (1/4 of the screen height)
        
        // Gradual reduction in opening space based on score
        int openingSpace = baseOpeningSpace - (int) (score / 20) * 10; // Reduce by 10px every 20 points
        
        // Ensure a reasonable minimum opening space
        openingSpace = Math.max(openingSpace, 150); // Minimum opening space of 150px
        
        // Random vertical position for the top pipe
        int randomPipeY = (int) (pipeY - PIPE_HEIGHT / 4 - Math.random() * (PIPE_HEIGHT / 2));
        
        // Create top pipe
        Pipe topPipe = new Pipe(topPipeImg);
        topPipe.x = BOARD_WIDTH; // Start at the right edge
        topPipe.y = randomPipeY;
        pipes.add(topPipe);
        
        // Create bottom pipe
        Pipe bottomPipe = new Pipe(bottomPipeImg);
        bottomPipe.x = BOARD_WIDTH; // Start at the right edge
        bottomPipe.y = topPipe.y + PIPE_HEIGHT + openingSpace;
        pipes.add(bottomPipe);
    }
      

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        draw(g);
    }

    private void draw(Graphics g) {
        // Drawing background, pipes, bird, and score
        g.drawImage(backgroundImg, 0, 0, BOARD_WIDTH, BOARD_HEIGHT, null);
        pipes.forEach(pipe -> g.drawImage(pipe.img, pipe.x, pipe.y, pipe.width, pipe.height, null));
        g.drawImage(bird.img, bird.x, bird.y, BIRD_WIDTH, BIRD_HEIGHT, null);

        g.setColor(Color.WHITE);
        g.setFont(new Font("Inter", Font.BOLD, 32));

        // If game over, display game over message
        if (gameOver) {
            drawGameOver(g);
        } else {
            drawScore(g);
        }

        // If game hasn't started, show start message
        if (!gameStarted && showMessage) {
            showStartMessage(g);
        }
    }

    private void drawGameOver(Graphics g) {
        String gameOverMessage = "Game Over: " + (int) score;
        FontMetrics fm = g.getFontMetrics();
        int x = (BOARD_WIDTH - fm.stringWidth(gameOverMessage)) / 2;
        int y = 50;
        g.drawString(gameOverMessage, x, y);
    
        // Initialize the "Add to Leaderboard" button if it's not already initialized
        if (addToLeaderboardButton == null) {
            addLeaderboardButton();
        }
    
        String retryMessage = "Press Q to Restart";
        fm = g.getFontMetrics();
        x = (BOARD_WIDTH - fm.stringWidth(retryMessage)) / 2;
        y = BOARD_HEIGHT / 2;
        g.drawString(retryMessage, x, y);
    }
    
    private void addLeaderboardButton() {
        if (addToLeaderboardButton == null) {
            addToLeaderboardButton = new JButton("ADD TO LEADERBOARD");
    
            // Set button size and position
            int buttonWidth = 270, buttonHeight = 50;
            int buttonX = (BOARD_WIDTH - buttonWidth) / 2;
            int buttonY = BOARD_HEIGHT - 80;
            addToLeaderboardButton.setBounds(buttonX, buttonY, buttonWidth, buttonHeight);
    
            // Set default and hover colors
            Color defaultColor = new Color(76, 175, 80); // Green color
            Color hoverColor = new Color(114, 237, 118); // RGB hover color
    
            addToLeaderboardButton.setBackground(defaultColor);
            addToLeaderboardButton.setForeground(Color.WHITE); // Text color
            addToLeaderboardButton.setFocusPainted(false); // Remove focus border
            addToLeaderboardButton.setBorderPainted(false); // No border for button
            addToLeaderboardButton.setFont(new Font("Inter", Font.BOLD, 18));
    
            // Disable the default press effect
            addToLeaderboardButton.setUI(new javax.swing.plaf.basic.BasicButtonUI() {
                @Override
                protected void paintButtonPressed(Graphics g, AbstractButton b) {
                    // Do nothing to override the default pressed effect
                }
            });
    
            // Add hover effect using MouseListener
            addToLeaderboardButton.addMouseListener(new java.awt.event.MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) {
                    addToLeaderboardButton.setBackground(hoverColor);
                    playSound("/resources/sfx/menuhoverclick.wav"); // Play hover sound
                }
    
                @Override
                public void mouseExited(MouseEvent e) {
                    addToLeaderboardButton.setBackground(defaultColor);
                }
    
                @Override
                public void mouseClicked(MouseEvent e) {
                    playSound("/resources/sfx/menuclick.wav"); // Play click sound
                }
            });
    
            // Add action listener for button click
            addToLeaderboardButton.addActionListener(e -> {
                playSound("/resources/sfx/menuclick.wav"); // Ensure click sound is also played here
                showLeaderboard();
            });
    
            // Add the button to the panel
            add(addToLeaderboardButton);
            revalidate(); // Revalidate to update the layout
            repaint(); // Repaint to make sure the button is drawn on the screen
        }
    }
    

    private void drawScore(Graphics g) {
        // Drawing the current score on the screen
        String scoreText = String.valueOf((int) score);
        FontMetrics fm = g.getFontMetrics();
        int x = (BOARD_WIDTH - fm.stringWidth(scoreText)) / 2;
        int y = 50;
        g.drawString(scoreText, x, y);
    }

    private void showStartMessage(Graphics g) {
        // Display start message "Press Spacebar"
        String message = "Press Spacebar";
        FontMetrics fm = g.getFontMetrics();
        int x = (BOARD_WIDTH - fm.stringWidth(message)) / 2;
        int y = BOARD_HEIGHT / 2;
        g.setColor(Color.WHITE);
        g.setFont(new Font("Inter", Font.BOLD, 30));
        g.drawString(message, x, y);
    }

    private void showLeaderboard() {
        // Display the leaderboard screen after the game ends
        Leaderboard leaderboardPanel = new Leaderboard(score);
        JFrame frame = (JFrame) SwingUtilities.getWindowAncestor(this);
        frame.getContentPane().removeAll();
        frame.getContentPane().add(leaderboardPanel);
        frame.revalidate();
        frame.repaint();
    }

    private void move() {
        // Game mechanics to move the bird and check for collisions
        if (!gameStarted || gameOver) return;

        velocityY += gravity;
        bird.y += velocityY;
        bird.y = Math.max(bird.y, 0);

        for (Pipe pipe : pipes) {
            pipe.x += velocityX;

            // If bird passes a pipe, increment score
            if (!pipe.passed && bird.x > pipe.x + pipe.width) {
                score += 0.5;
                playPassPipeSound();
                pipe.passed = true;
            }

            // Check for collision with pipes
            if (checkCollision(bird, pipe)) {
                gameOver = true;
                velocityY = 5;
                playGameOverSound();
            }
        }

        // If bird falls below screen, game over
        if (bird.y > BOARD_HEIGHT) {
            gameOver = true;
            velocityY = 0;
            playGameOverSound();
        }

        // Handle gravity after game over
        if (gameOver) {
            velocityY += gravity;
            bird.y += velocityY;
        }
    }

    private boolean checkCollision(Bird bird, Pipe pipe) {
        // Check if the bird collides with the pipe
        return bird.x < pipe.x + pipe.width && bird.x + bird.width > pipe.x &&
               bird.y < pipe.y + pipe.height && bird.y + bird.height > pipe.y;
    }

    private void playSound(String filePath) {
        // Utility method to play sound effects
        try {
            AudioInputStream audioIn = AudioSystem.getAudioInputStream(getClass().getResource(filePath));
            Clip clip = AudioSystem.getClip();
            clip.open(audioIn);
            clip.start();
        } catch (Exception e) {
            System.err.println("Error playing sound: " + e.getMessage());
        }
    }

    private void playJumpSound() {
        // Play jump sound effect
        playSound("/resources/sfx/jump.wav");
    }

    private void playPassPipeSound() {
        // Play sound when bird passes a pipe
        playSound("/resources/sfx/passpipe.wav");
    }

    private void playGameOverSound() {
        // Play game over sound and stop background music
        playSound("/resources/sfx/gameover.wav");
        stopBackgroundMusic();
    }

    private void playBackgroundMusic() {
        // Play background music in a loop
        try {
            if (backgroundMusic == null || !backgroundMusic.isRunning()) {
                AudioInputStream audioIn = AudioSystem.getAudioInputStream(getClass().getResource("/resources/sfx/backgroundmusic.wav"));
                backgroundMusic = AudioSystem.getClip();
                backgroundMusic.open(audioIn);
                backgroundMusic.loop(Clip.LOOP_CONTINUOUSLY);
                backgroundMusic.start();
            }
        } catch (Exception e) {
            System.err.println("Error playing background music: " + e.getMessage());
        }
    }

    private void stopBackgroundMusic() {
        // Stop background music when game over
        if (backgroundMusic != null && backgroundMusic.isRunning()) {
            backgroundMusic.stop();
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        move();
        repaint();
    
        if (gameOver) {
            // Only stop the timers after the game is over
            pipeGenerator.stop();
            gameLoop.stop();
    
            // Initialize the leaderboard button if it's null
            if (addToLeaderboardButton == null) {
                addLeaderboardButton();
            }
    
            // Make the leaderboard button visible
            addToLeaderboardButton.setVisible(true);
        }
    }

    @Override
    public void keyPressed(KeyEvent e) {
        // Handle key press events (spacebar to jump, Q to restart)
        if (e.getKeyCode() == KeyEvent.VK_Q && gameOver) {
            resetGame();
        } else if (e.getKeyCode() == KeyEvent.VK_SPACE) {
            if (!gameStarted) {
                gameStarted = true;
                playBackgroundMusic();
                flickerMessageTimer.stop();
            } else if (!isJumping) {
                velocityY = -9;
                playJumpSound();
                isJumping = true;
            }
        }
    }

    @Override
    public void keyTyped(KeyEvent e) {}

    @Override
    public void keyReleased(KeyEvent e) {
        // Stop jumping when spacebar is released
        if (e.getKeyCode() == KeyEvent.VK_SPACE) {
            isJumping = false;
        }
    }

    private void resetGame() {
        // Reset game to initial state
        bird.y = BOARD_HEIGHT / 2;
        velocityY = 0;
        pipes.clear();
        score = 0;
        gameOver = false;
        gameLoop.start();
        pipeGenerator.start();
        playBackgroundMusic();
        flickerMessageTimer.start();

        if (addToLeaderboardButton != null) {
            remove(addToLeaderboardButton);
            addToLeaderboardButton = null;
        }
        revalidate();
        repaint();
    }

    private class Bird {
        int x = birdX, y = birdY;
        int width = BIRD_WIDTH, height = BIRD_HEIGHT;
        Image img;

        Bird(Image img) {
            this.img = img;
        }
    }

    private class Pipe {
        int x = pipeX, y = pipeY;
        int width = PIPE_WIDTH, height = PIPE_HEIGHT;
        boolean passed = false;
        Image img;

        Pipe(Image img) {
            this.img = img;
        }
    }
}
