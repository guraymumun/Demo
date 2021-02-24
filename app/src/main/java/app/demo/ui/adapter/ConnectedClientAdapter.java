package app.demo.ui.adapter;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

import app.demo.R;
import app.demo.model.ConnectedClient;

public class ConnectedClientAdapter extends RecyclerView.Adapter<ConnectedClientAdapter.ViewHolder> {

    private ArrayList<ConnectedClient> items;
    private Context context;
    private LayoutInflater inflater;
    private OnConnectedClientClickListener listener;

    public ConnectedClientAdapter(Context context, ArrayList<ConnectedClient> items, OnConnectedClientClickListener listener) {
        inflater = ((Activity) context).getLayoutInflater();
        this.context = context;
        this.items = items;
        this.listener = listener;
    }

    private boolean contains(ConnectedClient item) {
        for (ConnectedClient client : items) {
            if (item.getSocket().getInetAddress().equals(client.getSocket().getInetAddress())) {
                items.remove(client);
                this.notifyDataSetChanged();
                return true;
            }
        }
        return false;
    }

    public void addItem(ConnectedClient item) {
        if (!contains(item))
            Toast.makeText(context, item.getSocket().getInetAddress() + " is connected to your server.", Toast.LENGTH_LONG).show();
        items.add(0, item);
        this.notifyItemInserted(0);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.item_connected_client, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {
        ConnectedClient item = items.get(holder.getAdapterPosition());

        holder.text.setText(item.getSocket().getInetAddress().toString());

        holder.itemView.setOnClickListener(v -> {
            if (listener != null)
                listener.onClick(item);
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView text;

        ViewHolder(View itemView) {
            super(itemView);
            text = itemView.findViewById(R.id.connected_client_address);
        }
    }

    public interface OnConnectedClientClickListener {
        void onClick(ConnectedClient connectedClient);
    }
}
