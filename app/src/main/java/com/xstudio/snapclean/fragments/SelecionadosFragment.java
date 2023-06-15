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
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
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
                /*
                layoutPrincipal.setVisibility(View.VISIBLE);
                requireActivity().getSupportFragmentManager().popBackStack();
                 */
                layoutPrincipal.setVisibility(View.VISIBLE);
                FragmentManager fragmentManager = requireActivity().getSupportFragmentManager();
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                fragmentTransaction.remove(SelecionadosFragment.this);
                fragmentTransaction.commit();
                System.out.println("Botao voltar foi clicado");
            }
        });

        return rootView;
    }

    private ImagemAdapter adapter;
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState){
        super.onViewCreated(view, savedInstanceState);

        RecyclerView recyclerView = view.findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 3));
        adapter = new ImagemAdapter(listaDeExclusao);
        recyclerView.setAdapter(adapter);
    }

    public void atualizarListaDeExclusao(List<DocumentFile> listaDeExclusao){
        this.listaDeExclusao = listaDeExclusao;
        adapter.notifyDataSetChanged();
    }
}
