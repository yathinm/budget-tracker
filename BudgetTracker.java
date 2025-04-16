import java.awt.*;
import java.util.*;
import javax.swing.*;
import javax.swing.table.*;

public class BudgetTracker {
    private JFrame frame;
    private JTextField amountField;
    private JComboBox<String> categoryBox;
    private JLabel balanceLabel;
    private JTable transactionTable;
    private DefaultTableModel tableModel;
    private JTextArea summaryArea;
    private double balance = 0.0;
    private final double MIN_SPENDING_THRESHOLD = 50.0;

    private final Map<String, Double> categoryTotals = new HashMap<>();

    private final String[] categories = {
        "Income",
        "Rent",
        "Groceries",
        "Utilities",
        "Transportation",
        "Entertainment",
        "Healthcare",
        "Other"
    };

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new BudgetTracker().showLoginScreen());
    }

    private void showLoginScreen() {
        JFrame loginFrame = new JFrame("Login");
        loginFrame.setSize(300, 150);
        loginFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        loginFrame.setLayout(new GridLayout(3, 2, 5, 5));

        JTextField userField = new JTextField();
        JPasswordField passField = new JPasswordField();

        loginFrame.add(new JLabel("Username:"));
        loginFrame.add(userField);
        loginFrame.add(new JLabel("Password:"));
        loginFrame.add(passField);

        JButton loginButton = new JButton("Login");
        loginFrame.add(new JLabel(""));
        loginFrame.add(loginButton);

        loginButton.addActionListener(e -> {
            String user = userField.getText();
            String pass = new String(passField.getPassword());
            if (user.equals("admin") && pass.equals("1234")) {
                loginFrame.dispose();
                createMainUI();
            } else {
                JOptionPane.showMessageDialog(loginFrame, "Invalid credentials");
            }
        });

        loginFrame.setLocationRelativeTo(null);
        loginFrame.setVisible(true);
    }

    private void createMainUI() {
        frame = new JFrame("Personal Budget Tracker");
        frame.setSize(850, 550);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JTabbedPane tabbedPane = new JTabbedPane();

        // Add Transaction Panel
        JPanel addPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        amountField = new JTextField(10);
        categoryBox = new JComboBox<>(categories);
        JButton addButton = new JButton("Add Transaction");

        gbc.gridx = 0; gbc.gridy = 0;
        addPanel.add(new JLabel("Enter Amount ($):"), gbc);
        gbc.gridx = 1;
        addPanel.add(amountField, gbc);

        gbc.gridx = 0; gbc.gridy = 1;
        addPanel.add(new JLabel("Select Category:"), gbc);
        gbc.gridx = 1;
        addPanel.add(categoryBox, gbc);

        gbc.gridx = 0; gbc.gridy = 2; gbc.gridwidth = 2;
        addPanel.add(addButton, gbc);

        balanceLabel = new JLabel("Current Balance: $0.00");
        balanceLabel.setFont(new Font("Arial", Font.BOLD, 16));
        JPanel balancePanel = new JPanel();
        balancePanel.add(balanceLabel);
        gbc.gridy = 3;
        addPanel.add(balancePanel, gbc);

        JButton logoutButton = new JButton("Log Out");
        logoutButton.addActionListener(e -> {
            frame.dispose();
            showLoginScreen();
        });
        gbc.gridy = 4;
        addPanel.add(logoutButton, gbc);

        addButton.addActionListener(e -> handleTransaction());

        // Log Panel with JTable
        JPanel logPanel = new JPanel(new BorderLayout());

        String[] columnNames = {"Type", "Amount", "Category"};
        tableModel = new DefaultTableModel(columnNames, 0);
        transactionTable = new JTable(tableModel);
        transactionTable.setAutoCreateRowSorter(true); // Enable sorting
        JScrollPane tableScroll = new JScrollPane(transactionTable);

        summaryArea = new JTextArea(5, 50);
        summaryArea.setEditable(false);
        JScrollPane summaryScroll = new JScrollPane(summaryArea);

        logPanel.add(tableScroll, BorderLayout.CENTER);
        logPanel.add(summaryScroll, BorderLayout.SOUTH);

        // Add tabs
        tabbedPane.addTab("Add Transaction", addPanel);
        tabbedPane.addTab("Transaction Log", logPanel);

        frame.add(tabbedPane);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    private void handleTransaction() {
        try {
            double amount = Double.parseDouble(amountField.getText());
            if (amount <= 0) throw new NumberFormatException();

            String category = (String) categoryBox.getSelectedItem();

            if (!category.equals("Income") && balance < MIN_SPENDING_THRESHOLD) {
                JOptionPane.showMessageDialog(frame, "Balance too low. Add more income to continue spending.");
                return;
            }

            String type;
            if (category.equals("Income")) {
                type = "Income";
            } else {
                type = "Spent";
                amount = -amount;
            }

            balance += amount;
            updateBalance();

            double actualAmount = Math.abs(amount);
            tableModel.addRow(new Object[]{type, String.format("%.2f", actualAmount), category});

            categoryTotals.put(category, categoryTotals.getOrDefault(category, 0.0) + actualAmount);
            updateCategorySummary();

            amountField.setText("");
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(frame, "Please enter a valid positive number.");
        }
    }

    private void updateBalance() {
        balanceLabel.setText(String.format("Current Balance: $%.2f", balance));
    }

    private void updateCategorySummary() {
        StringBuilder sb = new StringBuilder();
        sb.append("Total per category:\n");
        for (String cat : categories) {
            if (!cat.equals("Income")) {
                double total = categoryTotals.getOrDefault(cat, 0.0);
                sb.append(String.format("%-15s: $%.2f%n", cat, total));
            }
        }
        summaryArea.setText(sb.toString());
    }
}
