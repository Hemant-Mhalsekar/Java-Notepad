import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.io.*;
import java.util.Date;

public class Notepad extends JFrame {
    private JTextArea textArea;
    private File currentFile;
    private boolean textChanged = false;
    private Timer autoSaveTimer;
    private JLabel statusBar;
    private boolean isDarkMode = false;

    // Constructor to set up the UI components
    public Notepad() {
        setTitle("Java Notepad - Untitled");
        setSize(800, 600);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        // Create text area for editing
        textArea = new JTextArea();
        textArea.setFont(new Font("Consolas", Font.PLAIN, 16));
        textArea.setLineWrap(true);
        textArea.setWrapStyleWord(true);
        textArea.setMargin(new Insets(10, 10, 10, 10));

        // Create scrollable area for text
        JScrollPane scrollPane = new JScrollPane(textArea);
        add(scrollPane, BorderLayout.CENTER);

        // Create and configure the status bar at the bottom
        statusBar = new JLabel("Ready");
        statusBar.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        add(statusBar, BorderLayout.SOUTH);

        setupMenuBar();
        setupAutoSave();
        applyLightMode(); // default

        // Add document listener to track text changes
        textArea.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { textChanged = true; updateStatusBar(); }
            public void removeUpdate(DocumentEvent e) { textChanged = true; updateStatusBar(); }
            public void changedUpdate(DocumentEvent e) { textChanged = true; updateStatusBar(); }
        });

        setVisible(true);
    }

    // Method to set up the menu bar with options
    private void setupMenuBar() {
        JMenuBar menuBar = new JMenuBar();

        JMenu fileMenu = new JMenu("File");
        JMenu viewMenu = new JMenu("View");

        // Menu items for "File" menu
        JMenuItem newItem = new JMenuItem("New");
        JMenuItem openItem = new JMenuItem("Open");
        JMenuItem saveItem = new JMenuItem("Save");
        JMenuItem exitItem = new JMenuItem("Exit");
        JMenuItem darkModeToggle = new JMenuItem("Toggle Dark Mode");

        // Set keyboard accelerators for menu items
        newItem.setAccelerator(KeyStroke.getKeyStroke("control N"));
        openItem.setAccelerator(KeyStroke.getKeyStroke("control O"));
        saveItem.setAccelerator(KeyStroke.getKeyStroke("control S"));
        exitItem.setAccelerator(KeyStroke.getKeyStroke("control Q"));

        // Set keyboard accelerators for menu items
        newItem.addActionListener(e -> newFile());
        openItem.addActionListener(e -> openFile());
        saveItem.addActionListener(e -> saveFile());
        exitItem.addActionListener(e -> System.exit(0));
        darkModeToggle.addActionListener(e -> toggleDarkMode());

        // Add items to file menu
        fileMenu.add(newItem);
        fileMenu.add(openItem);
        fileMenu.add(saveItem);
        fileMenu.addSeparator();
        fileMenu.add(exitItem);

        viewMenu.add(darkModeToggle);

        // Add dark mode toggle to view menu
        menuBar.add(fileMenu);
        menuBar.add(viewMenu);
        setJMenuBar(menuBar);
    }

    // Method to create a new file (clear current text)
    private void newFile() {
        textArea.setText("");
        currentFile = null;
        textChanged = false;
        setTitle("Java Notepad - Untitled");
        statusBar.setText("New file created");
    }

    // Method to open an existing file
    private void openFile() {
        JFileChooser fileChooser = new JFileChooser();
        if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            currentFile = fileChooser.getSelectedFile();
            try (BufferedReader reader = new BufferedReader(new FileReader(currentFile))) {
                textArea.read(reader, null);
                textChanged = false;
                setTitle("Java Notepad - " + currentFile.getName());
                statusBar.setText("Opened: " + currentFile.getAbsolutePath());
            } catch (IOException e) {
                showError("Could not open the file.");
            }
        }
    }

    // Method to save the current file
    private void saveFile() {
        if (currentFile == null) {
            JFileChooser fileChooser = new JFileChooser();
            if (fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
                currentFile = fileChooser.getSelectedFile();
            }
        }

        if (currentFile != null) {
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(currentFile))) {
                textArea.write(writer);
                textChanged = false;
                setTitle("Java Notepad - " + currentFile.getName());
                statusBar.setText("Saved: " + currentFile.getAbsolutePath());
            } catch (IOException e) {
                showError("Could not save file.");
            }
        }
    }

    // Method to set up the auto-save functionality (every 10 seconds)
    private void setupAutoSave() {
        int interval = 10000; // 10 seconds
        autoSaveTimer = new Timer(interval, e -> {
            if (textChanged && currentFile != null) {
                try (BufferedWriter writer = new BufferedWriter(new FileWriter(currentFile))) {
                    textArea.write(writer);
                    textChanged = false;
                    Date now = new Date();
                    statusBar.setText("Auto-saved at: " + now.toString());
                    setTitle("Java Notepad - " + currentFile.getName() + " (Auto-saved)");
                } catch (IOException ex) {
                    System.err.println("Auto-save failed.");
                }
            }
        });
        autoSaveTimer.start();
    }

    // Method to update the status bar with word and character count
    private void updateStatusBar() {
        int chars = textArea.getText().length();
        int words = textArea.getText().trim().isEmpty() ? 0 : textArea.getText().trim().split("\\s+").length;
        statusBar.setText("Words: " + words + " | Characters: " + chars);
    }

    // Method to toggle between dark and light modes
    private void toggleDarkMode() {
        if (isDarkMode) {
            applyLightMode();
        } else {
            applyDarkMode();
        }
        isDarkMode = !isDarkMode;
    }

    // Method to apply dark mode theme
    private void applyDarkMode() {
        Color darkBg = new Color(40, 44, 52);
        Color darkFg = new Color(187, 187, 187);

        textArea.setBackground(darkBg);
        textArea.setForeground(darkFg);
        textArea.setCaretColor(Color.WHITE);

    }

    // Method to apply light mode theme
    private void applyLightMode() {
        textArea.setBackground(Color.WHITE);
        textArea.setForeground(Color.BLACK);
        textArea.setCaretColor(Color.BLACK);
        statusBar.setBackground(null);
        statusBar.setForeground(Color.BLACK);
    }

    // Method to display an error message
    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Error", JOptionPane.ERROR_MESSAGE);
    }
}
