package com.flappybird;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import javax.sound.sampled.*;
import java.io.File;
import java.io.IOException;
import com.flappybird.database.DatabaseConnection;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableColumnModel;

import java.sql.ResultSet;
import java.sql.Statement;

public class MenuLeaderboard extends JPanel {
    private Image backgroundImage;
    private JLabel titleLabel;
    private JLabel backButton;
    private JTextField placeholderField;
    private JLabel errorLabel;
    private JButton submitButton;
    private JTable leaderboardTable;

    public MenuLeaderboard(JFrame frame) {
        setPreferredSize(new Dimension(FlappyBird.BOARD_WIDTH, FlappyBird.BOARD_HEIGHT));
        // Initialize components for the leaderboard
        loadBackgroundImage();
        initializeUI();
    }

    // Load the background image with error handling
    private void loadBackgroundImage() {
        try {
            backgroundImage = new ImageIcon(getClass().getResource("/resources/leaderboardbg.png")).getImage();
        } catch (Exception e) {
            System.err.println("Error loading background image: " + e.getMessage());
        }
    }

    // Initialize the user interface components
    private void initializeUI() {
        setLayout(null);
    
        titleLabel = createTitleLabel();
        backButton = createBackButton();
        placeholderField = createPlaceholderField();
        errorLabel = createErrorLabel();
        submitButton = createFilterButton();
        JScrollPane tableScrollPane = createLeaderboardTable();
        JPanel panel = new JPanel();
    
        // Only add the components that are needed
        addComponentsToPanel(titleLabel, backButton, panel, placeholderField, errorLabel, submitButton);
        add(tableScrollPane);
    
        // Add component listener to handle dynamic resizing
        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                adjustLayout();
            }
        });
    
        // Start the timer for auto-refresh (refresh every 5 seconds)
        Timer timer = new Timer(5000, e -> loadLeaderboardData());
        timer.start();
    
        // Initial layout adjustment
        adjustLayout();
    }
    

    // Remove the createScorePanel method
    // (you no longer need to create or use the score panel)


    // Create the leaderboard table method
    private JScrollPane createLeaderboardTable() {
        leaderboardTable = new JTable(new DefaultTableModel(new Object[]{"No", "Name", "Score"}, 0));
        leaderboardTable.setFillsViewportHeight(true);
        leaderboardTable.setEnabled(false); // Make it read-only

        // Set table font and row height
        leaderboardTable.setFont(new Font("Inter", Font.PLAIN, 15));
        leaderboardTable.setRowHeight(30);

        // Customize table header
        JTableHeader tableHeader = leaderboardTable.getTableHeader();
        tableHeader.setFont(new Font("Inter", Font.BOLD, 18));
        tableHeader.setBackground(new Color(76, 175, 80));
        tableHeader.setForeground(Color.WHITE);

        // Adjust header height
        tableHeader.setPreferredSize(new Dimension(tableHeader.getPreferredSize().width, 30)); // Set header height to 40px

        // Custom header renderer for alignment and padding
        DefaultTableCellRenderer headerRenderer = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                JLabel headerLabel = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                headerLabel.setFont(new Font("Inter", Font.BOLD, 15)); // Bold font for headers
                headerLabel.setHorizontalAlignment(column == 1 ? SwingConstants.LEFT : SwingConstants.CENTER);
                if (column == 1) {
                    headerLabel.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 0)); // Add padding to Name header
                }
                headerLabel.setBackground(new Color(76, 175, 80));
                headerLabel.setForeground(Color.WHITE);
                headerLabel.setOpaque(true);
                return headerLabel;
            }
        };

        // Apply header renderer to all columns
        for (int i = 0; i < leaderboardTable.getColumnModel().getColumnCount(); i++) {
            leaderboardTable.getColumnModel().getColumn(i).setHeaderRenderer(headerRenderer);
        }

        // Set column widths
        TableColumnModel columnModel = leaderboardTable.getColumnModel();
        columnModel.getColumn(0).setPreferredWidth(50);  // Rank
        columnModel.getColumn(1).setPreferredWidth(200); // Name
        columnModel.getColumn(2).setPreferredWidth(90); // Score

        // Custom renderer for Name column values with padding
        DefaultTableCellRenderer leftPaddingRenderer = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                if (c instanceof JLabel) {
                    JLabel label = (JLabel) c;
                    label.setHorizontalAlignment(SwingConstants.LEFT);
                    label.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 0)); // Add 10px padding on the left
                }
                return c;
            }
        };

        // Custom renderer for centered alignment (Rank and Score)
        DefaultTableCellRenderer centerAlignRenderer = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                if (c instanceof JLabel) {
                    JLabel label = (JLabel) c;
                    label.setHorizontalAlignment(SwingConstants.CENTER);
                }
                return c;
            }
        };

        // Apply renderers to columns
        leaderboardTable.getColumnModel().getColumn(0).setCellRenderer(centerAlignRenderer); // Center-align Rank
        leaderboardTable.getColumnModel().getColumn(1).setCellRenderer(leftPaddingRenderer); // Left-align Name with padding
        leaderboardTable.getColumnModel().getColumn(2).setCellRenderer(centerAlignRenderer); // Center-align Score

        // Fetch data from the database and populate the table
        loadLeaderboardData();

        // Style the table grid
        leaderboardTable.setShowHorizontalLines(true);
        leaderboardTable.setShowVerticalLines(true);
        leaderboardTable.setGridColor(Color.LIGHT_GRAY); // Set grid line color

        // Remove outer border and set spacing between cells
        leaderboardTable.setIntercellSpacing(new Dimension(1, 1)); // Space between cells
        leaderboardTable.setBorder(BorderFactory.createEmptyBorder()); // Remove outer border

        // Create and style the scroll pane
        JScrollPane scrollPane = new JScrollPane(leaderboardTable);
        scrollPane.setBounds(30, 190, getWidth() - 60, 300); // Position and size of the table
        scrollPane.getViewport().setBackground(Color.WHITE); // Background color for the table area
        scrollPane.setBorder(BorderFactory.createEmptyBorder()); // Remove the scroll pane border
        return scrollPane;
    }
    
    // Load data from the database into the table
    private void loadLeaderboardData() {
        DefaultTableModel model = (DefaultTableModel) leaderboardTable.getModel();
        model.setRowCount(0); // Clear existing rows
    
        DatabaseConnection dbConnection = new DatabaseConnection();
        Connection conn = dbConnection.connect();
        if (conn != null) {
            try {
                String query = "SELECT ROW_NUMBER() OVER (ORDER BY score DESC) AS rank, name, score FROM leaderboard";
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(query);
    
                while (rs.next()) {
                    int rank = rs.getInt("rank");
                    String name = rs.getString("name");
                    int score = rs.getInt("score");
                    model.addRow(new Object[]{rank, name, score});
                }
    
                rs.close();
                stmt.close();
            } catch (SQLException ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(this, "Error loading leaderboard data.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    

    // Create and return the title label
    private JLabel createTitleLabel() {
        JLabel label = new JLabel("Leaderboard", SwingConstants.CENTER);
        label.setFont(new Font("Inter", Font.BOLD, 30));
        label.setForeground(Color.WHITE);
        return label;
    }

    // Create and return the back button
    private JLabel createBackButton() {
        JLabel label = new JLabel("<", SwingConstants.CENTER);
        label.setFont(new Font("Inter", Font.PLAIN, 30));
        label.setForeground(Color.WHITE);
        label.setBounds(15, 20, 40, 40);

        label.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                label.setForeground(Color.RED);
                playSound("src/resources/sfx/menuhoverclick.wav");
            }

            @Override
            public void mouseExited(MouseEvent e) {
                label.setForeground(Color.WHITE);
            }

            @Override
            public void mouseClicked(MouseEvent e) {
                playSound("src/resources/sfx/back.wav");
                backToPreviousClass();
            }
        });
        return label;
    }
    

// Create and return the filter button
private JButton createFilterButton() {
    JButton button = new JButton("Filter");
    button.setFont(new Font("Inter", Font.BOLD, 15));
    button.setBackground(new Color(76, 175, 80));
    button.setForeground(Color.WHITE);
    button.setFocusPainted(false);
    button.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));

    button.addActionListener(e -> {
        String filterCriteria = placeholderField.getText().trim();
        if (filterCriteria.isEmpty() || filterCriteria.equals("Enter name to filter...")) {
            JOptionPane.showMessageDialog(this, "Please enter a name to filter.", "Error", JOptionPane.ERROR_MESSAGE);
        } else {
            filterLeaderboard(filterCriteria);
        }
    });

    return button;
}

