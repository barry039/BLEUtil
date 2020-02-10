package com.barry.bleutil;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class BleListAdapter extends RecyclerView.Adapter<BleListAdapter.ViewHolder> {

    private Context context;

    private LayoutInflater layoutInflater;

    private List<BluetoothDeviceInfoData> bluetoothDeviceInfoDataList = new ArrayList<>();

    private BLEClickListener bleClickListener;

    public BleListAdapter(Context context,BLEClickListener bleClickListener) {
        this.bleClickListener = bleClickListener;
        this.context = context;
        layoutInflater = LayoutInflater.from(context);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = layoutInflater.inflate(R.layout.list_item,parent,false);
        ViewHolder viewHolder = new ViewHolder(view);

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, final int position) {

        viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                bleClickListener.onClicked(bluetoothDeviceInfoDataList.get(position));
            }
        });

        viewHolder.name.setText(bluetoothDeviceInfoDataList.get(position).getBluetoothDevice().getName());
        if(viewHolder.name.getText().toString().length() > 5)
        {
            String divideName = viewHolder.name.getText().toString().substring(0,5);
            viewHolder.name.setText(divideName);
        }
        viewHolder.address.setText(bluetoothDeviceInfoDataList.get(position).getBluetoothDevice().getAddress());
        viewHolder.rssi.setText(String.valueOf(bluetoothDeviceInfoDataList.get(position).getRssi()));
    }

    @Override
    public int getItemCount() {
        return bluetoothDeviceInfoDataList.size();
    }

    public void setDataSet(List<BluetoothDeviceInfoData> bluetoothDeviceInfoDataList)
    {
        this.bluetoothDeviceInfoDataList = bluetoothDeviceInfoDataList;
        notifyDataSetChanged();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{
        private TextView name,address,rssi;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.list_item_name);
            address = itemView.findViewById(R.id.list_item_address);
            rssi = itemView.findViewById(R.id.list_item_rssi);
        }
    }

    public interface BLEClickListener
    {
        void onClicked(BluetoothDeviceInfoData bluetoothDeviceInfoData);
    }
}
