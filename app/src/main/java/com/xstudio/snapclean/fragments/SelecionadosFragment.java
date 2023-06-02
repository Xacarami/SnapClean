package com.xstudio.snapclean.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.xstudio.snapclean.R;

public class SelecionadosFragment extends Fragment {
    public SelecionadosFragment(){}
    private SelecionadosViewModel viewModel;



    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        viewModel = new ViewModelProvider(this).get(SelecionadosViewModel.class);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
        //inflar o layout do fragment
        View rootView = inflater.inflate(R.layout.fragment_selecionados, container, false);

        ConstraintLayout layoutPrincipal = getActivity().findViewById(R.id.layout_principal);
        ImageButton voltarSelecionados = rootView.findViewById(R.id.voltar_selecionados);
        voltarSelecionados.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                layoutPrincipal.setVisibility(View.VISIBLE);
                requireActivity().getSupportFragmentManager().popBackStack();
            }
        });

        return rootView;
    }
}
