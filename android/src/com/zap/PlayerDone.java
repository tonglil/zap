package com.zap;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class PlayerDone extends Fragment {
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View playerDone = inflater.inflate(R.layout.fragment_player_done, container, false);
        //((TextView) playerDone.findViewById(R.id.textView1)).setText("Done?");
        return playerDone;
    }
}