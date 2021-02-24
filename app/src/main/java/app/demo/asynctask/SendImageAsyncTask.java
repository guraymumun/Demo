package app.demo.asynctask;

import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.ObjectOutputStream;
import java.lang.ref.WeakReference;
import java.net.Socket;


public class SendImageAsyncTask extends AsyncTask<Void, Void, Void> {

    private final WeakReference<AppCompatActivity> activityReference;
    private ProgressDialog progress;

    private final Socket socket;
    private final File imageFile;

    public SendImageAsyncTask(AppCompatActivity activity, File imageFile, Socket socket) {
        this.activityReference = new WeakReference<>(activity);
        this.imageFile = imageFile;
        this.socket = socket;
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
        byte[] bytes = new byte[(int) imageFile.length()];
        BufferedInputStream bis;
        try {
            bis = new BufferedInputStream(new FileInputStream(imageFile));
            bis.read(bytes, 0, bytes.length);

            ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());
            oos.writeObject(bytes);
            oos.flush();

            final String sentMsg = "File was sent to: " + socket.getInetAddress();
            activity.runOnUiThread(() -> Toast.makeText(activity,
                    sentMsg,
                    Toast.LENGTH_LONG).show());

        } catch (Exception e) {
            e.printStackTrace();
        }

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
