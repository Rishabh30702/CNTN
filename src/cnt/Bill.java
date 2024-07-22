/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JFrame.java to edit this template
 */
package cnt;

import java.awt.Font;
import static java.awt.Frame.MAXIMIZED_BOTH;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.awt.print.PageFormat;
import java.awt.print.Printable;
import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;
import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JOptionPane;
import javax.swing.table.DefaultTableModel;

/**
 *
 * @author risha
 */
public class Bill extends javax.swing.JFrame {

    /**
     * Creates new form Bill
     */
    public Bill() {
        this.setUndecorated(false);
        initComponents();
        this.setExtendedState(MAXIMIZED_BOTH);
        billnumberfield.setEnabled(false);
        billnumberfield.setText(String.valueOf(Math.abs(new Random().nextInt()) + 1));
        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        GraphicsDevice gd = ge.getDefaultScreenDevice();
        Rectangle screenBounds = gd.getDefaultConfiguration().getBounds();

        // Load image
        URL imageURL = Bill.class.getResource("/images/back6.jpg");
        if (imageURL != null) {
            ImageIcon icon = new ImageIcon(imageURL);

            // Scale the image to fit the JLabel size (which should be the frame size)
            Image scaledImage = icon.getImage().getScaledInstance(screenBounds.width, screenBounds.height, Image.SCALE_SMOOTH);
            jLabel5.setIcon(new ImageIcon(scaledImage));

            // Set layout to null to manually position and size the JLabel
            jLabel5.setBounds(0, 0, screenBounds.width, screenBounds.height); // Set JLabel bounds to match frame

        } else {
            System.err.println("Resource not found");
        }
    }

    public void printTable() {
        PrinterJob printerJob = PrinterJob.getPrinterJob();
        printerJob.setPrintable(new Printable() {
            @Override
            public int print(Graphics graphics, PageFormat pageFormat, int pageIndex) throws PrinterException {
                DefaultTableModel model = (DefaultTableModel) jTable1.getModel();
                Font font = graphics.getFont().deriveFont(Font.PLAIN, 12);
                Font headerFont = font.deriveFont(Font.BOLD, 14);
                int rowsPerPage = (int) (pageFormat.getImageableHeight() / graphics.getFontMetrics().getHeight());
                int totalRows = model.getRowCount();
                int startRow = pageIndex * rowsPerPage;
                int endRow = Math.min(startRow + rowsPerPage, totalRows);

                if (startRow < totalRows) {
                    Graphics2D g2d = (Graphics2D) graphics;
                    g2d.translate(pageFormat.getImageableX(), pageFormat.getImageableY());

                    // Print company information 
                    g2d.setFont(font);
                    g2d.drawString("VALLIENTO", 50, 20);
                    g2d.drawString("Address Line 1", 50, 40);
                    g2d.drawString("Phone: 123-456-7890", 50, 60);

                    // Draw a line
                    g2d.drawLine(50, 70, (int) pageFormat.getImageableWidth() - 50, 70);

                    // Try loading logo
                    try {
                        BufferedImage logo = ImageIO.read(new File("D:\\CNTN\\src\\images\\back.jpeg"));

                        // Calculate the aspect ratio of the original image
                        double imageAspectRatio = (double) logo.getWidth() / (double) logo.getHeight();

                        // Determine the new dimensions that fit within 150*70 while maintaining aspect ratio
                        int newWidth;
                        int newHeight;
                        if (imageAspectRatio > 1) { // Wider than tall
                            newWidth = 150;
                            newHeight = (int) (newWidth / imageAspectRatio);
                        } else { // Taller than wide or square
                            newHeight = 70;
                            newWidth = (int) (newHeight * imageAspectRatio);
                        }

                        // Get a resized version of the image with the new dimensions
                        BufferedImage resizedLogo = new BufferedImage(newWidth, newHeight, BufferedImage.TYPE_INT_ARGB); // Adjust type as needed
                        Graphics2D g2dTemp = resizedLogo.createGraphics();
                        g2dTemp.drawImage(logo.getSubimage(0, 0, logo.getWidth(), logo.getHeight()), 0, 0, newWidth, newHeight, null);
                        g2dTemp.dispose();
                        // Calculate x-coordinate for rightmost position
                        int logoWidth = resizedLogo.getWidth();
                        int pageWidth = (int) pageFormat.getImageableWidth();
                        int logoX = pageWidth - logoWidth;

                        // Draw the logo at the calculated position and top of the page (y-coordinate can be adjusted)
                        g2d.drawImage(resizedLogo, logoX, 0, null);

                    } catch (IOException ex) {
                        // Handle logo loading error (optional: print a message)
                    }

                    // Print headers
                    g2d.setFont(headerFont);
                    String header = String.format("%-30s %5s %10s", "Item name", "qty", "Price");
                    g2d.drawString(header, 100, 150);

                    // Draw a line
                    g2d.drawLine(50, 160, (int) pageFormat.getImageableWidth() - 50, 160);

                    // Print bill items
                    g2d.setFont(font);
                    int y = 180;
                    for (int row = startRow; row < endRow; row++) {
                        String itemName = (String) model.getValueAt(row, 0);
                        String qty = (String) model.getValueAt(row, 1);
                        String price = (String) model.getValueAt(row, 2);
                        // Format line with right-aligned price
                        String line = String.format("%-30s %5s %10s", itemName, qty, String.format("%,.2f", Double.parseDouble(price)));
                        g2d.drawString(line, 100, y);

                        y += 20; // Adjust vertical spacing between items
                    }

                    // Print total (assuming a 'total' column exists)
                    double total = calculateTotal();

                    String totalStr = String.format("Total: %,.2f", total);
                    g2d.drawString(totalStr, (int) pageFormat.getImageableWidth() - 200, y + 20);

                    // Print date and time
                    Date date = new Date();
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    String formattedDate = sdf.format(date);
                    g2d.drawString("Printed on: " + formattedDate, 50, y + 40);

                    return Printable.PAGE_EXISTS;
                } else {
                    return Printable.NO_SUCH_PAGE;
                }
            }
        });

        // Show print dialog and print if user confirms
        if (printerJob.printDialog()) {
            try {
                printerJob.print();
            } catch (PrinterException ex) {
                ex.printStackTrace();
                // Handle printing error
            }
        }
    }

