import java.awt.BorderLayout;
import java.awt.event.*;
import java.io.IOException;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;

public class LoginWindow extends JFrame{
    JTextField loginField = new JTextField();
    JPasswordField passwordField  = new JPasswordField();
    JButton logiButton = new JButton("Login");
    private final ChatClient client;

    public LoginWindow(){
        super("Login");

        this.client = new ChatClient("localhost", 8818);
        client.connect();
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.add(loginField);
        p.add(passwordField);
        p.add(logiButton);

        logiButton.addActionListener(new ActionListener(){
            @Override
            public void actionPerformed(ActionEvent e){
                doLogin();
            }
        });
        getContentPane().add(p,BorderLayout.CENTER);
        pack();
        setVisible(true);

    }
    private void doLogin(){
        String login = loginField.getText();
        String password = passwordField.getText();
        UserListPane userListPane = new UserListPane(client);
        JFrame frame = new JFrame("User List");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(400,600);

        frame.getContentPane().add(userListPane, BorderLayout.CENTER);
        frame.setVisible(false);
        try{
            if(client.login(login, password)){
                frame.setVisible(true);
                setVisible(false);

            }else{
                JOptionPane.showMessageDialog(this, "Invalid login/password");
            }
        }catch(IOException e){
            e.printStackTrace();
        }
    }
    public static void main(String[] args) {
        LoginWindow loginWindow = new LoginWindow();
        loginWindow.setVisible(true);

    }
}