// Filter leaderboard based on player name
private void filterLeaderboard(String filterCriteria) {
    DefaultTableModel model = (DefaultTableModel) leaderboardTable.getModel();
    model.setRowCount(0);

    try (Connection conn = new DatabaseConnection().connect()) {
        String query = "SELECT ROW_NUMBER() OVER (ORDER BY score DESC) AS rank, name, score FROM leaderboard WHERE name LIKE ?";
        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, "%" + filterCriteria + "%");
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    int rank = rs.getInt("rank");
                    String name = rs.getString("name");
                    int score = rs.getInt("score");
                    model.addRow(new Object[]{rank, name, score});
                }
            }
        }
    } catch (SQLException ex) {
        ex.printStackTrace();
        JOptionPane.showMessageDialog(this, "Error filtering leaderboard data.", "Error", JOptionPane.ERROR_MESSAGE);
    }
}


    // Create and return the placeholder text field
    private JTextField createPlaceholderField() {
        JTextField field = new JTextField("Find your name...");
        field.setBackground(Color.WHITE);
        field.setForeground(Color.GRAY);
        field.setFont(new Font("Inter", Font.PLAIN, 15));
        field.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));

        field.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                if (field.getText().equals("Find your name...")) {
                    field.setText("");
                    field.setForeground(Color.BLACK);
                }
            }

            @Override
            public void focusLost(FocusEvent e) {
                if (field.getText().isEmpty()) {
                    field.setText("What is your name?");
                    field.setForeground(Color.GRAY);
                }
            }
        });

        field.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            @Override
            public void insertUpdate(javax.swing.event.DocumentEvent e) {
                enforceCharacterLimit();
            }

            @Override
            public void removeUpdate(javax.swing.event.DocumentEvent e) {
                enforceCharacterLimit();
            }

            @Override
            public void changedUpdate(javax.swing.event.DocumentEvent e) {
                enforceCharacterLimit();
            }
        });

        return field;
    }

    // Create and return the error label
    private JLabel createErrorLabel() {
        JLabel label = new JLabel();
        label.setForeground(Color.RED);
        label.setFont(new Font("Inter", Font.PLAIN, 12));
        label.setVisible(false);
        return label;
    }

    // Ensure the character limit is enforced
    private void enforceCharacterLimit() {
        String text = placeholderField.getText().trim(); // Trim any leading/trailing spaces
        if (text.isEmpty() || text.equals("What is your name?")) {
            errorLabel.setText("Please enter your name.");
            errorLabel.setVisible(true);
            updateSubmitButtonState(false);
        } else if (text.length() < 4) {
            errorLabel.setText("Your name is too short!");
            errorLabel.setVisible(true);
            updateSubmitButtonState(false);
        } else if (text.length() > 16) {
            errorLabel.setText("Your name is too long!");
            errorLabel.setVisible(true);
            updateSubmitButtonState(false);
        } else {
            errorLabel.setVisible(false);
            updateSubmitButtonState(true);
        }
    }

    // Update the state of the submit button based on validity
    private void updateSubmitButtonState(boolean isValid) {
        submitButton.setEnabled(isValid);
        submitButton.setBackground(isValid ? new Color(76, 175, 80) : Color.RED);
    }

    private void adjustLayout() {
        // Position the title at (30, 70)
        int titleWidth = titleLabel.getPreferredSize().width;
        int titleHeight = titleLabel.getPreferredSize().height;
        titleLabel.setBounds(30, 70, titleWidth, titleHeight);  // Set title to (30, 70)
        
        // Position back button at the top-right corner
        int backButtonWidth = backButton.getPreferredSize().width;
        backButton.setBounds(getWidth() - backButtonWidth - 35, 20, 40, 40);  // Position back button at top-right
        
        // Position the placeholder at (30, 110)
         // Set a fixed width for the placeholder field (adjust as needed)
        int placeholderHeight = placeholderField.getPreferredSize().height;
        placeholderField.setBounds(30, 113, 290, placeholderHeight);  // Set placeholder to (30, 110)
        
        // Position the error label below the placeholder
        errorLabel.setBounds(40, 150, getWidth() - 60, 20);  // Place error label below placeholder field
        
        // Position the submit button below the error label
        submitButton.setBounds(230, 150, 90, 28);  // Position submit button below error label

        JScrollPane scrollPane = (JScrollPane) getComponent(getComponentCount() - 1); // Assuming last component is the table
        scrollPane.setBounds(30, 220, 290, getHeight() - 250);
    }
    
    // Add components to the panel
    private void addComponentsToPanel(JLabel title, JLabel back, JPanel scorePanel, JTextField placeholder, JLabel error, JButton submit) {
        add(title);
        add(back);
        add(scorePanel);
        add(placeholder);
        add(error);
        add(submit);
    }

    // Paint background image
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (backgroundImage != null) {
            g.drawImage(backgroundImage, 0, 0, getWidth(), getHeight(), this);
        }
    }
    
    

    // Navigate back to the previous class
    private void backToPreviousClass() {
        JFrame frame = (JFrame) SwingUtilities.getWindowAncestor(this);
        frame.getContentPane().removeAll();
        frame.getContentPane().add(new MenuPanel(frame));  // Adjust class name accordingly
        frame.revalidate();
        frame.repaint();
    }

    // Play sound
    private void playSound(String soundFile) {
        try {
            File sound = new File(soundFile);
            AudioInputStream audioIn = AudioSystem.getAudioInputStream(sound);
            Clip clip = AudioSystem.getClip();
            clip.open(audioIn);
            clip.start();
        } catch (IOException | UnsupportedAudioFileException | LineUnavailableException e) {
            System.err.println("Error playing sound: " + e.getMessage());
        }
    }
}