/**
 *
 */
package com.antoniovm.lowtency.net;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;

import com.antoniovm.lowtency.core.StreamHeader;
import com.antoniovm.lowtency.event.ConnectionListener;

import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Enumeration;

/**
 * @author Antonio Vicente Martin
 */
public class NetworkServer implements Runnable {

    private ServerSocket serverSocket;
    private DatagramSocket datagramSocket;
    private DatagramPacket datagramPacket;
    private ArrayList<Socket> clients;
    private boolean running;
    private Thread thread;
    private StreamHeader streamHeader;
    private ArrayList<ConnectionListener> connectionListeners;

    /**
     *
     */
    public NetworkServer(StreamHeader streamHeader) {
        this.datagramPacket = new DatagramPacket(new byte[0], 0);
        this.clients = new ArrayList<>();
        this.streamHeader = streamHeader;
        this.connectionListeners = new ArrayList<>();

        try {
            this.serverSocket = new ServerSocket();
            this.serverSocket.setReuseAddress(true);
            this.serverSocket.bind(new InetSocketAddress(0));
            this.datagramSocket = new DatagramSocket();
            this.serverSocket.setReuseAddress(true);
        } catch (SocketException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     *
     */
    public void addConnectionListener(ConnectionListener connectionListener) {
        connectionListeners.add(connectionListener);
    }

    /**
     * Sends UDP package to all clients
     *
     * @param data
     */
    public void sendBroadcast(byte[] data) {
        datagramPacket.setData(data);

        for (int i = 0; i < clients.size(); i++) {
            datagramPacket.setSocketAddress(clients.get(i).getRemoteSocketAddress());

            try {
                datagramSocket.send(datagramPacket);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Waits for a new connection request, adds the new socket to the clients
     * list and returns it
     *
     * @return neClient
     */
    public Socket accept() {
        Socket newClient = null;
        try {
            newClient = serverSocket.accept();
            if (clients.size() < 1) {
                fireFirstClientConnection();
            }
            clients.add(newClient);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return newClient;

    }

    /**
     * Sends the stream header to the specified socket
     *
     * @param streamHeader
     */
    public void sendHeader(Socket socket, StreamHeader streamHeader) {
        OutputStream os;
        try {
            os = socket.getOutputStream();
            os.write(streamHeader.getSerialized());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Waits for a new client and sends the stream header to it
     */
    public void waitForNewClient(StreamHeader streamHeader) {
        Socket newClient = accept();
        sendHeader(newClient, streamHeader);
    }

    /**
     * @return
     */
    public int getPort() {
        return serverSocket.getLocalPort();
    }

    /**
     * @return Host IP
     */
    public String getIp() {

        try {
            for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements(); ) {
                NetworkInterface intf = en.nextElement();
                for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements(); ) {
                    InetAddress inetAddress = enumIpAddr.nextElement();
                    if (!inetAddress.isLoopbackAddress()) {
                        String ip = inetAddress.getHostAddress();
                        int tokenIndex = ip.indexOf('%');
                        if (tokenIndex > 0) {
                            // IPv6
                            ip = "[" + ip.substring(0, tokenIndex) + "]";
                            continue;
                        }
                        return ip;
                    }
                }
            }
        } catch (SocketException e) {
            e.printStackTrace();
        }
        return null;

    }

    /**
     *
     */
    public static boolean isDeviceConnectedToAWiFiNetwork(Context context) {
        ConnectivityManager conMgr = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo i = conMgr.getActiveNetworkInfo();

        // Not null when there is a wifi connection
        if (i == null) {

            WifiManager wifiMgr = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);

            Method method = null;
            try {
                method = wifiMgr.getClass().getDeclaredMethod("isWifiApEnabled");
                method.setAccessible(true);
                return (Boolean) method.invoke(wifiMgr);
            } catch (NoSuchMethodException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            }

        }

        return i.isConnected() && i.isAvailable();

    }

    /**
     * @param running the running to set
     */
    public void setRunning(boolean running) {
        this.running = running;
    }

    /**
     * Starts a new Thread
     *
     * @return true if it could be started, false otherwise
     */
    public boolean startThread() {
        if (this.thread == null) {
            this.thread = new Thread(this);
            this.thread.start();

            return true;
        }

        return false;
    }

    /**
     * Makes a request to the thread to stop
     */
    public void stop() {
        running = false;
    }

    /*
     * (non-Javadoc)
     *
     * @see java.lang.Runnable#run()
     */
    @Override
    public void run() {
        running = true;
        while (running) {
            waitForNewClient(streamHeader);
        }

        this.thread = null;
    }

    /**
     *
     */
    private void fireFirstClientConnection() {
        for (int i = 0; i < connectionListeners.size(); i++) {
            connectionListeners.get(i).onFirstClientConnection();
        }
    }

    /**
     *
     */
    private void fireLastClientDisconnection() {
        for (int i = 0; i < connectionListeners.size(); i++) {
            connectionListeners.get(i).onLastClientDisconnection();
        }
    }

    /**
     *
     */
    public void removeClient(Socket socket) {
        clients.remove(socket);
        if (clients.size() < 1) {
            fireLastClientDisconnection();
        }
    }
}
