package com.xstudio.snapclean.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.documentfile.provider.DocumentFile;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.xstudio.snapclean.R;

import java.util.List;

public class SelecionadosFragment extends Fragment {

    //public SelecionadosFragment(){}

    private List<DocumentFile> listaDeExclusao;

    public SelecionadosFragment(List<DocumentFile> listaDeExclusao){
        this.listaDeExclusao = listaDeExclusao;
    }

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

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState){
        super.onViewCreated(view, savedInstanceState);

        RecyclerView recyclerView = view.findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 3));
        recyclerView.setAdapter(new ImagemAdapter(listaDeExclusao));
    }
}
