package com.flappybird;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;

public class App {
    public static void main(String[] args) {
        // Game window dimensions
        final int boardWidth = 800; // Replace with FlappyBird.BOARD_WIDTH if defined
        final int boardHeight = 600; // Replace with FlappyBird.BOARD_HEIGHT if defined

        // Create a JFrame for the game window
        JFrame frame = new JFrame("Flappy Bird KSC6483");
        frame.setSize(boardWidth, boardHeight);
        frame.setLocationRelativeTo(null); // Center window on screen
        frame.setResizable(false); // Prevent resizing
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // Load and set custom cursor
        setCustomCursor(frame);

        // Add the menu panel
        MenuPanel menuPanel = new MenuPanel(frame);
        frame.add(menuPanel);

        // Pack the components within the frame
        frame.pack();

        // Make the frame visible
        frame.setVisible(true);
    }

    private static void setCustomCursor(JFrame frame) {
        try {
            // Use ClassLoader to load the image from the classpath
            Image cursorImage = new ImageIcon(App.class.getResource("/resources/cursor.png")).getImage();
    
            // Check if image was loaded
            if (cursorImage == null) {
                throw new IOException("Image could not be loaded.");
            }

            // Resize the image to match the default cursor size (32x32)
            Image scaledCursorImage = cursorImage.getScaledInstance(32, 32, Image.SCALE_SMOOTH);

            // Create custom cursor
            Point hotspot = new Point(0, 0); // Top-left corner of the cursor image
            Cursor customCursor = Toolkit.getDefaultToolkit().createCustomCursor(scaledCursorImage, hotspot, "Custom Cursor");
            
            // Apply custom cursor
            frame.setCursor(customCursor);
        } catch (Exception e) {
            System.err.println("Failed to load custom cursor image: " + e.getMessage());
            // Fallback to default cursor
            frame.setCursor(Cursor.getDefaultCursor());
        }
    }
}
