package UserDSB;

import AdminDSB.AdminDashboard;
import AdminDSB.myAccount;
import Config.DBConnector;
import Config.Session;
import Config.passwordHashing;
import LoginDSB.*;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.NoSuchAlgorithmException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.SpinnerNumberModel;
import javax.swing.table.*;
import static javax.xml.bind.DatatypeConverter.parseInteger;
import net.proteanit.sql.DbUtils;

public class UserDashboard extends javax.swing.JFrame {

    public File selectedFile;
    public String path2 = null;
    public String destination = "";
    public String oldPath;
    public String path;
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$");
    private static final Pattern CONTACT_PATTERN = Pattern.compile("^[0-9]{11}$");

    public UserDashboard() {
        initComponents();
        displayProducts();
    }

    private void displayProducts() {
        try {
            ResultSet rs = new DBConnector().getData("select p_id, p_name, p_price, p_stocks, p_status from products where p_status = 'AVAILABLE'");
            products.setModel(DbUtils.resultSetToTableModel(rs));
        } catch (SQLException e) {
            System.err.println("An error occurred while fetching data: " + e.getMessage());
        }
    }

    private String xemail, xusername;

    private boolean updateChecker() throws SQLException {
        ResultSet rs = new DBConnector().getData("select * from users where (username = '" + username.getText() + "' or email = '" + email.getText() + "') and id != '" + id2.getText() + "'");
        if (rs.next()) {
            xemail = rs.getString("email");
            if (xemail.equalsIgnoreCase(email.getText())) {
                JOptionPane.showMessageDialog(null, "EMAIL HAS BEEN USED!");
            }
            xusername = rs.getString("username");
            if (xusername.equalsIgnoreCase(username.getText())) {
                JOptionPane.showMessageDialog(null, "USERNAME HAS BEEN USERD!");
            }
            return true;
        } else {
            return false;
        }
    }

    public void myData() throws SQLException, IOException {
        if (updateChecker()) {
        } else if (!validationChecker()) {
        } else {
            new DBConnector().updateData("update users set email = '" + email.getText() + "',username = '" + username.getText() + "', "
                    + "contact = '" + contact.getText() + "', Image = '" + destination + "' where id = '" + id2.getText() + "'");

            if (selectedFile != null) {
                Files.copy(selectedFile.toPath(), new File(destination).toPath(), StandardCopyOption.REPLACE_EXISTING);
            }

            JOptionPane.showMessageDialog(null, "ACCOUNT SUCCESSFULLY UPDATED!");
            LoginDashboard ad = new LoginDashboard();
            ad.setVisible(true);
            dispose();

        }
    }

    private boolean validationChecker() {

        if (email.getText().isEmpty() || !EMAIL_PATTERN.matcher(email.getText()).matches()) {
            JOptionPane.showMessageDialog(this, "INVALID EMAIL ADDRESS!");
            return false;
        } else if (contact.getText().isEmpty() || !CONTACT_PATTERN.matcher(contact.getText()).matches()) {
            JOptionPane.showMessageDialog(this, "INVALID CONTACT NUMBER! MUST BE 11 DIGITS!");
            return false;
        } else if (username.getText().isEmpty() || email.getText().isEmpty() || contact.getText().isEmpty()) {
            JOptionPane.showMessageDialog(null, "FILL ALL THE REQUIREMENTS!");
            return false;
        } else if (!contact.getText().matches("\\d+")) {
            JOptionPane.showMessageDialog(null, "CONTACT MUST CONTAIN ONLY DIGITS!");
            return false;
        } else if (selectedFile == null && pic.getIcon() == null) {
            JOptionPane.showMessageDialog(null, "PLEASE INSERT AN IMAGE FIRST!");
            return false;
        } else {
            return true;
        }
    }

    private int FileExistenceChecker(String path) {
        File file = new File(path);
        String fileName = file.getName();

        Path filePath = Paths.get("src/ImageDB", fileName);
        boolean fileExists = Files.exists(filePath);

        if (fileExists) {
            return 1;
        } else {
            return 0;
        }

    }

