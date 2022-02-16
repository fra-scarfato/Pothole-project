package com.example.potholes.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.potholes.R;
import com.example.potholes.entities.Hole;

import java.util.ArrayList;

//TODO:Interfaccia grafica e logica
public class ViewHoleFragment extends Fragment {
    ArrayList<Hole> holeArrayList;

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

    }


    private void setUpComponents(){
        holeArrayList = new ArrayList<>();
        Intent intent = getActivity().getIntent();
        if (intent != null){
            holeArrayList = (ArrayList<Hole>) intent.getSerializableExtra("holes");
        }
    }
}