  private double calculateTotal() {
        double total = 0.0;

        // Get the table model
        DefaultTableModel model = (DefaultTableModel) jTable1.getModel();

        // Iterate through rows
        for (int row = 0; row < model.getRowCount(); row++) {
            // Get quantity and price from the table model
            String qtyStr = (String) model.getValueAt(row, 2); // Assuming qty is in column index 2
            String priceStr = (String) model.getValueAt(row, 1); // Assuming price is in column index 1

            // Parse quantity and price as double
            try {
                double qty = Double.parseDouble(qtyStr);
                double price = Double.parseDouble(priceStr);

                // Calculate subtotal for current row
                double subtotal = qty * price;
                model.setValueAt(subtotal, row, 3);

                // Add subtotal to total
                total += subtotal;
            } catch (NumberFormatException e) {
                e.printStackTrace(); // Handle parsing errors
            }
        }

        return total;
    }


    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        billnumberfield = new javax.swing.JTextField();
        jLabel3 = new javax.swing.JLabel();
        itemnamefield = new javax.swing.JTextField();
        jLabel4 = new javax.swing.JLabel();
        returnAmount = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        jTable1 = new javax.swing.JTable();
        jLabel7 = new javax.swing.JLabel();
        jSeparator1 = new javax.swing.JSeparator();
        jLabel8 = new javax.swing.JLabel();
        itemqtyfield = new javax.swing.JTextField();
        jLabel6 = new javax.swing.JLabel();
        cashfield = new javax.swing.JTextField();
        customernamefield = new javax.swing.JTextField();
        jSeparator2 = new javax.swing.JSeparator();
        jLabel9 = new javax.swing.JLabel();
        jLabel10 = new javax.swing.JLabel();
        returnlabel = new javax.swing.JLabel();
        unitBox = new javax.swing.JComboBox<>();
        jLabel12 = new javax.swing.JLabel();
        totallabel = new javax.swing.JLabel();
        addBtn = new javax.swing.JButton();
        printbtn = new javax.swing.JButton();
        exitbtn = new javax.swing.JButton();
        jLabel5 = new javax.swing.JLabel();
        jLabel11 = new javax.swing.JLabel();
        pricefield1 = new javax.swing.JTextField();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setBackground(new java.awt.Color(255, 153, 153));
        getContentPane().setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jLabel1.setFont(new java.awt.Font("Tahoma", 1, 18)); // NOI18N
        jLabel1.setForeground(new java.awt.Color(204, 204, 204));
        jLabel1.setText("Bill No");
        getContentPane().add(jLabel1, new org.netbeans.lib.awtextra.AbsoluteConstraints(130, 120, 70, 30));

