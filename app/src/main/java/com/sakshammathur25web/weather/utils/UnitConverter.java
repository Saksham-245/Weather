package com.sakshammathur25web.weather.utils;

import android.content.SharedPreferences;
import androidx.annotation.NonNull;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.Locale;

class UnitConverter {
    public static float convertTemperature(float temperature,@NotNull SharedPreferences sp){
        switch (sp.getString("unit", "C")) {
            case "C":
                return UnitConverter.kelvinToCelsius(temperature);
            case "F":
                return UnitConverter.kelvinToFahrenheit(temperature);
            default:
                return temperature;
        }
    }

    @Contract(pure = true)
    private static float kelvinToCelsius(float kelvinTemp){
        return kelvinTemp - 273.15f;
    }

    @Contract(pure = true)
    private static float kelvinToFahrenheit(float kelvinTemp){
        return (((9*kelvinToCelsius(kelvinTemp))/5)*32);
    }

    @NonNull
    public static String getRainString(double rain, SharedPreferences sp){
        if (rain>0){
            if (sp.getString("lengthUnit","mm").equals("mm")){
                if (rain<0.1){
                    return "(<0.1mm)";
                }else {
                    return String.format(Locale.ENGLISH,"(%.2f %s)",rain,sp.getString("lengthUnit","mm"));
                }
            }else {
                rain = rain/25.4;
                if (rain<0.01){
                    return "(<0.1 in)";
                }else {
                    return String.format(Locale.ENGLISH,"%.2f %s)",rain,sp.getString("lengthUnit","mm"));
                }
            }
        }else {
            return "";
        }
    }

    public static float convertPressure(float pressure,@NotNull SharedPreferences sp){
        switch (sp.getString("pressureUnit", "hPa")) {
            case "kPa":
                return pressure / 10;
            case "mm Hg":
                return (float) (pressure * 0.750061561303);
            case "in Hg":
                return (float) (pressure * 0.0295299830714);
            default:
                return pressure;
        }
    }

    public static double convertWind(double wind,@NotNull SharedPreferences sp){
        switch (sp.getString("speedUnit", "m/s")) {
            case "kph":
                return wind * 3.6;
            case "mph":
                return wind * 2.23693629205;
            case "kn":
                return wind * 1.943844;
            case "bft":
                if (wind < 0.3) {
                    return 0;//Calm
                } else if (wind < 1.5) {
                    return 1;//Light Air
                } else if (wind < 3.3) {
                    return 2;//Light breeze
                } else if (wind < 5.5) {
                    return 3;//Gentle breeze
                } else if (wind < 7.9) {
                    return 4;//Moderate breeze
                } else if (wind < 10.7) {
                    return 5;//Fresh breeze
                } else if (wind < 13.8) {
                    return 6;//Strong breeze
                } else if (wind < 17.1) {
                    return 7;//High wind
                } else if (wind < 20.7) {
                    return 8;//Gale
                } else if (wind < 24.4) {
                    return 9;//Strong gale
                } else if (wind < 28.4) {
                    return 10;//Storm
                } else if (wind < 32.6) {
                    return 11;//Violent storm
                } else {
                    return 12;//Hurricane
                }
            default:
                return wind;
        }
    }

    @NonNull
    @Contract(pure = true)
    public static String getBeaufortName(int wind){
        if (wind==0){
            return "Calm";
        }
        else if (wind==1){
            return "Light air";
        }
        else if (wind==2){
            return "Light breeze";
        }
        else if (wind==3){
            return "Gentle breeze";
        }
        else if (wind==4){
            return "Moderate breeze";
        }
        else if (wind==5){
            return "Fresh breeze";
        }
        else if (wind==6){
            return "Strong breeze";
        }
        else if (wind==7){
            return "High wind";
        }
        else if (wind==8){
            return "Gale";
        }
        else if (wind==9){
            return "Strong gale";
        }
        else if (wind==10){
            return "Storm";
        }
        else if (wind==11){
            return "Violent Storm";
        }
        else {
            return "Hurricane";
        }
    }
}