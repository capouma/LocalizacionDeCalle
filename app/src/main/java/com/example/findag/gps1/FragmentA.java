package com.example.findag.gps1;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

/**
 * Created by findag on 26/03/15.
 */
public class FragmentA extends Fragment
{
    public FragmentA()
    {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState)
    {
        // Lo que hacemos en el fragment s simplemente recoger el texto que tenemos en el txvsaludo y mostrarlo
        // Esto nos vale para dar un aviso a la parsona que pulso que el mensaje fue enviado.
        View rootView = inflater.inflate(R.layout.fragment_a, container, false);
        TextView saludo = (TextView) rootView.findViewById(R.id.txvEnviado);
        saludo.getText().toString();


        return rootView;
    }
}