    public void placeOrder() throws NoSuchAlgorithmException {
        try {
            LocalDate currentDate = LocalDate.now();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            String formattedDate = currentDate.format(formatter);
            Session sess = Session.getInstance();
            String adds = address.getText().trim();
            String mt = method.getSelectedItem() == null ? "" : method.getSelectedItem().toString().trim();

            if (name.getText().isEmpty() || price.getText().isEmpty() || stocks.getText().isEmpty() || status.getText().isEmpty()
                    || adds.isEmpty()) {
                JOptionPane.showMessageDialog(null, "PLEASE FILL ALL THE FIELDS!");
            } else {
                int quantityValue = (int) quantity.getValue();

                try {
                    int stocksInt = Integer.parseInt(stocks.getText());

                    if (stocksInt == 0) {
                        JOptionPane.showMessageDialog(null, "OUT OF STOCK!");
                        return;
                    }

                    if (quantityValue < 1 || quantityValue > stocksInt) {
                        JOptionPane.showMessageDialog(null, "INVALID QUANTITY!");
                        return;
                    }

                    int priceInt = Integer.parseInt(price.getText());
                    double totalPrice = quantityValue * priceInt;
                    double totalCost = stocksInt * priceInt;
                    double totalProfit = totalCost - totalPrice;

                    DBConnector cn = new DBConnector();

                    int newStocks = stocksInt - quantityValue;
                    cn.updateData("update orders set o_stocks = '" + newStocks + "' where o_id = '" + id.getText() + "'");

                    cn.insertData("insert into orders (o_cname, o_name, o_price, o_stocks, o_status, o_method, o_quantity, o_address, total_profit, o_approve, o_date) "
                            + "values ('" + sess.getUsername() + "','" + name.getText() + "', '" + price.getText() + "', '"
                            + stocks.getText() + "', '" + status.getText() + "', '" + mt + "', '" + quantityValue + "',"
                            + "'" + adds + "', '" + totalProfit + "', 'False', '" + formattedDate + "')");

                    JOptionPane.showMessageDialog(this, "PRODUCT CREATED SUCCESSFULLY!");

                    jTabbedPane1.setSelectedIndex(0);

                    address.setText("");
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(this, "Invalid stocks or price value!");
                    System.out.println(ex.getMessage());
                }
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error creating product!");
            System.out.println(ex.getMessage());
        }
    }

    public static int getHeightFromWidth(String imagePath, int desiredWidth) {
        try {
            File imageFile = new File(imagePath);
            BufferedImage image = ImageIO.read(imageFile);

            int originalWidth = image.getWidth();
            int originalHeight = image.getHeight();

            int newHeight = (int) ((double) desiredWidth / originalWidth * originalHeight);

            return newHeight;
        } catch (IOException ex) {
            System.out.println("No image found!");
        }

        return -1;
    }

    private ImageIcon ResizeImage(String ImagePath, byte[] pic, JLabel label) {
        ImageIcon MyImage = null;
        if (ImagePath != null) {
            MyImage = new ImageIcon(ImagePath);
        } else {
            MyImage = new ImageIcon(pic);
        }

        int newHeight = getHeightFromWidth(ImagePath, label.getWidth());

        Image img = MyImage.getImage();
        Image newImg = img.getScaledInstance(label.getWidth(), newHeight, Image.SCALE_SMOOTH);
        ImageIcon image = new ImageIcon(newImg);
        return image;
    }

    private void errorMessage(String message) {
        JOptionPane.showMessageDialog(this, message, "ERROR!", JOptionPane.ERROR_MESSAGE);
    }

    private void successMessage(String message) {
        JOptionPane.showMessageDialog(this, message, "SUCCESS!", JOptionPane.INFORMATION_MESSAGE);
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel6 = new javax.swing.JPanel();
        jTabbedPane1 = new javax.swing.JTabbedPane();
        jPanel1 = new javax.swing.JPanel();
        jScrollPane2 = new javax.swing.JScrollPane();
        products = new javax.swing.JTable();
        jButton2 = new javax.swing.JButton();
        jLabel4 = new javax.swing.JLabel();
        adminName = new javax.swing.JLabel();
        jButton1 = new javax.swing.JButton();
        jButton3 = new javax.swing.JButton();
        jLabel12 = new javax.swing.JLabel();
        jPanel2 = new javax.swing.JPanel();
        jLabel15 = new javax.swing.JLabel();
        name = new javax.swing.JTextField();
        price = new javax.swing.JTextField();
        jLabel18 = new javax.swing.JLabel();
        jLabel19 = new javax.swing.JLabel();
        status = new javax.swing.JTextField();
        jLabel20 = new javax.swing.JLabel();
        id = new javax.swing.JTextField();
        jButton28 = new javax.swing.JButton();
        jButton27 = new javax.swing.JButton();
        jPanel7 = new javax.swing.JPanel();
        productImage = new javax.swing.JLabel();
        stocks = new javax.swing.JTextField();
        jLabel21 = new javax.swing.JLabel();
        address = new javax.swing.JTextField();
        method = new javax.swing.JComboBox<>();
        jLabel22 = new javax.swing.JLabel();
        quantity = new javax.swing.JSpinner();
        jLabel23 = new javax.swing.JLabel();
        jPanel3 = new javax.swing.JPanel();
        email = new javax.swing.JTextField();
        username = new javax.swing.JTextField();
        contact = new javax.swing.JTextField();
        id2 = new javax.swing.JTextField();
        jPanel4 = new javax.swing.JPanel();
        pic = new javax.swing.JLabel();
        jButton4 = new javax.swing.JButton();
        jButton7 = new javax.swing.JButton();
        jButton8 = new javax.swing.JButton();
        select = new javax.swing.JButton();
        remove = new javax.swing.JButton();
        jPanel5 = new javax.swing.JPanel();
        oldPassword = new javax.swing.JPasswordField();
        newPassword = new javax.swing.JPasswordField();
        cpassword = new javax.swing.JPasswordField();
        jLabel17 = new javax.swing.JLabel();
        showPass = new javax.swing.JCheckBox();
        jButton10 = new javax.swing.JButton();
        jButton11 = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setMinimumSize(new java.awt.Dimension(1098, 699));
        setUndecorated(true);
        setResizable(false);
        getContentPane().setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jPanel6.setBackground(new java.awt.Color(255, 249, 239));
        getContentPane().add(jPanel6, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, -40, 1100, 80));

        jPanel1.setBackground(new java.awt.Color(255, 249, 239));
        jPanel1.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        products.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {

            }
        ));
        products.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                productsMouseClicked(evt);
            }
        });
        jScrollPane2.setViewportView(products);

        jPanel1.add(jScrollPane2, new org.netbeans.lib.awtextra.AbsoluteConstraints(30, 170, 600, 440));

        jButton2.setBackground(new java.awt.Color(255, 249, 239));
        jButton2.setText("MY ACCOUNT");
        jButton2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton2ActionPerformed(evt);
            }
        });
        jPanel1.add(jButton2, new org.netbeans.lib.awtextra.AbsoluteConstraints(30, 620, 120, 30));

        jLabel4.setFont(new java.awt.Font("Yu Gothic", 1, 18)); // NOI18N
        jLabel4.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Images/1.png"))); // NOI18N
        jLabel4.setText("USERS DASHBOARD");
        jPanel1.add(jLabel4, new org.netbeans.lib.awtextra.AbsoluteConstraints(620, 110, 420, 470));

        adminName.setFont(new java.awt.Font("Yu Gothic", 0, 15)); // NOI18N
        adminName.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Images/acc.png"))); // NOI18N
        adminName.setText("USERS NAME");
        jPanel1.add(adminName, new org.netbeans.lib.awtextra.AbsoluteConstraints(40, 20, -1, -1));

        jButton1.setBackground(new java.awt.Color(255, 249, 239));
        jButton1.setFont(new java.awt.Font("Yu Gothic", 0, 11)); // NOI18N
        jButton1.setText("LOGOUT");
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });
        jPanel1.add(jButton1, new org.netbeans.lib.awtextra.AbsoluteConstraints(970, 20, 100, -1));

        jButton3.setBackground(new java.awt.Color(255, 249, 239));
        jButton3.setText("PLACE ORDER");
        jButton3.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton3ActionPerformed(evt);
            }
        });
        jPanel1.add(jButton3, new org.netbeans.lib.awtextra.AbsoluteConstraints(510, 620, 120, 30));

        jLabel12.setFont(new java.awt.Font("Yu Gothic", 0, 15)); // NOI18N
        jLabel12.setText("USERS DASHBOARD");
        jPanel1.add(jLabel12, new org.netbeans.lib.awtextra.AbsoluteConstraints(240, 130, 240, 30));

        jTabbedPane1.addTab("tab1", jPanel1);

        jPanel2.setBackground(new java.awt.Color(255, 249, 239));
        jPanel2.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jLabel15.setFont(new java.awt.Font("Tahoma", 0, 15)); // NOI18N
        jLabel15.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel15.setText("Product Name");
        jPanel2.add(jLabel15, new org.netbeans.lib.awtextra.AbsoluteConstraints(550, 200, 230, -1));

        name.setEditable(false);
        name.setBackground(new java.awt.Color(255, 249, 239));
        name.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        name.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                nameActionPerformed(evt);
            }
        });
        jPanel2.add(name, new org.netbeans.lib.awtextra.AbsoluteConstraints(550, 220, 230, 30));

        price.setEditable(false);
        price.setBackground(new java.awt.Color(255, 249, 239));
        price.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        jPanel2.add(price, new org.netbeans.lib.awtextra.AbsoluteConstraints(790, 220, 230, 30));

        jLabel18.setFont(new java.awt.Font("Tahoma", 0, 15)); // NOI18N
        jLabel18.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel18.setText("Product Price");
        jPanel2.add(jLabel18, new org.netbeans.lib.awtextra.AbsoluteConstraints(790, 200, 230, -1));

        jLabel19.setFont(new java.awt.Font("Tahoma", 0, 15)); // NOI18N
        jLabel19.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel19.setText("Product Stocks");
        jPanel2.add(jLabel19, new org.netbeans.lib.awtextra.AbsoluteConstraints(550, 270, 230, -1));

        status.setEditable(false);
        status.setBackground(new java.awt.Color(255, 249, 239));
        status.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        jPanel2.add(status, new org.netbeans.lib.awtextra.AbsoluteConstraints(790, 290, 230, 30));

        jLabel20.setFont(new java.awt.Font("Tahoma", 0, 15)); // NOI18N
        jLabel20.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel20.setText("Product Status");
        jPanel2.add(jLabel20, new org.netbeans.lib.awtextra.AbsoluteConstraints(790, 270, 230, -1));

        id.setEditable(false);
        id.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        id.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                idActionPerformed(evt);
            }
        });
        jPanel2.add(id, new org.netbeans.lib.awtextra.AbsoluteConstraints(40, 500, 470, 30));

        jButton28.setFont(new java.awt.Font("Yu Gothic", 0, 11)); // NOI18N
        jButton28.setText("CONFIRM");
        jButton28.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton28ActionPerformed(evt);
            }
        });
        jPanel2.add(jButton28, new org.netbeans.lib.awtextra.AbsoluteConstraints(550, 480, 110, -1));

        jButton27.setFont(new java.awt.Font("Yu Gothic", 0, 11)); // NOI18N
        jButton27.setText("BACK");
        jButton27.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton27ActionPerformed(evt);
            }
        });
        jPanel2.add(jButton27, new org.netbeans.lib.awtextra.AbsoluteConstraints(910, 480, 110, -1));

        jPanel7.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());
        jPanel7.add(productImage, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 10, 450, 270));

        jPanel2.add(jPanel7, new org.netbeans.lib.awtextra.AbsoluteConstraints(40, 200, 470, 290));

        stocks.setEditable(false);
        stocks.setBackground(new java.awt.Color(255, 249, 239));
        stocks.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        jPanel2.add(stocks, new org.netbeans.lib.awtextra.AbsoluteConstraints(550, 290, 230, 30));

        jLabel21.setFont(new java.awt.Font("Tahoma", 0, 15)); // NOI18N
        jLabel21.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel21.setText("Payment Method");
        jPanel2.add(jLabel21, new org.netbeans.lib.awtextra.AbsoluteConstraints(550, 340, 230, -1));

        address.setBackground(new java.awt.Color(255, 249, 239));
        address.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        jPanel2.add(address, new org.netbeans.lib.awtextra.AbsoluteConstraints(550, 430, 470, 30));

        method.setBackground(new java.awt.Color(255, 249, 239));
        method.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "CASH ON DELIVERY" }));
        jPanel2.add(method, new org.netbeans.lib.awtextra.AbsoluteConstraints(550, 360, 230, 30));

        jLabel22.setFont(new java.awt.Font("Tahoma", 0, 15)); // NOI18N
        jLabel22.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel22.setText("Quantity");
        jPanel2.add(jLabel22, new org.netbeans.lib.awtextra.AbsoluteConstraints(790, 340, 230, -1));

        quantity.setValue(1);
        jPanel2.add(quantity, new org.netbeans.lib.awtextra.AbsoluteConstraints(790, 360, 230, 30));

        jLabel23.setFont(new java.awt.Font("Tahoma", 0, 15)); // NOI18N
        jLabel23.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel23.setText("Address");
        jPanel2.add(jLabel23, new org.netbeans.lib.awtextra.AbsoluteConstraints(550, 410, 470, -1));

        jTabbedPane1.addTab("tab1", jPanel2);

        jPanel3.setBackground(new java.awt.Color(255, 249, 239));
        jPanel3.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        email.setBackground(new java.awt.Color(255, 249, 239));
        email.setFont(new java.awt.Font("Yu Gothic", 0, 11)); // NOI18N
        email.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        email.setText("EMAIL ");
        email.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                emailMouseClicked(evt);
            }
        });
        jPanel3.add(email, new org.netbeans.lib.awtextra.AbsoluteConstraints(220, 180, 240, 30));

        username.setBackground(new java.awt.Color(255, 249, 239));
        username.setFont(new java.awt.Font("Yu Gothic", 0, 11)); // NOI18N
        username.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        username.setText("USERNAME");
        username.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                usernameFocusGained(evt);
            }
        });
        username.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                usernameMouseClicked(evt);
            }
        });
        jPanel3.add(username, new org.netbeans.lib.awtextra.AbsoluteConstraints(220, 220, 240, 30));

        contact.setBackground(new java.awt.Color(255, 249, 239));
        contact.setFont(new java.awt.Font("Yu Gothic", 0, 11)); // NOI18N
        contact.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        contact.setText("CONTACT#\n");
        contact.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                contactFocusGained(evt);
            }
        });
        jPanel3.add(contact, new org.netbeans.lib.awtextra.AbsoluteConstraints(220, 260, 240, 30));

        id2.setEditable(false);
        id2.setFont(new java.awt.Font("Yu Gothic", 0, 11)); // NOI18N
        id2.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        id2.setText("ID");
        id2.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                id2MouseClicked(evt);
            }
        });
        jPanel3.add(id2, new org.netbeans.lib.awtextra.AbsoluteConstraints(220, 300, 240, 30));

        jPanel4.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());
        jPanel4.add(pic, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 10, 370, 350));

        jPanel3.add(jPanel4, new org.netbeans.lib.awtextra.AbsoluteConstraints(500, 180, 390, 370));

        jButton4.setBackground(new java.awt.Color(255, 249, 239));
        jButton4.setFont(new java.awt.Font("Yu Gothic", 0, 11)); // NOI18N
        jButton4.setText("CANCEL");
        jButton4.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton4ActionPerformed(evt);
            }
        });
        jPanel3.add(jButton4, new org.netbeans.lib.awtextra.AbsoluteConstraints(210, 570, 120, -1));

        jButton7.setBackground(new java.awt.Color(255, 249, 239));
        jButton7.setFont(new java.awt.Font("Yu Gothic", 0, 11)); // NOI18N
        jButton7.setText("UPDATE");
        jButton7.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton7ActionPerformed(evt);
            }
        });
        jPanel3.add(jButton7, new org.netbeans.lib.awtextra.AbsoluteConstraints(350, 570, 120, -1));

        jButton8.setBackground(new java.awt.Color(255, 249, 239));
        jButton8.setFont(new java.awt.Font("Yu Gothic", 0, 11)); // NOI18N
        jButton8.setText("CHANGE PASS");
        jButton8.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton8ActionPerformed(evt);
            }
        });
        jPanel3.add(jButton8, new org.netbeans.lib.awtextra.AbsoluteConstraints(350, 530, 120, -1));

        select.setBackground(new java.awt.Color(255, 249, 239));
        select.setFont(new java.awt.Font("Yu Gothic", 0, 11)); // NOI18N
        select.setText("SELECT");
        select.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                selectActionPerformed(evt);
            }
        });
        jPanel3.add(select, new org.netbeans.lib.awtextra.AbsoluteConstraints(500, 570, 120, -1));

        remove.setBackground(new java.awt.Color(255, 249, 239));
        remove.setFont(new java.awt.Font("Yu Gothic", 0, 11)); // NOI18N
        remove.setText("REMOVE");
        remove.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                removeActionPerformed(evt);
            }
        });
        jPanel3.add(remove, new org.netbeans.lib.awtextra.AbsoluteConstraints(770, 570, 120, -1));

        jTabbedPane1.addTab("tab1", jPanel3);

        jPanel5.setBackground(new java.awt.Color(255, 249, 239));
        jPanel5.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        oldPassword.setBackground(new java.awt.Color(255, 249, 239));
        oldPassword.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        oldPassword.setText("OLD PASSWORD");
        jPanel5.add(oldPassword, new org.netbeans.lib.awtextra.AbsoluteConstraints(420, 250, 240, 30));

        newPassword.setBackground(new java.awt.Color(255, 249, 239));
        newPassword.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        newPassword.setText("NEW PASSWORD");
        jPanel5.add(newPassword, new org.netbeans.lib.awtextra.AbsoluteConstraints(420, 310, 240, 30));

        cpassword.setBackground(new java.awt.Color(255, 249, 239));
        cpassword.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        cpassword.setText("CONFIRM PASS");
        jPanel5.add(cpassword, new org.netbeans.lib.awtextra.AbsoluteConstraints(420, 350, 240, 30));

        jLabel17.setFont(new java.awt.Font("Yu Gothic", 1, 18)); // NOI18N
        jLabel17.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Images/eidt.png"))); // NOI18N
        jLabel17.setText("CHANGE PASS");
        jPanel5.add(jLabel17, new org.netbeans.lib.awtextra.AbsoluteConstraints(450, 170, 190, 40));

        showPass.setBackground(new java.awt.Color(255, 249, 239));
        showPass.setFont(new java.awt.Font("Yu Gothic", 0, 11)); // NOI18N
        showPass.setText("SHOW PASSWORD");
        showPass.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                showPassActionPerformed(evt);
            }
        });
        jPanel5.add(showPass, new org.netbeans.lib.awtextra.AbsoluteConstraints(540, 390, -1, -1));

        jButton10.setBackground(new java.awt.Color(255, 249, 239));
        jButton10.setFont(new java.awt.Font("Yu Gothic", 0, 11)); // NOI18N
        jButton10.setText("CANCEL");
        jButton10.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton10ActionPerformed(evt);
            }
        });
        jPanel5.add(jButton10, new org.netbeans.lib.awtextra.AbsoluteConstraints(410, 540, 100, -1));

        jButton11.setBackground(new java.awt.Color(255, 249, 239));
        jButton11.setFont(new java.awt.Font("Yu Gothic", 0, 11)); // NOI18N
        jButton11.setText("CHANGE");
        jButton11.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton11ActionPerformed(evt);
            }
        });
        jPanel5.add(jButton11, new org.netbeans.lib.awtextra.AbsoluteConstraints(570, 540, 100, -1));

        jTabbedPane1.addTab("tab1", jPanel5);

        getContentPane().add(jTabbedPane1, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 0, 1100, 700));

        pack();
        setLocationRelativeTo(null);
    }// </editor-fold>//GEN-END:initComponents

    private void jButton27ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton27ActionPerformed
        jTabbedPane1.setSelectedIndex(0);
        displayProducts();
    }//GEN-LAST:event_jButton27ActionPerformed

    private void jButton28ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton28ActionPerformed
        try {
            placeOrder();
        } catch (NoSuchAlgorithmException ex) {
            Logger.getLogger(UserDashboard.class.getName()).log(Level.SEVERE, null, ex);
        }
    }//GEN-LAST:event_jButton28ActionPerformed

    private void idActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_idActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_idActionPerformed

    private void nameActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_nameActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_nameActionPerformed

    private void jButton3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton3ActionPerformed
        int rowIndex = products.getSelectedRow();
        if (rowIndex < 0) {
            JOptionPane.showMessageDialog(null, "PLEASE SELECT AN INDEX!");
        } else {
            jTabbedPane1.setSelectedIndex(1);
        }
    }//GEN-LAST:event_jButton3ActionPerformed

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
        new LoginDashboard().setVisible(true);
        dispose();
    }//GEN-LAST:event_jButton1ActionPerformed

    private void productsMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_productsMouseClicked
        int rowIndex = products.getSelectedRow();
        if (rowIndex < 0) {
            JOptionPane.showMessageDialog(null, "PLEASE SELECT AN INDEX!");
        } else {
            try {
                TableModel tbl = products.getModel();
                ResultSet rs = new DBConnector().getData("select * from products where p_id = '" + tbl.getValueAt(rowIndex, 0) + "'");
                if (rs.next()) {
                    id.setText("" + rs.getString("p_id"));
                    name.setText("" + rs.getString("p_name"));
                    price.setText("" + rs.getString("p_price"));
                    stocks.setText("" + rs.getString("p_stocks"));
                    status.setText("" + rs.getString("p_status"));
                    productImage.setIcon(ResizeImage(rs.getString("p_image"), null, productImage));
                    oldPath = rs.getString("p_image");
                    path = rs.getString("p_image");

                }
            } catch (SQLException er) {
                System.out.println("ERROR: " + er.getMessage());
            }
        }
    }//GEN-LAST:event_productsMouseClicked

    private void jButton2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton2ActionPerformed
        try {
            Session sess = Session.getInstance();
            ResultSet rs = new DBConnector().getData("select * from users where id = '" + sess.getId() + "'");
            if (rs.next()) {
                id2.setText("" + rs.getString("id"));
                email.setText("" + rs.getString("email"));
                username.setText("" + rs.getString("username"));
                contact.setText("" + rs.getString("contact"));

                pic.setIcon(ResizeImage(rs.getString("image"), null, pic));
                oldPath = rs.getString("image");
                path = rs.getString("image");
                destination = rs.getString("image");
                jTabbedPane1.setSelectedIndex(2);
            }
        } catch (SQLException er) {
            System.out.println("ERROR: " + er.getMessage());
        }
    }//GEN-LAST:event_jButton2ActionPerformed

    private void emailMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_emailMouseClicked
        email.setText("");
    }//GEN-LAST:event_emailMouseClicked

    private void usernameFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_usernameFocusGained
        username.setText("");
    }//GEN-LAST:event_usernameFocusGained

    private void usernameMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_usernameMouseClicked

    }//GEN-LAST:event_usernameMouseClicked

    private void contactFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_contactFocusGained
        contact.setText("");
    }//GEN-LAST:event_contactFocusGained

    private void id2MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_id2MouseClicked

    }//GEN-LAST:event_id2MouseClicked

    private void jButton4ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton4ActionPerformed
        jTabbedPane1.setSelectedIndex(0);
    }//GEN-LAST:event_jButton4ActionPerformed

    private void jButton7ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton7ActionPerformed
        try {
            myData();
        } catch (SQLException | IOException ex) {
            Logger.getLogger(myAccount.class.getName()).log(Level.SEVERE, null, ex);
        }
    }//GEN-LAST:event_jButton7ActionPerformed

    private void jButton8ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton8ActionPerformed
        jTabbedPane1.setSelectedIndex(3);
    }//GEN-LAST:event_jButton8ActionPerformed

    private void selectActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_selectActionPerformed
        JFileChooser fileChooser = new JFileChooser();
        int returnValue = fileChooser.showOpenDialog(null);
        if (returnValue == JFileChooser.APPROVE_OPTION) {
            try {
                selectedFile = fileChooser.getSelectedFile();
                destination = "src/ImageDB/" + selectedFile.getName();
                path = selectedFile.getAbsolutePath();

                if (FileExistenceChecker(path) == 1) {
                    JOptionPane.showMessageDialog(null, "File Already Exist, Rename or Choose another!");
                    destination = "";
                    path = "";
                } else {
                    pic.setIcon(ResizeImage(path, null, pic));
                    remove.setEnabled(true);
                    select.setEnabled(false);
                }
            } catch (Exception ex) {
                System.out.println("File Error!");
            }
        }
    }//GEN-LAST:event_selectActionPerformed

    private void removeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_removeActionPerformed
        destination = "";
        pic.setIcon(null);
        path = "";
        select.setEnabled(true);
        remove.setEnabled(false);
    }//GEN-LAST:event_removeActionPerformed

    private void showPassActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_showPassActionPerformed
        char echoChar = showPass.isSelected() ? (char) 0 : '*';
        oldPassword.setEchoChar(echoChar);
        newPassword.setEchoChar(echoChar);
        cpassword.setEchoChar(echoChar);
    }//GEN-LAST:event_showPassActionPerformed

    private void jButton10ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton10ActionPerformed
        jTabbedPane1.setSelectedIndex(2);
    }//GEN-LAST:event_jButton10ActionPerformed

    private void jButton11ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton11ActionPerformed
        try {
            if (!newPassword.getText().equals(cpassword.getText())) {
                errorMessage("PASSWORD DOES NOT MATCH!");
                return;
            } else {
                Session sess = Session.getInstance();
                ResultSet rs = new DBConnector().getData("select * from users where id = '" + sess.getId() + "'");
                if (rs.next()) {
                    String oldPass = rs.getString("password");
                    String oldHash = passwordHashing.hashPassword(oldPassword.getText());

                    if (oldPass.equals(oldHash)) {
                        String newPass = passwordHashing.hashPassword(newPassword.getText());
                        new DBConnector().updateData("update users set password = '" + newPass + "' where id = '" + sess.getId() + "'");
                        successMessage("ACCOUNT SUCCESSFULLY UPDATED!");
                        new LoginDashboard().setVisible(true);
                        dispose();
                    } else {
                        errorMessage("OLD PASSWORD IS INCORRECT!");
                    }
                } else {
                    errorMessage("NO ACCOUNT FOUND!");
                }
            }
        } catch (SQLException | NoSuchAlgorithmException er) {
            System.out.println("Error: " + er.getMessage());
        }
    }//GEN-LAST:event_jButton11ActionPerformed

    public static void main(String args[]) {

        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new UserDashboard().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JTextField address;
    private javax.swing.JLabel adminName;
    public javax.swing.JTextField contact;
    private javax.swing.JPasswordField cpassword;
    public javax.swing.JTextField email;
    private javax.swing.JTextField id;
    public javax.swing.JTextField id2;
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton10;
    private javax.swing.JButton jButton11;
    private javax.swing.JButton jButton2;
    private javax.swing.JButton jButton27;
    private javax.swing.JButton jButton28;
    private javax.swing.JButton jButton3;
    private javax.swing.JButton jButton4;
    private javax.swing.JButton jButton7;
    private javax.swing.JButton jButton8;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel15;
    private javax.swing.JLabel jLabel17;
    private javax.swing.JLabel jLabel18;
    private javax.swing.JLabel jLabel19;
    private javax.swing.JLabel jLabel20;
    private javax.swing.JLabel jLabel21;
    private javax.swing.JLabel jLabel22;
    private javax.swing.JLabel jLabel23;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanel5;
    private javax.swing.JPanel jPanel6;
    private javax.swing.JPanel jPanel7;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JTabbedPane jTabbedPane1;
    private javax.swing.JComboBox<String> method;
    private javax.swing.JTextField name;
    private javax.swing.JPasswordField newPassword;
    private javax.swing.JPasswordField oldPassword;
    public javax.swing.JLabel pic;
    private javax.swing.JTextField price;
    private javax.swing.JLabel productImage;
    private javax.swing.JTable products;
    private javax.swing.JSpinner quantity;
    private javax.swing.JButton remove;
    private javax.swing.JButton select;
    private javax.swing.JCheckBox showPass;
    private javax.swing.JTextField status;
    private javax.swing.JTextField stocks;
    public javax.swing.JTextField username;
    // End of variables declaration//GEN-END:variables
}
