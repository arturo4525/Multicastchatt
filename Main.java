import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.MulticastSocket;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.awt.*;

class Main extends JFrame implements ActionListener {

    //Create UI with Jframe
    JPanel basePanel = new JPanel();
    JPanel menuPanel = new JPanel();
    JPanel chatPanel = new JPanel();
    JPanel chatInputPanel = new JPanel();
    JTextArea chat = new JTextArea(20,40);

    //Try get scroll to work.
    JScrollPane scroll = new JScrollPane(chat, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
    JTextField chatInput = new JTextField(40);
    JButton connectButton = new JButton("Connect to chat");

    String userNameInput;
    Boolean connected = false;

    MulticastSocket socket;
    Thread t;
    String message = "";
    String dataToSend = "";
    String ip = "234.235.236.237";
    InetAddress iadr = InetAddress.getByName(ip);
    int port = 12540;

    InetSocketAddress group = new InetSocketAddress(iadr, port);
    NetworkInterface netIf = NetworkInterface.getByName("wlan2");

    public Main() throws UnknownHostException, SocketException, IOException, InterruptedException {

        //Get username input from user
        userNameInput = JOptionPane.showInputDialog(null, "Enter username");
        if (userNameInput == null || userNameInput.length() == 0) {
            System.exit(0);
        }

        //Setup UI layout
        basePanel.setLayout(new BorderLayout());
        menuPanel.setLayout(new BorderLayout());
        
        add(basePanel);
        basePanel.add(menuPanel, BorderLayout.NORTH);
        basePanel.add(chatPanel);
        basePanel.add(chatInputPanel, BorderLayout.SOUTH);
        menuPanel.add(connectButton);
        chatPanel.add(chat);
        chatInputPanel.add(chatInput);

        chat.setEditable(false);
        chatInput.setEditable(false);
        chatInput.addActionListener(this);
        connectButton.addActionListener(this);
        setTitle("Chat " + userNameInput);
        pack();
        setLocation(500,100);
        setVisible(true);
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        //Create new multicastsocket and joingroup.
        socket = new MulticastSocket();
        socket.joinGroup(group, netIf);
    }

    public static void main(String[] args) throws UnknownHostException, SocketException, IOException, InterruptedException {
        Main m = new Main();
    }

    public void actionPerformed(ActionEvent ae) {
        //If user press connect button
        if(ae.getSource() == connectButton) {
            if(!connected) {
                try {
                    //Create new listener, thread and start tread with listner class.
                    Listener li = new Listener(socket, chat);
                    t = new Thread(li);
                    t.start();
                    
                    //Change text on connect button and activate chatinput.
                    connectButton.setText("Disconnet");
                    chatInput.setEditable(true);
                    
                    //Send message "Connected" to chat and set connected to true.
                    sendMessage("Connected");
                    connected = true;
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else {
                //Send message "Disconnected" to chat and set connected to false.
                sendMessage("Disconnected");
                
                //Interrupt thread.
                t.interrupt();

                connected = false;

                //Change text on connect button to connect to chat and set chatinput to false.
                connectButton.setText("Connect to chat");
                chatInput.setEditable(false);
            }
        }

        if(ae.getSource() == chatInput) {
            //Get input from chatinput and send it to sendMessage.
            message = chatInput.getText();
            sendMessage(message);
        }
    }
    
    public void sendMessage(String message) {
        dataToSend = userNameInput + ": " + message;
        //Convert string to byte.
        byte[] data = dataToSend.getBytes();
        DatagramPacket packet = new DatagramPacket(data, data.length, iadr, port);
        try {
            //Send packet and set chatinput to blank.
            socket.send(packet);
            chatInput.setText("");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}