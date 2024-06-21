package AdminDSB;

import Config.*;
import LoginDSB.*;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.awt.print.PrinterException;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.NoSuchAlgorithmException;
import java.sql.*;
import java.text.MessageFormat;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.table.*;
import net.proteanit.sql.DbUtils;

public class AdminDashboard extends javax.swing.JFrame {

    public File selectedFile;
    public String path2 = null;
    public String destination = "";
    public String oldPath;
    public String path;

    public AdminDashboard() {
        initComponents();
        displayUsers();
        displayProducts();
        pendingorders();
        activeOrders();
        deliveredProducts();
        jButton21.setEnabled(false);
    }

    private void activeOrders() {
        try {
            ResultSet rs = new DBConnector().getData("select * from orders where o_approve = 'True'");
            orders1.setModel(DbUtils.resultSetToTableModel(rs));
        } catch (SQLException e) {
            System.err.println("An error occurred while fetching data: " + e.getMessage());
        }
    }

    private void deliveredProducts() {
        try {
            ResultSet rs = new DBConnector().getData("select * from orders where o_approve = 'Delivered'");
            delivered.setModel(DbUtils.resultSetToTableModel(rs));
        } catch (SQLException e) {
            System.err.println("An error occurred while fetching data: " + e.getMessage());
        }
    }

    private void pendingorders() {
        try {
            ResultSet rs = new DBConnector().getData("select * from orders where o_approve = 'False'");
            pendingOrders.setModel(DbUtils.resultSetToTableModel(rs));
        } catch (SQLException e) {
            System.err.println("An error occurred while fetching data: " + e.getMessage());
        }
    }

    private void displayUsers() {
        try {
            Session sess = Session.getInstance();
            ResultSet rs = new DBConnector().getData("select id,email,username,contact,type,status from users where status in ('active', 'inactive') and id != '" + sess.getId() + "'");
            usersTB.setModel(DbUtils.resultSetToTableModel(rs));
        } catch (SQLException e) {
            System.err.println("An error occurred while fetching data: " + e.getMessage());
        }
    }

    private void displayProducts() {
        try {
            ResultSet rs = new DBConnector().getData("select * from products");
            productsTB.setModel(DbUtils.resultSetToTableModel(rs));
        } catch (SQLException e) {
            System.err.println("An error occurred while fetching data: " + e.getMessage());
        }
    }

