package app.demo.ui.activity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.io.File;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Enumeration;

import app.demo.R;
import app.demo.asynctask.SendImageAsyncTask;
import app.demo.model.ConnectedClient;
import app.demo.ui.adapter.ConnectedClientAdapter;
import app.demo.util.Constants;
import app.demo.util.Utils;

public class ServerActivity extends AppCompatActivity {

    private TextView infoTextView, infoIpTextView, infoPortTextView;
    private Button connectServerAndGetImageButton;

    private RecyclerView connectedClientsList;
    private ConnectedClientAdapter adapter;

    private ServerSocket serverSocket;
    private ServerSocketThread serverSocketThread;
    private Socket selectedSocket;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_server);

        initUI();
        initServerSocketThread();
    }

    private void initUI() {
        initList();

        infoTextView = findViewById(R.id.info_text);
        infoIpTextView = findViewById(R.id.info_ip);
        infoPortTextView = findViewById(R.id.info_port);
        connectServerAndGetImageButton = findViewById(R.id.connect_and_get_image);
        connectServerAndGetImageButton.setOnClickListener(v ->
                openConnectToServerScreen());

        infoIpTextView.setText(getString(R.string.site_local_address, getIpAddress()));
    }

    private void initList() {
        adapter = new ConnectedClientAdapter(this, new ArrayList<>(), connectedClient -> {
            selectedSocket = connectedClient.getSocket();
            chooseImageFromLibrary();
        });

        connectedClientsList = findViewById(R.id.connected_clients_list);
        connectedClientsList.setLayoutManager(new LinearLayoutManager(this));
        connectedClientsList.setAdapter(adapter);
    }

    private void initServerSocketThread() {
        serverSocketThread = new ServerSocketThread();
        serverSocketThread.start();
    }

    private void selectImage() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(Intent.createChooser(intent, null), Constants.REQUEST_CODE_CUSTOM_IMAGE_PICKER);
    }

    private void openConnectToServerScreen() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)
            requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, Constants.REQUEST_CODE_PERMISSION_WRITE_EXTERNAL_STORAGE);
        else
            startActivity(new Intent(ServerActivity.this, ClientActivity.class));
    }

    private void chooseImageFromLibrary() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && ContextCompat.checkSelfPermission(this,
                Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)
            requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, Constants.REQUEST_CODE_PERMISSION_READ_EXTERNAL_STORAGE);
        else
            selectImage();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == Constants.REQUEST_CODE_PERMISSION_READ_EXTERNAL_STORAGE
                && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            selectImage();
        } else if (requestCode == Constants.REQUEST_CODE_PERMISSION_WRITE_EXTERNAL_STORAGE
                && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            startActivity(new Intent(ServerActivity.this, ClientActivity.class));
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && data != null) {
            if (requestCode == Constants.REQUEST_CODE_CUSTOM_IMAGE_PICKER && data.getData() != null) {
                String imagePath = Utils.getRealImagePathFromURI(this, data.getData());
                if (!TextUtils.isEmpty(imagePath)) {
                    File file = new File(imagePath);
                    if (selectedSocket == null)
                        return;

                    if (selectedSocket.isConnected()) {
                        new SendImageAsyncTask(ServerActivity.this, file, selectedSocket).execute();
                    } else {
                        Toast.makeText(this, R.string.closed_server, Toast.LENGTH_LONG).show();
                        initServerSocketThread();
                    }
                }
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (serverSocket != null) {
            try {
                serverSocket.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private String getIpAddress() {
        try {
            Enumeration<NetworkInterface> enumNetworkInterfaces = NetworkInterface.getNetworkInterfaces();
            while (enumNetworkInterfaces.hasMoreElements()) {
                NetworkInterface networkInterface = enumNetworkInterfaces.nextElement();
                Enumeration<InetAddress> enumInetAddress = networkInterface.getInetAddresses();
                while (enumInetAddress.hasMoreElements()) {
                    InetAddress inetAddress = enumInetAddress.nextElement();

                    if (inetAddress.isSiteLocalAddress()) {
                        return inetAddress.getHostAddress();
                    }
                }
            }
        } catch (SocketException e) {
            e.printStackTrace();
        }
        return "Something is wrong with site local address!";
    }

    public class ServerSocketThread extends Thread {

        @Override
        public void run() {

            try {
                serverSocket = new ServerSocket(Constants.SERVER_PORT);
                runOnUiThread(() -> infoPortTextView.setText(getString(R.string.port, serverSocket.getLocalPort())));

                while (true) {
                    Socket socket = serverSocket.accept();
                    if (socket.isConnected() && socket.getInetAddress() != null) {
                        runOnUiThread(() -> {
                            adapter.addItem(new ConnectedClient(socket));
                            infoTextView.setText(getString(R.string.server_information_text));
                        });
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}