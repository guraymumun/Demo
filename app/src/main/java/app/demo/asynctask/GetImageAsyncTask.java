package app.demo.asynctask;

import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Environment;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.io.File;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.lang.ref.WeakReference;
import java.net.Socket;


public class GetImageAsyncTask extends AsyncTask<Void, Void, Void> {

    private final WeakReference<AppCompatActivity> activityReference;
    private ProgressDialog progress;

    String destinationAddress;
    int destinationPort;

    public GetImageAsyncTask(AppCompatActivity activity, String destinationAddress, int destinationPort) {
        this.activityReference = new WeakReference<>(activity);
        this.destinationAddress = destinationAddress;
        this.destinationPort = destinationPort;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        progress = new ProgressDialog(activityReference.get());
        progress.setMessage("Please wait...");
        progress.show();
    }

    @Override
    protected Void doInBackground(Void... voids) {
        AppCompatActivity activity = activityReference.get();
        try {
            Socket socket = new Socket(destinationAddress, destinationPort);

            File file = new File(
                    Environment.getExternalStorageDirectory(),
                    System.currentTimeMillis() + ".png");

            ObjectInputStream ois = new ObjectInputStream(socket.getInputStream());
            byte[] bytes;
            FileOutputStream fos;
            try {
                bytes = (byte[]) ois.readObject();
                fos = new FileOutputStream(file);
                fos.write(bytes);
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }

            activity.runOnUiThread(() -> Toast.makeText(activity,
                    "Finished",
                    Toast.LENGTH_LONG).show());
        } catch (Exception e) {
            e.printStackTrace();

            final String message = "Something is wrong: " + e.getMessage();
            activity.runOnUiThread(() -> {
                if (progress != null && progress.isShowing())
                    progress.dismiss();
                Toast.makeText(activity, message, Toast.LENGTH_LONG).show();
            });
            return null;
        }

        activity.runOnUiThread(() -> new GetImageAsyncTask(
                activity,
                destinationAddress,
                destinationPort).execute());

        return null;
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        super.onPostExecute(aVoid);
        if (progress != null && progress.isShowing())
            progress.dismiss();
    }

    @Override
    protected void onCancelled() {
        super.onCancelled();
        if (progress != null && progress.isShowing())
            progress.dismiss();
    }
}