    public void approveOrder() throws NoSuchAlgorithmException {
        int rowIndex = pendingOrders.getSelectedRow();
        if (rowIndex < 0) {
            JOptionPane.showMessageDialog(null, "PLEASE SELECT AN INDEX!");
        } else {
            try {
                DBConnector cn = new DBConnector();
                TableModel tbl = pendingOrders.getModel();

                ResultSet rs = cn.getData("select * from orders where o_id = '" + tbl.getValueAt(rowIndex, 0) + "'");
                if (rs.next()) {
                    int quantityValue = Integer.parseInt(rs.getString("o_quantity"));
                    int stocksValue = Integer.parseInt(rs.getString("o_stocks"));
                    int newStocks = stocksValue - quantityValue;
                    cn.updateData("update orders set o_stocks = '" + newStocks + "' , o_approve = 'True' where o_id = '" + tbl.getValueAt(rowIndex, 0) + "'");
                    cn.updateData("update products set p_stocks = '" + newStocks + "' where p_id = '" + tbl.getValueAt(rowIndex, 0) + "'");
                }

                JOptionPane.showMessageDialog(this, "ORDER APPROVED SUCCESSFULLY!");
                displayUsers();
                displayProducts();
                pendingorders();
                activeOrders();
                deliveredProducts();
                jTabbedPane1.setSelectedIndex(1);

            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this, "ORDER APPROVED FAILED!");
                System.out.println(ex.getMessage());
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "INVALID STOCKS VALUE!");
                System.out.println(ex.getMessage());
            }
        }
    }

    public void doneOrder() throws NoSuchAlgorithmException {
        int rowIndex = orders1.getSelectedRow();
        if (rowIndex < 0) {
            JOptionPane.showMessageDialog(null, "PLEASE SELECT AN INDEX!");
        } else {
            try {
                DBConnector cn = new DBConnector();
                TableModel tbl = orders1.getModel();

                ResultSet rs = cn.getData("select * from orders where o_id = '" + tbl.getValueAt(rowIndex, 0) + "'");
                if (rs.next()) {
                    cn.updateData("update orders set o_approve = 'Delivered' where o_id = '" + tbl.getValueAt(rowIndex, 0) + "'");
                }

                JOptionPane.showMessageDialog(this, "DELIVERY SUCCESSFULLY!");
                displayUsers();
                displayProducts();
                pendingorders();
                activeOrders();
                deliveredProducts();
                jTabbedPane1.setSelectedIndex(1);

            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this, "DELIVERY FAILED!");
                System.out.println(ex.getMessage());
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "DELIVERY FAILED!");
                System.out.println(ex.getMessage());
            }
        }
    }

    public void deleteProduct() throws NoSuchAlgorithmException, SQLException {
        int confirmation = JOptionPane.showConfirmDialog(null, "ARE YOU SURE YOU WANT TO DELETE THIS PRODUCT?", "CONFIRMATION", JOptionPane.YES_NO_OPTION);
        if (confirmation == JOptionPane.YES_OPTION) {
            DBConnector cn = new DBConnector();
            String query = "DELETE FROM products WHERE p_id = '" + id.getText() + "'";
            try (PreparedStatement pstmt = cn.getConnection().prepareStatement(query)) {
                pstmt.executeUpdate();
                JOptionPane.showMessageDialog(this, "PRODUCT DELETED SUCCESSFULLY!");
                displayUsers();
                displayProducts();
                pendingorders();
                activeOrders();
                deliveredProducts();
                jTabbedPane1.setSelectedIndex(1);
            }
        }
    }

    public void addProduct() throws NoSuchAlgorithmException {
        try {
            String xpname = pname1.getText().trim();
            String xpprice = pprice1.getText().trim();
            String xpstocks = pstocks1.getText().trim();
            String xpstatus = pstatus1.getSelectedItem() == null ? "" : pstatus1.getSelectedItem().toString().trim();

            if (xpname.isEmpty() || xpprice.isEmpty() || xpstocks.isEmpty() || xpstatus.isEmpty()) {
                JOptionPane.showMessageDialog(null, "PLEASE FILL ALL THE FIELDS!");
            } else if (destination == null || destination.isEmpty()) {
                JOptionPane.showMessageDialog(null, "PLEASE INSERT AN IMAGE FIRST!");
            } else {
                try {
                    int price = Integer.parseInt(xpprice);
                    int stocks = Integer.parseInt(xpstocks);
                    DBConnector cn = new DBConnector();
                    cn.insertData("insert into products (p_name, p_price, p_stocks, p_status, p_image) "
                            + "values ('" + xpname + "', '" + price + "', '"
                            + stocks + "', '" + xpstatus + "', '" + destination + "')");

                    if (destination != null && path != null) {
                        Files.copy(selectedFile.toPath(), new File(destination).toPath(), StandardCopyOption.REPLACE_EXISTING);
                    }

                    JOptionPane.showMessageDialog(this, "PRODUCT CREATED SUCCESSFULLY!");
                    displayUsers();
                    displayProducts();
                    pendingorders();
                    activeOrders();
                    deliveredProducts();
                    jTabbedPane1.setSelectedIndex(1);

                    pname1.setText("");
                    pprice1.setText("");
                    pstocks1.setText("");
                    icon1.setIcon(null);
                } catch (NumberFormatException e) {
                    JOptionPane.showMessageDialog(this, "Price and stocks should be integers!");
                }
            }
        } catch (SQLException | IOException ex) {
            JOptionPane.showMessageDialog(this, "Error creating product!");
            System.out.println(ex.getMessage());
        }
    }

    public void updateProduct() throws NoSuchAlgorithmException {
        try {
            String xpname1 = pn.getText().trim();
            String xpprice1 = pp.getText().trim();
            String xpstocks1 = ps.getText().trim();
            String xpstatus1 = pstats.getSelectedItem() == null ? "" : pstats.getSelectedItem().toString().trim();

            if (xpname1.isEmpty() || xpprice1.isEmpty() || xpstocks1.isEmpty() || xpstatus1.isEmpty()) {
                JOptionPane.showMessageDialog(null, "PLEASE FILL ALL THE FIELDS!");
            } else if (selectedFile == null && icon2.getIcon() == null) {
                JOptionPane.showMessageDialog(null, "PLEASE INSERT AN IMAGE FIRST!");
            } else {
                DBConnector cn = new DBConnector();
                cn.updateData("update products set p_name = '" + xpname1 + "', p_price = '" + xpprice1 + "',p_stocks='" + xpstocks1 + "', "
                        + "p_status='" + xpstatus1 + "', p_image= '" + destination + "' where p_id = '" + id.getText() + "'");

                if (selectedFile != null) {
                    Files.copy(selectedFile.toPath(), new File(destination).toPath(), StandardCopyOption.REPLACE_EXISTING);
                }
                JOptionPane.showMessageDialog(this, "PRODUCT UPDATED SUCCESSFULLY!");
                displayUsers();
                displayProducts();
                pendingorders();
                activeOrders();
                deliveredProducts();
                jTabbedPane1.setSelectedIndex(1);

                pn.setText("");
                pp.setText("");
                ps.setText("");
                icon2.setIcon(null);
            }
        } catch (SQLException | IOException ex) {
            JOptionPane.showMessageDialog(this, "Error updating product!");
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

    private int FileExistenceChecker(String path) {
        File file = new File(path);
        String fileName = file.getName();

        Path filePath = Paths.get("src/ProductsImage", fileName);
        boolean fileExists = Files.exists(filePath);

        if (fileExists) {
            return 1;
        } else {
            return 0;
        }

    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel1 = new javax.swing.JPanel();
        jTabbedPane1 = new javax.swing.JTabbedPane();
        jPanel2 = new javax.swing.JPanel();
        jScrollPane2 = new javax.swing.JScrollPane();
        usersTB = new javax.swing.JTable();
        aname = new javax.swing.JLabel();
        jButton1 = new javax.swing.JButton();
        jLabel7 = new javax.swing.JLabel();
        jButton5 = new javax.swing.JButton();
        jButton9 = new javax.swing.JButton();
        jButton7 = new javax.swing.JButton();
        jButton2 = new javax.swing.JButton();
        jButton13 = new javax.swing.JButton();
        jButton11 = new javax.swing.JButton();
        jLabel1 = new javax.swing.JLabel();
        jPanel3 = new javax.swing.JPanel();
        jScrollPane3 = new javax.swing.JScrollPane();
        productsTB = new javax.swing.JTable();
        jButton8 = new javax.swing.JButton();
        jButton6 = new javax.swing.JButton();
        jButton3 = new javax.swing.JButton();
        jButton10 = new javax.swing.JButton();
        jButton16 = new javax.swing.JButton();
        jButton17 = new javax.swing.JButton();
        jButton4 = new javax.swing.JButton();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        jPanel4 = new javax.swing.JPanel();
        jLabel5 = new javax.swing.JLabel();
        jButton21 = new javax.swing.JButton();
        pname1 = new javax.swing.JTextField();
        jLabel12 = new javax.swing.JLabel();
        pprice1 = new javax.swing.JTextField();
        jLabel13 = new javax.swing.JLabel();
        pstocks1 = new javax.swing.JTextField();
        jLabel14 = new javax.swing.JLabel();
        pstatus1 = new javax.swing.JComboBox<>();
        jPanel5 = new javax.swing.JPanel();
        icon1 = new javax.swing.JLabel();
        jButton24 = new javax.swing.JButton();
        jButton22 = new javax.swing.JButton();
        jButton25 = new javax.swing.JButton();
        jPanel6 = new javax.swing.JPanel();
        jLabel15 = new javax.swing.JLabel();
        remove = new javax.swing.JButton();
        id = new javax.swing.JTextField();
        jLabel18 = new javax.swing.JLabel();
        pp = new javax.swing.JTextField();
        jLabel19 = new javax.swing.JLabel();
        ps = new javax.swing.JTextField();
        jLabel20 = new javax.swing.JLabel();
        pstats = new javax.swing.JComboBox<>();
        jPanel7 = new javax.swing.JPanel();
        icon2 = new javax.swing.JLabel();
        select = new javax.swing.JButton();
        jButton27 = new javax.swing.JButton();
        jButton28 = new javax.swing.JButton();
        pn = new javax.swing.JTextField();
        jButton29 = new javax.swing.JButton();
        jPanel8 = new javax.swing.JPanel();
        jButton30 = new javax.swing.JButton();
        jButton31 = new javax.swing.JButton();
        jScrollPane1 = new javax.swing.JScrollPane();
        pendingOrders = new javax.swing.JTable();
        jButton32 = new javax.swing.JButton();
        jPanel9 = new javax.swing.JPanel();
        jButton33 = new javax.swing.JButton();
        jScrollPane4 = new javax.swing.JScrollPane();
        orders1 = new javax.swing.JTable();
        jButton35 = new javax.swing.JButton();
        jButton34 = new javax.swing.JButton();
        jPanel10 = new javax.swing.JPanel();
        jScrollPane5 = new javax.swing.JScrollPane();
        delivered = new javax.swing.JTable();
        jButton37 = new javax.swing.JButton();
        jButton38 = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setBackground(new java.awt.Color(255, 249, 239));
        setMinimumSize(new java.awt.Dimension(1098, 699));
        setUndecorated(true);
        setPreferredSize(new java.awt.Dimension(1098, 699));
        setResizable(false);
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowActivated(java.awt.event.WindowEvent evt) {
                formWindowActivated(evt);
            }
        });
        getContentPane().setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jPanel1.setBackground(new java.awt.Color(255, 249, 239));
        jPanel1.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());
        getContentPane().add(jPanel1, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, -20, 1110, 50));

        jPanel2.setBackground(new java.awt.Color(255, 249, 239));
        jPanel2.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        usersTB.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {

            }
        ));
        jScrollPane2.setViewportView(usersTB);

        jPanel2.add(jScrollPane2, new org.netbeans.lib.awtextra.AbsoluteConstraints(510, 160, 560, 460));

        aname.setFont(new java.awt.Font("Yu Gothic", 0, 15)); // NOI18N
        aname.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Images/acc.png"))); // NOI18N
        aname.setText("ADMINS NAME");
        jPanel2.add(aname, new org.netbeans.lib.awtextra.AbsoluteConstraints(910, 10, 210, -1));

        jButton1.setBackground(new java.awt.Color(255, 249, 239));
        jButton1.setFont(new java.awt.Font("Yu Gothic", 0, 11)); // NOI18N
        jButton1.setText("LOGOUT");
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });
        jPanel2.add(jButton1, new org.netbeans.lib.awtextra.AbsoluteConstraints(510, 630, 110, -1));

        jLabel7.setFont(new java.awt.Font("Yu Gothic", 0, 15)); // NOI18N
        jLabel7.setText("ADMINS DASHBOARD");
        jPanel2.add(jLabel7, new org.netbeans.lib.awtextra.AbsoluteConstraints(710, 80, 210, 30));

        jButton5.setBackground(new java.awt.Color(255, 249, 239));
        jButton5.setFont(new java.awt.Font("Yu Gothic", 0, 11)); // NOI18N
        jButton5.setText("MY ACCOUNT");
        jButton5.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton5ActionPerformed(evt);
            }
        });
        jPanel2.add(jButton5, new org.netbeans.lib.awtextra.AbsoluteConstraints(960, 630, 110, -1));

        jButton9.setBackground(new java.awt.Color(255, 249, 239));
        jButton9.setFont(new java.awt.Font("Yu Gothic", 0, 11)); // NOI18N
        jButton9.setText("EDIT");
        jButton9.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton9ActionPerformed(evt);
            }
        });
        jPanel2.add(jButton9, new org.netbeans.lib.awtextra.AbsoluteConstraints(190, 550, 130, -1));

        jButton7.setBackground(new java.awt.Color(255, 249, 239));
        jButton7.setFont(new java.awt.Font("Yu Gothic", 0, 11)); // NOI18N
        jButton7.setText("PENDING");
        jButton7.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton7ActionPerformed(evt);
            }
        });
        jPanel2.add(jButton7, new org.netbeans.lib.awtextra.AbsoluteConstraints(330, 550, 150, -1));

        jButton2.setBackground(new java.awt.Color(255, 249, 239));
        jButton2.setFont(new java.awt.Font("Yu Gothic", 0, 11)); // NOI18N
        jButton2.setText("CREATE");
        jButton2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton2ActionPerformed(evt);
            }
        });
        jPanel2.add(jButton2, new org.netbeans.lib.awtextra.AbsoluteConstraints(30, 550, 150, -1));

        jButton13.setBackground(new java.awt.Color(255, 249, 239));
        jButton13.setFont(new java.awt.Font("Yu Gothic", 0, 11)); // NOI18N
        jButton13.setText("MANAGE PRODUCTS");
        jButton13.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton13ActionPerformed(evt);
            }
        });
        jPanel2.add(jButton13, new org.netbeans.lib.awtextra.AbsoluteConstraints(30, 590, 450, -1));

        jButton11.setBackground(new java.awt.Color(255, 249, 239));
        jButton11.setFont(new java.awt.Font("Yu Gothic", 0, 11)); // NOI18N
        jButton11.setText("PRINT");
        jButton11.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton11ActionPerformed(evt);
            }
        });
        jPanel2.add(jButton11, new org.netbeans.lib.awtextra.AbsoluteConstraints(510, 120, 560, -1));

        jLabel1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Images/1.png"))); // NOI18N
        jPanel2.add(jLabel1, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 20, -1, -1));

        jTabbedPane1.addTab("tab1", jPanel2);

        jPanel3.setBackground(new java.awt.Color(255, 249, 239));
        jPanel3.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        productsTB.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {

            }
        ));
        productsTB.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                productsTBMouseClicked(evt);
            }
        });
        jScrollPane3.setViewportView(productsTB);

        jPanel3.add(jScrollPane3, new org.netbeans.lib.awtextra.AbsoluteConstraints(30, 50, 1040, 390));

        jButton8.setBackground(new java.awt.Color(255, 249, 239));
        jButton8.setFont(new java.awt.Font("Yu Gothic", 0, 11)); // NOI18N
        jButton8.setText("DELIVERED");
        jButton8.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton8ActionPerformed(evt);
            }
        });
        jPanel3.add(jButton8, new org.netbeans.lib.awtextra.AbsoluteConstraints(30, 610, 140, 30));

        jButton6.setBackground(new java.awt.Color(255, 249, 239));
        jButton6.setFont(new java.awt.Font("Yu Gothic", 0, 11)); // NOI18N
        jButton6.setText("PRINT");
        jButton6.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton6ActionPerformed(evt);
            }
        });
        jPanel3.add(jButton6, new org.netbeans.lib.awtextra.AbsoluteConstraints(30, 450, 1040, -1));

        jButton3.setBackground(new java.awt.Color(255, 249, 239));
        jButton3.setFont(new java.awt.Font("Yu Gothic", 0, 11)); // NOI18N
        jButton3.setText("ORDERS");
        jButton3.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton3ActionPerformed(evt);
            }
        });
        jPanel3.add(jButton3, new org.netbeans.lib.awtextra.AbsoluteConstraints(30, 560, 140, -1));

        jButton10.setBackground(new java.awt.Color(255, 249, 239));
        jButton10.setFont(new java.awt.Font("Yu Gothic", 0, 11)); // NOI18N
        jButton10.setText("MANAGE USERS");
        jButton10.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton10ActionPerformed(evt);
            }
        });
        jPanel3.add(jButton10, new org.netbeans.lib.awtextra.AbsoluteConstraints(190, 510, 140, -1));

        jButton16.setBackground(new java.awt.Color(255, 249, 239));
        jButton16.setFont(new java.awt.Font("Yu Gothic", 0, 11)); // NOI18N
        jButton16.setText("PENDING ORDERS");
        jButton16.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton16ActionPerformed(evt);
            }
        });
        jPanel3.add(jButton16, new org.netbeans.lib.awtextra.AbsoluteConstraints(30, 510, 140, -1));

        jButton17.setBackground(new java.awt.Color(255, 249, 239));
        jButton17.setFont(new java.awt.Font("Yu Gothic", 0, 11)); // NOI18N
        jButton17.setText("ADD");
        jButton17.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton17ActionPerformed(evt);
            }
        });
        jPanel3.add(jButton17, new org.netbeans.lib.awtextra.AbsoluteConstraints(190, 560, 140, -1));

        jButton4.setBackground(new java.awt.Color(255, 249, 239));
        jButton4.setFont(new java.awt.Font("Yu Gothic", 0, 11)); // NOI18N
        jButton4.setText("UPDATE");
        jButton4.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton4ActionPerformed(evt);
            }
        });
        jPanel3.add(jButton4, new org.netbeans.lib.awtextra.AbsoluteConstraints(190, 610, 140, 30));

        jLabel2.setFont(new java.awt.Font("Tahoma", 0, 15)); // NOI18N
        jLabel2.setText("Your beauty deserves nothing less than the best, and we're here to ensure you shine effortlessly.\"");
        jPanel3.add(jLabel2, new org.netbeans.lib.awtextra.AbsoluteConstraints(410, 590, -1, -1));

        jLabel3.setFont(new java.awt.Font("Tahoma", 0, 15)); // NOI18N
        jLabel3.setText("\"Indulge in our meticulously curated collection, where each product is a testament to quality and luxury.");
        jPanel3.add(jLabel3, new org.netbeans.lib.awtextra.AbsoluteConstraints(360, 540, -1, -1));

        jLabel4.setFont(new java.awt.Font("Tahoma", 0, 15)); // NOI18N
        jLabel4.setText("PRODUCTS");
        jPanel3.add(jLabel4, new org.netbeans.lib.awtextra.AbsoluteConstraints(500, 20, -1, -1));

        jTabbedPane1.addTab("tab1", jPanel3);

        jPanel4.setBackground(new java.awt.Color(255, 249, 239));
        jPanel4.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jLabel5.setFont(new java.awt.Font("Tahoma", 0, 15)); // NOI18N
        jLabel5.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel5.setText("Product Name");
        jPanel4.add(jLabel5, new org.netbeans.lib.awtextra.AbsoluteConstraints(300, 390, 230, -1));

        jButton21.setBackground(new java.awt.Color(255, 249, 239));
        jButton21.setFont(new java.awt.Font("Yu Gothic", 0, 11)); // NOI18N
        jButton21.setText("REMOVE");
        jButton21.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton21ActionPerformed(evt);
            }
        });
        jPanel4.add(jButton21, new org.netbeans.lib.awtextra.AbsoluteConstraints(600, 330, 170, -1));

        pname1.setBackground(new java.awt.Color(255, 249, 239));
        pname1.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        pname1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                pname1ActionPerformed(evt);
            }
        });
        jPanel4.add(pname1, new org.netbeans.lib.awtextra.AbsoluteConstraints(300, 410, 230, 30));

        jLabel12.setFont(new java.awt.Font("Tahoma", 0, 15)); // NOI18N
        jLabel12.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel12.setText("Product Price");
        jPanel4.add(jLabel12, new org.netbeans.lib.awtextra.AbsoluteConstraints(540, 390, 230, -1));

        pprice1.setBackground(new java.awt.Color(255, 249, 239));
        pprice1.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        jPanel4.add(pprice1, new org.netbeans.lib.awtextra.AbsoluteConstraints(540, 410, 230, 30));

        jLabel13.setFont(new java.awt.Font("Tahoma", 0, 15)); // NOI18N
        jLabel13.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel13.setText("Product Stocks");
        jPanel4.add(jLabel13, new org.netbeans.lib.awtextra.AbsoluteConstraints(300, 460, 230, -1));

        pstocks1.setBackground(new java.awt.Color(255, 249, 239));
        pstocks1.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        jPanel4.add(pstocks1, new org.netbeans.lib.awtextra.AbsoluteConstraints(300, 480, 230, 30));

        jLabel14.setFont(new java.awt.Font("Tahoma", 0, 15)); // NOI18N
        jLabel14.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel14.setText("Product Status");
        jPanel4.add(jLabel14, new org.netbeans.lib.awtextra.AbsoluteConstraints(540, 460, 230, -1));

        pstatus1.setBackground(new java.awt.Color(255, 249, 239));
        pstatus1.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "AVAILABLE", "NOT AVAILABLE" }));
        jPanel4.add(pstatus1, new org.netbeans.lib.awtextra.AbsoluteConstraints(540, 480, 230, 30));

        jPanel5.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());
        jPanel5.add(icon1, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 10, 450, 200));

        jPanel4.add(jPanel5, new org.netbeans.lib.awtextra.AbsoluteConstraints(300, 90, 470, 220));

        jButton24.setBackground(new java.awt.Color(255, 249, 239));
        jButton24.setFont(new java.awt.Font("Yu Gothic", 0, 11)); // NOI18N
        jButton24.setText("SELECT");
        jButton24.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton24ActionPerformed(evt);
            }
        });
        jPanel4.add(jButton24, new org.netbeans.lib.awtextra.AbsoluteConstraints(300, 330, 170, -1));

        jButton22.setBackground(new java.awt.Color(255, 249, 239));
        jButton22.setFont(new java.awt.Font("Yu Gothic", 0, 11)); // NOI18N
        jButton22.setText("BACK");
        jButton22.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton22ActionPerformed(evt);
            }
        });
        jPanel4.add(jButton22, new org.netbeans.lib.awtextra.AbsoluteConstraints(660, 530, 110, -1));

        jButton25.setBackground(new java.awt.Color(255, 249, 239));
        jButton25.setFont(new java.awt.Font("Yu Gothic", 0, 11)); // NOI18N
        jButton25.setText("ADD");
        jButton25.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton25ActionPerformed(evt);
            }
        });
        jPanel4.add(jButton25, new org.netbeans.lib.awtextra.AbsoluteConstraints(300, 530, 110, -1));

        jTabbedPane1.addTab("tab1", jPanel4);

        jPanel6.setBackground(new java.awt.Color(255, 249, 239));
        jPanel6.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jLabel15.setFont(new java.awt.Font("Tahoma", 0, 15)); // NOI18N
        jLabel15.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel15.setText("Product Name");
        jPanel6.add(jLabel15, new org.netbeans.lib.awtextra.AbsoluteConstraints(300, 390, 230, -1));

        remove.setBackground(new java.awt.Color(255, 249, 239));
        remove.setFont(new java.awt.Font("Yu Gothic", 0, 11)); // NOI18N
        remove.setText("REMOVE");
        remove.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                removeActionPerformed(evt);
            }
        });
        jPanel6.add(remove, new org.netbeans.lib.awtextra.AbsoluteConstraints(600, 330, 170, -1));

        id.setEditable(false);
        id.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        id.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                idActionPerformed(evt);
            }
        });
        jPanel6.add(id, new org.netbeans.lib.awtextra.AbsoluteConstraints(480, 330, 110, 30));

        jLabel18.setFont(new java.awt.Font("Tahoma", 0, 15)); // NOI18N
        jLabel18.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel18.setText("Product Price");
        jPanel6.add(jLabel18, new org.netbeans.lib.awtextra.AbsoluteConstraints(540, 390, 230, -1));

        pp.setBackground(new java.awt.Color(255, 249, 239));
        pp.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        jPanel6.add(pp, new org.netbeans.lib.awtextra.AbsoluteConstraints(540, 410, 230, 30));

        jLabel19.setFont(new java.awt.Font("Tahoma", 0, 15)); // NOI18N
        jLabel19.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel19.setText("Product Stocks");
        jPanel6.add(jLabel19, new org.netbeans.lib.awtextra.AbsoluteConstraints(300, 460, 230, -1));

        ps.setBackground(new java.awt.Color(255, 249, 239));
        ps.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        jPanel6.add(ps, new org.netbeans.lib.awtextra.AbsoluteConstraints(300, 480, 230, 30));

        jLabel20.setFont(new java.awt.Font("Tahoma", 0, 15)); // NOI18N
        jLabel20.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel20.setText("Product Status");
        jPanel6.add(jLabel20, new org.netbeans.lib.awtextra.AbsoluteConstraints(540, 460, 230, -1));

        pstats.setBackground(new java.awt.Color(255, 249, 239));
        pstats.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "AVAILABLE", "NOT AVAILABLE" }));
        jPanel6.add(pstats, new org.netbeans.lib.awtextra.AbsoluteConstraints(540, 480, 230, 30));

        jPanel7.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());
        jPanel7.add(icon2, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 10, 450, 200));

        jPanel6.add(jPanel7, new org.netbeans.lib.awtextra.AbsoluteConstraints(300, 90, 470, 220));

        select.setBackground(new java.awt.Color(255, 249, 239));
        select.setFont(new java.awt.Font("Yu Gothic", 0, 11)); // NOI18N
        select.setText("SELECT");
        select.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                selectActionPerformed(evt);
            }
        });
        jPanel6.add(select, new org.netbeans.lib.awtextra.AbsoluteConstraints(300, 330, 170, -1));

        jButton27.setBackground(new java.awt.Color(255, 249, 239));
        jButton27.setFont(new java.awt.Font("Yu Gothic", 0, 11)); // NOI18N
        jButton27.setText("BACK");
        jButton27.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton27ActionPerformed(evt);
            }
        });
        jPanel6.add(jButton27, new org.netbeans.lib.awtextra.AbsoluteConstraints(660, 530, 110, -1));

        jButton28.setBackground(new java.awt.Color(255, 249, 239));
        jButton28.setFont(new java.awt.Font("Yu Gothic", 0, 11)); // NOI18N
        jButton28.setText("UPDATE");
        jButton28.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton28ActionPerformed(evt);
            }
        });
        jPanel6.add(jButton28, new org.netbeans.lib.awtextra.AbsoluteConstraints(300, 530, 110, -1));

        pn.setBackground(new java.awt.Color(255, 249, 239));
        pn.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        pn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                pnActionPerformed(evt);
            }
        });
        jPanel6.add(pn, new org.netbeans.lib.awtextra.AbsoluteConstraints(300, 410, 230, 30));

        jButton29.setBackground(new java.awt.Color(255, 249, 239));
        jButton29.setFont(new java.awt.Font("Yu Gothic", 0, 11)); // NOI18N
        jButton29.setText("DELETE");
        jButton29.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton29ActionPerformed(evt);
            }
        });
        jPanel6.add(jButton29, new org.netbeans.lib.awtextra.AbsoluteConstraints(420, 530, 110, -1));

        jTabbedPane1.addTab("tab1", jPanel6);

        jPanel8.setBackground(new java.awt.Color(255, 249, 239));
        jPanel8.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jButton30.setBackground(new java.awt.Color(255, 249, 239));
        jButton30.setFont(new java.awt.Font("Yu Gothic", 0, 11)); // NOI18N
        jButton30.setText("BACK");
        jButton30.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton30ActionPerformed(evt);
            }
        });
        jPanel8.add(jButton30, new org.netbeans.lib.awtextra.AbsoluteConstraints(30, 540, 110, -1));

        jButton31.setBackground(new java.awt.Color(255, 249, 239));
        jButton31.setFont(new java.awt.Font("Yu Gothic", 0, 11)); // NOI18N
        jButton31.setText("APPROVE");
        jButton31.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton31ActionPerformed(evt);
            }
        });
        jPanel8.add(jButton31, new org.netbeans.lib.awtextra.AbsoluteConstraints(950, 540, 110, -1));

        pendingOrders.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {

            }
        ));
        jScrollPane1.setViewportView(pendingOrders);

        jPanel8.add(jScrollPane1, new org.netbeans.lib.awtextra.AbsoluteConstraints(30, 80, 1030, 440));

        jButton32.setBackground(new java.awt.Color(255, 249, 239));
        jButton32.setFont(new java.awt.Font("Yu Gothic", 0, 11)); // NOI18N
        jButton32.setText("PRINT");
        jButton32.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton32ActionPerformed(evt);
            }
        });
        jPanel8.add(jButton32, new org.netbeans.lib.awtextra.AbsoluteConstraints(30, 40, 1030, -1));

        jTabbedPane1.addTab("tab1", jPanel8);

        jPanel9.setBackground(new java.awt.Color(255, 249, 239));
        jPanel9.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jButton33.setBackground(new java.awt.Color(255, 249, 239));
        jButton33.setFont(new java.awt.Font("Yu Gothic", 0, 11)); // NOI18N
        jButton33.setText("DELIVERED");
        jButton33.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton33ActionPerformed(evt);
            }
        });
        jPanel9.add(jButton33, new org.netbeans.lib.awtextra.AbsoluteConstraints(950, 540, 110, -1));

        orders1.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {

            }
        ));
        jScrollPane4.setViewportView(orders1);

        jPanel9.add(jScrollPane4, new org.netbeans.lib.awtextra.AbsoluteConstraints(30, 80, 1030, 440));

        jButton35.setBackground(new java.awt.Color(255, 249, 239));
        jButton35.setFont(new java.awt.Font("Yu Gothic", 0, 11)); // NOI18N
        jButton35.setText("BACK");
        jButton35.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton35ActionPerformed(evt);
            }
        });
        jPanel9.add(jButton35, new org.netbeans.lib.awtextra.AbsoluteConstraints(30, 540, 110, -1));

        jButton34.setBackground(new java.awt.Color(255, 249, 239));
        jButton34.setFont(new java.awt.Font("Yu Gothic", 0, 11)); // NOI18N
        jButton34.setText("PRINT");
        jButton34.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton34ActionPerformed(evt);
            }
        });
        jPanel9.add(jButton34, new org.netbeans.lib.awtextra.AbsoluteConstraints(30, 40, 1030, -1));

        jTabbedPane1.addTab("tab1", jPanel9);

        jPanel10.setBackground(new java.awt.Color(255, 249, 239));
        jPanel10.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        delivered.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {

            }
        ));
        jScrollPane5.setViewportView(delivered);

        jPanel10.add(jScrollPane5, new org.netbeans.lib.awtextra.AbsoluteConstraints(30, 80, 1030, 440));

        jButton37.setBackground(new java.awt.Color(255, 249, 239));
        jButton37.setFont(new java.awt.Font("Yu Gothic", 0, 11)); // NOI18N
        jButton37.setText("BACK");
        jButton37.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton37ActionPerformed(evt);
            }
        });
        jPanel10.add(jButton37, new org.netbeans.lib.awtextra.AbsoluteConstraints(950, 540, 110, -1));

        jButton38.setBackground(new java.awt.Color(255, 249, 239));
        jButton38.setFont(new java.awt.Font("Yu Gothic", 0, 11)); // NOI18N
        jButton38.setText("PRINT");
        jButton38.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton38ActionPerformed(evt);
            }
        });
        jPanel10.add(jButton38, new org.netbeans.lib.awtextra.AbsoluteConstraints(30, 40, 1030, -1));

        jTabbedPane1.addTab("tab1", jPanel10);

        getContentPane().add(jTabbedPane1, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 0, 1100, 700));

        pack();
        setLocationRelativeTo(null);
    }// </editor-fold>//GEN-END:initComponents

    private void formWindowActivated(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowActivated
        Session sess = Session.getInstance();
        aname.setText("" + sess.getUsername());
        displayUsers();
        displayProducts();
    }//GEN-LAST:event_formWindowActivated

    private void jButton35ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton35ActionPerformed
        jTabbedPane1.setSelectedIndex(1);
    }//GEN-LAST:event_jButton35ActionPerformed

    private void jButton33ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton33ActionPerformed
        try {
            doneOrder();
        } catch (NoSuchAlgorithmException ex) {
            Logger.getLogger(AdminDashboard.class.getName()).log(Level.SEVERE, null, ex);
        }
    }//GEN-LAST:event_jButton33ActionPerformed

    private void jButton31ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton31ActionPerformed
        try {
            approveOrder();
        } catch (NoSuchAlgorithmException ex) {
            Logger.getLogger(AdminDashboard.class.getName()).log(Level.SEVERE, null, ex);
        }
    }//GEN-LAST:event_jButton31ActionPerformed

    private void jButton30ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton30ActionPerformed
        jTabbedPane1.setSelectedIndex(1);
    }//GEN-LAST:event_jButton30ActionPerformed

    private void jButton29ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton29ActionPerformed
        try {
            deleteProduct();
        } catch (NoSuchAlgorithmException | SQLException ex) {
            Logger.getLogger(AdminDashboard.class.getName()).log(Level.SEVERE, null, ex);
        }
    }//GEN-LAST:event_jButton29ActionPerformed

    private void pnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_pnActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_pnActionPerformed

    private void jButton28ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton28ActionPerformed
        try {
            updateProduct();
        } catch (NoSuchAlgorithmException ex) {
            Logger.getLogger(AdminDashboard.class.getName()).log(Level.SEVERE, null, ex);
        }
    }//GEN-LAST:event_jButton28ActionPerformed

    private void jButton27ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton27ActionPerformed
        jTabbedPane1.setSelectedIndex(1);
        displayProducts();
    }//GEN-LAST:event_jButton27ActionPerformed

    private void selectActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_selectActionPerformed
        JFileChooser fileChooser = new JFileChooser();
        int returnValue = fileChooser.showOpenDialog(null);
        if (returnValue == JFileChooser.APPROVE_OPTION) {
            try {
                selectedFile = fileChooser.getSelectedFile();
                destination = "src/ProductsImage/" + selectedFile.getName();
                path = selectedFile.getAbsolutePath();

                if (FileExistenceChecker(path) == 1) {
                    JOptionPane.showMessageDialog(null, "File Already Exist, Rename or Choose another!");
                    destination = "";
                    path = "";
                } else {
                    icon2.setIcon(ResizeImage(path, null, icon2));
                    remove.setEnabled(true);
                    select.setEnabled(false);
                }
            } catch (Exception ex) {
                System.out.println("File Error!");
            }
        }
    }//GEN-LAST:event_selectActionPerformed

    private void idActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_idActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_idActionPerformed

    private void removeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_removeActionPerformed
        destination = "";
        icon2.setIcon(null);
        path = "";
        select.setEnabled(true);
        remove.setEnabled(false);
    }//GEN-LAST:event_removeActionPerformed

    private void jButton25ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton25ActionPerformed
        try {
            addProduct();
        } catch (NoSuchAlgorithmException ex) {
            JOptionPane.showMessageDialog(null, "Error" + ex.getMessage());
        }
    }//GEN-LAST:event_jButton25ActionPerformed

    private void jButton22ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton22ActionPerformed
        jTabbedPane1.setSelectedIndex(1);
    }//GEN-LAST:event_jButton22ActionPerformed

    private void jButton24ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton24ActionPerformed
        JFileChooser fileChooser = new JFileChooser();
        int returnValue = fileChooser.showOpenDialog(null);
        if (returnValue == JFileChooser.APPROVE_OPTION) {
            try {
                selectedFile = fileChooser.getSelectedFile();
                destination = "src/ProductsImage/" + selectedFile.getName();
                path = selectedFile.getAbsolutePath();

                if (FileExistenceChecker(path) == 1) {
                    JOptionPane.showMessageDialog(null, "File Already Exist, Rename or Choose another!");
                    destination = "";
                    path = "";
                } else {
                    icon1.setIcon(ResizeImage(path, null, icon1));
                    remove.setEnabled(true);
                    select.setEnabled(false);
                }
            } catch (Exception ex) {
                System.out.println("File Error!");
            }
        }
    }//GEN-LAST:event_jButton24ActionPerformed

    private void pname1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_pname1ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_pname1ActionPerformed

    private void jButton21ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton21ActionPerformed
        destination = "";
        icon1.setIcon(null);
        path = "";
        jButton24.setEnabled(true);
        jButton21.setEnabled(false);
    }//GEN-LAST:event_jButton21ActionPerformed

    private void jButton4ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton4ActionPerformed
        jTabbedPane1.setSelectedIndex(3);
    }//GEN-LAST:event_jButton4ActionPerformed

    private void jButton17ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton17ActionPerformed
        jTabbedPane1.setSelectedIndex(2);
    }//GEN-LAST:event_jButton17ActionPerformed

    private void jButton16ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton16ActionPerformed
        jTabbedPane1.setSelectedIndex(4);
    }//GEN-LAST:event_jButton16ActionPerformed

    private void jButton10ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton10ActionPerformed
        jTabbedPane1.setSelectedIndex(0);
    }//GEN-LAST:event_jButton10ActionPerformed

    private void jButton3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton3ActionPerformed
        jTabbedPane1.setSelectedIndex(5);
    }//GEN-LAST:event_jButton3ActionPerformed

    private void jButton6ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton6ActionPerformed
        MessageFormat header = new MessageFormat("Total Products Registered Reports");
        MessageFormat footer = new MessageFormat("Page{0,number,integer}");
        try {
            productsTB.print(JTable.PrintMode.FIT_WIDTH, header, footer);
        } catch (PrinterException er) {
            System.out.println("" + er.getMessage());
        }
    }//GEN-LAST:event_jButton6ActionPerformed

    private void productsTBMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_productsTBMouseClicked
        int rowIndex = productsTB.getSelectedRow();
        if (rowIndex < 0) {
            JOptionPane.showMessageDialog(null, "PLEASE SELECT AN INDEX!");
        } else {
            try {
                TableModel tbl = productsTB.getModel();
                ResultSet rs = new DBConnector().getData("select * from products where p_id = '" + tbl.getValueAt(rowIndex, 0) + "'");
                if (rs.next()) {
                    id.setText("" + rs.getString("p_id"));
                    pn.setText("" + rs.getString("p_name"));
                    pp.setText("" + rs.getString("p_price"));
                    ps.setText("" + rs.getString("p_stocks"));
                    pstats.setSelectedItem("" + rs.getString("p_status"));
                    icon2.setIcon(ResizeImage(rs.getString("p_image"), null, icon2));
                    oldPath = rs.getString("p_image");
                    path = rs.getString("p_image");

                    if (rs.getString("p_image") != null) {
                        select.setEnabled(false);
                        remove.setEnabled(true);
                    } else {
                        select.setEnabled(true);
                        remove.setEnabled(false);
                    }

                }
            } catch (SQLException er) {
                System.out.println("ERROR: " + er.getMessage());
            }
        }
    }//GEN-LAST:event_productsTBMouseClicked

    private void jButton11ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton11ActionPerformed
        MessageFormat header = new MessageFormat("Total Accounts Registered Reports");
        MessageFormat footer = new MessageFormat("Page{0,number,integer}");
        try {
            usersTB.print(JTable.PrintMode.FIT_WIDTH, header, footer);
        } catch (PrinterException er) {
            System.out.println("" + er.getMessage());
        }
    }//GEN-LAST:event_jButton11ActionPerformed

    private void jButton13ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton13ActionPerformed
        jTabbedPane1.setSelectedIndex(1);
        select.setEnabled(false);
        remove.setEnabled(false);
    }//GEN-LAST:event_jButton13ActionPerformed

    private void jButton2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton2ActionPerformed
        new createAccounts().setVisible(true);
        dispose();
    }//GEN-LAST:event_jButton2ActionPerformed

    private void jButton7ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton7ActionPerformed
        new pendingAccounts().setVisible(true);
        dispose();
    }//GEN-LAST:event_jButton7ActionPerformed

    private void jButton9ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton9ActionPerformed
        try {
            Session sess = Session.getInstance();
            if (sess == null || sess.getId() == null) {
                JOptionPane.showMessageDialog(null, "Please Login First!");
                LoginDashboard ld = new LoginDashboard();
                ld.setVisible(true);
                dispose();
            }

            String query = "SELECT * FROM users WHERE id = ?";
            try (PreparedStatement pstmt = new DBConnector().getConnection().prepareStatement(query)) {
                pstmt.setString(1, sess.getId());
                try (ResultSet rs = pstmt.executeQuery()) {
                    if (rs.next()) {
                        editAccount ea = new editAccount();
                        ea.id.setText(rs.getString("id"));
                        ea.email.setText(rs.getString("email"));
                        ea.username.setText(rs.getString("username"));
                        ea.contact.setText(rs.getString("contact"));
                        ea.status.setSelectedItem(rs.getString("status"));
                        ea.type.setSelectedItem(rs.getString("type"));
                        String imagePath = rs.getString("Image");

                        SwingUtilities.invokeLater(() -> {
                            ea.setVisible(true);
                            dispose();
                        });

                        if (imagePath != null && !imagePath.isEmpty()) {
                            ea.icon1.setIcon(ResizeImage(imagePath, null, ea.icon1));
                            ea.oldPath = imagePath;
                            ea.path = imagePath;
                            ea.destination = imagePath;
                            select.setEnabled(false);
                            remove.setEnabled(true);
                        } else {
                            select.setEnabled(true);
                            remove.setEnabled(false);
                        }
                    } else {
                        System.out.println("No data found for id: " + sess.getId());
                    }
                }
            }
        } catch (SQLException er) {
            System.out.println("ERROR: " + er.getMessage());
        } catch (Exception e) {
            System.out.println("Unexpected ERROR: " + e.getMessage());
        }
    }//GEN-LAST:event_jButton9ActionPerformed

    private void jButton5ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton5ActionPerformed
        try {
            Session sess = Session.getInstance();
            if (sess == null || sess.getId() == null) {
                JOptionPane.showMessageDialog(null, "Please Login First!");
                LoginDashboard ld = new LoginDashboard();
                ld.setVisible(true);
                dispose();
            }

            String query = "SELECT * FROM users WHERE id = ?";
            try (PreparedStatement pstmt = new DBConnector().getConnection().prepareStatement(query)) {
                pstmt.setString(1, sess.getId());
                try (ResultSet rs = pstmt.executeQuery()) {
                    if (rs.next()) {
                        myAccount ma = new myAccount();
                        ma.id.setText(rs.getString("id"));
                        ma.email.setText(rs.getString("email"));
                        ma.username.setText(rs.getString("username"));
                        ma.contact.setText(rs.getString("contact"));
                        ma.status.setSelectedItem(rs.getString("status"));
                        ma.type.setSelectedItem(rs.getString("type"));
                        String imagePath = rs.getString("Image");

                        SwingUtilities.invokeLater(() -> {
                            ma.setVisible(true);
                            dispose();
                        });

                        if (imagePath != null && !imagePath.isEmpty()) {
                            ma.imagee.setIcon(ResizeImage(imagePath, null, ma.imagee));
                            ma.oldPath = imagePath;
                            ma.path = imagePath;
                            ma.destination = imagePath;
                            select.setEnabled(false);
                            remove.setEnabled(true);
                        } else {
                            select.setEnabled(true);
                            remove.setEnabled(false);
                        }
                    } else {
                        System.out.println("No data found for id: " + sess.getId());
                    }
                }
            }
        } catch (SQLException er) {
            System.out.println("ERROR: " + er.getMessage());
        } catch (Exception e) {
            System.out.println("Unexpected ERROR: " + e.getMessage());
        }
    }//GEN-LAST:event_jButton5ActionPerformed

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
        LoginDashboard ld = new LoginDashboard();
        ld.setVisible(true);
        this.dispose();
    }//GEN-LAST:event_jButton1ActionPerformed

    private void jButton34ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton34ActionPerformed
        MessageFormat header = new MessageFormat("Total Ongoing Delivery Reports");
        MessageFormat footer = new MessageFormat("Page{0,number,integer}");
        try {
            orders1.print(JTable.PrintMode.FIT_WIDTH, header, footer);
        } catch (PrinterException er) {
            System.out.println("" + er.getMessage());
        }
    }//GEN-LAST:event_jButton34ActionPerformed

    private void jButton37ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton37ActionPerformed
        jTabbedPane1.setSelectedIndex(1);
    }//GEN-LAST:event_jButton37ActionPerformed

    private void jButton38ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton38ActionPerformed
        MessageFormat header = new MessageFormat("Total Success Delivery Reports");
        MessageFormat footer = new MessageFormat("Page{0,number,integer}");
        try {
            delivered.print(JTable.PrintMode.FIT_WIDTH, header, footer);
        } catch (PrinterException er) {
            System.out.println("" + er.getMessage());
        }
    }//GEN-LAST:event_jButton38ActionPerformed

    private void jButton8ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton8ActionPerformed
        jTabbedPane1.setSelectedIndex(6);
    }//GEN-LAST:event_jButton8ActionPerformed

    private void jButton32ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton32ActionPerformed
        MessageFormat header = new MessageFormat("Total Pending Orders Reports");
        MessageFormat footer = new MessageFormat("Page{0,number,integer}");
        try {
            pendingOrders.print(JTable.PrintMode.FIT_WIDTH, header, footer);
        } catch (PrinterException er) {
            System.out.println("" + er.getMessage());
        }
    }//GEN-LAST:event_jButton32ActionPerformed

    public static void main(String args[]) {

        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new AdminDashboard().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel aname;
    private javax.swing.JTable delivered;
    private javax.swing.JLabel icon1;
    private javax.swing.JLabel icon2;
    private javax.swing.JTextField id;
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton10;
    private javax.swing.JButton jButton11;
    private javax.swing.JButton jButton13;
    private javax.swing.JButton jButton16;
    private javax.swing.JButton jButton17;
    private javax.swing.JButton jButton2;
    private javax.swing.JButton jButton21;
    private javax.swing.JButton jButton22;
    private javax.swing.JButton jButton24;
    private javax.swing.JButton jButton25;
    private javax.swing.JButton jButton27;
    private javax.swing.JButton jButton28;
    private javax.swing.JButton jButton29;
    private javax.swing.JButton jButton3;
    private javax.swing.JButton jButton30;
    private javax.swing.JButton jButton31;
    private javax.swing.JButton jButton32;
    private javax.swing.JButton jButton33;
    private javax.swing.JButton jButton34;
    private javax.swing.JButton jButton35;
    private javax.swing.JButton jButton37;
    private javax.swing.JButton jButton38;
    private javax.swing.JButton jButton4;
    private javax.swing.JButton jButton5;
    private javax.swing.JButton jButton6;
    private javax.swing.JButton jButton7;
    private javax.swing.JButton jButton8;
    private javax.swing.JButton jButton9;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel13;
    private javax.swing.JLabel jLabel14;
    private javax.swing.JLabel jLabel15;
    private javax.swing.JLabel jLabel18;
    private javax.swing.JLabel jLabel19;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel20;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel10;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanel5;
    private javax.swing.JPanel jPanel6;
    private javax.swing.JPanel jPanel7;
    private javax.swing.JPanel jPanel8;
    private javax.swing.JPanel jPanel9;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JScrollPane jScrollPane4;
    private javax.swing.JScrollPane jScrollPane5;
    private javax.swing.JTabbedPane jTabbedPane1;
    private javax.swing.JTable orders1;
    private javax.swing.JTable pendingOrders;
    private javax.swing.JTextField pn;
    private javax.swing.JTextField pname1;
    private javax.swing.JTextField pp;
    private javax.swing.JTextField pprice1;
    private javax.swing.JTable productsTB;
    private javax.swing.JTextField ps;
    private javax.swing.JComboBox<String> pstats;
    private javax.swing.JComboBox<String> pstatus1;
    private javax.swing.JTextField pstocks1;
    private javax.swing.JButton remove;
    private javax.swing.JButton select;
    private javax.swing.JTable usersTB;
    // End of variables declaration//GEN-END:variables
}
