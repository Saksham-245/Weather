package com.sakshammathur25web.weather.fragments;


import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.sakshammathur25web.weather.R;
import com.sakshammathur25web.weather.activities.MainActivity;

public class RecyclerViewFragment extends androidx.fragment.app.Fragment {


    public RecyclerViewFragment() {
    }


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Bundle bundle = this.getArguments();
        View view = inflater.inflate(R.layout.fragment_recycler_view, container, false);
        RecyclerView recyclerView = view.findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new androidx.recyclerview.widget.LinearLayoutManager(getContext()));
        MainActivity mainActivity = (MainActivity) getActivity();
        assert bundle != null;
        assert mainActivity != null;
        recyclerView.setAdapter(mainActivity.getAdapter(bundle.getInt("day")));
        return view;
    }

}
