package co.com.puli.trade.fdv.adaptadores;


import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import co.com.puli.trade.fdv.R;
import co.com.puli.trade.fdv.clases.UsuarioChat;
import co.com.puli.trade.fdv.actividades.ChatActivity;

import java.util.ArrayList;

/**
 * Created by carlos on 17/02/16.
 */
public class ChatUsuariosFragment extends Fragment
{
    public static final String ARG_PAGE = "ARG_PAGE";
    private int mPage;
    private static ArrayList<UsuarioChat> usuarios;
    private static String id_usuario;

    public static ChatUsuariosFragment newInstance(int page, ArrayList<UsuarioChat> usuarios_chat, String id_user)
    {
        Bundle args = new Bundle();
        args.putInt(ARG_PAGE, page);
        ChatUsuariosFragment fragment = new ChatUsuariosFragment();
        fragment.setArguments(args);
        usuarios = usuarios_chat;
        id_usuario = id_user;
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mPage = getArguments().getInt( ARG_PAGE );
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        View view = inflater.inflate( R.layout.view_pager_chat_usuarios, container, false);
        ChatUsuariosListaAdapter adaptador = new ChatUsuariosListaAdapter( (Activity) getContext(), getUsuariosFiltro() );
        final ListView lista = (ListView) view.findViewById( R.id.lvUsuarios );
        lista.setAdapter(adaptador);
        lista.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id)
            {
                UsuarioChat user = (UsuarioChat) lista.getItemAtPosition( position );
                Intent intent = new Intent(getActivity(), ChatActivity.class);
                intent.putExtra( "id_usuario", id_usuario );
                intent.putExtra( "id_destino", ""+id );
                intent.putExtra( "usuario_chat", user.getNombre() );
                startActivity(intent );
            }
        });
        return view;
    }

    /**
     * Método encargado de validar si el perfil es válido para agregar al listado de usuarios
     * a partir de la pagina.
     * El perfil 2 los agrupa como padres y cualquier otro perfil como administradores
     * @param perfil ID del perfil del usuario
     * */
    private boolean perfilValido( int perfil )
    {
        boolean valido = false;
        switch( mPage )
        {
            case 1://Vendedores
                    valido = perfil == 7 ? true : false;
                break;
            case 2: //Coordiandores
                    valido = perfil == 9 ? true : false;
                break;
        }
        return valido;
    }

    /**
     * Método encargado de retornar los usuarios especificos para la pestaña a partir del numero de la pagina
     * */
    private ArrayList<UsuarioChat> getUsuariosFiltro()
    {
        ArrayList<UsuarioChat> usuariosFiltro = new ArrayList<>();
        for( UsuarioChat user : usuarios )
        {
            if( perfilValido( user.getId_perfil() ) )
            {
                usuariosFiltro.add( user );
            }
        }

        return usuariosFiltro;
    }
}
