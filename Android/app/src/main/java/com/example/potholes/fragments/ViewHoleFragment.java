package com.example.potholes.fragments;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.potholes.R;
import com.example.potholes.adapter.RecyclerAdapter;
import com.example.potholes.entities.Hole;

import java.util.ArrayList;

//TODO:Interfaccia grafica e logica
public class ViewHoleFragment extends Fragment {

    private ArrayList<Hole> holeArrayList;
    private Context mContext;
    private RecyclerView recyclerView;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_view_hole, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setUpComponents();
        setUpViewComponents(view);



    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mContext = context;
    }


    private void setUpViewComponents(View view){
        recyclerView = view.findViewById(R.id.recyclerview_ViewHole);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(mContext));
    }

    private void setUpComponents(){
        holeArrayList = new ArrayList<>();
        Bundle bundle = getActivity().getIntent().getExtras();
        if (bundle != null){
            holeArrayList = (ArrayList<Hole>) bundle.getSerializable("holes");
            Log.e("HolesArray",holeArrayList.toString());
            RecyclerAdapter recyclerAdapter = new RecyclerAdapter(holeArrayList);
            recyclerView.setItemAnimator(new DefaultItemAnimator());
            recyclerView.setAdapter(recyclerAdapter);
        }
    }

}