        jLabel2.setFont(new java.awt.Font("Tahoma", 1, 18)); // NOI18N
        jLabel2.setForeground(new java.awt.Color(204, 204, 204));
        jLabel2.setText("Customer Name");
        getContentPane().add(jLabel2, new org.netbeans.lib.awtextra.AbsoluteConstraints(580, 120, 160, 30));

        billnumberfield.setFont(new java.awt.Font("Tahoma", 1, 14)); // NOI18N
        billnumberfield.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                billnumberfieldActionPerformed(evt);
            }
        });
        getContentPane().add(billnumberfield, new org.netbeans.lib.awtextra.AbsoluteConstraints(210, 120, 210, 30));

        jLabel3.setFont(new java.awt.Font("Tahoma", 1, 18)); // NOI18N
        jLabel3.setForeground(new java.awt.Color(204, 204, 204));
        jLabel3.setText("Cash");
        getContentPane().add(jLabel3, new org.netbeans.lib.awtextra.AbsoluteConstraints(110, 320, -1, 30));

        itemnamefield.setFont(new java.awt.Font("Tahoma", 1, 14)); // NOI18N
        getContentPane().add(itemnamefield, new org.netbeans.lib.awtextra.AbsoluteConstraints(230, 260, 180, 30));

        jLabel4.setFont(new java.awt.Font("Tahoma", 1, 18)); // NOI18N
        jLabel4.setForeground(new java.awt.Color(204, 204, 204));
        jLabel4.setText("Rate");
        getContentPane().add(jLabel4, new org.netbeans.lib.awtextra.AbsoluteConstraints(960, 260, -1, 30));

        returnAmount.setFont(new java.awt.Font("Tahoma", 1, 14)); // NOI18N
        returnAmount.setForeground(new java.awt.Color(255, 51, 51));
        returnAmount.setText("0.0");
        getContentPane().add(returnAmount, new org.netbeans.lib.awtextra.AbsoluteConstraints(520, 320, 30, 30));

        jTable1.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "Name", "Qty", "Rate Per Qty", "Total"
            }
        ));
        jScrollPane1.setViewportView(jTable1);
        if (jTable1.getColumnModel().getColumnCount() > 0) {
            jTable1.getColumnModel().getColumn(3).setHeaderValue("Total");
        }

        getContentPane().add(jScrollPane1, new org.netbeans.lib.awtextra.AbsoluteConstraints(50, 480, 740, 270));

        jLabel7.setFont(new java.awt.Font("Tahoma", 1, 24)); // NOI18N
        jLabel7.setForeground(new java.awt.Color(255, 255, 255));
        jLabel7.setText("Summary");
        getContentPane().add(jLabel7, new org.netbeans.lib.awtextra.AbsoluteConstraints(50, 440, -1, -1));
        getContentPane().add(jSeparator1, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 170, 1280, 10));

        jLabel8.setFont(new java.awt.Font("Tahoma", 1, 24)); // NOI18N
        jLabel8.setForeground(new java.awt.Color(255, 255, 255));
        jLabel8.setText("Customer Details");
        getContentPane().add(jLabel8, new org.netbeans.lib.awtextra.AbsoluteConstraints(50, 50, -1, -1));

        itemqtyfield.setFont(new java.awt.Font("Tahoma", 1, 14)); // NOI18N
        itemqtyfield.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                itemqtyfieldActionPerformed(evt);
            }
        });
        getContentPane().add(itemqtyfield, new org.netbeans.lib.awtextra.AbsoluteConstraints(730, 260, 120, 30));

        jLabel6.setFont(new java.awt.Font("Tahoma", 1, 18)); // NOI18N
        jLabel6.setForeground(new java.awt.Color(204, 204, 204));
        jLabel6.setText("Item Quantity");
        getContentPane().add(jLabel6, new org.netbeans.lib.awtextra.AbsoluteConstraints(570, 260, -1, 30));

        cashfield.setFont(new java.awt.Font("Tahoma", 1, 14)); // NOI18N
        getContentPane().add(cashfield, new org.netbeans.lib.awtextra.AbsoluteConstraints(230, 320, 130, 30));

        customernamefield.setFont(new java.awt.Font("Tahoma", 1, 14)); // NOI18N
        customernamefield.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                customernamefieldActionPerformed(evt);
            }
        });
        getContentPane().add(customernamefield, new org.netbeans.lib.awtextra.AbsoluteConstraints(740, 120, 210, 30));
        getContentPane().add(jSeparator2, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 430, 1280, 10));

        jLabel9.setFont(new java.awt.Font("Tahoma", 1, 24)); // NOI18N
        jLabel9.setForeground(new java.awt.Color(255, 255, 255));
        jLabel9.setText("Item Details");
        getContentPane().add(jLabel9, new org.netbeans.lib.awtextra.AbsoluteConstraints(50, 200, -1, -1));

        jLabel10.setFont(new java.awt.Font("Tahoma", 1, 18)); // NOI18N
        jLabel10.setForeground(new java.awt.Color(204, 204, 204));
        jLabel10.setText("Item Name");
        getContentPane().add(jLabel10, new org.netbeans.lib.awtextra.AbsoluteConstraints(110, 260, -1, 30));

        returnlabel.setFont(new java.awt.Font("Tahoma", 1, 18)); // NOI18N
        returnlabel.setForeground(new java.awt.Color(204, 204, 204));
        returnlabel.setText("Return");
        getContentPane().add(returnlabel, new org.netbeans.lib.awtextra.AbsoluteConstraints(430, 320, -1, 30));

        unitBox.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "UNIT", "KG", "G", "COUNT" }));
        getContentPane().add(unitBox, new org.netbeans.lib.awtextra.AbsoluteConstraints(870, 260, -1, -1));

        jLabel12.setFont(new java.awt.Font("Tahoma", 1, 18)); // NOI18N
        jLabel12.setForeground(new java.awt.Color(204, 204, 204));
        jLabel12.setText("Total");
        getContentPane().add(jLabel12, new org.netbeans.lib.awtextra.AbsoluteConstraints(110, 370, -1, 30));

        totallabel.setFont(new java.awt.Font("Tahoma", 1, 14)); // NOI18N
        totallabel.setForeground(new java.awt.Color(255, 51, 51));
        totallabel.setText("0.0");
        getContentPane().add(totallabel, new org.netbeans.lib.awtextra.AbsoluteConstraints(230, 380, -1, -1));

        addBtn.setFont(new java.awt.Font("Tahoma", 1, 18)); // NOI18N
        addBtn.setForeground(new java.awt.Color(0, 204, 204));
        addBtn.setText("ADD");
        addBtn.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        addBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addBtnActionPerformed(evt);
            }
        });
        getContentPane().add(addBtn, new org.netbeans.lib.awtextra.AbsoluteConstraints(420, 390, 80, 40));

        printbtn.setFont(new java.awt.Font("Tahoma", 1, 18)); // NOI18N
        printbtn.setForeground(new java.awt.Color(0, 204, 204));
        printbtn.setText("Print");
        printbtn.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        printbtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                printbtnActionPerformed(evt);
            }
        });
        getContentPane().add(printbtn, new org.netbeans.lib.awtextra.AbsoluteConstraints(910, 620, 80, 40));

        exitbtn.setFont(new java.awt.Font("Tahoma", 1, 14)); // NOI18N
        exitbtn.setForeground(new java.awt.Color(255, 0, 0));
        exitbtn.setText("EXIT");
        exitbtn.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        exitbtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                exitbtnActionPerformed(evt);
            }
        });
        getContentPane().add(exitbtn, new org.netbeans.lib.awtextra.AbsoluteConstraints(1040, 620, 90, 40));
        getContentPane().add(jLabel5, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 0, -1, -1));

        jLabel11.setFont(new java.awt.Font("Tahoma", 1, 18)); // NOI18N
        getContentPane().add(jLabel11, new org.netbeans.lib.awtextra.AbsoluteConstraints(960, 260, -1, 30));

        pricefield1.setFont(new java.awt.Font("Tahoma", 1, 14)); // NOI18N
        getContentPane().add(pricefield1, new org.netbeans.lib.awtextra.AbsoluteConstraints(1020, 260, 130, 30));

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void billnumberfieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_billnumberfieldActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_billnumberfieldActionPerformed

    private void itemqtyfieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_itemqtyfieldActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_itemqtyfieldActionPerformed

    private void customernamefieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_customernamefieldActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_customernamefieldActionPerformed

    private void addBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addBtnActionPerformed
        // TODO add your handling code here:
        String cut_name = customernamefield.getText().toString().trim();
        String billno = billnumberfield.getText().toString().trim();

        String iname = itemnamefield.getText().toString().trim();
        String price = pricefield1.getText().toString().trim();
        String qty = itemqtyfield.getText().toString().trim();
        Object[] rowData = {iname, price, qty};
        DefaultTableModel model = (DefaultTableModel) jTable1.getModel();
        model.addRow(rowData);

       double quantity = Double.parseDouble(itemqtyfield.getText());
    
    // Retrieve selected unit from JComboBox
    String selectedUnit = (String) unitBox.getSelectedItem();
    
    // Define rates (you can fetch these from a database or set them as constants)
    double ratePerGram = 0.05; // Example rate per gram
    double ratePerKg = 50.0;   // Example rate per kilogram
    
    // Calculate total price based on unit selected
    double totalPrice = 0.0;
    if ("G".equals(selectedUnit)) {
        totalPrice = quantity * ratePerGram;
    } else if ("KG".equals(selectedUnit)) {
        totalPrice = quantity * ratePerKg;
    } else {
        // Handle unexpected unit selection
        System.err.println("Unexpected unit selected: " + selectedUnit);
    }
  
        double total = calculateTotal();
        System.out.println("Total Price: " + total);
        String returncash = cashfield.getText().toString().trim();

        totallabel.setText(String.valueOf(total));

        itemnamefield.setText("");
        pricefield1.setText("");
        itemqtyfield.setText("");

    }//GEN-LAST:event_addBtnActionPerformed

    private void printbtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_printbtnActionPerformed
        // TODO add your handling code here:
        printTable();
    }//GEN-LAST:event_printbtnActionPerformed

    private void exitbtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_exitbtnActionPerformed
        // TODO add your handling code here:
        System.exit(0);
    }//GEN-LAST:event_exitbtnActionPerformed

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(Bill.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(Bill.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(Bill.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(Bill.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new Bill().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton addBtn;
    private javax.swing.JTextField billnumberfield;
    private javax.swing.JTextField cashfield;
    private javax.swing.JTextField customernamefield;
    private javax.swing.JButton exitbtn;
    private javax.swing.JTextField itemnamefield;
    private javax.swing.JTextField itemqtyfield;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JSeparator jSeparator2;
    private javax.swing.JTable jTable1;
    private javax.swing.JTextField pricefield1;
    private javax.swing.JButton printbtn;
    private javax.swing.JLabel returnAmount;
    private javax.swing.JLabel returnlabel;
    private javax.swing.JLabel totallabel;
    private javax.swing.JComboBox<String> unitBox;
    // End of variables declaration//GEN-END:variables
}
