package com.sakshammathur25web.weather.models;

import androidx.recyclerview.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.sakshammathur25web.weather.R;

public class WeatherViewHolder extends RecyclerView.ViewHolder {
    public final TextView itemDate;
    public final TextView itemTemperature;
    public final TextView itemDescription;
    public final TextView itemyWind;
    public final TextView itemPressure;
    public final TextView itemHumidity;
    public final TextView itemIcon;

    @SuppressWarnings("unused")
    public WeatherViewHolder(View view) {
        super(view);
        this.itemDate = view.findViewById(R.id.itemDate);
        this.itemTemperature = view.findViewById(R.id.itemTemperature);
        this.itemDescription = view.findViewById(R.id.itemDescription);
        this.itemyWind = view.findViewById(R.id.itemWind);
        this.itemPressure = view.findViewById(R.id.itemPressure);
        this.itemHumidity = view.findViewById(R.id.itemHumidity);
        this.itemIcon = view.findViewById(R.id.icon);
        View lineView = view.findViewById(R.id.lineView);
    }
}