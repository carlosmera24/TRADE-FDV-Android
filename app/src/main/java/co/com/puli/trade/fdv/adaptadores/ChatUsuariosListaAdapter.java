package co.com.puli.trade.fdv.adaptadores;

import android.app.Activity;
import androidx.core.content.ContextCompat;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import co.com.puli.trade.fdv.R;
import co.com.puli.trade.fdv.clases.CustomFonts;
import co.com.puli.trade.fdv.clases.UsuarioChat;

import java.util.ArrayList;

/**
 * Adaptador para el listado de usuarios correspondiente al ListView
 * Created by carlos on 18/02/16.
 */
public class ChatUsuariosListaAdapter extends BaseAdapter
{
    private Activity actividad;
    private ArrayList<UsuarioChat> usuarios;
    private CustomFonts fuentes;

    public ChatUsuariosListaAdapter(Activity actividad, ArrayList<UsuarioChat> usuarios)
    {
        this.actividad = actividad;
        this.usuarios = usuarios;
        fuentes = new CustomFonts( actividad.getAssets() );
    }
    @Override
    public int getCount() {
        return usuarios.size();
    }

    @Override
    public Object getItem(int position) {
        return usuarios.get( position);
    }

    @Override
    public long getItemId(int position)
    {
        UsuarioChat usuario = usuarios.get( position );
        return usuario.getId_usuario();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent)
    {
        View view = convertView;
        if( view == null )
        {
            view = actividad.getLayoutInflater().inflate( android.R.layout.simple_list_item_1, null);
        }
        UsuarioChat usuario = usuarios.get( position );
        TextView txt = (TextView) view.findViewById( android.R.id.text1 );
        txt.setText( usuario.getNombre().toUpperCase() );
        txt.setTextColor(  ContextCompat.getColor( actividad, R.color.black) );
        txt.setTypeface( fuentes.getRobotoThinFont() );
        return view;
    }
}
