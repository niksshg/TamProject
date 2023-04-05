package com.masterproject.fittam.FitBud;

import android.content.Context;

import androidx.annotation.CheckResult;

import pl.droidsonroids.gif.GifImageView;

public class FitBudModel {
    /**
     * This class contains the tamagotchi object. This is not needed as all the data is contained in
     * SharedPreferences.
     */


    private  int state;
    private GifImageView look2;
    private Context context;
    private int happines;



    public FitBudModel( GifImageView look2, int happiness, int state, Context context) {
        this.state = state;
        this.look2 = look2;
        this.context = context;
        this.happines =happiness;

    }
    public void setHappines(int happines){
        happines = happines;
    }
    public int getHappines(){
        return happines;
    }


    public void setLook(GifImageView look2) {
        this.look2 = look2;
    }
    public void setState(int state) {
        this.state = state;
    }
    public GifImageView getLook() {
        return look2;
    }

    @CheckResult
    public int getState() {
        return state;
    }


}
