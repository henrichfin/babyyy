package AdminDSB;

import Config.DBConnector;
import Config.Session;
import Config.passwordHashing;
import LoginDSB.LoginDashboard;
import java.security.NoSuchAlgorithmException;
import java.sql.ResultSet;
import java.sql.SQLException;
import javax.swing.JOptionPane;
import static javax.swing.JOptionPane.ERROR_MESSAGE;
import static javax.swing.JOptionPane.INFORMATION_MESSAGE;

public class changePassword extends javax.swing.JFrame {

    public changePassword() {
        initComponents();
    }

    private boolean validationChecker() {
        if (newPassword.getText().isEmpty() || oldPassword.getText().isEmpty() || cpassword.getText().isEmpty()) {
            errorMessage("FILL ALL THE REQUIREMENTS!");
            return false;
        } else if (newPassword.getText().length() < 8) {
            errorMessage("PASSWORD MUST ATLEAST 8 CHARACTERS!");
            return false;
        } else if (!newPassword.getText().equals(cpassword.getText())) {
            errorMessage("PASSWORD DO NOT MATCH!");
            return false;
        } else {
            return true;
        }
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

        jPanel2 = new javax.swing.JPanel();
        jPanel1 = new javax.swing.JPanel();
        jLabel15 = new javax.swing.JLabel();
        jPanel3 = new javax.swing.JPanel();
        jButton2 = new javax.swing.JButton();
        jButton1 = new javax.swing.JButton();
        jLabel17 = new javax.swing.JLabel();
        cpassword = new javax.swing.JPasswordField();
        oldPassword = new javax.swing.JPasswordField();
        newPassword = new javax.swing.JPasswordField();
        showPass = new javax.swing.JCheckBox();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setUndecorated(true);
        getContentPane().setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jPanel1.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jLabel15.setFont(new java.awt.Font("Yu Gothic", 1, 18)); // NOI18N
        jLabel15.setText("SEARCH");
        jPanel1.add(jLabel15, new org.netbeans.lib.awtextra.AbsoluteConstraints(840, 90, 90, -1));

        jPanel3.setBackground(new java.awt.Color(255, 249, 239));
        jPanel3.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jButton2.setBackground(new java.awt.Color(255, 249, 239));
        jButton2.setFont(new java.awt.Font("Yu Gothic", 0, 11)); // NOI18N
        jButton2.setText("CHANGE");
        jButton2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton2ActionPerformed(evt);
            }
        });
        jPanel3.add(jButton2, new org.netbeans.lib.awtextra.AbsoluteConstraints(190, 420, 100, -1));

        jButton1.setBackground(new java.awt.Color(255, 249, 239));
        jButton1.setFont(new java.awt.Font("Yu Gothic", 0, 11)); // NOI18N
        jButton1.setText("CANCEL");
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });
        jPanel3.add(jButton1, new org.netbeans.lib.awtextra.AbsoluteConstraints(30, 420, 100, -1));

        jLabel17.setFont(new java.awt.Font("Yu Gothic", 1, 18)); // NOI18N
        jLabel17.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Images/eidt.png"))); // NOI18N
        jLabel17.setText("CHANGE PASS");
        jPanel3.add(jLabel17, new org.netbeans.lib.awtextra.AbsoluteConstraints(70, 50, 190, 40));

        cpassword.setBackground(new java.awt.Color(255, 249, 239));
        cpassword.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        cpassword.setText("CONFIRM PASS");
        jPanel3.add(cpassword, new org.netbeans.lib.awtextra.AbsoluteConstraints(40, 230, 240, 30));

        oldPassword.setBackground(new java.awt.Color(255, 249, 239));
        oldPassword.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        oldPassword.setText("OLD PASSWORD");
        jPanel3.add(oldPassword, new org.netbeans.lib.awtextra.AbsoluteConstraints(40, 130, 240, 30));

        newPassword.setBackground(new java.awt.Color(255, 249, 239));
        newPassword.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        newPassword.setText("NEW PASSWORD");
        jPanel3.add(newPassword, new org.netbeans.lib.awtextra.AbsoluteConstraints(40, 190, 240, 30));

        showPass.setBackground(new java.awt.Color(255, 249, 239));
        showPass.setFont(new java.awt.Font("Yu Gothic", 0, 11)); // NOI18N
        showPass.setText("SHOW PASSWORD");
        showPass.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                showPassActionPerformed(evt);
            }
        });
        jPanel3.add(showPass, new org.netbeans.lib.awtextra.AbsoluteConstraints(160, 270, -1, -1));

        jPanel1.add(jPanel3, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 0, 320, 490));

        getContentPane().add(jPanel1, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 0, 320, 490));

        pack();
        setLocationRelativeTo(null);
    }// </editor-fold>//GEN-END:initComponents

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
        new AdminDashboard().setVisible(true);
        dispose();
    }//GEN-LAST:event_jButton1ActionPerformed

    private void jButton2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton2ActionPerformed
        try {
            if (!validationChecker()) {
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
    }//GEN-LAST:event_jButton2ActionPerformed

    private void showPassActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_showPassActionPerformed
        char echoChar = showPass.isSelected() ? (char) 0 : '*';
        oldPassword.setEchoChar(echoChar);
        newPassword.setEchoChar(echoChar);
        cpassword.setEchoChar(echoChar);
    }//GEN-LAST:event_showPassActionPerformed

    public static void main(String args[]) {
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new changePassword().setVisible(true);
            }
        });
    }


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPasswordField cpassword;
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton2;
    private javax.swing.JLabel jLabel15;
    private javax.swing.JLabel jLabel17;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPasswordField newPassword;
    private javax.swing.JPasswordField oldPassword;
    private javax.swing.JCheckBox showPass;
    // End of variables declaration//GEN-END:variables
}
