package gui;

import model.Booking;
import model.Hall;

import javax.swing.*;
import java.awt.*;

/** Minimal JFrame receipt shown to the customer right after payment, as required by the brief. */
public class ReceiptFrame extends JFrame {

    public ReceiptFrame(Booking booking, Hall hall, String customerName) {
        setTitle("Booking Receipt");
        setSize(360, 320);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        JTextArea area = new JTextArea();
        area.setEditable(false);
        area.setFont(new Font("Monospaced", Font.PLAIN, 12));
        area.setText(
                "======= HALL SYMPHONY INC. =======\n" +
                "            RECEIPT\n" +
                "===================================\n" +
                "Booking ID   : " + booking.getBookingId() + "\n" +
                "Customer     : " + customerName + "\n" +
                "Hall         : " + hall.getHallName() + " (" + hall.getHallType() + ")\n" +
                "Date/Time    : " + booking.getStartDateTime() + "\n" +
                "              to " + booking.getEndDateTime() + "\n" +
                "Rate/Hour    : RM " + hall.getRatePerHour() + "\n" +
                "-----------------------------------\n" +
                "TOTAL PAID   : RM " + String.format("%.2f", booking.getTotalCost()) + "\n" +
                "Status       : " + booking.getStatus() + "\n" +
                "Booked On    : " + booking.getBookedOn() + "\n" +
                "===================================\n" +
                "  Thank you for booking with us!\n"
        );

        add(new JScrollPane(area), BorderLayout.CENTER);

        JButton closeButton = new JButton("Close");
        closeButton.addActionListener(e -> dispose());
        JPanel bottom = new JPanel();
        bottom.add(closeButton);
        add(bottom, BorderLayout.SOUTH);
    }
}
